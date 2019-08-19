package com.linestorm.looker.model.contact;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Db;
import com.jfinal.plugin.activerecord.Model;
import com.jfinal.plugin.activerecord.Record;

@TableBind(tableName="contact_friend",pkName="id")
public class ContactFriend extends Model<ContactFriend> {
	private static final long serialVersionUID = 1L;
	public final static ContactFriend dao = new ContactFriend();

	public boolean blnFriend(String user_id, String friend_id){
	    boolean r = false;
	    if(user_id.equals(friend_id)){
	    	r = true;
		}else{
			Record rl = Db.findFirst("select * from contact_friend cf \n" +
					"where cf.user_id=? and cf.friend_id=?", user_id, friend_id);
			r = rl == null ? false : true;
		}
		return r;
	}

}
