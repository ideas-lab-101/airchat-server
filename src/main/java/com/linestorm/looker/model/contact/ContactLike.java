package com.linestorm.looker.model.contact;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="contact_like",pkName="id")
public class ContactLike extends Model<ContactLike> {
	private static final long serialVersionUID = 1L;
	public final static ContactLike dao = new ContactLike();


}
