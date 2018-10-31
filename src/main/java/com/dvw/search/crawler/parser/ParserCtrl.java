package com.dvw.search.crawler.parser;


import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.dvw.search.crawler.assistant.JudgeCondition;
import com.dvw.search.crawler.core.CrawlerMain;
import com.dvw.search.crawler.core.FirstPageThread;
import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.FileInfo;
import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.RegExp;
import com.dvw.search.crawler.domain.ResultFile;
import com.dvw.search.crawler.domain.SeedInfo;
import com.dvw.search.crawler.util.PageCharsetUtil;
import com.dvw.search.crawler.util.ParserFactory;
import com.dvw.search.crawler.util.PropertiesUtil;

import sun.misc.BASE64Decoder;


/**
 * url解析类，核心类
 * @author David.Wang
 *
 */
public class ParserCtrl {

	private Log log=LogFactory.getLog(this.getClass());
	//解析类
	private ParserPage parserPage=ParserFactory.getParserPage();
	private DBOperation dbo=new DBOperation();
	Set<ProgramTotal> programTotals;
	//正则表达式集合
	private List<RegExp> regExps=DataSource.getRegExps();
	//网页关键词集合
	private List<String> keywords=DataSource.getKeywords();
	//节目域名范围
	private List<String> programAreas=DataSource.getProgramAreas();;
    //url链接后缀关键字集合
	private List<FileInfo> suffixKeywords=DataSource.getSuffixKeywords();
	/**
	 * 解析链接，获取链接url、链接对应图片、名称，并将链接集合入库
	 */
	public void parserLink(HttpLink link,SeedInfo seedInfo,boolean ignoreLink,boolean isFirst) throws Exception
	{
		//站点是否使用入口字符集
		if(seedInfo.getSubCharset()==0)
		{
			programTotals=parserPage.parserLink(link, seedInfo.getCharset(), seedInfo.getDomain(),seedInfo.getRegFilter());	
		}
		else
		{
			String charset=PageCharsetUtil.getCharsetInMemo(link.getLink());
			if(charset!=null)
			{
				programTotals=parserPage.parserLink(link, charset, seedInfo.getDomain(),seedInfo.getRegFilter());	
			}
			else
			{
				programTotals=parserPage.parserLink(link, seedInfo.getCharset(), seedInfo.getDomain(),seedInfo.getRegFilter());	
			}
		}
			
		if(isFirst)
		{
			executeLinks(programTotals, seedInfo.getTableNumber(), seedInfo.getHashLink());
		}			
		else
		{
			executeLinks(programTotals, seedInfo.getTableNumber(), seedInfo.getHashLink(),ignoreLink);
		}
			
		programTotals.clear();
		
	}
	
	
	/**
	 * 解析链接,获取对应图片，并将图片集合入库
	 */
	public void parserImage(HttpLink link,SeedInfo seedInfo) throws Exception
	{
		programTotals=parserPage.parserImage(link, seedInfo.getCharset(), seedInfo.getDomain());
		executeImages(programTotals, seedInfo.getTableNumber(), seedInfo.getHashLink());			
		programTotals.clear();		
	}
	
