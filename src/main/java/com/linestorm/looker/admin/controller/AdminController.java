package com.linestorm.looker.admin.controller;


import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.aop.Duang;
import com.jfinal.ext.plugin.shiro.ShiroInterceptor;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.admin.common.AdminBaseController;
import com.linestorm.looker.admin.common.AdminLoginInterceptor;
import com.linestorm.looker.admin.common.LoginValidator;
import com.linestorm.looker.extend.CacheKey;
import com.linestorm.looker.extend.CaptchaRender;
import com.linestorm.looker.model.user.SysUser;
import com.sagacity.utility.DateUtils;
import net.sf.json.JSONObject;

import java.util.List;

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

@ControllerBind(controllerKey = "/admin", viewPath = "/static")
public class AdminController extends AdminBaseController {

    @Override
    public void index(){
        JSONObject userInfo = getCurrentUser();
        setAttr("userInfo", userInfo);

        render("index.html");
    }

    /** 桌面 */
    public void desktop(){
        //加载模块列表
        JSONObject userInfo = getCurrentUser();
        setAttr("moduleList", Db.find(SqlKit.sql("sys.getModule"), userInfo.get("role_id")));
        render("desktop.html");
    }

    public void logout() {
        String token = this.getCookie("token");
        CacheKit.remove(CacheKey.CACHE_ADMIN_AUTH, token);
        redirect("/index.html");
    }

    @Clear(AdminLoginInterceptor.class)
    public void captcha() {
        CaptchaRender img = new CaptchaRender(4);
        this.setSessionAttr(CaptchaRender.DEFAULT_CAPTCHA_MD5_CODE_KEY, img.getMd5RandonCode());
        render(img);
    }

    /**
     * 管理用户登陆，下一步加入短信验证
     */
    @Before(LoginValidator.class)
    @Clear(AdminLoginInterceptor.class)
    public void login() {
        String username = getPara("username");
        String password = getPara("password");
        String addr = getRequest().getRemoteAddr();
        String token = HashKit.md5(DateUtils.getLongDateMilliSecond()+"");

        try{
            Record admin = SysUser.dao.doAdminAuth(username, password);
            //写入缓存
            JSONObject jo = new JSONObject();
            jo.put("loginTime", DateUtils.nowDateTime());
            jo.put("userInfo", admin.toJson());
            jo.put("addr", addr); //保存IP地址
            CacheKit.put(CacheKey.CACHE_ADMIN_AUTH, token, jo);
            //写入cookie
            this.setCookie("token", token, 86400000);
            rest.success("验证成功，即将登陆!");

        }catch (Exception ex) {
            rest.error(ex.getMessage());
        }
        renderJson(rest);
    }

    /**
     * 根据Code加载下级菜单
     */
    public void loadMenuTree(){
        String moduleID = getPara("moduleID");
        JSONObject userInfo = getCurrentUser();

        String menuSql = SqlKit.sql("sys.getMenu");
        List<Record> menuSet = Db.find(menuSql, moduleID, userInfo.get("role_id"));
        for (Record menu : menuSet){
            List<Record> subMenu = Db.find(menuSql, menu.get("id"), userInfo.get("role_id"));
            if(subMenu != null){
                menu.set("children", subMenu);
            }
        }
        renderJson(menuSet);
    }

}
