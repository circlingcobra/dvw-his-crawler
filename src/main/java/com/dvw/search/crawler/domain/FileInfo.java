package com.dvw.search.crawler.domain;

/**
 * 实体类，和input库中t_fileinfo表对应
 * @author David.Wang
 *
 */
public class FileInfo {

	//节目类型id
	private int fileinfo_id;
	//节目类型
	private String postfix;
	//节目大小
	private int length;
	
	public FileInfo(int fileinfo_id,String postfix)
	{
		this.fileinfo_id=fileinfo_id;
		this.postfix=postfix;
	}
	
	public int getFileinfo_id() {
		return fileinfo_id;
	}
	public void setFileinfo_id(int fileinfo_id) {
		this.fileinfo_id = fileinfo_id;
	}
	public String getPostfix() {
		return postfix;
	}
	public void setPostfix(String postfix) {
		this.postfix = postfix;
	}
	public int getLength() {
		return length;
	}
	public void setLength(int length) {
		this.length = length;
	}
	
	
}
