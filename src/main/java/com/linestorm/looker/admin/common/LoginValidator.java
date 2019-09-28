package com.linestorm.looker.admin.common;

import com.jfinal.core.Controller;
import com.jfinal.kit.StrKit;
import com.jfinal.validate.Validator;
import com.linestorm.looker.extend.CaptchaRender;
import com.linestorm.looker.extend.ResponseCode;
import com.linestorm.looker.extend.RestResult;

public class LoginValidator extends Validator {

	public RestResult rest = new RestResult();

	@Override
	protected void validate(Controller controller) {
		validateRequiredString("username", ResponseCode.MSG, "请输入用户名！");
		validateRequiredString("password", ResponseCode.MSG, "请输入密码！");
		validateCaptcha("capval", ResponseCode.MSG, "验证码错误!");
	}

	@Override
	protected void validateRequiredString(String field, String errorKey, String errorMessage) {
		if (StrKit.isBlank(this.controller.getPara(field))){
			rest.error(errorMessage);
			this.setShortCircuit(true);
			this.addError(errorKey, errorMessage);
		}
	}

	@Override
	protected void validateCaptcha(String filed, String errorKey, String errorMessage){
		String md5Code = null;
		if(this.controller.getSession() != null){
			md5Code = (String)this.controller.getSession().getAttribute(CaptchaRender.DEFAULT_CAPTCHA_MD5_CODE_KEY);
		}
		boolean isRight = CaptchaRender.validate(md5Code, this.controller.getPara(filed));
		if (!isRight){
			rest.error(errorMessage);
			this.addError(errorKey, errorMessage);
		}
	}

	@Override
	protected void handleError(Controller controller) {
		controller.keepPara("loginName");
		controller.renderJson(rest);
	}
}
