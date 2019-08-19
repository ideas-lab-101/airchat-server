package com.linestorm.looker.api.controller.v1;

import com.jfinal.aop.Before;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.activerecord.tx.Tx;
import com.linestorm.looker.api.common.UserBaseController;
import com.linestorm.looker.model.contact.ZoneGuest;
import com.linestorm.looker.extend.ResponseCode;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONObject;

import java.util.List;

@ControllerBind(controllerKey = "/api/zone")
public class ZoneController extends UserBaseController{

    @Override
    public void index(){

    }

    /**
     * 获得世界的瀑布流数据
     */
    public void getOpenZone(){
        //判断当前用户是否登录
        JSONObject userInfo = getCurrentUser(getPara("token"));

        String sqlSelect = "select distinct ui.username,ui.avatar_url,ui.username label,tb.*";
        String sqlFrom = "from topic_baseInfo tb\n" +
                "        inner join user_info ui on ui.user_id=tb.user_id";
        sqlFrom += " where tb.deleted=0 and DATEDIFF(now(),FROM_UNIXTIME(tb.created_time))<=1 and (tb.open_setting='public' or tb.open_setting='autoDelete')";
        //搜索
        if(StringTool.notNull(getPara("key"))&&StringTool.notBlank(getPara("key"))){
            sqlFrom += " and (tags like '%"+getPara("key")+"%' or content like '%"+getPara("key")+"%')";
        }
        //范围
        if(StringTool.notNull(getPara("topLeft"))&&StringTool.notBlank(getPara("topLeft"))){
            JSONObject topLeft = JSONObject.fromObject(getPara("topLeft"));
            JSONObject bottomRight = JSONObject.fromObject(getPara("bottomRight"));
            sqlFrom += " and (tb.longtitude>"+ bottomRight.getString("longtitude")+" and tb.longtitude<"+ topLeft.getString("longtitude")+")";
            sqlFrom += " and (tb.latitude>"+ bottomRight.getString("latitude")+" and tb.latitude<"+ topLeft.getString("latitude")+")";
        }
        sqlFrom += " order by tb.created_time DESC";
        List<Record> rList = null;
        if (StringTool.notNull(getPara("pageIndex"))&&StringTool.notBlank(getPara("pageIndex"))){
            Page<Record> recordPage = Db.paginate(getParaToInt("pageIndex") + 1, getParaToInt("pageSize", 10),
                    sqlSelect, sqlFrom);
            rList = recordPage.getList();
            responseData.put(ResponseCode.TotalPage, recordPage.getTotalPage());
            responseData.put(ResponseCode.PageNumber, recordPage.getPageNumber());
        }else{
            rList = Db.find(sqlSelect+" \n"+sqlFrom);
        }
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

    @Before(Tx.class)
    public void addZoneGuest(){
        boolean r = false;
        JSONObject user = getCurrentUser(getPara("token"));
        int user_id = getParaToInt("user_id");

        r = new ZoneGuest().set("guest_id", user_id).set("user_id", user.get("user_id"))
                .set("created_time", DateUtils.getTimeStamp()).save();
        if(r){
            responseData.put(ResponseCode.MSG, "操作成功！");
        }else{
            responseData.put(ResponseCode.MSG, "操作失败！");
        }
        responseData.put(ResponseCode.CODE, r? 1:0);
        renderJson(responseData);
    }

    /**
     * 获取访客信息
     */
    public void getZoneGuestList(){
        JSONObject user = getCurrentUser(getPara("token"));

        String sqlSelect = "select max(zg.id) id,zg.guest_id,ul.login_name account,uf.username,uf.avatar_url,uf.introduction";
        String sqlFrom = "from zone_guest zg\n" +
                "left join user_login ul on ul.id=zg.guest_id\n" +
                "left join user_info uf on uf.user_id=ul.id\n" +
                "where zg.user_id=?\n";
        sqlFrom +=" group by zg.guest_id \n" +
                "order by zg.created_time DESC";

        if (StringTool.notNull(getPara("pageIndex")) && StringTool.notBlank(getPara("pageIndex"))){
            renderJson(convertPageData(Db.paginate(getParaToInt("pageIndex")+1, getParaToInt("pageSize",10)
                    , sqlSelect, sqlFrom, user.get("user_id"))));
        }else{
            responseData.put(ResponseCode.CODE, 1);
            responseData.put(ResponseCode.LIST, Db.find(sqlSelect+" \n"+sqlFrom, user.get("user_id")));
            renderJson(responseData);
        }
    }
}
