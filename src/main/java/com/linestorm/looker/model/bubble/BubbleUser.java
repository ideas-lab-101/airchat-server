package com.linestorm.looker.model.bubble;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="bubble_userindex",pkName="id")
public class BubbleUser extends Model<BubbleUser> {
	private static final long serialVersionUID = 1L;
	public final static BubbleUser dao = new BubbleUser();


}
