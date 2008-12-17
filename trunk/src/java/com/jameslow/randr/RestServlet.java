package com.jameslow.randr;

import java.util.HashMap;
import java.util.Map;
import java.util.Iterator;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class RestServlet extends HttpServlet {
	public Map reqToMap(HttpServletRequest req) {
		return paramatiseMap(req.getParameterMap());
	}
	public Map paramatiseMap(Map rawmap) {
		Map params = new HashMap();
		Iterator it = rawmap.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry)it.next();
			params.put(e.getKey(),((String[])e.getValue())[0]);
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