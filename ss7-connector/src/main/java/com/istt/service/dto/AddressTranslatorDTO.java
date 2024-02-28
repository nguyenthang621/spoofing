package com.istt.service.dto;

import lombok.Data;

@Data
public class AddressTranslatorDTO {

	private long prefix;
	
	private int length;
	
	private int noa = 0;
	
	private boolean enabled = true;

	public long getPrefix() {
		return prefix;
	}

	public void setPrefix(long prefix) {
		this.prefix = prefix;
	}

	public int getLength() {
		return length;
	}

	public void setLength(int length) {
		this.length = length;
	}

	public int getNoa() {
		return noa;
	}

	public void setNoa(int noa) {
		this.noa = noa;
	}

	public boolean isEnabled() {
		return enabled;
	}

	public void setEnabled(boolean enabled) {
		this.enabled = enabled;
	}

	
}
