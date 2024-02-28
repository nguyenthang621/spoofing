package com.istt.inboundtraffic.config;

import javax.sip.ListeningPoint;
import javax.sip.SipFactory;
import javax.sip.SipProvider;
import javax.sip.SipStack;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.istt.inboundtraffic.service.Constants;

@Configuration
public class SipConfiguration {

	@Bean
	public SipFactory sipFactory() {
		SipFactory sipFactory = SipFactory.getInstance();
		// Set the path name to the implementation package
		sipFactory.setPathName("gov.nist");
		return sipFactory;
	}

	@Bean
	public SipStack sipStack(SipFactory sipFactory) throws Exception {
		// Set the STACK_NAME property
		System.setProperty("javax.sip.STACK_NAME", "sipphonelogs");

		SipStack sipStack = sipFactory.createSipStack(System.getProperties());
		return sipStack;
	}

	@Bean
	public SipProvider sipProvider(SipStack sipStack) throws Exception {
		// Create and configure your SIP provider
		ListeningPoint listeningPoint = sipStack.createListeningPoint(Constants.getHost(), 5069, "udp");
		return sipStack.createSipProvider(listeningPoint);
	}
}
