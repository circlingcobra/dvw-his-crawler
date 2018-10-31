package com.dvw.search.crawler.core;

import java.util.List;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.ResultFile;
import com.dvw.search.crawler.parser.ParserPage;
import com.dvw.search.crawler.util.ParserFactory;


/**
 * 用于爬虫站点入口页面中节目的解析，由于需要统计节目所在层数，所以由于爬虫设计问题单独提出一个线程
 * @author David.Wang
 *
 */
public class FirstPageThread extends Thread{

	private Log log=LogFactory.getLog(this.getClass());
	
	//后缀匹配节目
	private Set<ProgramTotal> p1;
	//正则匹配节目
	private Set<ProgramTotal> p2;
	//待关键字匹配的节目
	private Set<ProgramTotal> p3;
	//关键字集合
	private List<String> keywords=DataSource.getKeywords();
	//tableNumber
	private int tableNumber;
	
	private DBOperation dbo=new DBOperation();
	
	private ParserPage parserPage=ParserFactory.getParserPage();
	
	public FirstPageThread(Set<ProgramTotal> p1,Set<ProgramTotal> p2,Set<ProgramTotal> p3,int tableNumber){
		this.p1=p1;
		this.p2=p2;
		this.p3=p3;
		this.tableNumber=tableNumber;
	}

	@Override
	public void run() {
		log.info("*****首页解析线程开始运行*****");
		for(ProgramTotal pt:p1)
		{
			dbo.insertResultFile(pt, pt.getFileinfoId(), tableNumber);
		}
		
		for(ProgramTotal pt:p2)
		{
			ResultFile file=parserPage.matchPageInfo(pt);
			dbo.insertRegResultFile(file, tableNumber);
		}
		
		for(ProgramTotal pt:p3)
		{
			ResultFile file=parserPage.matchKeywords(pt, keywords);
			if(file!=null)
				dbo.insertRegResultFile(file, tableNumber);
		}
		log.info("*****首页解析线程运行结束*****");
	}
		
}
