package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;

@TableBind(tableName="sys_role_func",pkName="mapping_id")
public class RoleFunc extends Model<RoleFunc> {
	private static final long serialVersionUID = 1L;
	public final static RoleFunc dao = new RoleFunc();

}
