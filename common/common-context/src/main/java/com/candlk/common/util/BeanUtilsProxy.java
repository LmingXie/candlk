package com.candlk.common.util;

import java.beans.*;
import java.lang.reflect.*;
import java.util.function.*;

import org.springframework.beans.*;
import org.springframework.core.*;
import org.springframework.lang.*;
import org.springframework.util.*;

public abstract class BeanUtilsProxy extends BeanUtils {

	/**
	 * Copy the property values of the given source bean into the given target bean,
	 * ignoring the given "ignoreProperties".
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>This is just a convenience method. For more complex transfer needs,
	 * consider using a full BeanWrapper.
	 *
	 * @param source the source bean
	 * @param target the target bean
	 * @param propertyFilter property filter
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	public static void copyProperties(Object source, Object target, @Nullable Predicate<String> propertyFilter) throws BeansException {
		copyProperties(source, target, null, propertyFilter);
	}

	/**
	 * Copy the property values of the given source bean into the given target bean.
	 * <p>Note: The source and target classes do not have to match or even be derived
	 * from each other, as long as the properties match. Any bean properties that the
	 * source bean exposes but the target bean does not will silently be ignored.
	 * <p>As of Spring Framework 5.3, this method honors generic type information
	 * when matching properties in the source and target objects.
	 *
	 * @param source the source bean
	 * @param target the target bean
	 * @param editable the class (or interface) to restrict property setting to
	 * @param propertyFilter property filter
	 * @throws BeansException if the copying failed
	 * @see BeanWrapper
	 */
	private static void copyProperties(Object source, Object target, @Nullable Class<?> editable,
	                                   @Nullable Predicate<String> propertyFilter) throws BeansException {

		Assert.notNull(source, "Source must not be null");
		Assert.notNull(target, "Target must not be null");

		Class<?> actualEditable = target.getClass();
		if (editable != null) {
			if (!editable.isInstance(target)) {
				throw new IllegalArgumentException("Target class [" + target.getClass().getName() +
						"] not assignable to Editable class [" + editable.getName() + "]");
			}
			actualEditable = editable;
		}
		PropertyDescriptor[] targetPds = getPropertyDescriptors(actualEditable);
		for (PropertyDescriptor targetPd : targetPds) {
			Method writeMethod = targetPd.getWriteMethod();
			if (writeMethod != null && (propertyFilter == null || propertyFilter.test(targetPd.getName()))) {
				PropertyDescriptor sourcePd = getPropertyDescriptor(source.getClass(), targetPd.getName());
				if (sourcePd != null) {
					Method readMethod = sourcePd.getReadMethod();
					if (readMethod != null) {
						ResolvableType sourceResolvableType = ResolvableType.forMethodReturnType(readMethod);
						ResolvableType targetResolvableType = ResolvableType.forMethodParameter(writeMethod, 0);

						// Ignore generic types in assignable check if either ResolvableType has unresolvable generics.
						boolean isAssignable =
								(sourceResolvableType.hasUnresolvableGenerics() || targetResolvableType.hasUnresolvableGenerics() ?
										ClassUtils.isAssignable(writeMethod.getParameterTypes()[0], readMethod.getReturnType()) :
										targetResolvableType.isAssignableFrom(sourceResolvableType));

						if (isAssignable) {
							try {
								if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
									readMethod.setAccessible(true);
								}
								Object value = readMethod.invoke(source);
								if (!Modifier.isPublic(writeMethod.getDeclaringClass().getModifiers())) {
									writeMethod.setAccessible(true);
								}
								writeMethod.invoke(target, value);
							} catch (Throwable ex) {
								throw new FatalBeanException(
										"Could not copy property '" + targetPd.getName() + "' from source to target", ex);
							}
						}
					}
				}
			}
		}
	}

}
