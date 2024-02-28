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
import com.istt.ss7.dto.SriCallDTO;

import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class SriCallService {

	@Autowired
	ApplicationProperties props;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@Autowired
	MessageProcessingService msgService;

	@Autowired
	SriService sriService;

	/**
	 * Perform SRI Call lookup, throw exception if need EX: curl -vv
	 * 'http://localhost:8089/api/public/sri-client/perform-sri?msisdn=84914628252'
	 * {"msrn":"84910245209","imsi":"452021061624044","msc":"8491020466","vlr":"8491020466"}
	 * {"msrn":"84004977014185","imsi":"45200"}
	 * {"msrn":"84001936414498","imsi":"45200"}
	 * {"errorCode":"13","error":"operatorBarring","remoteGT":"8491020303"}
	 *
	 * @param cdr
	 */
	public void sriCall(CallLog cdr) {
		// perform SRI lookup
        System.out.println("--------------------------perform SRICALL lookup--------------------------");
		String uri = props.getSriCallUrl() + "?msisdn=" + cdr.getCalling() + "&callref=" + cdr.getCallref()
				+ "&channel=SRI&instance=" + props.getInstanceID();
		log.debug("Request {}", uri);
		cdr.setSriAt(Instant.now());
		try {
			ResponseEntity<SriCallDTO> response = restTemplate.getForEntity(uri, SriCallDTO.class);
			SriCallDTO dto = response.getBody();
			cdr.setSriRespAt(Instant.now());
			System.out.println("--------------------------check api done--------------------------");
			msgService.checkVlrImsiMsrn(cdr, dto.getVlr(), dto.getImsi(), dto.getMsrn());
			System.out.println("--------------------------checkVlrImsiMsrn done--------------------------");
		} catch (ResourceAccessException e) {
			log.debug("Unable to retrieve request in time");
			throw Problem.builder().withStatus(Status.GATEWAY_TIMEOUT).withDetail("Unable to retrieve result").build();
		} catch (HttpServerErrorException e) {
			log.debug("{}: {} | {}", e.getClass().getCanonicalName(), e.getStatusCode().getReasonPhrase(),
					e.getResponseBodyAsString());
			log.debug("HLR return error for SRI Call, perform fallback to SRI");
			sriService.sri(cdr);
		} catch (HttpClientErrorException e) {
			cdr.setSriRespAt(Instant.now());
			String sriResp = e.getResponseBodyAsString();
			log.debug("{}: {} | {}", e.getClass().getCanonicalName(), e.getStatusCode().getReasonPhrase(), sriResp);
			try {
				Problem errorMap = objectMapper.readValue(sriResp, Problem.class);
				log.debug("errorMap: {}", errorMap);
				ProblemBuilder builder = msgService.handleError(errorMap, e.getRawStatusCode());
				throw builder.build();
			} catch (JsonProcessingException e1) {
				log.error("Failed to parse error code", e1);
				throw Problem.builder().withStatus(Status.BAD_GATEWAY).withDetail("HLR return invalid response")
						.build();
			}
		}
	}
}
