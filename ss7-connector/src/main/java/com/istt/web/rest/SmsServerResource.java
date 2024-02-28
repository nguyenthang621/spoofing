package com.istt.web.rest;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.config.app.SmsServerConfiguration;
import com.istt.service.SmsServerService;
import com.istt.ss7.dto.SriSMDTO;

import java.io.Serializable;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.mobicents.protocols.ss7.map.api.MAPException;
import org.redisson.Redisson;
import org.redisson.api.RCountDownLatch;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
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
@RequestMapping(value = "/api/public/sms-server")
public class SmsServerResource {

	private final Logger log = LoggerFactory.getLogger(SmsServerResource.class);

	@Autowired
	ApplicationProperties props;

	@Autowired
	SmsServerService smsServerService;

	@Autowired
	SmsServerConfiguration smsServerProps;

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
	@GetMapping(value = "/perform-sri-sm")
	public ResponseEntity<SriSMDTO> performSRIForSM(@RequestParam String msisdn) throws MAPException, ThrowableProblem, IllegalArgumentException, InterruptedException {
		String cacheKey = SmsServerService.SRI_CACHE + "_" + msisdn;
		SriSMDTO jsonMeta = (SriSMDTO) serializableRedisTemplate.opsForValue().get(cacheKey);
		if (jsonMeta != null) {
			return processJsonMeta(jsonMeta);
		}
		return performSRIForSMNoCache(msisdn);
	}

	@DeleteMapping(value = "/perform-sri-sm")
	public ResponseEntity<SriSMDTO> performSRIForSMNoCache(@RequestParam String msisdn) throws MAPException, ThrowableProblem, IllegalArgumentException, InterruptedException {
		String cacheKey = SmsServerService.SRI_CACHE + "_" + msisdn;
		long invokeId = smsServerService.performSRIForSM(msisdn);
		RCountDownLatch latch = redisson.getCountDownLatch(Constants.TXN + invokeId);
		if (latch.await(props.getTimeout(), TimeUnit.SECONDS)) {
			SriSMDTO jsonMeta = Optional.ofNullable(
					(SriSMDTO) serializableRedisTemplate.opsForValue().get(SmsServerService.SRI_CACHE + invokeId))
					.orElseThrow(() -> Problem.builder().withStatus(Status.GATEWAY_TIMEOUT)
							.withDetail("No response from data source").build());
			serializableRedisTemplate.opsForValue().set(cacheKey, jsonMeta, props.getTtl(), TimeUnit.MILLISECONDS);
			latch.deleteAsync();
			return processJsonMeta(jsonMeta);
		} else {
			throw Problem.builder().withStatus(Status.GATEWAY_TIMEOUT).withDetail("Request timeout")
					.with("msisdn", msisdn).withTitle("SRI_SM Timeout").build();
		}
	}

	/**
	 * Read a JSON Meta string and compose the error
	 * 
	 * @param jsonMeta
	 * @return
	 */
	private ResponseEntity<SriSMDTO> processJsonMeta(SriSMDTO payload) {
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
