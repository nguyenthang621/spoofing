package com.istt.config.ss7;

import com.istt.config.ApplicationProperties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.mobicents.protocols.ss7.tcap.TCAPStackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties specific to Kafka Endpoint.
 *
 * <p>Properties are configured in the {@code application.yml} file. See {@link
 * io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@Configuration
public class TCAPConfiguration {

  private final Logger log = LoggerFactory.getLogger(TCAPConfiguration.class);

  @Autowired ApplicationProperties props;

  @Autowired SCCPConfiguration sccp;

  public static TCAPStackImpl tcapStack;

  @PostConstruct
  public void startUp() throws Exception {
    log.info("-------- Starting TCAP Stack -------------");
    log.info("======== TCAPConfiguration ===============");
    log.info(this.toString());
    log.info("======== TCAPConfiguration ===============");
    tcapStack =
        new TCAPStackImpl(
            props.getSs7Name(), SCCPConfiguration.sccpStack.getSccpProvider(), props.getSs7Ssn());
    tcapStack.setPersistDir(props.getPersistDir());
    tcapStack.start();
    log.info("Done.");
  }

  @PreDestroy
  public void shutdown() throws Exception {
    log.info("Stopping TCAP Stack...");
    tcapStack.stop();
    log.info("Done.");
  }
}
