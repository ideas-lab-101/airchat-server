package com.linestorm.looker.api.controller.v3;

import com.jfinal.aop.Before;
import com.jfinal.aop.Clear;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.linestorm.looker.api.common.AppBaseController;
import com.linestorm.looker.api.common.AppLoginInterceptor;
import com.linestorm.looker.extend.NotifyType;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.model.contact.ContactFriend;
import com.linestorm.looker.model.contact.ContactLike;
import com.linestorm.looker.model.contact.ContactRelation;
import com.linestorm.looker.model.user.Account;
import com.linestorm.looker.service.message.Notify;
import com.linestorm.looker.service.message.PushMessage;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONObject;

@ControllerBind(controllerKey = "/api/contact/v2")
public class ContactController extends AppBaseController {

    @Override
    public void index(){

    }

    /**
     * 获得好友列表
     */
    public void getFriendList(){
        JSONObject user = getCurrentUser(getPara("token"));

        String sql_base = "select cf.id,cf.friend_id,cf.label,ul.login_name account,uf.username,uf.avatar_url,uf.introduction,uf.public_key \n" +
                "from contact_friend cf\n" +
                "left join user_login ul on ul.id=cf.friend_id\n" +
                "left join user_info uf on uf.user_id=ul.id\n" +
                "where cf.user_id=?\n";

        String sql1 = sql_base + " order by uf.created_time";

        data.put(ResponseCode.LIST, Db.find(sql1, user.get("user_id")));
        rest.success().setData(data);
        renderJson(rest);
    }

    public void getUserInfo(){
        JSONObject user = getCurrentUser(getPara("token"));

        Account ac = null;
        if(StringTool.notNull(getPara("user_id")) && StringTool.notBlank(getPara("user_id"))){
            ac = Account.dao.findById(getPara("user_id"));
        }else if (StringTool.notNull(getPara("account")) && StringTool.notBlank(getPara("account"))){
            ac = Account.dao.findFirst("select * from user_login where login_name=?", getPara("account"));
        }

        Record userInfo = Db.findFirst(SqlKit.sql("user.getUserInfo"), ac.get("id"));
        userInfo.set("blnFriend", ContactFriend.dao.blnFriend(user.getString("user_id"), ac.get("id")+""));
        rest.success().setData(userInfo);
        renderJson(rest);
    }

    /**
     * 删除好友关系
     */
    @Before(Tx.class)
    public void delFriend(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        final int friend_id = getParaToInt("friend_id");

        String sql = "delete from contact_friend where user_id=? and friend_id=?";
        r = Db.update(sql, user.get("user_id"), friend_id) >0 ? true: false;
        r = Db.update(sql, friend_id, user.get("user_id")) >0 ? true: false;

        if(r){
            //给用户发送消息
            new Thread(){
                public void run(){
                    Notify.dao.notify(friend_id + "", NotifyType.del_friend, "");
                }
            }.start();
            rest.success("操作成功！");
        }else{
            rest.error("操作失败！");
        }
        renderJson(rest);
    }

    /**
     * 好友申请 1-成功；-2 已经是好友； -3 超过好友限制
     */
    @Before(Tx.class)
    public void applyFriend(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        final int user_id = getParaToInt("user_id");

        if(Db.findFirst("select * from contact_relation where apply_user_id=? and confirm_user_id=? and confirmed=0", user.get("user_id"), user_id) == null){
            r = new ContactRelation().set("apply_user_id", user.get("user_id")).set("confirm_user_id", user_id)
                    .set("apply_msg", getPara("apply_msg"))
                    .set("confirmed", 0).set("apply_time", DateUtils.getTimeStamp()).save();
            //组装推送
            final String content = user.getString("username")+"申请成为你的好友……";
            final String[] receivers = new String[]{user_id+""};
            new Thread(){
                public void run(){
                    Notify.dao.notify(user_id+"", NotifyType.apply_friend, "");
                    PushMessage.dao.push(receivers, content, true, 3);
                }
            }.start();
            if(r){
                rest.success("操作成功！");
            }else{
                rest.error("操作失败！");
            }
        }else{
            rest.error("请不要重复申请！").setCode(-2);
        }
        renderJson(rest);
    }

    /**
     * 好友确认
     */
    @Before(Tx.class)
    public void confirmFriend(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        int op_type = getParaToInt("op_type", 1);
        final int user_id = getParaToInt("user_id");
        String content = "";

        r = ContactRelation.dao.findById(getPara("data_id"))
                .set("confirmed", op_type).set("confirm_time", DateUtils.getTimeStamp()).update();

        //查重
        if(Db.findFirst("select id from contact_friend where user_id=? and friend_id=?"
                ,user.get("user_id"), user_id) == null){
            //增加相互关系
            if(op_type == 1){
                //已有关系检查
                r = new ContactFriend().set("user_id", user.get("user_id")).set("friend_id", user_id)
                        .set("created_time", DateUtils.getTimeStamp()).save();
                r = new ContactFriend().set("user_id", user_id).set("friend_id", user.get("user_id"))
                        .set("created_time", DateUtils.getTimeStamp()).save();
                //组装推送
                content = user.getString("username")+"同意了你的好友申请……";
            }else{
                content = user.getString("username")+"拒绝了你的好友申请……";
            }

            final String[] receivers = new String[]{user_id+""};
            final String cc = content;
            new Thread(){
                public void run(){
                Notify.dao.notify(user_id + "", NotifyType.confirm_friend, "");
                PushMessage.dao.push(receivers, cc, true, 3);
                }
            }.start();
            if(r){
                rest.success("操作成功！");
            }else{
                rest.error("操作失败！");
            }
        }else{
            rest.error("请不要重复操作！").setCode(-2);
        }
        renderJson(rest);
    }

