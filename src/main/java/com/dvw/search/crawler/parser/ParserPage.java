package com.dvw.search.crawler.parser;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.htmlparser.util.ParserException;

import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.ResultFile;

public interface ParserPage {

	/**
	 * 返回ProgramTotal对象集合
	 * @param link           链接
	 * @param encoding       编码字符集
	 * @param domain         主域名
	 * @param regFilter      过滤表达式
	 * @return
	 * @throws ParserException
	 */
	public Set<ProgramTotal> parserLink(HttpLink link,String encoding,String domain,String regFilter) throws Exception;
	
	/**
	 * 返回ProgramTotal对象集合
	 * @param link           链接
	 * @param encoding       编码字符集
	 * @param domain         主域名
	 * @return
	 * @throws ParserException
	 */
	public Set<ProgramTotal> parserImage(HttpLink link,String encoding,String domain) throws Exception;
	
	/**
	 * 通过解析网站获得节目所有信息，最好通过正则,内部做好异常捕获
	 * @param pt
	 * @return
	 */
	public ResultFile matchPageInfo(ProgramTotal pt);
	
	/**
	 * 解析网页的title，内部做好异常捕获
	 * @param url
	 * @param encoding
	 * @return
	 */
	public String parseTitle(Object parser,String url,String encoding);

	/**
	 *  通过网页关键词挖掘节目
	 * @param pt
	 * @param keywords 
	 * @return
	 */
	public ResultFile matchKeywords(ProgramTotal pt, List<String> keywords);
}
