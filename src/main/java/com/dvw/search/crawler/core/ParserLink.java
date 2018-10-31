package com.dvw.search.crawler.core;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.SeedInfo;
import com.dvw.search.crawler.parser.ParserCtrl;
import com.dvw.search.crawler.util.PageCharsetUtil;
import com.dvw.search.crawler.util.PropertiesUtil;
import com.dvw.search.crawler.util.UrlCheckUtil;


/**
 * 网页解析副主线程，该对象会派生出更多线程进行页面解析
 * @author David.Wang
 *
 */
public class ParserLink implements Runnable{

	private Log log=LogFactory.getLog(ParserLink.class);
	//种子列表
	private List<SeedInfo> seeds;
	private ParserCtrl parserCtrl;
	//链接列表
	private Vector<HttpLink> links;
	private DBOperation dbo=new DBOperation();
	//是否忽略新增链接
	private boolean ignoreLink=PropertiesUtil.isIgnoreLink();
	//未解析页面为多少时忽略新增链接
	private int ignoreLinkNumber=PropertiesUtil.getIgnoreLinkNumber();
	public static HashSet<Integer>[] hashLinks;	
	SeedInfo seedInfo;
	
	public ParserLink(List<SeedInfo> seeds,HashSet<Integer>[] hashLinkss){
		this.seeds=seeds;
		hashLinks=hashLinkss;
	}
	
	public SeedInfo getSeedInfo(){
		synchronized(ParserLink.class){
			if(!seeds.isEmpty())
				return seeds.remove(0);
			else
				return null;
		}
	}
	
	public void run() {
		//初始化线程
		Thread[] threads=new Thread[PropertiesUtil.getMaxThread()];
		parserCtrl=new ParserCtrl();
		while((seedInfo=getSeedInfo())!=null){
			
			//站点入口
			String seed=seedInfo.getSeed();
			//站点链接所存储的table号http_$tableNumber
			int tableNumber=seedInfo.getId()%10;
			//获取种子主域名，根据站点是否为wap站点，采用不同策略
			String domain=null;
			if(!PropertiesUtil.isWap()){
				domain=UrlCheckUtil.getSeedDomain(seed);
			}
			else{
				domain=UrlCheckUtil.getWapDomain(seed,seedInfo.getDomain());
			}
			
			//创建链接HashSet,用于内存中的链接排重			
			hashLinks[tableNumber]=DataSource.getHashLink(tableNumber,hashLinks[tableNumber],domain);
			HashSet<Integer> hashLink=hashLinks[tableNumber];
			seed="http://"+seed;
			//获取终于对应站点的字符集
			String charset=PageCharsetUtil.getCharset(seed);
			
			//如果种子无法判断出字符集，很有可能站点已经关闭，就算未关闭搜索上的数据也会因为字符集问题产生乱码，所以不再解析该种子
			if(charset==null)
			{
				//种子解析失败，种子task_id设置为-1
				DataSource.updateSeedInfo("taskId",-1,seedInfo.getSeed());
				//该种子解析完毕，siteState状态值减1，用于爬虫自动关闭
				CrawlerMain.siteState--;
				continue;
			}
			
			//SeedInfo对象属性赋值
			seedInfo.setDomain(domain);
			seedInfo.setCharset(charset);
			seedInfo.setTableNumber(tableNumber);
			seedInfo.setHashLink(hashLink);
			//如果运行状态为1，则表示重新开始搜索，起始路径为种子,如果task_id=0说明该站点没有被抓取过,-1说明站点之前解析异常
			if(CrawlerMain.taskInfo.getStatus()==1||seedInfo.getTaskId()==0)
			{
				try {
					HttpLink link=new HttpLink(seed,0);
					parserCtrl.parserLink(link,seedInfo,false,true);
					//如果配置文件searchImage==TRUE，搜索图片
					if(PropertiesUtil.isSearchImage())
						parserCtrl.parserImage(link, seedInfo);
					log.fatal("已经进入种子"+seed+"的第一层,更改种子状态~");
					DataSource.updateSeedInfo("taskId",1,seedInfo.getSeed());
					//设置该变量内存中数值
					seedInfo.setLayer(0);
				} catch (Exception e) {
		//			e.printStackTrace();
					log.fatal("网页解析错误："+seed);
				}
			}

			for(int i=seedInfo.getLayer();i<=PropertiesUtil.getMaxLayer();i++)
			{
				//将状态为0的链接设置为1，0--未解析，1--正在解析，2--解析完毕
				//这个条件限制是为了防止断点续搜的时候产生的还有未解析状态的链接转变成为正在解析状态以及由此产生的不合理的层数增加
				int parsingCount = dbo.getTotalHttpStatusCount(1,domain,tableNumber);
				if((parsingCount==0) || (parsingCount<=PropertiesUtil.getMaxThread()&&dbo.getTotalHttpStatusCount(0,domain,tableNumber)>=PropertiesUtil.getMaxThread()))
				{
					dbo.updateTotalHttpStatus(1,domain,tableNumber);
					DataSource.updateSeedInfo("layer", i+1 ,seedInfo.getSeed());
					log.fatal("进入下一层");
				}
				
				if(ignoreLink==true)
				{
//					if((dbo.getTotalHttpStatusCount(0, domain, tableNumber)+dbo.getTotalHttpStatusCount(1, domain, tableNumber))<ignoreLinkNumber)
					if(hashLink.size()<ignoreLinkNumber)
						ignoreLink=false;
					else
					{
						ignoreLink=true;
						log.fatal("忽略链接~");
					}						
				}
				while((links=dbo.getLinks(1,domain,tableNumber)).size()!=0)					
				{
					if(links.size()<PropertiesUtil.getMaxThread()&&dbo.getTotalHttpStatusCount(0, domain, tableNumber)>=PropertiesUtil.getMaxThread())
						break;
					for(int j=0;j<PropertiesUtil.getMaxThread();j++)
					{
						threads[j]=new ParserLinkThread(links,seedInfo,ignoreLink);
					}
					for(int j=0;j<PropertiesUtil.getMaxThread();j++)
					{
						threads[j].start();
					}
					
					while(true)
					{	
						try {
							Thread.sleep(60*1000*5);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						int deadCount=0;
						for(int j=0;j<PropertiesUtil.getMaxThread();j++)
						{
							if(!threads[j].isAlive())
								deadCount++;
						}

						if(deadCount>0)
						{
//							System.out.println("程序疑似跑死，需重新创造线程  ===========================================");
							for(int j=0;j<PropertiesUtil.getMaxThread();j++)
							{
								if(threads[j].isAlive())
								{
									threads[j].stop();
									threads[j]=null;
								}
									
							}
							try {
								Thread.sleep(10*1000);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							break;
						}
						
					}
					
				}
//				if(links.size()==0)
//					continue;				
			}
			dbo.updateTotalHttpStatus(1,domain,tableNumber);
			//该种子解析完毕，siteState状态值减1，用于爬虫自动关闭
			CrawlerMain.siteState--;
			//站点运行状态置为2，表示站点在本轮已经爬行完毕
			DataSource.updateSeedInfo("taskId",2,seedInfo.getSeed());
			//丢弃旧有的链接hash内存存储空间
			hashLinks[tableNumber].clear();
		}
	}

	
}
