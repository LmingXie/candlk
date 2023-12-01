package com.candlk.context.web;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.candlk.common.web.Logs;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.*;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * 全局的请求日志拦截处理器
 */
@Slf4j
@Aspect
@Component
public class GlobalLogInterceptor {

	/**
	 * 定义拦截规则：拦截 com.candlk..action 包下面的所有类中。
	 */
	@Pointcut("@annotation(org.springframework.web.bind.annotation.PostMapping) || @annotation(org.springframework.web.bind.annotation.GetMapping) || @annotation(org.springframework.web.bind.annotation.RequestMapping) && execution(* com.candlk..*Action.*(..))")
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
	}

}
