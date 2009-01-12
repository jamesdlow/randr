package com.jameslow.randr;

public abstract class RestResponse {
	private RestResponse base;
	
	public RestResponse() {
		this(null);
	}
	public RestResponse(RestResponse base) {
		this.base = base;
	}
	public RestResponse getBase() {
		return base;
	}
	public void setBase(RestResponse base) {
		this.base = base;
	}
	public void WriteSuccess(String message) throws ResponseException {
		WriteSuccess(message,0);
	}
	public void WriteSuccess(String message, int code) throws ResponseException {
		WriteResponse(message,code,true);
	}
	public void WriteError(RestException e) throws ResponseException {
		WriteError(e.getErrorMsg(),e.getNumber());
	}
	public void WriteError(String message) throws ResponseException {
		WriteError(message,-1);
	}
	public void WriteError(String message, int code) throws ResponseException {
		WriteResponse(message,code,false);
	}
	public void WriteResponse(String message, int code, boolean success) throws ResponseException {
		if (base != null) {
			base.WriteResponse(message, code, success);
		}
		writeResponseImplementation(message,code,success);
	}
	protected abstract void writeResponseImplementation(String message, int code, boolean success) throws ResponseException;
}
