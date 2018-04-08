package com.sap.cloud.sample.connectivity.cf.destination;

import static com.sap.cloud.sample.connectivity.cf.EnvironmentVariableAccessor.getXsuaaDestinationInstanceName;
import static java.net.HttpURLConnection.HTTP_NOT_FOUND;

import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

import java.io.InputStream;

import org.json.JSONException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.util.StringUtils;

import com.sap.cloud.sample.connectivity.cf.ConnectionAttributes;
import com.sap.cloud.sample.connectivity.cf.EnvironmentVariableAccessor;
import com.sap.cloud.sample.connectivity.cf.Route;
import com.sap.cloud.sample.connectivity.cf.SharedConstants;
import com.sap.cloud.sample.connectivity.cf.auth.TokenCacher;
import com.sap.cloud.sample.connectivity.cf.auth.TokenFactory;
import com.sap.cloud.sample.connectivity.cf.exceptions.DestinationNotFoundException;
import com.sap.cloud.sample.connectivity.cf.http.HttpResponse;
import com.sap.cloud.sample.connectivity.cf.http.HttpUtils;

public final class DestinationAccessor {
	private static final Logger LOGGER = LoggerFactory.getLogger(DestinationAccessor.class);
	
	private static final String DESTINATION_SERVICE_PROP_URI = "uri";

	private static final String DESTINATION_SERVICE_NAME = "destination";
	private static final String DESTINATION_SERVICE_PATH = "/destination-configuration/v1/destinations/%s";

	private final TokenFactory tokenFactory;

	private final TokenCacher tokenCacher;

	public DestinationAccessor(TokenFactory tokenFactory, TokenCacher tokenCacher) {
		this.tokenFactory = tokenFactory;
		this.tokenCacher = tokenCacher;
	}
	
	public ConnectionAttributes getDestination(Route route, final String path) throws Exception {
		final String destinationName = route.getDestinationName();
		String accessToken = tokenCacher.getToken(destinationName);
		if (accessToken != null) {
			LOGGER.info("Got cached token [" + accessToken + "] for destination [" + destinationName + "] in cache [" + tokenCacher.getCacheId() + "]");
			return getDestination(route, accessToken, path);
		}
		
		accessToken = tokenFactory.getClientCredentialsGrantAccessToken(DESTINATION_SERVICE_NAME, getXsuaaDestinationInstanceName());
		ConnectionAttributes destination = getDestination(route, accessToken, path);

		if (isUserPropagationDestination(destination)) {
			LOGGER.info("Is user propagation destination, will get user exchange token");
			accessToken = tokenFactory.getUserExchangeGrantAccessToken(DESTINATION_SERVICE_NAME, getXsuaaDestinationInstanceName());
			destination = getDestination(route, accessToken, path);
		}
		tokenCacher.cache(destinationName, accessToken);
		LOGGER.info("Cached token [" + accessToken + "] for destination [" + destinationName + "] in cache [" + tokenCacher.getCacheId() + "]");
		return destination;
	}

	private boolean isUserPropagationDestination(ConnectionAttributes destination) {
		boolean isSamlBearerDestination = destination.getAuthenticationType() == ConnectionAttributes.AuthenticationType.SAMLBEARER_AUTHENTICATION;
		if (! isSamlBearerDestination) {
			return false;
		}
		
		boolean hasSystemUser = !StringUtils.isEmpty(destination.getSystemUser());
		return !hasSystemUser;
	}

	private ConnectionAttributes getDestination(Route route, String accessToken, final String path) throws JSONException, MalformedURLException {
		String destinationName = route.getDestinationName();
		String destinationServiceUri = EnvironmentVariableAccessor.getServiceCredentialsAttribute(DESTINATION_SERVICE_NAME, DESTINATION_SERVICE_PROP_URI);
		LOGGER.info("Will get destination [" + destinationName + "], uri [" + destinationServiceUri + "] with access token [" + accessToken + "]");

		// to call a custom Destination Service, append its URL to the destination name separated by space
		String[] split = destinationName.split("\\s+");
		if (split.length > 1) {
			destinationName = split[0];
			destinationServiceUri = split[1];
		}
		
		String destinationPath = String.format(DESTINATION_SERVICE_PATH, destinationName);
		URL destinationUrl = new URL(destinationServiceUri + destinationPath);
		
		HttpURLConnection httpClient = null;
		try {
			httpClient = HttpUtils.openUrlConnection(destinationUrl);
			httpClient.setRequestProperty(SharedConstants.HEADER_AUTORIZATION, SharedConstants.BEARER_WITH_TRAILING_SPACE + accessToken);

			HttpResponse response = HttpUtils.getResponse(httpClient);

			if (response.getResponseCode() == HTTP_NOT_FOUND) {
				throw new DestinationNotFoundException("Destination '" + destinationName + "' could not be found");
			}
			
			return ConnectionAttributes.fromDestination(response.getResponseStream(), route.getDestinationEntryPath() + path);
			
		} catch (Exception e) { 
			String msg = "EXCEPTION: " + e.getMessage() + ", desitnationName [" + destinationName + "], destination URL [" + destinationUrl + "]"; 
			LOGGER.error(msg, e);
			throw new IllegalStateException(e);
		} finally {
			if(httpClient != null) {
				httpClient.disconnect();
			}
		}
	}
}
