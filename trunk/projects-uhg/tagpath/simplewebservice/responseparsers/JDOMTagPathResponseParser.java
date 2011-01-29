package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import java.util.List;

import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleWSJDOMUtils;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;

public abstract class JDOMTagPathResponseParser extends JDOMSimpleResponseParser 
{
	private static final Logger log = Logger.getLogger(JDOMTagPathResponseParser.class);
	List<String> initialTagPath = null;
	boolean nullStrict = true;

	public Object processDOM(Document domDoc)
	{		
		// apply xpath
		Element element = domDoc.getRootElement();
		if (initialTagPath != null) {
			element = SimpleWSJDOMUtils.getTagPath(element, initialTagPath);
			if (element == null && nullStrict) {
				// if the initialXPath returns null, the response is likely an error message
				if (log.isDebugEnabled())try{log.debug("Initial TagPath produced null result for request "+SimpleWSJDOMUtils.serializeDOMToString(domDoc));}catch(Exception e){}
				throw new SimpleResponseParsingException("Initial XPath produced null result");
			}
		}
		
		// parse to return object
		Object responseObject = parseElement(element);		
		return responseObject;
	}
	
	public abstract Object parseElement(Element element);


	public List<String> getInitialTagPath() {
		return initialTagPath;
	}

	public void setInitialTagPath(List<String> initialTagPath) {
		this.initialTagPath = initialTagPath;
	}

	public boolean isNullStrict() {
		return nullStrict;
	}

	public void setNullStrict(boolean nullStrict) {
		this.nullStrict = nullStrict;
	}
	
	

}
