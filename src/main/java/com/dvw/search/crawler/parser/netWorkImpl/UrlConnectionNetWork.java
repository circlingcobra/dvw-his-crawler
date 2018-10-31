package com.dvw.search.crawler.parser.netWorkImpl;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.zip.GZIPInputStream;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.Constants;
import com.dvw.search.crawler.parser.NetWork;
import com.dvw.search.crawler.util.StreamStringBytesTrans;


public class UrlConnectionNetWork implements NetWork{

	private static Log log=LogFactory.getLog(UrlConnectionNetWork.class);
	
	public String getResource(String url, String encoding) {
		String pageType=Constants.EMPTY_STRING;
		String resource=Constants.EMPTY_STRING;
		URL u=null;
		HttpURLConnection connection=null;
		try {
			u = new URL(url);
			connection=(HttpURLConnection)u.openConnection();
			connection.setRequestProperty("User-Agent", "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0");   
			//获取链接内容类型
			for(int i=0;i<3;i++)
			{
				pageType=connection.getContentType();
				if(pageType!=null)
				{
					break;
				}
			}
			//若获取不到类型就默认为html类型
			if(pageType==null)
			{
				pageType="text/html";
			}
			//如果是其他类型就抛出异常，因为下载的必须是网页
			if(pageType.indexOf("text")<0)
			{
				log.fatal("节目文件,抛出异常："+url);
				throw new Exception();
			}
			//获取网页的输入流
			InputStream responseBody=connection.getInputStream();
//			System.out.println(connection.getContentType());
//			System.out.println(connection.getContentEncoding());
			if(connection.getContentEncoding()!=null&&connection.getContentEncoding().toLowerCase().equals("gzip"))
				resource=StreamStringBytesTrans.inputStream2String(encoding,new GZIPInputStream(responseBody));
			else if(connection.getContentEncoding()!=null&&connection.getContentEncoding().toLowerCase().equals("deflate"))
				resource=StreamStringBytesTrans.deflateByte2String(encoding,StreamStringBytesTrans.stream2Bytes(responseBody));
			else
				resource=StreamStringBytesTrans.inputStream2String(encoding,responseBody);
			log.fatal("链接解析成功："+url);
			return resource;
//			System.out.println("===============================");
//			System.out.println(resource);
		} catch (MalformedURLException e) {
			log.fatal("链接解析失败："+url+"\t"+connection.getContentType());
			e.printStackTrace();
		} catch (Exception e) {
			log.fatal("链接解析失败："+url+"\t"+connection.getContentType());
			e.printStackTrace();
		}
		
		return null;
	}

	
}
