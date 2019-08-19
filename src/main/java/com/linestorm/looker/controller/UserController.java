package com.linestorm.looker.controller;


import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.render.excel.PoiRender;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.linestorm.looker.common.AdminBaseController;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.model.user.Account;
import com.linestorm.looker.model.user.RoleInfo;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.Map;

/**
 * Created by mulaliu on 15/2/5.
 */
@ControllerBind(controllerKey = "/user")
public class UserController extends AdminBaseController {

    @Override
    public void index(){

    }

    public void getRoleList() {
        renderJson (ResponseCode.LIST, Db.find("select * from sys_roles"));
    }

    public void saveRoleChange(){
        boolean r = false;
        String msg ="";
        JSONObject userInfo = getCurrentUser();

        JSONArray ja = JSONArray.fromObject(getPara("data"));
        for (int i=0;i<ja.size();i++){
            Map o=(Map)ja.get(i);
            switch (o.get("_state").toString()){
                case "added" :
                    r=new RoleInfo().set("RoleCode", o.get("RoleCode"))
                            .set("RoleName", o.get("RoleName")).set("Description",o.get("Description")).save();
                    break;
                case "removed" :
                    //判断年级有班级适用
                    if(Db.find("select * from sys_users where RoleID=?", o.get("RoleID")).size()>0){
                        msg = "角色已启用，不允许删除！";
                    }else{
                        r = RoleInfo.dao.deleteById(o.get("RoleID"));
                    }
                    break;
                case "modified" :
                    r = RoleInfo.dao.findById(o.get("RoleID")).set("RoleCode", o.get("RoleCode"))
                            .set("RoleName",o.get("RoleName")).set("Description",o.get("Description")).update();
                    break;
                default :
                    break;
            }
        }
        if(r){
            responseData.put(ResponseCode.MSG, "角色更新成功！");
        }else{
            responseData.put(ResponseCode.MSG, msg == "" ? "角色更新失败！" : msg);
        }
        responseData.put(ResponseCode.RESULT, r);
        renderJson(responseData);
    }

    /**
     * 此方法支持查询管理用户与普通用户
     */
    public void getOrgUserList() {
        JSONObject userInfo = getCurrentUser();

        //加入对检索的支持
        String searchKey=getPara("key");
        Object orgID = getPara("orgID") == null || getPara("orgID") == "" ? userInfo.getString("OrgID") : getPara("orgID");

        String sqlSelect = SqlKit.sql("user.getUserList-select");
        String sqlFrom = SqlKit.sql("user.getUserList-from");

        if (StringTool.notNull(searchKey)  || !StringTool.isBlank(searchKey)) {
            sqlFrom += " and (u.Caption LIKE '%"+ searchKey +"%' or u.LoginName LIKE '%" + searchKey + "%')";
        }
        renderJson(Db.paginate(getParaToInt("pageIndex") + 1, getParaToInt("pageSize"),
                sqlSelect, sqlFrom, getPara("roleType"), orgID));
    }

    /**
     * 导出用户数据
     */
    public void exportUserData(){
        JSONObject userInfo = getCurrentUser();
        Object orgID = getPara("orgID") == null || getPara("orgID") == "" ? userInfo.getString("OrgID") : getPara("orgID");
        String roleType = getPara("roleType");

        String sqlUser = SqlKit.sql("user.getUserList-select") + "\n" + SqlKit.sql("user.getUserList-from");

        String filename = DateUtils.nowDateTime() + "-userData.xls";
        String[] headers = new String[] {"用户帐号","关注时间","用户显示名","认证信息"};
        String[] columns = new String[] {"LoginName","AddTime","Caption","RelationInfo"};
        render(PoiRender.me(Db.find(sqlUser, roleType, orgID)).fileName(filename)
                .sheetName("用户数据").headers(headers).columns(columns).cellWidth(5000));
    }

    /**
     * 修改用户密码
     */
    @Before(Tx.class)
    public void changePassword(){
        JSONObject userInfo = getCurrentUser();
        String newPwd = getPara("newPwd");
        boolean r = false;
        StringBuffer msg = new StringBuffer("");
        Account ui = Account.dao.findFirst("select * from user_login u where u.user_id=?", userInfo.get("userId"));
        String oldPwd = getPara("oldPwd") == null ? ui.getStr("Password") : getPara("oldPwd");
        if (ui.getInt("intState")==0){
            msg.append("用户已停用，不允许更改密码！");
        }else if(!oldPwd.equals(ui.getStr("Password"))){
            msg.append("原始密码输入错误，请重新输入！");
        }else{
            if (Account.dao.checkPasswordRule(ui, newPwd, msg)){
                r = ui.set("Password", newPwd).update();
                if(r){
                    msg.append("密码更新成功！");
                }else{
                    msg.append("密码更新失败！");
                }
            }
        }
        responseData.put(ResponseCode.RESULT, r);
        responseData.put(ResponseCode.MSG, msg.toString());
        renderJson(responseData);
    }

    public void getUserDeviceInfo() {
        String sqlSelect="select ud.DeviceID,ud.OSType,ud.OSVersion,ud.DeviceToken,ud.RegisterTime,ud.UpdateTime,ud.intActive";
        String sqlFrom="from sys_userDevice ud where ud.UserID=?";
        Page<Record> page = Db.paginate(getParaToInt("pageIndex", 0) + 1,
                getParaToInt("pageSize", 20),
                sqlSelect,
                sqlFrom, getPara("userID"));
        renderJson(page);
    }
}
