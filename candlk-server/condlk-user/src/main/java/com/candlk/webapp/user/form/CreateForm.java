package com.candlk.webapp.user.form;

import com.candlk.common.validator.Check;
import com.candlk.common.validator.Form;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class CreateForm implements Form {

	@Check(value = "ID")
	public Long id;

	@Check(value = "合约地址")
	public String ca;

}
