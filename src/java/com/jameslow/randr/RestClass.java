package com.managedlearning.common.api.rest;

import java.util.Date;
import java.util.Map;
import java.util.TreeMap;

public class RestClass {
	protected String apikey;
	protected String prefix;
	protected String[] reserved;
	protected String sigvar;
	protected String timevar;
	protected String apivar;
	protected String testvar;
	private static final String TIME = "time";
	private static final String SIG = "sig";
	private static final String APIKEY = "apikey";
	private static final String TEST = "test";
	public static final String TEST_ECHO = "echo";
	public static final String DELIM = ";";

	public RestClass(String apikey) {
		this(apikey,null);
	}
	public RestClass(String apikey, String prefix) {
		this.apikey = apikey;
		this.prefix = (prefix == null || "".compareTo(prefix) == 0 ?  "" : prefix + "_");
		this.sigvar = this.prefix + SIG;
		this.timevar = this.prefix + TIME;
		this.apivar = this.prefix + APIKEY;
		this.testvar = this.prefix + TEST;
		reserved = new String[4];
		reserved[0] = this.sigvar;
		reserved[1] = this.timevar;
		reserved[2] = this.apivar;
		reserved[3] = this.testvar;
	}
	public String getApiKey() {
		return apikey;
	}
	protected long getTime() {
		return (long) Math.floor((new Date()).getTime()/1000);
	}
	//Check the params are signed with the api key and can therefore be trusted
	public String signature(Map<String,String> params) throws RestException {
		String result = "";
		//Sort alphabetically by key name
		TreeMap<String,String> sorted = new TreeMap<String,String>(params);
		//Include the API key in the signature, even though we don't transmit
		sorted.put(apivar,apikey);
		//Map is already sorted alphabetically
		for (Map.Entry<String, String> e : sorted.entrySet()) {
			String key = e.getKey();
			if (key.compareTo(sigvar) != 0 &&
				("".compareTo(prefix) == 0 || (key.length() > prefix.length() && key.substring(0, prefix.length()).compareTo(prefix) == 0))) {
					result = result + key + e.getValue();
			}
		}
		try {
			return AeSimpleMD5.MD5(result);
		} catch (Exception e) {
			throw new RestException(e.getMessage(),-1,e);
		}
	}
}
