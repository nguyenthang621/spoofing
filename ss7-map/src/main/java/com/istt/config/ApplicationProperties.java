package com.istt.config;

import java.util.ArrayList;
import java.util.List;
import javax.annotation.PostConstruct;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Properties specific to Kafka Endpoint.
 *
 * <p>Properties are configured in the {@code application.yml} file. See {@link
 * io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@ConfigurationProperties(prefix = "application", ignoreUnknownFields = true)
@Data
@Slf4j
public class ApplicationProperties {

    private String ss7Name = "SS7";

    private String ss7Product = "ADAPTER";

    private String pcSrc = "2";

    private String gtSrc = "8491020537";

    private String gtDest = "8491020533";

    private String persistDir = "/tmp/ss7-adapter";

    private String callbackUrl = "http://localhost:8089/";

    private int ss7Ssn = 6;

    private int timeout = 10; // seconds

    private List<String> inputAsp = new ArrayList<>();

    private List<String> outputAsp = new ArrayList<>();

    private List<String> outputPc = new ArrayList<>();

    /** Strip off all zero to E164 */
    private String mcc = "84";

    /**
     * Cache time to live
     */
    private int ttl = 60000;

    @PostConstruct
    protected void init() {
        log.info("=== Application Properties ===\n{}", this);
    }

    /**
     * Return the correct MSISDN format for the whole number
     *
     * @param msisdn
     * @return
     */
    public String msisdn(String msisdn) {
        try {
            msisdn = "" + Long.parseLong(msisdn.trim());
            if ((mcc == null) || (mcc.isEmpty())) {
                return msisdn + "";
            } else {
                msisdn = mcc + msisdn;
            }
            return Long.parseLong(msisdn) + "";
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * Return the correct MSISDN format for the whole number
     *
     * @param msisdn
     * @return
     */
    public String isdn(String msisdn) {
        msisdn = "" + Long.parseLong(msisdn.trim());
        if ((mcc == null) || (mcc.isEmpty())) {
            return msisdn + "";
        } else {
            msisdn = msisdn.substring(mcc.length());
        }
        return String.valueOf(Long.parseLong(msisdn));
    }
}
