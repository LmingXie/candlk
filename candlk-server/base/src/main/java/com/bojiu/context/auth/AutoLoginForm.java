package com.bojiu.context.auth;

import java.io.Serializable;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.context.Env;
import com.bojiu.common.validator.Check;
import com.bojiu.common.validator.Size;
import com.bojiu.common.web.Client;
import com.bojiu.context.model.MemberType;
import com.bojiu.context.web.RequestContextImpl;
import lombok.Getter;
import me.codeplayer.util.StringUtil;

@Getter
public class AutoLoginForm implements Serializable {

	public Client client;
	public boolean fromBackstage;
	public String ip;

	/** 设备信息 */
	@Check(required = false)
	@Size(max = 100)
	public String deviceId;

	public void setDeviceId(String deviceId) {
		if (!Env.inProduction()) {
			this.deviceId = deviceId; // 生产环境暂不允许自定义设备ID，后续如果有需要，也要加密传输
		}
	}

	public static AutoLoginForm create(HttpServletRequest request, final String clientId) {
		final AutoLoginForm form = new AutoLoginForm();
		form.client = RequestContextImpl.doGetClient(request);
		form.fromBackstage = MemberType.fromBackstage();
		if (Env.inProduction() || StringUtil.isEmpty(form.ip = request.getHeader("ip"))) {
			// 只有非生产环境才允许自定义 IP 请求头
			form.ip = RequestContextImpl.doGetClientIP(request);
		}
		form.deviceId = clientId;
		return form;
	}

}