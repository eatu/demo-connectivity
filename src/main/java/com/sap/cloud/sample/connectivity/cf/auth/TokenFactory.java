package com.sap.cloud.sample.connectivity.cf.auth;

import static com.sap.cloud.sample.connectivity.cf.EnvironmentVariableAccessor.getServiceCredentials;

import java.net.URI;
import java.net.URISyntaxException;

import org.json.JSONException;
import org.json.JSONObject;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.provider.authentication.OAuth2AuthenticationDetails;

import com.sap.xs2.security.container.SecurityContext;
import com.sap.xs2.security.container.UserInfoException;
import com.sap.xsa.security.container.XSTokenRequest;

public final class TokenFactory {
	private static final String CLIENT_ID = "clientid";
	private static final String CLIENT_SECRET = "clientsecret";

	private static final String XSUAA_SERVICE_PROP_URL = "url";
	private static final String XSUAA_SERVICE_NAME = "xsuaa";

	private static final Logger LOGGER = LoggerFactory.getLogger(TokenFactory.class);
	
	public class GetTokenException extends Exception{
		private static final long serialVersionUID = 1924287017301950182L;

		private GetTokenException(String msg, Exception e) {
			super(msg, e);
		}
	}

	public String getClientCredentialsGrantAccessToken(String serviceName, String xsuaaInstanceName) throws GetTokenException {
		
		String token;
		try {
			OAuthCredentials credentials = credentials(serviceName, xsuaaInstanceName);
			token =  getPlainToken(credentials.getXsUaaUri(), credentials.getClientId(), credentials.getClientSecret());
		} catch (JSONException | URISyntaxException | UserInfoException e) {
			throw new GetTokenException("Could not get ClientCredentialsGrantAccessToken", e);
		} 
		LOGGER.info("Got client credentials grant JWT token: " + token);
		return token;
	}
	
	public String getUserExchangeGrantAccessToken(String serviceName, String xsUaaInstanceName) throws GetTokenException {
		String token;
		try {
			OAuthCredentials credentials = credentials(serviceName, xsUaaInstanceName);
			token = getUserExchangeToken(credentials.getXsUaaUri(), credentials.getClientId(), credentials.getClientSecret());
		} catch (UserInfoException | URISyntaxException | JSONException e) {
			throw new GetTokenException("Could not get UserExchangeGrantAccessToken", e);
		}
		LOGGER.info("Got user exchange grant JWT token: " + token);
		return token;
	}
	
	public String getClientOAuthToken() throws UserInfoException {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			throw new UserInfoException("User not authenticated");
		}
		OAuth2AuthenticationDetails details = (OAuth2AuthenticationDetails) auth.getDetails();
		return details.getTokenValue();
	}

	private String getPlainToken(URI xsUaaUri, String clientId, String clientSecret) throws URISyntaxException, UserInfoException {
		XSTokenRequest tokenReq = new XSTokenRequest(xsUaaUri.toString());
		tokenReq.setClientId(clientId).setClientSecret(clientSecret).setType(XSTokenRequest.TYPE_CLIENT_CREDENTIALS_TOKEN);
		
		return SecurityContext.getUserInfo().requestToken(tokenReq);
	}

	private String getUserExchangeToken(URI xsUaaUri, String clientId, String clientSecret) throws UserInfoException, URISyntaxException {
		XSTokenRequest tokenReq = new XSTokenRequest(xsUaaUri.toString());
		tokenReq.setClientId(clientId).setClientSecret(clientSecret).setType(XSTokenRequest.TYPE_USER_TOKEN);
		
		return SecurityContext.getUserInfo().requestToken(tokenReq);
	}
	
	private OAuthCredentials credentials(String serviceName, String xsuaaInstanceName) throws JSONException, URISyntaxException {
		JSONObject credentials = getServiceCredentials(serviceName);
		String clientId = credentials.getString(CLIENT_ID);
		String clientSecret = credentials.getString(CLIENT_SECRET);

		JSONObject xsuaaCredentials = getServiceCredentials(XSUAA_SERVICE_NAME, xsuaaInstanceName);
		return new OAuthCredentials(clientId, clientSecret, new URI(xsuaaCredentials.getString(XSUAA_SERVICE_PROP_URL))); 
	}
	
}
