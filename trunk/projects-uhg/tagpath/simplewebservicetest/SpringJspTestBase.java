package com.uhg.ovations.portal.partd.simplewebservicetest;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.context.ApplicationContext;
import org.springframework.web.context.WebApplicationContext;

public abstract class SpringJspTestBase extends JspTestBase
{

	public Object getContext(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{
		Object appcontext = springAppContextLookup(request);
		return appcontext;
	}
	
	public Object springAppContextLookup(HttpServletRequest request) throws Exception
	{
		ServletContext ctx = request.getSession().getServletContext();
		ApplicationContext appContext = (ApplicationContext)ctx.getAttribute(WebApplicationContext.ROOT_WEB_APPLICATION_CONTEXT_ATTRIBUTE);
		return appContext;
	}
	

}
