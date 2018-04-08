package com.sap.cloud.sample.connectivity.cf.http;

import java.util.List;
import java.util.Map;

import java.io.InputStream;


public class HttpResponse {
	private final int responseCode;
	private final InputStream responseStream;
	private final Map<String,List<String>> header;

	public HttpResponse(int responseCode, InputStream responseStream, Map<String,List<String>> header) {
		this.responseCode = responseCode;
		this.responseStream = responseStream;
		this.header = header;
	}

	public int getResponseCode() {
		return responseCode;
	}

	public InputStream getResponseStream() {
		return responseStream;
	}

	public Map<String,List<String>> getHeader() {
		return header;
	}
	
}