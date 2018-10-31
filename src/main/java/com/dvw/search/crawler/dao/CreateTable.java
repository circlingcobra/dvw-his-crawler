package com.dvw.search.crawler.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.dvw.search.crawler.util.ConnectionUtil;



public class CreateTable {

	Connection conn=null;
	Statement stmt=null;
	
	public void createHttps() throws SQLException
	{
		conn=ConnectionUtil.getCrawlerConnection();
		for(int i=0;i<10;i++)
		{
			stmt=conn.createStatement();
			stmt.execute("create table http_"+i+" (domain varchar(128),site varchar(128),page_url varchar(256) not null,file_url varchar(255),file_name varchar(256),pic_url varchar(512),status int(1) default 0,layer int(2) default 0,hashCode int not null,PRIMARY KEY  (`file_url`))");
		}
		stmt.close();
	}
	
	public void createHttpsIndex() throws SQLException
	{
		for(int i=0;i<10;i++)
		{
			stmt=conn.createStatement();
			stmt.execute("create index index_http_"+i+" on http_"+i+" (file_url)");
		}
		stmt.close();
	}
	
	public void createTotalHttp() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("create view totalhttp as select * from http_0 union select * from http_1 union select * from http_2 union select * from http_3 union select * from http_4 union select * from http_5 union select * from http_6 union select * from http_7 union select * from http_8 union select * from http_9");
		stmt.close();
	}
	
	public void createFiles() throws SQLException
	{
		for(int i=0;i<10;i++)
		{
			stmt=conn.createStatement();
			stmt.execute("create table file_"+i+"(file_url varchar(255) primary key,keyword varchar(512) default ' ',anchor_text varchar(512) default ' ',fileinfo_id int(11) default 0,page_url varchar(512) not null,site varchar(128) not null,domain varchar(128) not null,status int(11) default 0,get_time timestamp default current_timestamp,play_count int(11) default 0,comment_count int(11) default 0,upload_time datetime ,file_content varchar(512) default ' ',pic_url varchar(512) default ' ',spare_int1 int(11) default 0,spare_int2 int(11) default 0,spare_int3 int(11) default 0,spare_int4 int(11) default 0,spare_char1 varchar(256) default ' ',spare_char2 varchar(256) default ' ',spare_char3 varchar(512) default ' ',spare_char4 text(4096),spare_char5 varchar(512) default ' ',spare_char6 varchar(512) default ' ',spare_char7 varchar(512) default ' ',spare_char8 varchar(512) default ' ',spare_char9 varchar(512) default ' ',spare_date1 datetime,layer int(2) default 0,hashCode int not null)ENGINE=MyISAM DEFAULT CHARSET=utf8 ");
		}
		stmt.close();
	}
	
	public void createFilesIndex() throws SQLException
	{
		for(int i=0;i<10;i++)
		{
			stmt=conn.createStatement();
			stmt.execute("create index index_file_"+i+" on file_"+i+" (file_url)");
		}
		stmt.close();
	}
	
	public void createResultFile() throws SQLException
	{
		stmt=conn.createStatement();
//		stmt.execute("create view t_result_file as select * from file_0 union select * from file_1 union select * from file_2 union select * from file_3 union select * from file_4 union select * from file_5 union select * from file_6 union select * from file_7 union select * from file_8 union select * from file_9");
		stmt.execute("create table t_result_file(file_url varchar(255) primary key,keyword varchar(512) default ' ',anchor_text varchar(512) default ' ',fileinfo_id int(11) default 0,page_url varchar(512) not null,site varchar(128) not null,domain varchar(128) not null,status int(11) default 0,get_time timestamp default current_timestamp,play_count int(11) default 0,comment_count int(11) default 0,upload_time datetime ,file_content varchar(512) default ' ',pic_url varchar(512) default ' ',spare_int1 int(11) default 0,spare_int2 int(11) default 0,spare_int3 int(11) default 0,spare_int4 int(11) default 0,spare_char1 varchar(256) default ' ',spare_char2 varchar(256) default ' ',spare_char3 varchar(512) default ' ',spare_char4 text(4096),spare_char5 varchar(512) default ' ',spare_char6 varchar(512) default ' ',spare_char7 varchar(512) default ' ',spare_char8 varchar(512) default ' ',spare_char9 varchar(512) default ' ',spare_date1 datetime,layer int(2) default 0,hashCode int not null )");
//				" ENGINE=MERGE DEFAULT CHARSET=utf8 UNION=(`file_0`,`file_1`,`file_2`,`file_3`,`file_4`,`file_5`,`file_6`,`file_7`,`file_8`,`file_9`)");
		stmt.close();
	}
	
	public void createTaskInfo() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("CREATE TABLE `t_task_info` ( `task_id` int(2) NOT NULL auto_increment,`site_area` int(2) default '1',`status` int(2) default '1',  `start_time` varchar(12) default NULL,`end_time` varchar(12) default NULL,`file_count` int(11) default '0',`page_count` int(11) default '0',`site_count` int(11) default '0',PRIMARY KEY  (`task_id`))");
		stmt.close();
	}
	
	public void createResultSite() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("CREATE TABLE `t_result_site` (`site_url` varchar(128) NOT NULL,`domain` varchar(128) NOT NULL,`ip_address` varchar(64) default ' ', `site_name` varchar(256) default ' ', `page_num` int(11) default '0',`get_time` timestamp NOT NULL default CURRENT_TIMESTAMP,  `status` int(11) default '0',  PRIMARY KEY  (`site_url`)) ");
		stmt.close();
	}
	
	public void createTaskInfoRecord() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("insert into t_task_info values()");
		stmt.close();
	}
	/**
	 * @param args
	 * @throws SQLException 
	 */
	public static void main(String[] args) throws SQLException {
		// TODO Auto-generated method stub
		CreateTable ct=new CreateTable();
		ct.createHttps();
//		ct.createHttpsIndex();
		ct.createTotalHttp();
//		ct.createFiles();
//		ct.createFilesIndex();
		ct.createResultFile();
		ct.createTaskInfo();
		ct.createResultSite();
		ct.createTaskInfoRecord();
		ct.conn.close();
	}

}
