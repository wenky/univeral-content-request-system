package com.uhg.ovations.portal.partd.simplewebservice;

import java.io.InputStream;

// implementations of this interface parse the raw inputstream of the response
public interface SimpleResponseParser 
{
	Object parseResponse(InputStream response);
}
