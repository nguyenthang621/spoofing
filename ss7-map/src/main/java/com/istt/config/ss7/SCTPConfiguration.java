package com.istt.config.ss7;

import com.istt.config.ApplicationProperties;
import io.micrometer.core.instrument.Metrics;
import io.micrometer.core.instrument.Tags;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import org.mobicents.protocols.api.Association;
import org.mobicents.protocols.api.Management;
import org.mobicents.protocols.sctp.netty.NettySctpManagementImpl;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SCTPConfiguration {

    private static final Logger log = LoggerFactory.getLogger(SCCPConfiguration.class);

    @Autowired
    ApplicationProperties props;

    public static Management sctpManagement;

    public static Map<String, AtomicInteger> gauges = new ConcurrentHashMap<>();

    /**
     * When `sctpManagement.start()` is called, jSS7 searches for a file named [path]_Client_sctp.xml_
     * in the directory path set by user by calling `sctpManagement.setPersistDir("<your directory
     * path>")`. For example in case of linux you can pass something like
     * `this.sctpManagement.setPersistDir("/home/abhayani/workarea/mobicents/git/jss7/master/map/load/client")`.
     * If directory path is not set, Management searches for system property `sctp.persist.dir` to get
     * the path for directory. Even if `sctp.persist.dir` system property is not set, Management will
     * look at System set property `user.dir`.
     *
     * @throws Exception
     */
    @PostConstruct
    public void initialize() throws Exception {
        log.info("-------- Starting SCTP Stack -------------");
        log.info("======== SCTPConfiguration ===============");
        log.info(this.toString());
        log.info("======== SCTPConfiguration ===============");
        sctpManagement = new NettySctpManagementImpl(props.getSs7Name());
        sctpManagement.setPersistDir(props.getPersistDir());
        sctpManagement.setSingleThread(false);
        sctpManagement.start();
        sctpManagement.setConnectDelay(10000);

        sctpManagement
            .getAssociations()
            .entrySet()
            .forEach(
                entry -> {
                    String assocName = entry.getKey();
                    Association assoc = entry.getValue();
                    int state = 0;
                    if (assoc.isStarted()) state = state | 1;
                    if (assoc.isConnected()) state = state | 2;
                    if (assoc.isUp()) state = state | 4;
                    AtomicInteger status = Metrics.globalRegistry.gauge(
                        "sctp_association",
                        Tags.of(
                            "name",
                            assocName,
                            "host",
                            assoc.getHostAddress() + ":" + assoc.getHostPort(),
                            "peer",
                            assoc.getPeerAddress() + ":" + assoc.getPeerPort()
                        ),
                        new AtomicInteger(state)
                    );
                    gauges.put(assocName, status);
                }
            );
    }

    @PreDestroy
    public void shutDown() throws Exception {
        sctpManagement.stop();
    }

    /**
     * Next step is adding of the Association and/or Server depending on whether this setup will be
     * acting as client or server or both. Normally a configured set of Association and/or Server are
     * stored in the xml config file and this way is recommended. But if you want to remove all
     * previously configured Associations and Servers you need to call this command (after a Stack
     * start).
     *
     * @throws Exception
     */
    public void cleanUp() throws Exception {
        sctpManagement.removeAllResourses();
    }
}
