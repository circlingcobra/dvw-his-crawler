package com.dvw.search.crawler.parser.parserPageImpl;


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
import org.htmlparser.PrototypicalNodeFactory;
import org.htmlparser.beans.StringBean;
import org.htmlparser.filters.NodeClassFilter;
import org.htmlparser.filters.OrFilter;
import org.htmlparser.filters.TagNameFilter;
import org.htmlparser.nodes.TagNode;
import org.htmlparser.tags.CardTag;
import org.htmlparser.tags.ImageTag;
import org.htmlparser.tags.InputTag;
import org.htmlparser.tags.LiTag;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.tags.StrongTag;
import org.htmlparser.tags.TitleTag;
import org.htmlparser.tags.UlTag;
import org.htmlparser.util.NodeIterator;
import org.htmlparser.util.NodeList;
import org.htmlparser.util.ParserException;

import com.dvw.search.crawler.Constants;
import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.RegExp;
import com.dvw.search.crawler.domain.ResultFile;
import com.dvw.search.crawler.parser.NetWork;
import com.dvw.search.crawler.parser.ParserPage;
import com.dvw.search.crawler.util.CharsetTransUtil;
import com.dvw.search.crawler.util.KeywordUtil;
import com.dvw.search.crawler.util.ParserFactory;
import com.dvw.search.crawler.util.PatternUtil;
import com.dvw.search.crawler.util.PropertiesUtil;
import com.dvw.search.crawler.util.SpecialLinkTransUtil;
import com.dvw.search.crawler.util.UrlCheckUtil;


public class HttpClientHtmlParserPage implements ParserPage{
	
	private Set<ProgramTotal> programTotals=new HashSet<ProgramTotal>();

	private static Log log=LogFactory.getLog(HttpClientHtmlParserPage.class);
	
	private  List regExps=DataSource.getRegExps();
	
	private NetWork netWork=ParserFactory.getNetWork();
	//节点注册工厂
	private static PrototypicalNodeFactory factory = new PrototypicalNodeFactory ();  
 
	//节目域名范围
	static List<String> programAreas;
	
	static{		
		//注册节点
		factory.registerTag (new CardTag());  
		factory.registerTag (new StrongTag());  
		factory.registerTag (new LiTag());
		factory.registerTag(new UlTag());
	}

