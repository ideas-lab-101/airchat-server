package com.linestorm.looker.model.contact;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="contact_relation",pkName="id")
public class ContactRelation extends Model<ContactRelation> {
	private static final long serialVersionUID = 1L;
	public final static ContactRelation dao = new ContactRelation();


}
