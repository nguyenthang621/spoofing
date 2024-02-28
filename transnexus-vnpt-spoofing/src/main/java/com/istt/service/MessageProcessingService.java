package com.istt.service;

import java.util.Objects;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.zalando.problem.Problem;
import org.zalando.problem.ProblemBuilder;
import org.zalando.problem.Status;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.istt.config.ApplicationProperties;
import com.istt.domain.CallLog;
import com.istt.service.dto.ErrorMapping;

import lombok.Getter;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@Getter
public class MessageProcessingService {

	@Autowired
	ApplicationProperties props;

	@Autowired
	RestTemplate restTemplate;

	@Autowired
	ObjectMapper objectMapper;

	@Value("${spring.application.instance-id:spoofing}")
	String instanceID;

	/**
	 * Check VLR and IMSI information
	 *
	 * @param cdr
	 * @param vlr
	 * @param imsi
	 */
	public void checkVlrImsi(CallLog cdr, String vlr, String imsi) {
		if (vlr == null) {
			throw Problem.builder().withStatus(Status.BAD_GATEWAY).withDetail("Missing VLR information").build();
		} else if (imsi == null) {
			throw Problem.builder().withStatus(Status.BAD_GATEWAY).withDetail("Missing IMSI information").build();
		} else {
			cdr.setVlr(vlr);
			checkWhitelistedVlr(cdr, vlr);
			if (vlr.startsWith(props.getGtprefix()) && imsi.startsWith(props.getImsiprefix())) {
				throw Problem.builder().withStatus(Status.FORBIDDEN).withDetail("Forbidden VLR and IMSI")
						.with("vlr", vlr).with("imsi", imsi).build();
			}
		}
	}
	
	public void checkWhitelistedVlr(CallLog cdr, String vlr) {
		for (String wl : props.getVlrWhitelist()) {
			if (vlr.equalsIgnoreCase(wl)) {
				cdr.whitelisted(true);
				cdr.setState(1);
				throw Problem.builder().withStatus(Status.CREATED).withDetail("VLR is whitelisted").with("vlr", vlr).build();
			}
		}
	}

	/**
	 * Throw error based on checking
	 * 
	 * @param cdr
	 * @param vlr
	 * @param imsi
	 * @param msrn
	 */
	public void checkVlrImsiMsrn(CallLog cdr, String vlr, String imsi, String msrn) {
		if ((vlr != null) && (imsi != null)) {
			cdr.setVlr(vlr);
			checkWhitelistedVlr(cdr, vlr);
			if (vlr.startsWith(props.getGtprefix()) && imsi.toString().startsWith(props.getImsiprefix())) {
				throw Problem.builder().withStatus(Status.FORBIDDEN).withDetail("Forbidden VLR and IMSI")
						.with("vlr", vlr).with("imsi", imsi).build();
			}
		} else if (msrn != null) {
			lookupMnp(msrn.toString(), cdr);
		}
	}

	/**
	 * Send a request to remote MNP proxy curl
	 * http://localhost:1330/search?prefix=IMSI&phoneNumber=MSRN
	 * 
	 * @param msrn
	 * @param imsi
	 */
	public void lookupMnp(String msrn, CallLog cdr) {
		if (!msrn.startsWith("840")) {
			throw Problem.builder().withStatus(Status.BAD_REQUEST).withDetail("Invalid Mobile Station Roaming Number")
					.with("msrn", msrn).build();
		}
		String prefix = msrn.substring(0, 5);
		String msisdn = msrn.substring(0, 2) + msrn.substring(5);
		String uri = props.getMnpCallUrl() + "?prefix=" + prefix + "&phoneNumber=" + msisdn + "&callref="
				+ cdr.getCallref() + "&channel=MNP&instance=" + instanceID;
		restTemplate.getForEntity(uri, Object.class);
	}

	/**
	 * Check if remote GT should throw a problem
	 *
	 * @param hlrGt
	 */
	public void checkRemoteGT(String hlrGt) {
		if ((props.getHlrPrefix() == null) || props.getHlrPrefix().isEmpty()
				|| (props.getHlrPrefix().stream().filter(s -> Objects.nonNull(s) && !s.isEmpty()).count() == 0)) {
			log.error("Missing valid HLR prefixes");
			throw Problem.builder()
			.withStatus(Status.SERVICE_UNAVAILABLE)
			.withDetail("Missing valid HLR prefixes")
			.build();
		} else if (hlrGt == null) {
			throw Problem.builder().withStatus(Status.GATEWAY_TIMEOUT).withDetail("Gateway timeout").build();
		} else if (!props.getHlrPrefix().stream()
				.filter(s -> Objects.nonNull(s) && !s.isEmpty()).anyMatch(hlrGt::startsWith)) {
			log.error("Forbidden HLR {} {}", props.getHlrPrefix(), hlrGt);
			throw Problem.builder().withStatus(Status.SERVICE_UNAVAILABLE).withDetail("Forbidden HLR").with("gt", hlrGt)
					.build();
		}
	}
	/**
	 * Return an error based on mapping SRI errors
	 *
	 * @param errorMap
	 * @return
	 */
	public ProblemBuilder handleError(Problem errorMap, int statusCode) {
		int sriErrorCode = Optional.ofNullable(errorMap.getParameters().get("errorCode")).map(Object::toString).map(Integer::parseInt)
				.orElse(0);
		ErrorMapping mappedError = props.getMapHttpErrorMapping().stream().filter(i -> i.getErrorCode() == sriErrorCode)
				.findFirst().orElse(null);
		log.debug("Mapped error:{}", mappedError);
		ProblemBuilder builder = Problem.builder();
		errorMap.getParameters().forEach((k, v) -> builder.with(k, v));
		if (mappedError != null) {
			builder.withStatus(Status.valueOf(mappedError.getCauseCode())).withDetail(mappedError.getErrorDesc());
		} else {
			builder.withStatus(Status.valueOf(statusCode));
		}
		return builder;
	}

}
