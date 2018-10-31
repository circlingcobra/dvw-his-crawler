package com.dvw.search.crawler.core;

import java.util.HashSet;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.util.ParserException;

import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.SeedInfo;
import com.dvw.search.crawler.parser.ParserCtrl;
import com.dvw.search.crawler.util.PropertiesUtil;

/**
 * 页面解析线程
 * @author David.Wang
 *
 */
public class ParserLinkThread extends Thread{

	private Log log=LogFactory.getLog(getClass());
	private Vector<HttpLink> links;
	private ParserCtrl parserCtrl=new ParserCtrl();
	private boolean ignoreLink;
    private SeedInfo seedInfo;
	
	public ParserLinkThread(Vector<HttpLink> links,SeedInfo seedInfo,boolean ignoreLink){
		this.links=links;
		this.seedInfo=seedInfo;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		DBOperation dbo=new DBOperation();
		while(!links.isEmpty())
		{
			HttpLink link=links.remove(0);
			try {
				//页面解析
				parserCtrl.parserLink(link, seedInfo,ignoreLink,false);
				if(PropertiesUtil.isSearchImage())
				{
					//解析图片连接
					parserCtrl.parserImage(link, seedInfo);
				}				
			} catch (Exception e) {
				log.fatal("网页解析错误："+link.getLink());
				//将该链接状态设置为已经解析完毕
				dbo.updateTotalHttpStatusForUrl(2, link.getLink(),seedInfo.getTableNumber());
			}
			//将该链接状态设置为已经解析完毕
			dbo.updateTotalHttpStatusForUrl(2, link.getLink(),seedInfo.getTableNumber());
		}
	}
	
	
}
