package com.istt.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.service.SriCallService;
import com.istt.ss7.dto.SriCallDTO;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import lombok.extern.slf4j.Slf4j;
import org.restcomm.protocols.ss7.map.api.MAPException;
import org.redisson.Redisson;
import org.redisson.api.RCountDownLatch;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

@RestController
@RequestMapping(value = "/api/public/sri-client")
@Slf4j
public class SriCallResource {

	@Autowired
	ApplicationProperties props;

	@Autowired
	SriCallService sriClientService;

	@Autowired
	Redisson redisson;

	@Autowired
	RedisTemplate<String, Serializable> serializableRedisTemplate;

	/**
	 * http://localhost:8081/api/public/sms-server/perform-sri-sm?msisdn=84936414498
	 *
	 * @param text
	 * @param to
	 * @param from
	 * @return Map SRI response from HLR
	 * @throws MAPException
	 * @throws IllegalArgumentException 
	 * @throws ThrowableProblem 
	 * @throws InterruptedException
	 * @throws JsonProcessingException
	 * @throws JsonMappingException
	 */
	@GetMapping(value = "/perform-sri")
	public ResponseEntity<SriCallDTO> performSRIForSM(@RequestParam String msisdn) throws MAPException, ThrowableProblem, IllegalArgumentException, InterruptedException {
		String cacheKey = SriCallService.SRI_CALL_CACHE + "_" + msisdn;
		SriCallDTO jsonMeta = (SriCallDTO) serializableRedisTemplate.opsForValue().get(cacheKey);
		if (jsonMeta != null) {
			return processJsonMeta(jsonMeta);
		} 
		return performSRIForSMNoCache(msisdn);
	}
	
	@DeleteMapping(value = "/perform-sri")
	public ResponseEntity<SriCallDTO> performSRIForSMNoCache(@RequestParam String msisdn) throws MAPException, ThrowableProblem, IllegalArgumentException, InterruptedException {
		String cacheKey = SriCallService.SRI_CALL_CACHE + "_" + msisdn;
		long invokeId = sriClientService.performSRIForSM(msisdn);
		RCountDownLatch latch = redisson.getCountDownLatch(Constants.TXN + invokeId);
			SriCallDTO jsonMeta;
			if (latch.await(props.getTimeout(), TimeUnit.SECONDS)) {
				jsonMeta = Optional.ofNullable(
						(SriCallDTO) serializableRedisTemplate.opsForValue().get(SriCallService.SRI_CALL_CACHE + invokeId))
						.orElseThrow(() -> Problem.builder()
								.withStatus(Status.GATEWAY_TIMEOUT)
								.withDetail("No response from data source")
								.build());
				serializableRedisTemplate.opsForValue().set(cacheKey, jsonMeta, props.getTtl(), TimeUnit.MILLISECONDS);
				latch.deleteAsync();
				return processJsonMeta(jsonMeta);
			} else {
				throw Problem.builder()
						.withStatus(Status.GATEWAY_TIMEOUT)
						.withDetail("No response from data source")
						.with("msisdn", msisdn)
						.build();
			}
	}

	private ResponseEntity<SriCallDTO> processJsonMeta(SriCallDTO payload) {
		try {
			String error = payload.getError();
			if (error != null) {
				return ResponseEntity.status(HttpStatus.NOT_EXTENDED).body(payload);
			}
			return ResponseEntity.ok(payload);
		} catch (Exception e1) {
			log.error("Cannot process data: {}", payload, e1);
			return ResponseEntity.status(HttpStatus.BAD_GATEWAY).body(payload);
		}
	}
}
