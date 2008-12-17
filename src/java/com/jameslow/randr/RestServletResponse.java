package com.managedlearning.common.api.rest;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class RestServletResponse extends RestResponse {
	private HttpServletResponse response;
	public RestServletResponse(HttpServletResponse response) {
		this.response = response;
	}
	public String GenerateXML(String message, int code, boolean success) {
		return "<response><status>"+(success ? SUCCESS : ERROR)+"</status><code>"+code+"</code><message>"+message+"</message></response>";
	}
	public void writeResponseImplementation(String message, int code, boolean success) throws ResponseException {
		try {
			response.getWriter().write(GenerateXML(message,code,success));
		} catch (IOException e) {
			throw new ResponseException("Could not write to HttpServletResponse",-1,e);
		}
	}
}
