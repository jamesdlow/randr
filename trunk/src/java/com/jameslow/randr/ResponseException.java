package com.jameslow.randr;

public class ResponseException extends BaseException {
	public ResponseException(String errorMsg) {
		super(errorMsg);
	}
	public ResponseException(String errorMsg, int number) {
		super(errorMsg,number);
	}
	public ResponseException(String errorMsg, int number, Exception e) {
		super(errorMsg,number,e);
	}
}
