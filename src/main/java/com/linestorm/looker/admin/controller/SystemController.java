package com.linestorm.looker.admin.controller;


import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.admin.common.AdminBaseController;
import com.linestorm.looker.admin.common.AdminLoginInterceptor;
import com.linestorm.looker.admin.common.LoginValidator;
import com.linestorm.looker.extend.CacheKey;
import com.linestorm.looker.extend.CaptchaRender;
import com.linestorm.looker.model.sys.AppVersion;
import com.linestorm.looker.model.user.SysUser;
import com.sagacity.utility.DateUtils;
import com.sun.deploy.ui.AppInfo;
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

@ControllerBind(controllerKey = "/system", viewPath = "/static")
public class SystemController extends AdminBaseController {

    @Override
    public void index(){

    }

    public void appManage(){
        setAttr("apps", Db.find("select * from app_version"));
        render("appManage.html");
    }

    public void editAppInfo(){
        setAttr("app", AppVersion.dao.findById(getPara("id")));
        render("appInfo.html");
    }

    @Before(Tx.class)
    public void saveAppInfo(){
        boolean r = getModel(AppVersion.class, "app").update();
        if(r){
            rest.success();
        }else{
            rest.error();
        }
        renderJson(rest);
    }

}
