package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="sys_appVersion",pkName="VersionID")
public class AppVersion extends Model<AppVersion> {
	private static final long serialVersionUID = 1L;
	public final static AppVersion dao = new AppVersion();

}
