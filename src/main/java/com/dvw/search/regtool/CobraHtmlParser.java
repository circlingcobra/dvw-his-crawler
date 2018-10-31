package com.dvw.search.regtool;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.net.HttpURLConnection;
import java.net.InetAddress;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.UnknownHostException;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.lobobrowser.html.domimpl.HTMLDocumentImpl;
import org.lobobrowser.html.parser.DocumentBuilderImpl;
import org.lobobrowser.html.parser.InputSourceImpl;
import org.lobobrowser.html.test.SimpleUserAgentContext;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html2.HTMLCollection;



public class CobraHtmlParser {
//	public static MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

//	private static int connectionTimeOut = PropertiesUtil.connectionTimeOut;
//
//	private static int socketTimeOut = PropertiesUtil.socketTimeOut;
//
//	private static int maxConnectionPerHost = PropertiesUtil.maxConnectionPerHost;
//
//	private static int maxTotalConnections = PropertiesUtil.maxTotalConnections;


	private static HttpClient httpClient;
	
	private static GetMethod getMethod;


	static{
		setPara();
	}
	

	public static void setPara() {
//		manager.getParams().setConnectionTimeout(connectionTimeOut);
//		manager.getParams().setSoTimeout(socketTimeOut);
//		manager.getParams().setDefaultMaxConnectionsPerHost(maxConnectionPerHost);
//		manager.getParams().setMaxTotalConnections(maxTotalConnections);
		httpClient = new HttpClient();
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT,"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0");
		
	}
	
	
	
	public static String getParseredString(String url,String proxyHost,String proxyPort)
	{
		String contents=""; 
		String encoding=getCharset(url,proxyHost,proxyPort);
		 try{
			 HTMLDocumentImpl document=getStreamSourceDocument(url,encoding,proxyHost,proxyPort);
			 contents=document.getInnerText();
		 }catch(Exception e)
		 {
			 System.out.println("解析文本信息错误~"+url);
			 return "";
		 }
				
		contents=contents.trim().replaceAll("\t","").replaceAll("\n{2,}", "\n");
		System.out.println(contents);
		return contents;
	}
	
	
	public static String parserScript(String url,String proxyHost,String proxyPort)
	{
		String html="";
		String encoding=getCharset(url,proxyHost,proxyPort);
        try {
            HTMLDocumentImpl document=getStreamSourceDocument(url,encoding,proxyHost,proxyPort);
            NodeList scripts = document.getElementsByTagName("script");
            for(int i=0;i<scripts.getLength();i++)
            {
            	html+=((HTMLDocumentImpl) scripts.item(i)).getTextContent();
            }
		} catch (Exception e) {
			e.printStackTrace();
		}
		return html;
	}
	
	
	public static String parserPicUrl(String url,String proxyHost,String proxyPort)
	{
		String link="";
		String linkAll="";
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			HTMLDocumentImpl document = getStreamSourceDocument(url,encoding,proxyHost,proxyPort);
			HTMLCollection images = document.getImages();
			for(int i=0;i<images.getLength();i++)
			{
				//链接url
				if(images.item(i).getAttributes().getNamedItem("src")!=null)
				{
					link=images.item(i).getAttributes().getNamedItem("src").getNodeValue().replaceAll("&amp;", "&");
					link=getFullPath(link,url,getSubDomain(url));
					linkAll+=link+"\n";
				}				
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
				
		return linkAll;
	}
	
	
	public static String parserPicUrl(String reg,String url,String proxyHost,String proxyPort)
	{
		String link="";
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			HTMLDocumentImpl document = getStreamSourceDocument(url,encoding,proxyHost,proxyPort);
			HTMLCollection images = document.getImages();
			for(int i=0;i<images.getLength();i++)
			{
				//链接url
				link=images.item(i).getAttributes().getNamedItem("src").getNodeValue().replace("&amp;", "&");
				link=getFullPath(link,url,getSubDomain(url));
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
				while(matcher.find())
				{
					return link;
				}
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
				
		return "";
	}
	
	
	public static String parseDownloadLink(String url,String proxyHost,String proxyPort)
	{
		String linkAll="";
		String link="";
		String thunder="";
		String fg="";
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
	        HTMLDocumentImpl document = getStreamSourceDocument(url,encoding,proxyHost,proxyPort);
	        HTMLCollection links = document.getLinks();
				
			for(int i=0;i<links.getLength();i++)
			{
				
				//获取链接对应的名称
				String linkName=links.item(i).getTextContent();
				//获取迅雷链接（如果有的话）
				
				thunder=links.item(i).getAttributes().getNamedItem("thunderhref")==null?null:links.item(i).getAttributes().getNamedItem("thunderhref").getNodeValue();
				//获取FlashGet链接（如果有的话）
				fg=links.item(i).getAttributes().getNamedItem("fg")==null?null:links.item(i).getAttributes().getNamedItem("fg").getNodeValue();
				//判断该链接是否是普通链接
				if(thunder!=null)
					link=thunder;
				else if(fg!=null)
					link=fg;
				else{
					link=links.item(i).toString().replaceAll("&amp;","&");
					link=getFullPath(link,url,getSubDomain(url));
				}
					
				linkAll+=link+"\n";		
			}
			if(linkAll.length()==0)
				return "该网页没有链接";
			return linkAll;
		} catch (Exception e1) {
			System.out.println("URL--"+url+" 抓取异常\n encoding--"+encoding);
			e1.printStackTrace();
		}
								
		return "网页解析异常";
	}
	
	
	public static String parseDownloadLink(String reg,String url,String proxyHost,String proxyPort)
	{
		String linkAll="";
		String link="";
		String thunder="";
		String fg="";
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
	        HTMLDocumentImpl document = getStreamSourceDocument(url,encoding,proxyHost,proxyPort);
	        HTMLCollection links = document.getLinks();
				
			for(int i=0;i<links.getLength();i++)
			{
				//获取链接对应的名称
				String linkName=links.item(i).getTextContent();
				//获取迅雷链接（如果有的话）
				thunder=links.item(i).getAttributes().getNamedItem("thunderHref")==null?null:links.item(i).getAttributes().getNamedItem("thunderHref").getNodeValue();
				//获取FlashGet链接（如果有的话）
				fg=links.item(i).getAttributes().getNamedItem("fg")==null?null:links.item(i).getAttributes().getNamedItem("fg").getNodeValue();
				//判断该链接是否是普通链接
				if(thunder!=null)
					link=thunder;
				else if(fg!=null)
					link=fg;
				else{
					link=links.item(i).toString().replace("&amp;","&");
					link=getFullPath(link,url,getSubDomain(url));
				}				
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
				while(matcher.find())
				{			
					linkAll+=link+",";
					if(linkAll.length()>4096)
						return linkAll.substring(0,linkAll.length()-1);
				}
			}
			if(linkAll.equals(","))
				return "";
			//去掉最后的逗号
			return linkAll.substring(0,(linkAll.length()-1)<0?0:linkAll.length()-1);
		} catch (Exception e1) {
			System.out.println("URL--"+url+" 抓取异常\n encoding--"+encoding);
			e1.printStackTrace();
		}
				
				
		return "网页解析异常";
	}
	
	/**
	 * 获取指定url的字符集
	 * @param url
	 * @return
	 */
	public static String getCharset(String url,String proxyHost,String proxyPort) {
		String charset=null;
		//代理设置
		if(proxyHost!=null&&proxyHost.trim().length()>0)
		{
			int port=80;
    		try{
    			port=Integer.parseInt(proxyPort);
    		}catch(Exception e)
    		{
    			port=80;
    		}
    		System.getProperties().put("proxySet","true");  
			System.getProperties().put("proxyHost",proxyHost);  
			System.getProperties().put("proxyPort",String.valueOf(port));  
		}
		else{
			System.getProperties().remove("proxyHost");
			System.getProperties().remove("proxySet");  
			System.getProperties().remove("proxyPort");  
		}
		
		try {
			for (int i = 0; i < 3; i++) {
				URL path = new URL(url);
				HttpURLConnection urlconn = (HttpURLConnection) path.openConnection();
//				urlconn.setConnectTimeout(PropertiesUtil.connectionTimeOut);
				urlconn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
				urlconn.setRequestMethod("HEAD");
				urlconn.setReadTimeout(4000);
				String charset_bak = urlconn.getContentType();
				System.out.println(charset_bak);
				if(charset_bak != null && charset_bak.indexOf("charset=") >=0)
					charset=charset_bak.substring(charset_bak.indexOf("charset=") + "charset=".length());
				if(charset!=null)
					break;
			}
		} catch (HttpException e) {
		//	logger.info("分析字符集时链接超时。");
		} catch (IOException e) {
			//ignore
		}
		
		if(charset==null)
			try {
				charset=getCharsetByPage(url,proxyHost,proxyPort);
			} catch (SocketTimeoutException e) {
				// TODO Auto-generated catch block
				charset="gbk";
				e.printStackTrace();
			} catch (StringIndexOutOfBoundsException e) {
				// TODO Auto-generated catch block
				charset="gbk";
				e.printStackTrace();
			} catch (HttpException e) {
				// TODO Auto-generated catch block
				charset="gbk";
				e.printStackTrace();
			} catch (IllegalArgumentException e) {
				// TODO Auto-generated catch block
				charset="gbk";
				e.printStackTrace();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				charset="gbk";
				e.printStackTrace();
			}
		System.out.println("charset:"+charset);
		return charset;
	}
	
	public static String getCharsetByPage(String url,String proxyHost,String proxyPort) throws HttpException, IOException, IllegalArgumentException, SocketTimeoutException, StringIndexOutOfBoundsException {

		//代理设置
		if(proxyHost!=null&&proxyHost.trim().length()>0)
		{
			int port=80;
    		try{
    			port=Integer.parseInt(proxyPort);
    		}catch(Exception e)
    		{
    			port=80;
    		}
			httpClient.getHostConfiguration().setProxy(proxyHost, port);
		}
		getMethod=new GetMethod(url);
//		 //使用系统提供的默认的恢复策略
//		getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
//				 new DefaultHttpMethodRetryHandler());
		httpClient.executeMethod(getMethod);

	
		String encodeType = "GBK";
		String htmlContent = "";
			long length=0;

			byte[] buffer=null;
			 	
//			for (int i = 0; i < 2; i++) {
//				length = urlConn.getContentLength();
//				if (length != -1)
//					break;
//			}

			int i = 0;
			
			buffer =getMethod.getResponseBody();
			htmlContent=new String(buffer);

			if (buffer != null) {
				htmlContent = new String(buffer, encodeType);
			}
			getMethod.releaseConnection();
	        // 获取网页的字符集，然后重新解码
			htmlContent = htmlContent.toLowerCase();
//			System.out.println(htmlContent);
			if (htmlContent.indexOf("charset=gb2312") != -1||htmlContent.indexOf("charset=\"gb2312\"")!=-1) {
				return "gb2312";
			} else if (htmlContent.indexOf("charset=utf-8") != -1||htmlContent.indexOf("charset=\"utf-8\"")!=-1) {
				return "utf-8";
			} else if (htmlContent.indexOf("charset=gbk") != -1||htmlContent.indexOf("charset=\"gbk\"")!=-1) {
			    return "gbk";
			}else{
				encodeType=getMethod.getResponseCharSet();
				if(encodeType.length()>3)
					return encodeType;
			}
				return "gbk";

	}
	
	
	/**
	 * 获取子域名
	 * @param url
	 * @return
	 */
	public static String getSubDomain(String url)
	{
		String[] strs=url.split("/");
		String domain=null;
		try{
			domain=strs[2];
			if(domain.indexOf("#")>=0)
				domain=domain.substring(0,domain.indexOf("#"));
		}catch(ArrayIndexOutOfBoundsException e){
			System.out.println("错误链接");
			domain="";
		}
		
		return domain;
	}
	
	/**
	 * @param url    待解析的URL
	 * @param link   链接父页面URL
	 * @param site   链接所在站点
	 * @return
	 */
	private static String getFullPath(String url,String link,String site)
	{
		if(!url.equals("")&&!url.startsWith("http"))
		{
			if(url.startsWith("../"))
			{
				StringBuffer sb=new StringBuffer("");
				if(link.lastIndexOf("/")!=link.length()-1)
					link=link+"/";
				String[] areas=link.split("/");
				Pattern pattern=Pattern.compile("\\.\\./");
				Matcher matcher=pattern.matcher(url);
				int count=0;
				while(matcher.find()==true)
				{
					url=url.replaceFirst("../", "");
					matcher=pattern.matcher(url);
					count++;
				}
				for(int i=areas.length-1;i>areas.length-2-count;i--)
				{
					areas[i]=" ";
				}
				
				for(int i=0;i<areas.length;i++)
				{
					sb.append(areas[i]).append("/");
				}
				url=sb.toString().replaceAll(" /", "")+url;
			}
		    else if(url.startsWith("/"))
		    {
		    	url="http://"+site+url;
		    }
		    else if(url.startsWith("./"))
		    {
		    	if(link.lastIndexOf("/")==link.length()-1)
					url=link+url.replaceFirst("\\./", "");
				else if(link.lastIndexOf("/")!=6)
					url=link.substring(0,link.lastIndexOf("/"))+"/"+url.replaceFirst("\\./", "");
				else
					url=link+"/"+url.replaceFirst("\\./", "");
		    }
			else{
				if(link.lastIndexOf("/")==link.length()-1)
					url=link+url;
				else if(link.lastIndexOf("/")!=6)
					url=link.substring(0,link.lastIndexOf("/"))+"/"+url;
				else
					url=link+"/"+url;
			}
			return url;
		}
		return url;
	}
	
    private static HTMLDocumentImpl getStreamSourceDocument(String url,String encoding,String proxyHost,String proxyPort)
	{
		LocalUserAgentContext uacontext = new LocalUserAgentContext();
		uacontext.setScriptingEnabled(false);
		uacontext.setExternalCSSEnabled(false);
        DocumentBuilderImpl builder = new DocumentBuilderImpl(uacontext);
       // 构造HttpClient的实例
		HttpClient httpClient = new HttpClient();
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT,"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0");
		//代理设置
		if(proxyHost!=null&&proxyHost.trim().length()>0)
		{
			int port=80;
    		try{
    			port=Integer.parseInt(proxyPort);
    		}catch(Exception e)
    		{
    			port=80;
    		}
			httpClient.getHostConfiguration().setProxy(proxyHost, port);
		}
		
