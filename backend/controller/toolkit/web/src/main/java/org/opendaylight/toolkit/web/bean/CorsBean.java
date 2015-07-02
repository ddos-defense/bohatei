package org.opendaylight.toolkit.web.bean;

import java.io.Serializable;

public class CorsBean implements Serializable {
	private static final long serialVersionUID = 1L;
	private String address;
	
	public void setAddress(String address) {
		this.address = address;
	}
	
	public String getAddress() {
		return address;
	}
}
