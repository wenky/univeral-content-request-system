package com.uhg.ovations.portal.partd.simplewebservice.responseparsers;

import java.io.IOException;
import java.io.InputStream;

import org.apache.commons.io.IOUtils;

import com.uhg.ovations.portal.partd.simplewebservice.SimpleResponseParser;

public class SerializeResponseToString implements SimpleResponseParser
{	
	public Object parseResponse(InputStream response) 
	{
		try {
			// serialize response to string
			String responseAsString = IOUtils.toString(response);
			return responseAsString;
		} catch (IOException ioe) {
			throw new RuntimeException("IOException while serializing webservice response to string",ioe);
		}				
	}

}
