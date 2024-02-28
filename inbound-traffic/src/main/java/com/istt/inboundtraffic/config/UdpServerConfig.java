package com.istt.inboundtraffic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.istt.inboundtraffic.UDPServer.UDPServer;
import com.istt.inboundtraffic.service.CustomUDPServerListener;

@Configuration
public class UdpServerConfig {

	@Bean
	public UDPServer udpServer() {
		UDPServer udpServer = new UDPServer("127.0.0.1", 20019);
		udpServer.setAsync(true); // Set to true if you want the server to run asynchronously
		udpServer.setListener(new CustomUDPServerListener()); // Implement your custom listener
		System.out.println("setup done");
		return udpServer;
	}
}
