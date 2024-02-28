package com.istt.service.dto;

import java.io.Serializable;
import java.time.Instant;
import lombok.Data;

/** A CallLog. */
@Data
public class CallLog implements Serializable {

    private static final long serialVersionUID = 1L;
    private Long id;

    private String calling;

    private String called;

    private Instant createdAt = Instant.now();

    private Instant requestAt = Instant.now();

    private Instant responseAt = Instant.now();

    private Integer state = 200;

    private String route;

    private Long routePrefix;

    private Integer routeLength;

    private Boolean dryRun;

    private Boolean whitelisted;

    private Boolean blacklisted;

    private Integer errorCode;

    private String errorDesc;

    private String vlr;

    private Instant sriAt;

    private Instant sriRespAt;

    private String sriResponse;

    private String peerIp;

    private String channel;

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

    public CallLog sriResponse(String sriResponse) {
        this.sriResponse = sriResponse;
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
