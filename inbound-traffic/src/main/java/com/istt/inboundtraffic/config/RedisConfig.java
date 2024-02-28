package com.istt.inboundtraffic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

@Configuration
public class RedisConfig {

//	@Bean
//	public RedisTemplate<Object, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
//
//		RedisTemplate<Object, Object> template = new RedisTemplate<>();
//		template.setConnectionFactory(connectionFactory);
//		// Add some specific configuration such as key serializers, etc.
//		return template;
//	}

	@Bean
	public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory connectionFactory) {
		RedisTemplate<String, Object> template = new RedisTemplate<>();
		template.setConnectionFactory(connectionFactory);
		template.setValueSerializer(new GenericToStringSerializer<>(Object.class));
		return template;
	}
}