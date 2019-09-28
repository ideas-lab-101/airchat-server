package com.linestorm.looker.model.user;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_roles",pkName="role_id")
public class RoleInfo extends Model<RoleInfo> {
	private static final long serialVersionUID = 1L;
	public final static RoleInfo dao = new RoleInfo();
}
