package com.linestorm.looker.model.topic;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="topic_attachment",pkName="id")
public class TopicAttach extends Model<TopicAttach> {
	private static final long serialVersionUID = 1L;
	public final static TopicAttach dao = new TopicAttach();


}
