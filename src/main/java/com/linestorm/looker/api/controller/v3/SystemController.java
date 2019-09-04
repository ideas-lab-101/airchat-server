package com.linestorm.looker.api.controller.v3;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.mail.MailKit;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.HashKit;
import com.jfinal.kit.StrKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.api.common.AppBaseController;
import com.linestorm.looker.api.common.AppLoginInterceptor;
import com.linestorm.looker.extend.CacheKey;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.extend.RoleType;
import com.linestorm.looker.model.sys.AppVersion;
import com.linestorm.looker.model.user.Account;
import com.linestorm.looker.model.user.UserDevice;
import com.linestorm.looker.model.user.UserInfo;
import com.linestorm.looker.service.message.SMSMessage;
import com.linestorm.looker.service.openapi.Qiniu;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONObject;

/**
 * Created by mulaliu on 16/6/23.
 */

@ControllerBind(controllerKey = "/api/system/v2")
public class SystemController extends AppBaseController {

    @Override
    public void index(){

    }

    /**
     * 返回注册随机码
     */
    @Clear(AppLoginInterceptor.class)
    public void getRegCode(){
        String token = HashKit.md5(DateUtils.getLongDateMilliSecond()+"");
        String regCode = StringTool.generateNumberString(6);
        CacheKit.put(CacheKey.CACHE_REG_CODE, token, regCode);

        data.put("regToken", token);
        data.put("regCode", regCode);
        rest.success().setData(data);
        renderJson(rest);
    }

    @Clear(AppLoginInterceptor.class)
    @Before(Tx.class)
    public void registerAccount(){
        boolean r = false;
        StringBuilder account = new StringBuilder("");

        String regToken = getPara("regToken");
        String regCode = getPara("regCode");
        String password = getPara("password");

        String languageSettings = getPara("languageSettings");
        String name = getPara("name");
        String sex = getPara("sex");

        String public_key = getPara("publicKey");

        if(regCode.equals(CacheKit.get(CacheKey.CACHE_REG_CODE, regToken))){
            r  =  Account.dao.regAccount(account, password, languageSettings, name, sex, public_key);

            if(r){
                CacheKit.remove(CacheKey.CACHE_REG_CODE, regToken);
                rest.success("注册成功").setData(account.toString());
            }else{
                rest.error("注册失败");
            }
        }else{
            rest.error("注册码失效").setCode(-2);
        }
        renderJson(rest);
    }

    /**
     *  检查账号是否可注册
     */
    private String SMSIdentifyCache = "SMSIdentifyCache";
    @Clear(AppLoginInterceptor.class)
    public void checkAccount(){
        String account = getPara("account");
        int accountType = getParaToInt("accountType", 1);

        String content = "";

        //检查loginName是否已经有了
        if(Db.findFirst("select login_name from user_login where login_name=?", account.trim()) == null){
            if(StringTool.checkEmail(account) || StringTool.checkMobileNumber(account)){
                //生成验证码
                String regCode = StringTool.generateNumberString(6);
                //下发验证码
                switch (accountType){
                    case 1: //电话注册
                        content = "【ChatCare】用户注册，你的注册码是："+ regCode;
//                        System.out.println(content);
                        SMSMessage.dao.push(account, content);
                        break;
                    case 2: //邮件发送
                        content = "【ChatCare】用户注册，你的注册码是："+ regCode;
                        MailKit.send(account, null, "千寻注册码", content);
                        break;
                }
                CacheKit.put(SMSIdentifyCache, account, regCode);
                rest.success("请注意查收验证码");
            }else{
                rest.error("无效的账号");
            }
        }else{
            rest.error("账号已注册");
        }
        renderJson(rest);
    }

    /**
     * 注册账号
     */
//    @Clear(AppLoginInterceptor.class)
//    @Before(Tx.class)
//    public void registerAccount(){
//        boolean r = false;
//        String account = getPara("account");
//        String password = getPara("password");
//        String registerCode = getPara("registerCode");
//
//        String languageSettings = getPara("languageSettings");
//        String name = getPara("name");
//        String sex = getPara("sex");
//
//        String public_key = getPara("publicKey");
//
//        if(CacheKit.get(SMSIdentifyCache, account).equals(registerCode)){
//            Account na =  Account.dao.createAccount(account, password);
//            //写附表
//            r = new UserInfo().set("user_id", na.get("id")).set("username", name).set("sex", sex).set("language_settings", languageSettings)
//                    .set("voice_settings", "cat").set("public_key", public_key)
//                    .set("created_time", DateUtils.getTimeStamp()).set("deleted", 0).save();
//            if(r){
//                rest.success("注册成功");
//            }else{
//                rest.error("注册失败");
//            }
//        }else{
//            rest.error("注册码错误").setCode(-2);
//        }
//        renderJson(rest);
//    }


    @Clear(AppLoginInterceptor.class)
    public void retrievePassword(){
        String account = getPara("account");
        int accountType = getParaToInt("accountType", 1);

        String content = "";

        //检查loginName是否已经有了
        if(Db.findFirst("select login_name from user_login where login_name=?", account.trim()) != null){
            //生成验证码
            String regCode = StringTool.generateNumberString(6);
            //下发验证码
            switch (accountType){
                case 1: //电话
                    content = "【ChatCare】用户你好，你的验证码是："+ regCode;
                    SMSMessage.dao.push(account, content);
                    break;
                case 2: //邮件
                    content = "【ChatCare】用户你好，你的验证码是："+ regCode;
                    MailKit.send(account, null, "千寻验证码", content);
                    break;
            }
            CacheKit.put(SMSIdentifyCache, account, regCode);
            rest.success("请注意查收验证码");
        }else{
            rest.error("账号错误");
        }
        renderJson(rest);
    }

