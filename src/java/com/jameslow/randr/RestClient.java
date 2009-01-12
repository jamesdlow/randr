package com.jameslow.randr;

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
import java.util.Iterator;

public class RestClient extends RestClass {
	private String url;
	private Map params = new HashMap();
	
	public RestClient(String url, String apikey) {
		this(url,apikey,null);
	}
	public RestClient(String url, String apikey, String prefix) {
		super(apikey, prefix);
		this.url = url;
	}
	//Clear all current params
	public void clearParams() {
		params = new HashMap();
	}
	//Add a parameter checking if its not one of the predefined reserved params
	public void addParam(String param, String value) throws RestException {
		String full = prefix + param;
		int i;
		for (i=0;i<reserved.length;i++) {
			checkParam(full, reserved[i]);
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
	public Map getParams() throws RestException {
		return getParams("");
	}
	//Get the params valid at this time
	public Map getParams(String test) throws RestException {
		Map finalparams = new HashMap(params);
		if (test != null && "".compareTo(test) != 0) { 
			finalparams.put(testvar,test);
		}
		finalparams.put(timevar,""+getTime());
		finalparams.put(sigvar,signature(finalparams));
		return finalparams;
	}
	//Post the params to the given url and return the contents, echo
	public String echoParams() throws RestException{
		return sendParams(TEST_ECHO);
	}
	//Post the params to the given url and return a result, echo
	public RestResult echoResult() throws RestException{
		return getResult(TEST_ECHO);
	}
	//Post the params to the given url and return a result
	public RestResult getResult() throws RestException{
		return getResult("");
	}
	//Post the params to the given url and return a result, optional test
	public RestResult getResult(String test) throws RestException{
		return new RestResult(sendParams(test));
	}
	//Post the params to the given url and return the contents
	public String sendParams() throws RestException{
		return sendParams("");
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
			Iterator it = getParams(test).entrySet().iterator();
			while (it.hasNext()) {
				Map.Entry e = (Map.Entry)it.next();
				if (firsttime) {
					firsttime = false;
				} else {
				content.append("&");
				}
				content.append((String)e.getKey()).append("=").append(URLEncoder.encode((String)e.getValue(),"UTF-8"));
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
