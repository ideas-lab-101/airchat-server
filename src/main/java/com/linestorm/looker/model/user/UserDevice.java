package com.linestorm.looker.model.user;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.sagacity.utility.DateUtils;
import net.sf.json.JSONObject;

@TableBind(tableName="user_device",pkName="id")
public class UserDevice extends Model<UserDevice> {
	private static final long serialVersionUID = 1L;
	public final static UserDevice dao = new UserDevice();

	public boolean registerDeviceInfo(String user_id, String deviceInfo){

		boolean r = false;
		try {
			JSONObject jo = JSONObject.fromObject(deviceInfo);

			//将当前用户所有设备信息更新为停用
			Db.update("update user_device set state=0 where user_id=?", user_id);
			UserDevice ud = UserDevice.dao.findFirst("select * from user_device where user_id=? and unique_id=?", user_id, jo.get("uniqueId"));
			if(ud != null){
				r=ud.set("os_type", jo.get("osType")).set("os_version", jo.get("osVersion")).set("state", 1)
						.set("token",jo.get("deviceToken")).set("updated_time", DateUtils.getTimeStamp()).update();
			}else{
				r= new UserDevice().set("user_id", user_id).set("unique_id",jo.get("uniqueId")).set("token", jo.get("deviceToken")).set("os_type", jo.get("osType"))
						.set("os_version", jo.get("osVersion")).set("state", 1).set("created_time", DateUtils.getTimeStamp()).save();
			}
		}catch (Exception ex){
			r = false;
		}
		return  r;
	}

	public boolean removeDeviceInfo(String user_id){
		boolean r = Db.update("update user_device set state=0 where user_id=?", user_id)>0 ? true : false;
		return r;
	}
}
