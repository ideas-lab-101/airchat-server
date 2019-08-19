package com.linestorm.looker.api.controller.v1;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.linestorm.looker.api.common.UserBaseController;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.model.user.UserInfo;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONObject;

/**
 * Created by mulaliu on 16/6/23.
 */

@ControllerBind(controllerKey = "/api/user")
public class UserController extends UserBaseController {

    @Override
    public void index(){

    }


    @Before(Tx.class)
    public void updateAvatar(){
        JSONObject user = getCurrentUser(getPara("token"));
        String avatarUrl = getPara("avatarUrl");

        UserInfo ui = UserInfo.dao.findFirst("select * from user_info where user_id=?", user.get("user_id"));
        boolean r = ui.set("avatar_url", avatarUrl).update();

        if(r){
            responseData.put(ResponseCode.MSG, "更新成功！");
        }else{
            responseData.put(ResponseCode.MSG, "更新失败！");
        }
        responseData.put(ResponseCode.CODE, r?1:0);
        renderJson(responseData);

    }

    /**
     * 修改用户信息
     */
    @Before(Tx.class)
    public void updateUserInfo(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));

        UserInfo ui = UserInfo.dao.findFirst("select * from user_info where user_id=?", user.get("user_id"));
        if(StringTool.notNull(getPara("username"))){
            r = ui.set("username", getPara("username")).update();
        }
        if(StringTool.notNull(getPara("introduction"))){
            r = ui.set("introduction", getPara("introduction")).update();
        }
        if(StringTool.notNull(getPara("constellation"))){
            r = ui.set("constellation", getPara("constellation")).update();
        }
        if(StringTool.notNull(getPara("background_url"))){
            r = ui.set("background_url", getPara("background_url")).update();
        }
        if(StringTool.notNull(getPara("voice_settings"))){
            r = ui.set("voice_settings", getPara("voice_settings")).update();
        }
        if(StringTool.notNull(getPara("public_key"))){
            r = ui.set("public_key", getPara("public_key")).update();
        }
        if(r){
            responseData.put(ResponseCode.MSG, "更新成功！");
        }else{
            responseData.put(ResponseCode.MSG, "更新失败！");
        }
        responseData.put(ResponseCode.CODE, r?1:0);
        renderJson(responseData);
    }

}
