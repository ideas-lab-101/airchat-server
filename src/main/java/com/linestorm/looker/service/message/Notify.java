package com.linestorm.looker.service.message;

import com.jfinal.kit.HttpKit;
import com.linestorm.looker.extend.NotifyType;
import com.linestorm.looker.model.user.Account;
import com.sagacity.utility.PropertiesFactoryHelper;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

public class Notify {

    public final static Notify dao = new Notify();

    private String notifyApiUrl = PropertiesFactoryHelper.getInstance()
            .getConfig("notify.url");

    /**
     * 推送主体
     * @param userID
     * @param notifyType
     * @return
     */
    public void notify(String userID, int notifyType, String id){
        Account account = Account.dao.findById(userID);
        Map<String, String> params = new HashMap<String, String>();
        params.put("account", account.getStr("login_name"));
        params.put("notifyType", notifyType+"");
        //扩展内容
        params.put("content", id);
        HttpKit.get(notifyApiUrl, params);
    }

    public static void main(String[] args) throws IOException {
        Notify.dao.notify("1", NotifyType.bubble_notify, "10");
    }
}
