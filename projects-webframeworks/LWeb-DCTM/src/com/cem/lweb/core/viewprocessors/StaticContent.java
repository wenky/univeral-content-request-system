package com.cem.lweb.core.viewprocessors;

import java.util.Map;

import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.interfaces.IViewProcessor;
import com.cem.lweb.loadresource.ResourceLoader;


public class StaticContent implements IViewProcessor
{

	public String renderView(Map config, String resource) 
	{
        String generatedview = ResourceLoader.loadWebResource(ThreadData.getServletContext(),resource);
		return generatedview;
	}

}