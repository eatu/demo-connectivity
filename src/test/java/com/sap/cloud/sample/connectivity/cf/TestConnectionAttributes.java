package com.sap.cloud.sample.connectivity.cf;

import static org.junit.Assert.assertNotNull;

import org.json.JSONException;
import org.junit.Before;
import org.junit.Test;

import com.sap.cloud.sample.connectivity.cf.httpclient.SampleHTTPClientType;

public class TestConnectionAttributes {

	public static String json = "{\"owner\":{\"SubaccountId\":\"05bf4e19-f61d-49d0-b75a-c0ad79e14e09\",\"InstanceId\":null},\"destinationConfiguration\":{\"Name\":\"saml2AuthVertxToSFSF\",\"Type\":\"HTTP\",\"URL\":\"https://apisalesdemo2.successfactors.eu/odata/v2/\",\"Authentication\":\"OAuth2SAMLBearerAssertion\",\"ProxyType\":\"Internet\",\"audience\":\"www.successfactors.com\",\"authnContextClassRef\":\"urn:oasis:names:tc:SAML:2.0:ac:classes:PasswordProtectedTransport\",\"company_id\":\"SFPART018845\",\"clientKey\":\"ZTZhODk2ZGQ3YWNhZjJhZmMxZDY5OTgyNjVlNA\",\"api_key\":\"ZTZhODk2ZGQ3YWNhZjJhZmMxZDY5OTgyNjVlNA\",\"SystemUser\":\"sfadmin\",\"tokenServiceURL\":\"https://salesdemo.successfactors.eu/oauth/token\",\"CloudConnectorVersion\":\"2\"},\"authTokens\":[{\"type\":\"Bearer\",\"value\":\"____token_____\"}]}";

	private ConnectionAttributes attr;

	@Before
	public void setup() throws Exception {
		attr = ConnectionAttributes.fromDestination(json, "dummyToken", null);
	}

	@Test
	public void testToStringMethod() throws JSONException {
		assertNotNull(attr.toString());
	}

	@Test
	public void testNotNullAuthType() throws JSONException {
		assertNotNull(attr.getAuthenticationType());
	}

	@Test
	public void testNotNullToken() throws JSONException {
		assertNotNull(attr.getAuthenticationToken());

	}
}
