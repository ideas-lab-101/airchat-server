package com.linestorm.looker.extend;

/**
 * 缓存目录 KEY
 * @author Rlax
 *
 */
public class CacheKey {

	/** 基础数据 对应 data 表 keyValue缓存在cache的name */
	public static final String CACHE_KEYVALUE = "keyValue";

	/** 页面数据缓存 */
	public static final String CACHE_PAGE = "pageCache";

	/** 30分钟缓存 */
	public static final String CACHE_H1M30 = "h1m30";
	
	/** 导航目录缓存 */
	public static final String CACHE_MENU = "menuCache";

	/** 管理用户授权缓存 */
	public static final String CACHE_ADMIN_AUTH = "AdminCache";

	/** 普通用户授权缓存 */
	public static final String CACHE_USER_AUTH = "UserCache";

	/** 验证码缓存 */
	public static final String CACHE_REG_CODE = "SMSIdentifyCache";

	/** 错误锁定缓存*/
	public static final String CACHE_ACCOUNT_LOCK = "AccountLockCache";
}
