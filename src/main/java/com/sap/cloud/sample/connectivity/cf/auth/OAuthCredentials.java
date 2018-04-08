package com.sap.cloud.sample.connectivity.cf.auth;

import java.net.URI;

public class OAuthCredentials {

	private final String clientId;
	private final String clientSecret;
	private final URI xsUaaUri;

	public OAuthCredentials(String clientId, String clientSecret, URI xsUaaUri) {
		this.clientId = clientId;
		this.clientSecret = clientSecret;
		this.xsUaaUri = xsUaaUri;
	}

	public String getClientId() {
		return clientId;
	}

	public String getClientSecret() {
		return clientSecret;
	}

	public URI getXsUaaUri() {
		return xsUaaUri;
	}

}
