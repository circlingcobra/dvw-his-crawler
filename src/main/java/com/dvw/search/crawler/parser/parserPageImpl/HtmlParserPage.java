package com.dvw.search.crawler.parser.parserPageImpl;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.RegExp;
import com.dvw.search.crawler.domain.ResultFile;
import com.dvw.search.crawler.parser.ParserPage;
import com.dvw.search.crawler.util.CharsetTransUtil;
import com.dvw.search.crawler.util.PropertiesUtil;
import com.dvw.search.crawler.util.SpecialLinkTransUtil;
import com.dvw.search.crawler.util.UrlCheckUtil;


public class HtmlParserPage implements ParserPage{

	private Set<ProgramTotal> programTotals=new HashSet<ProgramTotal>();

	private static Log log=LogFactory.getLog(HtmlParserPage.class);
	
	private static List regExps=DataSource.getRegExps();
	
	//节目域名范围
	static List<String> programAreas;

	/**
	 * 解析链接，获取链接url、链接对应图片、名称，并将链接集合入库
	 */
	public Set<ProgramTotal> parserLink(HttpLink link,String encoding,String domain,String regFilter) throws ParserException
	{
		String site=UrlCheckUtil.getSubDomain(link.getLink());
		
		String pageTitle=CharsetTransUtil.encodeWebString(parseTitle(null,link.getLink(),encoding));
		
		Parser parser=getParser(link.getLink(),encoding);
			
		NodeList list = new NodeList ();
		NodeFilter filter = new TagNameFilter("A");

		for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
		    e.nextNode ().collectInto (list, filter);
		
		for(int i=0;i<list.size();i++)
		{
			//链接url
			String programLink="";
			//图片链接
			String pictureLink="";
			//节目名称
			String programName="";

			programLink=CharsetTransUtil.encodeWebString(((LinkTag)(list.elementAt(i))).getLink().toString()).replaceAll("&amp;", "&");
			if(list.elementAt(i).getChildren()!=null&&null!=list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0))
			{
//				System.out.println(((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getImageURL());		
				pictureLink=((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getImageURL().toString().replaceAll("&amp;", "&");
				if(((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getAttribute("thumb")!=null)
					pictureLink=((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getAttribute("thumb").toString().replaceAll("&amp;", "&");
				
				if(((LinkTag)(list.elementAt(i))).getAttribute("title")!=null)
					programName=CharsetTransUtil.encodeWebString(((LinkTag)(list.elementAt(i))).getAttribute("title"));
				else if(((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getAttribute("alt")!=null)
					programName=CharsetTransUtil.encodeWebString(((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getAttribute("alt"));
				else if(((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getAttribute("title")!=null)
					programName=CharsetTransUtil.encodeWebString(((ImageTag)((list.elementAt(i).getChildren().extractAllNodesThatMatch(new TagNameFilter ("IMG")).elementAt(0)))).getAttribute("title"));					
			}					
			else{
//				System.out.println("名称------"+list.elementAt(i).toPlainTextString());
				programName=CharsetTransUtil.encodeWebString(list.elementAt(i).toPlainTextString());
			}
			if(!programLink.contains("http"))
				programLink="http://"+site+programLink;
			if(!pictureLink.equals("")&&!pictureLink.contains("http"))
				pictureLink="http://"+site+pictureLink;
			if(regFilter!=null&&regFilter.trim().length()!=0)
				programLink=programLink.replaceAll(regFilter, "");
			
			ProgramTotal pt=new ProgramTotal(domain,site,link.getLink(),pageTitle,programLink,programName,pictureLink,encoding,link.getLayer()+1);
			if(programTotals.contains(pt)&&pt.getPic_url()!=null&&pt.getPic_url().trim().length()!=0)
			{
				programTotals.remove(pt);
			}
			programTotals.add(pt);	
		}
		return programTotals;
		
	}
	
	
	/**
	 * 解析链接,获取对应图片，并将图片集合入库
	 */
	public Set<ProgramTotal> parserImage(HttpLink link,String encoding,String domain) throws ParserException
	{
		String site=UrlCheckUtil.getSubDomain(link.getLink());		
		String pageTitle=CharsetTransUtil.encodeWebString(parseTitle(null,link.getLink(),encoding));
		String pictureLink="";
		
		Parser parser=getParser(link.getLink(),encoding);		
		NodeList list = new NodeList ();
		NodeFilter filter = new TagNameFilter("IMG");

		for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
		    e.nextNode ().collectInto (list, filter);
			
		for(int i=0;i<list.size();i++)
		{
			//图片url
			pictureLink=((ImageTag)(list.elementAt(i))).getImageURL().toString().replaceAll("&amp;","&");		
			if(!pictureLink.equals("")&&!pictureLink.contains("http"))
				pictureLink="http://"+site+pictureLink;
			programTotals.add(new ProgramTotal(domain,site,link.getLink(),pageTitle,pictureLink,"",pictureLink,encoding,link.getLayer()+1));
		}
		return programTotals;
		
	}
	
		
	
	/**
	 * 在url符合正则规范的情况下，对url对应页面进行解析，正则匹配关键字
	 * @param pt
	 */
	public ResultFile matchPageInfo(ProgramTotal pt)
	{
		Parser parser=null;
		try {
			parser=getParser(pt.getFile_url(),pt.getEncoding());
		} catch (ParserException e) {
			log.debug("网页正则解析异常"+pt.getFile_url());
			//将匹配信息存入节目表 
			ResultFile file=new ResultFile(pt.getFile_url(),pt.getFile_name(),pt.getPage_title(),0,pt.getPage_url(),pt.getSite(),pt.getDomain(),pt.getLayer(),
					0,0,null,"",0,0,0,0,"","","","","","","","","",null);
			return file;
		}
		//url的名称--<title>
		String title=CharsetTransUtil.encodeWebString(parseTitle(parser,pt.getFile_url(),pt.getEncoding()));
		//节目后缀号
		int profix=0;
		//点击数
		int playCount=0;
		//回复数
		int commentCount=0;
		//上传日期
		Date uploadTime=null;
		//节目类别--spare_char1
		String programType="";
		//节目标签--spare_char2
		String label="";
		//节目简介--spare_char3
		String introduce="";
		//节目上传人--spare_char4		
		String downloadPath="";
		//导演--spare_char5
		String director="";
		//主演--spare_char6
		String majorActor="";
		//视频语言--spare_char7
		String language="";
		//视频发行地区--spare_char8
		String region="";
		//种子上传人--spare_char9
		String uploadUser="";
		//图片url
		String picUrl=pt.getPic_url();
		//节目格式
		String format="";
		//下载--spare_int1
		int collectionCount=0;
		//订阅--spare_int2
		int subscibeCount=0;
		//引用--spare_int3
		int refersCount=0;
		//视频数--spare_int4
		int videoNumber=0;
		
		
		String s=getTextInfo(null,pt.getFile_url(),pt.getEncoding());
				
		Iterator it=regExps.iterator();
		while(it.hasNext())
		{
			RegExp regExp=(RegExp)it.next();
			if(pt.getDomain().indexOf(regExp.getDomain().trim())>=0)
			{
				int regType=regExp.getRegType();
				//点击数正则匹配--play_count
				if(regType==2)
				{
					playCount=parserInt(s,regExp.getRegex());
					continue;
				}
				//回复数正则匹配--comment_count
				if(regType==3)
				{
					commentCount=parserInt(s,regExp.getRegex());
					continue;
				}
				//上传时间正则匹配--upload_time
				if(regType==5)
				{
					uploadTime=parserDate(s,regExp.getRegex(),regExp.getRegExample());
					continue;
				}
				//SPARE_CHAR1
				if(regType==8)
				{
					programType=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR2
				if(regType==9)
				{
					label=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR3
				if(regType==10)
				{
					introduce=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					if(introduce.length()>512)
						introduce=introduce.substring(0,300);
					continue;
				}
				//SPARE_CHAR4
				if(regType==11)
				{
					//处理下载页跳转的问题
					String downloadPage=regExp.getRegex();
					String reg;
					//发生跳转的情况下，正则中用,号隔开两个字符串。前一个字符串为跳转页面url的正则，后一个为跳转页面中下载链接的正则表达式
					if(downloadPage.indexOf(",")>0)
					{
						reg=downloadPage.split(",")[1];
						downloadPage=downloadPage.split(",")[0];						
						downloadPage=CharsetTransUtil.encodeWebString(parseDownloadLink(downloadPage,pt.getFile_url(),pt.getEncoding(),0,true));
						//一个网页中可能会有多个中间页，循环处理
						String[] downloadPages=downloadPage.split(",");
						for(int i=0;i<downloadPages.length;i++)
						{
							String str=downloadPages[i];
//							if(!str.contains("http://"))
//								str="http://"+pt.getDomain()+"/"+str;
							log.info("下载跳转URL："+str);
							downloadPath+=CharsetTransUtil.encodeWebString(parseDownloadLink(reg,str,pt.getEncoding(),i,false));
							//下载链接的总长度过大，则截取
							if(downloadPath.length()>4096)
							{
								downloadPath=downloadPath.substring(0,4096);
								downloadPath=downloadPath.substring(0,downloadPath.lastIndexOf(","));
								break;
							}
							
						}
					}						
					else
					{
						downloadPage=pt.getFile_url();
						reg=regExp.getRegex();
						downloadPath=CharsetTransUtil.encodeWebString(parseDownloadLink(reg,downloadPage,pt.getEncoding(),0,false));
					}											
					continue;
				}
				//SPARE_CHAR5
				if(regType==12)
				{
					director=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					if(director.length()>128)
						director=director.substring(0, 64);
					continue;
				}
				//SPARE_CHAR6
				if(regType==13)
				{
					majorActor=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_INT1
				if(regType==14)
				{
					collectionCount=parserInt(s,regExp.getRegex());
					continue;
				}
				//SPARE_INT2
				if(regType==15)
				{
					subscibeCount=parserInt(s,regExp.getRegex());
					continue;
				}
				//SPARE_INT3
				if(regType==16)
				{
					refersCount=parserInt(s,regExp.getRegex());
					continue;
				}
				//SPARE_INT4
				if(regType==17)
				{
					videoNumber=parserInt(s,regExp.getRegex());
					continue;
				}
				//pic_url
				if(regType==18)
				{
					picUrl=CharsetTransUtil.encodeWebString(parserPicUrl(regExp.getRegex(),pt.getFile_url(),pt.getEncoding()));
					continue;
				}
				//fileinfo_id
				if(regType==19)
				{
					format=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					continue;
				}
				//keyword
				if(regType==20)
				{
					pt.setFile_name(CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex())));
					continue;
				}
				//SPARE_CHAR7
				if(regType==21)
				{
					language=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR8
				if(regType==22)
				{
					region=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR9
				if(regType==23)
				{
					uploadUser=CharsetTransUtil.encodeWebString(parserString(s,regExp.getRegex()));
				}
				
				//获取ajax异步请求url，url需要获取参数，参数通过regExp.getRegex()返回的正则获取，多个参数用@符号分割，然后分别解析。
				//解析完参数后，将参数以此替换regExp.getRegExample()中的num_of_counter字符串，最终获得异步url
				if(regType==100)
				{
					String ajaxUrl="";
					String script=parserScript(pt.getFile_url());
					if(regExp.getRegex().indexOf("@")<0)
					{
						String id=parserString(script,regExp.getRegex());
						log.info("id-------"+id);
						ajaxUrl=regExp.getRegExample().replace("num_of_counter",id.trim());
						log.info("ajaxUrl------"+ajaxUrl);
					}else
					{
						log.info("多参数ajax");
						String[] regs=regExp.getRegex().split("@");
						String[] ids=new String[regs.length];
						ajaxUrl=regExp.getRegExample();
						for(int i=0;i<regs.length;i++)
						{
							ids[i]=parserString(script,regs[i]);
							ajaxUrl=ajaxUrl.replaceFirst("num_of_counter",ids[i]);
						}
						log.info("ajaxUrl------"+ajaxUrl);						
					}
					//完成异步url的解析后，对异步url返回的内容进行解析
					String ajaxInfo=getTextInfo(null,ajaxUrl,pt.getEncoding());
					Iterator ajaxIt=regExps.iterator();
					while(ajaxIt.hasNext())
					{
						RegExp ajaxRegExp=(RegExp)ajaxIt.next();
						if(ajaxRegExp.getDomain().equals(pt.getDomain())&&ajaxRegExp.getRegType()>100)
						{
							//101--异步点击数
							if(ajaxRegExp.getRegType()==101)
							{
								if(playCount==0)
									playCount=parserInt(ajaxInfo,ajaxRegExp.getRegex());
								continue;
							}
							//102--异步回复数
							if(ajaxRegExp.getRegType()==102)
							{
								if(commentCount==0)
									commentCount=parserInt(ajaxInfo,ajaxRegExp.getRegex());
								continue;
							}
						}
					}
				}
			}	
			
		}
		
/**		
 		详细参数排序和数据库字段请参考此构造函数
		public ResultFile(String fileUrl, String keyword, String anchorText,
				int fileinfoId, String pageUrl, String site, int layer,
				int playCount, int commentCount, Date uploadTime, String picUrl,
				int spareInt1, int spareInt2, int spareInt3, int spareInt4,
				String spareChar1, String spareChar2, String spareChar3,
				String spareChar4, String spareChar5, String spareChar6,
				String spareChar7, String spareChar8, String spareChar9
				) 
*/
		//将匹配信息存入节目表 
		ResultFile file=new ResultFile(pt.getFile_url(),pt.getFile_name(),title,profix,pt.getPage_url(),pt.getSite(),pt.getDomain(),pt.getLayer(),
				playCount,commentCount,uploadTime,picUrl,collectionCount,subscibeCount,refersCount,videoNumber,programType,label,introduce,downloadPath,director,majorActor,language,region,uploadUser,null);
		return file;
	}
	
	/**
	 * 匹配数字正则的类
	 * @param s
	 * @param reg
	 * @return
	 */
	public int parserInt(String s,String reg)
	{

//		try {
//			reg=new String(reg.getBytes("utf-8"),"gbk");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		int result=0;
		String reg1=reg.substring(0,reg.indexOf("num_of_counter"));
		String reg2=reg.substring(reg.indexOf("num_of_counter")+14);
		reg=reg.replace("num_of_counter","\\s*\\d+(,\\d+)*\\s*");
//		System.out.println(index1);
//		System.out.println(index2);
		
		Pattern pattern=Pattern.compile(reg);
		Matcher matcher=pattern.matcher(s);
		if(matcher.find())
		{
			String str=matcher.group();
			if(reg1.trim().length()>0)
				str=str.replaceFirst(reg1, "");
			if(reg2.trim().length()>0)
				str=str.replaceFirst(reg2, "");
//			System.out.println(str);
			if(str.indexOf(",")>0)
				str=str.replace(",", "");
			try{
				result=Integer.parseInt(str.trim());
			}catch(Exception e)
			{
				log.error("int类型转换异常");
				return 0;
			}
			return result;
		}else
			return 0;
	}
	
	/**
	 * 匹配字符串正则的类
	 * @param s
	 * @param reg
	 * @return
	 */
	public String parserString(String s,String reg)
	{
//		try {
//			reg=new String(reg.getBytes("utf-8"),"gbk");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		String reg1=reg.substring(0,reg.indexOf("num_of_counter"));
		String reg2=reg.substring(reg.indexOf("num_of_counter")+14);

		reg=reg.replace("num_of_counter",".*");
//		System.out.println(reg1);
//		System.out.println(reg2);
		Pattern pattern=Pattern.compile(reg);
		Matcher matcher=pattern.matcher(s);
		if(matcher.find())
		{
			String str=matcher.group();
//			System.out.println(str);
			if(reg1.trim().length()>0)
				str=str.replaceFirst(reg1, "");
			if(reg2.trim().length()>0)
				str=str.replaceFirst(reg2, "");
//			System.out.println(str+"--------after");
			
			return str.trim();
		}else
			return "";

	}
	
	
	/**
	 * 匹配日期正则的类
	 * @param s
	 * @param reg
	 * @param example
	 * @return
	 */
	public Date parserDate(String s,String reg,String example)
	{
//		try {
//			reg=new String(reg.getBytes("utf-8"),"gbk");
//			example=new String(example.getBytes("utf-8"),"gbk");
//		} catch (UnsupportedEncodingException e1) {
//			// TODO Auto-generated catch block
//			e1.printStackTrace();
//		}
		String reg1=reg.substring(0,reg.indexOf("num_of_counter"));
		String reg2=reg.substring(reg.indexOf("num_of_counter")+14);
		reg=reg.replace("num_of_counter",".*");
//		System.out.println(reg1);
//		System.out.println(reg2);
		
		Pattern pattern=Pattern.compile(reg);
		Matcher matcher=pattern.matcher(s);
		if(matcher.find())
		{
			Date date;
			String str=matcher.group();
//			System.out.println(str);
			if(reg1.trim().length()>0)
				str=str.replaceFirst(reg1, "");
			if(reg2.trim().length()>0)
				str=str.replaceFirst(reg2, "");
			
			//处理x天前,个月前的情况
			if(str.indexOf("前")>=0)
			{
				pattern=Pattern.compile("\\d+");
				matcher=pattern.matcher(str);
				if(matcher.find())
				{
					int number=Integer.parseInt(matcher.group());
					if(str.indexOf("分")>=0)
					{
						Calendar c1=Calendar.getInstance();
						c1.add(Calendar.MINUTE,-number);
//						System.out.println(c1.getTime().toLocaleString());
						return c1.getTime();
					}
					if(str.indexOf("时")>=0)
					{
						Calendar c1=Calendar.getInstance();
						c1.add(Calendar.HOUR_OF_DAY,-number);
//						System.out.println(c1.getTime().toLocaleString());
						return c1.getTime();
					}
					if(str.indexOf("天")>=0||str.indexOf("日")>=0)
					{
						Calendar c1=Calendar.getInstance();
						c1.add(Calendar.DATE,-number);
//						System.out.println(c1.getTime().toLocaleString());
						return c1.getTime();
					}
					if(str.indexOf("月")>=0)
					{
						Calendar c1=Calendar.getInstance();
						c1.add(Calendar.MONTH,-number);
//						System.out.println(c1.getTime().toLocaleString());
						return c1.getTime();
					}
					if(str.indexOf("年")>=0)
					{
						Calendar c1=Calendar.getInstance();
						c1.add(Calendar.YEAR,-number);
//						System.out.println(c1.getTime().toLocaleString());
						return c1.getTime();
					}
				}
				return null;
			}
			DateFormat df=new SimpleDateFormat(example);
			try {
				date=df.parse(str.trim());
			} catch (ParseException e) {
				// TODO Auto-generated catch block
				log.error("时间转换异常");
				return null;
			}
			return date;
		}else
			return null;
	}
	
	
	/**
	 * 根据页面解析下载链接
	 * @param reg        下载链接的正则
	 * @param url        下载链接所在的url
	 * @param encoding   下载链接所在网页的字符集
	 * @param times      次数，对多个链接进行字符串连接的时候起作用
	 * @param isJump     是否是跳转页，如果是跳转页，则不需要存储链接对应的名称，否则需要存储下载链接对应的名称
	 * @return
	 */
	public String parseDownloadLink(String reg,String url,String encoding,int times,boolean isJump)
	{
		String linkAll;
		if(times==0)
			linkAll="";
		else
			linkAll=",";
		String link="";
		String thunder="";
		String fg="";
		Parser parser;
		try {
			parser=getParser(url,encoding);	
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("A");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{
				//获取链接对应的名称
				String linkName=(list.elementAt(i).toPlainTextString());
				//获取迅雷链接（如果有的话）
				thunder=((LinkTag)list.elementAt(i)).getAttribute("thunderHref");
				//获取FlashGet链接（如果有的话）
				fg=((LinkTag)list.elementAt(i)).getAttribute("fg");
				//判断该链接是否是普通链接
				if(thunder!=null)
					link=thunder;
				else if(fg!=null)
					link=fg;
				else
					link=(((LinkTag)(list.elementAt(i))).getLink().toString()).replaceAll("&amp;", "&");
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
				while(matcher.find())
				{			
					if(link.toLowerCase().indexOf("thunder")>=0)
						link=SpecialLinkTransUtil.getThunderParsedLink(link);
					else if(link.toLowerCase().indexOf("flashget")>=0)
						link=SpecialLinkTransUtil.getFlashGetParsedLink(link);
					if(isJump)
						linkAll+=link+",";
					else//如果不是跳转页面链接，而是下载链接则需要添加链接对应的名称，名称和url用$符号隔开
						linkAll+=linkName+"$"+link+",";
					if(linkAll.length()>4096)
						return linkAll.substring(0,linkAll.length()-1);
				}
			}
			if(linkAll.equals(","))
				return "";
			//去掉最后的逗号
			return linkAll.substring(0,(linkAll.length()-1)<0?0:linkAll.length()-1);
		} catch (Exception e1) {
			log.error("URL--"+url+" 抓取异常\n encoding--"+encoding);
			e1.printStackTrace();
		}
				
		return null;
	}
	
	/**
	 * 解析title名称
	 * @param url
	 * @param encoding
	 * @return
	 */
	public String parseTitle(Object object,String url,String encoding) 
	{		
		String name="";
		NodeFilter filter=new NodeClassFilter(TitleTag.class);
		Parser parser;
		try {
			if(object!=null)
				parser=(Parser)object;
			else
				parser=getParser(url,encoding);
//			parser.setEncoding(parser.getEncoding());
			NodeList list = parser.extractAllNodesThatMatch(filter);
			TitleTag node=(TitleTag)list.elementAt(0);
			log.info(node.getTitle().toString());
			name=node.getTitle().toString();
			if(name.indexOf("��")>=0)
				name=url.substring(7);
		}catch (ParserException e) {
			// TODO Auto-generated catch block
			log.info("节目名称抓取异常");
			name=url.substring(7);
		}catch(NullPointerException e1)
		{
			log.fatal("没有抓到title:"+url);
		}catch(StringIndexOutOfBoundsException e2)
		{
			log.fatal("框架错误：StringIndexOutOfBoundsException");
		}catch(IllegalArgumentException e3)
		{
			log.fatal("参数错误：IllegalArgumentException--"+url);
		}catch(Exception e4)
		{
			log.fatal("异常:"+url);
		}
		return name;
	}
	

	/**
	 * 根据正则表达式抓取图片链接
	 * @param reg
	 * @param url
	 * @param encoding
	 * @return
	 */
	public String parserPicUrl(String reg,String url,String encoding)
	{
		String link="";
		Parser parser;
		try {
			parser=getParser(url,encoding);	
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("IMG");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{
				//链接url
				link=(((ImageTag)(list.elementAt(i))).getImageURL().toString()).replaceAll("&amp;", "&");				
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
				while(matcher.find())
				{
					return link;
				}
			}
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			System.out.println("URL--"+url+" 抓取异常\n encoding--"+encoding);
			e1.printStackTrace();
		}
				
		return "";
	}
	
	/**
	 * 解析js代码
	 * @param url
	 * @return
	 */
	public String parserScript(String url)
	{
		String html="";
		NodeFilter filter = new TagNameFilter("SCRIPT");
		Parser parser ;
		try {
			parser=getParser(url,"iso8859-1");
			NodeList list = parser.extractAllNodesThatMatch(filter);
			
			for(int i=0;i<list.size();i++)
			{
				html+=list.elementAt(i).toHtml();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return html;
	}
	
	/**
	 * 对该url下的网页源码进行去标签处理 
	 * @param url
	 * @return
	 */
	public String getTextInfo(Parser parser,String url,String encoding)
	{
		try {
			if(parser==null)
				parser=getParser(url,encoding);
			StringBean sb=new StringBean(parser);
			sb.setLinks(false);
			sb.setReplaceNonBreakingSpaces(true);
			sb.setCollapse(true);
//			sb.setURL(url);
			String s=sb.getStrings();
//			System.out.println(s);
			return s;
		} catch (ParserException e) {
			e.printStackTrace();
			return "";
		}
		
	}
	
	public Parser getParser(String url,String encoding) throws ParserException{
		
		Parser parser=null;
		parser = new Parser ();
		if(PropertiesUtil.isProxy())
		{
			parser.getConnectionManager().setProxyHost(PropertiesUtil.getProxyHost());
			parser.getConnectionManager().setProxyPort(PropertiesUtil.getProxyPort());			
		}
		
		parser.setURL(url);
		parser.setEncoding(encoding);
		parser.getConnection().setConnectTimeout(PropertiesUtil.getConnectionTimeOut());
		parser.getConnection().setReadTimeout(PropertiesUtil.getSocketTimeOut());
		return parser;
	}
	
    public static void main(String[] args){
    	if(PropertiesUtil.isProxy()==true)
		{
			 log.info("开始设置代理----Host:"+PropertiesUtil.getProxyHost()+"\t\tPort:"+PropertiesUtil.getProxyPort());
			 Properties pro=System.getProperties();
			 pro.put("http.proxyHost", PropertiesUtil.getProxyHost());
			 pro.put("http.proxyPort", PropertiesUtil.getProxyPort());
			 if(PropertiesUtil.getUsername()!=null&&PropertiesUtil.getUsername().length()!=0)
			 {
				 log.info("设置代理用户----User:"+PropertiesUtil.getProxyUser()+"\t\tPassword:"+PropertiesUtil.getProxyPassword());
				 pro.put("http.proxyUser", PropertiesUtil.getProxyUser());
				 pro.put("http.proxyPassword", PropertiesUtil.getProxyPassword());
			 }
		}
    	    	new HtmlParserPage().parseTitle(null,"www.youmaker.com/video/sv?id=64fceefca0f64af49dae1a818992be15001", "utf-8");
//    	    	try {
//    				new HtmlParserPage().parserLink(new HttpLink("http://wap.m1905.com/dl/movie_w.jsp?st=show+3516&proxy=0&k_f=m1905_a&",1), "utf-8", "6.cn");
//    			} catch (Exception e) {
//    				// TODO Auto-generated catch block
//    				e.printStackTrace();
//    			}
//    	new HtmlParserPage().getTextInfo(null,"http://6.cn/watch/12266118.html","utf-8");
    	    }

	public ResultFile matchKeywords(ProgramTotal pt, List<String> keywords) {
		// TODO Auto-generated method stub
		return null;
	}
    	
}
