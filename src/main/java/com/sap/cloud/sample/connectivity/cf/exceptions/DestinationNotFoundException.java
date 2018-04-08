package com.sap.cloud.sample.connectivity.cf.exceptions;

public class DestinationNotFoundException extends Exception {
	private static final long serialVersionUID = 1L;

	public DestinationNotFoundException(String message) {
		super(message);
	}
}
