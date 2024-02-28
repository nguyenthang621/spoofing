package com.istt.config.ss7;

import com.istt.config.ApplicationProperties;
import com.istt.service.M3uaManagementEventListenerImpl;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.mobicents.protocols.ss7.m3ua.Asp;
import org.mobicents.protocols.ss7.m3ua.AspFactory;
import org.mobicents.protocols.ss7.m3ua.impl.M3UAManagementImpl;
import org.mobicents.protocols.ss7.mtp.Mtp3UserPartListener;
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
public class M3UAConfiguration {

  private final Logger log = LoggerFactory.getLogger(M3UAConfiguration.class);

  public static M3UAManagementImpl clientM3UAMgmt;

  private int DELIVERY_TRANSFER_MESSAGE_THREAD_COUNT = 100;

  @Autowired private ApplicationProperties props;

  @Autowired SCTPConfiguration sctp;

  @Autowired Mtp3UserPartListener listener;
  
  @Autowired M3uaManagementEventListenerImpl m3uaManagementEventListener;

  @PostConstruct
  public void initialize() throws Exception {
    log.info("-------- Starting M3UA Stack -------------");
    log.info("======== M3UAConfiguration ===============");
    log.info(this.toString());
    log.info("======== M3UAConfiguration ===============");
    clientM3UAMgmt = new M3UAManagementImpl(props.getSs7Name(), props.getSs7Product());
    clientM3UAMgmt.setPersistDir(props.getPersistDir());

    // For M3UA, it should know which underlying SCTP layer to use
    // `clientM3UAMgmt.setTransportManagement(sctpManagement);`
    clientM3UAMgmt.setTransportManagement(SCTPConfiguration.sctpManagement);
    clientM3UAMgmt.setDeliveryMessageThreadCount(DELIVERY_TRANSFER_MESSAGE_THREAD_COUNT);
    clientM3UAMgmt.addMtp3UserPartListener(listener);
	clientM3UAMgmt.addM3UAManagementEventListener(m3uaManagementEventListener);
    clientM3UAMgmt.start();
    log.info("-------- Done --------");
    
    for (AspFactory f : clientM3UAMgmt.getAspfactories()) {
    	for (Asp asp : f.getAspList()) {
    		if (!asp.isStarted()) {
    			try {
        			clientM3UAMgmt.startAsp(asp.getName());
        		} catch (Exception e) {
        			// TODO Auto-generated catch block
        			e.printStackTrace();
        		}
    		}
    	}
    }
  }

  @PreDestroy
  public void shutdown() throws Exception {
    clientM3UAMgmt.stop();
  }

  /**
   * Once M3UA is configured and started, next step is to add the As, Asp and routing rules for
   * M3UA. Configured set of As, Asp and routing rules is trored in [path]_Client_m3ua1.xml_ (in
   * "your directory path" folder). But if you want to remove all previously configured As, Asp and
   * routing rules you need to call this command (after a Stack start).
   *
   * @throws Exception
   */
  public void cleanUp() throws Exception {
    clientM3UAMgmt.removeAllResourses();
  }
}
