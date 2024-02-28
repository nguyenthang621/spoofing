package com.istt.config;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.PostConstruct;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

import com.istt.service.dto.ErrorMapping;

import lombok.Data;

/**
 * Properties specific to Kafka Endpoint.
 *
 * <p>
 * Properties are configured in the {@code application.yml} file. See
 * {@link tech.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
@Data
public class ApplicationProperties {

	private final Logger log = LoggerFactory.getLogger(ApplicationProperties.class);

	/** license key API **/
	private String license;

	/** The call back URL to SS7-Connector module */
	private int queryMode = 2;

	private String sriUrl = "http://localhost:8089/api/public/sms-server/perform-sri-sm";

	private String atiUrl = "http://localhost:8089/api/public/ati-client/perform-ati";

	private String sriCallUrl = "http://localhost:8089/api/public/sri-client/perform-sri";

	private String mnpCallUrl = "http://localhost:1330/search";

	private List<ErrorMapping> errorMap = new ArrayList<>();

	/** Dry run mode */
	private boolean dryRun = false;

	/**
	 * Default code when perform SRI success but cannot determine if we should let
	 * them pass or not
	 */
	private int defaultCode = 2;

	/** Strip off all zero to E164 */
	private String mcc = "84";

	/** Dynamic blacklist threshold **/
	private int blacklistCnt = 5;

	/** GT Prefix */
	private String gtprefix = "84";

	private Set<String> hlrPrefix = new HashSet<>();

	private Set<String> vlrWhitelist = new HashSet<>();

	private String imsiprefix = "45201";

	private List<ErrorMapping> mapHttpErrorMapping = new ArrayList<>();

	/**
	 * Timeout for request in second
	 */
	// private int timeout = 0;
	private int timeout = 3;

	private String calledWhitelistPrefixes = "84910";

	@Value("${spring.application.instance-id:spoofing}")
	private String instanceID;

	@PostConstruct
	protected void init() {
		log.info(" == Application Reloaded: {}", this);
	}

}
