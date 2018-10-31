package com.dvw.search.crawler.util;


import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.MalformedURLException;
import java.net.Socket;
import java.net.SocketAddress;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * Url信息获取工具类
 * @author David.Wang
 *
 */
public class UrlCheckUtil {

	private static Log log = LogFactory.getLog(UrlCheckUtil.class);
	
	
	public UrlCheckUtil() {
		
	}

	
	

	public static class DomainPostfixs {
		//通用顶级域名列表
		public static String[] postfixs = { "com", "org", "net", "edu", "mil", "int", "pro", "idv", 
			"biz", "museum", "coop", "aero", "info", "name", "cc", "tv","gov"
			,"travel","asia","jobs","mobi","tel","cat","gd"};
		//国家地理域名缩写
		public static String[] locaion_post_fixs={
			"ac","ad","ae","af","ag","ai","al","am","an","ao","aq","ar","as","at","au"
			,"aw","ax","az","ba","bb","bd","be","bf","bg","bh","bi","bj","bm","bn","bo"
			,"br","bs","bt","bv","bw","by","bz","ca","cc","cd","cf","cg","ch","ci","ck"
			,"cl","cm","cn","co","cr","cu","cv","cx","cy","cz","de","dj","dk","dm","do"
			,"dz","ec","ee","eg","eh","er","es","et","eu","fi","fj","fk","fm","fo","fr","ga"
			,"gb","gd","ge","gf","gg","gh","gi","gl","gm","gn","gp","gq","gr","gs","gt"
			,"gu","gw","gy","hk","hm","hn","hr","ht","hu","id","ie","il","im","in","io"
			,"iq","ir","is","it","je","jm","jo","jp","ke","kg","kh","ki","km","kn","kp"
			,"kr","kw","ky","kz","la","lb","lc","li","lk","lr","ls","lt","lu","lv","ly"
			,"ma","mc","md","me","mg","mh","mk","ml","mm","mn","mo","mp","mq","mr","ms"
			,"mt","mu","mv","mw","mx","my","mz","na","nc","ne","nf","ng","ni","nl","no"
			,"np","nr","nu","nz","om","pa","pe","pf","pg","ph","pk","pl","pm","pn","pr"
			,"ps","pt","pw","py","qa","re","ro","rs","ru","rw","sa","sb","sc","sd","se"
			,"sg","sh","si","sj","sk","sl","sm","sn","so","sr","st","su","sv","sy","sz"
			,"tc","td","tf","tg","th","tj","tk","tl","tm","tn","to","tp","tr","tt","tv"
			,"tw","tz","ua","ug","uk","um","us","uy","uz","va","vc","ve","vg","vi","vn"
			,"vu","wf","ws","ye","yt","yu","za","zm","zw"};


		static String[] getPostfixs() {
			return postfixs;
		}
		
		/**
		 * 检测该域名是否属于通用顶级域名列表
		 * @param domainSegment
		 * @return
		 */
		public static String findPostfixs(String domainSegment) {
			for (String postfix : postfixs) {
				if (postfix.equalsIgnoreCase(domainSegment)) {
					return postfix;
				}
			}
			return null;
		}
		/**
		 * 检测该域名是否属于国家地理域名
		 * @param domainSegment
		 * @return
		 */
		public static boolean isLocaionPostfixs(String domainSegment) {
			String result=null;
			for (String postfix : locaion_post_fixs) {
				if (postfix.equalsIgnoreCase(domainSegment)) {
					result = postfix;
				}
			}
			return null!=result;
		}
	}

	
	
	/**
	 * 判断链接类型，是否为网页
	 * @param url
	 * @return
	 */
	public static boolean isHtml(String url)
	{
		URL u;
		try {
			u = new URL(url);
			HttpURLConnection urlConn = (HttpURLConnection) u.openConnection();
			String type = urlConn.getContentType();
			if (type == null || type.length() <= 5) {
				type = "unknownUrlType";
			}
			// System.out.print(urlConn.getResponseCode());
			if (type.toLowerCase().indexOf("htm") != -1){
				return true;
			}
		} catch (MalformedURLException e) {
			// TODO Auto-generated catch block
			log.fatal("URL格式出错");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			log.fatal("IO异常");
		}		
			
		return false;
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
			log.fatal("错误链接");
			domain="";
		}
		
