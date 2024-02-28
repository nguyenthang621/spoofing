package com.istt.web.rest;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.istt.service.HealthService;

import lombok.extern.slf4j.Slf4j;

/**
 * Rest endpoint to configure SCTP Server configuration
 *
 * @author dinhtrung
 */
@RestController
@RequestMapping(value = "/api/public/health")
@Slf4j
public class HealthManagementResource {

	@Autowired
	HealthService healthService;

	/**
	 * List all defined associations in SCTP layer
	 *
	 * @param message
	 * @throws Exception
	 */
	@PatchMapping(value = "/up")
	public ResponseEntity<Void> bringUp() throws Exception {
		log.info("STARTING UP INPUT LINKS");
		healthService.bringupInputLinkIfStop();
		return ResponseEntity.accepted().body(null);
	}

	@PostMapping(value = "/down")
	public ResponseEntity<Void> shutDown() throws Exception {
		log.error("SHUTTING DOWN INPUT LINKS");
		healthService.shutdownInputLinks();
		return ResponseEntity.accepted().body(null);
	}
}
