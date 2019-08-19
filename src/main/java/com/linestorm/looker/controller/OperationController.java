package com.linestorm.looker.controller;


import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.activerecord.Record;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.common.AdminBaseController;
import com.linestorm.looker.extend.ResponseCode;
import com.sagacity.utility.StringTool;
import net.sf.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

@ControllerBind(controllerKey = "/operation")
public class OperationController extends AdminBaseController {
	
	@Override
	public void index() {
	}

    /**
     * 获取信息推送列表
     */
	public void messagePushNav(){
		JSONObject userInfo = getCurrentUser();

		String searchKey = getPara("key");
		String sqlSelect = "select m.MessageID,o.OrgName,u.Caption,m.Content,m.AddTime,count(i.indexID) pCount"
				+ ",m.blnSMS,m.FixedTime,m.ExecuteTime";
		String sqlFrom = "from message_master m \n"
				+"left join message_index i on i.MessageID=m.MessageID \n"
				+"left join sys_users u on u.UserID=m.UserID \n"
				+"left join org_baseInfo o on o.OrgID=m.OrgID \n";
		if (userInfo.getInt("RoleID") == 1){ //平台管理员
			sqlFrom += "where 1=1";
		}else{
			sqlFrom += "where m.OrgID='"+userInfo.get("OrgID")+"'";
		}
		if (StringTool.notNull(searchKey) && !StringTool.isBlank(searchKey)) {
			sqlFrom += " and m.Content like '%" + searchKey + "%'";
		}
		sqlFrom +=" group by m.MessageID order by m.AddTime Desc";
		Page<Record> messages = Db.paginate(getParaToInt("pageIndex") + 1,
                getParaToInt("pageSize"), sqlSelect, sqlFrom);
		renderJson(messages);
	}

    /**
     * 获取在线用户列表
     */
    public void getOnlineUser(){
        String cacheName = "";
        int cacheIndex = getParaToInt("cacheIndex");

        switch (cacheIndex){
            case 1 :
                cacheName = "AdminCache";
                break;
            case 2 :
                cacheName = "UserCache";
                break;
        }

        int pageSize = getParaToInt("pageSize",20);
        List<Record> r = new ArrayList();

        List<String> keys = CacheKit.getKeys(cacheName);
        for(String key : keys){
            Record record = new Record().set("id", key);
            try{
                JSONObject jo = CacheKit.get(cacheName, key);
                record.set("AddTime", jo.get("AddTime"));
                record.set("UserID", jo.getJSONObject("UserInfo").get("UserID"));
                record.set("LoginName", jo.getJSONObject("UserInfo").get("LoginName"));
                record.set("Caption", jo.getJSONObject("UserInfo").get("Caption"));
                record.set("DeviceInfo", jo.get("DeviceInfo"));
            }catch (Exception ex){
                ex.printStackTrace();
            }
            r.add(record);
        }

        if (StringTool.notNull(getPara("pageIndex")) && StringTool.notBlank(getPara("pageIndex"))){
            responseData.put(ResponseCode.LIST, r.subList((getParaToInt("pageIndex")) * pageSize,
                    (getParaToInt("pageIndex")+1) * pageSize > r.size() ? r.size() : (getParaToInt("pageIndex")+1) * pageSize));
            responseData.put(ResponseCode.TotalPage, Math.ceil((double)r.size() / pageSize));
            responseData.put(ResponseCode.PageIndex, getParaToInt("pageIndex"));
            responseData.put(ResponseCode.TotalRow, r.size());
            renderJson(responseData);
        }else{
            renderJson(ResponseCode.LIST, r);
        }
    }

    /**
     * 踢出在线用户
     */
    public void kickUser(){

        String cacheName = "";
        String key = getPara("key");

        switch (getParaToInt("cacheIndex")){
            case 1 :
                cacheName = "AdminCache";
                break;
            case 2 :
                cacheName = "UserCache";
                break;
        }
        CacheKit.remove(cacheName, key);
        responseData.put(ResponseCode.RESULT, true);
        responseData.put(ResponseCode.MSG, "在线用户已踢出！");

        renderJson(responseData);
    }
	
}