    @Before(Tx.class)
    public void changePassword(){
        boolean r = false;
        String account = getPara("account");
        String password = getPara("password");
        String verifyCode = getPara("verifyCode");

        if(CacheKit.get(SMSIdentifyCache, account).equals(verifyCode)){
            Account na =  Account.dao.findFirst("select * from user_login where login_name=?", account);
            r = na.set("password", password).update();
            if(r){
                rest.success("密码修改成功");
            }else{
                rest.error("密码修改失败");
            }
        }else{
            rest.error("验证码错误");
        }
        renderJson(rest);
    }

    /**
     * 提供第三方服务认证用户后获得需要的用户信息，例如推送token，用户设置等。
     */
    @Clear(AppLoginInterceptor.class)
    public void authUser(){
        String account = getPara("account");
        String password = getPara("password");

        int code = 0;
        StringBuffer msg = new StringBuffer();
        String token = "";

        Record record_user = Db.findFirst(SqlKit.sql("user.userAuth"), account, password);
        if (record_user != null){
            code = 1 ;
            data.put("userInfo", record_user);
            rest.success("Auth Success！").setData(data);
        }else{
            rest.error("Auth Failed！");
        }
        renderJson(rest);
    }

    /**
     * 检查客户端版本
     */
    @Clear(AppLoginInterceptor.class)
    public void checkAppVersion(){
        String os = getPara("os");
        String version = getPara("version");

        AppVersion av = AppVersion.dao.findFirst("select * from app_version where os=? and state=1"
                , os);
        if(av.getStr("version").compareTo(version)<=0){
            rest.error("你已更新到最新版！");
        }else{
            data.put("version_desc", av.getStr("version_desc"));
            data.put("version_url", av.getStr("version_url"));
            rest.success("有新的版本可下载！").setData(data);
        }
        renderJson(rest);
    }

    /**
     * 登陆认证
     * userName —— 用户名
     * password —— 密码
     * deviceInfo —— 设备信息（包括uniqueID，deviceToken）
     */
    private String AccountLockCacheName = "AccountLockCache";

    @Clear(AppLoginInterceptor.class)
    public void userLogin(){
        String account = getPara("account");
        String password = getPara("password");
        String deviceInfo = getPara("deviceInfo");

        int code = 0;
        StringBuffer msg = new StringBuffer();
        String token = "";

        Record record_user = Db.findFirst(SqlKit.sql("user.userLoginIdentify"), account, password);
        int errorCount = CacheKit.get(AccountLockCacheName, account)!= null ? (int) CacheKit.get(AccountLockCacheName, account) : 0 ;
        if (errorCount < 5){
            if (record_user != null && record_user.getInt("role_id") == RoleType.AppUser){
                token = HashKit.md5(DateUtils.getLongDateMilliSecond()+"");

                JSONObject o = new JSONObject(); //此对象中保存用户信息和当前设备信息，用以控制后续锁定设备登陆等功能。

                o.put("addTime", DateUtils.nowDateTime());
                o.put("userInfo", record_user.toJson());
                o.put("deviceInfo", deviceInfo);
                CacheKit.put(cacheName, token, o);
                //更新用户设备表
                if(deviceInfo != null){
                    UserDevice.dao.registerDeviceInfo(record_user.getInt("user_id")+"", deviceInfo);
                }
                msg.append("登录验证成功！");
                code = 1 ;
            }else{
                CacheKit.put(AccountLockCacheName, account, errorCount+1); //写入错误次数
                Record u = Db.findFirst("select * from user_login u where u.login_name=?", account);
                if(u == null){
                    msg.append("用户账号不存在！");
                }else if(u.getInt("state") == 0){
                    msg.append("用户账号已停用！");
                }else if(!u.getStr("password").equals(password)){
                    msg.append("密码错误！");
                }else{
                    msg.append("登录验证错误！");
                }

            }
        }else{
            msg.append("达到最大失败尝试次数,请10分钟后再次登陆!");
        }
        if(code == 1){
            data.put("token", token);
            data.put("userInfo", record_user);
            rest.success(msg.toString()).setData(data);
        }else{
            rest.error(msg.toString());
        }
        renderJson(rest);
    }

    public void userLogout(){
        String token = getPara("token");

        UserDevice.dao.removeDeviceInfo(getCurrentUser(token).getString("user_id"));
        CacheKit.remove(cacheName, token);
        rest.success();
        renderJson(rest);
    }

    /**
     * 获得7牛上传鉴权
     */
    public void getUploadToken(){
        rest.success().setData(Qiniu.dao.getUploadToken(getParaToInt("type", 1)));
        renderJson(rest);
    }

    public void getTagList(){
        String token = getPara("token");
        int tag_type = getParaToInt("tag_type", 1);
        String sql = "select t.* from sys_tags t\n" +
                "where t.tag_type=? and t.state=1\n" +
                "order by t.tag_count DESC";
        data.put(ResponseCode.LIST, Db.find(sql, tag_type));

        rest.success().setData(data);
        renderJson(rest);
    }

    /**
     * 下载页面
     */
    @Clear(AppLoginInterceptor.class)
    public void download(){
        render("/download.html");
    }

}
