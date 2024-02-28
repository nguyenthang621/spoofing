package com.istt.service.dto;

import lombok.Data;

@Data
public class ErrorMapping {

	private int errorCode = 0;

	private String errorDesc;

	private int causeCode = 0;

}
