package com.jameslow.randr;

import java.io.IOException;
import javax.servlet.http.HttpServletResponse;

public class RestServletResponse extends RestResponse {
	private HttpServletResponse response;
	public RestServletResponse(HttpServletResponse response) {
		this.response = response;
	}
	public String GenerateXML(String message, int code, boolean success) {
		return "<"+RestClass.RESULT+"><"+RestClass.STATUS+">"+(success ? RestClass.SUCCESS : RestClass.ERROR)+"</"+RestClass.STATUS+"><"+RestClass.CODE+">"+code+"</"+RestClass.CODE+"><"+RestClass.MESSAGE+"><![CDATA["+message+"]]></"+RestClass.MESSAGE+"></"+RestClass.RESULT+">";
	}
	public void writeResponseImplementation(String message, int code, boolean success) throws ResponseException {
		try {
			response.getWriter().write(GenerateXML(message,code,success));
		} catch (IOException e) {
			throw new ResponseException("Could not write to HttpServletResponse",-1,e);
		}
	}
}
