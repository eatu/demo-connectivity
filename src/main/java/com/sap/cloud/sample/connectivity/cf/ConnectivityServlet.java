package com.sap.cloud.sample.connectivity.cf;


import java.io.InputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Enumeration;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;
import com.sap.cloud.sample.connectivity.cf.auth.TokenCacher;
import com.sap.cloud.sample.connectivity.cf.auth.TokenFactory;
import com.sap.cloud.sample.connectivity.cf.destination.DestinationAccessor;
import com.sap.cloud.sample.connectivity.cf.http.HttpResponse;
import com.sap.cloud.sample.connectivity.cf.http.HttpUtils;

/**
 * Servlet class making HTTP calls to specified on-premise back end systems<br>
 * exposed via SAP Cloud Connector. It could be used in the following example
 * connectivity scenarios:<br>
 * - No authentication to on-premise system<br>
 * - Principal Propagation to on-premise system<br>
 * - Basic authentication to on-premise system<br>
 *
 * Connection can be made by specifying details as HTTP parameters or by
 * specifying a destination identified by name.
 *
 */
public class ConnectivityServlet extends HttpServlet {

	private static final long serialVersionUID = 1L;

	private static final String HTTPS = "https://";
	private static final String HTTPS_PREFIX = "https";
	
	private static final String[] ALLOWED_HEADERS_IN  = new String[]{"accept", "accept-language", "dataserviceversion", "maxdataserviceversion", "content-length", "content-type", "x-csrf-token", "cookie"};
	private static final String[] ALLOWED_HEADERS_OUT = new String[]{"content-type", "content-length", "x-csrf-token", "set-cookie"};

	private static final Logger LOGGER = LoggerFactory.getLogger(ConnectivityServlet.class);
	
	private RouteProvider routeProvider;
	
	@Override
	public void init() throws ServletException {
		super.init();
		routeProvider = (RouteProvider) this.getServletContext().getAttribute(RouteProvider.class.getCanonicalName());
	}

	/** {@inheritDoc} */
	@Override
	protected void service(HttpServletRequest clientRequest, HttpServletResponse responseToClient)
			throws ServletException, IOException {
		
		HttpURLConnection client = null;
		ConnectionAttributes connectionAttributes = null;
		TokenFactory tokenFactory = new TokenFactory();

		try {
			connectionAttributes = getConnectionAttributes(clientRequest, tokenFactory);
			LOGGER.info("Connecting to backend system " + connectionAttributes);

			URL url = new URL(getHttpUrl(connectionAttributes));
			client = HttpUtils.openUrlConnection(url, connectionAttributes.getProxyType());
			client.setRequestMethod(clientRequest.getMethod());

			// Get JWT token from Security Context
			String token = tokenFactory.getClientOAuthToken();

			ConnectionAttributes.AuthenticationType authType = connectionAttributes.getAuthenticationType();
			switch (authType) {
			case NO_AUTHENTICATION:
				// No action needed
				break;
			case PRINCIPAL_PROPAGATION:
				// Forward JWT token to Connectivity Service
				client.setRequestProperty(SharedConstants.HEADER_SAP_CONNECTIVITY_AUTHENTICATION,
						SharedConstants.BEARER_WITH_TRAILING_SPACE + token);
				break;
			case BASIC_AUTHENTICATION:
				client.setRequestProperty(SharedConstants.HEADER_AUTORIZATION, SharedConstants.BASIC_WITH_TRAILING_SPACE + 
						connectionAttributes.getAuthenticationToken());
				break;
			case SAMLBEARER_AUTHENTICATION:
				String authHeader = SharedConstants.BEARER_WITH_TRAILING_SPACE
						+ connectionAttributes.getAuthenticationToken();
				LOGGER.info(SharedConstants.HEADER_AUTORIZATION + ": " + authHeader);
				client.setRequestProperty(SharedConstants.HEADER_AUTORIZATION, authHeader);
			default:
				break;
			}

			optionallyAddSCCLocationId(client, connectionAttributes);

			for(String header : ALLOWED_HEADERS_IN) {
				Enumeration<String> headerValues = clientRequest.getHeaders(header);
				while (headerValues.hasMoreElements()) {
					client.addRequestProperty(header, headerValues.nextElement());
				}
			}
			
			InputStream postData = null;
			if(clientRequest.getIntHeader("content-length") > 0) {
				client.setDoOutput(true);
				postData = clientRequest.getInputStream();
			}
			
			processConnection(client, responseToClient, connectionAttributes, postData);

		} catch (Exception e) {
			String messagePrefix = "Connectivity operation failed with reason: ";
			LOGGER.error(messagePrefix, e);
			sendErrorResponse(e, responseToClient);
		} finally {
			if(client != null) {
				client.disconnect();
			}			
		}
	}

	private void sendErrorResponse(Exception reason, HttpServletResponse responseToClient) throws IOException {
		responseToClient.setStatus(500);
		responseToClient.setContentType("application/json");
		String json = new Gson().toJson(reason);
		responseToClient.getWriter().println(json);
	}

	private ConnectionAttributes getConnectionAttributes(HttpServletRequest clientRequest, TokenFactory tokenFactory) throws Exception {
		String path = clientRequest.getPathInfo();
		if(clientRequest.getQueryString() != null) {
			path += "?" + clientRequest.getQueryString();
		}
		
		Route route = this.routeProvider.getRoute(clientRequest.getServletPath());
		DestinationAccessor destinationAccessor = new DestinationAccessor(tokenFactory, new TokenCacher(clientRequest.getSession(), clientRequest.getUserPrincipal().getName()));
		
		LOGGER.info("Destination: " + route.getDestinationName());
		
		return destinationAccessor.getDestination(route, path);
	}

	private String getHttpUrl(ConnectionAttributes attributes) throws Exception {
		String destinationUrl = attributes.getUrl();

		// As we make request to HTTP proxy we have to ensure that we are using HTTP
		// protocol
		if (destinationUrl != null && destinationUrl.startsWith(HTTPS_PREFIX)) {
			destinationUrl =  destinationUrl.replaceFirst(HTTPS, SharedConstants.HTTP);
		}
		return destinationUrl;
	}

	private void optionallyAddSCCLocationId(HttpURLConnection client, ConnectionAttributes attributes) {
		// Optionally, if passed as parameter, add the SCC location ID
		String sccLocationId = attributes.getCloudConnectorLocationId();
		if (sccLocationId != null && !sccLocationId.isEmpty()) {
			client.setRequestProperty(SharedConstants.HEADER_SCC_LOCATION_ID, sccLocationId);
		}
	}

	private void processConnection(HttpURLConnection client, HttpServletResponse responseToClient,
			ConnectionAttributes connectionAttributes, InputStream postData) throws IOException {
		
		HttpResponse response = HttpUtils.getResponse(client, postData);

		response.getHeader().entrySet().forEach( e -> {
			e.getValue().forEach( v -> {
				responseToClient.addHeader(e.getKey(), v);
			});
		});
		
		responseToClient.setStatus(response.getResponseCode());
		
		OutputStream clientOutStream = responseToClient.getOutputStream();
		IOUtils.copy(response.getResponseStream(), clientOutStream);
	}
}
