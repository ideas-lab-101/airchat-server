package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

/**
 * @类名字：Districts
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-9-11
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */
@TableBind(tableName="sys_district" ,pkName="DistrictID")
public class Districts extends Model<Districts> {
	private static final long serialVersionUID = 1L;
	public static final Districts dao=new Districts();

}
