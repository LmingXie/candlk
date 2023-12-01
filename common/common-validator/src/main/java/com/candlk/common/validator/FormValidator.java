package com.candlk.common.validator;

import java.beans.*;
import java.lang.annotation.Annotation;
import java.lang.reflect.*;
import java.util.*;
import java.util.Map.Entry;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;
import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;

import com.candlk.common.validator.RuleValidator.Result;
import com.candlk.common.validator.ValidateContext.AnnotatedValidatorEntry;
import com.candlk.common.validator.ValidateContext.ValidateItemInfo;
import lombok.extern.slf4j.Slf4j;
import me.codeplayer.util.X;
import org.apache.commons.lang3.reflect.FieldUtils;

@Slf4j
public class FormValidator implements ConstraintValidator<CheckForm, Object> {

	protected CheckForm check;

	public void initialize(CheckForm constraint) {
		check = constraint;
	}

	public boolean isValid(Object obj, ConstraintValidatorContext context) {
		context.disableDefaultConstraintViolation();
		if (obj == null) {
			if (check.notNull()) {
				String error = ValidateError.resolveRequiredError(check.label());
				context.buildConstraintViolationWithTemplate(error).addConstraintViolation();
				return false;
				// throw new ValidateException(error);
			}
			return true;
		}
		return validate(obj, check, context);
	}

	public static boolean validate(Object obj, CheckForm check, ConstraintValidatorContext context) {
		Form form = null;
		if (obj instanceof Form) {
			form = (Form) obj;
			form.preHandle();
		}
		BeanInfo beanInfo;
		try {
			beanInfo = BeanInfo.of(obj.getClass(), check);
		} catch (Exception e) {
			throw new IllegalStateException("初始化校验器时出错！", e);
		}
		ValidateContext.ValidateItem root = new ValidateContext.ValidateItem(beanInfo.root, obj);
		Map<String, ValidateContext.ValidateItem> items = new LinkedHashMap<>(beanInfo.items.size(), 1F);
		for (Entry<String, ValidateItemInfo> entry : beanInfo.items.entrySet()) {
			items.put(entry.getKey(), new ValidateContext.ValidateItem(entry.getValue()));
		}
		ValidateContext.DefaultValidateContext ctx = new ValidateContext.DefaultValidateContext(root, items);
		boolean stop = false;
		final boolean debugEnabled = log.isDebugEnabled();
		for (Entry<String, ValidateContext.ValidateItem> entry : items.entrySet()) {
			ValidateContext.ValidateItem item = entry.getValue();
			ctx.setCurrent(item);
			Object val = item.getFieldValue(obj);
			if (debugEnabled) {
				log.debug("开始校验：【{}】 = {}", item.getInfo().getName(), val);
			}
			if (val == null || (val instanceof CharSequence && val.toString().isEmpty())) {
				if (item.getInfo().required) {
					ctx.addError(() -> ValidateError.resolveRequiredError(ctx.getCurrentLabel()));
					if (debugEnabled) {
						log.debug("\t\t{} 执行校验结果：{}", "@Check.required", Result.NO);
					}
					stop = true;
				}
			} else {
				for (AnnotatedValidatorEntry v : item.getInfo().getToValidates()) {
					ctx.setCurrentEntry(v);
					if (debugEnabled) {
						log.debug("\t\t开始执行校验规则：{}", v.getRule().annotationType().getSimpleName());
					}
					RuleValidator<?, Object> validator = v.getValidator();
					Result result = validator.validate(item.val, ctx);
					if (debugEnabled) {
						log.debug("\t\t{} 执行校验结果：{}", v.getRule().annotationType().getSimpleName(), result);
					}
					if (result != Result.YES) {
						if (result == Result.NO) {
							stop = true;
						}
						break;
					}
				}
			}
			if (stop) {
				break;
			}
		}
		List<Supplier<String>> errors = ctx.getErrors();
		if (X.isValid(errors)) {
			context.buildConstraintViolationWithTemplate(errors.get(0).get()).addConstraintViolation();
			return false;
			// throw new ValidateException(errors.get(0).get());
		}
		if (form != null) {
			form.validate();
			form.postHandle();
		}
		return true;
	}

	public static class BeanInfo {

		public final ValidateItemInfo root;
		public final Map<String, ValidateItemInfo> items;

		static Map<Class<?>, BeanInfo> cache = new ConcurrentHashMap<>();

		public BeanInfo(ValidateItemInfo root, Map<String, ValidateItemInfo> items) {
			this.root = root;
			this.items = items;
		}

		public static BeanInfo of(Class<?> clazz, CheckForm check) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException, IntrospectionException {
			BeanInfo info = cache.get(clazz);
			if (info == null) {
				info = buildBeanInfo(clazz, check);
				cache.put(clazz, info);
			}
			return info;
		}

		protected static BeanInfo buildBeanInfo(Class<?> clazz, CheckForm check) throws IntrospectionException, InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			ValidateItemInfo rootInfo = new ValidateItemInfo(clazz, check.label(), check.notNull());
			Field[] fields = FieldUtils.getFieldsWithAnnotation(clazz, Check.class);
			final PropertyDescriptor[] pds = Introspector.getBeanInfo(clazz).getPropertyDescriptors();
			final Map<String, Method> fieldGetters = new LinkedHashMap<>(pds.length, 1F);
			for (PropertyDescriptor pd : pds) {
				if ("class".equals(pd.getName())) {
					continue;
				}
				Method getter = pd.getReadMethod();
				if (getter != null) {
					Check label = getter.getAnnotation(Check.class);
					if (label != null) {
						fieldGetters.put(pd.getName(), getter);
					}
				}
			}
			Map<String, ValidateItemInfo> items = new LinkedHashMap<>(fields.length, 1F);
			final List<AnnotatedValidatorEntry> list = new ArrayList<>(5);
			for (Field field : fields) {
				Annotation[] annotations = field.getAnnotations();
				Check fieldCheck = field.getAnnotation(Check.class);
				extractRules(list, annotations);
				String name = field.getName();
				Method method = fieldGetters.remove(name);
				if (method != null) {
					fieldCheck = method.getAnnotation(Check.class);
					extractRules(list, method.getAnnotations());
				}
				Collections.sort(list);
				items.put(name, new ValidateItemInfo(field, fieldCheck.value(), fieldCheck.required(), list.toArray(new AnnotatedValidatorEntry[0])));
				list.clear();
			}
			return new BeanInfo(rootInfo, items);
		}

		protected static void extractRules(List<AnnotatedValidatorEntry> list, Annotation[] annotations) throws InstantiationException, IllegalAccessException, InvocationTargetException, NoSuchMethodException {
			for (Annotation a : annotations) {
				Rule rule = a.annotationType().getAnnotation(Rule.class);
				if (rule != null) {
					Class<? extends RuleValidator<Annotation, Object>>[] vClasses = X.castType(rule.value());
					for (Class<? extends RuleValidator<Annotation, Object>> vClass : vClasses) {
						RuleValidator<Annotation, Object> validator = vClass.getConstructor().newInstance();
						list.add(new AnnotatedValidatorEntry(a, validator));
					}
				}
			}
		}

	}

}
