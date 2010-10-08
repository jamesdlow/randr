package com.jameslow.randr;

import java.io.*;
import javax.xml.parsers.*;
import org.w3c.dom.*;
import org.xml.sax.*;
//import javax.xml.transform.*;
//import javax.xml.transform.dom.*;
//import javax.xml.transform.stream.*;

public class RestResult {
	private boolean status;
	private int code;
	private String message;
	private String result = null;

	public RestResult(boolean status, int code, String message) {
		this.status = status;
		this.code = code;
		this.message = message;
	}
	public RestResult(String result) throws RestException {
		try {
			this.result = result;
			if ("".compareTo(result) != 0) {
				DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
				DocumentBuilder db = dbf.newDocumentBuilder();
				Document dom = db.parse(new InputSource(new StringReader(result)));
				Element docEle = dom.getDocumentElement();
				if (RestClass.RESULT.compareTo(docEle.getNodeName()) != 0) {
					throw new RestException("Root node in "+result+" must be "+RestClass.RESULT+".",-1);
				}
				status = RestClass.SUCCESS.compareTo(getResult(docEle,RestClass.STATUS).toUpperCase()) == 0;
				code = Integer.parseInt(getResult(docEle,RestClass.CODE));
				message = getResult(docEle,RestClass.MESSAGE);
			}
		} catch(RestException e) {
			throw e;
		} catch(Exception e) {
			throw new RestException("Error parsing "+result+".",-1,e);
		}
	}
	
	private String getResult(Element el, String node) throws RestException {
		NodeList nl = el.getElementsByTagName(node);
		if (nl != null) {
			if (nl.getLength() != 1) {
				throw new RestException("There should be exactly one node of type "+node+".",-1);
			} else {
				/*try {
					DOMSource domSource = new DOMSource((Element)nl.item(0));
					StringWriter writer = new StringWriter();
					StreamResult result = new StreamResult(writer);
					TransformerFactory tf = TransformerFactory.newInstance();
					Transformer transformer = tf.newTransformer();
					transformer.transform(domSource, result);
					return writer.toString().replaceAll("\\s+$", "").replaceAll("^\\s+", "");
				} catch(TransformerException e) {
					throw new RestException("Error converint XML to string.",-1,e);
				}*/
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
	public String getRawResult() {
		return result;
	}
}
