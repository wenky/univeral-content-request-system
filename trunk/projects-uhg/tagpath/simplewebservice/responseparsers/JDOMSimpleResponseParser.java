package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import java.io.InputStream;

import org.apache.log4j.Logger;
import org.jdom.Document;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleResponseParser;
import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSJDOMUtils;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceException;

public abstract class JDOMSimpleResponseParser implements SimpleResponseParser
{	
	private static final Logger log = Logger.getLogger(JDOMSimpleResponseParser.class);
	
	public Object parseResponse(InputStream response) 
	{
		// DOM-parse response
		Document doc = SimpleWSJDOMUtils.parseXmlToDOM(response);
		if(log.isDebugEnabled())log.debug("JDOM response: "+SimpleWSJDOMUtils.serializeDOMToString(doc));
		try { 
			Object responseobject = processDOM(doc);
			return responseobject;
		} catch (SimpleServiceException sse) {
			// rethrow, nothing to see here...
			throw(sse);
		} catch (Exception e) {
			// for all others, re-package as parse exception
			throw new SimpleResponseParsingException("Exception in simple response parsing",e);
		}		
	}
	
	public abstract Object processDOM(Document domDoc);
	
}
