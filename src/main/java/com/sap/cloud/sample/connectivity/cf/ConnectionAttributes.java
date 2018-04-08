package com.sap.cloud.sample.connectivity.cf;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import java.io.InputStream;

/**
 * Connection attributes for connecting to back end systems.
 */
public class ConnectionAttributes {

	public enum ProxyType {
		INTERNET, ONPREMISE;

		public static ProxyType fromString(String text) {
			return ProxyType.valueOf(text.toUpperCase());
		}
	}

	public enum AuthenticationType {
		NO_AUTHENTICATION("NoAuthentication"), BASIC_AUTHENTICATION("BasicAuthentication"), PRINCIPAL_PROPAGATION(
				"PrincipalPropagation"), SAMLBEARER_AUTHENTICATION("OAuth2SAMLBearerAssertion");

		private String name;

		private AuthenticationType(String name) {
			this.name = name;
		}

		public static AuthenticationType fromString(String text) {
			for (AuthenticationType authType : AuthenticationType.values()) {
				if (authType.name.equalsIgnoreCase(text)) {
					return authType;
				}
			}
			return null;

		}

		@Override
		public String toString() {
			return name;
		}
	}

	private static final String PARAM_DESTINATION_CONFIG = "destinationConfiguration";
	private static final String PARAM_URL = "URL";
	private static final String PARAM_PROXY_TYPE = "ProxyType";
	private static final String PARAM_AUTHENTICATION_TYPE = "Authentication";
	private static final String PARAM_SYSTEM_USER = "SystemUser";
	private static final String PARAM_SCC_LOCATION_ID = "CloudConnectorLocationId";

	private static final String PARAM_TOKEN_ARRAY = "authTokens";
	private static final String PARAM_TOKEN_VALUE = "value";
	private static final String PARAM_TOKEN_TYPE = "type";
	private static final String PARAM_TOKEN_NAME = "BasicAuthToken"; // For logging purposes

	private String url;
	private ProxyType proxyType;
	private AuthenticationType authenticationType;
	private String authToken;
	private String systemUser;
	private String cloudConnectorLocationId;

	private ConnectionAttributes() {
		// cannot be created outside
	}

	public String getUrl() {
		return url;
	}

	public ProxyType getProxyType() {
		return proxyType;
	}

	public AuthenticationType getAuthenticationType() {
		return authenticationType;
	}

	public String getAuthenticationToken() {
		return authToken;
	}

	public String getCloudConnectorLocationId() {
		return cloudConnectorLocationId;
	}

	/**
	 * Creates connection attributes from destination JSON attributes
	 * @param destServiceJwtToken 
	 */
	public static ConnectionAttributes fromDestination(InputStream destinationJson, String suffix) throws JSONException {
		JSONObject destination = new JSONObject(new JSONTokener(destinationJson));
		JSONObject destinationConfiguration = destination.getJSONObject(PARAM_DESTINATION_CONFIG);

		ConnectionAttributes attributes = new ConnectionAttributes();
		attributes.url = destinationConfiguration.getString(PARAM_URL);
		if(suffix != null) {
			attributes.url += suffix;
		}
		attributes.proxyType = ProxyType.fromString(destinationConfiguration.optString(PARAM_PROXY_TYPE));
		attributes.authenticationType = AuthenticationType
				.fromString(destinationConfiguration.optString(PARAM_AUTHENTICATION_TYPE));
		attributes.cloudConnectorLocationId = destinationConfiguration.optString(PARAM_SCC_LOCATION_ID);
		attributes.systemUser = destinationConfiguration.has(PARAM_SYSTEM_USER) ? destinationConfiguration.getString(PARAM_SYSTEM_USER) : null;

		if (ifUseAuthTokens(attributes)) {
			JSONArray tokensArr = destination.getJSONArray(PARAM_TOKEN_ARRAY);

			for (int i = 0; i < tokensArr.length(); i++) {
				JSONObject token = tokensArr.getJSONObject(i);
				String tokenType = token.getString(PARAM_TOKEN_TYPE);

				if (isValidAuthorization(tokenType)) {
					attributes.authToken = token.getString(PARAM_TOKEN_VALUE);
					break;
				}
			}
		}

		return attributes;
	}

	public String getSystemUser() {
		return systemUser;
	}

	private static boolean isValidAuthorization(String tokenType) {
		return tokenType.equals(SharedConstants.BASIC_WITH_TRAILING_SPACE.trim()) || tokenType.equals(SharedConstants.BEARER_WITH_TRAILING_SPACE.trim());
	}

	private static boolean ifUseAuthTokens(ConnectionAttributes attributes) {
		return attributes.authenticationType == AuthenticationType.BASIC_AUTHENTICATION || attributes.authenticationType == AuthenticationType.SAMLBEARER_AUTHENTICATION;
	}

	@Override
	public String toString() {
		StringBuilder builder = new StringBuilder();
		appendIfNonEmpty(builder, PARAM_URL, url);
		appendIfNonEmpty(builder, PARAM_PROXY_TYPE, proxyType.toString());
		appendIfNonEmpty(builder, PARAM_AUTHENTICATION_TYPE, authenticationType.toString());
		appendIfNonEmpty(builder, PARAM_TOKEN_NAME, authToken);
		appendIfNonEmpty(builder, PARAM_SCC_LOCATION_ID, cloudConnectorLocationId);
		return builder.toString();
	}

	private void appendIfNonEmpty(StringBuilder builder, String key, String value) {
		if (value != null && !value.isEmpty()) {
			builder.append(key);
			builder.append("=");
			builder.append(value);
			builder.append("\n");
		}
	}

}
