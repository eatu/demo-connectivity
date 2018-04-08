package com.sap.cloud.sample.connectivity.cf.proxy;

import static com.sap.cloud.sample.connectivity.cf.EnvironmentVariableAccessor.getServiceCredentials;
import static com.sap.cloud.sample.connectivity.cf.EnvironmentVariableAccessor.getXsuaaConnectivityInstanceName;

import java.net.HttpURLConnection;

import org.json.JSONException;
import org.json.JSONObject;

import com.sap.cloud.sample.connectivity.cf.SharedConstants;
import com.sap.cloud.sample.connectivity.cf.auth.TokenFactory;
import com.sap.cloud.sample.connectivity.cf.auth.TokenFactory.GetTokenException;

public final class OnPremiseProxyTools {
	private static final String CONNECTIVITY_SERVICE_NAME = "connectivity";

	private static final String OP_HTTP_PROXY_HOST = "onpremise_proxy_host";
	private static final String OP_HTTP_PROXY_PORT = "onpremise_proxy_port";

	private OnPremiseProxyTools() {
		// Can not be created from outside the class
	}

	/** Returns proxy credentials for on-premise connectivity */
	public static ProxyCredentials getProxyCredentials() throws JSONException {
		JSONObject credentials = getServiceCredentials(CONNECTIVITY_SERVICE_NAME);

		String proxyHost = credentials.getString(OP_HTTP_PROXY_HOST);
		int proxyPort = Integer.parseInt(credentials.getString(OP_HTTP_PROXY_PORT));

		return new ProxyCredentials(proxyHost, proxyPort);
	}

	public static void proxyAuthorization(HttpURLConnection client) throws GetTokenException {
		String token = new TokenFactory().getClientCredentialsGrantAccessToken(CONNECTIVITY_SERVICE_NAME, getXsuaaConnectivityInstanceName());

		// Forward JWT token to Connectivity Service
		client.setRequestProperty(SharedConstants.HEADER_PROXY_AUTHORIZATION, SharedConstants.BEARER_WITH_TRAILING_SPACE + token);
	}
}
