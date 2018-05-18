/*
 * Copyright (c) 2017, Letsun and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.letsun.cache.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 
 * @author shiw
 * @date 2017年7月21日
 */
@Target({ElementType.METHOD})
@Retention(RetentionPolicy.RUNTIME)
public @interface Cache {
	
	/**
	 * 缓存的值类型，如“country”（国家），“city”（城市）
	 * @return
	 */
	abstract String value();
	
	/**
	 * 有效期，单位：秒，默认永久
	 * @return
	 */
	long expire() default 0;
}
