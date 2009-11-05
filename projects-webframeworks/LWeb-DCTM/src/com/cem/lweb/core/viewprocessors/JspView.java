package com.cem.lweb.core.viewprocessors;

import java.util.Map;

import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.interfaces.IViewProcessor;
import com.cem.lweb.loadresource.ResourceLoader;


public class JspView implements IViewProcessor
{

	public String renderView(Map config, String resource) 
	{
		String generatedview = ResourceLoader.callActiveUrl(ThreadData.getServletContext(), ThreadData.getHttpRequest(), ThreadData.getHttpResponse(), resource);
		return generatedview;
	}

}
