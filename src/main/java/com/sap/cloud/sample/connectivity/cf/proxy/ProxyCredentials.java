package com.sap.cloud.sample.connectivity.cf.proxy;

public class ProxyCredentials {
	private String proxyHost;
	private int proxyPort;

	public String getProxyHost() {
		return proxyHost;
	}

	public void setProxyHost(String proxyHost) {
		this.proxyHost = proxyHost;
	}

	public int getProxyPort() {
		return proxyPort;
	}

	public void setProxyPort(int proxyPort) {
		this.proxyPort = proxyPort;
	}

	public ProxyCredentials(String proxyHost, int proxyPort) {
		this.setProxyHost(proxyHost);
		this.setProxyPort(proxyPort);
	}
}