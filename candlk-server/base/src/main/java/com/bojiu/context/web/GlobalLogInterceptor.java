package com.bojiu.context.web;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.bojiu.common.web.Logs;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
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
	@Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* com.bojiu..*Action.*(..))")
	public void actionMethodPointcut() {
	}

	@AfterReturning(returning = "ret", pointcut = "actionMethodPointcut()")
	public void doAfterReturning(JoinPoint joinPoint, Object ret) throws Exception {
		doLog(joinPoint, ret, null);
	}

	@AfterThrowing(pointcut = "actionMethodPointcut()", throwing = "e")
	public void doAfterThrowing(JoinPoint joinPoint, Throwable e) throws Exception {
		doLog(joinPoint, null, e);
	}

	public void doLog(JoinPoint joinPoint, @Nullable Object retVal, @Nullable Throwable e) {
		ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
		HttpServletRequest request = attributes.getRequest();

		if (retVal != null) {
			request.setAttribute(Logs.RESPONSE, retVal);
		}
		if (e != null) {
			request.setAttribute(Logs.EXCEPTION, e);
		}

		DeferTask.executeDeferTasks(request, retVal, e);
	}

}