package com.dvw.search.crawler.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;
import java.util.zip.InflaterInputStream;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.ibm.icu.text.CharsetDetector;
/**
 * 处理网页字符集的获取
 * @author David.Wang
 *
 */
public class PageCharsetUtil {

	private static Log log=LogFactory.getLog(PageCharsetUtil.class);
	
	public static MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

	private static int connectionTimeOut = PropertiesUtil.getConnectionTimeOut();

	private static int socketTimeOut = PropertiesUtil.getSocketTimeOut();

	private static int maxConnectionPerHost = PropertiesUtil.getMaxConnectionPerHost();

	private static int maxTotalConnections = PropertiesUtil.getMaxTotalConnections();
    //存放字符集<"hd.tudou.com","utf-8">
	private static Map<String,String> charsetMap=new HashMap<String,String>();
	
	private static HttpClient httpClient;
	
	private static GetMethod getMethod;


	static{
		setPara();
	}
	
	public static void setPara() {
		manager.getParams().setConnectionTimeout(connectionTimeOut);
		manager.getParams().setSoTimeout(socketTimeOut);
		manager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
		manager.getParams().setMaxTotalConnections(maxTotalConnections);
		httpClient = new HttpClient(manager);
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT,"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0");
		//代理设置
		if(PropertiesUtil.isProxy())
		{
			httpClient.getHostConfiguration().setProxy(PropertiesUtil.getProxyHost(), PropertiesUtil.getProxyPort());
		}
	
		//IP地址绑定
		if(PropertiesUtil.getBindIp().trim().length()!=0)
		{
			try {
				httpClient.getHostConfiguration().setLocalAddress(InetAddress.getByName(PropertiesUtil.getBindIp().trim()));
			} catch (UnknownHostException e) {
				log.fatal("IP绑定失败，使用默认IP地址");
			}
		}
	}
	
	/**
	 * 获取指定url的字符集
	 * @param url
	 * @param isDomain 
	 * @return
	 * @throws Exception 
	 */
	public static String getCharset(String url)  {
		String charset=null;
		try {
			for (int i = 0; i < 3; i++) {
				CharsetDetector cd=new CharsetDetector();
				URL path = new URL(url);
				HttpURLConnection urlconn = (HttpURLConnection) path.openConnection();
				urlconn.setConnectTimeout(connectionTimeOut);
				urlconn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
//				urlconn.setRequestMethod("HEAD");
				urlconn.setReadTimeout(4000);
				urlconn.connect();
				System.out.println(urlconn.getContentEncoding());
				if(urlconn.getContentEncoding()!=null&&urlconn.getContentEncoding().toLowerCase().equals("gzip"))
					cd.setText(new BufferedInputStream(new GZIPInputStream(urlconn.getURL().openStream())));
				else if(urlconn.getContentEncoding()!=null&&urlconn.getContentEncoding().toLowerCase().equals("deflate"))
					cd.setText(new BufferedInputStream(new InflaterInputStream(urlconn.getURL().openStream())));
				else
					cd.setText(new BufferedInputStream(urlconn.getURL().openStream()));
				charset = cd.detect().getName();
//				System.oxiout.println(inputStream2String(charset, urlconn.getURL().openStream()).toString());
				urlconn.disconnect();
				if(charset != null)
					break;
			}
		} catch (HttpException e) {
			log.fatal("分析字符集时链接超时。");
		} catch (IOException e) {
			//ignore
			log.fatal("分析字符集时链接超时。");
		}
		
		if(charset==null)
			try {
				charset=getCharsetByPage(url);
			} catch (SocketTimeoutException e) {
				charset=null;
				log.fatal("字符集判定异常"+"\t"+url,e);
			} catch (StringIndexOutOfBoundsException e) {
				charset=null;
				log.fatal("字符集判定异常"+"\t"+url,e);
			} catch (HttpException e) {
				charset=null;
				log.fatal("字符集判定异常"+"\t"+url,e);
			} catch (IllegalArgumentException e) {
				charset=null;
				log.fatal("字符集判定异常"+"\t"+url,e);
			} catch (IOException e) {
				charset=null;
				log.fatal("字符集判定异常"+"\t"+url,e);
			}catch(Exception e)
			{
				charset=null;
				log.fatal("字符集判定异常"+"\t"+url,e);
			}
			log.fatal("charset:"+charset+"\t\tsite:"+url);
		return charset;
	}
	
	
	public static String getCharsetByPage(String url) throws HttpException, IOException, IllegalArgumentException, SocketTimeoutException, StringIndexOutOfBoundsException {
		getMethod=new GetMethod(url);
//		 //使用系统提供的默认的恢复策略
//		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//				 new DefaultHttpMethodRetryHandler());
		httpClient.executeMethod(getMethod);

	
		String encodeType = "GBK";
		String htmlContent = "";
			long length=0;

			byte[] buffer=null;

			int i = 0;
			
			buffer =getMethod.getResponseBody();
			htmlContent=new String(buffer);

			if (buffer != null) {
				htmlContent = new String(buffer, encodeType);
			}
			getMethod.releaseConnection();
	        // 获取网页的字符集，然后重新解码
//			System.out.println(htmlContent);
			Pattern pattern=Pattern.compile(".*(?:charset|CHARSET)=(?:\")?(.*?)(?:\")?(;)*(\").*");
			Matcher matcher=pattern.matcher(htmlContent);
			
			if(matcher.find())
			{
				System.out.println(matcher.group(1));
				return matcher.group(1);
			}
			else
			{
				encodeType=getMethod.getResponseCharSet();
				if(encodeType.length()>3)
					return encodeType;
			}
			
			return null;
	}
	
	/**
	 * 获取url对应子域名的字符集
	 * @param link
	 * @return
	 */
	public static String getCharsetInMemo(String link) {
		String charset=null;
		String subDomain=UrlCheckUtil.getSubDomain(link);
		//如果map中没有子域名对应的字符集则进行字符集探测
		if(charsetMap.get(subDomain)==null)
		{
			charset=getCharset(link);
			if(charset!=null)
				charsetMap.put(subDomain, charset);
		}
					
		return charsetMap.get(subDomain);
	}
	
	
	public static StringBuilder inputStream2String(String charset, InputStream input) throws Exception {
		BufferedReader in = null;
		StringBuilder htmlDocument = new StringBuilder();
		String inputLine = null;
		try {
			if (charset != null && charset.length() > 0) {
				in = new BufferedReader(new InputStreamReader(input, charset));
			} else {
				in = new BufferedReader(new InputStreamReader(input));
			}
			while ((inputLine = in.readLine()) != null) {
				htmlDocument.append(inputLine).append("\r\n");
			}

		} catch (Exception e) {
			System.out.println("无法建立连接");
		} finally {
			if (in != null) {
				in.close();
			}
			if (input != null) {
				input.close();
			}
		}

		return htmlDocument;
	}
	
	public static void main(String args[]) throws SocketTimeoutException, StringIndexOutOfBoundsException, HttpException, IllegalArgumentException, IOException
	{
//		getCharsetByPage("http://www.6.cn",false);
		try {
			System.out.println(getCharset("http://6.cn/"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
//		getCharsetInMemo("http://www.6.cn");
//		getCharsetInMemo("http://www.6.cn");
//		getCharsetInMemo("http://www.6.cn");
	}



	
}
