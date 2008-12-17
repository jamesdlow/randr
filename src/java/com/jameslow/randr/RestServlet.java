package com.managedlearning.common.api.rest;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestServlet extends HttpServlet {
	public Map<String,String> reqToMap(HttpServletRequest req) {
		return paramatiseMap(req.getParameterMap());
	}
	public Map<String,String> paramatiseMap(Map rawmap) {
		Map<String,String> params = new HashMap<String,String>();
		for (Object o : rawmap.keySet()) {
			params.put(o.toString(),((String[])rawmap.get(o))[0]);
		}
		return params;
	}
	protected void service(HttpServletRequest req, HttpServletResponse resp) throws ServletException,java.io.IOException {
		try {
			new RestService("",reqToMap(req),new RestServletResponse(resp));
		} catch (ResponseException e) {
			throw new ServletException(e.getMessage(),e);
		}
	}
}