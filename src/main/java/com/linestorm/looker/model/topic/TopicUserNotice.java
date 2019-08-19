package com.linestorm.looker.model.topic;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="topic_userNotice",pkName="id")
public class TopicUserNotice extends Model<TopicUserNotice> {
	private static final long serialVersionUID = 1L;
	public final static TopicUserNotice dao = new TopicUserNotice();

}
