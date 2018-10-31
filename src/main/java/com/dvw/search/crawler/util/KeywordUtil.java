package com.dvw.search.crawler.util;

import java.util.List;

public class KeywordUtil {

	public static boolean matchKeywords(String html, List<String> keywords,int matchNumber) {
		int count=0;
		for(String keyword:keywords)
		{
			if(html.indexOf(keyword)>=0)
				count++;
		}
		
		if(count>matchNumber&&matchNumber>0)
			return true;
		
		return false;
	}

}
