package com.istt.inboundtraffic.modal;

import java.io.Serializable;

import javax.sip.ServerTransaction;
import javax.sip.message.Request;

public class CacheData implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private ServerTransaction serverTransaction;
	private Request request;

	public CacheData(ServerTransaction serverTransaction, Request request) {
		this.serverTransaction = serverTransaction;
		this.request = request;
	}

	public ServerTransaction getServerTransaction() {
		return serverTransaction;
	}

	public Request getRequest() {
		return request;
	}
}
