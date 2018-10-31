package com.dvw.search.crawler.parser;

/**
 * url链接访问接口
 * @author David.Wang
 *
 */
public interface NetWork {

	public String getResource(String url,String encoding);
}
