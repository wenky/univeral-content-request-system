package com.cem.lweb.core.viewprocessors;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;


import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.cem.base.EEx;
import com.cem.base.Lg;
import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.interfaces.IViewProcessor;
import com.cem.lweb.loadresource.ResourceLoader;

public class VelocityView implements IViewProcessor
{
	public static VelocityEngine ve = null;
	static {
		try { 
			ve = new VelocityEngine();
			ve.init();
		} catch (Exception e) {
            /*-ERROR-*/Lg.err("Velocity Engine initialization error ",e);
            throw EEx.create("VelocityView-ClassInit","Velocity Engine initialization error ",e);
		}
	}
	
	public String renderView(Map config, String resource) 
	{
		String resourcecontent = ResourceLoader.loadWebResource(ThreadData.getServletContext(),resource); 
		
		VelocityContext velocityctx = new VelocityContext();
		velocityctx.put("C",ThreadData.getActionContext());
		velocityctx.put("request",ThreadData.getHttpRequest());
		velocityctx.put("response",ThreadData.getHttpResponse());
		velocityctx.put("session",ThreadData.getHttpRequest().getSession(false));
		
		String result = null;
        StringWriter buff = new StringWriter();
        try { 
            ve.evaluate(velocityctx, buff, null, new StringReader(resourcecontent));
            result = buff.toString();
        } catch (ParseErrorException pee) {
            /*-WARN-*/Lg.wrn("Velocity Processing syntax exception [%s] - [%s] [%d:%d]",resource,pee.getInvalidSyntax(),pee.getLineNumber(),pee.getColumnNumber(),pee);
            result = "Parse Exception in template "+resource+"<BR>\n"+
                     "Invalid syntax: "+pee.getInvalidSyntax()+"<BR>\n"+
                     "on Line "+pee.getLineNumber()+" Column "+pee.getColumnNumber()+"<BR>\n"+
                     "Exception message: "+pee.getMessage();
        } catch (MethodInvocationException mie) {
            /*-WARN-*/Lg.wrn("Velocity Processing method invocation error [%s] - [%s] [%d:%d]",resource,mie.getMethodName(),mie.getLineNumber(),mie.getColumnNumber(),mie);
            result = "Method Invocation Exception in template "+resource+"<BR>\n"+
                     "Method Name: "+mie.getMethodName()+"<BR>\n"+
                     "on Line "+mie.getLineNumber()+" Column "+mie.getColumnNumber()+"<BR>\n"+
                     "Exception message: "+mie.getMessage();
        } catch (ResourceNotFoundException rnfe) {
            /*-WARN-*/Lg.wrn("Velocity Processing resource not found error [%s]",resource,rnfe);
            result = "Resource Not Found Exception in template "+resource+"<BR>\n"+"Exception message: "+rnfe.getMessage();                
        } catch (IOException ioe) {
            /*-WARN-*/Lg.wrn("Velocity Processing ioexception on [%s]",resource,ioe);
            result = "Unexpected Input/Output Error in template "+resource+"<BR>\n"+"Exception message: "+ioe.getMessage();                
        }
		
		
		return result;
	}
	

}
 