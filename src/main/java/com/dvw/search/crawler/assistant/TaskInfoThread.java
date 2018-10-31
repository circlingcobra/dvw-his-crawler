package com.dvw.search.crawler.assistant;

import com.dvw.search.crawler.dao.DBOperation;
import com.dvw.search.crawler.dao.DataSource;
import com.dvw.search.crawler.domain.TaskInfo;


/**
 * t_task_info表信息统计类
 * @author David.Wang
 *
 */
public class TaskInfoThread implements Runnable{

	private DBOperation dbo=new DBOperation();
	public void run() {
		while(true)
		{
			try {
				Thread.sleep(60*1000*2);
//				System.out.println("开始统计t_task_info ----------------------");
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			// TODO Auto-generated method stub
			TaskInfo totalInfo=new TaskInfo();
			totalInfo=dbo.getTaskInfo();
			DataSource.updateTaskInfo(totalInfo);
		}
	}

	
}
