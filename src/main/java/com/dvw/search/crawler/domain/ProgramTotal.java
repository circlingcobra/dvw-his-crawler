package com.dvw.search.crawler.domain;

/**
 * 实体类，和crawler库中的totalhttp表对应
 * @author David.Wang
 *
 */
public class ProgramTotal {

	//节目所在域名
	private String domain;
	//节目所在站点
	private String site;
	//节目父页面
	private String page_url;
	//父页面title
	private String page_title;
	//节目url
	private String file_url;
	//节目名称
	private String file_name;
	//节目图片链接
	private String pic_url;
	//网页encoding
	private String encoding;
	//链接所在层数
	private int layer;
	//fileinfo_id
	private int fileinfoId;
	
	public int getFileinfoId() {
		return fileinfoId;
	}

	public void setFileinfoId(int fileinfoId) {
		this.fileinfoId = fileinfoId;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}

	public ProgramTotal(String domain,String site,String page_url,String page_title,
			String file_url,String file_name,String pic_url,String encoding,int layer){
		this.domain=domain;
		this.site=site;
		this.page_url=page_url;
		this.page_title=page_title;
		this.file_url=file_url;
		this.file_name=file_name;
		this.pic_url=pic_url;
		this.encoding=encoding;
		this.layer=layer;
	}

	public String getSite() {
		return site;
	}

	public void setSite(String site) {
		this.site = site;
	}

	public String getPage_url() {
		return page_url;
	}

	public void setPage_url(String page_url) {
		this.page_url = page_url;
	}

	public String getFile_url() {
		return file_url;
	}

	public void setFile_url(String file_url) {
		this.file_url = file_url;
	}

	public String getFile_name() {
		return file_name;
	}

	public void setFile_name(String file_name) {
		this.file_name = file_name;
	}

	public String getPic_url() {
		return pic_url;
	}

	public void setPic_url(String pic_url) {
		this.pic_url = pic_url;
	}

	@Override
	public boolean equals(Object obj) {
		// TODO Auto-generated method stub
		boolean rst=false;
		if(obj instanceof ProgramTotal)
		{
			ProgramTotal pt=(ProgramTotal) obj;
			rst=(site.equals(pt.getSite())&&file_url.equals(pt.getFile_url()));
		}
		return rst;
	}

	@Override
	public int hashCode() {
		// TODO Auto-generated method stub
		return site.hashCode()+file_url.hashCode();
	}

	public String getDomain() {
		return domain;
	}

	public void setDomain(String domain) {
		this.domain = domain;
	}

	public String getEncoding() {
		return encoding;
	}

	public String getPage_title() {
		return page_title;
	}

	public void setPage_title(String page_title) {
		this.page_title = page_title;
	}
	
}
