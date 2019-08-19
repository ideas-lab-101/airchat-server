package com.linestorm.looker.controller;

import com.jfinal.ext.route.ControllerBind;
import com.jfinal.plugin.ehcache.CacheKit;
import com.linestorm.looker.common.AdminBaseController;
import com.linestorm.looker.model.sys.Cities;
import com.linestorm.looker.model.sys.Districts;
import com.linestorm.looker.model.sys.Provinces;

import java.util.ArrayList;
import java.util.List;

@ControllerBind(controllerKey = "/area")
public class AreaController extends AdminBaseController {

	private String cacheName = "SystemCache";

	@SuppressWarnings({ "rawtypes", "unchecked" })
	@Override
	public void index() {
		List list = (List) CacheKit.get(cacheName, "areaList");
		if (list == null) {
			list = new ArrayList<Object>();
			List<Provinces> provinces = Provinces.dao
					.find("SELECT DISTINCT concat('p',p.ProvinceID) id ,p.ProvinceID provinceID,p.ProvinceName name ,'true' as 'extend' \n"
							+"from sys_province p \n"
							+"INNER JOIN org_baseinfo ob ON ob.ProvinceID=p.ProvinceID");
			for (int i = 0; i < provinces.size(); i++) {
				Provinces provinces2 = provinces.get(i);
				// 把省级数据放入集合中
				list.add(provinces2);
				int xid = provinces.get(i).getInt("provinceID");
				List<Cities> cities = Cities.dao
						.find("SELECT DISTINCT concat('c',c.CityID) id,c.CityID cityID,c.CityName name ,concat('p',?) as 'pid' \n"
								+"from sys_city c \n"
								+"INNER JOIN org_baseinfo ob ON ob.CityID=c.CityID WHERE c.ProvinceID=?",
								new Object[] { xid, xid });
				if (cities.size() > 0) {
					for (Cities cities2 : cities) {
						// 把城市数据放入集合中
						list.add(cities2);
						int cid = cities2.getInt("cityID");
						List<Districts> districts = Districts.dao
								.find("SELECT DISTINCT concat('d',d.DistrictID) id,d.DistrictID districtID,d.DistrictName name ,concat('c',?) as 'pid' \n"
										+"from sys_district d \n"
										+"INNER JOIN org_baseinfo ob ON ob.DistrictID=d.DistrictID WHERE d.CityID=?",
										new Object[] { cid, cid });
						if (districts.size() > 0) {
							for (Districts districts2 : districts) {
								// 把区域数据放入集合中
								list.add(districts2);
							}
						}
					}
				}
			}
			CacheKit.put(cacheName, "areaList", list);
		}
		renderJson(list);
	}

	/**
	 * 
	 * @方法名:getProvinces
	 * @方法描述：获取省份列表
	 * @author: Carl.Wu
	 * @return: void
	 * @version: 2013-9-13 下午12:04:09
	 */
	public void getProvinces() {
		int nationID = getParaToInt("nationID", 86);
		List<Provinces> list = Provinces.dao.findByCache(cacheName, "provinces_" +nationID,
				"select * from sys_province where NationID=?", nationID);
		renderJson(list);

	}

	/**
	 * @方法名:getCities
	 * @方法描述：获取城市列表
	 * @author: Carl.Wu
	 * @return: void
	 * @version: 2013-9-13 下午12:04:01
	 */
	public void getCities() {
		if (getParaToInt("provinceID") != null) {
			List<Cities> cities = Cities.dao.findByCache(cacheName, "cities_"
					+ getParaToInt("provinceID"),
					"select * from sys_city where ProvinceID=?",
					getParaToInt("provinceID"));
			renderJson(cities);
		} else
			return;

	}

	/**
	 * @方法名:getDistricts
	 * @方法描述：获取区县列表
	 * @author: Carl.Wu
	 * @return: void
	 * @version: 2013-9-13 下午12:05:18
	 */
	public void getDistricts() {
		if (getParaToInt("cityID") != null) {
			List<Districts> districts = Districts.dao.findByCache(cacheName, "districts_"
                    + getParaToInt("cityID"),
					"select * from sys_district where CityID=?",
					getParaToInt("cityID"));
			renderJson(districts);
		} else
			return;

	}

}
