package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_enumdetail",pkName="DetailID")
public class EnumDetail extends Model<EnumDetail> {
	private static final long serialVersionUID = 1L;
	public final static EnumDetail dao = new EnumDetail();

}
