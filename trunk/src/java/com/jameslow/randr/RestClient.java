package com.managedlearning.common.api.rest;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLEncoder;
import java.util.Map;
import java.util.HashMap;

public class RestClient extends RestClass {
	private String url;
	private Map<String,String> params = new HashMap<String,String>();
	
	public RestClient(String url, String apikey) {
		this(url,apikey,null);
	}
	public RestClient(String url, String apikey, String prefix) {
		super(apikey, prefix);
		this.url = url;
	}
	//Clear all current params
	public void clearParams() {
		params = new HashMap<String,String>();
	}
	//Add a parameter checking if its not one of the predefined reserved params
	public void addParam(String param, String value) throws RestException {
		String full = prefix + param;
		for (String check : reserved) {
			checkParam(full, check);
		}
		params.put(full,value);
	}
	//Check if a param is not equal
	private void checkParam(String param, String check) throws RestException {
		if (param.compareTo(check) == 0) {
			throw new RestException("Param name cannot equal " + param.substring(prefix.length()));		
		}
	}
	//Get the params valid at this time, don't echo
	public Map<String,String> getParams() throws RestException {
		return getParams("");
	}
	//Get the params valid at this time
	public Map<String,String> getParams(String test) throws RestException {
		Map<String,String> finalparams = new HashMap<String,String>(params);
		finalparams.put(timevar,""+getTime());
		if (test != null && "".compareTo(test) != 0) { 
			finalparams.put(testvar,test);
		}
		finalparams.put(sigvar,signature(finalparams));
		return finalparams;
	}
	//Post the params to the given url and return the contents, don't echo
	public String sendParams() throws RestException{
		return sendParams(false);
	}
	//Post the params to the given url and return the contents, optional echo
	public String sendParams(boolean echo) throws RestException{
		return sendParams((echo ? TEST_ECHO : ""));
	}
	//Post the params to the given url and return the contents, optional test
	public String sendParams(String test) throws RestException{
		try {
			URL u = new URL(url);
		    HttpURLConnection huc = (HttpURLConnection) u.openConnection();
		    huc.setRequestMethod("POST");
		    huc.setUseCaches(false);
		    huc.setDoInput(true);
		    huc.setDoOutput(true);
		    HttpURLConnection.setFollowRedirects(true);
		    huc.setInstanceFollowRedirects(true);
		    huc.setRequestProperty ("Content-Type","application/x-www-form-urlencoded");
		    StringBuffer content = new StringBuffer();
		    boolean firsttime = true;
		    for (Map.Entry<String, String> e : getParams(test).entrySet()) {
		    	if (firsttime) {
		    		firsttime = false;
		    	} else {
		    		content.append("&");
		    	}
		    	content.append(e.getKey()).append("=").append(URLEncoder.encode(e.getValue(),"UTF-8"));
		    }
		    huc.setRequestProperty("Content-Length", "" + content.toString().getBytes().length);
		    DataOutputStream out = new DataOutputStream(huc.getOutputStream());
			out.writeBytes(content.toString());
			out.flush();
			out.close();
		    int code = huc.getResponseCode();
		    StringBuffer result = new StringBuffer();
		    BufferedReader in = new BufferedReader( new InputStreamReader(huc.getInputStream()));
	    	while (in.ready()) {
	    		result.append(in.readLine());	
	    	}
	    	huc.disconnect();
		    if (code >= 200 && code < 300) {
			    //Codes 200 - 300 indicate a correct response
		    } else {
		    	throw new RestException("HTTP error occured: " + code);
		    }		    
			return result.toString();
		} catch (MalformedURLException e1) {
			throw new RestException("Incorrect URL format",-1,e1);
		} catch (IOException e2) {
			throw new RestException("Error connecting to REST service",-1,e2);
		}
	}
}
