/*
 * Copyright (c) 2017, Letsun and/or its affiliates. All rights reserved.
 * Use, Copy is subject to authorized license.
 */
package com.letsun.cache;

/**
 * 
 * @author shiw
 * @date 2017年7月24日
 */
public interface CacheOperator {
	
	public void update(String key, Object value);
	
	public void remove(String key);
}
