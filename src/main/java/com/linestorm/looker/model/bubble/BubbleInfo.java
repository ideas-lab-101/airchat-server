package com.linestorm.looker.model.bubble;

import com.jfinal.ext.plugin.tablebind.TableBind;
import com.jfinal.plugin.activerecord.Model;

@TableBind(tableName="bubble_baseinfo",pkName="id")
public class BubbleInfo extends Model<BubbleInfo> {
	private static final long serialVersionUID = 1L;
	public final static BubbleInfo dao = new BubbleInfo();

	public void validCheck(){
        String sql ="select bi.* \n" +
                "from bubble_baseinfo bi\n" +
                "where bi.deleted=0 and (UNIX_TIMESTAMP(now())-bi.created_time)/3600>bi.interval";

        for (BubbleInfo dm : BubbleInfo.dao.find(sql)){
            dm.set("deleted", 1).update();
        }
	}
}
