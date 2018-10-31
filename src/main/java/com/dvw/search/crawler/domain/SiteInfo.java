package com.dvw.search.crawler.domain;

/**
 * 实体类，记录某域名下的节目数
 * @author David.Wang
 *
 */
public class SiteInfo {

	//域名url
	private String siteUrl;
	//站点主域名
	private String domain;
	//该域名下搜索到的节目数量
	private int programNum;
	
	public SiteInfo(String siteUrl,String domain,int programNum)
	{
		this.siteUrl=siteUrl;
		this.domain=domain;
		this.programNum=programNum;
	}

	public String getSiteUrl() {
		return siteUrl;
	}

	public int getProgramNum() {
		return programNum;
	}

	public String getDomain() {
		return domain;
	}
	
	
}
