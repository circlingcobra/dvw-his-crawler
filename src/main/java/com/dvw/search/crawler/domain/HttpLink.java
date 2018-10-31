package com.dvw.search.crawler.domain;

/**
 * 父页面对象，对应http_x表
 * @author David.Wang
 * 
 */
public class HttpLink {

	//父页面链接
	private String link;
	//层数
	private int layer;
	
	public HttpLink(String link,int layer)
	{
		this.layer=layer;
		this.link=link;
	}

	public String getLink() {
		return link;
	}

	public void setLink(String link) {
		this.link = link;
	}

	public int getLayer() {
		return layer;
	}

	public void setLayer(int layer) {
		this.layer = layer;
	}
	
}
