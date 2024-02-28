package com.istt.inboundtraffic.service;

import java.util.Properties;

import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.SipStack;

import org.springframework.stereotype.Service;

@Service
public class SipStackManager {

	private static SipStack sipStack;

	SipStackManager() {
	}

	public static synchronized SipStack getSipStack() throws PeerUnavailableException {
		if (sipStack == null) {
			SipFactory sipFactory = SipFactory.getInstance();
			Properties properties = new Properties();
			sipStack = sipFactory.createSipStack(properties);
		}
		return sipStack;
	}
}
