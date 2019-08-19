package com.linestorm.looker.model.topic;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="topic_uservote",pkName="id")
public class TopicVote extends Model<TopicVote> {
	private static final long serialVersionUID = 1L;
	public final static TopicVote dao = new TopicVote();


}
