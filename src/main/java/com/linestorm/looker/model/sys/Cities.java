package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

/**
 * @类名字：Cities
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-9-11
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */
@TableBind(tableName="sys_city",pkName="CityID")
public class Cities extends Model<Cities> {
	private static final long serialVersionUID = 1L;
	public static final Cities dao=new Cities();

}
