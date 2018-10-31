package com.dvw.search.crawler.assistant;

import com.dvw.search.crawler.core.CrawlerMain;
import com.dvw.search.crawler.util.PropertiesUtil;


/**
 * 时钟统计类，用于定时关闭爬虫程序
 * @author David.Wang
 *
 */
public class TimeClock implements Runnable{

	public void run() {
		while(true)
		{
			if(canClose())
			{
				CrawlerMain.siteState=0;
				return;
			}
			try {
				Thread.sleep(60*1000*2);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	/**
	 * 判断爬虫是否可以关闭
	 * @return
	 */
	private boolean canClose()
	{
		if(PropertiesUtil.getMaxTime()<=0)
			return false;
		else if((System.currentTimeMillis()-CrawlerMain.time)/60000>=PropertiesUtil.getMaxTime()){
			return true;
		}else
			return false;
	}
}
