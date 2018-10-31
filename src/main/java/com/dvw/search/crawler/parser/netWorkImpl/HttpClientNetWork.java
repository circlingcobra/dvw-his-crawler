package com.dvw.search.crawler.parser.netWorkImpl;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.zip.GZIPInputStream;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.Constants;
import com.dvw.search.crawler.parser.NetWork;
import com.dvw.search.crawler.util.PropertiesUtil;
import com.dvw.search.crawler.util.StreamStringBytesTrans;



public class HttpClientNetWork implements NetWork{

public static MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();
	
	private static int connectionTimeOut = PropertiesUtil.getConnectionTimeOut();

	private static int socketTimeOut = PropertiesUtil.getSocketTimeOut();

	private static int maxConnectionPerHost = PropertiesUtil.getMaxConnectionPerHost();

	private static int maxTotalConnections = PropertiesUtil.getMaxTotalConnections();
	
	private String bindIp=PropertiesUtil.getBindIp().trim();
	
	private HttpClient httpClient;
	
	private static Log log=LogFactory.getLog(HttpClientNetWork.class);
	
	static{
		manager.getParams().setConnectionTimeout(connectionTimeOut);
		manager.getParams().setSoTimeout(socketTimeOut);
		manager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
		manager.getParams().setMaxTotalConnections(maxTotalConnections);
		
	}
	
	public String getResource(String url, String encoding) {
		String resource=Constants.EMPTY_STRING;
		// 构造HttpClient的实例
		if(httpClient==null)
		{
			httpClient = new HttpClient(manager);
			httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT,"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0");
			//代理设置
			if(PropertiesUtil.isProxy())
			{
				httpClient.getHostConfiguration().setProxy(PropertiesUtil.getProxyHost(), PropertiesUtil.getProxyPort());
			}
		
			//IP地址绑定
			if(bindIp.length()!=0)
			{
				try {
					httpClient.getHostConfiguration().setLocalAddress(InetAddress.getByName(bindIp));
				} catch (UnknownHostException e) {
					log.fatal("IP绑定失败，使用默认IP地址");
				}
			}
		}
				
		// 创建GET方法的实例
		GetMethod getMethod = new GetMethod(url);

		// 使用系统提供的默认的恢复策略
		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
			new DefaultHttpMethodRetryHandler());
		try {
			httpClient.executeMethod(getMethod);
			if(getMethod.getResponseHeader("Content-Type")!=null&&getMethod.getResponseHeader("Content-Type").toString().indexOf("text")<0)
				throw new Exception();
//			System.out.println(getMethod.getResponseHeader("Content-Type"));
//			System.out.println(getMethod.getResponseHeader("Content-Encoding"));
			if(getMethod.getResponseHeader("Content-Encoding")!=null&&getMethod.getResponseHeader("Content-Encoding").getValue().toLowerCase().equals("gzip"))
				resource=StreamStringBytesTrans.inputStream2String(encoding,new GZIPInputStream(getMethod.getResponseBodyAsStream()));
			else if(getMethod.getResponseHeader("Content-Encoding")!=null&&getMethod.getResponseHeader("Content-Encoding").getValue().toLowerCase().equals("deflate"))
				resource=StreamStringBytesTrans.deflateByte2String(encoding,getMethod.getResponseBody());
			else
				resource= new String(getMethod.getResponseBody(),encoding);
			log.fatal("链接解析成功："+url);
			return resource;
		} catch (Exception e) {
			log.fatal("链接解析失败："+url+"\t"+getMethod.getResponseHeader("Content-Type"));
			e.printStackTrace();
		}finally{
			if(getMethod!=null)
				getMethod.releaseConnection();
		}
	     
		return null;
	}

}
