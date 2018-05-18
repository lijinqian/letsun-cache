/*
 * Copyright (c) 2017, Letsun and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.letsun.cache;

import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

/**
 * 
 * @author shiw
 * @date 2017年7月24日
 */
@Component
public class RedisCacheOperator implements CacheOperator {
	
	@Autowired
	RedisTemplate<String, String> redisTemplate;
	
	@Value("${system.code}")
	String systemCode;
	
	@Override
	public void update(String key, Object value) {
		
	}

	@Override
	public void remove(String key) {
		Set<String> set = this.redisTemplate.keys(String.format("%s_%s:*", systemCode, key));
		this.redisTemplate.delete(set);
	}

}
