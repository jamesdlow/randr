package com.jameslow.randr;

import java.io.StringReader;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;

public class RestResult {
	private boolean status;
	private int code;
	private String message;

	public RestResult(String result) throws RestException {
		try {
			if ("".compareTo(result) != 0) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(new InputSource(new StringReader(result)));
				Element docEle = dom.getDocumentElement();
				if (RestClass.RESULT.compareTo(docEle.getNodeName()) != 0) {
					throw new RestException("Root node in result must be "+RestClass.RESULT+".",-1);
				}
				status = RestClass.SUCCESS.compareTo(getResult(docEle,RestClass.STATUS).toUpperCase()) == 0;
				code = Integer.parseInt(getResult(docEle,RestClass.CODE));
				message = getResult(docEle,RestClass.MESSAGE);
			}
		} catch(RestException e) {
			throw e;
		} catch(Exception e) {
			throw new RestException("Error parsing "+RestClass.RESULT+".",-1,e);
		}
	}
	
	private String getResult(Element e, String node) throws RestException {
		NodeList nl = e.getElementsByTagName(node);
		if (nl != null) {
			if (nl.getLength() != 1) {
				throw new RestException("There should be exactly one node of type "+node+".",-1);
			} else {
				return ((Element)nl.item(0)).getFirstChild().getNodeValue();
			}
		} else {
			throw new RestException("There should be exactly one node of type "+node+".",-1);
		}
	}
	
	public boolean getStatus() {
		return status;
	}
	public int getCode() {
		return code;
	}
	public String getMessage() {
		return message;
	}
}
