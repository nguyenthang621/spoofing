package com.istt.service;

import org.mobicents.protocols.api.Association;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import com.istt.config.ApplicationProperties;
import com.istt.config.ss7.M3UAConfiguration;
import com.istt.config.ss7.SCTPConfiguration;

import lombok.extern.slf4j.Slf4j;

@Service
@EnableScheduling
@Slf4j
public class HealthService {

	@Autowired
	ApplicationProperties props;

	/**
	 * Automatically monitoring links
	 */
	@Scheduled(fixedRate = 15000)
	public void adjustGauges() {
		SCTPConfiguration.sctpManagement.getAssociations().entrySet().forEach(entry -> {
			String assocName = entry.getKey();
			Association assoc = entry.getValue();
			int state = 0;
			if (assoc.isStarted())
				state = state | 1;
			if (assoc.isConnected())
				state = state | 2;
			if (assoc.isUp())
				state = state | 4;
			SCTPConfiguration.gauges.get(assocName).lazySet(state);
			;
		});
	}

	/**
	 * Bring Up Input Link
	 */
	public void bringupInputLinkIfStop() {
		for (String inputAsp : props.getInputAsp()) {
			log.warn("starting up INPUT ASP: {}", inputAsp);
			try {
				M3UAConfiguration.clientM3UAMgmt.startAsp(inputAsp);
				log.info("ASP {} started", inputAsp);
			} catch (Exception e) {
				log.error("Cannot start ASP: {}", inputAsp, e);
			}
		}
	}

	/**
	 * Shutdown Input Link try to shutdown all input links
	 */
	public void shutdownInputLinks() {
		for (String inputAsp : props.getInputAsp()) {
			log.warn("shutting down INPUT ASP: {}", inputAsp);
			try {
				M3UAConfiguration.clientM3UAMgmt.stopAsp(inputAsp);
				log.error("ASP {} shut down", inputAsp);
			} catch (Exception e) {
				log.error("Cannot Stop ASP: {}", inputAsp, e);
			}
		}
	}
}
