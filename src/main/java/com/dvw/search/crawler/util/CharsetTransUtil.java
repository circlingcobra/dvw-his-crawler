package com.dvw.search.crawler.util;

import java.io.UnsupportedEncodingException;

/**
 * 编码格式装换工具
 * @author David.Wang
 *
 */
public class CharsetTransUtil {

	/**
	 * 字符集转换，将默认编码格式转换成mysql的latin1
	 * @param s
	 * @return
	 */
	public static String encodeWebString(String s) {
		try {
//			String string = new String(s.getBytes(), "gbk");
			return s;
		} catch (Exception e) {
			return s;
		}
	}
}
