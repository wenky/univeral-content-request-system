package com.medtronic.documentum.ajax;

import java.util.Map;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public class UcfCallbackTest extends HttpServlet {

	public UcfCallbackTest(HttpServletRequest req, HttpServletResponse resp)
	{
		Map params = req.getParameterMap();
		String qstr = req.getQueryString();
		Cookie[] cooks = req.getCookies();
		String requrl = req.getRequestURL().toString();
		String requri = req.getRequestURI();
		params.getClass(); qstr.getClass(); cooks.getClass(); requrl.getClass(); requri.getClass(); 
		
	}
	
	public void service(HttpServletRequest req, HttpServletResponse resp)
	{
		Map params = req.getParameterMap();
		String qstr = req.getQueryString();
		Cookie[] cooks = req.getCookies();
		String requrl = req.getRequestURL().toString();
		String requri = req.getRequestURI();
		params.getClass(); qstr.getClass(); cooks.getClass(); requrl.getClass(); requri.getClass(); 
		
	}
	
}
