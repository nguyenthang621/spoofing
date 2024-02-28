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
  
  /**
   * Output-ASP is the upstream Application Server Process this box connected to
   */
  private List<String> outputAsp = new ArrayList<>();
  
  private List<String> outputPc = new ArrayList<>();
  
  /**
   * Input ASP is the inbound application server process this box connected to.
   */
  private List<String> inputAsp = new ArrayList<>();
  
  /** Strip off all zero to E164 */
  private String mcc = "84";
  
  /**
   * Cache time to live
   */
  private int ttl = 60000;
  
  /**
   * Blacklist TTL is the time window that we should count the A number blacklisted event on
   */
  private int blacklistTtl = 600; // 5 minutes
  
  /**
   * If counter reach this blacklistThreshold, increase the blacklistBlockTtl
   */
  private int blacklistThreshold = 5; // move A number to persist blacklist after 5 attempt within blacklistTtl
  
  /**
   * Once counter larger than threshold, use this TTL instead
   */
  private int blacklistBlockTtl = 10800; // 3 hours
  
  private boolean simulate=false;
  private int handleMode=0; //0-old mode , 1 
  private String isupFsmPath=""; //file name of finish state machine handle isup message
  private int localPointCode=0; 
  private int sctpWorkerThread=0; //number of sctp worker threads
  private int workerThreadMode=0; //0: using sctp , 1: using async,2: iam -> using other thread
  private int workerThreadNum=100; //number of woker thread for mode 1,2
  
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
