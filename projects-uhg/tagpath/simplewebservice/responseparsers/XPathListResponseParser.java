package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSUtils;

public abstract class XPathListResponseParser extends DOMSimpleResponseParser 
{
	String initialXPath = null;

	public Object processDOM(Document domDoc)
	{		
		// apply xpath
		NodeList nodelist = SimpleWSUtils.execNodeListXPath(domDoc, getInitialXPath());
		
		// parse to return object
		Object responseObject = parseNodeList(nodelist);
		
		return responseObject;
	}
	
	public abstract Object parseNodeList(NodeList nodelist);

	public String getInitialXPath() {
		return initialXPath;
	}

	public void setInitialXPath(String initialXPath) {
		this.initialXPath = initialXPath;
	}
	
	

}
