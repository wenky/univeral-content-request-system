package com.uhg.ovations.portal.partd.simplewebservicetest;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.thoughtworks.xstream.XStream;

public abstract class XStreamJspTestBase extends JspTestBase
{

	public Object getContext(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		Object deserialized = xstreamSetup(request);
		return deserialized;
	}
	
	public Object xstreamSetup(HttpServletRequest request) throws Exception
	{
		XStream xs = new XStream();
		Object deserialized = xs.fromXML(request.getInputStream());
		return deserialized;
	}

	

}
