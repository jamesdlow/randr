package com.managedlearning.common.api.rest;

public abstract class RestResponse {
	public static final String SUCCESS = "success";
	public static final String ERROR = "error";
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
		WriteResponse(message,code,true);
	}
	public void WriteResponse(String message, int code, boolean error) throws ResponseException {
		if (base != null) {
			base.WriteResponse(message, code, error);
		}
		writeResponseImplementation(message,code,error);
	}
	protected abstract void writeResponseImplementation(String message, int code, boolean error) throws ResponseException;
}
