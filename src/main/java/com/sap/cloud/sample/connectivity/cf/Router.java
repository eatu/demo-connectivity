package com.sap.cloud.sample.connectivity.cf;

import java.io.IOException;
import java.io.InputStream;
import java.util.LinkedList;
import java.util.List;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletRegistration;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

public class Router implements ServletContextListener{
	
	private List<Route> routes;
	
	private void loadRoutes(ServletContext context) {
		routes = new LinkedList<>();
		try (InputStream in = context.getResourceAsStream("neo-app.json")) {
			if(in == null) {
				throw new RuntimeException("Could not find neo-app.json");
			}
			JSONObject neoappjson = new JSONObject(new JSONTokener(in));
			JSONArray routes = neoappjson.getJSONArray("routes");
			int nRoutes = routes.length();
			for(int i = 0; i < nRoutes; ++i) {
				this.routes.add(Route.fromJSON(routes.getJSONObject(i)));
			}
		} catch (JSONException | IOException e) {
			throw new RuntimeException("Cloud not parse neo-app.json", e);
		}
	}

	@Override
	public void contextInitialized(ServletContextEvent sce) {
		final ServletContext context = sce.getServletContext();
		
		this.loadRoutes(context);
		
		ServletRegistration.Dynamic servletRegistration = context.addServlet("connectivityServlet", ConnectivityServlet.class);
		
		for(Route route : routes) {
			servletRegistration.addMapping(route.getPath() + "/*");
		}
		
		context.setAttribute(RouteProvider.class.getCanonicalName(), new RouteProvider() {
			@Override
			public Route getRoute(String path) {
				return routes.stream().filter(r -> r.matches(path)).findFirst().orElseGet(() -> null);
			}
		});
	}

	@Override
	public void contextDestroyed(ServletContextEvent sce) {
		
	}


}
