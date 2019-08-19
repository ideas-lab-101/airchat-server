package com.linestorm.looker.api.controller.v1;

import com.jfinal.aop.Before;
import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.linestorm.looker.api.common.UserBaseController;
import com.linestorm.looker.model.contact.ContactFriend;
import com.linestorm.looker.extend.NotifyType;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.service.message.Notify;
import com.linestorm.looker.service.message.PushMessage;
import com.linestorm.looker.model.topic.*;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.FileUtil;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONArray;
import net.sf.json.JSONObject;

import java.util.List;
import java.util.Map;

/**
 * Created by mulaliu on 16/6/23.
 */

@ControllerBind(controllerKey = "/api/topic")
public class TopicController extends UserBaseController {

    @Override
    public void index(){

    }

    /**
     * 获得个人的zone
     */
    public void getOwnZone(){

        JSONObject userInfo = getCurrentUser(getPara("token"));
        responseData.put("userInfo", Db.findFirst(SqlKit.sql("user.getUserInfo"), userInfo.get("user_id")));

        String sqlSelect = SqlKit.sql("topic.getTopicList-select");
        String sqlFrom = SqlKit.sql("topic.getTopicList-from");
        sqlFrom += " where deleted=0 and open_setting!='autoDelete'";
        sqlFrom += " order by zone.created_time DESC";
        List<Record> rList = null;
        if (StringTool.notNull(getPara("pageIndex"))&&StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> recordPage = Db.paginate(getParaToInt("pageIndex") + 1, getParaToInt("pageSize", 10),
                    sqlSelect, sqlFrom, userInfo.get("user_id"), userInfo.get("user_id"));
            rList = recordPage.getList();
            responseData.put(ResponseCode.TotalPage, recordPage.getTotalPage());
            responseData.put(ResponseCode.PageNumber, recordPage.getPageNumber());
        }else{
            rList = Db.find(sqlSelect+" \n"+sqlFrom);
        }
        //其它信息
        String sqlLike = "select * from topic_userVote \n" +
                "tv where tv.user_id=? and tv.topic_id=?";
        for (Record rs : rList) {
            rs.set("blnLike", Db.findFirst(sqlLike, userInfo.get("user_id"), rs.get("id")) == null? 0:1);
            rs.set("attachList", Db.find("select id,attach_type,attach_info,attach_url from topic_attachment where topic_id=?", rs.get("id")));
        }
        responseData.put(ResponseCode.CODE, 1);
        responseData.put(ResponseCode.LIST, rList);
        renderJson(responseData);
    }

    /**
     * 获得指定用户的zone
     */
    public void getUserZone(){

        JSONObject userInfo = getCurrentUser(getPara("token"));
        String user_id = getPara("user_id");
        responseData.put("userInfo", Db.findFirst(SqlKit.sql("user.getUserInfo"), user_id));

        //判断是否为好友关系
        if(ContactFriend.dao.blnFriend(userInfo.getString("user_id"), user_id)){
            String sqlSelect = SqlKit.sql("topic.getTopicList-select");
            String sqlFrom = SqlKit.sql("topic.getTopicList-from");
            sqlFrom += " where deleted=0 and zone.user_id= "+ user_id +" order by zone.created_time DESC";
            List<Record> rList = null;
            if (StringTool.notNull(getPara("pageIndex"))&&StringTool.notBlank(getPara("pageIndex"))){
                Page<Record> recordPage = Db.paginate(getParaToInt("pageIndex") + 1, getParaToInt("pageSize", 10),
                        sqlSelect, sqlFrom, user_id, user_id);
                rList = recordPage.getList();
                responseData.put(ResponseCode.TotalPage, recordPage.getTotalPage());
                responseData.put(ResponseCode.PageNumber, recordPage.getPageNumber());
            }else{
                rList = Db.find(sqlSelect+" \n"+sqlFrom);
            }
            //其它信息
            String sqlLike = "select * from topic_userVote \n" +
                    "tv where tv.user_id=? and tv.topic_id=?";
            for (Record rs : rList) {
                rs.set("blnLike", Db.findFirst(sqlLike, user_id, rs.get("id")) == null? 0:1);
                rs.set("attachList", Db.find("select id,attach_type,attach_info,attach_url from topic_attachment where topic_id=?", rs.get("id")));
            }
            responseData.put(ResponseCode.CODE, 1);
            responseData.put(ResponseCode.LIST, rList);
        }else{
            responseData.put(ResponseCode.CODE, 0);
            responseData.put(ResponseCode.MSG, "非好友关系");
        }
        renderJson(responseData);
    }

