package com.istt.ss7.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class SriCallDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -4170672362515178850L;

	private String address;

	private String error;
	
	private Long errorCode;
	
	private String remoteGT;
	
	private String imei;
	
	private String vlr;
	
	private String msc;
	
	private Integer mcc;
	
	private Integer mnc;

	private String subscriberState;
	private String imsi;
	private String msisdn;
	private String msrn;
	private String mnp;
	
}