	/**
	 * 处理图片链接
	 */
	private void executeImages(Set<ProgramTotal> programTotals,int tableNumber,HashSet<Integer> hashLink){

		Iterator it=programTotals.iterator();
		while(it.hasNext())
		{		
			int fileinfo_id=4200;
			ProgramTotal pt=(ProgramTotal) it.next();

			//链接是否有效，如果该节目的父页面不在域名范围内则判断无效
			boolean isEffect=false;
			
			if(CrawlerMain.taskInfo.getSiteArea()==1)
			{
				//对链接和节目有效性进行判断
				for(String area:programAreas)
				{
					if(pt.getSite().contains(area))
					{
						isEffect=true;
					}
				}
			}else{
				isEffect=true;
			}
			
			//如果链接无效，则继续
			if(!isEffect||pt.getFile_url().length()>128||pt.getPage_url().length()>128||!pt.getFile_url().contains("http://")||pt.getFile_url().contains("(")||pt.getFile_url().contains("\""))
				continue;
			
			//判断该数据是否已经插入
			if(!JudgeCondition.notRepeat(pt.getFile_url(),CrawlerMain.hashProgram))       
			{
				log.fatal("hashProgram判定图片重复："+pt.getFile_url());
				continue;
			}else
			{
				CrawlerMain.hashProgram.add(pt.getFile_url().hashCode());
			}
			
			dbo.insertResultFile(pt,fileinfo_id,tableNumber);			
		}
	}
	
	
	/**
	 * 处理网页解析出的链接和节目，处理站点首页
	 * @throws SQLException 
	 */
	private void executeLinks(Set<ProgramTotal> programTotals,int tableNumber,HashSet<Integer> hashLink) {
		//存放首页后缀匹配节目
		Set<ProgramTotal> program1=new HashSet<ProgramTotal>();
		//存放首页正则匹配节目
		Set<ProgramTotal> program2=new HashSet<ProgramTotal>();
		//存放首页关键字匹配
		Set<ProgramTotal> program3=new HashSet<ProgramTotal>();
		//存放链接，批量插入连接表
		Set<ProgramTotal> httpLinks=new HashSet<ProgramTotal>();
	
		Iterator it=programTotals.iterator();
		while(it.hasNext())
		{
			int fileinfo_id=0;
			ProgramTotal pt=(ProgramTotal) it.next();
			//处理相对路径
//				if(!pt.getFile_url().contains("http")&&pt.getFile_url().contains("/"))
//				{
//					pt.setFile_url("http://"+pt.getSite()+pt.getFile_url());
//					System.out.println("相对路径："+pt.getFile_url());
//				}
			//链接是否有效，如果该节目的父页面不在域名范围内则判断无效
			boolean isEffect=false;
			//判断链接是否可以作为链接路径集合，如果节目url不在域名范围内则判断无效
			boolean linkEffect=false;
			
			if(CrawlerMain.taskInfo.getSiteArea()==1)
			{
				//对链接和节目有效性进行判断
				for(String area:programAreas)
				{
					if(pt.getSite().contains(area))
					{
						isEffect=true;
						if(pt.getFile_url().contains(area))
							linkEffect=true;
						break;
//							continue;
					}
				}
			}else{
				isEffect=true;
				linkEffect=true;
			}
			log.fatal(pt.getFile_url()+"\t\t层数:"+pt.getLayer());
			//如果链接无效，则继续
			if(!isEffect||pt.getFile_url().length()>128||pt.getPage_url().length()>128||!pt.getFile_url().contains("http://")||pt.getFile_url().contains("(")||pt.getFile_url().contains("\""))
				continue;
			//如果链接中含有节目后缀关键字，则插入节目表
			if((fileinfo_id=JudgeCondition.isProgram(pt.getFile_url(),suffixKeywords))!=0)
			{
				pt.setFileinfoId(fileinfo_id);
				program1.add(pt);
				//向节目hashset添加链接
				CrawlerMain.hashProgram.add(pt.getFile_url().hashCode());
				continue;
			}

			int status=0;
			//如果该链接符合正则要求，则对网页进行解析，抓取数据
			if((status=JudgeCondition.matchRegister(regExps,pt,tableNumber))!=0)
			{
				program2.add(pt);
				if(!PropertiesUtil.isRegProgramAsLink())
					continue;
			}//如果进行网页关键字匹配
			else if(PropertiesUtil.isKeywordMatch())
			{
				program3.add(pt);
			}
			
			//如果该链接不属于域名控制范围，则继续
			if(!linkEffect)
				continue;
			
			//如果该链接在链接集合表中重复，则继续
			if(!JudgeCondition.notRepeat(pt.getFile_url(),hashLink))
			{
				log.info("HashSet判定链接重复："+pt.getFile_url());
				continue;
			}else
			{
				hashLink.add(pt.getFile_url().hashCode());
			}
//			hashLink.add(pt.getFile_url().hashCode());
			httpLinks.add(pt);
		}
		dbo.insertTotalHttp(httpLinks, tableNumber);
		//启动首页解析线程
		new FirstPageThread(program1,program2,program3,tableNumber).start();
	}

