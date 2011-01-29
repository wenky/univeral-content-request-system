package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import java.util.List;

import org.jdom.Document;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSJDOMUtils;

public abstract class JDOMXPathListResponseParser extends JDOMSimpleResponseParser 
{
	String initialXPath = null;

	public Object processDOM(Document domDoc)
	{		
		// apply xpath
		List nodelist = SimpleWSJDOMUtils.execListXPath(domDoc.getRootElement(), getInitialXPath());
		
		// parse to return object
		Object responseObject = parseList(nodelist);
		
		return responseObject;
	}
	
	public abstract Object parseList(List nodelist);

	public String getInitialXPath() {
		return initialXPath;
	}

	public void setInitialXPath(String initialXPath) {
		this.initialXPath = initialXPath;
	}
	
	

}
