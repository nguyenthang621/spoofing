package com.istt.service;

import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.zalando.problem.Problem;
import org.zalando.problem.Status;
import org.zalando.problem.ThrowableProblem;

import com.google.common.collect.Sets;
import com.istt.config.ApplicationProperties;
import com.istt.config.Constants;
import com.istt.domain.CallLog;
import com.istt.domain.RoutingRule;
import com.istt.repository.BlacklistRepository;
import com.istt.repository.CallLogRepository;
import com.istt.repository.RoutingRuleRepository;
import com.istt.repository.WhitelistRepository;
import com.istt.service.dto.ErrorMapping;
import com.istt.service.util.MessageUtil;

import io.micrometer.core.annotation.Timed;
import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class FraudManagementService {

	@Autowired
	ApplicationProperties props;

	@Autowired
	LicenseService licenceService;

	@Autowired
	MessageProcessingService msgService;

	@Autowired
	RoutingRuleRepository routingRuleRepository;

	@Autowired
	WhitelistRepository whitelistRepository;

	@Autowired
	BlacklistRepository blacklistRepository;

	@Autowired
	CallLogRepository cdrRepository;

	@Autowired
	AtiService atiService;

	@Autowired
	SriService sriService;

	@Autowired
	SriCallService sricallService;

	@Autowired
	RedisTemplate<String, String> redisTemplate;

	/**
	 * Wrapper function to perform dry run after response
	 *
	 * @param cdr
	 */
	@Async
	public void performDryrun(CallLog cdr) {
		cdr = performCallSpoofing(cdr);
		cdr.setDryRun(true);
		commit(cdr);
		cdr = cdrRepository.save(cdr);
		log.info("DryRun CDR: {}", cdr);
	}

	/**
	 * Main logic to handle traffic
	 *
	 * @param cdr
	 * @param channel
	 * @return
	 */
	public CallLog performCallSpoofing(CallLog cdr) {
		// licenceService.checkLicense();
		try {
			handlingCallSpoofing(cdr);
		} catch (ThrowableProblem p) {
			cdr.setErrorCode(p.getStatus().getStatusCode());
			cdr.setErrorDesc(p.getDetail());
		} catch (Exception e2) {
			log.error("Unknown error", e2);
			cdr.setErrorCode(500);
			cdr.setErrorDesc(Status.INTERNAL_SERVER_ERROR.getReasonPhrase());
		} finally {
			if (cdr.getErrorCode() == null)
				cdr.setErrorCode(100);
		}
		return cdr;
	}

	/**
	 * Call spoofing main logic
	 *
	 * @param calling
	 * @throws Exception
	 */
	@Timed(value = "inbound_call", description = "Total inbound call")
	public CallLog handlingCallSpoofing(CallLog cdr) {
		System.out.println("--------------------------Starting handle--------------------------");
		String calling = Optional.ofNullable(cdr.getCalling())
				.orElseThrow(() -> Problem.builder().withStatus(Status.EXPECTATION_FAILED)
						.withDetail("Invalid phone number").with("calling", cdr.getCalling()).build());
		Set<Long> callingPrefixes = MessageUtil.toPrefixSet(calling);
		// A2P Routing : Check Allowed A prefix
		RoutingRule route = lookupRoute(calling);
		System.out.println("--------------------------------lookup route done--------------------------");
		cdr.setRoute(route.getName());
		cdr.setRouteLength(route.getAlength());
		cdr.setRoutePrefix(route.getAprefix());
		System.out.println("--------------------------Check route done--------------------------");

		// First check whitelist if we should process on this or not
		Set<Integer> validLengths = Sets.newHashSet(0, calling.length());
		if (whitelistRepository.countByPrefixInAndStateAndLengthIn(callingPrefixes, 200, validLengths) > 0) {
			cdr.setWhitelisted(true);
			throw Problem.builder().withStatus(Status.CREATED).withDetail("Calling is whitelisted")
					.with("calling", calling).build();
		} else if (Optional.ofNullable(redisTemplate.opsForValue().get(Constants.BLACKLIST_PREFIX + calling))
				.map(Integer::parseInt).orElse(0) > props.getBlacklistCnt()) { // check dynamic blacklist on redis
																				// template
			cdr.setBlacklisted(true);
			throw Problem.builder().withStatus(Status.TOO_MANY_REQUESTS).withTitle("calling is temporary blacklisted")
					.with("calling", calling).build();
		} else if (blacklistRepository.countByPrefixInAndStateAndLengthIn(callingPrefixes, 200, validLengths) > 0) {
			cdr.setBlacklisted(true);
			throw Problem.builder().withStatus(Status.UNAUTHORIZED).withDetail("Calling is blacklisted")
					.with("calling", calling).build();
		}
		System.out.println("--------------------------start check ss7--------------------------");
		// External server lookup via SRICALL+SRI or SRI only
		if ((props.getQueryMode() & Constants.MODE_SRI_CALL) == Constants.MODE_SRI_CALL) {
			System.out.println("--------------------------MODE_SRI_CALL--------------------------");
			sricallService.sriCall(cdr);
		} else if ((props.getQueryMode() & Constants.MODE_SRI) == Constants.MODE_SRI) {
			System.out.println("--------------------------MODE_SRI--------------------------");
			sriService.sri(cdr);
		}
		// Lookup by ATI query
		if ((props.getQueryMode() & Constants.MODE_ATI) == Constants.MODE_ATI) {
			System.out.println("--------------------------MODE_ATI--------------------------");
			atiService.ati(cdr);
		}
		return cdr;
	}

	/**
	 * Return matching rule
	 *
	 * @param calling
	 * @return
	 */
	public RoutingRule lookupRoute(String calling) {
		Set<Long> callingPrefixes = MessageUtil.toPrefixSet(calling);
		System.out.println("--------------------------------lookup route--------------------------");
		List<RoutingRule> routes = routingRuleRepository.findAllByStateAndAprefixInAndAlengthIn(200, callingPrefixes,
				Arrays.asList(0, calling.length()));
		System.out.println("--------------------------------find in database--------------------------");
		return routes.stream()
				.max(Comparator.comparingInt(e -> (16 * (e.getAprefix() + "").length()) + calling.length()))
				.orElseThrow(() -> Problem.builder().withStatus(Status.UNPROCESSABLE_ENTITY)
						.withDetail("Route Not Found").build());
	}

	/**
	 * Assign Hangup cause code into cdr.state and Error desc based on CDR error
	 * code
	 *
	 * @param cdr
	 * @param channel
	 * @return
	 * @throws AgiException
	 */
	public CallLog commit(CallLog cdr) {
		int state = props.getErrorMap().stream().filter(i -> i.getErrorCode() == cdr.getErrorCode())
				.peek(j -> Optional.ofNullable(j.getErrorDesc()).ifPresent(s -> cdr.setErrorDesc(s)))
				.map(ErrorMapping::getCauseCode).findFirst().orElse(props.getDefaultCode());
		cdr.setState(state);
		Metrics.globalRegistry
				.counter("CDR", "state", Optional.ofNullable(cdr.getState()).map(Object::toString).orElse("UNKNOWN"),
						"error", Optional.ofNullable(cdr.getErrorCode()).map(Object::toString).orElse("UNKNOWN"))
				.increment();
		return cdr;
	}
}
