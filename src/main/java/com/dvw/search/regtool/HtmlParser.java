package com.dvw.search.regtool;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.swing.JFrame;

import org.apache.commons.httpclient.DefaultHttpMethodRetryHandler;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.MultiThreadedHttpConnectionManager;
import org.apache.commons.httpclient.methods.GetMethod;
import org.apache.commons.httpclient.params.HttpMethodParams;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;

import com.ibm.icu.text.CharsetDetector;




public class HtmlParser {

	
	
	public static MultiThreadedHttpConnectionManager manager = new MultiThreadedHttpConnectionManager();

	private static int connectionTimeOut =8000;

	private static int socketTimeOut = 5000;

	private static int maxConnectionPerHost = 6;

	private static int maxTotalConnections = 20;


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
		httpClient = new HttpClient();
		httpClient.getParams().setParameter(HttpMethodParams.USER_AGENT,"Mozilla/5.0 (Windows; U; Windows NT 5.1; en-US) AppleWebKit/532.0 (KHTML, like Gecko) Chrome/3.0.195.38 Safari/532.0");
	}
	
	
	
	public static String getParseredString(String url,String proxyHost,String proxyPort)
	{
		String s="";
		try{
			String encoding=getCharset(url,proxyHost,proxyPort);
			Parser parser=getHtmlParser(url,encoding,proxyHost,proxyPort);
			parser.getLexer().getPage().setUrl(url);
			StringBean sb=new StringBean(parser);
			sb.setLinks(false);
			sb.setReplaceNonBreakingSpaces(true);
			sb.setCollapse(true);
//			sb.setURL(url);
			s=sb.getStrings();
//			System.out.println(s);
		}catch(Exception e){
			s=e.getMessage();
		}
		
		return s;
	}
	
	
	public static String parserScript(String url,String proxyHost,String proxyPort)
	{
		String html="";
		NodeFilter filter = new TagNameFilter("SCRIPT");
		Parser parser;
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			parser=getHtmlParser(url,encoding,proxyHost,proxyPort);	
			NodeList list = parser.extractAllNodesThatMatch(filter);
			
			for(int i=0;i<list.size();i++)
			{
				html+=list.elementAt(i).toHtml();
			}
		} catch (ParserException e) {
			e.printStackTrace();
		}
		return html;
	}
	
	
	public static String parserPicUrl(String url,String proxyHost,String proxyPort)
	{
		String linkAll="";
		String link="";
		Parser parser;
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			parser=getHtmlParser(url,encoding,proxyHost,proxyPort);		
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("IMG");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{
				//链接url
				link=(((ImageTag)(list.elementAt(i))).getImageURL().toString()).replaceAll("&amp;", "&");
				linkAll+=link+"\n";
				System.out.println(link);
			}
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			System.out.println("URL--"+url+" 抓取异常");
			e1.printStackTrace();
			return "网页解析异常";
		}
		if(linkAll.length()==0)
			return "该网页没有图片";
		return linkAll;
	}
	
	
	public static String parserPicUrl(String reg,String url,String proxyHost,String proxyPort)
	{
		String link="";
		Parser parser;
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			parser=getHtmlParser(url,encoding,proxyHost,proxyPort);		
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("IMG");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{
				//链接url
				link=(((ImageTag)(list.elementAt(i))).getImageURL().toString()).replaceAll("&amp;", "&");
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
				while(matcher.find())
				{
					System.out.println(link);
					return link;
				}
			}
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			System.out.println("URL--"+url+" 抓取异常");
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
		Parser parser;
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			parser=getHtmlParser(url,encoding,proxyHost,proxyPort);			
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("A");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{				
				thunder=((LinkTag)list.elementAt(i)).getAttribute("thunderHref");
				fg=((LinkTag)list.elementAt(i)).getAttribute("fg");
				if(thunder!=null)
					link=thunder;
				else if(fg!=null)
					link=fg;
				else
					link=(((LinkTag)(list.elementAt(i))).getLink().toString().replaceAll("&amp;", "&"));
//				System.out.println(list.elementAt(i).toHtml());
				linkAll+=link+"\n";				
			}
			if(linkAll.length()==0)
				return "该网页没有链接";
			return linkAll;
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			System.out.println("URL--"+url+" 抓取异常");
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
		Parser parser;
		String encoding=getCharset(url,proxyHost,proxyPort);
		try {
			parser=getHtmlParser(url,encoding,proxyHost,proxyPort);	
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("A");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{				
				thunder=((LinkTag)list.elementAt(i)).getAttribute("thunderHref");
				fg=((LinkTag)list.elementAt(i)).getAttribute("fg");
				if(thunder!=null)
					link=thunder;
				else if(fg!=null)
					link=fg;
				else
					link=(((LinkTag)(list.elementAt(i))).getLink().toString().replaceAll("&amp;", "&"));
//				System.out.println(list.elementAt(i).toHtml());
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
				while(matcher.find())
				{			
//					if(link.toLowerCase().indexOf("thunder")>=0)
//						link=this.getThunderParsedLink(link);
//					else if(link.toLowerCase().indexOf("flashget")>=0)
//						link=this.getFlashGetParsedLink(link);
					
					linkAll+=link+",";
					if(linkAll.length()>4096)
						return linkAll.substring(0,linkAll.length()-1);
				}
			}
			if(linkAll.equals(","))
				return "";
			return linkAll.substring(0,(linkAll.length()-1)<0?0:linkAll.length()-1);
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			System.out.println("URL--"+url+" 抓取异常");
			e1.printStackTrace();
		}
				
		return null;
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
				CharsetDetector cd=new CharsetDetector();
				URL path = new URL(url);
				HttpURLConnection urlconn = (HttpURLConnection) path.openConnection();
//				urlconn.setConnectTimeout(PropertiesUtil.connectionTimeOut);
				urlconn.setRequestProperty("User-Agent", "Mozilla/4.0 (compatible; MSIE 7.0; Windows NT 5.1)");
				urlconn.setReadTimeout(4000);
				urlconn.connect();
				cd.setText(new BufferedInputStream(urlconn.getURL().openStream()));
				charset = cd.detect().getName();
				if(charset!=null)
					break;
			}
		} catch (HttpException e) {
		//	logger.info("分析字符集时链接超时。");
		} catch (IOException e) {
			//ignore
		}
		
		if(charset==null||charset.indexOf("windows")>=0||charset.indexOf("8859")>0||charset.indexOf("KR")>=0)
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
			System.out.println(htmlContent);
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

	
	private static StringBuffer html;
	//获取异步信息
	public static String parseByMozswing(String url) {

		html=new StringBuffer("");
		System.setProperty("mozswing.xulrunner.home", "E:\\OpenSource\\mozswing-2.0beta2\\native\\win32\\xulrunner");
//		MozillaWindow win=new MozillaWindow();
//		win.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
//		win.addNotify();
//		
//    	win.setVisible(false);
//    	getImage(win,url);
//    	System.out.println(html.toString());
//    	win.onLoadingEnded();   	
		return html.toString();
	}
	

	
	
    public static void print(Node node) {

        if (node == null) {
            return;
        }

        int type = node.getNodeType();
        switch (type) {
            case Node.DOCUMENT_NODE:
                print(((Document) node).getDocumentElement());
                break;

            case Node.ELEMENT_NODE:
                try {
                    NamedNodeMap attrs = node.getAttributes();
                    if(attrs != null) {
                        for (int i = 0; i < attrs.getLength(); i++) {
 //                           System.out.print(' ');
                            if(attrs.item(i) != null) {
 //                               System.out.print(attrs.item(i).getNodeValue());
                            }
                        }
                        org.w3c.dom.NodeList children = node.getChildNodes();
                        if (children != null) {
                            int len = children.getLength();
                            for (int i = 0; i < len; i++) {
                                print(children.item(i));
                            }
                        }
                        break;
                    }
                } catch (Exception e) {
                	org.w3c.dom.NodeList children = node.getChildNodes();
                    if (children != null) {
                        int len = children.getLength();
                        for (int i = 0; i < len; i++) {
                            print(children.item(i));
                        }
                    }
                }

            case Node.TEXT_NODE:
            	if(node!=null&&node.getNodeValue()!=null&&node.getNodeValue().trim().length()!=0)
            	{
            		html.append(node.getNodeValue()).append("\n");
            	}
            		
 //               System.out.print(node.getNodeValue());
                break;

        }
    }
    
    private static Parser getHtmlParser(String url,String encoding,String proxyHost,String proxyPort) throws ParserException{
    	Parser parser = new Parser();
    	if(proxyHost!=null&&proxyHost.trim().length()>0)
		{
    		System.out.println("设置了代理~~~~~~~~~~~~~~~~~~~~~~~");
    		int port=80;
    		try{
    			port=Integer.parseInt(proxyPort);
    		}catch(Exception e)
    		{
    			port=80;
    		}
    			
			parser.getConnectionManager().setProxyHost(proxyHost);
			parser.getConnectionManager().setProxyPort(port);			
		}
    	parser.setURL(url);
		parser.setEncoding(encoding);	
    	return parser;
    }
    
	public static void main(String[] args)
	{
//		System.out.println(getParseredString("http://www.sina.com")+"~~~~~~~~~~~~~~~");
	}



}
