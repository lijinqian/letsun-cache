/*
 * Copyright (c) 2017, Letsun and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.letsun.cache.aspect;

import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.Signature;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import com.letsun.cache.annotation.Cache;

import tcsyn.basic.util.JsonUtil;

/**
 * 
 * @author shiw
 * @date 2017年7月21日
 */
@Aspect
@Component
public class CacheAspect {
	
	final Logger logger = Logger.getLogger(getClass());
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Pointcut("execution(* com.wncsjt.qjb.service..*Service.*(..)) && @annotation(com.letsun.cache.annotation.Cache)")
	private void anyMethod() {}
	
	@Value("${system.code}")
	String systemCode;
	
	@Around("anyMethod()")
	public Object execute(ProceedingJoinPoint pjp) throws Throwable {
		try {
			Signature s = pjp.getSignature();
			String className = pjp.getTarget().getClass().getName();
			MethodSignature ms = (MethodSignature)s;
			Method method = ms.getMethod();
			Cache cache = method.getAnnotation(Cache.class);
			long expire = cache.expire();
			String cacheValue = cache.value();
			String methodName = method.getName();
			String cacheKey = this.getCacheKey(pjp, className, methodName, cacheValue);
		    
			ValueOperations<String, String> valueOperations = this.redisTemplate.opsForValue();
			String value = valueOperations.get(cacheKey);
			if (StringUtils.isNotBlank(value)) {
				return this.getCacheValue(ms, method, value);
			} else {
				Object obj = pjp.proceed();
				valueOperations.set(cacheKey, JsonUtil.toJson(obj));
				if (expire > 0) {
					this.redisTemplate.expire(cacheKey, expire, TimeUnit.SECONDS);
				}
				return obj;
			}
		} catch (Exception e) {
			logger.error(e.getMessage(), e);
			return pjp.proceed();
		}
	}

	/**
	 * 
	 * @param pjp
	 * @param className
	 * @param methodName
	 * @return
	 */
	private String getCacheKey(ProceedingJoinPoint pjp, String className, String methodName, String cacheValue) {
		String cacheKey = String.format("%s_%s:%s#%s", systemCode, cacheValue, className, methodName);
		String argsHashcode = this.getArgsHashcode(pjp);
		if (StringUtils.isNotBlank(argsHashcode)) {
			cacheKey += "_" + argsHashcode;
		}
		return cacheKey;
	}

	/**
	 * 
	 * @param ms
	 * @param method
	 * @param value
	 * @return
	 * @throws Exception
	 */
	private Object getCacheValue(MethodSignature ms, Method method, String value) throws Exception {
		Class<?> clazz = ms.getReturnType();
		if (!clazz.isAssignableFrom(List.class)) {
			return JsonUtil.fromJson(value, clazz);
		} else {
			Type Type = method.getGenericReturnType();
			if (Type instanceof ParameterizedType) {
		        Type[] typesto = ((ParameterizedType) Type).getActualTypeArguments();
		        return JsonUtil.fromJsonList(value, (Class<?>) typesto[0]);
		    } else {
		    	throw new RuntimeException("不能反序列化缓存值，请指定泛型类型");
		    }
		}
	}
	
	/**
	 * 
	 * @param pjp
	 * @param m
	 * @return
	 */
	private String getArgsHashcode(ProceedingJoinPoint pjp) {
		String key = "";
		if (pjp.getArgs().length > 0) {
			for(Object obj : pjp.getArgs()) {
				if (obj != null) {
					key += obj.hashCode();
				}
			}
		}
		return key;
	}
}
