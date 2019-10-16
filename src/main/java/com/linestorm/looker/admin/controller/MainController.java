package com.linestorm.looker.admin.controller;


import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.linestorm.looker.admin.common.AdminBaseController;
import com.linestorm.looker.admin.common.AdminLoginInterceptor;

@ControllerBind(controllerKey = "/", viewPath = "/")
public class MainController extends AdminBaseController {

    @Override
    @Clear(AdminLoginInterceptor.class)
    public void index(){
        render("index.html");
    }
}
