package com.istt.inboundtraffic.modal;

import org.json.JSONObject;

public class CallLog {

	private String id;
	private String calling;
	private String called;
	private String createdAt;
	private String requestAt;
	private String responseAt;
	private int state;
	private String route;
	private String routePrefix;
	private Integer routeLength;
	private boolean dryRun;
	private Boolean whitelisted;
	private Boolean blacklisted;
	private int errorCode;
	private String errorDesc;
	private String vlr;
	private String sriAt;
	private String sriRespAt;
	private String peerIp;
	private String channel;
	private String callref;
	private String callId;

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public String getCalling() {
		return calling;
	}

	public void setCalling(String calling) {
		this.calling = calling;
	}

	public String getCalled() {
		return called;
	}

	public void setCalled(String called) {
		this.called = called;
	}

	public String getCreatedAt() {
		return createdAt;
	}

	public void setCreatedAt(String createdAt) {
		this.createdAt = createdAt;
	}

	public String getRequestAt() {
		return requestAt;
	}

	public void setRequestAt(String requestAt) {
		this.requestAt = requestAt;
	}

	public String getResponseAt() {
		return responseAt;
	}

	public void setResponseAt(String responseAt) {
		this.responseAt = responseAt;
	}

	public int getState() {
		return state;
	}

	public void setState(int state) {
		this.state = state;
	}

	public String getRoute() {
		return route;
	}

	public void setRoute(String route) {
		this.route = route;
	}

	public String getRoutePrefix() {
		return routePrefix;
	}

	public void setRoutePrefix(String routePrefix) {
		this.routePrefix = routePrefix;
	}

	public Integer getRouteLength() {
		return routeLength;
	}

	public void setRouteLength(Integer routeLength) {
		this.routeLength = routeLength;
	}

	public boolean isDryRun() {
		return dryRun;
	}

	public void setDryRun(boolean dryRun) {
		this.dryRun = dryRun;
	}

	public Boolean getWhitelisted() {
		return whitelisted;
	}

	public void setWhitelisted(Boolean whitelisted) {
		this.whitelisted = whitelisted;
	}

	public Boolean getBlacklisted() {
		return blacklisted;
	}

	public void setBlacklisted(Boolean blacklisted) {
		this.blacklisted = blacklisted;
	}

	public int getErrorCode() {
		return errorCode;
	}

	public void setErrorCode(int errorCode) {
		this.errorCode = errorCode;
	}

	public String getErrorDesc() {
		return errorDesc;
	}

	public void setErrorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
	}

	public String getVlr() {
		return vlr;
	}

	public void setVlr(String vlr) {
		this.vlr = vlr;
	}

	public String getSriAt() {
		return sriAt;
	}

	public void setSriAt(String sriAt) {
		this.sriAt = sriAt;
	}

	public String getSriRespAt() {
		return sriRespAt;
	}

	public void setSriRespAt(String sriRespAt) {
		this.sriRespAt = sriRespAt;
	}

	public String getPeerIp() {
		return peerIp;
	}

	public void setPeerIp(String peerIp) {
		this.peerIp = peerIp;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getCallref() {
		return callref;
	}

	public void setCallref(String callref) {
		this.callref = callref;
	}

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

	public CallLog(String id, String calling, String called, String createdAt, String requestAt, String responseAt,
			int state, String route, String routePrefix, Integer routeLength, boolean dryRun, Boolean whitelisted,
			Boolean blacklisted, int errorCode, String errorDesc, String vlr, String sriAt, String sriRespAt,
			String peerIp, String channel, String callref, String callId) {
		this.id = id;
		this.calling = calling;
		this.called = called;
		this.createdAt = createdAt;
		this.requestAt = requestAt;
		this.responseAt = responseAt;
		this.state = state;
		this.route = route;
		this.routePrefix = routePrefix;
		this.routeLength = routeLength;
		this.dryRun = dryRun;
		this.whitelisted = whitelisted;
		this.blacklisted = blacklisted;
		this.errorCode = errorCode;
		this.errorDesc = errorDesc;
		this.vlr = vlr;
		this.sriAt = sriAt;
		this.sriRespAt = sriRespAt;
		this.peerIp = peerIp;
		this.channel = channel;
		this.callref = callref;
		this.callId = callId;
	}



	public static CallLog fromJson(String json) {
		JSONObject jsonObject = new JSONObject(json);

	
		return new CallLog(jsonObject.optString("id"), jsonObject.optString("calling"), jsonObject.optString("called"),
				jsonObject.optString("createdAt"), jsonObject.optString("requestAt"),
				jsonObject.optString("responseAt"), jsonObject.optInt("state"), jsonObject.optString("route"),
				jsonObject.optString("routePrefix"),
				jsonObject.has("routeLength") && !jsonObject.isNull("routeLength") ? jsonObject.getInt("routeLength")
						: null,
				jsonObject.optBoolean("dryRun"),
				jsonObject.has("whitelisted") && !jsonObject.isNull("whitelisted")
						? jsonObject.getBoolean("whitelisted")
						: false,
				jsonObject.has("blacklisted") && !jsonObject.isNull("blacklisted")
						? jsonObject.getBoolean("blacklisted")
						: false,
				jsonObject.optInt("errorCode"), jsonObject.optString("errorDesc"), jsonObject.optString("vlr"),
				jsonObject.optString("sriAt"), jsonObject.optString("sriRespAt"), jsonObject.optString("peerIp"),
				jsonObject.optString("channel"), jsonObject.optString("callref"), jsonObject.optString("callId"));
	}

}
