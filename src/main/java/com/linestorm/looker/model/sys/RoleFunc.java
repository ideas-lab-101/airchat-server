package com.linestorm.looker.model.sys;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

import java.util.ArrayList;
import java.util.List;

@TableBind(tableName="sys_roleFunc",pkName="MappingID")
public class RoleFunc extends Model<RoleFunc> {
	private static final long serialVersionUID = 1L;
	public final static RoleFunc dao = new RoleFunc();

	/**
	 * 根据角色获取按钮权限
	 * @param menuID
	 * @param roleID
	 * @return
	 */
	public ArrayList getFuncMenu(int menuID, int roleID){
		ArrayList menuList = new ArrayList();
		List<Record> menus = Db.find("select sf.* from sys_function sf\n" +
				"inner join sys_roleFunc rf on rf.FuncID=sf.FuncID\n" +
				"where sf.FuncType='button' and sf.PID=? and rf.RoleID=?", menuID, roleID);
		for(Record m : menus){
			menuList.add(m.get("FuncID"));
		}
		return menuList;
	}
}
