package com.istt.inboundtraffic.service;

import java.util.List;

public final class Constants {

	public static final String host = "192.168.10.44";

	public static final int port = 5060;

	public static final String logfiles = "/home/rnd/datalogs/sipphonelogs.log";

	public static final String debugfiles = "/home/rnd/datalogs/sipphonedebug.log";

	public static final String api_spoofing = "http://192.168.10.44:8087/api/public/perform-spoofing";

	public static final String api_ping = "http://192.168.10.44:8087/api/whitelists";

	public static final List<String> IGNORE_STATUS_CODES = List.of("410", "429", "401", "403", "409", "417", "508",
			"507", "505", "500", "510");

	public static final int timeoutPetchAPI = 1000;

	public static final int replicationsAPI = 3;

	public static String getDebugfiles() {
		return debugfiles;
	}

	public static int getReplicationsapi() {
		return replicationsAPI;
	}

	public static int getTimeoutpetchapi() {
		return timeoutPetchAPI;
	}

	public static String getApiPing() {
		return api_ping;
	}

	public static String getHost() {
		return host;
	}

	public static int getPort() {
		return port;
	}

	public static String getLogfiles() {
		return logfiles;
	}

	public static String getApiSpoofing() {
		return api_spoofing;
	}

	public static List<String> getIgnoreStatusCodes() {
		return IGNORE_STATUS_CODES;
	}
}