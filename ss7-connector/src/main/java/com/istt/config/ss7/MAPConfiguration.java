package com.istt.config.ss7;

import com.istt.config.ApplicationProperties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.mobicents.protocols.ss7.map.MAPStackImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

/**
 * Properties specific to Kafka Endpoint.
 *
 * <p>Properties are configured in the {@code application.yml} file. See {@link
 * io.github.jhipster.config.JHipsterProperties} for a good example.
 */
@Configuration
public class MAPConfiguration {

  private final Logger log = LoggerFactory.getLogger(MAPConfiguration.class);

  public static MAPStackImpl mapStack;

  @Autowired ApplicationProperties props;

  @Autowired TCAPConfiguration tcap;

  @Autowired SCCPConfiguration sccp;

  @PostConstruct
  public void initialize() throws Exception {
    log.info("Initilize MAP stack...");
    mapStack = new MAPStackImpl(props.getSs7Name(), TCAPConfiguration.tcapStack.getProvider());
    mapStack.start();
    log.info("Done.");
  }

  @PreDestroy
  public void shutdown() throws Exception {
    log.info("Stopping MAP stack...");
    mapStack.stop();
    log.info("Done.");
  }

  public MAPStackImpl getMAPStack() {
    return mapStack;
  }
}
