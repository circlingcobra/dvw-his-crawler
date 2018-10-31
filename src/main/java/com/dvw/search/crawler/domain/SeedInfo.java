package com.dvw.search.crawler.domain;

import java.util.HashSet;

/**
 * 种子，对应input库 t_seedsite表
 * @author David.Wang
 *
 */
public class SeedInfo {
	
	//种子id,用于定位爬虫关联哪张表
	private int id;
	//种子
	private String seed;
	//种子所在主域名
	private String domain;
	//链接过滤正则，过滤掉链接中影响业务的部分
	private String regFilter;
	//是否对所有子域名分别探测其字符集，0--网页使用的字符集统一为入口字符集，1--网页使用的字符集为当前子域名的字符集
	private int subCharset; 
	//种子状态 0-未运行  1-已运行
	private int taskId;
	//种子运行层数
	private int layer;
	
	//============以下为非与数据库对应的全局变量（逻辑用）
	//站点字符集
	private String charset;
	//站点链接所存储的http_$tableNumber
	private int tableNumber;
	//站点对应url链表结合
	private HashSet<Integer> hashLink;

	public HashSet<Integer> getHashLink() {
		return hashLink;
	}

	public void setHashLink(HashSet<Integer> hashLink) {
		this.hashLink = hashLink;
	}

	public SeedInfo(int id,String seed,String domain,String regFilter,int subCharset,int taskId,int layer)
	{
		this.id=id;
		this.seed=seed;
		this.domain=domain;
		this.regFilter=regFilter;
		this.subCharset=subCharset;
		this.taskId=taskId;
		this.layer=layer;
	}

	public String getSeed() {
		return seed;
	}

	public void setSeed(String seed) {
		this.seed = seed;
	}

	public int getTaskId() {
		return taskId;
	}

	public void setTaskId(int taskId) {
		this.taskId = taskId;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getRegFilter() {
		return regFilter;
	}

	public void setRegFilter(String regFilter) {
		this.regFilter = regFilter;
	}

	public String getCharset() {
		return charset;
	}

	public void setCharset(String charset) {
		this.charset = charset;
	}

	public int getTableNumber() {
		return tableNumber;
	}

	public void setTableNumber(int tableNumber) {
		this.tableNumber = tableNumber;
	}

	public int getSubCharset() {
		return subCharset;
	}

	public void setSubCharset(int subCharset) {
		this.subCharset = subCharset;
	}
	
}
