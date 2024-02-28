package com.istt.domain;

import java.time.Instant;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.validation.constraints.NotNull;

import lombok.Data;

/** A CallLog. */
@Entity
@Table(name = "call_log")
@Data
public class CallLog  {

	@Id
	@GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
	@SequenceGenerator(name = "sequenceGenerator")
	private Long id;

	@NotNull
	@Column(name = "calling", nullable = false)
	private String calling;

	@NotNull
	@Column(name = "called", nullable = false)
	private String called;

	@NotNull
	@Column(name = "created_at", nullable = false)
	private Instant createdAt = Instant.now();

	@NotNull
	@Column(name = "request_at", nullable = false)
	private Instant requestAt = Instant.now();

	@Column(name = "response_at")
	private Instant responseAt = Instant.now();

	@NotNull
	@Column(name = "state", nullable = false)
	private Integer state = 200;

	@Column(name = "route")
	private String route;

	@Column(name = "route_prefix")
	private Long routePrefix;

	@Column(name = "route_length")
	private Integer routeLength;

	@Column(name = "dry_run")
	private Boolean dryRun;

	@Column(name = "whitelisted")
	private Boolean whitelisted;

	@Column(name = "blacklisted")
	private Boolean blacklisted;

	@Column(name = "error_code")
	private Integer errorCode;

	@Column(name = "error_desc")
	private String errorDesc;

	@Column(name = "vlr")
	private String vlr;

	@Column(name = "sri_at")
	private Instant sriAt;

	@Column(name = "sri_resp_at")
	private Instant sriRespAt;

	@Column(name = "peer_ip")
	private String peerIp;

	@Column(name = "channel")
	private String channel;
	
	@Column(name = "callref")
	private String callref;

	public CallLog calling(String calling) {
		this.calling = calling;
		return this;
	}

	public CallLog called(String called) {
		this.called = called;
		return this;
	}

	public CallLog createdAt(Instant createdAt) {
		this.createdAt = createdAt;
		return this;
	}

	public CallLog requestAt(Instant requestAt) {
		this.requestAt = requestAt;
		return this;
	}

	public CallLog responseAt(Instant responseAt) {
		this.responseAt = responseAt;
		return this;
	}

	public CallLog state(Integer state) {
		this.state = state;
		return this;
	}

	public CallLog route(String route) {
		this.route = route;
		return this;
	}

	public CallLog routePrefix(Long routePrefix) {
		this.routePrefix = routePrefix;
		return this;
	}

	public CallLog routeLength(Integer routeLength) {
		this.routeLength = routeLength;
		return this;
	}

	public CallLog dryRun(Boolean dryRun) {
		this.dryRun = dryRun;
		return this;
	}

	public CallLog whitelisted(Boolean whitelisted) {
		this.whitelisted = whitelisted;
		return this;
	}

	public CallLog blacklisted(Boolean blacklisted) {
		this.blacklisted = blacklisted;
		return this;
	}

	public CallLog errorCode(Integer errorCode) {
		this.errorCode = errorCode;
		return this;
	}

	public CallLog errorDesc(String errorDesc) {
		this.errorDesc = errorDesc;
		return this;
	}

	public CallLog vlr(String vlr) {
		this.vlr = vlr;
		return this;
	}

	public CallLog sriAt(Instant sriAt) {
		this.sriAt = sriAt;
		return this;
	}

	public CallLog sriRespAt(Instant sriRespAt) {
		this.sriRespAt = sriRespAt;
		return this;
	}

	public CallLog peerIp(String peerIp) {
		this.peerIp = peerIp;
		return this;
	}

	public CallLog channel(String channel) {
		this.channel = channel;
		return this;
	}
	
	public CallLog callref(String callref) {
		this.callref = callref;
		return this;
	}

}
