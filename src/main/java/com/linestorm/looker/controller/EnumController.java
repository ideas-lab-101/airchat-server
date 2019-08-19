package com.linestorm.looker.controller;

import com.jfinal.ext.plugin.sqlinxml.SqlKit;
import com.jfinal.ext.route.ControllerBind;
import com.jfinal.kit.PropKit;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.ehcache.CacheKit;
import com.jfinal.render.JsonRender;
import com.jfinal.upload.UploadFile;
import com.linestorm.looker.common.AdminBaseController;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.model.sys.EnumDetail;
import com.linestorm.looker.model.sys.EnumMaster;
import com.sagacity.utility.DateUtils;
import com.sagacity.utility.FileUtil;
import net.sf.json.JSONArray;

import java.io.File;
import java.util.List;
import java.util.Map;

@ControllerBind(controllerKey = "/enum")
public class EnumController extends AdminBaseController {
	
	@Override
	public void index() {

	}

    public void enumManage(){
        render("/system/enumManage.html");
    }

    public void getEnumMaster(){
        renderJson(Db.find("select * from sys_enumMaster em where em.intState=1"));
    }

    public void getEnumDetail(){
        int masterID = getParaToInt("masterID", 0);
        List<EnumDetail> ed = EnumDetail.dao.findByCache("SystemCache", "enum_"+ masterID,
                SqlKit.sql("enum.getEnumDetail"), masterID);
        renderJson(ed);
    }

    public void saveEnumMasterChange() {
        boolean r = false;

        JSONArray ja = JSONArray.fromObject(getPara("data"));
        for (int i=0;i<ja.size();i++){
            Map o=(Map)ja.get(i);
            switch (o.get("_state").toString()){
                case "added" :
                    r=new EnumMaster().set("Caption", o.get("Caption")).set("Code",o.get("Code"))
                            .set("AddTime", DateUtils.nowDateTime()).set("intState", 1).save();
                    break;
                case "removed" :
                    r = EnumMaster.dao.deleteById(o.get("MasterID"));
                    break;
                case "modified" :
                    r = EnumMaster.dao.findById(o.get("MasterID")).set("Caption",o.get("Caption"))
                            .set("Code",o.get("Code")).update();
                    break;
                default :
                    break;
            }
        }
        if(r){
            responseData.put(ResponseCode.RESULT, r);
            responseData.put(ResponseCode.MSG, "码表键更新成功！");
        }else{
            responseData.put(ResponseCode.RESULT, r);
            responseData.put(ResponseCode.MSG, "码表键更新失败！");
        }
        renderJson(responseData);
    }

    public void saveEnumDetailChange() {
        boolean r = false;

        JSONArray ja = JSONArray.fromObject(getPara("data"));
        for (int i=0;i<ja.size();i++){
            Map o=(Map)ja.get(i);
            switch (o.get("_state").toString()){
                case "added" :
                    r=new EnumDetail().set("MasterID",getPara("masterID")).set("PID",0)
                            .set("Caption", o.get("Caption")).set("Code",o.get("Code")).set("intState", 1).save();
                    break;
                case "removed" :
                    r = EnumDetail.dao.deleteById(o.get("DetailID"));
                    break;
                case "modified" :
                    r = EnumDetail.dao.findById(o.get("DetailID")).set("Caption",o.get("Caption"))
                            .set("Code",o.get("Code")).update();
                    break;
                default :
                    break;
            }
        }
        if(r){
            CacheKit.removeAll("SystemCache");
            responseData.put(ResponseCode.RESULT, r);
            responseData.put(ResponseCode.MSG, "码表值更新成功！");
        }else{
            responseData.put(ResponseCode.RESULT, r);
            responseData.put(ResponseCode.MSG, "码表值更新失败！");
        }
        renderJson(responseData);
    }

    public void uploadIcon(){

        String config_dir = PropKit.get("resource.dir");
        boolean r= true;

        File f1 = new File(config_dir+"/imgTemp/");
        if (!f1.exists()) {
            f1.mkdirs();
        }
        UploadFile uploadFile = getFile("iconFile", f1.getAbsolutePath());
        File f2 = new File(config_dir+ "/enum/"+getPara("masterID")+"/");
        if (!f2.exists()) {
            f2.mkdirs();
        }
        File nFile = uploadFile.getFile();
        File iconFile = null;
        if (nFile!=null) {
            iconFile = FileUtil.copyFile(nFile, f2, System.currentTimeMillis()+"");
            r = EnumDetail.dao.findById(getPara("detailID"))
                    .set("IconURL", "/enum/"+getPara("masterID")+"/"+iconFile.getName()).update();
        }else if (nFile==null){
            r = false;
        }
        if (r) {
            CacheKit.removeAll("SystemCache");
            responseData.put(ResponseCode.MSG, "icon设置成功！");
        }else{
            responseData.put(ResponseCode.MSG, "icon设置失败！");
        }
        responseData.put(ResponseCode.RESULT, r);
        render(new JsonRender(responseData).forIE());
    }

    public void removeIcon(){
        boolean r= false;

        r = EnumDetail.dao.findById(getPara("detailID")).set("IconURL", null).update();
        if (r) {
            CacheKit.removeAll("SystemCache");
            responseData.put(ResponseCode.MSG, "icon清除成功！");
        }else{
            responseData.put(ResponseCode.MSG, "icon清除失败！");
        }
        responseData.put(ResponseCode.RESULT, r);
        renderJson(responseData);
    }
	
}
