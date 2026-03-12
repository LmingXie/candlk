package com.bojiu.webapp.user.form;

import com.bojiu.common.security.AES;
import com.bojiu.common.util.Common;
import com.bojiu.common.validator.Check;
import com.bojiu.common.validator.Form;
import com.bojiu.context.web.Jsons;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class VerifyCardForm implements Form {

	@Check("银行卡号")
	public String cardNum;

	@Check("银行编码")
	public Integer code;

	/** 银行卡类型（1=储蓄卡；2=信用卡） */
	@Check("银行卡类型")
	public Integer brankType;

	/** 业务类型（1=绑银行卡） */
	public Integer type = 1;

	/** 手机号（130****1422） */
	public String phone;
	/** 短信码 */
	public String phoneCode;
	/** 实名名字（张**） */
	public String realName;
	/** 开户地址（甘肃省,兰州市,七里河区） */
	public String address;

	/**
	 * 开云绑卡：/page/fd/api/v1/bankCard/insertMemberBanksV1
	 */
	public String toX(String key, String vi) {
		final AES aes = new AES(Common.decodeBase64(key), Common.decodeBase64(vi));
		return "{\"x\":\"" + aes.encrypt(Jsons.encode(this)) + "\"}";
	}

}
