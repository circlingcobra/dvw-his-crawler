package com.dvw.search.crawler.core;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.assistant.RecordSiteInfo;
import com.dvw.search.crawler.assistant.TaskInfoThread;
import com.dvw.search.crawler.assistant.TimeClock;
import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.FileInfo;
import com.dvw.search.crawler.domain.SeedInfo;
import com.dvw.search.crawler.domain.TaskInfo;
import com.dvw.search.crawler.util.ConnectionUtil;
import com.dvw.search.crawler.util.DateUtil;
import com.dvw.search.crawler.util.PropertiesUtil;



/**
 * 程序运行主函数
 * @author David.Wang
 *
 */
public class CrawlerMain {

	private static Log log=LogFactory.getLog(CrawlerMain.class);
	//节目后缀关键字列表
	private static List<FileInfo> suffixKeywords;
	//种子列表
	private static List<SeedInfo> seeds;
	//域名范围列表
	private static List<String> programAreas;
	//该状态随着各种子的搜索完成递减,直至变为0
	public static int siteState;
	//t_task_info表，控制是否进行扩展搜索和断点续搜
	public static TaskInfo taskInfo;
	//爬虫运行起始时间
	public static long time;
	//链接排重HashSet
	public static HashSet<Integer>[] hashLinks; 
	//节目排重HashSet
	public static HashSet<Integer> hashProgram;
	//数据初始化
	static{
		//DNS 域名缓存时间设置30秒
		java.security.Security.setProperty("networkaddress.cache.ttl", "30");
		taskInfo=DataSource.getTaskInfo();
		//如果taskInfo状态为1，则清空所有数据，为2则不清空数据，进行断点续搜
		if(taskInfo.getStatus()==1)
		{
			//删除数据库中上一轮的totalhttp表中的数据
			DBOperation.deleteTotalHttp();
			//删除数据库中上一轮的t_result_file表中的数据
			DBOperation.deleteResultFile();
			//删除数据库中上一轮的t_result_site表中的数据
			DBOperation.deleteResultSite();
		}	
		if(taskInfo.getStatus()!=1&&taskInfo.getStatus()!=2)
		{
			log.fatal("t_task_info表status状态位异常");
			System.exit(0);
		}
		if(taskInfo.getSiteArea()!=1&&taskInfo.getSiteArea()!=0)
		{
			log.fatal("t_task_info表site_area状态位异常");
			System.exit(0);
		}
		
		//创建链接排重Hash对象
		hashLinks= new HashSet[10];
		for(int i=0;i<hashLinks.length;i++)
		{
			hashLinks[i]=new HashSet<Integer>();
		}
		//创建节目排重Hash对象
		hashProgram = new HashSet<Integer>();
		hashProgram = DataSource.getHashProgram(hashProgram);
		//获取t_fileinfo表中的节目后缀名称
		suffixKeywords=DataSource.getSuffixKeywords();
		//获取t_seedsite表中的种子
		seeds=DataSource.getSeeds();
		//获取节目范围表t_site_area中的域名范围
		programAreas=DataSource.getProgramAreas();
		//获取种子数
		siteState=DataSource.getSeedNum();
		//修改t_task_info表数据
		DataSource.updateTaskInfo("start", taskInfo.getStatus());
		
		if(taskInfo.getStatus()==1)
			time=System.currentTimeMillis();
		else{
			try {
				log.fatal("start时间："+taskInfo.getStartTime());
				time=DateUtil.String2Date(taskInfo.getStartTime()).getTime();
				log.fatal("开始运行时间："+DateUtil.String2Date(taskInfo.getStartTime()));
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
//		//代理设置
		if(PropertiesUtil.isProxy()==true)
		{
			 log.fatal("开始设置代理----Host:"+PropertiesUtil.getProxyHost()+"\t\tPort:"+PropertiesUtil.getProxyPort());
			 Properties pro=System.getProperties();
			 pro.put("http.proxyHost", PropertiesUtil.getProxyHost());
			 pro.put("http.proxyPort", String.valueOf(PropertiesUtil.getProxyPort()));
			 if(PropertiesUtil.getUsername()!=null&&PropertiesUtil.getUsername().length()!=0)
			 {
				 log.fatal("设置代理用户----User:"+PropertiesUtil.getProxyUser()+"\t\tPassword:"+PropertiesUtil.getProxyPassword());
				 pro.put("http.proxyUser", PropertiesUtil.getProxyUser());
				 pro.put("http.proxyPassword", PropertiesUtil.getProxyPassword());
			 }
		}
	}
	

	public static void main(String[] args) throws InterruptedException, SQLException
	{
		//初始化种子线程
		Thread[] threads=new Thread[PropertiesUtil.getSiteThread()];
		for(int i=0;i<PropertiesUtil.getSiteThread();i++)
		{
			threads[i]=new Thread(new ParserLink(seeds,hashLinks));
		}
		
		for(int i=0;i<PropertiesUtil.getSiteThread();i++)
		{
			threads[i].start();
			log.info("站点线程："+i+" 开始运行");

		}

		//时钟线程，用于控制程序停止的时间
		Thread timeClock=new Thread(new TimeClock());
		timeClock.start();
		
		//初始化RecordSiteInfo,该类在一定时间间隔统计爬虫运行总体信息
		Thread siteOp=new Thread(new RecordSiteInfo());
		siteOp.start();
		
		//TaskInfo统计线程
		Thread taskInfoThread= new Thread(new TaskInfoThread());
		taskInfoThread.start();
		
		//如果siteState字段大于0，说明爬虫依然在运行，如果小于等于0，说明爬虫搜索线程都运行结束或已到搜索最大时间
		while(siteState>0)
		{
			Thread.sleep(60000*2);
//			System.out.println("状态检测完毕------------------------------------------");
		}
		//程序运行结束，修改t_task_info表状态值
		DataSource.updateTaskInfo("end", 3);
		//关闭连接池
		ConnectionUtil.getBasicDataSource().close();
		log.fatal("爬虫正常结束~");
		System.exit(0);
	}
}
