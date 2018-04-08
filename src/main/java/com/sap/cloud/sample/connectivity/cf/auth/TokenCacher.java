package com.sap.cloud.sample.connectivity.cf.auth;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpSession;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TokenCacher {

	private static final Logger LOGGER = LoggerFactory.getLogger(TokenCacher.class);
	
	private final HttpSession httpSession;
	private final String userName;

	private final Map<String, String> destinationAccessTokenCache = new HashMap<String, String>();

	
	private static final String TOKEN_CACHE_SESSION_ATTRIBUTE = "tokenCache";

	public TokenCacher(HttpSession httpSession, String userName) {
		this.httpSession = httpSession;
		this.userName = userName;
		initCache(userName);
	}

	public String getToken(String destinationName) {
		return cache() == null ? null : cache().get(destinationName);
	}

	public void cache(String destinationName, String accessToken) {
		cache().put(destinationName, accessToken);
	}
	
	public String getCacheId() {
		return userName + ":" + httpSession;
	}
	
	@SuppressWarnings("unchecked")
	private Map<String, String> cache(){
		return (Map<String, String>)httpSession.getAttribute(TOKEN_CACHE_SESSION_ATTRIBUTE);
	}
	
	private void initCache(String userName) {
		if (httpSession.getAttribute(TOKEN_CACHE_SESSION_ATTRIBUTE) == null) {
			httpSession.setAttribute(TOKEN_CACHE_SESSION_ATTRIBUTE, destinationAccessTokenCache);
			LOGGER.info("Token cache initialized: [" + getCacheId() + "]");
		}
	}

}
