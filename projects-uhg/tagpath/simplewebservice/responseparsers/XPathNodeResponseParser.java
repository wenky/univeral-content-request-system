package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSUtils;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;

public abstract class XPathNodeResponseParser extends DOMSimpleResponseParser 
{
	private static final Logger log = Logger.getLogger(XPathNodeResponseParser.class);
	String initialXPath = null;
	boolean nullStrict = true;

	public Object processDOM(Document domDoc)
	{		
		// apply xpath
		Node node = domDoc;
		if (StringUtils.isNotEmpty(initialXPath)) {
			node = SimpleWSUtils.execNodeXPath(domDoc, getInitialXPath());
			if (node == null && nullStrict) {
				// if the initialXPath returns null, the response is likely an error message
				if (log.isDebugEnabled())try{log.debug("Initial XPath "+initialXPath+" produced null node result for request "+SimpleWSUtils.serializeDOMToString(domDoc));}catch(Exception e){}
				throw new SimpleResponseParsingException("Initial XPath "+initialXPath+" produced null node result");
			}
		}
		
		// parse to return object
		Object responseObject = parseNode(node);
		
		return responseObject;
	}
	
	public abstract Object parseNode(Node node);

	public String getInitialXPath() {
		return initialXPath;
	}

	public void setInitialXPath(String initialXPath) {
		this.initialXPath = initialXPath;
	}

	public boolean isNullStrict() {
		return nullStrict;
	}

	public void setNullStrict(boolean nullStrict) {
		this.nullStrict = nullStrict;
	}
	
	

}
