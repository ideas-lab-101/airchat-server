package com.linestorm.looker.admin.controller;


import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.linestorm.looker.admin.common.AdminBaseController;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.model.user.Account;
import com.linestorm.looker.model.user.UserInfo;
import com.sagacity.utility.StringTool;

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

@ControllerBind(controllerKey = "/user", viewPath = "/static")
public class UserController extends AdminBaseController {

    @Override
    public void index(){

    }

    public void userManage(){
        render("userManage.html");
    }

    public void getUserList(){
        String sql_select = "select ul.id,ul.login_name,ul.snnumber,from_unixtime(ul.created_time) created_time,ui.username,ul.state";
        String sql_from = "from user_login ul\n" +
                "left join user_info ui on ui.user_id=ul.id\n" +
                "where 1=1\n" +
                "order by ul.created_time DESC";

        if (StringTool.notNull(getPara("pageIndex")) && !StringTool.isBlank(getPara("pageIndex"))){
            Page<Record> dataList = Db.paginate(getParaToInt("pageIndex", 1),
                    getParaToInt("pageSize", 10), sql_select, sql_from);
            rest.success().setData(dataList);
        }else {
            data.put(ResponseCode.LIST, Db.find(sql_select + "\n" + sql_from));
            rest.success().setData(data);
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void delUser(){
        rest.error();
        renderJson(rest);
    }

    @Before(Tx.class)
    public void setUserState(){
        boolean r = false;
        int state = getParaToBoolean("state")? 1:0;
        r = Account.dao.findById(getPara("id")).set("state", state).update();
        if(r){
            rest.success("设置成功！");
        }else{
            rest.error("设置失败！");
        }
        renderJson(rest);
    }

}