	/**
	 * 解析链接，获取链接url、链接对应图片、名称，并将链接集合入库
	 */
	public Set<ProgramTotal> parserLink(HttpLink link,String encoding,String domain,String regFilter) throws ParserException
	{
		String site=UrlCheckUtil.getSubDomain(link.getLink());		
		String html=netWork.getResource(link.getLink(),encoding);		
		Parser parser=getParser(html,encoding);
		String pageTitle=CharsetTransUtil.encodeWebString(parseTitle(parser,link.getLink(),encoding));
		//一次parser对象只能用一次,所以重新获取
		parser=getParser(html,encoding);	
		NodeList list = new NodeList ();
		NodeFilter filter = new TagNameFilter("A");

		for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
		    e.nextNode ().collectInto (list, filter);
		
		for(int i=0;i<list.size();i++)
		{
			//链接url
			String programLink=Constants.EMPTY_STRING;
			//图片链接
			String pictureLink=Constants.EMPTY_STRING;
			//节目名称
			String programName=Constants.EMPTY_STRING;

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
			
//			System.out.println(programName);
			programLink=UrlCheckUtil.getFullPath(programLink,link.getLink(),site);
			pictureLink=UrlCheckUtil.getFullPath(pictureLink,link.getLink(),site);			
			if(regFilter!=null&&regFilter.trim().length()!=0)
				programLink=programLink.replaceAll(regFilter, Constants.EMPTY_STRING);	
//			System.out.println(programLink+"************************");
			ProgramTotal pt=new ProgramTotal(domain,site,link.getLink(),pageTitle,programLink,programName,pictureLink,encoding,link.getLayer()+1);
			if(programTotals.contains(pt)&&pt.getPic_url()!=null&&pt.getPic_url().trim().length()!=0)
			{
				programTotals.remove(pt);
			}
			programTotals.add(pt);	
		}
		
//		for(ProgramTotal pt:programTotals)
//		{
//			if(pt.getPic_url().length()!=0)
//			System.out.println(pt.getFile_name()+"\t"+pt.getPic_url());
//		}
		
		return programTotals;
		
	}
	
	
	/**
	 * 解析链接,获取对应图片，并将图片集合入库
	 */
	public Set<ProgramTotal> parserImage(HttpLink link,String encoding,String domain) throws ParserException
	{
		String site=UrlCheckUtil.getSubDomain(link.getLink());		
		String pictureLink=Constants.EMPTY_STRING;
		
		String html=netWork.getResource(link.getLink(),encoding);		
		Parser parser=getParser(html,encoding);
		String pageTitle=CharsetTransUtil.encodeWebString(parseTitle(parser,link.getLink(),encoding));
		parser=getParser(html,encoding);
		NodeList list = new NodeList ();
		NodeFilter filter = new TagNameFilter("IMG");

		for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
		    e.nextNode ().collectInto (list, filter);
			
		for(int i=0;i<list.size();i++)
		{
			//图片url
			pictureLink=((ImageTag)(list.elementAt(i))).getImageURL().toString().replaceAll("&amp;","&");		
			pictureLink=UrlCheckUtil.getFullPath(pictureLink,link.getLink(),site);
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
		String html=null;
		try {
			html=netWork.getResource(pt.getFile_url(),pt.getEncoding());
			parser=getParser(html,pt.getEncoding());
		} catch (ParserException e) {
			log.debug("网页正则解析异常"+pt.getFile_url());
			//将匹配信息存入节目表 
			ResultFile file=new ResultFile(pt.getFile_url(),pt.getFile_name(),pt.getPage_title(),0,pt.getPage_url(),pt.getSite(),pt.getDomain(),pt.getLayer(),
					0,0,null,pt.getPic_url(),0,0,0,0,"","","","","","","","","",null);
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
		String programType=Constants.EMPTY_STRING;
		//节目标签--spare_char2
		String label=Constants.EMPTY_STRING;
		//节目简介--spare_char3
		String introduce=Constants.EMPTY_STRING;
		//节目上传人--spare_char4		
		String downloadPath=Constants.EMPTY_STRING;
		//导演--spare_char5
		String director=Constants.EMPTY_STRING;
		//主演--spare_char6
		String majorActor=Constants.EMPTY_STRING;
		//视频语言--spare_char7
		String language=Constants.EMPTY_STRING;
		//视频发行地区--spare_char8
		String region=Constants.EMPTY_STRING;
		//种子上传人--spare_char9
		String uploadUser=Constants.EMPTY_STRING;
		//图片url
		String picUrl=pt.getPic_url();
		//节目格式
		String format=Constants.EMPTY_STRING;
		//下载--spare_int1
		int collectionCount=0;
		//订阅--spare_int2
		int subscibeCount=0;
		//引用--spare_int3
		int refersCount=0;
		//视频数--spare_int4
		int videoNumber=0;
		//其他时间 --spare_date1
		Date otherTime=null;
		
		String s;
		s=getTextInfo(html,pt.getFile_url(),pt.getEncoding());
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
					playCount=PatternUtil.parserInt(s,regExp.getRegex());
					continue;
				}
				//回复数正则匹配--comment_count
				if(regType==3)
				{
					commentCount=PatternUtil.parserInt(s,regExp.getRegex());
					continue;
				}
				//上传时间正则匹配--upload_time
				if(regType==5)
				{
					uploadTime=PatternUtil.parserDate(s,regExp.getRegex(),regExp.getRegExample());
					continue;
				}
				//SPARE_CHAR1
				if(regType==8)
				{
					programType=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR2
				if(regType==9)
				{
					label=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR3
				if(regType==10)
				{
					introduce=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
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
						downloadPage=CharsetTransUtil.encodeWebString(parseDownloadLink(html,downloadPage,pt.getFile_url(),pt.getEncoding(),0,true,pt.getSite()));
						//一个网页中可能会有多个中间页，循环处理
						String[] downloadPages=downloadPage.split(",");
						for(int i=0;i<downloadPages.length;i++)
						{
							String str=downloadPages[i];
							log.info("下载跳转URL："+str);
							downloadPath+=CharsetTransUtil.encodeWebString(parseDownloadLink(null,reg,str,pt.getEncoding(),i,false,pt.getSite()));
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
						downloadPath=CharsetTransUtil.encodeWebString(parseDownloadLink(html,reg,downloadPage,pt.getEncoding(),0,false,pt.getSite()));
					}											
					continue;
				}
				//SPARE_CHAR5
				if(regType==12)
				{
					director=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					if(director.length()>128)
						director=director.substring(0, 64);
					continue;
				}
				//SPARE_CHAR6
				if(regType==13)
				{
					majorActor=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_INT1
				if(regType==14)
				{
					collectionCount=PatternUtil.parserInt(s,regExp.getRegex());
					continue;
				}
				//SPARE_INT2
				if(regType==15)
				{
					subscibeCount=PatternUtil.parserInt(s,regExp.getRegex());
					continue;
				}
				//SPARE_INT3
				if(regType==16)
				{
					refersCount=PatternUtil.parserInt(s,regExp.getRegex());
					continue;
				}
				//SPARE_INT4
				if(regType==17)
				{
					videoNumber=PatternUtil.parserInt(s,regExp.getRegex());
					continue;
				}
				//pic_url
				if(regType==18)
				{
					picUrl=CharsetTransUtil.encodeWebString(parserPicUrl(html,regExp.getRegex(),pt.getFile_url(),pt.getEncoding()));
					continue;
				}
				//fileinfo_id
				if(regType==19)
				{
					format=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//keyword
				if(regType==20)
				{
					pt.setFile_name(CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex())));
					continue;
				}
				//SPARE_CHAR7
				if(regType==21)
				{
					language=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR8
				if(regType==22)
				{
					region=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_CHAR9
				if(regType==23)
				{
					uploadUser=CharsetTransUtil.encodeWebString(PatternUtil.parserString(s,regExp.getRegex()));
					continue;
				}
				//SPARE_DATE1
				if(regType==24)
				{
					otherTime=PatternUtil.parserDate(s,regExp.getRegex(),regExp.getRegExample());
					continue;
				}
				
				//获取ajax异步请求url，url需要获取参数，参数通过regExp.getRegex()返回的正则获取，多个参数用@符号分割，然后分别解析。
				//解析完参数后，将参数以此替换regExp.getRegExample()中的num_of_counter字符串，最终获得异步url
				if(regType==100)
				{
					String ajaxUrl=Constants.EMPTY_STRING;
					String script=parserScript(html,pt.getFile_url());
					if(regExp.getRegex().indexOf("@")<0)
					{
						String id=PatternUtil.parserString(script,regExp.getRegex());
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
							ids[i]=PatternUtil.parserString(script,regs[i]);
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
									playCount=PatternUtil.parserInt(ajaxInfo,ajaxRegExp.getRegex());
								continue;
							}
							//102--异步回复数
							if(ajaxRegExp.getRegType()==102)
							{
								if(commentCount==0)
									commentCount=PatternUtil.parserInt(ajaxInfo,ajaxRegExp.getRegex());
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
				String spareChar7, String spareChar8, String spareChar9,spareDate1
				) 
*/
		//将匹配信息存入节目表 
		ResultFile file=new ResultFile(pt.getFile_url(),pt.getFile_name(),title,profix,pt.getPage_url(),pt.getSite(),pt.getDomain(),pt.getLayer(),
				playCount,commentCount,uploadTime,picUrl,collectionCount,subscibeCount,refersCount,videoNumber,programType,label,introduce,downloadPath,director,majorActor,language,region,uploadUser,otherTime);
		return file;
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
	public String parseDownloadLink(String html,String reg,String url,String encoding,int times,boolean isJump,String site)
	{
		String linkAll;
		if(times==0)
			linkAll=Constants.EMPTY_STRING;
		else
			linkAll=",";
		String link=Constants.EMPTY_STRING;
		String thunder=Constants.EMPTY_STRING;
		String fg=Constants.EMPTY_STRING;
		Parser parser;
		try {
			if(html!=null){
				parser=getParser(html,encoding);
			}
			else{
				html=netWork.getResource(url,encoding);
				parser=getParser(html,encoding);
			}
				
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
				else{
					link=(((LinkTag)(list.elementAt(i))).getLink().toString()).replaceAll("&amp;", "&");
					link=UrlCheckUtil.getFullPath(link,url,site);
				}
					
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
				return Constants.EMPTY_STRING;
			//去掉最后的逗号
			return linkAll.substring(0,(linkAll.length()-1)<0?0:linkAll.length()-1);
		} catch (Exception e1) {
			log.fatal("URL--"+url+" 抓取异常\t encoding--"+encoding);
//			e1.printStackTrace();
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
		
		String name=Constants.EMPTY_STRING;
		TagNode node=null;
		NodeList nodeList = null;  
		NodeFilter filterTitle=new NodeClassFilter(TitleTag.class);
		NodeFilter filterCard=new NodeClassFilter(CardTag.class);
		Parser parser;
		try {
			if(object!=null)
			{
				parser=(Parser)object;
			}				
			else
			{
				String html=netWork.getResource(url,encoding);
				parser=getParser(html,encoding);
			}
			OrFilter filter = new OrFilter();
//			OrFilter filter = new OrFilter(){
//				public boolean accept(Node node) {
//					if(node instanceof CardTag)
//						return true;
//					if(node instanceof TitleTag)
//						return true;
//					return false;
//				}
//			};
			filter.setPredicates(new NodeFilter[] { filterTitle, filterCard });	  
			nodeList = parser.parse(filter);

			if(nodeList.elementAt(0) instanceof TitleTag) 
			{
				node=(TitleTag)nodeList.elementAt(0);
				name=((TitleTag)nodeList.elementAt(0)).getTitle();
			}
				
			if(nodeList.elementAt(0) instanceof CardTag)
			{
				node=(CardTag)nodeList.elementAt(0);
				name=((CardTag)nodeList.elementAt(0)).getTitle();
			}
				
			
			log.fatal(name+"\t"+url);
			if(name.indexOf("��")>=0)
				name=UrlCheckUtil.getSubDomain(url);
			if(name.indexOf("????")>=0)
				name=UrlCheckUtil.getSubDomain(url);
		}catch (ParserException e) {
			// TODO Auto-generated catch block
			log.fatal("节目名称抓取异常");
			name=url.substring(7);
		}catch(NullPointerException e1)
		{
			log.fatal("没有抓到title:"+url,e1);
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
	public String parserPicUrl(String html,String reg,String url,String encoding)
	{
		String link=Constants.EMPTY_STRING;
		Parser parser;
		try {
			if(html!=null){
				parser=getParser(html,encoding);	
			}else{
				html=netWork.getResource(url,encoding);
				parser=getParser(html,encoding);
			}
			
			NodeList list = new NodeList ();
			NodeFilter filter = new TagNameFilter("IMG");

			for (NodeIterator e = parser.elements (); e.hasMoreNodes (); )
			    e.nextNode ().collectInto (list, filter);
				
			for(int i=0;i<list.size();i++)
			{
				//链接url
				link=(((ImageTag)(list.elementAt(i))).getImageURL().toString()).replaceAll("&amp;", "&");
				link=UrlCheckUtil.getFullPath(link,url,UrlCheckUtil.getSubDomain(url));
				Pattern pattern=Pattern.compile(reg);
				Matcher matcher=pattern.matcher(link);
//				System.out.println(link);
				while(matcher.find())
				{
					return link;
				}
			}
		} catch (ParserException e1) {
			// TODO Auto-generated catch block
			log.fatal("URL--"+url+" 抓取异常\n encoding--"+encoding);
//			e1.printStackTrace();
		}
				
		return "";
	}
	
	/**
	 * 解析js代码
	 * @param url
	 * @return
	 */
	public String parserScript(String html,String url)
	{
		String script=Constants.EMPTY_STRING;
		NodeFilter filter = new TagNameFilter("SCRIPT");
		Parser parser ;
		try {
			if(html!=null){
				parser=getParser(html,"iso8859-1");
			}else{
				html=netWork.getResource(url,"iso8859-1");
				parser=getParser(html,"iso8859-1");
			}
			
			NodeList list = parser.extractAllNodesThatMatch(filter);
			
			for(int i=0;i<list.size();i++)
			{
				script+=list.elementAt(i).toHtml();
			}
		} catch (Exception e) {
			e.printStackTrace();
		}

		return script;
	}
	
	/**
	 * 对该url下的网页源码进行去标签处理 
	 * @param url
	 * @return
	 */
	public String getTextInfo(String html,String url,String encoding)
	{
		Parser parser;
		try {
			if(html!=null){
				parser=getParser(html,encoding);	
			}else{
				html=netWork.getResource(url,encoding);
				parser=getParser(html,encoding);
			}
			parser.getLexer().getPage().setUrl(url);
			StringBean sb=new StringBean(parser);
			sb.setLinks(false);
			sb.setReplaceNonBreakingSpaces(true);
			sb.setCollapse(true);
//			sb.setURL(url);
			String s=sb.getStrings();
//			System.out.println(s);
			return s;
		} catch (Exception e) {
//			e.printStackTrace();
			return Constants.EMPTY_STRING;
		}
		
	}
	
	/**
	 * 接收网页源文件和网页代码，获取Parser对象
	 * @param html
	 * @param encoding
	 * @return
	 * @throws ParserException
	 */
	public Parser getParser(String html,String encoding) throws ParserException{		
		Parser parser=null;
		parser = Parser.createParser(html,encoding);
		parser.setNodeFactory (factory);
		return parser;
	}
	
	

	
//	public static String deflateStream2String(String charset,InflaterInputStream input)throws Exception{
//		 ByteArrayOutputStream decoded = new ByteArrayOutputStream ();
//		 byte[] buffer=new byte[1024];
//		 int length=0;
//		 while((length=input.read(buffer))!=-1)
//		 {
//			 decoded.write(buffer,0,length);
//		 }
//
//		return new String(decoded.toByteArray(),charset);
//	}
	
	public ResultFile matchKeywords(ProgramTotal pt,List<String> keywords) {
		Parser parser=null;
		String html=null;
		try {
			html=netWork.getResource(pt.getFile_url(),pt.getEncoding());
			parser=getParser(html,pt.getEncoding());
		} catch (ParserException e) {
			log.debug("网页解析异常"+pt.getFile_url());
			//将匹配信息存入节目表 
			return null;
		}
		boolean isMatch=KeywordUtil.matchKeywords(html,keywords,PropertiesUtil.getKeywordNum());
		if(isMatch)
		{
			String title=CharsetTransUtil.encodeWebString(parseTitle(parser,pt.getFile_url(),pt.getEncoding()));
			if(title.length()>128)
				title=title.substring(0,128);
			ResultFile file=new ResultFile(pt.getFile_url(),pt.getFile_name(),title,1,pt.getPage_url(),pt.getSite(),pt.getDomain(),pt.getLayer(),
					0,0,null,pt.getPic_url(),0,0,0,0,"","","","","","","","","",null);
			return file;
		}
				
		return null;
	}
	
    public static void main(String[] args){
    	if(PropertiesUtil.isProxy()==true)
		{
			 log.fatal("开始设置代理----Host:"+PropertiesUtil.getProxyHost()+"\t\tPort:"+PropertiesUtil.getProxyPort());
			 Properties pro=System.getProperties();
			 if(PropertiesUtil.getUsername()!=null&&PropertiesUtil.getUsername().length()!=0)
			 {
				 log.fatal("设置代理用户----User:"+PropertiesUtil.getProxyUser()+"\t\tPassword:"+PropertiesUtil.getProxyPassword());
			 }
		}
//    	    	System.out.println(new HttpClientHtmlParserPage().parseTitle(null,"http://wap.monternet.com/", "utf-8"));
//    	    	try {
//    				new HttpClientHtmlParserPage().parserLink(new HttpLink("http://www.verycd.com/sto/page4",1), "utf-8", "6.cn","((/members)|(/groups))/.*");
//    			} catch (Exception e) {
//    				// TODO Auto-generated catch block
//    				e.printStackTrace();
//    			}
//    	new HttpClientHtmlParserPage().parserPicUrl(null, "\\.jpg", "http://www.xunlei3gp.com/downinfo/3985.html", "gb2312");  	
    	new HttpClientHtmlParserPage().getTextInfo(null,"http://www.longsai.com", "gb2312");

    	    }

}
