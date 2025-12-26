package com.bojiu.webapp.user.vo;

import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.MetaValue;
import com.bojiu.webapp.base.vo.AbstractVO;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.model.MetaType;
import lombok.Getter;
import lombok.Setter;
import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;

@Getter
@Setter
public class MetaVO extends AbstractVO<Meta> {

	/** 商户ID */
	Long merchantId;
	/** 所属类型 */
	MetaType type;
	/** 值 */
	String value;
	/** 名称 */
	String label;
	/** 别名 */
	String name;
	/** 备注 */
	String remark;
	/** 排序 */
	Integer sort;
	/** 状态：0=停用，1=启用 */
	Integer status;

	/** 积分规则配置字符串（一般为JSON字符串或其他特定的格式） */
	@SuppressWarnings("rawtypes")
	protected transient MetaValue parsedValue;

	public static MetaVO from(Meta source) {
		return copy(source, MetaVO::new);
	}

	public static MetaVO fromItem(Meta source) {
		return copy(source, MetaVO::new, "sort", "status");
	}

	public Integer getType() {
		return X.map(type, MetaType::getValue);
	}

	public String getType_() {
		return label(type);
	}

	public String getStatus_() {
		return super.getStatus_(status);
	}

	public Object getConfig() {
		return parsedValue == null && StringUtil.notEmpty(value) ? Jsons.deserialize(value) : parsedValue;
	}

}