    /**
     * 好友备注
     */
    @Before(Tx.class)
    public void setFriendLabel(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        int user_id = getParaToInt("user_id");

        String sql = "update contact_friend set label=? where user_id=? and friend_id=?";
        r = Db.update(sql, getPara("label"), user.get("user_id"), user_id)>0? true : false;
        if(r){
            rest.success("操作成功！");
        }else{
            rest.error("操作失败！");
        }
        renderJson(rest);
    }

    @Clear(AppLoginInterceptor.class)
    public void getUserPublicKey(){
        String loginName = getPara("login_name");
        String sql = "select ui.user_id,ul.login_name,ul.snnumber,ui.public_key\n" +
                "from user_login ul\n" +
                "inner join user_info ui on ui.user_id=ul.id\n" +
                "where ul.login_name =?";
        rest.success().setData(Db.findFirst(sql, loginName).get("public_key"));
        renderJson(rest);
    }

    /**
     * 搜索用户，支持 login_name| username | snnumber
     */
    public void searchUser(){
        JSONObject user = getCurrentUser(getPara("token"));
        String key = getPara("key");
        String sql = "select ui.user_id,ul.login_name,ul.snnumber,ui.username,ui.sex,ui.avatar_url\n" +
                "from user_login ul\n" +
                "inner join user_info ui on ui.user_id=ul.id\n" +
                "where ul.snnumber like '%"+key+"%' or ul.login_name like '"+key+"' or ui.username like '"+key+"'";
        data.put(ResponseCode.LIST, Db.find(sql));
        rest.success().setData(data);
        renderJson(rest);
    }

    /**
     * 获取好友申请列表
     */
    public void getFriendApplyList(){
        JSONObject user = getCurrentUser(getPara("token"));

        String sql = "select cr.id, cr.apply_user_id,ui.username,ui.sex,ui.avatar_url,cr.apply_msg \n" +
                "from contact_relation cr\n" +
                "left join user_info ui on ui.user_id=cr.apply_user_id\n" +
                "where cr.confirmed=0 and cr.confirm_user_id=?";

        data.put(ResponseCode.LIST, Db.find(sql, user.get("user_id")));
        rest.success().setData(data);
        renderJson(rest);
    }

    /**
     * 获取关注列表
     */
    public void getLikeList(){
        JSONObject user = getCurrentUser(getPara("token"));

        String sqlSelect = "select cl.id,cl.iuser_id,ul.login_name account,uf.username,uf.avatar_url,uf.introduction";
        String sqlFrom = "from contact_like cl\n" +
                "left join user_login ul on ul.id=cl.iuser_id\n" +
                "left join user_info uf on uf.user_id=ul.id\n" +
                "where cl.user_id=?\n";

        sqlFrom +=" order by cl.created_time DESC";

        if (StringTool.notNull(getPara("pageIndex")) && StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> dataList = Db.paginate(getParaToInt("pageIndex")+1, getParaToInt("pageSize",10)
                    , sqlSelect, sqlFrom, user.get("user_id"));
            rest.success().setData(dataList);
        }else{
            data.put(ResponseCode.LIST, Db.find(sqlSelect+" \n"+sqlFrom, user.get("user_id")));
            rest.success().setData(data);
        }
        renderJson(rest);
    }

    /**
     * 获取粉丝列表
     */
    public void getFansList(){
        JSONObject user = getCurrentUser(getPara("token"));

        String sqlSelect = "select cl.id,cl.iuser_id,ul.login_name account,uf.username,uf.avatar_url,uf.introduction";
        String sqlFrom = "from contact_like cl\n" +
                "left join user_login ul on ul.id=cl.user_id\n" +
                "left join user_info uf on uf.user_id=ul.id\n" +
                "where cl.iuser_id=?\n";

        sqlFrom +=" order by cl.created_time DESC";

        if (StringTool.notNull(getPara("pageIndex")) && StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> dataList = Db.paginate(getParaToInt("pageIndex")+1, getParaToInt("pageSize",10)
                    , sqlSelect, sqlFrom, user.get("user_id"));
            rest.success().setData(dataList);
        }else{
            data.put(ResponseCode.LIST, Db.find(sqlSelect+" \n"+sqlFrom, user.get("user_id")));
            rest.success().setData(data);
        }
        renderJson(rest);
    }

    /**
     * 加关注
     */
    @Before(Tx.class)
    public void addUserLike(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        int user_id = getParaToInt("user_id");
        //判断是否已关注
        if(Db.findFirst("select * from contact_like where user_id=? and iuser_id=?", user.get("user_id"), user_id) == null){
            r = new ContactLike().set("user_id", user.get("user_id")).set("iuser_id", user_id)
                    .set("created_time", DateUtils.getTimeStamp()).save();
            if(r){
                rest.success("操作成功！");
            }else{
                rest.error("操作失败！");
            }
        }else{
            rest.error("已关注！").setCode(-2);
        }
        renderJson(rest);
    }

    /**
     * 取消关注
     */
    @Before(Tx.class)
    public void removeUserLike(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        int user_id = getParaToInt("user_id");
        r = Db.update("delete from contact_like where user_id=? and iuser_id=?", user.get("user_id"), user_id)>0? true: false;
        if(r){
            rest.success("操作成功！");
        }else{
            rest.error("操作失败！");
        }
        renderJson(rest);
    }


}
