package com.bojiu.context.auth;

import java.lang.annotation.*;

/**
 * 与商户无关的
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface WithoutMerchant {

}
