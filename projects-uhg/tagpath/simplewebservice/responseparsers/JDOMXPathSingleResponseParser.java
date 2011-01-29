package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSJDOMUtils;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;

public abstract class JDOMXPathSingleResponseParser extends JDOMSimpleResponseParser 
{
	private static final Logger log = Logger.getLogger(JDOMXPathSingleResponseParser.class);
	String initialXPath = null;
	boolean nullStrict = true;

	public Object processDOM(Document domDoc)
	{		
		// apply xpath
		Object value = null;
		if (StringUtils.isNotEmpty(initialXPath)) {
			value = SimpleWSJDOMUtils.execSingleXPath(domDoc, getInitialXPath());
			if (value == null && nullStrict) {
				// if the initialXPath returns null, the response is likely an error message
				if (log.isDebugEnabled())try{log.debug("Initial XPath "+initialXPath+" produced null result for request "+SimpleWSJDOMUtils.serializeDOMToString(domDoc));}catch(Exception e){}
				throw new SimpleResponseParsingException("Initial XPath "+initialXPath+" produced null result");
			}
		}
		
		// parse to return object
		Object responseObject = parseValue(value);
		
		return responseObject;
	}
	
	public abstract Object parseValue(Object value);

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
