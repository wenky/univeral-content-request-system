package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import java.io.InputStream;

import org.w3c.dom.Document;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleResponseParser;
import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSUtils;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceException;

public abstract class DOMSimpleResponseParser implements SimpleResponseParser
{	
	public Object parseResponse(InputStream response) 
	{
		// DOM-parse response
		Document doc = SimpleWSUtils.parseXmlToDOM(response);
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
