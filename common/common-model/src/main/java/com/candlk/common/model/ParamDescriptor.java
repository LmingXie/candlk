package com.candlk.common.model;

import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;
import me.codeplayer.util.StringUtil;

/**
 * 配置参数描述符
 *
 * @since 2016年9月9日
 */
@Getter
@Setter
@Accessors(chain = true)
public class ParamDescriptor {

	protected String name;
	protected String label;
	protected String placeholder;
	protected String desc;
	protected boolean required = true;

	public ParamDescriptor init(String name, String label, boolean required, String placeholder, String desc) {
		this.name = name;
		this.label = label;
		this.required = required;
		this.placeholder = placeholder;
		this.desc = desc;
		return this;
	}

	public ParamDescriptor init(String name, String label) {
		this.name = name;
		this.label = label;
		return this;
	}

	public static ParamDescriptor of(String name, String label) {
		return new ParamDescriptor().init(name, label);
	}

	public static ParamDescriptor of(String name, String label, boolean required, String placeholder, String desc) {
		return new ParamDescriptor().init(name, label, required, placeholder, desc);
	}

	public static ParamDescriptor of(String name, String label, boolean required) {
		return new ParamDescriptor().init(name, label, required, null, null);
	}

	/**
	 * 校验支付相关参数的正确性，如果校验通过则返回null，否则返回对应的错误提示信息
	 *
	 * @param config 配置项集合
	 * @param throwsEx 是否直接抛出异常
	 */
	public static String checkConfig(Map<String, String> config, ParamDescriptor[] descriptors, boolean throwsEx) {
		for (ParamDescriptor pd : descriptors) {
			if (pd.isRequired()) {
				String val = config.get(pd.getName());
				if (StringUtil.isBlank(val)) {
					String error = pd.getLabel() + "不能为空！";
					if (throwsEx) {
						throw new IllegalArgumentException(error);
					}
					return error;
				}
			}
		}
		return null;
	}

	/**
	 * 校验支付相关参数的正确性，校验不通过时直接抛出异常
	 *
	 * @param config 配置项集合
	 */
	public static void checkConfig(Map<String, String> config, ParamDescriptor[] descriptors) {
		checkConfig(config, descriptors, true);
	}

}
