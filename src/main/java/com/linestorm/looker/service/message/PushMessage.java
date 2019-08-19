package com.linestorm.looker.service.message;

import com.jfinal.kit.HttpKit;
import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.linestorm.looker.model.user.Account;
import com.linestorm.looker.model.user.UserDevice;
import net.sf.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PushMessage {

    public final static PushMessage dao = new PushMessage();
    private final static String sound = "cat.mp3"; //默认音

    private String pushApiUrl = PropKit.get("push.url");

    /**
     * 推送主体
     * @param users
     * @param message
     * @param blnProduction
     * @return
     */
    public boolean push(String[] users, String message, boolean blnProduction, int pushcode){

        ArrayList iosPush = new ArrayList<String>();
        ArrayList androidPush = new ArrayList<String>();

        Map<String, Object> params = new HashMap<String, Object>();
        List<Map<String, Object>> listPush = new ArrayList<Map<String, Object>>();
        for (String uid : users){
            Record ud = Db.findFirst("select * from user_device where user_id=? and state=1", uid);

            if(ud != null) {
                if(ud.getStr("os_type").equals("iOS")){
                    iosPush.add(ud.getStr("token"));
                }else if(ud.getStr("os_type").equals("android")){
                    androidPush.add(ud.getStr("token"));
                }
            }
        }
        //拼接ios
        Map<String, Object> pushInfo = new HashMap<String, Object>();
        if(iosPush.size()>0){
            pushInfo.put("tokens", (String[])iosPush.toArray(new String[iosPush.size()]));
            pushInfo.put("platform", 1);
            pushInfo.put("message", message);
            pushInfo.put("topic", "Cryeye.Inc.ChatCare");
            pushInfo.put("sound", sound);
            pushInfo.put("production", blnProduction? true: false);
            pushInfo.put("development", blnProduction? false: true);
            pushInfo.put("pushcode", pushcode);
            listPush.add(pushInfo);
        }
        //拼接android
        if(androidPush.size()>0){
            pushInfo.put("tokens", (String[])androidPush.toArray(new String[iosPush.size()]));
            pushInfo.put("platform", 2);
            pushInfo.put("message", message);
            pushInfo.put("sound", sound);
            pushInfo.put("production", blnProduction? true: false);
            pushInfo.put("development", blnProduction? false: true);
            pushInfo.put("pushcode", pushcode);
            listPush.add(pushInfo);
        }
        if(listPush.size() >0){
            params.put("notifications", listPush);
            String messageStr = JsonKit.toJson(params);
            JSONObject result = JSONObject.fromObject(HttpKit.post(pushApiUrl, messageStr));

//            System.out.println(result.toString());
            if(result.get("success") == null || result.get("success").equals("ok")){
                return true;
            }else{
                return false;
            }
        }else{
            return false;
        }
    }

    public static void main(String[] args) throws IOException {
        String[] tokens = new String[]{"2"};
        boolean r= PushMessage.dao.push(tokens, "业务消息推送", true, 1);
        System.out.println(r);
    }
}
