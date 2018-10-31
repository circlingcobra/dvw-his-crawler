package com.dvw.search.crawler.util;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * 配置类
 * @author David.Wang
 *
 */
public class PropertiesUtil {

	//数据库驱动
	private static String driver;
	//数据库服务器Crawler库url
	private static String crawlerUrl;
	//数据库服务器Input库url
	private static String inputUrl;
	//数据库用户名
	private static String username;
	//数据库用户密码
	private static String password;
	//种子最大线程数
	private static int maxThread;
	//站点线程数
	private static int siteThread;
	//爬虫运行最大层数
	private static int maxLayer;
	//爬虫最大运行时间,0为不限制时间，单位：分钟
	private static int maxTime;
	//是否在链接过多时忽略新增链接
	private static boolean ignoreLink;
	//未解析页面为多少时忽略新增链接
	private static int ignoreLinkNumber;
	//线程池连接数
    private static int connectionPoolSize;
    //线程池允许最大闲置连接数
    private static int connectionPoolMaxIdle;
    //是否添加搜索图片功能 1--搜索图片 0或其他 不添加
    private static boolean searchImage;
    //解析器类路径，通过类路径使用自己的解析器
    private static String parserClass;
    //URL访问类路径，通过类路径使用自己的URL访问类
    private static String netWorkClass;
	//Sets the timeout until a connection is etablished.
	private static int connectionTimeOut;
	//Sets the default socket timeout (SO_TIMEOUT) in milliseconds which is the timeout for waiting for data. A timeout value of zero is interpreted as an infinite timeout. 
	private static int socketTimeOut;
	//Sets the maximum number of connections to be used for the given host config.
	private static int maxConnectionPerHost;
	//Sets the maximum number of connections allowed.
	private static int maxTotalConnections;
	//是否设置代理，0不设置
	private static boolean proxy;
	//代理ip
	private static String proxyHost;
	//代理端口
	private static int proxyPort;
	//代理用户名
	private static String proxyUser;
	//代理密码
	private static String proxyPassword;
	//IP出口绑定
	private static String bindIp;
	//是否将正则匹配节目加入链接表
	private static boolean regProgramAsLink;
	//是否属于wap站点
	private static boolean isWap;
	//是否按照网页关键词进行节目挖掘
	private static boolean isKeywordMatch;
	//关键词匹配个数
	private static int keywordNum;
	
	private static final Properties pro=new Properties();
	
	static{
		loadProp();
		System.out.println("参数初始化结束");
	}
	
	public static void loadProp()
	{
		InputStream in=ClassLoader.getSystemResourceAsStream("conf/crawler.properties");
		try {
			pro.load(in);
			driver=pro.getProperty("jdbc.driver");
			crawlerUrl=pro.getProperty("jdbc.crawlerUrl");
			inputUrl=pro.getProperty("jdbc.inputUrl");
			username=pro.getProperty("jdbc.username");
			password=pro.getProperty("jdbc.password");
			maxThread=Integer.parseInt(pro.getProperty("maxThread"));
			siteThread=Integer.parseInt(pro.getProperty("siteThread"));
			maxLayer=Integer.parseInt(pro.getProperty("maxLayer"));
			maxTime=Integer.parseInt(pro.getProperty("maxTime"));
			connectionPoolSize=Integer.parseInt(pro.getProperty("connectionPoolSize"));
			connectionPoolMaxIdle=Integer.parseInt(pro.getProperty("connectionPoolMaxIdle"));
			searchImage=Boolean.parseBoolean(pro.getProperty("searchImage","false"));
			parserClass=pro.getProperty("parserClass","crawlerWap.parser.parserPageImpl.HtmlParserPage");
			netWorkClass=pro.getProperty("netWorkClass", "crawlerWap.parser.netWorkImpl.HttpClientNetWork");
			ignoreLink=Boolean.parseBoolean(pro.getProperty("ignoreLink"));
			ignoreLinkNumber=Integer.parseInt(pro.getProperty("ignoreLinkNumber"));
			connectionTimeOut=Integer.parseInt(pro.getProperty("connectionTimeOut"));
			socketTimeOut=Integer.parseInt(pro.getProperty("socketTimeOut"));
			maxConnectionPerHost=Integer.parseInt(pro.getProperty("maxConnectionPerHost"));
			maxTotalConnections=Integer.parseInt(pro.getProperty("maxTotalConnections"));
			proxy=Boolean.parseBoolean(pro.getProperty("proxy","false"));
			proxyHost=pro.getProperty("proxyHost","localhost");
			proxyPort=Integer.parseInt(pro.getProperty("proxyPort","80"));
			proxyUser=pro.getProperty("proxyUser","");
			proxyPassword=pro.getProperty("proxyPassword","");
			bindIp=pro.getProperty("bindIp","");
			regProgramAsLink=Boolean.parseBoolean(pro.getProperty("regProgramAsLink","false"));
			isWap=Boolean.parseBoolean(pro.getProperty("isWap","false"));
			isKeywordMatch=Boolean.parseBoolean(pro.getProperty("isKeywordMatch","false"));
			keywordNum=Integer.parseInt(pro.getProperty("keywordNum","5"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static String getDriver() {
		return driver;
	}

	public static String getCrawlerUrl() {
		return crawlerUrl;
	}

	public static String getInputUrl() {
		return inputUrl;
	}

	public static String getUsername() {
		return username;
	}

	public static String getPassword() {
		return password;
	}

	public static int getMaxThread() {
		return maxThread;
	}

	public static int getSiteThread() {
		return siteThread;
	}

	public static int getMaxLayer() {
		return maxLayer;
	}

	public static int getMaxTime() {
		return maxTime;
	}

	public static boolean isIgnoreLink() {
		return ignoreLink;
	}

	public static int getIgnoreLinkNumber() {
		return ignoreLinkNumber;
	}

	public static int getConnectionPoolSize() {
		return connectionPoolSize;
	}

	public static int getConnectionPoolMaxIdle() {
		return connectionPoolMaxIdle;
	}

	public static boolean isSearchImage() {
		return searchImage;
	}

	public static int getConnectionTimeOut() {
		return connectionTimeOut;
	}

	public static int getSocketTimeOut() {
		return socketTimeOut;
	}

	public static int getMaxConnectionPerHost() {
		return maxConnectionPerHost;
	}

	public static int getMaxTotalConnections() {
		return maxTotalConnections;
	}

	public static String getParserClass() {
		return parserClass;
	}

	public static boolean isProxy() {
		return proxy;
	}

	public static String getProxyHost() {
		return proxyHost;
	}

	public static int getProxyPort() {
		return proxyPort;
	}

	public static String getProxyUser() {
		return proxyUser;
	}

	public static String getProxyPassword() {
		return proxyPassword;
	}
	
	public static String getBindIp() {
		return bindIp;
	}

	public static boolean isRegProgramAsLink() {
		return regProgramAsLink;
	}

	public static boolean isWap() {
		return isWap;
	}

	public static boolean isKeywordMatch() {
		return isKeywordMatch;
	}

	public static int getKeywordNum() {
		return keywordNum;
	}

	public static String getNetWorkClass() {
		return netWorkClass;
	}

}
