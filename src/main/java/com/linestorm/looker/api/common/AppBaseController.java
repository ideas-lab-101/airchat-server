package com.linestorm.looker.api.common;

import com.jfinal.aop.Before;
import com.jfinal.core.Controller;
import com.jfinal.kit.PathKit;
import com.jfinal.plugin.activerecord.Page;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.extend.CacheKey;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.extend.RestResult;
import net.sf.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

/**
 * @类名字：AppBaseController
 * @类描述：
 * @author:Carl.Wu
 * @版本信息：
 * @日期：2013-9-11
 * @Copyright 足下 Corporation 2013 
 * @版权所有
 *
 */
@Before(AppLoginInterceptor.class)
public abstract class AppBaseController extends Controller {

	protected int pageSize = 20;
    protected static String ROOTPATH = PathKit.getWebRootPath();
    protected Map<String, Object> data = new HashMap<String, Object>();
    public RestResult rest = new RestResult();
	
	public abstract void index();
	
	@Override
	public void render(String view) {
		super.render(view);
	}

    /**
     * 获得app登陆用户的当前用户信息
     */
    public JSONObject getCurrentUser(String token){
        JSONObject jo = CacheKit.get(CacheKey.CACHE_USER_AUTH, token);
        if(jo!= null){
            return jo.getJSONObject("userInfo");
        }else{
            return null;
        }
    }

}