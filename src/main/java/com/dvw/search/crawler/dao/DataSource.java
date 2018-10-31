package com.dvw.search.crawler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import com.dvw.search.crawler.assistant.JudgeCondition;
import com.dvw.search.crawler.domain.FileInfo;
import com.dvw.search.crawler.domain.RegExp;
import com.dvw.search.crawler.domain.SeedInfo;
import com.dvw.search.crawler.domain.TaskInfo;
import com.dvw.search.crawler.util.ConnectionUtil;
import com.dvw.search.crawler.util.DateUtil;


/**
 * 数据配置初始化类
 * @author David.Wang
 *
 */
public class DataSource {

	static Connection conn=null;
	static PreparedStatement ps=null;
	static ResultSet rs=null;
	
	//存放节目联接关键字mp3、flv、wmv...
	private static List<FileInfo> suffixKeywords=new ArrayList<FileInfo>();
	//存放域名搜索范围
	private static List<String> programAreas=new ArrayList<String>();
	//种子列表
	private static List<SeedInfo> seeds=new ArrayList<SeedInfo>();
	//正则对象列表
	private static List<RegExp> regExps=new ArrayList<RegExp>();
	//种子数量
	private static int seedNum;
	//关键词集合
	private static List<String> keywords=new ArrayList<String>();
	//t_task_info表对象
	private static TaskInfo taskInfo;
	
