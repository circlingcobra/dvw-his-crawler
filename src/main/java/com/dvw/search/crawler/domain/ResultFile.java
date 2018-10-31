package com.dvw.search.crawler.domain;

import java.util.Date;

/**
 * 对应t_result_file表
 * @author David.Wang
 *
 */
public class ResultFile {

	//节目URL（正则匹配或符合后缀要求的）
	String fileUrl;
	//标题
	String keyword;
	//正则匹配节目名称
	String anchorText;
	//节目类别id
	int fileinfoId;
	//节目父页面
	String pageUrl;
	//节目所在站点（子站点）
	String site;
	//节目所在主域名
	String domain;
	//节目所在搜索的层数
	int layer;
	//点击数
	int playCount;
	//评论数
	int commentCount;
	//节目上传时间
	Date uploadTime;
	//图片URL
	String picUrl;
	//spare_int1
	int spareInt1;
	//spare_int2
	int spareInt2;
	//spare_int3
	int spareInt3;
	//spare_int4
	int spareInt4;
	//spare_char1
	String spareChar1;
	//spare_char2
	String spareChar2;
	//spare_char3
	String spareChar3;
	//spare_char4
	String spareChar4;
	//spare_char5
	String spareChar5;
	//spare_char6
	String spareChar6;
	//spare_chart7
	String spareChar7;
	//spare_char8
	String spareChar8;
	//spare_char9
	String spareChar9;
	//spare_date1
	Date spareDate1;
	
	public ResultFile() {
		super();
	}
	public String getFileUrl() {
		return fileUrl;
	}
	public void setFileUrl(String fileUrl) {
		this.fileUrl = fileUrl;
	}
	public String getKeyword() {
		return keyword;
	}
	public void setKeyword(String keyword) {
		this.keyword = keyword;
	}
	public String getAnchorText() {
		return anchorText;
	}
	public void setAnchorText(String anchorText) {
		this.anchorText = anchorText;
	}
	public int getFileinfoId() {
		return fileinfoId;
	}
	public void setFileinfoId(int fileinfoId) {
		this.fileinfoId = fileinfoId;
	}
	public String getPageUrl() {
		return pageUrl;
	}
	public void setPageUrl(String pageUrl) {
		this.pageUrl = pageUrl;
	}
	public String getSite() {
		return site;
	}
	public void setSite(String site) {
		this.site = site;
	}
	public int getPlayCount() {
		return playCount;
	}
	public void setPlayCount(int playCount) {
		this.playCount = playCount;
	}
	public int getCommentCount() {
		return commentCount;
	}
	public void setCommentCount(int commentCount) {
		this.commentCount = commentCount;
	}
	public Date getUploadTime() {
		return uploadTime;
	}
	public void setUploadTime(Date uploadTime) {
		this.uploadTime = uploadTime;
	}
	public String getPicUrl() {
		return picUrl;
	}
	public void setPicUrl(String picUrl) {
		this.picUrl = picUrl;
	}
	public int getSpareInt1() {
		return spareInt1;
	}
	public void setSpareInt1(int spareInt1) {
		this.spareInt1 = spareInt1;
	}
	public int getSpareInt2() {
		return spareInt2;
	}
	public void setSpareInt2(int spareInt2) {
		this.spareInt2 = spareInt2;
	}
	public int getSpareInt3() {
		return spareInt3;
	}
	public void setSpareInt3(int spareInt3) {
		this.spareInt3 = spareInt3;
	}
	public int getSpareInt4() {
		return spareInt4;
	}
	public void setSpareInt4(int spareInt4) {
		this.spareInt4 = spareInt4;
	}
	public String getSpareChar1() {
		return spareChar1;
	}
	public void setSpareChar1(String spareChar1) {
		this.spareChar1 = spareChar1;
	}
	public String getSpareChar2() {
		return spareChar2;
	}
	public void setSpareChar2(String spareChar2) {
		this.spareChar2 = spareChar2;
	}
	public String getSpareChar3() {
		return spareChar3;
	}
	public void setSpareChar3(String spareChar3) {
		this.spareChar3 = spareChar3;
	}
	public String getSpareChar4() {
		return spareChar4;
	}
	public void setSpareChar4(String spareChar4) {
		this.spareChar4 = spareChar4;
	}
	public String getSpareChar5() {
		return spareChar5;
	}
	public void setSpareChar5(String spareChar5) {
		this.spareChar5 = spareChar5;
	}
	public String getSpareChar6() {
		return spareChar6;
	}
	public void setSpareChar6(String spareChar6) {
		this.spareChar6 = spareChar6;
	}
	public String getSpareChar7() {
		return spareChar7;
	}
	public void setSpareChar7(String spareChar7) {
		this.spareChar7 = spareChar7;
	}
	public String getSpareChar8() {
		return spareChar8;
	}
	public void setSpareChar8(String spareChar8) {
		this.spareChar8 = spareChar8;
	}
	public String getSpareChar9() {
		return spareChar9;
	}
	public void setSpareChar9(String spareChar9) {
		this.spareChar9 = spareChar9;
	}
	public int getLayer() {
		return layer;
	}
	public void setLayer(int layer) {
		this.layer = layer;
	}

	public ResultFile(String fileUrl, String keyword, String anchorText,
			int fileinfoId, String pageUrl, String site,String domain, int layer,
			int playCount, int commentCount, Date uploadTime, String picUrl,
			int spareInt1, int spareInt2, int spareInt3, int spareInt4,
			String spareChar1, String spareChar2, String spareChar3,
			String spareChar4, String spareChar5, String spareChar6,
			String spareChar7, String spareChar8, String spareChar9,Date spareDate1
			) 
	{
		this.fileUrl = fileUrl;
		this.keyword = keyword;
		this.anchorText = anchorText;
		this.fileinfoId = fileinfoId;
		this.pageUrl = pageUrl;
		this.site = site;
		this.domain=domain;
		this.layer = layer;
		this.playCount = playCount;
		this.commentCount = commentCount;
		this.uploadTime = uploadTime;
		this.picUrl = picUrl;
		this.spareInt1 = spareInt1;
		this.spareInt2 = spareInt2;
		this.spareInt3 = spareInt3;
		this.spareInt4 = spareInt4;
		this.spareChar1 = spareChar1;
		this.spareChar2 = spareChar2;
		this.spareChar3 = spareChar3;
		this.spareChar4 = spareChar4;
		this.spareChar5 = spareChar5;
		this.spareChar6 = spareChar6;
		this.spareChar7 = spareChar7;
		this.spareChar8 = spareChar8;
		this.spareChar9 = spareChar9;
		this.spareDate1 = spareDate1;
	}
	public String getDomain() {
		return domain;
	}
	public void setDomain(String domain) {
		this.domain = domain;
	}
	public Date getSpareDate1() {
		return spareDate1;
	}
	public void setSpareDate1(Date spareDate1) {
		this.spareDate1 = spareDate1;
	}
	
	
	
}