		return domain;
	}
	
	
	
	/**
	 * 去掉url中的端口号字符串 url不可以带诸如http://的协议头，只能
	 * 是www.sina.com.cn:80之类的格式
	 * @param url
	 * @return
	 */
	public static String removeUrlPort(String strTmp) {
		if (strTmp.indexOf(':')<0) {
			return strTmp;
		} else {
			return strTmp.substring(0, strTmp.indexOf(':'));
		}
	}
	
	
	/**
	 * 获取种子主域名
	 * @param seed
	 * @return
	 */
	public static String getSeedDomain(String domainTmp){
		if(domainTmp.indexOf("/")>0)
			domainTmp=domainTmp.substring(0,domainTmp.indexOf("/"));
		//如果包含了端口信息，先去掉端口信息
		if(domainTmp.indexOf(":")>0){
			domainTmp = removeUrlPort(domainTmp);
		} 
		String[] domain_segments = domainTmp.split("\\.");
		String domainReturn = null;

		if (domain_segments.length <= 1) {
			return null;
		} 
		int lastIndex = domain_segments.length - 1;
//		if (domain_segments[lastIndex].length() == 2 
//				&& !domain_segments[lastIndex].equals("tv")
//				&& !domain_segments[lastIndex].equals("cc") 
//				&& !Character.isDigit(domain_segments[lastIndex].charAt(0))
//				&& !Character.isDigit(domain_segments[lastIndex].charAt(0))) {
//			domainReturn = domain_segments[lastIndex-1];
//			domainReturn += ".";
//			domainReturn += domain_segments[lastIndex];
//
//			if (domain_segments.length == 2) {
//				return domainReturn; // url = "sina.cn" return "sina.cn"
//			} else if (null != DomainPostfixs.findPostfixs(domain_segments[lastIndex-1])) {
//				domainReturn = domain_segments[lastIndex-2] + "." + domainReturn;
//			}
//			return domainReturn;
//		}
		//通用顶级域名结尾的域名 .com
		if (null != DomainPostfixs.findPostfixs(domain_segments[lastIndex])) {
			domainReturn = domain_segments[lastIndex-1] + "." + domain_segments[lastIndex];
			return domainReturn;
		}
		//国家地理域名结尾的处理 .cn
		if (DomainPostfixs.isLocaionPostfixs(domain_segments[lastIndex])) {
			//如果下一级域名属于通用顶级域名 sina.com.cn
			if (null != DomainPostfixs.findPostfixs(domain_segments[lastIndex-1])) {
				domainReturn = domain_segments[lastIndex-2] + "." +domain_segments[lastIndex-1] + "." + domain_segments[lastIndex];
				return domainReturn;
			}
			//sina.cn
			domainReturn = domain_segments[lastIndex-1] + "." + domain_segments[lastIndex];
			return domainReturn;
		}
		return null;
	}
	
	/**
	 * 获取站点ip地址
	 * @param url
	 * @return
	 */
	public static String getIPAddress(String url)
	{
		String ip="";
		Socket socket=new Socket();
		SocketAddress remoteAddr=new InetSocketAddress(url,80);
		try {
			socket.connect(remoteAddr,60000);
			ip=socket.getRemoteSocketAddress().toString();
			ip=ip.substring(url.length()+1,ip.length()-3);
			log.info(ip);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			return ip;
		}
		
		return ip;
	}
	
	
	/**
	 * @param url    待解析的URL
	 * @param link   链接父页面URL
	 * @param site   链接所在站点
	 * @return
	 */
	public static String getFullPath(String url,String link,String site)
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
		    }else if(url.toLowerCase().startsWith("ed2k"))
		    {

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
	

	/**
	 * 返回wap站点的主域名
	 * @param seed
	 * @param domain
	 * @return
	 */
	public static String getWapDomain(String seed, String domain) {
		
		if(domain!=null&&domain.trim().length()>0)
			return domain;
		if(seed.indexOf("/")>0)
			seed=seed.substring(0,seed.indexOf("/"));
		return seed;
	}
	
	
}
