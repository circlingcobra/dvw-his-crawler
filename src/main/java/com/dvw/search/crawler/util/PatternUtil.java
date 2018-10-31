package com.dvw.search.crawler.util;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.oro.text.regex.MalformedPatternException;
import org.apache.oro.text.regex.MatchResult;
import org.apache.oro.text.regex.Pattern;
import org.apache.oro.text.regex.PatternCompiler;
import org.apache.oro.text.regex.PatternMatcher;
import org.apache.oro.text.regex.Perl5Compiler;
import org.apache.oro.text.regex.Perl5Matcher;

public class PatternUtil {

	private static Log log=LogFactory.getLog(PatternUtil.class);
	/**
	 * 匹配数字正则的类
	 * @param s
	 * @param reg
	 * @return
	 */
	public static int parserInt(String s,String reg)
	{

//		try {
//			reg=new String(reg.getBytes("utf-8"),"gbk");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		int result=0;
		Pattern pattern=null;
		String reg1="(?:"+reg.substring(0,reg.indexOf("num_of_counter"))+")";
		String reg2="(?:"+reg.substring(reg.indexOf("num_of_counter")+14)+")";
		
		reg=reg1+"(\\s*\\d+(,\\d+)*\\s*)"+reg2;
//		System.out.println(index1);
//		System.out.println(index2);
		
		try {		
			PatternCompiler compiler = new Perl5Compiler();	
			pattern = compiler.compile(reg,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher(); 
			
			if(matcher.contains(s, pattern))
			{
				MatchResult mr = matcher.getMatch();
				String str=mr.group(1);
//				System.out.println(str);
				if(str.indexOf(",")>0)
					str=str.replace(",", "");
				try{
					result=Integer.parseInt(str.trim());
				}catch(Exception e)
				{
					log.error("int类型转换异常");
					return 0;
				}
				return result;
			}else
				return 0;
		} catch (Exception e1) {
			e1.printStackTrace();
			return 0;
		}
		
	}
	
	/**
	 * 匹配字符串正则的类
	 * @param s
	 * @param reg
	 * @return
	 */
	public static String parserString(String s,String reg)
	{
//		try {
//			reg=new String(reg.getBytes("utf-8"),"gbk");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		Pattern pattern=null;
		String reg1="(?:"+reg.substring(0,reg.indexOf("num_of_counter"))+")";
		String reg2="(?:"+reg.substring(reg.indexOf("num_of_counter")+14)+")";

		reg=reg1+"(.*)"+reg2;
//		System.out.println(reg1);
//		System.out.println(reg2);
		try {		
			PatternCompiler compiler = new Perl5Compiler();	
			pattern = compiler.compile(reg,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher(); 
			if(matcher.contains(s, pattern))
			{
				MatchResult mr = matcher.getMatch();
				String str=mr.group(1);
//				System.out.println(str+"--------after");
				
				return str.trim();
			}else
				return "";
		} catch (Exception e1) {
			e1.printStackTrace();
			return "";
		}
				
	}
	
	
	/**
	 * 匹配日期正则的类
	 * @param s
	 * @param reg
	 * @param example
	 * @return
	 */
	public static Date parserDate(String s,String reg,String example)
	{
//		try {
//			reg=new String(reg.getBytes("utf-8"),"gbk");
//			example=new String(example.getBytes("utf-8"),"gbk");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		Pattern pattern=null;
		PatternCompiler compiler=null;
		String reg1="(?:"+reg.substring(0,reg.indexOf("num_of_counter"))+")";
		String reg2="(?:"+reg.substring(reg.indexOf("num_of_counter")+14)+")";
		reg=reg1+"(.*)"+reg2;
//		System.out.println(reg1);
//		System.out.println(reg2);
		
		try {		
			compiler = new Perl5Compiler();	
			pattern = compiler.compile(reg,
					Perl5Compiler.CASE_INSENSITIVE_MASK);
			PatternMatcher matcher = new Perl5Matcher();
			if(matcher.contains(s, pattern))
			{
				Date date;
				MatchResult mr = matcher.getMatch();
				String str=mr.group(1);
				
				//处理x天前,个月前的情况
				if(str.indexOf("前")>=0)
				{
					try {
						pattern= compiler.compile("\\d+",
								Perl5Compiler.CASE_INSENSITIVE_MASK);
					} catch (MalformedPatternException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
					matcher = new Perl5Matcher();
					if(matcher.contains(str, pattern))
					{
						int number=Integer.parseInt(matcher.getMatch().group(0));
											
						if(str.indexOf("年")>=0)
						{
							Calendar c1=Calendar.getInstance();
							c1.add(Calendar.YEAR,-number);
//							System.out.println(c1.getTime().toLocaleString());
							return c1.getTime();
						}
						
						if(str.indexOf("月")>=0)
						{
							Calendar c1=Calendar.getInstance();
							c1.add(Calendar.MONTH,-number);
//							System.out.println(c1.getTime().toLocaleString());
							return c1.getTime();
						}
						
						if(str.indexOf("天")>=0||str.indexOf("日")>=0)
						{
							Calendar c1=Calendar.getInstance();
							c1.add(Calendar.DATE,-number);
//							System.out.println(c1.getTime().toLocaleString());
							return c1.getTime();
						}
						
						if(str.indexOf("时")>=0)
						{
							Calendar c1=Calendar.getInstance();
							c1.add(Calendar.HOUR_OF_DAY,-number);
//							System.out.println(c1.getTime().toLocaleString());
							return c1.getTime();
						}
						
						if(str.indexOf("分")>=0)
						{
							Calendar c1=Calendar.getInstance();
							c1.add(Calendar.MINUTE,-number);
//							System.out.println(c1.getTime().toLocaleString());
							return c1.getTime();
						}
					}
					return null;
				}
				DateFormat df=new SimpleDateFormat(example);
				try {
					date=df.parse(str.trim());
				} catch (ParseException e) {
					// TODO Auto-generated catch block
					log.error("时间转换异常");
					return null;
				}
				return date;
			}else
				return null;
		} catch (Exception e1) {
			e1.printStackTrace();
			return null;
		}
		
	}
}
