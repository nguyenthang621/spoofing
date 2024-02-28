package com.istt.ss7.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SriSMDTO implements Serializable {


	/**
	 * 
	 */
	private static final long serialVersionUID = -7230222046062384188L;
	
	private Long dialogID;
	
	private String address;
	
	private String vlr;
	
	private String destinationImsi;
	
	private String remoteGT;
	
	private String error;
	
	private Long errorCode;

}
