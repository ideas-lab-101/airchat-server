package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

@TableBind(tableName="sys_function",pkName="FuncID")
public class FuncInfo extends Model<FuncInfo> {
	private static final long serialVersionUID = 1L;
	public final static FuncInfo dao = new FuncInfo();

	public int addFuncInfo(FuncInfo f){
		Record pr = null;
		int PID = f.getInt("PID");
        if(PID >0){
            pr = Db.findFirst("select d1.FuncCode,count(d2.FuncID) FCount \n"
                    + "from sys_function d1 \n"
                    + "left join sys_function d2 on d2.PID=d1.FuncID \n"
                    + "where d1.FuncID=? group by d1.FuncCode", PID);
        }else {
            pr = Db.findFirst("select '' FuncCode,count(d1.FuncID) FCount \n"
                    + "from sys_function d1 \n"
                    + "where d1.PID=? and d1.PlatformID=?", PID, f.getInt("PlatformID"));
        }
		String funcCode=(pr.getStr("FuncCode").length() == 0? "" : pr.getStr("FuncCode") + "-") + (pr.getLong("FCount") >= 9 ? (pr.getLong("FCount") + 1) : ("0" + (pr.getLong("FCount") + 1)));
		if(f.set("intState", 1).set("FuncCode", funcCode).save()){
			return f.getInt("FuncID");
		}else{
			return 0;
		}
	}
}
