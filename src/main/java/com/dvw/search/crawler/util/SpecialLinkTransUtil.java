package com.dvw.search.crawler.util;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import sun.misc.BASE64Decoder;
/**
 * 解析特殊链接
 * @author David.Wang
 *
 */
public class SpecialLinkTransUtil {
	
	private static Log log=LogFactory.getLog(SpecialLinkTransUtil.class);
	
	/**
	 * 解析迅雷链接
	 * @param thunder
	 * @return
	 */
	public static String getThunderParsedLink(String thunder)
	{
//		String thunder="thunder://QUFodHRwOi8vMjIuZG93bi5tZWktanUuY29tOjkyMC8wNzA0L9eqvcfT9rW9sK4zNy/Xqr3H0/a1vbCuMzdfMS4zZ3BaWg==";
		String url="";
		thunder=thunder.substring(10);
		BASE64Decoder decode=new BASE64Decoder();
		try {
			url=new String(decode.decodeBuffer(thunder));
			if(url.length()>2)
				url=url.substring(2, url.length()-2);
			else 
				return "";
			log.info("迅雷解析:"+url);
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		return url;
	}
	
	
	/**
	 * 解析FlashGet 链接
	 * @param flashGet
	 * @return
	 */
	public static String getFlashGetParsedLink(String flashGet)
	{
//		String flashGet="Flashget://W0ZMQVNIR0VUXWh0dHA6Ly9kb3duLnJub3ZlbC5jb20vYm9va3R4dC8zLzEzNjgyLzEzNjgyLnppcFtGTEFTSEdFVF0=&1064";
		String url="";
		flashGet=flashGet.substring(11);
		BASE64Decoder decode=new BASE64Decoder();
		try {
			url=new String(decode.decodeBuffer(flashGet));
			if(url.length()>13)
				url=url.substring(10, url.length()-13);
			else
				return "";
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return url;
	}
}
