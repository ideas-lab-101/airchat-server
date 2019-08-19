package com.linestorm.looker.model.bubble;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="bubble_post",pkName="id")
public class BubblePost extends Model<BubblePost> {
	private static final long serialVersionUID = 1L;
	public final static BubblePost dao = new BubblePost();


}
