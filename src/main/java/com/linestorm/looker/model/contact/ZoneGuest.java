package com.linestorm.looker.model.contact;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="zone_guest",pkName="id")
public class ZoneGuest extends Model<ZoneGuest> {
	private static final long serialVersionUID = 1L;
	public final static ZoneGuest dao = new ZoneGuest();


}
