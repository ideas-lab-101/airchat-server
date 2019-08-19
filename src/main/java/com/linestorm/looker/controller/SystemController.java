package com.linestorm.looker.controller;


import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.common.AdminBaseController;
import com.linestorm.looker.common.AdminLoginInterceptor;
import com.linestorm.looker.common.LoginValidator;
import com.linestorm.looker.extend.ResponseCode;
import com.sagacity.utility.DateUtils;
import net.sf.json.JSONObject;

/**
 * @类名字：CommonController
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-11-14
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */

@ControllerBind(controllerKey = "/")
public class SystemController extends AdminBaseController {

    private String cacheName = "AdminCache";

    public void index(){

    }

	public void admin() {
        JSONObject userInfo = getCurrentUser();
		setAttr("userInfo", userInfo);
        setAttr("resourceUrl", PropKit.get("resource.url"));
		render("adminIndex.html");
	}

    public void logout() {
        String uid = this.getCookie("u_id");
        CacheKit.remove(cacheName, uid);
        if("wap".equals(this.getCookie("loginType"))){
            redirect("/wx/adminLogin.html");
        }else{
            redirect("/adminLogin.html");
        }
    }

    /**
     * 管理用户登陆，下一步加入短信验证
     */
    @Before(LoginValidator.class)
    @Clear(AdminLoginInterceptor.class)
    public void adminLogin() {
        boolean r = false;
        String username = getPara("username");
        String password = getPara("password");
        String loginType = getPara("loginType");

        Record user = Db.findFirst(SqlKit.sql("user.userLoginIdentify"), username, password);
        if (user != null && user.getInt("RoleID") == 1) {
            String uid = "u_" + user.getStr("UserID");
            if(CacheKit.get(cacheName, uid) != null){ //已有登陆信息
                JSONObject jo = CacheKit.get(cacheName, uid);
                jo.put("AddTime", DateUtils.nowDateTime());
                jo.put("DeviceInfo", getRequest().getRemoteAddr());
                CacheKit.put(cacheName, uid, jo);
                r = true;
                responseData.put(ResponseCode.MSG, "验证成功，重新登陆！");
            }else {
                JSONObject jo = new JSONObject();
                jo.put("AddTime", DateUtils.nowDateTime());
                jo.put("DeviceInfo", getRequest().getRemoteAddr());
                jo.put("UserInfo", user.toJson());
                CacheKit.put(cacheName, uid, jo);
                this.setCookie("u_id", uid, 86400000);
                this.setCookie("loginType", loginType, 86400000);
                r = true;
                responseData.put(ResponseCode.MSG, "验证成功，即将登陆！");
            }
        }else{
            responseData.put(ResponseCode.MSG, "验证失败，请重新输入！");
        }
        responseData.put(ResponseCode.RESULT, r);
        renderJson(responseData);
    }

    /**
     * 加载子节点菜单
     */
    public void loadModuleList(){
        int platformID = getParaToInt("platformID", 0);
        JSONObject userInfo = getCurrentUser();
        renderJson(ResponseCode.LIST, Db.find(SqlKit.sql("sys.getModuleList"), platformID, userInfo.get("RoleID")));
    }

    /**
     * 根据Code加载下级菜单
     */
    public void loadMenuTree(){
        int platformID = getParaToInt("platformID", 0);
        String funcCode = getPara("funcCode");
        JSONObject userInfo = getCurrentUser();
        String sqlStr = "select mf.*,'top' iconPosition from sys_function mf \n" +
                "inner join sys_roleFunc rf on rf.FuncID = mf.FuncID \n" +
                "where mf.FuncType='menu' and mf.intState=1 and mf.PlatformID=? and mf.FuncCode like '"+funcCode+"%' and rf.RoleID=? \n" +
                "order by mf.FuncCode";
        renderJson(ResponseCode.LIST, Db.find(sqlStr, platformID, userInfo.get("RoleID")));
    }

}
