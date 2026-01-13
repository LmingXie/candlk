package com.bojiu.webapp.user.form;

import java.util.List;

import com.bojiu.common.context.I18N;
import com.bojiu.common.validator.*;
import com.bojiu.context.i18n.AdminI18nKey;
import com.bojiu.context.web.Jsons;
import com.bojiu.webapp.base.entity.MetaValue;
import com.bojiu.webapp.base.form.BaseForm;
import com.bojiu.webapp.user.dto.BaseRateConifg;
import com.bojiu.webapp.user.dto.BetApiConfig;
import com.bojiu.webapp.user.entity.Meta;
import com.bojiu.webapp.user.model.MetaType;
import lombok.*;
import lombok.experimental.Accessors;
import me.codeplayer.util.*;

@SuppressWarnings("rawtypes")
@Getter
@Setter
@Accessors(chain = true)
public class MetaForm extends BaseForm<Meta> {

	public Long id;

	/** 所属类型 */
	@Check(AdminI18nKey.FIELD_TYPE_BELONG)
	public MetaType type;

	/** 值 */
	@Check(value = AdminI18nKey.FIELD_VALUE, required = false)
	@Trim
	@NotEmpty
	public String value;

	/** 别名 */
	@Check(value = AdminI18nKey.FIELD_ALIAS, required = false)
	@Trim
	@NotEmpty
	public String name;
	/** 名称 */
	@Check(value = AdminI18nKey.FIELD_NAME, required = false)
	@Trim
	@NotEmpty
	public String label;
	/** 状态 */
	@Check(value = AdminI18nKey.FIELD_STATUS, required = false)
	@IntBool
	public Integer status;

	@Check(value = AdminI18nKey.FIELD_RULE_CONFIG, required = false)
	@Trim
	@NotEmpty
	public String config;
	public Long merchantId;
	@Setter(AccessLevel.NONE)
	public MetaValue ruleConfig;
	@Setter(AccessLevel.NONE)
	public List<? extends MetaValue> ruleConfigs;

	@Override
	public void validate() {
		if (StringUtil.notEmpty(config)) {
			switch (type) {
				case bet_config -> ruleConfig = Jsons.parseObject(config, BetApiConfig.class);
				case base_rate_config -> ruleConfig = Jsons.parseObject(config, BaseRateConifg.class);
				default -> ruleConfig = type.deserialize(config);
			}
		}
		if (ruleConfig != null) {
			ruleConfig.validate();
			ruleConfig.init();
			value = config = Jsons.encodeRaw(ruleConfig);
		} else if (X.isValid(ruleConfigs)) {
			for (MetaValue ruleConfig : ruleConfigs) {
				ruleConfig.validate();
				ruleConfig.init();
			}
			value = config = Jsons.encodeRaw(ruleConfigs);
		}
		I18N.assertTrue(type.isEnabled(), AdminI18nKey.FROM_TYPE_NOT_SUPPORT);
		Assert.notEmpty(value, I18N.msg(AdminI18nKey.FROM_VALUE_REQUIRED));
	}

}