    @Before(Tx.class)
    public void userLike(){
        boolean r = false;

        JSONObject userInfo = getCurrentUser(getPara("token"));
        int topic_id = getParaToInt("topic_id", 0);
        int vote = getParaToInt("vote", 0);

        if(Db.find("select * from topic_userVote where topic_id=? and user_id=?", topic_id, userInfo.get("user_id")).size()>0){
            responseData.put(ResponseCode.MSG, "你已经点赞！");
        }else {
            TopicVote tv  = new TopicVote().set("topic_id", topic_id).set("user_id", userInfo.get("user_id"))
                    .set("longtitude", getPara("longtitude")).set("latitude", getPara("latitude")).set("vote", vote).set("created_time", DateUtils.getTimeStamp());
            r = tv.save();
            TopicInfo ti = TopicInfo.dao.findById(topic_id);
            r = ti.set("like_count", ti.getInt("like_count")+1).update();
            //写入与我相关
            String receiverID = ti.getInt("user_id")+"";
            String content = userInfo.get("username")+"点赞了你的话题";
            r = new TopicUserNotice().set("topic_id", topic_id).set("user_id", userInfo.get("user_id")).set("notice_id", receiverID)
                    .set("data_id", tv.get("id")).set("data_type", "vote").set("notice_content", content)
                    .set("created_time", DateUtils.getTimeStamp()).save();
            Notify.dao.notify(receiverID, NotifyType.new_comment, "");
            if(r){
                responseData.put(ResponseCode.MSG, "点赞成功！");
            }else{
                responseData.put(ResponseCode.MSG, "点赞失败！");
            }
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);

    }

    public void getTopicLikeList(){
        int topic_id = getParaToInt("topic_id");

        String sql = "select fv.latitude,fv.longtitude,ui.username,ui.avatar_url \n" +
                "from topic_uservote fv\n" +
                "left join user_info ui on ui.user_id=fv.user_id\n" +
                "where fv.topic_id=?";
        responseData.put(ResponseCode.CODE, 1);
        responseData.put(ResponseCode.LIST, Db.find(sql, topic_id));
        renderJson(responseData);
    }

    public void getCommentList(){
        JSONObject userInfo = getCurrentUser(getPara("token"));

        int topic_id = getParaToInt("topic_id");
        String sqlSelect = "select tc.id,u.user_id,u.username,u.avatar_url\n" +
                ",tc.refer_id,tcr.user_id refer_user_id,ru.username refer_user_name\n" +
                ",tc.text_comment,tc.created_time";
        String sqlFrom = "from topic_comment tc\n" +
                "left join user_info u on u.user_id=tc.user_id\n" +
                "left join topic_comment tcr on tcr.id=tc.refer_id\n" +
                "left join user_info ru on ru.user_id=tcr.user_id\n" +
                "where tc.deleted=0 and tc.topic_id=?";
        sqlFrom += " order by tc.created_time DESC";
        if (StringTool.notNull(getPara("pageIndex"))&&StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> list = Db.paginate(getParaToInt("pageIndex")+1, getParaToInt("pageSize",10), sqlSelect, sqlFrom, topic_id);
            renderJson(convertPageData(list));
        }else{
            responseData.put(ResponseCode.CODE, 1);
            responseData.put(ResponseCode.LIST, Db.find(sqlSelect+" \n"+sqlFrom, topic_id));
            renderJson(responseData);
        }
    }

