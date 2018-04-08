package com.sap.cloud.sample.connectivity.cf.http;

import static java.net.HttpURLConnection.HTTP_BAD_REQUEST;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.InetSocketAddress;
import java.net.Proxy;
import java.net.URL;

import org.apache.commons.io.IOUtils;
import org.json.JSONException;

import com.sap.cloud.sample.connectivity.cf.ConnectionAttributes.ProxyType;
import com.sap.cloud.sample.connectivity.cf.SharedConstants;
import com.sap.cloud.sample.connectivity.cf.proxy.OnPremiseProxyTools;
import com.sap.cloud.sample.connectivity.cf.proxy.ProxyCredentials;

public class HttpUtils {

	private HttpUtils() {
		// Can not be created from outside the class
	}
	
	public static HttpURLConnection openUrlConnection(URL url) throws IOException {
		return openUrlConnection(url, ProxyType.INTERNET);
	}

	public static HttpURLConnection openUrlConnection(URL url, ProxyType proxyType) throws IOException {
		HttpURLConnection connection;
		
		switch (proxyType) {
		case ONPREMISE:
			// Use connectivity service HTTP proxy for on-premise calls
			ProxyCredentials proxyCredentials;
			try {
				proxyCredentials = OnPremiseProxyTools.getProxyCredentials();
			} catch (JSONException e) {
				throw new IOException("Could not get proxy credentials: ", e);
			}
			Proxy proxy = new Proxy(Proxy.Type.HTTP,
					new InetSocketAddress(proxyCredentials.getProxyHost(), proxyCredentials.getProxyPort()));

			connection = (HttpURLConnection) url.openConnection(proxy);
			// Proxy authorization
			try {
				OnPremiseProxyTools.proxyAuthorization(connection);
			} catch (Exception e) {
				e.printStackTrace();
			}
			break;

		case INTERNET:
			connection = (HttpURLConnection) url.openConnection();
			break;
		default:
			throw new IllegalArgumentException("Unsupported Proxy Type: " + proxyType);
		}
		
		connection.setConnectTimeout(SharedConstants.CONNECT_TIMEOUT);
		connection.setReadTimeout(SharedConstants.READ_TIMEOUT);
		return connection;
	}

	public static HttpResponse getResponse(HttpURLConnection urlConnection) throws IOException {
		return getResponse(urlConnection, null);
	}

	public static HttpResponse getResponse(HttpURLConnection urlConnection, InputStream postData) throws IOException {
		urlConnection.connect();
		
		if(postData != null) {
			IOUtils.copy(postData, urlConnection.getOutputStream());
		}
		
		int backendResponseCode = urlConnection.getResponseCode();
		
		InputStream backendInStream = null;

		if (backendResponseCode < HTTP_BAD_REQUEST) {
			backendInStream = urlConnection.getInputStream();
		} else if (urlConnection.getErrorStream() != null) {
			backendInStream = urlConnection.getErrorStream();
		}
		
		if(backendInStream == null) {
			backendInStream = IOUtils.toInputStream("Empty input stream from backend!", "UTF-8");;
		}
		
		return new HttpResponse(backendResponseCode, backendInStream, urlConnection.getHeaderFields());
	}
}
