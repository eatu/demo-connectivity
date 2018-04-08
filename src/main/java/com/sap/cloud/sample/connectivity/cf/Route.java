package com.sap.cloud.sample.connectivity.cf;

import org.json.JSONException;
import org.json.JSONObject;

public class Route {
	
	private String path;
	private String destinationName;
	private String destinationEntryPath;

	private Route() {}
	
	public static Route fromJSON(JSONObject json) {
		Route route = new Route();
		try {
			route.path = json.getString("path");
			route.destinationName = json.getJSONObject("target").getString("name");
			route.destinationEntryPath = json.getJSONObject("target").getString("entryPath");
		} catch (JSONException | NullPointerException e) {
			throw new RuntimeException("Only valid routes of type destination are supported", e);
		}
		
		while(route.path.endsWith("/")) {
			route.path.substring(0, route.path.length() - 1);
		}
		
		return route;
	}
	
	public boolean matches(String path) {
		return path.startsWith(this.path);
	}
	
	public String getPath() {
		return path;
	}

	public String getDestinationName() {
		return destinationName;
	}

	public String getDestinationEntryPath() {
		return destinationEntryPath;
	}

}
