package com.istt.domain;

import java.io.Serializable;
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
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/** A CallLog. */
@Entity
@Table(name = "connector_log")
@Cache(usage = CacheConcurrencyStrategy.NONSTRICT_READ_WRITE)
@Data
public class ConnectorLog implements Serializable {

    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.SEQUENCE, generator = "sequenceGenerator")
    @SequenceGenerator(name = "sequenceGenerator")
    private Long id;

    @NotNull
    @Column(name = "callref", nullable = false)
    private String callref;

    @Column(name = "request_channel", nullable = false)
    private String requestChannel;

    @NotNull
    @Column(name = "request_instance", nullable = false)
    private String requestInstance;

    @NotNull
    @Column(name = "request_at", nullable = false)
    private Instant requestAt = Instant.now();

    @Column(name = "response_at")
    private Instant responseAt = Instant.now();

    @NotNull
    @Column(name = "jitter", nullable = false)
    private Integer jitter = 0;

    @Column(name = "error_code")
    private Integer errorCode;

    @Column(name = "error_desc")
    private String errorDesc;

    @Column(name = "request_params")
    private String requestParams;

    @Column(name = "request_body")
    private String requestBody;

    @Column(name = "response_payload")
    private String responsePayload;

    public ConnectorLog id(Long id) {
        this.id = id;
        return this;
    }

    public ConnectorLog callref(String callref) {
        this.callref = callref;
        return this;
    }

    public ConnectorLog requestChannel(String requestChannel) {
        this.requestChannel = requestChannel;
        return this;
    }

    public ConnectorLog requestInstance(String requestInstance) {
        this.requestInstance = requestInstance;
        return this;
    }

    public ConnectorLog requestAt(Instant requestAt) {
        this.requestAt = requestAt;
        return this;
    }

    public ConnectorLog responseAt(Instant responseAt) {
        this.responseAt = responseAt;
        return this;
    }

    public ConnectorLog jitter(Integer jitter) {
        this.jitter = jitter;
        return this;
    }

    public ConnectorLog errorCode(Integer errorCode) {
        this.errorCode = errorCode;
        return this;
    }

    public ConnectorLog errorDesc(String errorDesc) {
        this.errorDesc = errorDesc;
        return this;
    }

    public ConnectorLog requestParams(String requestParams) {
        this.requestParams = requestParams;
        return this;
    }

    public ConnectorLog requestBody(String requestBody) {
        this.requestBody = requestBody;
        return this;
    }

    public ConnectorLog responsePayload(String responsePayload) {
        this.responsePayload = responsePayload;
        return this;
    }
}