    @Before(Tx.class)
    public void addComment(){
        int topic_id = getParaToInt("topic_id");
        int refer_id = getParaToInt("refer_id", 0); //直接针对topic的回复
        JSONObject userInfo = getCurrentUser(getPara("token"));
        String text_comment = getPara("text_comment");
        boolean r = false;

        TopicComment tc  = new TopicComment().set("refer_id", refer_id).set("topic_id",topic_id).set("user_id",userInfo.get("user_id"))
                .set("text_comment", text_comment).set("is_readed",0).set("deleted", 0)
                .set("created_time", DateUtils.getTimeStamp());
        r = tc.save();
        TopicInfo ti = TopicInfo.dao.findById(topic_id);
        r = ti.set("comment_count", ti.getInt("comment_count")+1).update();

        //组装@我相关的数据
        String content = "", data_type = "", receiverID = "";
        if(refer_id == 0){ //话题回复
            content = userInfo.get("username")+"评论了你的话题……";
            data_type = "comment";
            receiverID = ti.getInt("user_id")+"";
        }else { //对回复的回复
            content = userInfo.get("username")+"回复了你的评论……";
            data_type = "reply";
            TopicComment tm = TopicComment.dao.findFirst("select * from topic_comment where id=?", refer_id);
            receiverID = tm.getInt("user_id")+"";
        }
        //写入与我相关
        r = new TopicUserNotice().set("topic_id", topic_id).set("user_id", userInfo.get("user_id")).set("notice_id", receiverID)
                .set("data_id", tc.get("id")).set("data_type", data_type).set("notice_content", text_comment)
                .set("created_time", DateUtils.getTimeStamp()).save();
        //组装推送数据
        String[] receivers = new String[]{receiverID};
        Notify.dao.notify(receiverID, NotifyType.new_comment, "");
        PushMessage.dao.push(receivers, content, true, 2);
        if(r){
            responseData.put(ResponseCode.MSG, "评论成功！");
        }else{
            responseData.put(ResponseCode.MSG, "评论失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }

    /**
     * 不做物理删除(否则有refer会出错)；仅设置删除标志
     */
    @Before(Tx.class)
    public void delComment(){
        boolean r = TopicComment.dao.findById(getPara("comment_id")).set("deleted", 1).update();

        if(r){
            responseData.put(ResponseCode.MSG, "操作成功！");
        }else{
            responseData.put(ResponseCode.MSG, "操作失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);

    }


    @Before(Tx.class)
    public void addTopic(){
        boolean r = false;

        JSONObject userInfo = getCurrentUser(getPara("token"));
        String content = getPara("content");

        TopicInfo tp = new TopicInfo().set("open_setting", getPara("open_setting")).set("content", content)
                .set("user_id", userInfo.get("user_id")).set("tags", getPara("tags"))
                .set("longtitude", getPara("longtitude")).set("latitude", getPara("latitude"))
                .set("read_count", 0).set("like_count", 0).set("comment_count", 0).set("deleted", 0)
                .set("created_time", DateUtils.getTimeStamp());
        r = tp.save();
        //位置信息

        JSONArray attachList = JSONArray.fromObject(getPara("attachList"));
        //处理附件数据
        for (int i=0;i<attachList.size();i++){
            Map o = (Map) attachList.get(i);
            String attach_url = o.get("attach_url")+"";
            r= new TopicAttach().set("topic_id", tp.get("id")).set("attach_info", o.get("attach_info").toString())
                    .set("attach_type", FileUtil.getMimeTypeFromUrl(attach_url)).set("attach_url", attach_url)
                    .set("created_time", DateUtils.getTimeStamp()).save();
        }
        if(r){
            responseData.put(ResponseCode.MSG, "操作成功！");
        }else{
            responseData.put(ResponseCode.MSG, "操作失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }

    @Before(Tx.class)
    public void delTopic(){
        boolean r = TopicInfo.dao.findById(getPara("topic_id"))
                .set("deleted", 1).set("end_time", DateUtils.getTimeStamp()).update();

        if(r){
            responseData.put(ResponseCode.MSG, "操作成功！");
        }else{
            responseData.put(ResponseCode.MSG, "操作失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }

    public void getUserNotice(){
        JSONObject user = getCurrentUser(getPara("token"));

        String sqlSelect = "select ut.notice_content,ut.data_id,ut.data_type,ut.created_time,ut.user_id user_id1,uff.username username1,uff.avatar_url avatar_url1,cff.label label1\n" +
                ",tb.id,tb.user_id,tb.content,tb.tags,tb.read_count,tb.comment_count,tb.like_count,uf.username,uf.avatar_url,cf.label";
        String sqlFrom = "from topic_userNotice ut\n" +
                "left join user_login ull on ull.id=ut.user_id\n" +
                "left join user_info uff on uff.user_id=ull.id\n" +
                "left join contact_friend cff on cff.user_id=ut.notice_id and cff.friend_id=ull.id\n" +
                "left join topic_baseInfo tb on tb.id=ut.topic_id\n" +
                "left join user_login ul on ul.id=tb.user_id\n" +
                "left join user_info uf on uf.user_id=ul.id\n" +
                "left join contact_friend cf on cf.user_id=tb.user_id and cf.friend_id=ul.id\n" +
                "where ut.notice_id=?";

        sqlFrom +=" order by ut.created_time DESC";

        List<Record> rList = null;
        if (StringTool.notNull(getPara("pageIndex")) && StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> recordPage = Db.paginate(getParaToInt("pageIndex") + 1, getParaToInt("pageSize", 10),
                    sqlSelect, sqlFrom, user.get("user_id"));
            rList = recordPage.getList();
            responseData.put(ResponseCode.TotalPage, recordPage.getTotalPage());
            responseData.put(ResponseCode.PageNumber, recordPage.getPageNumber());
        }else{
            rList = Db.find(sqlSelect+" \n"+sqlFrom, user.get("user_id"));
        }
        //其它信息
        String sqlLike = "select * from topic_userVote \n" +
                "tv where tv.user_id=? and tv.topic_id=?";
        for (Record rs : rList) {
            rs.set("blnLike", Db.findFirst(sqlLike, user.get("user_id"), rs.get("id")) == null? 0:1);
            rs.set("attachList", Db.find("select id,attach_type,attach_info,attach_url from topic_attachment where topic_id=?", rs.get("id")));
        }
        responseData.put(ResponseCode.CODE, 1);
        responseData.put(ResponseCode.LIST, rList);
        renderJson(responseData);
    }


    @Before(Tx.class)
    public void delUserNotice(){

    }

}
