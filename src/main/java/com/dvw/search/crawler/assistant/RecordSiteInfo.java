package com.dvw.search.crawler.assistant;

import java.util.ArrayList;
import java.util.List;

import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.domain.SiteInfo;
import com.dvw.search.crawler.parser.ParserPage;
import com.dvw.search.crawler.util.CharsetTransUtil;
import com.dvw.search.crawler.util.PageCharsetUtil;
import com.dvw.search.crawler.util.ParserFactory;
import com.dvw.search.crawler.util.UrlCheckUtil;



/**
 * 定时进行爬虫信息统计(t_result_site) 站点信息
 * @author David.Wang
 *
 */
public class RecordSiteInfo implements Runnable{

	private DBOperation dbo=new DBOperation();
	
	private ParserPage parserPage=ParserFactory.getParserPage();
	
	List<SiteInfo> results=new ArrayList<SiteInfo>();
	public void run() {
		
		while(true)
		{
			try {
				Thread.sleep(60*1000*10);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			//统计站点及其所拥有的节目数
			results=dbo.getSiteInfo();
			for(SiteInfo siteInfo:results)
			{
				if(dbo.notRepeat(siteInfo.getSiteUrl()))
					insertSite(siteInfo);
				else
					dbo.updateSiteInfo(siteInfo);				
			}
		}
		
	}
	
	/**
	 * 向t_result_site表插入数据
	 * @param siteInfo
	 */
	private void insertSite(SiteInfo siteInfo){
		String url="http://"+siteInfo.getSiteUrl();
		String encoding=PageCharsetUtil.getCharset(url);
		String title=CharsetTransUtil.encodeWebString(parserPage.parseTitle(null,url, encoding));
		String ip=UrlCheckUtil.getIPAddress(siteInfo.getSiteUrl());
		dbo.insertResultSite(siteInfo.getSiteUrl(),siteInfo.getDomain(), ip, title, siteInfo.getProgramNum());
	}
	

}
