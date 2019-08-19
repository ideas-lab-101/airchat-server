package com.linestorm.looker.api.controller.v1;


import com.jfinal.aop.Clear;
import com.jfinal.ext.route.ControllerBind;
import com.linestorm.looker.api.common.UserBaseController;
import com.linestorm.looker.api.common.UserLoginInterceptor;
import com.linestorm.looker.extend.NotifyType;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.service.message.Notify;
import com.linestorm.looker.service.message.PushMessage;

@ControllerBind(controllerKey = "/api/message")
public class MessageController extends UserBaseController {


    @Override
    public void index(){

    }

    @Clear(UserLoginInterceptor.class)
    public void testPush(){
        String[] tokens = new String[]{"2"};
        boolean r= PushMessage.dao.push(tokens, "业务消息推送", true, 1);
        responseData.put(ResponseCode.RESULT, r);
        renderJson(responseData);
    }

    @Clear(UserLoginInterceptor.class)
    public void testNotify(){
        Notify.dao.notify("2", NotifyType.apply_friend, "");
        responseData.put(ResponseCode.RESULT, true);
        renderJson(responseData);
    }
}
