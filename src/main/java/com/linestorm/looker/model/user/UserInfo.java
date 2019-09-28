package com.linestorm.looker.model.user;

import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONObject;

@TableBind(tableName="user_info",pkName="id")
public class UserInfo extends Model<UserInfo> {
	private static final long serialVersionUID = 1L;
	public final static UserInfo dao = new UserInfo();

}
