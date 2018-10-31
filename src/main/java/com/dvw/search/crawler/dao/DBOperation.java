package com.dvw.search.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Vector;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import com.dvw.search.crawler.core.CrawlerMain;
import com.dvw.search.crawler.domain.HttpLink;
import com.dvw.search.crawler.domain.ProgramTotal;
import com.dvw.search.crawler.domain.ResultFile;
import com.dvw.search.crawler.domain.SiteInfo;
import com.dvw.search.crawler.domain.TaskInfo;
import com.dvw.search.crawler.util.ConnectionUtil;
import com.dvw.search.crawler.util.PropertiesUtil;


/**
 * 数据库操作类，负责jdbc数据库交互
 * @author David.Wang
 *
 */
public class DBOperation {

	private Log log=LogFactory.getLog(DBOperation.class);
	static List<String> programAreas;
	/**
	 * 删除http_x表中的数据
	 */
	public static  void deleteTotalHttp()
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		
		for(int i=0;i<10;i++)
		{
			try {
				ps=conn.prepareStatement("delete from http_"+i);
				ps.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					ps.close();
					if(conn.isClosed())
						conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}			
			}		
		}
		
	}
	
	/**
	 * 删除file_x表中的数据
	 */
	public static void deleteResultFile()
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
//				ps=conn.prepareStatement("delete from file_"+i);
				ps=conn.prepareStatement("delete from t_result_file");
				ps.execute();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}finally{
				try {
					if(conn.isClosed())
						conn.close();
				} catch (SQLException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}	
			}
	
	}
	
	
	/**
	 * 删除t_result_site表中的数据
	 */
	public static void deleteResultSite()
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("delete from t_result_site");
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
//				ps.close();
				if(conn.isClosed())
					conn.close();
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	
	/**
	 * 向http_x表中插入数据,
	 * @param programTotals ProgramTotal对象集
	 * @param tableNumber   链接表编号
	 */
	public synchronized void insertTotalHttp(Set<ProgramTotal> programTotals,int tableNumber)
	{		
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		programAreas=DataSource.getProgramAreas();
		try {
			ps=conn.prepareStatement("insert into http_"+tableNumber+"(domain,site,page_url,file_url,file_name,pic_url,layer,hashCode) values(?,?,?,?,?,?,?,?)");
			Iterator it=programTotals.iterator();
			while(it.hasNext())
			{
				ProgramTotal pt=(ProgramTotal) it.next();
				ps.setString(1, pt.getDomain());
				ps.setString(2, pt.getSite());
				ps.setString(3, pt.getPage_url());
				ps.setString(4,pt.getFile_url());
				ps.setString(5, pt.getFile_name());
				ps.setString(6, pt.getPic_url());
				ps.setInt(7, pt.getLayer());
				ps.setInt(8, pt.getFile_url().hashCode());
				try{
					ps.execute();
				}catch(Exception e)
				{
					log.fatal("循环判定重复数据");
					continue;
				}
			}
		} catch (SQLException e) {
			log.fatal("重复数据");
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * 获取链接表http_x的链接进行解析
	 * @param status
	 * @param domain
	 * @param tableNumber
	 * @return
	 */
	public synchronized Vector<HttpLink> getLinks(int status,String domain,int tableNumber){
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		Vector<HttpLink> links=new Vector<HttpLink>();
		try {
			ps=conn.prepareStatement("select file_url,layer from http_"+tableNumber+" where status=? and domain=? limit "+PropertiesUtil.getMaxThread()*100);
			ps.setInt(1, status);
			ps.setString(2, domain);
			rs=ps.executeQuery();
			while(rs.next())
			{
				links.add(new HttpLink(rs.getString("file_url"),rs.getInt("layer")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps,rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return links;
	}
	
	/**
	 * 更新http_x表数据的状态
	 * @param status status=0--该链接未被解析，status=1--该链接正在解析，status=2链接解析完毕
	 * @param domain 节目所属域名
	 */
	public synchronized void updateTotalHttpStatus(int status,String domain,int tableNumber){
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("update http_"+tableNumber+" set status=? where status=? and domain=?");
			ps.setInt(1, status);
			ps.setInt(2, status-1);
			ps.setString(3, domain);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	
	/**
	 * 修改http_x表个别节目状态,将已经解析的链接进行状态值改变
	 * @param status       链接状态值
	 * @param fileUrl	   链接url
	 * @param tableNumber  链接表的编号	  
	 */
	public void updateTotalHttpStatusForUrl(int status,String fileUrl,int tableNumber)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("update http_"+tableNumber+" set status=? where file_url=?");
			ps.setInt(1, status);
			ps.setString(2, fileUrl);
			synchronized(CrawlerMain.taskInfo)
			{
				ps.executeUpdate();
			}
			
		} catch (SQLException e) {
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * 排重，该方法由于mysql数据库不支持delete命令下的子查询导致方案改变，暂时不用
	 */
	public synchronized void deleteRepete(){
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("delete from totalhttp t where t.status=0 and t.file_url in (select file_url from totalhttp where status=1)");
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	
	/**
	 * 向file_x表中插入后缀关键字匹配的数据,首页线程使用
	 * @param pt
	 * @param fileinfo_id
	 */
	public synchronized void insertResultFile(ProgramTotal pt,int fileinfo_id,int tableNumber)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("insert into t_result_file(file_url,keyword,anchor_text,fileinfo_id,page_url,site,domain,spare_char4,layer,hashCode) values(?,?,?,?,?,?,?,?,?)");
			ps.setString(1, pt.getFile_url());
			ps.setString(2, pt.getFile_name());
			ps.setString(3, pt.getPage_title());
			ps.setInt(4, fileinfo_id);
			ps.setString(5, pt.getPage_url());
			ps.setString(6, pt.getSite());
			ps.setString(7, pt.getDomain());
			ps.setString(8, "");
			ps.setInt(9, pt.getLayer());
			ps.setInt(10, pt.getFile_url().hashCode());
			ps.executeUpdate();
		} catch (Exception e) {
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}		
		
	}
	
	
	/**
	 * 向file_x表中插入正则匹配的数据
	 * @param file_url  	节目URL
	 * @param keyword		节目名称	
	 * @param anchor_text   网页title
	 * @param fileinfo_id   节目类型
	 * @param page_url      节目父页面
	 * @param site          节目所属子域名
	 * @param play_count    节目点技术
	 * @param comment_count 节目回复数
	 * @param uploadTime    节目上传时间
	 * @param pic_url       图片utl
	 * @param programType   节目类别，娱乐、体育、新闻。。。
	 * @param label         节目标签
	 * @param introduce     节目简介
	 * @param tableNumber   节目表编号
	 */
	public void insertRegResultFile(ResultFile file,int tableNumber)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
//			ps=conn.prepareStatement("insert into file_"+tableNumber+"(file_url,keyword,anchor_text,fileinfo_id,page_url,site,play_count,comment_count,upload_time," +
//					"pic_url,spare_int1,spare_int2,spare_int3,spare_int4,spare_char1,spare_char2,spare_char3,spare_char4,spare_char5,spare_char6,spare_date1) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			ps=conn.prepareStatement("insert into t_result_file(file_url,keyword,anchor_text,fileinfo_id,page_url,site,domain,play_count,comment_count,upload_time," +
					"pic_url,spare_int1,spare_int2,spare_int3,spare_int4,spare_char1,spare_char2,spare_char3,spare_char4,spare_char5,spare_char6,spare_char7,spare_char8,spare_char9,spare_date1,layer,hashCode) values(?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?,?)");
			//file_url
			ps.setString(1, file.getFileUrl());
			//keyword
			ps.setString(2, file.getKeyword());
			//anchor_text
			ps.setString(3, file.getAnchorText());
			//fileinfo_id
			ps.setInt(4, file.getFileinfoId());
			//page_url
			ps.setString(5, file.getPageUrl());
			//site
			ps.setString(6, file.getSite());
			//domain
			ps.setString(7, file.getDomain());
			//play_count
			ps.setInt(8, file.getPlayCount());
			//comment_count
			ps.setInt(9, file.getCommentCount());
			//upload_time
			if(file.getUploadTime()!=null)
				ps.setTimestamp(10, new java.sql.Timestamp(file.getUploadTime().getTime()));
			else
				ps.setDate(10, null);
			//pic_url
			ps.setString(11, file.getPicUrl());
			//spare_int1
			ps.setInt(12, file.getSpareInt1());
			//spare_int2
			ps.setInt(13, file.getSpareInt2());
			//spare_int3
			ps.setInt(14, file.getSpareInt3());
			//spare_int4
			ps.setInt(15, file.getSpareInt4());
			//spare_char1
			ps.setString(16, file.getSpareChar1());
			//spare_char2
			ps.setString(17, file.getSpareChar2());
			//spare_char3
			ps.setString(18, file.getSpareChar3());
			//spare_char4
			ps.setString(19, file.getSpareChar4());
			//spare_char5
			ps.setString(20, file.getSpareChar5());
			//spare_char6
			ps.setString(21, file.getSpareChar6());
			//spare_char7
			ps.setString(22, file.getSpareChar7());
			//spare_char8
			ps.setString(23, file.getSpareChar8());
			//spare_char9
			ps.setString(24, file.getSpareChar9());
			//spare_date1
			if(file.getSpareDate1()!=null)
				ps.setTimestamp(25, new java.sql.Timestamp(file.getSpareDate1().getTime()));
			else
				ps.setDate(25, null);
			//layer
			ps.setInt(26, file.getLayer());
			//hashCode
			ps.setInt(27, file.getFileUrl().hashCode());
			ps.execute();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		try {
			close(conn,ps);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	
	/**
	 * 获取站点和该站点下的节目数
	 */
	public List<SiteInfo> getSiteInfo(){
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		List<SiteInfo> results=new ArrayList<SiteInfo>();
		try {
			ps=conn.prepareStatement("select site,domain,count(*) from t_result_file group by site,domain");
			rs=ps.executeQuery();
			while(rs.next())
			{
				SiteInfo siteInfo=new SiteInfo(rs.getString(1),rs.getString(2),rs.getInt(3));
				results.add(siteInfo);
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps,rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return results;
	}
	
	
	/**
	 * 判断站点表中的数据是否重复
	 * @param siteUrl
	 * @return
	 */
	public boolean notRepeat(String siteUrl)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		try {
			ps=conn.prepareStatement("select site_url from t_result_site where site_url=?");
			ps.setString(1, siteUrl.trim());
			rs=ps.executeQuery();
			if(rs.next())
			{
//				close(conn,ps,rs);
				return false;
			}
				
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps,rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}

		return true;
	}
	
	
	/**
	 * 更新t_result_site表中的page_num字段信息
	 */
	public void updateSiteInfo(SiteInfo siteInfo)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("update t_result_site set page_num=? where site_url=?");
			ps.setInt(1, siteInfo.getProgramNum());
			ps.setString(2,siteInfo.getSiteUrl());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * 向站点信息表插入数据
	 * @param siteUrl 	域名url
	 * @param domain 	主域名
	 * @param ip      	域名ip
	 * @param siteName	站点名称
	 * @param pageNum	站点所含页面
	 */
	public void insertResultSite(String siteUrl,String domain,String ip,String siteName,int pageNum)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		try {
			ps=conn.prepareStatement("insert into t_result_site(site_url,domain,ip_address,site_name,page_num) values(?,?,?,?,?)");
			ps.setString(1, siteUrl);
			ps.setString(2, domain);
			ps.setString(3, ip);
			ps.setString(4, siteName);
			ps.setInt(5, pageNum);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
	}
	
	/**
	 * 统计totalhttp/t_result_file/t_result_site表信息
	 * @return
	 */
	public TaskInfo getTaskInfo()
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		TaskInfo taskInfo=new TaskInfo();
		int page_count=0;
		int file_count=0;
		int site_count=0;
		try {
		/**原来用数据库统计节目和链接总数，这样当数据量大的时候会造成大量链接被锁，占用链接
			ps=conn.prepareStatement("select count(*) from totalhttp");
			rs=ps.executeQuery();
			rs.next();
			page_count=rs.getInt(1);
			ps=conn.prepareStatement("select count(*) from t_result_file");
			rs=ps.executeQuery();
			rs.next();
			file_count=rs.getInt(1);
		**/
			file_count=CrawlerMain.hashProgram.size();
			for(HashSet hashLink:CrawlerMain.hashLinks)
			{
				page_count+=hashLink.size();
			}
			ps=conn.prepareStatement("select count(*) from t_result_site");
			rs=ps.executeQuery();
			rs.next();
			site_count=rs.getInt(1);
			taskInfo.setFileCount(file_count);
			taskInfo.setPageCount(page_count);
			taskInfo.setSiteCount(site_count);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return taskInfo;
	}
	
	public int getTotalHttpStatusCount(int status,String domain,int tableNumber)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		int count=0;
		try {
			ps=conn.prepareStatement("select count(*) from http_"+tableNumber+" where status=? and domain=?");
			ps.setInt(1, status);
			ps.setString(2, domain);
			rs=ps.executeQuery();
			rs.next();
			count=rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				close(conn,ps,rs);
			} catch (SQLException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}	
		}
		return count;
	}
	
	private void close(Connection conn,PreparedStatement ps,ResultSet rs) throws SQLException
	{
		if(rs!=null)
			rs.close();
		if(ps!=null)
			ps.close();
		if(conn!=null)
			conn.close();
	}
	
	private void close(Connection conn,PreparedStatement ps) throws SQLException
	{
		if(ps!=null)
			ps.close();
		if(conn!=null)
			conn.close();
	}
	
	public static void main(String[] args)
	{
		new DBOperation().getSiteInfo();
	}
}
