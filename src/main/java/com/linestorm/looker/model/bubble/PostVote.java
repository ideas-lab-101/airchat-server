package com.linestorm.looker.model.bubble;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="bubble_postvote",pkName="id")
public class PostVote extends Model<PostVote> {
	private static final long serialVersionUID = 1L;
	public final static PostVote dao = new PostVote();


}
