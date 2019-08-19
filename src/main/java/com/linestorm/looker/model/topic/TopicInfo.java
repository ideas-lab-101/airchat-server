package com.linestorm.looker.model.topic;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="topic_baseinfo",pkName="id")
public class TopicInfo extends Model<TopicInfo> {
	private static final long serialVersionUID = 1L;
	public final static TopicInfo dao = new TopicInfo();


}
