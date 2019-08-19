package com.linestorm.looker.service.timer;

import com.linestorm.looker.model.bubble.BubbleInfo;
import org.quartz.Job;
import org.quartz.JobExecutionContext;
import org.quartz.JobExecutionException;

/**
 * @file MyJob.java
 * @brief XXX
 * 
 * @author Administrator
 * @date 2012-5-8
 * 
 * @details <i>CopyRright 2012 LEMOTE. All Rights Reserved.</i>
 */

/**
 * @brief 继承了Job接口的任务类
 * 
 */
public class SysJob1 implements Job {

	/**
     * 新优惠券提醒任务，过期提醒
	 * @param arg0
     * @throws JobExecutionException
	 */
	@Override
	public void execute(JobExecutionContext arg0) throws JobExecutionException {
		// TODO Auto-generated method stub
		//输出执行myjob的时间
		try {
			BubbleInfo.dao.validCheck();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}
}
