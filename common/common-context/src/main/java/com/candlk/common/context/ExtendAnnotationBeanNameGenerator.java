package com.candlk.common.context;

import java.beans.*;
import java.util.*;

import org.springframework.beans.factory.config.*;
import org.springframework.beans.factory.support.*;
import org.springframework.context.annotation.*;

/**
 * 扩展增强的基于Spring注解的Bean Name生成器
 *

 * @date 2015年7月1日
 * @since 1.0
 */
public class ExtendAnnotationBeanNameGenerator extends AnnotationBeanNameGenerator {

	private Map<String, BeanNameStrategy> map;

	// TODO 模块化切换的功能还不够完善，还需要进一步开发
	// 需要进一步支持子包模糊匹配、后代包模糊匹配
	@Override
	public String generateBeanName(BeanDefinition definition, BeanDefinitionRegistry registry) {
		// 此外还支持根据类名的前缀和后缀进行动态模块切换
		// 还可与配置文件、数据库等配置数据进行结合使用
		// selectImpl("service.b", BeanNameStrategy.BY_PACKAGE, "service.api");
		// selectImpl("com.service.impl", BeanNameStrategy.BY_PREFIX, "A");
		return generateBeanName0(definition, registry);
	}

	public void selectImpl(String targetPackage, int strategy, String base) {
		if (map == null) {
			map = new HashMap<>();
		}
		map.put(targetPackage, new BeanNameStrategy(targetPackage, strategy, base));
	}

	/**
	 * 实际用于生成BeanName的方法
	 *
	
	 * @since 1.0
	 */
	private String generateBeanName0(BeanDefinition definition, BeanDefinitionRegistry registry) {
		if (map != null) {
			Class<?> clazz;
			try {
				clazz = Class.forName(definition.getBeanClassName());
			} catch (ClassNotFoundException e) {
				throw new IllegalStateException(e);
			}
			BeanNameStrategy s = map.get(clazz.getPackage().getName());
			if (s != null) {
				String className;
				switch (s.strategy) {
				case BeanNameStrategy.BY_PACKAGE:
					for (Class<?> cl : clazz.getInterfaces()) {
						if (cl.getPackage().getName().equals(s.base)) {
							return Introspector.decapitalize(cl.getSimpleName());
						}
					}
					break;
				case BeanNameStrategy.BY_PREFIX:
					className = clazz.getSimpleName();
					if (className.endsWith("Impl") && className.startsWith(s.base)) {
						return Introspector.decapitalize(className.substring(s.base.length(), className.length() - 4));
					}
					break;
				default:
					throw new IllegalArgumentException("invalid BeanNameStrategy.strategy");
				}
			}
		}
		String beanName = super.generateBeanName(definition, registry);
		final String suffix = "Impl";
		if (beanName.endsWith(suffix)) { // 以Impl结尾的实现类，默认去掉Impl后缀
			beanName = beanName.substring(0, beanName.length() - suffix.length());
		}
		return beanName;
	}

	public static class BeanNameStrategy {

		public static final int BY_PACKAGE = 1;
		public static final int BY_PREFIX = 2;
		protected int strategy;
		protected String targetPackage;
		protected String base;

		public BeanNameStrategy(String implPackage, int strategy, String base) {
			this.targetPackage = implPackage;
			this.strategy = strategy;
			this.base = base;
		}
	}

}
