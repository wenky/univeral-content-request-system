package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import org.w3c.dom.Document;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSUtils;

public abstract class XPathStringResponseParser extends DOMSimpleResponseParser 
{
	String initialXPath = null;

	public Object processDOM(Document domDoc)
	{		
		// apply xpath
		String stringvalue = SimpleWSUtils.execStringXPath(domDoc, getInitialXPath());
		
		// parse to return object
		Object responseObject = parseString(stringvalue);
		
		return responseObject;
	}
	
	public abstract Object parseString(String stringvalue);

	public String getInitialXPath() {
		return initialXPath;
	}

	public void setInitialXPath(String initialXPath) {
		this.initialXPath = initialXPath;
	}
	
	

}
