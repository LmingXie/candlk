package com.bojiu.context.rpc;

import java.util.concurrent.atomic.AtomicLong;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.bojiu.common.context.Env;
import com.bojiu.context.ContextImpl;
import com.bojiu.context.web.RequestContextImpl;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.NumberUtil;
import me.codeplayer.util.StringUtil;
import org.apache.dubbo.common.URL;
import org.apache.dubbo.common.constants.CommonConstants;
import org.apache.dubbo.common.extension.Activate;
import org.apache.dubbo.metadata.MetadataService;
import org.apache.dubbo.rpc.*;

/**
 * Dubbo 消费者/生产者通用过滤器，在 调用提供者服务 或 被消费者调用 时触发
 */
@Activate(group = { CommonConstants.PROVIDER, CommonConstants.CONSUMER })
@Slf4j
public class DubboSessionFilter implements Filter {

	static final String SESSION_ID = "springSessionId";
	static final String INVOKE_ID = "invokeId";
	static final String MERCHANT_ID = "merchantId";
	static final AtomicLong counter = new AtomicLong();

	@Override
	public Result invoke(Invoker<?> invoker, Invocation invocation) throws RpcException {
		final URL uri = invoker.getUrl();
		if (MetadataService.isMetadataService(uri.getServiceInterface())) {
			return invoker.invoke(invocation);
		}
		final String threadName = Thread.currentThread().getName();
		final RpcContext context = RpcContext.getServiceContext();
		final RequestContextImpl req = RequestContextImpl.get();
		final boolean isConsumer = uri.getSide(CommonConstants.PROVIDER_SIDE).equals(CommonConstants.CONSUMER_SIDE); // 消费者端
		final Long merchantId;
		// 并不要求完全唯一，只要能够方便定位即可
		String invokeId;
		if (isConsumer) { // context.isConsumerSide() 在某些 dubbo 版本可能有 NPE 问题
			invokeId = System.currentTimeMillis() + "_" + counter.incrementAndGet();
			merchantId = ContextImpl.currentMerchantId();
			log.info("FROM: t={}, merchantId={}, invokeId={} | {}", threadName, merchantId, invokeId, uri);
			String sessionId = req.getSessionId();
			if (StringUtil.isEmpty(sessionId)) {
				HttpServletRequest request = req.getRequest();
				if (request != null) { // 来自 Web 端的原始请求
					HttpSession session = request.getSession();
					req.getSessionContext(request);
					req.setSessionId(sessionId = session.getId()); // 如果有多次调用，无需重复设置
					req.flushSession(true);
				}
			}
			context.setAttachment(SESSION_ID, sessionId);
			context.setAttachment(INVOKE_ID, invokeId);
			context.setAttachment(MERCHANT_ID, merchantId);
		} else { // 服务提供者
			String sessionId = context.getAttachment(SESSION_ID);
			merchantId = NumberUtil.getLong(context.getAttachment(MERCHANT_ID), null);
			if (merchantId != null) {
				ContextImpl.currentMerchantId(merchantId);
			}
			req.tryClean(); // 保证初始化数据是干净的
			req.setSessionId(sessionId);
			invokeId = context.getAttachment(INVOKE_ID);
			if (Env.inner()) {
				log.info("RUN: merchantId={}, sessionId={}, invokeId={}", merchantId, sessionId, invokeId);
			}
		}
		req.flushSession(false);
		try {
			return invoker.invoke(invocation);
		} finally {
			if (!isConsumer) { // 服务提供者一定要清空商户ID
				ContextImpl.removeCurrentMerchantId();
				log.info("TO: t={}, merchantId={}, invokeId={}", threadName, merchantId, invokeId);
			}
			req.flushSession(false);
		}
	}

}