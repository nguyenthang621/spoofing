package com.istt.config.ss7;

import com.istt.config.ApplicationProperties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.mobicents.protocols.ss7.sccp.impl.SccpStackImpl;
import org.mobicents.protocols.ss7.sccp.impl.parameter.ParameterFactoryImpl;
import org.mobicents.protocols.ss7.sccp.parameter.ParameterFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SCCPConfiguration {

  private final Logger log = LoggerFactory.getLogger(SCCPConfiguration.class);

  public static SccpStackImpl sccpStack;

  @Autowired M3UAConfiguration m3ua;

  @Autowired ApplicationProperties props;

  private ParameterFactory parameterFactory;

  /**
   * Configuring the SCCP layer follows exactly same architecture of persisting configuration in xml
   * file.
   */
  @PostConstruct
  public void initialize() throws Exception {
    log.info("Starting SCCP Stack...");
    sccpStack = new SccpStackImpl(props.getSs7Name());
    sccpStack.setPersistDir(props.getPersistDir());
    sccpStack.setMtp3UserPart(1, M3UAConfiguration.clientM3UAMgmt);
    this.parameterFactory = new ParameterFactoryImpl();
    sccpStack.start();
    log.info("Done.");
  }

  @PreDestroy
  public void shutdown() {
    sccpStack.stop();
  }

  public void cleanUp() throws Exception {
    sccpStack.removeAllResourses();
  }

  public SccpStackImpl getSccpStack() {
    return sccpStack;
  }

  public ParameterFactory getParameterFactory() {
    return parameterFactory;
  }

  public void setParameterFactory(ParameterFactory parameterFactory) {
    this.parameterFactory = parameterFactory;
  }
}