	//初始化数据
	static{
		setTaskInfo();
		setSeeds();
		setSuffixKeywords();
		setAreas();
		setRegExps();
		setSeedNum();
		setKeywords();
	}
	
	
	/**
	 *  获取网页匹配关键词
	 */
	public static void setKeywords(){
		conn=ConnectionUtil.getInputConnection();
		try {
			ps=conn.prepareStatement("select keyword from t_keywords where task_id=1");
			rs=ps.executeQuery();
			while(rs.next())
			{
				keywords.add(rs.getString("keyword"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 获取节目后缀关键字
	 */
	public static void setSuffixKeywords(){
		conn=ConnectionUtil.getInputConnection();
		try {
			ps=conn.prepareStatement("select fileinfo_id,postfix from t_fileinfo");
			rs=ps.executeQuery();
			while(rs.next())
			{
				suffixKeywords.add(new FileInfo(rs.getInt("fileinfo_id"),rs.getString("postfix")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	

	/**
	 * 获取节目搜索范围
	 */
	public static void setAreas(){
		conn=ConnectionUtil.getInputConnection();
		try {
			ps=conn.prepareStatement("select site_url from t_site_area");
			rs=ps.executeQuery();
			while(rs.next())
			{
				programAreas.add(rs.getString("site_url"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	

	/**
	 * 获取爬虫运行种子,获取状态为0或者1的
	 */
	public static void setSeeds(){
		
		if(taskInfo.getStatus()==1)
		{
			updateSeedInfo(0,"taskId");
			updateSeedInfo(0, "layer");	
		}
		conn=ConnectionUtil.getInputConnection();
		try {
			ps=conn.prepareStatement("select id,site_url,domain,reg_filter,sub_charset,task_id,layer from t_seedsite where task_id=0 or task_id=1");
			rs=ps.executeQuery();
			while(rs.next())
			{
				seeds.add(new SeedInfo(rs.getInt("id"),rs.getString("site_url"),rs.getString("domain"),rs.getString("reg_filter"),rs.getInt("sub_charset"),rs.getInt("task_id"),rs.getInt("layer")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}	
	
	
	/**
	 * 获取爬虫运行正则
	 */
	public static void setRegExps(){
		conn=ConnectionUtil.getInputConnection();
		try {
			ps=conn.prepareStatement("select site_url,reg_exp,example,reg_type from t_reg_exp");
			rs=ps.executeQuery();
			while(rs.next())
			{
				regExps.add(new RegExp(rs.getString("site_url"),rs.getString("reg_exp"),rs.getString("example"),rs.getInt("reg_type")));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
		

	/**
	 * 获取种子数量
	 */
	public static void setSeedNum()
	{
		conn=ConnectionUtil.getInputConnection();
		try {
			ps=conn.prepareStatement("select count(*) from t_seedsite");
			rs=ps.executeQuery();
			rs.next();
			seedNum=rs.getInt(1);
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 获取t_task_info表信息
	 */
	public static void setTaskInfo()
	{
		conn=ConnectionUtil.getCrawlerConnection();
		try {
			ps=conn.prepareStatement("select site_area,status,start_time from t_task_info");
			rs=ps.executeQuery();
			rs.next();
			taskInfo=new TaskInfo(rs.getInt("site_area"),rs.getInt("status"),rs.getString("start_time"));
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	/**
	 * 爬虫开始运行和爬虫运行结束更新爬虫状态和时间
	 * @param soe
	 * @param status
	 */
	public static void updateTaskInfo(String soe,int status)
	{
		conn=ConnectionUtil.getCrawlerConnection();
		try {
			if(soe.equals("start")&&status==1)
			{
				ps=conn.prepareStatement("update t_task_info set status=2,start_time=?");
				ps.setString(1, DateUtil.Date2String());
				ps.executeUpdate();
			}
			
			if(soe.equals("end"))
			{
				ps=conn.prepareStatement("update t_task_info set status=3,end_time=?");
				ps.setString(1, DateUtil.Date2String());
				ps.executeUpdate();
			}
			
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 更新t_task_info表关于节目数、站点数、链接数的信息
	 * @param taskInfo
	 */
	public static void updateTaskInfo(TaskInfo taskInfo)
	{
		conn=ConnectionUtil.getCrawlerConnection();
		try {
			ps=conn.prepareStatement("update t_task_info set file_count=?,page_count=?,site_count=?");
			ps.setInt(1, taskInfo.getFileCount());
			ps.setInt(2, taskInfo.getPageCount());
			ps.setInt(3, taskInfo.getSiteCount());
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}

	/**
	 * 更新种子状态,如果种子已被运行,则更新状态为1
	 * @param status
	 */
	public static void updateSeedInfo(String column,int status,String seed)
	{
		conn=ConnectionUtil.getInputConnection();
		PreparedStatement ps=null;
		try {
			if(column.equals("taskId"))
				ps=conn.prepareStatement("update t_seedsite set task_id=? where site_url=?");
			else
				ps=conn.prepareStatement("update t_seedsite set layer=? where site_url=?");
			ps.setInt(1, status);
			ps.setString(2, seed);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	
	/**
	 * 更新种子状态,如果种子已被运行,则更新状态为1
	 * @param status
	 */
	public static void updateSeedInfo(int status,String column)
	{
		conn=ConnectionUtil.getInputConnection();
		PreparedStatement ps=null;
		try {
			if(column.equals("taskId"))
				ps=conn.prepareStatement("update t_seedsite set task_id=?");
			else
				ps=conn.prepareStatement("update t_seedsite set layer=?");
			ps.setInt(1, status);
			ps.executeUpdate();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
	}
	
	//获得链接对应hash值
	public static HashSet<Integer> getHashLink(int tableNumber,HashSet<Integer> hashLink,String domain)
	{
		Connection conn=ConnectionUtil.getCrawlerConnection();
		PreparedStatement ps=null;
		ResultSet rs=null;
		try {
			ps=conn.prepareStatement("select hashCode from http_"+tableNumber+" where domain=?");
			ps.setString(1, domain);
			rs=ps.executeQuery();
			while(rs.next())
			{
				hashLink.add(rs.getInt("hashCode"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				rs.close();
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return hashLink;
	}
	
	public static HashSet<Integer> getHashProgram(HashSet<Integer> hashProgram)
	{
		conn=ConnectionUtil.getCrawlerConnection();
		try {
			ps=conn.prepareStatement("select hashCode from t_result_file");
			rs=ps.executeQuery();
			while(rs.next())
			{
				hashProgram.add(rs.getInt("hashCode"));
			}
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}finally{
			try {
				ps.close();
				conn.close();
			} catch (SQLException e1) {
				// TODO Auto-generated catch block
				e1.printStackTrace();
			}
		}
		return hashProgram;
	}
		

	public static List<String> getKeywords() {
		return keywords;
	}


	public static List<FileInfo> getSuffixKeywords() {
		return suffixKeywords;
	}

	public static List<String> getProgramAreas() {
		return programAreas;
	}

	public static List<SeedInfo> getSeeds() {
		return seeds;
	}

	public static List<RegExp> getRegExps() {
		return regExps;
	}
	
	public static int getSeedNum(){
		return seedNum;
	}
	
	public static TaskInfo getTaskInfo() {
		return taskInfo;
	}

	public static void main(String[] args){
//		DataSource.setTaskInfo();
//		System.out.println(JudgeCondition.isProgram("http://www.sina.rar?.rm",DataSource.getSuffixKeywords()));
	}


}
