package com.istt.config.ss7;

import com.istt.config.ApplicationProperties;
import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import lombok.extern.slf4j.Slf4j;
import org.restcomm.protocols.ss7.sccp.impl.SccpStackImpl;
import org.restcomm.protocols.ss7.sccp.impl.parameter.ParameterFactoryImpl;
import org.restcomm.protocols.ss7.sccp.parameter.ParameterFactory;
import org.restcomm.protocols.ss7.ss7ext.Ss7ExtInterfaceDefault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class SCCPConfiguration {

    public static SccpStackImpl sccpStack;

    @Autowired
    M3UAConfiguration m3ua;

    @Autowired
    ApplicationProperties props;

    private ParameterFactory parameterFactory;

    /**
     * Configuring the SCCP layer follows exactly same architecture of persisting configuration in xml
     * file.
     */
    @PostConstruct
    public void initialize() throws Exception {
        log.info("Starting SCCP Stack...");
        sccpStack = new SccpStackImpl(props.getSs7Name(), new Ss7ExtInterfaceDefault());
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
