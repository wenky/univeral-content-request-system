package com.cem.lweb.core.viewprocessors;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.cem.base.EEx;
import com.cem.base.Lg;
import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.interfaces.IViewProcessor;


public class GspView implements IViewProcessor
{

	public String renderView(Map config, String resource) 
	{
    	HttpServletRequest httpreq = ThreadData.getHttpRequest();
        try { 
	        String serverpath = httpreq.getScheme()+"://"+httpreq.getServerName()+":"+httpreq.getServerPort()+httpreq.getContextPath();
	        String gspfile = resource.substring(resource.lastIndexOf('/')+1);
        	String ctxpath = (String)ThreadData.getActionContext().getNamedContext("actionconfig").get("@Path");
	        String[] resourcepath = {serverpath+ctxpath};
	        GroovyScriptEngine gse = new GroovyScriptEngine(resourcepath); // how expensive is this?
	        Binding gbind = new Binding();
	        StringWriter strout = new StringWriter();
	        gbind.setVariable("C", ThreadData.getActionContext());
	        gbind.setVariable("request", httpreq);
	        gbind.setVariable("response", ThreadData.getHttpResponse());
	        gbind.setVariable("session", httpreq.getSession(false));
	        gbind.setVariable("out", strout);
	        // what else should we auto-bind?
	        Object retval = gse.run(gspfile,gbind);
	        String retvalstr = strout.toString();
	        return retvalstr;
        } catch (IOException ioe) {
            /*-ERROR-*/Lg.err("Groovy io error trying to exec %s",resource,ioe);
            throw EEx.create("DefaultGroovyActionError","Groovy io error trying to exec %s",resource,ioe);        	
        } catch (ResourceException re) {
            /*-ERROR-*/Lg.err("Groovy resource error trying to exec %s",resource,re);
            throw EEx.create("DefaultGroovyActionError","Groovy resource error trying to exec %s",resource,re);        	
        } catch (ScriptException se) {
            /*-ERROR-*/Lg.err("Groovy script error trying to exec %s",resource,se);
            throw EEx.create("DefaultGroovyActionError","Groovy script error trying to exec %s",resource,se);        	
        }
    }

}
