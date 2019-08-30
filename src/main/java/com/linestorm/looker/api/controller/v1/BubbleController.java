package com.linestorm.looker.api.controller.v1;

import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.linestorm.looker.api.common.UserBaseController;
import com.linestorm.looker.model.bubble.BubbleInfo;
import com.linestorm.looker.model.bubble.BubblePost;
import com.linestorm.looker.model.bubble.BubbleUser;
import com.linestorm.looker.model.bubble.PostVote;
import com.linestorm.looker.extend.NotifyType;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.service.message.Notify;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.FileUtil;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONObject;

@ControllerBind(controllerKey = "/api/bubble")
public class BubbleController extends UserBaseController{

    @Override
    public void index(){

    }

    /**
     * 创建气泡
     */
    public void createBubble(){
        boolean r = false;
        int interval = 24;

        JSONObject userInfo = getCurrentUser(getPara("token"));
        int blnAnonymous = getParaToInt("blnAnonymous", 0); //匿名
        String desc = getPara("desc");


        BubbleInfo bi = new BubbleInfo().set("blnAnonymous", blnAnonymous).set("desc", desc)
                .set("user_id", userInfo.get("user_id")).set("interval", interval)
                .set("longtitude", getPara("longtitude")).set("latitude", getPara("latitude"))
                .set("deleted", 0).set("created_time", DateUtils.getTimeStamp());
        r = bi.save();
        //创建人自动加入
        r = new BubbleUser().set("bubble_id", bi.get("id")).set("user_id", userInfo.get("user_id"))
                .set("created_time", DateUtils.getTimeStamp()).save();

        if(r){
            responseData.put(ResponseCode.MSG, "创建成功！");
        }else{
            responseData.put(ResponseCode.MSG, "创建失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }

    /**
     * 动态获得泡泡
     */
    public void getBubble(){
        JSONObject userInfo = getCurrentUser(getPara("token"));
        String longtitude = getPara("longtitude");
        String latitude = getPara("latitude");

        String sql = "select bi.id,bi.latitude,bi.longtitude,bi.desc,ui.avatar_url,ui.username,ui.user_id \n" +
                "from bubble_baseInfo bi\n" +
                "left join user_info ui on ui.user_id=bi.user_id\n" +
                "where bi.deleted=0 order by bi.created_time DESC limit 20";
        responseData.put(ResponseCode.LIST, Db.find(sql));
        responseData.put(ResponseCode.CODE, 1);
        renderJson(responseData);
    }

    /**
     * 气泡聊天主界面(指定泡泡和已有列表)
     */
    public void bubbleMain(){
        JSONObject userInfo = getCurrentUser(getPara("token"));
        int bubbleID = getParaToInt("bubbleID"); //指定的bubbleID

        //当前气泡
        String sql_base = "select bi.id,bi.desc,bi.blnAnonymous,bi.latitude,bi.longtitude,bi.interval,bi.created_time,ui.avatar_url,ui.username,ui.user_id,IFNULL(bui.uCount,0) uCount\n" +
                "from bubble_baseInfo bi\n" +
                "left join user_info ui on ui.user_id=bi.user_id\n" +
                "left join (select count(id) uCount, bubble_id from bubble_userindex group by bubble_id) bui on bui.bubble_id=bi.id\n";
        String sql_info = sql_base + "where bi.id=?";
        responseData.put(ResponseCode.DATA, Db.findFirst(sql_info, bubbleID));
        //气泡列表
        String sql_list = sql_base + "inner join bubble_userIndex bu on bu.bubble_id=bi.id\n where bi.deleted=0 and bu.user_id=?";

        responseData.put(ResponseCode.LIST, Db.find(sql_list, userInfo.get("user_id")));
        responseData.put("timeStamp", DateUtils.getTimeStamp());
        responseData.put(ResponseCode.CODE, 1);
        renderJson(responseData);
    }

    /**
     * 加入气泡 0-结束；1-成功；-2-未上传形象
     */
    public void joinBubble(){
        boolean r = false;
        JSONObject userInfo = getCurrentUser(getPara("token"));
        int bubbleID = getParaToInt("bubbleID"); //指定的bubbleID
        String longtitude = getPara("longtitude");
        String latitude = getPara("latitude");

        if(BubbleInfo.dao.findById(bubbleID).getInt("deleted") == 1) {
            responseData.put(ResponseCode.CODE, 0);
            responseData.put(ResponseCode.MSG, "气泡已结束！");
        }else if(Db.findFirst("select * from bubble_userInfo where user_id=?", userInfo.get("user_id")) == null){
            responseData.put(ResponseCode.CODE, -2);
            responseData.put(ResponseCode.MSG, "未上传个人形象！");
        }else {
            if(Db.findFirst("select * from bubble_userIndex where user_id=? and bubble_id=?", userInfo.get("user_id"), bubbleID) == null){
                r = new BubbleUser().set("bubble_id", bubbleID).set("user_id", userInfo.get("user_id"))
                        .set("created_time", DateUtils.getTimeStamp()).save();
            }
            responseData.put(ResponseCode.CODE, 1);
            responseData.put(ResponseCode.MSG, "加入成功！");
        }
        renderJson(responseData);
    }

    /**
     * 上传泡泡区形象图片
     */
    public void uploadPhoto(){
        JSONObject user = getCurrentUser(getPara("token"));
        String photoUrl = getPara("photoUrl");

        Db.update("update bubble_userInfo set state=0 where user_id=?", user.get("user_id"));
        boolean r = Db.save("bubble_userInfo", new Record().set("user_id", user.get("user_id"))
            .set("photo_url", photoUrl).set("created_time", DateUtils.getTimeStamp()).set("state",1));

        if(r){
            responseData.put(ResponseCode.MSG, "操作成功！");
        }else{
            responseData.put(ResponseCode.MSG, "操作失败！");
        }
        responseData.put(ResponseCode.CODE, r?1:0);
        renderJson(responseData);
    }

    /**
     * 发帖
     */
    public void addPost(){
        boolean r = false;
        JSONObject userInfo = getCurrentUser(getPara("token"));
        int bubbleID = getParaToInt("bubbleID"); //指定的bubbleID

        String content = getPara("content");
        String attach_url = getPara("attach_url");

        BubblePost bp = new BubblePost().set("bubble_id", bubbleID).set("content", content)
                .set("user_id", userInfo.get("user_id")).set("like_count", 0).set("deleted", 0)
                .set("attach_url", attach_url).set("attach_type", FileUtil.getMimeTypeFromUrl(attach_url))
                .set("attach_info", getPara("attach_info")).set("created_time", DateUtils.getTimeStamp());
        r = bp.save();

        if(r){
            responseData.put(ResponseCode.MSG, "操作成功！");
        }else{
            responseData.put(ResponseCode.MSG, "操作失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }


    /**
     * 获得帖子列表
     */
    public void getPostList(){
        JSONObject userInfo = getCurrentUser(getPara("token"));
        int bubbleID = getParaToInt("bubbleID"); //指定的bubbleID

        String sqlSelect = "select ui.username,ui.avatar_url,ui.sex,bp.*\n";
        String sqlFrom = "from bubble_post bp\n" +
                "        inner join user_info ui on ui.user_id=bp.user_id\n";
        sqlFrom += " and bp.deleted=0 and bp.bubble_id=?";
        sqlFrom += " order by bp.created_time DESC";

        if (StringTool.notNull(getPara("pageIndex"))&&StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> list = Db.paginate(getParaToInt("pageIndex")+1, getParaToInt("pageSize",10), sqlSelect, sqlFrom, bubbleID);
            renderJson(convertPageData(list));
        }else{
            responseData.put(ResponseCode.CODE, 1);
            responseData.put(ResponseCode.LIST, Db.find(sqlSelect+" \n"+sqlFrom, bubbleID));
            renderJson(responseData);
        }
    }

    /**
     * 获得点赞列表
     */
    public void getFavorList(){
        JSONObject userInfo = getCurrentUser(getPara("token"));
        int bubbleID = getParaToInt("bubbleID"); //指定的bubbleID

        String sqlSelect = "select ui.user_id,ui.username,ui.avatar_url,ui.sex,bui.photo_url,sum(bpv.voteCount) voteCount";
        String sqlFrom = "from bubble_post bp\n" +
                "left join (\n" +
                "select count(bv.id) voteCount,bv.user_id,bv.post_id \n" +
                "from bubble_postVote bv\n" +
                "group by bv.post_id,bv.user_id) bpv on bp.id=bpv.post_id\n" +
                "inner join user_info ui on ui.user_id=bpv.user_id\n" +
                "left join bubble_userInfo bui on bui.state=1 and bui.user_id=bpv.user_id\n" +
                "where bp.bubble_id=? and bp.user_id=?\n" +
                "group by bpv.user_id";

        if (StringTool.notNull(getPara("pageIndex"))&&StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> list = Db.paginate(getParaToInt("pageIndex")+1, getParaToInt("pageSize",10), sqlSelect, sqlFrom, bubbleID, userInfo.get("user_id"));
            renderJson(convertPageData(list));
        }else{
            responseData.put(ResponseCode.CODE, 1);
            responseData.put(ResponseCode.LIST, Db.find(sqlSelect+" \n"+sqlFrom, bubbleID, userInfo.get("user_id")));
            renderJson(responseData);
        }
    }

    /**
     * 帖子点赞
     */
    public void addPostFavor(){
        boolean r = false;

        JSONObject user = getCurrentUser(getPara("token"));
        int post_id = getParaToInt("post_id", 0);
        int vote = getParaToInt("vote", 1);

        PostVote pv  = new PostVote().set("post_id", post_id).set("user_id", user.get("user_id"))
                .set("vote", vote).set("created_time", DateUtils.getTimeStamp());
        r = pv.save();
        BubblePost bp = BubblePost.dao.findById(post_id);
        r = bp.set("like_count", bp.getInt("like_count")+vote).update();
        //在泡泡上显示变化
        Notify.dao.notify(user.getString("user_id"), bp.getStr("user_id"), NotifyType.bubble_notify, bp.getStr("bubble_id"));
        if(r){
            responseData.put(ResponseCode.MSG, "点赞成功！");
        }else{
            responseData.put(ResponseCode.MSG, "点赞失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }


}
