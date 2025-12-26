package com.bojiu.webapp.user.form.query;

import com.bojiu.webapp.base.form.MerchantForm;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.model.MetaType;
import lombok.*;

@Getter
@Setter
public class MetaQuery extends MerchantForm<Meta> {

	/** 所属类型 */
	public MetaType type;
	/** 值 */
	public String value;
	/** 名称 */
	public String label;
	public String name;
	/** 状态：0=停用，1=启用 */
	public Integer status;
	/** 附加SQL */
	@Setter(AccessLevel.NONE)
	public String applySql;
	/** 附属条件值 */
	@Setter(AccessLevel.NONE)
	public Object[] applyParams;
	/** 权限类型 用于权限检查 */
	String permCodeType;

}