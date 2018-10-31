package com.dvw.search.crawler.util;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import org.apache.commons.dbcp.BasicDataSource;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;


/**
 * 链接工厂
 * @author David.Wang
 *
 */
public class ConnectionUtil {

	private static Log log=LogFactory.getLog(ConnectionUtil.class);
	//DBCP连接池
	private static BasicDataSource basicDataSource = null;
	
	static{
		initDataSourcePool();
		log.info("数据库连接池初始化完毕");
	}
	
	private static void initDataSourcePool(){
		basicDataSource=new BasicDataSource();
		//设置驱动
		basicDataSource.setDriverClassName(PropertiesUtil.getDriver());
		//数据库URL
		basicDataSource.setUrl(PropertiesUtil.getCrawlerUrl());
		//数据库用户名
		basicDataSource.setUsername(PropertiesUtil.getUsername());
		//数据库密码
		basicDataSource.setPassword(PropertiesUtil.getPassword());
		//连接池初始连接个数
		basicDataSource.setInitialSize(PropertiesUtil.getConnectionPoolSize());
		//连接池最大并发提供的连接数
		basicDataSource.setMaxActive(PropertiesUtil.getConnectionPoolSize());
		//连接池允许最大闲置连接数，超过这个数值连接池会释放连接
		basicDataSource.setMaxIdle(PropertiesUtil.getConnectionPoolMaxIdle());
		//连接等待时间(ms)，超时则报异常，0 代表无穷等待
		basicDataSource.setMaxWait(0);
	}
	/**
	 * 获取crawler库的数据库链接
	 * @return
	 */
	public static synchronized Connection getCrawlerConnection()
	{
		Connection connection=null;
		try {
			connection = basicDataSource.getConnection();
		} catch (SQLException e) {
			log.fatal("================================================================\n" +
					"链接获取异常，目前连接池状态：\n" +
					"目前分配出的连接数："+basicDataSource.getNumActive()+
					"目前闲置连接数："+basicDataSource.getNumIdle());
			e.printStackTrace();
		}
		return connection;
	}
	
	/**
	 * 获取input库的数据库链接
	 * @return
	 */
	public static synchronized Connection getInputConnection()
	{
		Connection conn=null;
		try {
			Class.forName(PropertiesUtil.getDriver());
			conn=DriverManager.getConnection(PropertiesUtil.getInputUrl(),PropertiesUtil.getUsername(),PropertiesUtil.getPassword()); 
		} catch (ClassNotFoundException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return conn;
	}

	public static BasicDataSource getBasicDataSource() {
		return basicDataSource;
	}

	public static void setBasicDataSource(BasicDataSource basicDataSource) {
		ConnectionUtil.basicDataSource = basicDataSource;
	}

}
