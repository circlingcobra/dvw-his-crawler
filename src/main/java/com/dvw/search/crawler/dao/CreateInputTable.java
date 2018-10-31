package com.dvw.search.crawler.dao;

import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

import com.dvw.search.crawler.util.ConnectionUtil;



public class CreateInputTable {

	Connection conn=null;
	Statement stmt=null;
	
	
	public void createSeedSite() throws SQLException
	{
		conn=ConnectionUtil.getInputConnection();
		stmt=conn.createStatement();
		stmt.execute("create table t_seedsite(id int auto_increment primary key,site_url varchar(64) not null,domain varchar(64) default '',reg_filter varchar(64) default '',sub_charset int(2) default 0,task_id int(11) default 0,layer int(11) default 0)");
		stmt.close();
	}
	
	public void createSiteArea() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("create table t_site_area(id int auto_increment primary key,site_url varchar(64) not null,task_id int(11) default 0 )");
		stmt.close();
	}
	
	public void createFileInfo() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("CREATE TABLE `t_fileinfo` (`fileinfo_id` int(11) NOT NULL auto_increment,`postfix` varchar(10) NOT NULL default '',`length` int(11) NOT NULL default '1',`task_id` int(11) NOT NULL default '0',  PRIMARY KEY  (`fileinfo_id`),UNIQUE KEY `fileinfo_id` (`fileinfo_id`),  UNIQUE KEY `postfix` (`postfix`)) ENGINE=MyISAM AUTO_INCREMENT=4220");
		stmt.close();
	}
	
	public void createProtocolInfo() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("CREATE TABLE `t_protocol_info` (`FILEINFO_ID` int(11) NOT NULL auto_increment,`PROTOCOL` varchar(10) NOT NULL,`LENGTH` mediumint(9) NOT NULL default '0',PRIMARY KEY  (`FILEINFO_ID`),UNIQUE KEY `PROTOCOL` (`PROTOCOL`)) ENGINE=MyISAM AUTO_INCREMENT=5");
		stmt.close();
	}
	
	public void createKeyword() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("create table t_keywords(id int auto_increment primary key,keyword varchar(128) not null,task_id int(11) default 1 )");
		stmt.close();
	}
	
	public void createRegExp() throws SQLException
	{
		stmt=conn.createStatement();
		stmt.execute("CREATE TABLE `t_reg_exp` (`REG_ID` int(10) unsigned NOT NULL auto_increment,`SITE_URL` varchar(100) NOT NULL default '',`REG_EXP` varchar(512) NOT NULL default '',`EXAMPLE` varchar(512) NOT NULL default '',`REG_TYPE` tinyint(4) NOT NULL default '1',PRIMARY KEY  (`REG_ID`))");
		stmt.close();
	}
	

	public static void main(String[] args) throws SQLException
	{
		CreateInputTable cit=new CreateInputTable();
		cit.createSeedSite();
		cit.createSiteArea();
		cit.createFileInfo();
		cit.createProtocolInfo();
		cit.createRegExp();
		cit.createKeyword();
		cit.conn.close();
	}
}
