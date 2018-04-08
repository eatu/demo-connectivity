package com.sap.cloud.sample.connectivity.cf;

public final class SharedConstants {

	public static final String HEADER_PROXY_AUTHORIZATION = "Proxy-Authorization";
	public static final String HEADER_SCC_LOCATION_ID = "SAP-Connectivity-SCC-Location_ID";
	public static final String HEADER_SAP_CONNECTIVITY_AUTHENTICATION = "SAP-Connectivity-Authentication";
	public static final String HEADER_AUTORIZATION = "Authorization";

	public static final int CONNECT_TIMEOUT = 10000;
	public static final int READ_TIMEOUT = 60000;
	
	public static final String BEARER_WITH_TRAILING_SPACE = "Bearer ";
	public static final String BASIC_WITH_TRAILING_SPACE = "Basic ";

	public static final String HTTP = "http://";

	public static final String PARAM_HTTP_CLIENT = "HttpClient"; // A form element, used when reading from the HTML form

	private SharedConstants() {
		// Can not be created from outside the class
	}
}
