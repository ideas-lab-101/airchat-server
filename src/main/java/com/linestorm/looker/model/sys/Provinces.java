package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

/**
 * @类名字：Provinces
 * @类描述：省份信息表
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-5-13
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */
@TableBind(tableName="sys_province",pkName="ProvinceID")
public class Provinces extends Model<Provinces> {
	/** serialVersionUID*/
	private static final long serialVersionUID = 1L;
	public static final Provinces dao=new Provinces();
	
	
	

}
