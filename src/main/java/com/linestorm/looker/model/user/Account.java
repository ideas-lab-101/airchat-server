package com.linestorm.looker.model.user;

import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.sagacity.utility.DateUtils;

@TableBind(tableName="user_login",pkName="id")
public class Account extends Model<Account> {
	private static final long serialVersionUID = 1L;
	public final static Account dao = new Account();

	public boolean checkPasswordRule(Account account, String newPwd, StringBuffer msg){
	    boolean r = false;
		return r;
	}

	@Before(Tx.class)
	public Account createAccount(String account, String password){
	    Account a = new Account().set("login_name", account).set("password", password).set("deleted", 0)
                .set("role_id", 2).set("created_time", DateUtils.getTimeStamp()).set("state", 1);
		a.save();
	    //计算千寻号-最大值加1
        String sql = "select max(snnumber) sn from user_login";
		a.set("snnumber", Db.findFirst(sql).getLong("sn").intValue()+1).update();
	    return a;
	}

	@Before(Tx.class)
	public boolean regAccount(StringBuilder account, String password, String languageSettings, String name, String sex, String public_key){
	    boolean r = false;
		//计算千寻号-最大值加1
		int sn = Db.findFirst("select max(snnumber) sn from user_login").getLong("sn").intValue()+1;
        account = account.append(sn);
		Account a = new Account().set("login_name", account.toString()).set("snnumber", sn)
				.set("password", password).set("deleted", 0).set("role_id", 2).set("created_time", DateUtils.getTimeStamp()).set("state", 1);
		r = a.save();

		//写附表
		r = new UserInfo().set("user_id", a.get("id")).set("username", name).set("sex", sex).set("language_settings", languageSettings)
				.set("voice_settings", "cat").set("public_key", public_key)
				.set("created_time", DateUtils.getTimeStamp()).set("deleted", 0).save();
		return r;
	}

}
