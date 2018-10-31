package com.dvw.search.crawler.assistant;

import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.core.CrawlerMain;
import com.dvw.search.crawler.domain.FileInfo;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.RegExp;


/**
 * 判定链接是否重复
 * @author David.Wang
 * 2009-12-25
 */
public class JudgeCondition {

	private static Log log=LogFactory.getLog(JudgeCondition.class);
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
				return true;
			}
		}		
	}
	
	/**
	 * 判断该节目是否属于后缀名匹配范围 
	 */
	public static int isProgram(String file_url,List<FileInfo> suffixKeywords){
		for(FileInfo fi:suffixKeywords)
		{
			if(file_url.contains("."+fi.getPostfix())&&!file_url.matches(".*\\."+fi.getPostfix()+"\\w+.*"))
				return fi.getFileinfo_id();
		}
		return 0;
	}
	
	/**
	 * 判断url是否符合正则规范
	 * @param pt
	 * @param conn
	 * @return
	 */
	public static int matchRegister(List<RegExp> regExps,ProgramTotal pt,int tableNumber)
	{
		String reg_url="";
		Iterator it=regExps.iterator();
		while(it.hasNext())
		{
			RegExp re=(RegExp)it.next();
			if(pt.getDomain().indexOf(re.getDomain().trim())>=0&&re.getRegType()==1)
			{
				reg_url=re.getRegex();
				Pattern pattern=Pattern.compile(reg_url);
				Matcher matcher=pattern.matcher(pt.getFile_url());
				while(matcher.find())
				{
					if(notRepeat(pt.getFile_url(),CrawlerMain.hashProgram))			//"file_"+tableNumber
					{
						CrawlerMain.hashProgram.add(pt.getFile_url().hashCode());
						return 1;
					}
					else
						log.info("hashProgram判定节目重复："+pt.getFile_url());
					return -1;
				}
			}
		}
		return 0;
	}
}
