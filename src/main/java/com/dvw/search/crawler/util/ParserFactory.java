package com.dvw.search.crawler.util;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.parser.NetWork;
import com.dvw.search.crawler.parser.ParserPage;



/**
 * 解析类工厂，别人实现接口并通过配置实现自己的解析器
 * @author David.Wang
 *
 */
public class ParserFactory {
	
	private static Log log=LogFactory.getLog(ParserFactory.class);
	
	static Class claszParser;
	
	static Class classNetWork;
	
	static{
		try {
			claszParser=Class.forName(PropertiesUtil.getParserClass());
		} catch (ClassNotFoundException e) {
			log.fatal("对不起，找不到您配置的解析类,启用默认解析类--HtmlParser");
			try {
				claszParser=Class.forName("crawlerWap.parser.parserPageImpl.HtmlParserPage");
			} catch (ClassNotFoundException e1) {
				log.fatal("对不起，启用默认解析类--HtmlParser,失败，可能解析类已经丢失，程序退出");
				System.exit(0);
			}
		}
		
		try {
			classNetWork=Class.forName(PropertiesUtil.getNetWorkClass());
		} catch (ClassNotFoundException e) {
			log.fatal("对不起，找不到您配置的URL访问类,启用默认解析类--HtmlParser");
			try {
				classNetWork=Class.forName("crawlerWap.parser.netWorkImpl.HttpClientNetWork");
			} catch (ClassNotFoundException e1) {
				log.fatal("对不起，启用默认URL访问类--HttpClientNetWork失败，可能URL访问类已经丢失，程序退出");
				System.exit(0);
			}
		}
	}
	
	public static ParserPage getParserPage(){
		Object obj=null;
		try {
			obj=claszParser.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (ParserPage)obj;
	}
	
	public static NetWork getNetWork(){
		Object obj=null;
		try {
			obj=classNetWork.newInstance();
		} catch (InstantiationException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return (NetWork)obj;
	}
}
