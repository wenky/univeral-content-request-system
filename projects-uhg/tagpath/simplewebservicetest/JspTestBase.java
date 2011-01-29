package com.uhg.ovations.portal.partd.simplewebservicetest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;


public abstract class JspTestBase
{
	public abstract void test(HttpServletRequest request, HttpServletResponse response, Object context, Writer sw) throws Exception;
	public abstract boolean canExecute(HttpServletRequest request, HttpServletResponse response, Object context, Writer sw) throws Exception;
	public abstract Object getContext(HttpServletRequest request, HttpServletResponse response) throws Exception;
	
	public StringWriter runTest(HttpServletRequest request, HttpServletResponse response) throws Exception 
	{	
		StringWriter sw = new StringWriter();
		try {
			Object context = getContext(request,response);
			if (canExecute(request,response,context,sw)) {
				test(request,response,context,sw);				
			} else {
				sw.write("THIS TEST CURRENTLY NOT ENABLED<BR>");
			}
		} catch (Exception e) {
			PrintWriter pw = new PrintWriter(sw);
			pw.write("---------------------------------<BR>");
			pw.write("----      ERROR IN TEST      ----<BR>");
			pw.write("---------------------------------<BR>");
			pw.write("Error in test execution: "+e.getMessage()+"<BR>");
			e.printStackTrace(pw);			
		}
		return sw;
	}
	
	

	
	

}
