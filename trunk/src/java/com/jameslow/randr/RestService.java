package com.managedlearning.common.api.rest;

import java.util.Map;

public class RestService extends RestClass {
	protected RestResponse response;
	protected Map<String,String> params;
	private int timeout;
	private static final int TIMEOUT_DEFAULT = 15;
	
	public RestService(String apikey, Map<String,String> params, RestResponse response) throws ResponseException{
		this(apikey,params,response,null,TIMEOUT_DEFAULT,true);
	}
	public RestService(String apikey, Map<String,String> params, RestResponse response, String prefix, int timeout, boolean auto) throws ResponseException {
		super(apikey, prefix);
		this.params = params;
		this.response = response;
		this.timeout = timeout;
		//If auto is false, then consumers of this class will have to manually call process()
		if (auto) {
			process();
		}
	}
	
	//Main function that processes request
	public void process() throws ResponseException {
		try {
			validate();
			String test = "";
			if (params.containsKey(testvar)) {
				test = params.get(testvar);
			}
			String result = "";
			if (TEST_ECHO.compareTo(test) == 0) {
				result = testImplementation();
			} else {
				result = processImplementation();
			}
			response.WriteSuccess(result);
		} catch (RestException e) {
			response.WriteError(e);
		}
	}
	
	protected String testImplementation() throws RestException {
		String message = "";
		boolean firsttime = true;
		for (Map.Entry<String, String> e : params.entrySet()) {
			if (firsttime) {
				firsttime = false;
			} else {
				message =  message + DELIM;
			}
			message = message + e.getKey() + ":" + e.getValue();
		}
		return message;
	}
	
	//Process the request, child classes should implement this function to process the request params
	protected String processImplementation() throws RestException {
		return "";
	}
	
	//Validate the common params
	public void validate() throws RestException {
		long time = 0;
		if (params.containsKey(timevar)) {
			time = Long.parseLong(params.get(timevar));
		} else {
			throw new RestException("Time Variable Not Set.",1);
		}
		//Time should be in seconds
		long servertime = getTime();
		if (Math.abs(servertime - time) > timeout) {
			throw new RestException("Time Difference Too Great.",2);
		}
		String sig = "";
		if (params.containsKey(sigvar)) {
			sig = params.get(sigvar);
		} else {
			throw new RestException("Signature Variable Not Set.",3);
		}
		String comparesig;
		try {
			comparesig = signature(params);
		} catch (RestException e) {
			throw new RestException("Error Creating Signature On Server From Params.",4);
		}
		if (sig.compareTo(comparesig) != 0) {
			throw new RestException("Signature Invalid.",0);
		}
	}
	//Get a parameter auto adding the prefix
	public String getParam(String param) {
		return params.get(prefix + param);
	}
}