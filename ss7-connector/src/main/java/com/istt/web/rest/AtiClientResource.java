package com.istt.web.rest;

import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.config.app.AtiClientConfiguration;
import com.istt.service.AtiClientService;
import com.istt.ss7.dto.AtiDTO;

import java.io.IOException;
import java.io.Serializable;
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
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;

@RestController
@RequestMapping(value = "/api/public/ati-client")
public class AtiClientResource {

  private final Logger log = LoggerFactory.getLogger(AtiClientResource.class);

  @Autowired ApplicationProperties props;

  @Autowired AtiClientService atiClientService;

  @Autowired Redisson redisson;

  @Autowired RedisTemplate<String, Serializable> serializableRedisTemplate;

  /**
   * http://localhost:8081/api/public/ati-client/perform-ati?msisdn=MSISDN
   *
   * @param text
   * @param to
   * @param from
   * @throws IOException
   * @throws JsonMappingException
   * @throws JsonParseException
   * @throws InterruptedException
   * @throws MAPException
   */
  @GetMapping(value = "/perform-ati")
  public ResponseEntity<AtiDTO> performAti(@RequestParam String msisdn) throws MAPException, InterruptedException {
    AtiDTO jsonMeta = (AtiDTO) serializableRedisTemplate.opsForValue().get(AtiClientService.ATI_CACHE + "_" + msisdn);
    if (jsonMeta != null) {
    	return ResponseEntity.ok(jsonMeta);
    }
    return performAtiNoCache(msisdn);
  }

  @DeleteMapping(value = "/perform-ati")
  public ResponseEntity<AtiDTO> performAtiNoCache(@RequestParam String msisdn) throws MAPException, InterruptedException {
	  long invokeId = atiClientService.performAtiRequest(msisdn, null);
      RCountDownLatch latch = redisson.getCountDownLatch(Constants.TXN + invokeId);
      if (latch.await(props.getTimeout(), TimeUnit.SECONDS)) {
        AtiDTO jsonMeta = (AtiDTO) serializableRedisTemplate.opsForValue().get(AtiClientService.ATI_CACHE + invokeId);
        serializableRedisTemplate.opsForValue().set(AtiClientService.ATI_CACHE + "_" + msisdn, jsonMeta, props.getTtl(), TimeUnit.MILLISECONDS);
        latch.deleteAsync();
        return ResponseEntity.ok(jsonMeta);
      }
      throw Problem.builder().withTitle("request timeout").withStatus(Status.GATEWAY_TIMEOUT).withDetail("cannot wait for response from ATI client").with("msisdn", msisdn).build();
  }

  @PostMapping(value = "/perform-ati")
  public ResponseEntity<AtiDTO> performAtiCustom(@RequestParam String msisdn, @RequestBody AtiClientConfiguration override) throws MAPException, InterruptedException {
    log.info("Perform ATI Request for MSISDN {}", msisdn);
    String cacheKey = AtiClientService.ATI_CACHE + "_" + msisdn;
    AtiDTO jsonMeta = (AtiDTO) serializableRedisTemplate.opsForValue().get(cacheKey);
    if (jsonMeta != null) {
    	return ResponseEntity.ok(jsonMeta);
    } else {
    	long invokeId = atiClientService.performAtiRequest(msisdn, override);
        RCountDownLatch latch = redisson.getCountDownLatch(Constants.TXN + invokeId);
        if (latch.await(props.getTimeout(), TimeUnit.SECONDS)) {
          jsonMeta = (AtiDTO) serializableRedisTemplate.opsForValue().get(AtiClientService.ATI_CACHE + invokeId);
          serializableRedisTemplate.opsForValue().set(cacheKey, jsonMeta, props.getTtl(), TimeUnit.MILLISECONDS);
          return ResponseEntity.ok(jsonMeta);
        }
        throw Problem.builder().withTitle("request timeout").withStatus(Status.GATEWAY_TIMEOUT).withDetail("cannot wait for response from ATI client").with("msisdn", msisdn).build();
    }

  }


  @PutMapping(value = "/perform-ati")
  public ResponseEntity<AtiDTO> performAtiNoCache(@RequestParam String msisdn, @RequestBody AtiClientConfiguration override) throws MAPException, InterruptedException
  {
    log.info("Perform ATI Request for MSISDN {}", msisdn);
	long invokeId = atiClientService.performAtiRequest(msisdn, override);
    RCountDownLatch latch = redisson.getCountDownLatch(Constants.TXN + invokeId);
    if (latch.await(props.getTimeout(), TimeUnit.SECONDS)) {
      AtiDTO jsonMeta = (AtiDTO) serializableRedisTemplate.opsForValue().get(AtiClientService.ATI_CACHE + invokeId);
      return ResponseEntity.ok(jsonMeta);
    }
    throw new ResponseStatusException(
        HttpStatus.GATEWAY_TIMEOUT, "Cannot wait for response from ATI Client");

  }
}
