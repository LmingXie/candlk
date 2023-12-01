package com.candlk.common.model;

import java.lang.annotation.*;

/**
 * 应用配置标识。标识 哪些 属于应用配置，用于在新建应用项目时，指示哪些配置需要同步调整
 */
@Target({ ElementType.TYPE, ElementType.FIELD, ElementType.METHOD, ElementType.ANNOTATION_TYPE, ElementType.CONSTRUCTOR, ElementType.TYPE_PARAMETER })
@Retention(RetentionPolicy.SOURCE)
@Documented
public @interface AppConfig {

}
