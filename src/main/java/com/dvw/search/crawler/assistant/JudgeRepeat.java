package com.dvw.search.crawler.assistant;

import java.util.HashSet;

import com.dvw.search.crawler.core.CrawlerMain;


/**
 * 判定链接是否重复
 * @author David.Wang
 * 2009-12-25
 */
public class JudgeRepeat {

	/**
	 * 判断URL是否重复
	 * @param url
	 * @param hashLink
	 * @return
	 */
	public static boolean notRepeat(String url,HashSet<Integer> hashLink)
	{		
		synchronized(CrawlerMain.taskInfo)
		{
			if(hashLink.contains(url.hashCode()))
				return false;
			else
			{
				hashLink.add(url.hashCode());
				return true;
			}
		}		
	}
	
	
}
