package com.istt.modal;

import com.istt.domain.CallLog;

public class CdrResponse extends CallLog {
	private String callId;

	public String getCallId() {
		return callId;
	}

	public void setCallId(String callId) {
		this.callId = callId;
	}

}
