package com.istt.inboundtraffic;

import java.util.Properties;

import javax.sip.ListeningPoint;
import javax.sip.PeerUnavailableException;
import javax.sip.SipFactory;
import javax.sip.SipListener;
import javax.sip.SipProvider;
import javax.sip.SipStack;
import javax.sip.address.AddressFactory;
import javax.sip.header.HeaderFactory;
import javax.sip.message.MessageFactory;

import com.istt.inboundtraffic.service.Constants;

public class SipPhone {

	public void init(String ipAddr, Integer port) {

		SipFactory sipFactory = SipFactory.getInstance();
		if (null == sipFactory) {
			System.out.println("init sipFactory is null.");
			return;
		}

		sipFactory.setPathName("gov.nist");
		Properties properties = new Properties();
		properties.setProperty("javax.sip.STACK_NAME", "sipphone");
		// 16 for logging traces. 32 for debug + traces.
		properties.setProperty("gov.nist.javax.sip.TRACE_LEVEL", "32");
		properties.setProperty("gov.nist.javax.sip.DEBUG_LOG", Constants.getDebugfiles());
		properties.setProperty("gov.nist.javax.sip.SERVER_LOG", Constants.getLogfiles());
		properties.setProperty("gov.nist.javax.sip.MAX_SERVER_TRANSACTIONS", "-1");
		SipStack sipStack = null;
		try {
			sipStack = sipFactory.createSipStack(properties);
		} catch (PeerUnavailableException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return;
		}

		try {
			HeaderFactory headerFactory = sipFactory.createHeaderFactory();
			AddressFactory addressFactory = sipFactory.createAddressFactory();
			MessageFactory msgFactory = sipFactory.createMessageFactory();
			ListeningPoint lp = sipStack.createListeningPoint(ipAddr, port, "udp");

			SipProvider sipProvider = sipStack.createSipProvider(lp);
			SipListener sipListener = new MySipListener(ipAddr, port, addressFactory, msgFactory, headerFactory,
					sipProvider);
			System.out.println("udp provider " + sipProvider.toString());
			System.out.println("=====================OK==================");
			sipProvider.addSipListener(sipListener);
		} catch (Exception ex) {
			ex.printStackTrace();
			return;
		}

	}

}