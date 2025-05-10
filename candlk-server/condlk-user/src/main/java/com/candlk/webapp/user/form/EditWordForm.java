package com.candlk.webapp.user.form;

import java.util.List;

import com.candlk.common.validator.Check;
import com.candlk.common.validator.Form;
import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class EditWordForm implements Form {

	public List<Long> ids;

	/** 关键词类型：0=热门词；1=二级词；2=普通词；3=停用词； */
	@Check(value = "关键词类型", required = false)
	public Integer type;

	@Check(value = "状态", required = false)
	public Integer status;

}
