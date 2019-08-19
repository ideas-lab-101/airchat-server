package com.linestorm.looker.model.topic;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="topic_comment",pkName="id")
public class TopicComment extends Model<TopicComment> {
	private static final long serialVersionUID = 1L;
	public final static TopicComment dao = new TopicComment();


}
