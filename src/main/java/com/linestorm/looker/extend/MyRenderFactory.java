package com.linestorm.looker.extend;


import com.jfinal.render.JsonRender;
import com.jfinal.render.Render;
import com.jfinal.render.RenderFactory;

public class MyRenderFactory extends RenderFactory {
    @Override
    public Render getErrorRender(int errorCode) {
        super.getErrorRender(errorCode);
        RestResult rest = new RestResult();

        if (errorCode == 500) {
            rest.error("服务器500错误！").setCode(500);
        }else if(errorCode == 404){
            rest.error("服务器404错误！").setCode(404);
        }
        return new JsonRender(rest);
    }
}