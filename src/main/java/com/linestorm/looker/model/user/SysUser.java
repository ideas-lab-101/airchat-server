package com.linestorm.looker.model.user;

import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.kit.HashKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;
import net.sf.json.JSONObject;
import org.apache.shiro.SecurityUtils;

@TableBind(tableName="sys_users",pkName="user_id")
public class SysUser extends Model<SysUser> {
	private static final long serialVersionUID = 1L;
	public final static SysUser dao = new SysUser();

	public Record doAdminAuth(String username, String password) throws Exception{
		Record admin = Db.findFirst("select * from sys_users u where u.role_id=1 and u.login_name=?", username);
		if (null == admin) {
			throw new Exception("用户名不存在!");
		}
		if (admin.getInt("state") == 0) {
			throw new Exception("用户被锁定!");
		}
		System.out.println("password=="+HashKit.sha256(password));
		if (!HashKit.sha256(password).equals(admin.getStr("password"))){
			throw  new Exception("账号或密码错误!");
		}

		//返回登陆用户对象
		return admin;
	}
}
