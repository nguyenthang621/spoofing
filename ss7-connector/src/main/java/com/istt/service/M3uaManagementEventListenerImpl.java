package com.istt.service;

import org.mobicents.protocols.ss7.m3ua.As;
import org.mobicents.protocols.ss7.m3ua.Asp;
import org.mobicents.protocols.ss7.m3ua.AspFactory;
import org.mobicents.protocols.ss7.m3ua.M3UAManagementEventListener;
import org.mobicents.protocols.ss7.m3ua.State;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.istt.config.ApplicationProperties;
import com.istt.config.ss7.M3UAConfiguration;

import io.micrometer.core.instrument.Metrics;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
public class M3uaManagementEventListenerImpl implements M3UAManagementEventListener {
	
	@Autowired ApplicationProperties props;
	
	@Override
	public void onServiceStarted() {
		log.debug("onServiceStarted");
	}

	@Override
	public void onServiceStopped() {
		log.debug("onServiceStopped");
	}

	@Override
	public void onRemoveAllResources() {
		log.debug("onRemoveAllResources");
	}

	@Override
	public void onAsCreated(As as) {
		log.debug("onAsCreated {}", as.getName());
	}

	@Override
	public void onAsDestroyed(As as) {
		log.debug("onAsDestroyed {}", as.getName());		
	}

	@Override
	public void onAspFactoryCreated(AspFactory aspFactory) {
		log.debug("onAspFactoryCreated {}", aspFactory.getName());	
	}

	@Override
	public void onAspFactoryDestroyed(AspFactory aspFactory) {
		log.debug("onAspFactoryDestroyed {}", aspFactory.getName());	
	}

	@Override
	public void onAspAssignedToAs(As as, Asp asp) {
		log.debug("onAspAssignedToAs {} -> {}",asp.getName(), as);	
	}

	@Override
	public void onAspUnassignedFromAs(As as, Asp asp) {
		log.debug("onAspUnassignedFromAs {} -> {}",asp.getName(), as);
	}

	@Override
	public void onAspFactoryStarted(AspFactory aspFactory) {
		log.debug("onAspFactoryStarted {}",aspFactory.getName());
	}

	@Override
	public void onAspFactoryStopped(AspFactory aspFactory) {
		log.debug("onAspFactoryStopped {}",aspFactory.getName());
	}

	@Override
	public void onAspActive(Asp asp, State oldState) {
		log.info("onAspActive {} old state {}",asp.getName(), oldState);
		Metrics.globalRegistry.counter("asp", "name", asp.getName(), "state", "active").increment();
		if (!isRequiredAspUp()) {
			stopInputAsp();
		} else {
			startInputAsp();
		}
	}

	private void startInputAsp() {
		for (String inputAsp : props.getInputAsp()) {
			log.warn("ALL REQUIRED links are up, starting INPUT ASP: {}", inputAsp);
			try {
				M3UAConfiguration.clientM3UAMgmt.startAsp(inputAsp);
				log.info("ASP {} started", inputAsp);
			} catch (Exception e) {
				log.error("Cannot Start ASP: {}", inputAsp, e);
			}
		}
	}

	private void stopInputAsp() {
		for (String inputAsp : props.getInputAsp()) {
			log.error("ONE OF REQUIRED links not up, shutting down INPUT ASP: {}", inputAsp);
			try {
				M3UAConfiguration.clientM3UAMgmt.stopAsp(inputAsp);
				log.error("ASP {} shut down", inputAsp);
			} catch (Exception e) {
				log.error("Cannot Stop ASP: {}", inputAsp, e);
			}
		}
	}

	private boolean isRequiredAspUp() {
		for (AspFactory f : M3UAConfiguration.clientM3UAMgmt.getAspfactories()) {
			for (Asp a : f.getAspList()) {
				if (!a.isUp() && props.getOutputAsp().stream().anyMatch(i -> i.equalsIgnoreCase(a.getName()))) {
					log.error("REQUIRED ASP NOT UP: {} state {}", a.getName(), a.getState());
					return false;
				}
			}
		}
		return true;
	}

	@Override
	public void onAspInactive(Asp asp, State oldState) {
		log.info("onAspInactive {} old state {}",asp.getName(), oldState);
		Metrics.globalRegistry.counter("asp", "name", asp.getName(), "state", "inactive").increment();
		if (props.getOutputAsp().stream().anyMatch(i -> i.equalsIgnoreCase(asp.getName()))) {
			for (String inputAsp : props.getInputAsp()) {
				log.warn("ALL REQUIRED links not up, shutting down INPUT ASP: {}", inputAsp);
				try {
					M3UAConfiguration.clientM3UAMgmt.stopAsp(inputAsp);
					log.error("ASP {} shut down", inputAsp);
				} catch (Exception e) {
					log.error("Cannot Stop ASP: {}", inputAsp, e);
				}
			}
		}
	}

	@Override
	public void onAspDown(Asp asp, State oldState) {
		log.info("onAspDown {} old state {}",asp.getName(), oldState);
		Metrics.globalRegistry.counter("asp", "name", asp.getName(), "state", "down").increment();
		if (props.getOutputAsp().stream().anyMatch(i -> i.equalsIgnoreCase(asp.getName()))) {
			for (String inputAsp : props.getInputAsp()) {
				log.warn("ALL REQUIRED links not up, shutting down INPUT ASP: {}", inputAsp);
				try {
					M3UAConfiguration.clientM3UAMgmt.stopAsp(inputAsp);
					log.error("ASP {} shut down", inputAsp);
				} catch (Exception e) {
					log.error("Cannot Stop ASP: {}", inputAsp, e);
				}
			}
		}
	}

	@Override
	public void onAsActive(As as, State oldState) {
		// TODO Auto-generated method stub
		log.debug("onAsActive {} old state {}",as.getName(), oldState);
	}

	@Override
	public void onAsPending(As as, State oldState) {
		// TODO Auto-generated method stub
		log.debug("onAsPending {} old state {}",as.getName(), oldState);
	}

	@Override
	public void onAsInactive(As as, State oldState) {
		// TODO Auto-generated method stub
		log.debug("onAsInactive {} old state {}",as.getName(), oldState);
	}

	@Override
	public void onAsDown(As as, State oldState) {
		// TODO Auto-generated method stub
		log.debug("onAsDown {} old state {}",as.getName(), oldState);
	}
	
}
