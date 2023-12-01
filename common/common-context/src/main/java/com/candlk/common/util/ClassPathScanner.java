package com.candlk.common.util;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.LinkedHashSet;
import java.util.function.BiPredicate;
import java.util.function.Predicate;

import me.codeplayer.util.StringUtil;
import me.codeplayer.util.X;
import org.springframework.core.io.Resource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;
import org.springframework.core.type.classreading.CachingMetadataReaderFactory;
import org.springframework.core.type.classreading.MetadataReader;
import org.springframework.core.type.classreading.MetadataReaderFactory;
import org.springframework.lang.Nullable;
import org.springframework.util.ClassUtils;

/*
符合什么样的条件：
	ROOT 出发点（什么包名、什么路径）
	希望得到什么：Class List、Method List、Field List
		包过滤器
		类过滤器
		方法过滤器
		字段过滤器
		自定义过滤器
*/
public class ClassPathScanner {

	public static final String CLASS_PATH_PREFIX = "classpath";

	protected MetadataReaderFactory metadataReaderFactory;

	public void scan(String basePackageToScan, Predicate<MetadataReader> callback) throws IOException {
		PathMatchingResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
		String classPathPrefix = basePackageToScan.startsWith(CLASS_PATH_PREFIX) ? null : ResourcePatternResolver.CLASSPATH_ALL_URL_PREFIX;
		String locationPattern = StringUtil.concat(classPathPrefix, ClassUtils.convertClassNameToResourcePath(basePackageToScan), "/*.class");
		Resource[] resources = resolver.getResources(locationPattern);
		MetadataReaderFactory factory = getOrCreateMetadataReaderFactory();
		for (Resource resource : resources) {
			MetadataReader reader = factory.getMetadataReader(resource);
			if (!callback.test(reader)) {
				return;
			}
		}
	}

	public LinkedHashSet<Class<?>> findClasses(String basePackageToScan, @Nullable ClassLoader classLoader, @Nullable BiPredicate<MetadataReader, Class<?>> filter) throws IOException {
		final ClassLoader loader = X.getElse(classLoader, this::getClassLoader);
		final LinkedHashSet<Class<?>> classes = new LinkedHashSet<>();
		scan(basePackageToScan, reader -> {
			String className = reader.getClassMetadata().getClassName();
			Class<?> clazz = loadClass(className, loader);
			if (clazz != null && (filter == null || filter.test(reader, clazz))) {
				classes.add(clazz);
			}
			return true;
		});
		return classes;
	}

	public LinkedHashSet<Class<?>> findClasses(String basePackageToScan, @Nullable BiPredicate<MetadataReader, Class<?>> filter) throws IOException {
		return findClasses(basePackageToScan, null, filter);
	}

	public LinkedHashSet<Method> findMethods(String basePackageToScan, @Nullable ClassLoader classLoader, @Nullable BiPredicate<MetadataReader, Class<?>> classFilter, @Nullable Predicate<Method> methodFilter) throws IOException {
		final ClassLoader loader = X.getElse(classLoader, this::getClassLoader);
		final LinkedHashSet<Method> methods = new LinkedHashSet<>();
		scan(basePackageToScan, reader -> {
			String className = reader.getClassMetadata().getClassName();
			Class<?> clazz = loadClass(className, loader);
			if (clazz != null && (classFilter == null || classFilter.test(reader, clazz))) {
				Method[] declaredMethods = clazz.getDeclaredMethods();
				for (Method method : declaredMethods) {
					if (methodFilter == null || methodFilter.test(method)) {
						methods.add(method);
					}
				}
			}
			return true;
		});
		return methods;
	}

	public LinkedHashSet<Method> findMethods(String basePackageToScan, @Nullable BiPredicate<MetadataReader, Class<?>> classFilter, @Nullable Predicate<Method> methodFilter) throws IOException {
		return findMethods(basePackageToScan, null, classFilter, methodFilter);
	}

	@Nullable
	public static Class<?> loadClass(String className, ClassLoader loader) {
		try {
			return loader.loadClass(className);
		} catch (ClassNotFoundException ignore) {
			return null;
		}
	}

	public ClassLoader getClassLoader() {
		return getDefaultClassLoader();
	}

	public static ClassLoader getDefaultClassLoader() {
		return Thread.currentThread().getContextClassLoader();
	}

	public MetadataReaderFactory getOrCreateMetadataReaderFactory() {
		MetadataReaderFactory factory = this.metadataReaderFactory;
		if (factory == null) {
			this.metadataReaderFactory = factory = new CachingMetadataReaderFactory();
		}
		return factory;
	}

	public void setMetadataReaderFactory(MetadataReaderFactory metadataReaderFactory) {
		this.metadataReaderFactory = metadataReaderFactory;
	}

}
