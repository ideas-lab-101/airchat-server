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

}
