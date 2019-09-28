package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_enum_master",pkName="master_id")
public class EnumMaster extends Model<EnumMaster> {
	private static final long serialVersionUID = 1L;
	public final static EnumMaster dao = new EnumMaster();

}