//	
//		//IP地址绑定
//		if(bindIp.length()!=0)
//		{
//			try {
//				httpClient.getHostConfiguration().setLocalAddress(InetAddress.getByName(bindIp));
//			} catch (UnknownHostException e) {
//				log.fatal("IP绑定失败，使用默认IP地址");
//			}
//		}
		
		HTMLDocumentImpl document=null;
		InputStream in=null;
		try {
				// 创建GET方法的实例
				GetMethod getMethod = new GetMethod(url);

				// 使用系统提供的默认的恢复策略
				getMethod.getParams().setParameter(HttpMethodParams.RETRY_HANDLER,
					new DefaultHttpMethodRetryHandler());
				httpClient.executeMethod(getMethod);
				in = getMethod.getResponseBodyAsStream();		     
	            Reader reader = new InputStreamReader(in,encoding);
	            InputSourceImpl inputSource = new InputSourceImpl(reader);
//	            document = (HTMLDocumentImpl)builder.createDocument(inputSource);
	            document = (HTMLDocumentImpl)builder.parse(inputSource);
	            if(document==null)
	            	throw new Exception();
			}catch (Exception e) {
				System.out.println("网页解析错误");
//				e.printStackTrace();
			}finally{
				try {
					if(in!=null)
						in.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} 
			
        return document;
	}
    
    private static class LocalUserAgentContext extends SimpleUserAgentContext {
		@Override
		public void setScriptingOptimizationLevel(int level) {
			super.setScriptingOptimizationLevel(level);
		}

		@Override
		public void setExternalCSSEnabled(boolean enabled) {
			// TODO Auto-generated method stub
			super.setExternalCSSEnabled(enabled);
		}

		@Override
		public void setScriptingEnabled(boolean enable) {
			// TODO Auto-generated method stub
			super.setScriptingEnabled(enable);
		}

		@Override
		public boolean isScriptingEnabled() {
			// We don't need Javascript for this.
			return false;
		}

		@Override
		public boolean isExternalCSSEnabled() {
			// We don't need to load external CSS documents.
			return false;
		}

//		@Override
//		public String getUserAgent() {
//			return "Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0";
//		}		
	}
    
	public static void main(String[] args)
	{
		getCharset("http://www.youmaker.com/video/video?t=d&k=mc","127.0.0.1","9666");
		System.out.println(parserPicUrl("http://www.youku.com",null,null)+"~~~~~~~~~~~~~~~");
	}

}
