package com.linestorm.looker.common;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.common.AdminLoginInterceptor;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @类名字：BaseController
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-9-11
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */

@Before(AdminLoginInterceptor.class)
public abstract class AdminBaseController extends Controller {
	
	protected int pageSize = 20;
	protected static String ROOTPATH = PathKit.getWebRootPath();
	protected Map<String,Object> responseData = new HashMap<String, Object>();

	private String cacheName = "AdminCache";
	
	public abstract void index();
	
	@Override
	public void render(String view) {
		super.render(view);
	}

	public JSONObject getCurrentUser(){
		String uid = getCookie("u_id");
		JSONObject jo = CacheKit.get(cacheName, uid);
		if(jo!= null){
			return jo.getJSONObject("UserInfo");
		}else{
			return null;
		}
	}

}