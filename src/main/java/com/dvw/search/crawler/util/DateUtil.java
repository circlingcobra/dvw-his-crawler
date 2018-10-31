package com.dvw.search.crawler.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * 日期工具
 * @author David.Wang
 *
 */
public class DateUtil {

	/**
	 * 日期转换指定格式字符串
	 * @return
	 */
	public static String Date2String(){
		DateFormat df=new SimpleDateFormat("yyyyMMddHHmm");		
		return df.format(new Date());
	}
	
	public static Date String2Date(String source) throws ParseException{
		DateFormat df=new SimpleDateFormat("yyyyMMddHHmm");
		return df.parse(source.trim());
		
	}
	
	
	public static void main(String[] args){
		try {
			System.out.println(String2Date("200908182114"));
		} catch (ParseException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
