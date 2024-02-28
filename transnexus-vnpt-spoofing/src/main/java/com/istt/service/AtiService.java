package com.istt.service;

import java.time.Instant;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.HttpServerErrorException;
import org.springframework.web.client.ResourceAccessException;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.istt.config.ApplicationProperties;
import com.istt.domain.CallLog;
import com.istt.ss7.dto.AtiDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class AtiService {

	@Autowired
	ApplicationProperties props;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MessageProcessingService msgService;

	/**
	 * Perform SRI lookup, throw exception if need
	 *
	 * @param cdr
	 */
	public void ati(CallLog cdr) {
		// perform SRI lookup
		String uri = props.getAtiUrl() + "?msisdn=" + cdr.getCalling() + "&callref=" + cdr.getCallref()
				+ "&channel=ATI&instance=" + props.getInstanceID();
		log.debug("Request {}", uri);
		cdr.setSriAt(Instant.now());
		try {
			ResponseEntity<AtiDTO> response = restTemplate.getForEntity(uri, AtiDTO.class);
			AtiDTO dto = response.getBody();
			cdr.setSriRespAt(Instant.now());
			
			msgService.checkVlrImsi(cdr, dto.getVlr(), dto.getImsi());
		} catch (ResourceAccessException e) {
			log.debug("Unable to retrieve request in time");
			throw Problem.builder().withStatus(Status.GATEWAY_TIMEOUT).withDetail("Unable to retrieve result").build();
		} catch (HttpServerErrorException | HttpClientErrorException e) {
			cdr.setSriRespAt(Instant.now());
			String sriResp = e.getResponseBodyAsString();
			log.debug("HttpStatusCodeException: {} | {}", e.getStatusCode().getReasonPhrase(), sriResp);
			try {
				Problem errorMap = objectMapper.readValue(sriResp, Problem.class);
				log.debug("errorMap: {}", errorMap);
				String hlrGT = errorMap.getParameters().getOrDefault("remoteGT", "x").toString();
				msgService.checkRemoteGT(hlrGT);
				ProblemBuilder builder = msgService.handleError(errorMap, e.getRawStatusCode());
				throw builder.build();
			} catch (JsonProcessingException e1) {
				log.error("Failed to parse error code", e1);
				throw Problem.builder().withStatus(Status.BAD_GATEWAY).withDetail("HLR return invalid response").build();
			}
		}
	}

	
}
