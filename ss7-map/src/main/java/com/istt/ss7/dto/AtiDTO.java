package com.istt.ss7.dto;

import java.io.Serializable;

import lombok.Data;

@Data
public class AtiDTO implements Serializable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6368452397917603314L;

	private String address;

	private String vlr;

	private String imei;
	
	private String msc;
	
	private Integer mcc;
	
	private Integer mnc;
	
	private String subscriberState;
	
	private String imsi;
	
	private String msisdn;
	
	private String msrn;
	
	private String mnp;
	
	private String error;

}
