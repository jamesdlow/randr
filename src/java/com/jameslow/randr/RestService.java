package com.jameslow.randr;

import java.util.Map;
import java.util.Iterator;

public class RestService extends RestClass {
	protected RestResponse response;
	protected Map params;
	private int timeout;
	private static final int TIMEOUT_DEFAULT = 15;
	
	public RestService(Map params, RestResponse response) throws ResponseException{
		this(null,params,response);
	}
	public RestService(Map params, RestResponse response, String prefix, int timeout, boolean auto) throws ResponseException {
		this(null,params,response,prefix,timeout,auto);
	}
	public RestService(String apikey, Map params, RestResponse response) throws ResponseException{
		this(apikey,params,response,null,TIMEOUT_DEFAULT,true);
	}
	public RestService(String apikey, Map params, RestResponse response, String prefix, int timeout, boolean auto) throws ResponseException {
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
			if (apikey == null) {
				setApiKey(getApiKeyImplementation());
			}
			validate();
			String test = "";
			if (params.containsKey(testvar)) {
				test = (String)params.get(testvar);
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
	//Get the api key from params or ip address or something, child classes should implement this function if the api key isn't specifiedy during construction
	public String getApiKeyImplementation() throws RestException {
		return "";
	}
	//Process the request, child classes should implement this function to process the request params
	protected String processImplementation() throws RestException {
		return "";
	}
	protected String testImplementation() throws RestException {
		String message = "";
		boolean firsttime = true;
		Iterator it = params.entrySet().iterator();
		while (it.hasNext()) {
			Map.Entry e = (Map.Entry)it.next();
			if (firsttime) {
				firsttime = false;
			} else {
				message = message + DELIM;
			}
			message = message + (String)e.getKey() + ":" + (String)e.getValue();
		}
		return message;
	}
	
	//Validate the common params
	public void validate() throws RestException {
		long time = 0;
		if (params.containsKey(timevar)) {
			time = Long.parseLong((String)params.get(timevar));
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
			sig = (String)params.get(sigvar);
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
		return (String)params.get(prefix + param);
	}
}