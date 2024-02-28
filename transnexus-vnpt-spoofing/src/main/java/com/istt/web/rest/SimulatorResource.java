package com.istt.web.rest;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.zalando.problem.Status;

import com.istt.config.ApplicationProperties;
import com.istt.domain.CallLog;
import com.istt.domain.RoutingRule;
import com.istt.modal.CdrResponse;
import com.istt.repository.CallLogRepository;
import com.istt.service.FraudManagementService;

/** REST controller for managing {@link com.ft.domain.routingRule}. */
@RestController
@RequestMapping("/api")
public class SimulatorResource {

	private final Logger log = LoggerFactory.getLogger(SimulatorResource.class);

	/** Look up system routes */
	@Autowired
	FraudManagementService fraudSvc;

	@Autowired
	ApplicationProperties props;

	@Autowired
	CallLogRepository cdrRepository;

	@GetMapping("/lookup-route")
	public ResponseEntity<RoutingRule> getBestRoute(@RequestParam String msisdn) {
		return ResponseEntity.ok(fraudSvc.lookupRoute(msisdn));
	}

	@PostMapping("/lookup-result")
	public ResponseEntity<CallLog> fullSimulator(@RequestBody CallLog cdr) {
		// CallLog response = fraudSvc.handlingCallSpoofing(cdr);
		fraudSvc.handlingCallSpoofing(cdr);
		return ResponseEntity.ok().body(cdr);
	}

	@GetMapping("/public/ping")
	public ResponseEntity<String> ping() {
		return ResponseEntity.ok("true");
	}

	/**
	 * Do the same but record in CDR
	 *
	 * @param calling
	 * @param called
	 * @return
	 */
	@GetMapping("/public/perform-spoofing")
	public ResponseEntity<CallLog> performCallSpoofing(@RequestParam String calling, @RequestParam String called,
			@RequestParam(required = false) String callref,
			@RequestParam(required = false, defaultValue = "API") String channel,
			@RequestParam(required = false, name = "peer") String peerIp) {
		callref = Optional.ofNullable(callref).orElse(UUID.randomUUID().toString());
		CallLog cdr = new CallLog().dryRun(props.isDryRun()).calling(calling).called(called).channel(channel)
				.callref(callref).peerIp(peerIp);
		// 8491,8492,8493 -> Set<String> 8491,8492,8493
		if (StringUtils.commaDelimitedListToSet(props.getCalledWhitelistPrefixes()).stream()
				.anyMatch(called::startsWith)) {
			cdr.setErrorCode(Status.ACCEPTED.getStatusCode());
			cdr.setErrorDesc("Whitelisted B number");
			cdr.setState(1);
		} else {
			fraudSvc.performCallSpoofing(cdr);
		}
		fraudSvc.commit(cdr);
		cdr.responseAt(Instant.now());
		cdr = cdrRepository.save(cdr);
		log.info("+ CDR: {}", cdr);

		return (Optional.ofNullable(cdr.getDryRun()).orElse(false) || cdr.getState() == 1)
				? ResponseEntity.ok().body(cdr)
				: ResponseEntity.badRequest().body(cdr);
	}

	/**
	 * Do the same but record in CDR
	 *
	 * @param calling
	 * @param called
	 * @return
	 */
	@GetMapping("/public/perform-spoofing-replication1")
	public ResponseEntity<CdrResponse> performCallSpoofingReplication1(@RequestParam String calling,
			@RequestParam String called, @RequestParam String callid, @RequestParam(required = false) String callref,
			@RequestParam(required = false, defaultValue = "API") String channel,
			@RequestParam(required = false, name = "peer") String peerIp) {
		callref = Optional.ofNullable(callref).orElse(UUID.randomUUID().toString());

		CdrResponse cdr = (CdrResponse) new CdrResponse().dryRun(props.isDryRun()).calling(calling).called(called)
				.channel(channel).callref(callref).peerIp(peerIp);
		cdr.setCallId(callid);

		fraudSvc.performCallSpoofing(cdr);
//		fraudSvc.commit(cdr);
		cdr.responseAt(Instant.now());
//		cdr = cdrRepository.save(cdr);
//		log.info("+ CDR: {}", cdr);
//		fraudSvc.handlingCallSpoofing(cdr);

//		return (Optional.ofNullable(cdr.getDryRun()).orElse(false) || cdr.getState() == 1)
//				? ResponseEntity.ok().body(cdr)
//				: ResponseEntity.badRequest().body(cdr);
		return ResponseEntity.ok().body(cdr);
	}

	@GetMapping("/public/perform-spoofing-replication2")
	public ResponseEntity<CdrResponse> performCallSpoofingReplication2(@RequestParam String calling,
			@RequestParam String called, @RequestParam String callid, @RequestParam(required = false) String callref,
			@RequestParam(required = false, defaultValue = "API") String channel,
			@RequestParam(required = false, name = "peer") String peerIp) {
		callref = Optional.ofNullable(callref).orElse(UUID.randomUUID().toString());

		CdrResponse cdr = (CdrResponse) new CdrResponse().dryRun(props.isDryRun()).calling(calling).called(called)
				.channel(channel).callref(callref).peerIp(peerIp);
		cdr.setCallId(callid);

		fraudSvc.performCallSpoofing(cdr);
//		fraudSvc.commit(cdr);
		cdr.responseAt(Instant.now());
//		cdr = cdrRepository.save(cdr);
//		log.info("+ CDR: {}", cdr);
//		fraudSvc.handlingCallSpoofing(cdr);

//		return (Optional.ofNullable(cdr.getDryRun()).orElse(false) || cdr.getState() == 1)
//				? ResponseEntity.ok().body(cdr)
//				: ResponseEntity.badRequest().body(cdr);

		return ResponseEntity.ok().body(cdr);
	}

	@GetMapping("/public/perform-spoofing-replication3")
	public ResponseEntity<CdrResponse> performCallSpoofingReplication3(@RequestParam String calling,
			@RequestParam String called, @RequestParam String callid, @RequestParam(required = false) String callref,
			@RequestParam(required = false, defaultValue = "API") String channel,
			@RequestParam(required = false, name = "peer") String peerIp) {
		callref = Optional.ofNullable(callref).orElse(UUID.randomUUID().toString());

		CdrResponse cdr = (CdrResponse) new CdrResponse().dryRun(props.isDryRun()).calling(calling).called(called)
				.channel(channel).callref(callref).peerIp(peerIp);
		cdr.setCallId(callid);

		fraudSvc.performCallSpoofing(cdr);
//		fraudSvc.commit(cdr);
		cdr.responseAt(Instant.now());
//		cdr = cdrRepository.save(cdr);
//		log.info("+ CDR: {}", cdr);
//		fraudSvc.handlingCallSpoofing(cdr);

//		return (Optional.ofNullable(cdr.getDryRun()).orElse(false) || cdr.getState() == 1)
//				? ResponseEntity.ok().body(cdr)
//				: ResponseEntity.badRequest().body(cdr);
		return ResponseEntity.ok().body(cdr);
	}

}
