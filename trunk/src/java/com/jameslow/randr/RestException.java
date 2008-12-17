package com.jameslow.randr;

public class RestException extends BaseException {
	public RestException(String errorMsg) {
		super(errorMsg);
	}
	public RestException(String errorMsg, int number) {
		super(errorMsg,number);
	}
	public RestException(String errorMsg, int number, Exception e) {
		super(errorMsg,number,e);
	}
}
