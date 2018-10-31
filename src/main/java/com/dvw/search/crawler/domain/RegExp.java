package com.dvw.search.crawler.domain;

/**
 * 实体类，和input库中的t_reg_exp表对应
 * @author David.Wang
 *
 */
public class RegExp {

	//正则域名
	private String domain;
	//正则表达式
	private String regex;
	//正则类型
	private int regType;
	//正则用例，当regType=5时，该字段用于表示时间格式
	private String regExample;
	
	public RegExp(String domain,String regex,String regExample,int regType)
	{
		this.domain=domain;
		this.regex=regex;
		this.regExample=regExample;
		this.regType=regType;
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getRegex() {
		return regex;
	}

	public void setRegex(String regex) {
		this.regex = regex;
	}

	public int getRegType() {
		return regType;
	}

	public void setRegType(int regType) {
		this.regType = regType;
	}

	public String getRegExample() {
		return regExample;
	}

	public void setRegExample(String regExample) {
		this.regExample = regExample;
	}
	
}
