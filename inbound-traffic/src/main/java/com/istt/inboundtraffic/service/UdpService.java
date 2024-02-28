package com.istt.inboundtraffic.service;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.istt.inboundtraffic.UDPServer.UDPServer;

@Service
public class UdpService {

	private final UDPServer udpServer;

	@Autowired
	public UdpService(UDPServer udpServer) {
		this.udpServer = udpServer;
	}

	@PostConstruct
	public void startUdpServer() {
		udpServer.start();
	}

	@PreDestroy
	public void stopUdpServer() {
		udpServer.stop();
	}
}
