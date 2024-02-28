package com.istt.inboundtraffic;

import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableScheduling;

import com.istt.inboundtraffic.service.Constants;

@SpringBootApplication
@EnableScheduling
@ComponentScan("com.istt")
public class InboundTrafficApplication implements CommandLineRunner {

	public static void main(String[] args) {
		SpringApplication.run(InboundTrafficApplication.class, args);
	}

	@Override
	public void run(String... args) throws Exception {
		// TODO Auto-generated method stub
		new SipPhone().init(Constants.getHost(), Constants.getPort());

	}

}
