package com.dvw.search.crawler.util;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.zip.Inflater;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;



/**
 * 将网页文件从流或者字节数组转换成为字符串
 * @author David.Wang
 *
 */
public class StreamStringBytesTrans {

	private static Log log=LogFactory.getLog(StreamStringBytesTrans.class);
	/**
	 * 按指定编码把输入流转换成字符串
	 * 
	 * @param charset
	 *            字符集编码
	 * @param input
	 *            输入流
	 * @return
	 * @throws Exception
	 */
	public static String inputStream2String(String charset, InputStream input) throws Exception {
		BufferedReader in = null;
		StringBuilder htmlDocument = new StringBuilder();
		String inputLine = null;
		try {			
			in = new BufferedReader(new InputStreamReader(input, charset));			
			while ((inputLine = in.readLine()) != null) {
				htmlDocument.append(inputLine).append("\r\n");
			}

		} catch (Exception e) {
			log.error("无法建立连接", e);
		} finally {
			if (in != null) {
				in.close();
			}
			if (input != null) {
				input.close();
			}
		}

		return htmlDocument.toString();
	}

	/**
	 * 针对deflate压缩格式网页，进行特殊处理网页解析
	 * @param charset
	 * @param input
	 * @return
	 * @throws Exception
	 */
	public static String deflateByte2String(String charset,byte[] input)throws Exception{
		Inflater inflater = new Inflater(true); 
		inflater.setInput(input);
		byte[] temp=new byte[input.length*10];
		int length=inflater.inflate(temp);
		byte[] results=new byte[length];
		System.arraycopy(temp, 0, results, 0, length);
		inflater.end();
		return new String(results,charset);
	}
	
	public static byte[] stream2Bytes(InputStream is)
	{
		ByteArrayOutputStream bytestream = new ByteArrayOutputStream(); 
		try {
			int ch;   
			while ((ch = is.read()) != -1) {   
				bytestream.write(ch);   
			}   
			byte imgdata[] = bytestream.toByteArray();   		
			bytestream.close();
			return imgdata;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}   
		return null;   

	}
}
