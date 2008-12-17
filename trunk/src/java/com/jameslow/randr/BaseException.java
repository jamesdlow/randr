package com.managedlearning.common.api.rest;

public class BaseException extends Exception {
	private String errorMsg;
	private Exception exception;
	private int number;
	
	public BaseException(String errorMsg) {
		this(errorMsg,-1);
	}
	public BaseException(String errorMsg, int number) {
		this(errorMsg,-1,null);
	}
	public BaseException(String errorMsg, int number, Exception e) {
		super();
		this.errorMsg = errorMsg;
		this.exception = e;
		this.number = number;
	}
	public String getErrorMsg() {
		return errorMsg;
	}
	public void setErrorMsg(String errorMsg) {
		this.errorMsg = errorMsg;
	}
	public Exception getException() {
		return exception;
	}
	public void setException(Exception exception) {
		this.exception = exception;
	}
	public int getNumber() {
		return number;
	}
	public void setNumber(int number) {
		this.number = number;
	}
	public String getMessage() {
		if(exception != null)
			return errorMsg + ": " + exception.getMessage();
		else
			return errorMsg;
	}
}
