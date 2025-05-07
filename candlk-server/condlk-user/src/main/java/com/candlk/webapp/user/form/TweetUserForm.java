package com.candlk.webapp.user.form;

import com.candlk.common.validator.Check;
import com.candlk.common.validator.Form;
import com.candlk.webapp.user.model.TweetUserType;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class TweetUserForm implements Form {

	@Check(value = "ID")
	public Long id;

	@Check(value = "类型")
	public TweetUserType type;

}