	/**
	 * 向http_x表中插入数据
	 * @param programTotals ProgramTotal对象集
	 * @param tableNumber   链接表编号
	 */
	public void executeLinks(Set<ProgramTotal> programTotals,int tableNumber,HashSet<Integer> hashLink,boolean ignoreLink)
	{
		//存放链接，批量插入连接表
		Set<ProgramTotal> httpLinks=new HashSet<ProgramTotal>();
		Iterator it=programTotals.iterator();
		while(it.hasNext())
		{
			
			int fileinfo_id=0;
			ProgramTotal pt=(ProgramTotal) it.next();
			//链接是否有效，如果该节目的父页面不在域名范围内则判断无效
			boolean isEffect=false;
			//判断链接是否可以作为链接路径集合，如果节目url不在域名范围内则判断无效
			boolean linkEffect=false;
			
			if(CrawlerMain.taskInfo.getSiteArea()==1)
			{
				//对链接和节目有效性进行判断
				for(String area:programAreas)
				{
					if(pt.getSite().contains(area))
					{
						isEffect=true;
						if(pt.getFile_url().contains(area))
							linkEffect=true;
						break;
//							continue;
					}
				}
			}else{
				isEffect=true;
				linkEffect=true;
			}					
			//如果链接无效，则继续
			if(!isEffect||pt.getFile_url().length()>128||pt.getPage_url().length()>128||!pt.getFile_url().contains("http://")||pt.getFile_url().contains("(")||pt.getFile_url().contains("\""))
				continue;
			

			//如果链接中含有节目后缀关键字，则插入节目表
			if((fileinfo_id=JudgeCondition.isProgram(pt.getFile_url(),suffixKeywords))!=0)
			{
				//判断该数据是否已经插入
				if(!JudgeCondition.notRepeat(pt.getFile_url(),CrawlerMain.hashProgram))             //
				{
					log.info("hashProgram判定节目重复："+pt.getFile_url());
					continue;
				}else
				{
					CrawlerMain.hashProgram.add(pt.getFile_url().hashCode());
				}
				
				dbo.insertResultFile(pt,fileinfo_id,tableNumber);
				continue;
			}

			int status=0;
			//如果该链接符合正则要求，则对网页进行解析，抓取数据
			if((status=JudgeCondition.matchRegister(regExps,pt,tableNumber))!=0)
			{
				if(status==-1)
					continue;
				ResultFile file=parserPage.matchPageInfo(pt);
				if(file!=null)
					dbo.insertRegResultFile(file, tableNumber);
				if(!PropertiesUtil.isRegProgramAsLink())
					continue;
			}//如果进行网页关键字匹配
			else if(PropertiesUtil.isKeywordMatch())
			{
				//对于关键字匹配所挖掘的节目，除了正则匹配的所有链接都要解析
				if(JudgeCondition.notRepeat(pt.getFile_url(),hashLink))
				{
					ResultFile file=parserPage.matchKeywords(pt,keywords);
					if(file!=null)
						dbo.insertRegResultFile(file, tableNumber);
				}
			}
			//如果该链接不属于域名控制范围，则继续
			if(!linkEffect)
				continue;
			
			if(ignoreLink)
				continue;
			//如果该链接在链接集合表中重复，则继续
			if(!JudgeCondition.notRepeat(pt.getFile_url(),hashLink))
			{
				log.info("HashSet判定链接重复："+pt.getFile_url());
				continue;
			}else
			{
				hashLink.add(pt.getFile_url().hashCode());
			}
//			hashLink.add(pt.getFile_url().hashCode());
			httpLinks.add(pt);
		}
		dbo.insertTotalHttp(httpLinks, tableNumber);
	}
	
	
		
	
}
