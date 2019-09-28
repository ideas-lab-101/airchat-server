package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

@TableBind(tableName="sys_function",pkName="func_id")
public class FuncInfo extends Model<FuncInfo> {
	private static final long serialVersionUID = 1L;
	public final static FuncInfo dao = new FuncInfo();

}
