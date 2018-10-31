package com.dvw.search.crawler.domain;

/**
 * 实体类，和crawler库中的t_task_info表对应
 * @author David.Wang
 *
 */
public class TaskInfo {

	//是否控制搜索范围，1--控制，0--不控制
	private int siteArea;
	//爬虫状态，1--爬虫未运行，2--爬虫正在运行，3--爬虫运行完毕
	//如果爬虫运行过程中被突然中断，若状态为2支持断点续搜
	private int status;
	//爬虫开始运行时间
	private String startTime;
	//爬虫结束运行时间
	private String endTime;
	//爬虫搜索到的节目总数
	private int fileCount;
	//爬虫搜索到的链接总数
	private int pageCount;
	//爬虫搜索到的含节目的站点总数
	private int siteCount;
	
	public TaskInfo(){};
	
	public TaskInfo(int siteArea,int status,String startTime){
		this.siteArea=siteArea;
		this.status=status;
		this.startTime=startTime;
	}

	public int getSiteArea() {
		return siteArea;
	}

	public void setSiteArea(int siteArea) {
		this.siteArea = siteArea;
	}

	public int getStatus() {
		return status;
	}

	public void setStatus(int status) {
		this.status = status;
	}

	public String getStartTime() {
		return startTime;
	}

	public void setStartTime(String startTime) {
		this.startTime = startTime;
	}

	public String getEndTime() {
		return endTime;
	}

	public void setEndTime(String endTime) {
		this.endTime = endTime;
	}

	public int getFileCount() {
		return fileCount;
	}

	public void setFileCount(int fileCount) {
		this.fileCount = fileCount;
	}

	public int getPageCount() {
		return pageCount;
	}

	public void setPageCount(int pageCount) {
		this.pageCount = pageCount;
	}

	public int getSiteCount() {
		return siteCount;
	}

	public void setSiteCount(int siteCount) {
		this.siteCount = siteCount;
	}
	
	
}
