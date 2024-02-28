package com.istt.inboundtraffic.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istt.inboundtraffic.modal.CacheData;

@Service
public class CacheService {

	private final RedisTemplate<String, String> redisTemplate;
	private final ObjectMapper objectMapper;

	@Autowired
	public CacheService(RedisTemplate<String, String> redisTemplate, ObjectMapper objectMapper) {
		this.redisTemplate = redisTemplate;
		this.objectMapper = objectMapper;
	}

	public void cacheData(String key, CacheData cacheData) throws JsonProcessingException {
		String json = objectMapper.writeValueAsString(cacheData);
		redisTemplate.opsForValue().set(key, json);
	}

	public CacheData retrieveData(String key) throws JsonProcessingException {
		String json = redisTemplate.opsForValue().get(key);
		return objectMapper.readValue(json, CacheData.class);
	}
}
