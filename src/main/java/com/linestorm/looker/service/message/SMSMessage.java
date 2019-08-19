package com.linestorm.looker.service.message;

import com.jfinal.kit.JsonKit;
import com.jfinal.kit.PropKit;
import net.sf.json.JSONObject;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Set;

public class SMSMessage {

    public final static SMSMessage dao = new SMSMessage();

    private String smsApiUrl = PropKit.get("sms.url");
    private final static String appid = PropKit.get("sms.appid");
    private final static String appkey = PropKit.get("sms.appkey");

    public boolean push(String to, String content){

        HashMap<String, String> params = new HashMap<String, String>();
        params.put("to", to);
        params.put("content", content);
        params.put("appid", appid);
        params.put("signature", appkey);

        String messageStr = JsonKit.toJson(params);
//        Map<String, String > header = new HashMap<String, String>();
//        header.put("Content-Type", "application/x-www-form-urlencoded");
//        JSONObject result = JSONObject.fromObject(HttpKit.post(smsApiUrl, messageStr, header));

        JSONObject result = JSONObject.fromObject(executePostByUsual(smsApiUrl, params));
        if(result.get("status").equals("success")){
            return true;
        }else{
            return false;
        }
    }

    public static String executePostByUsual(String actionURL, HashMap<String, String> parameters) {
        String response = "";
        try {
            URL url = new URL(actionURL);
            HttpURLConnection connection = (HttpURLConnection) url.openConnection();
            // 发送post请求需要下面两行
            connection.setDoInput(true);
            connection.setDoOutput(true);
            connection.setUseCaches(false);
            connection.setRequestMethod("POST");
            connection.setRequestProperty("Connection", "Keep-Alive");
            connection.setRequestProperty("Charset", "UTF-8");
            ;
            connection.setRequestProperty("Content-Type", "application/x-www-form-urlencoded");
            // 设置请求数据内容
            String requestContent = "";
            Set<String> keys = parameters.keySet();
            for (String key : keys) {
                requestContent = requestContent + key + "=" + parameters.get(key) + "&";
            }
            requestContent = requestContent.substring(0, requestContent.lastIndexOf("&"));
            DataOutputStream ds = new DataOutputStream(connection.getOutputStream());
            // 使用write(requestContent.getBytes())是为了防止中文出现乱码
            ds.write(requestContent.getBytes());
            ds.flush();
            try {
                // 获取URL的响应
                BufferedReader reader = new BufferedReader(new InputStreamReader(connection.getInputStream(), "utf-8"));
                String s = "";
                String temp = "";
                while ((temp = reader.readLine()) != null) {
                    s += temp;
                }
                response = s;
                reader.close();
            } catch (IOException e) {
                e.printStackTrace();
                System.out.println("No response get!!!");
            }
            ds.close();
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Request failed!");
        }
        return response;
    }
}
