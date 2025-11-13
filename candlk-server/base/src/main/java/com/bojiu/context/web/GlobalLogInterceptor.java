package com.bojiu.context.web;

import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.web.Logs;
import com.bojiu.context.model.MemberType;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 全局的请求日志拦截处理器
 */
@Aspect
@Component
public class GlobalLogInterceptor {

	/**
	 * 定义拦截规则：拦截 com.bojiu..action 包下面的所有类中。
	 */
	@Around("@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* com.bojiu..*Action.*(..))")
	public Object doAround(ProceedingJoinPoint pjp) throws Throwable {
		HttpServletRequest request = ((ServletRequestAttributes) RequestContextHolder.getRequestAttributes()).getRequest();
		Object result = null;
		Throwable err = null;
		try {
			result = pjp.proceed();
			if (result != null) {
				request.setAttribute(Logs.RESPONSE, result);
				// 如果是后台 数据量 较多的分页列表，则可以酌情缓存总记录数
				if (MemberType.fromBackstage()) {
					ResponseDataHandler.handleResultIfNeeded(request, result);
				}
			}
			return result;
		} catch (Throwable e) {
			request.setAttribute(Logs.EXCEPTION, err = e);
			throw e;
		} finally {
			DeferTask.executeDeferTasks(request, result, err);
		}
	}

}