package org.webgen.core;

import groovy.lang.Binding;
import groovy.lang.Closure;
import groovy.servlet.AbstractHttpServlet;
import groovy.servlet.ServletCategory;
import groovy.util.GroovyScriptEngine;

import java.util.Date;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.codehaus.groovy.runtime.GroovyCategorySupport;


//GroovyProcess

// this class handles preprocessing before dispatching to the 
// specified Groovlet:
// - make sure session is active
// - verify with security manager
// - prep session with logger, etc. 
public class GroovyProcess extends AbstractHttpServlet 
{
    public static GroovyScriptEngine gse; 
	protected String controllerconfig;
	protected String exceptionconfig;
	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(GroovyProcess.class));	
	
	
	public void init(ServletConfig config) throws ServletException
	{
	    super.init(config);
		/*-CFG-*/String m = "init-"; 
		/*-INF-*/if(log.iOn)log.inf(m+"initializing servlet");
		/*-TRC-*/if(log.tOn)log.trc(m+"initializing groovy scripting engine instance");
        gse = new GroovyScriptEngine(this);
		/*-TRC-*/if(log.tOn)log.trc(m+"looking up servlet config parameters");
        controllerconfig = config.getInitParameter("Controllers");
		/*-TRC-*/if(log.tOn)log.trc(m+"controllers map: "+controllerconfig);
        exceptionconfig = config.getInitParameter("Exceptions");
		/*-TRC-*/if(log.tOn)log.trc(m+"exceptions map: "+exceptionconfig);
        final String initscript = config.getInitParameter("Initialization");
		/*-TRC-*/if(log.tOn)log.trc(m+"startup script: "+initscript);

		/*-TRC-*/if(log.tOn)log.trc(m+"setting up initialization binding context");
        final Binding initbinding = new Binding();
        initbinding.setVariable("context",config.getServletContext());
        initbinding.setVariable("application",config.getServletContext());
        initbinding.setVariable("binding",initbinding.getVariables()); // self-reference

        try {
			/*-TRC-*/if(log.tOn)log.trc(m+"instantiating initialization script closure");
	        Closure initialization = new Closure(gse) {
	            public Object call() {
	                try {
	                	return ((GroovyScriptEngine) getDelegate()).run(initscript, initbinding);
	                } catch (Exception e) {
	                	throw new RuntimeException("Initialization Error - initialization scripta threw error",e);
	            	}
	            }
	        };
			/*-TRC-*/if(log.tOn)log.trc(m+"calling initialization script...");
			Date starttime = new Date();
			initialization.call();
	        Date finishtime = new Date();
			/*-TRC-*/if(log.tOn)log.trc(m+"initalization script executed ["+(finishtime.getTime() - starttime.getTime())+"] milliseconds");
        } catch (Exception e) {
			/*-ERR-*/log.err(m+"initalization script execution threw an error",e);
			throw new RuntimeException("WebGEN initialization script threw an error",e);
        }
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		processRequest(req, resp);
	}
	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		processRequest(req, resp);
	}
	public void processRequest(HttpServletRequest req, HttpServletResponse resp)
	{
		Date startrequest = new Date();
		/*-CFG-*/String m = "processRequest-";
		/*-INF-*/if(log.iOn)log.inf(m+"Processing New Request ["+req.getRequestURI()+"]",null,req);
		// make sure session is active...
		/*-TRC-*/if(log.tOn)log.trc(m+"make sure session is active",null,req);
		req.getSession(true);
		
		// get initial controller request 
		/*-TRC-*/if(log.tOn)log.trc(m+"get initial controller from extra path info",null,req);
		if (req.getPathInfo() == null) 
		{
			/*-ERR-*/log.err(m+"no controller specified in request's extra path info",null,req);
			throw new NullPointerException("WebGEN unable to detect controller request in URL's Extra Path Info");
		}
		String controller = req.getPathInfo().substring(1);
		/*-TRC-*/if(log.tOn)log.trc(m+"controller ["+controller+"] requested",null,req);
		
		// prep the controller config lookup Closure
		// - this could be cached in init(), but would require a reload or special controller to do mapping refresh. If performance is
		//   unacceptable, we will cache...
		/*-TRC-*/if(log.tOn)log.trc(m+"setting up controller config closure",null,req);
		Date startcontrollerload = new Date();
		final String controllerconfigURI = controllerconfig;
		final Binding controllerconfigBinding = new ServletBinding(req,resp,servletContext);
        Closure configlookup = new Closure(gse) {
            public Object call() {
                try {
                	return ((GroovyScriptEngine) getDelegate()).run(controllerconfigURI, controllerconfigBinding);
                } catch (Exception e) {
                	throw new RuntimeException("Configuration Error - controller map threw error",e);
            	}
            }
        };
		Date finishcontrollerload = new Date();
		/*-TRC-*/if(log.tOn)log.trc(m+"done with controller config closure setup - "+(finishcontrollerload.getTime() - startcontrollerload.getTime()),null,req);
		
		// look up the groovy script that maps to the current controller request...		
		/*-TRC-*/if(log.tOn)log.trc(m+"looking up controller map");
		startcontrollerload = new Date();
		GroovyCategorySupport.use(ServletCategory.class, configlookup);
		finishcontrollerload = new Date();
		/*-TRC-*/if(log.tOn)log.trc(m+"done with controller map load - "+(finishcontrollerload.getTime() - startcontrollerload.getTime()));
        Map controllermap = (Map)controllerconfigBinding.getVariable("webgencontrollers");
		
    	// WebGEN/NEAT/Groovy main servlet!
		/*-TRC-*/if(log.tOn)log.trc(m+"setting up shared binding/context for controller execution(s)");
        final Binding controllerbinding = new ServletBinding(req,resp,servletContext);
        //controllerbinding.setVariable("binding",controllerbinding); // self-refer for easy access...
        boolean exceptionprocessing = false;
    	while (true)
    	{
            // get mapped controller script from Binding
    		List controllermapping = (List)controllermap.get(controller);
            final String controllerscript = (String)controllermapping.get(0);
            if (controllermapping.size() > 1) controllerbinding.setVariable("config",controllermapping.get(1));
    		/*-TRC-*/if(log.tOn)log.trc(m+"controller: "+controllerscript,null,req);
            // execute the current controller
            try {
            	// define script executor
        		/*-TRC-*/if(log.tOn)log.trc(m+"defining controller script closure",null,req);
        		Date starttime = new Date();
                Closure closure = new Closure(gse) { // funky...
                    public Object call() {
                        try {
                            return ((GroovyScriptEngine) getDelegate()).run(controllerscript, controllerbinding);
                        } catch (Exception e) {
                            throw new RuntimeException(e);
                        }
                    }
                };
                Date finishtime = new Date();
        		/*-TRC-*/if(log.tOn)log.trc(m+"controller script closure defined - "+(finishtime.getTime()-starttime.getTime()));
                // execute the script
        		/*-TRC-*/if(log.tOn)log.trc(m+"executing controller script");
        		starttime = new Date();
                GroovyCategorySupport.use(ServletCategory.class, closure);
                finishtime = new Date();
        		/*-TRC-*/if(log.tOn)log.trc(m+"controller script execution done - "+(finishtime.getTime()-starttime.getTime()));
            } catch (Exception e) {
            	// only look up exception recovery once
        		/*-TRC-*/if(log.tOn)log.trc(m+"Exception thrown during request processing",e,req);
            	if (!exceptionprocessing)
            	{
	            	// try to get exception mapping from exception map (like the controller map)
            		/*-TRC-*/if(log.tOn)log.trc(m+"loading exception handler mapping "+exceptionconfig);
	                final String exceptionmapping = exceptionconfig;
	                final Binding exceptionbinding = new ServletBinding(req,resp,servletContext); 
	                // load exception mapping...
	                Closure closure = new Closure(gse) {
	                    public Object call() {
	                        try {
	                            return ((GroovyScriptEngine) getDelegate()).run(exceptionmapping, exceptionbinding);
	                        } catch (Exception e) {                            	
	                            throw new RuntimeException("Configuration Error - exception map threw error",e);
	                        }
	                    }
	                };
	                // get forwarding to exception handler
            		/*-TRC-*/if(log.tOn)log.trc(m+"looking up exception map");
	                GroovyCategorySupport.use(ServletCategory.class, closure);
            		/*-TRC-*/if(log.tOn)log.trc(m+"exception map retrieved");
	                Map exceptionmap = (Map)exceptionbinding.getVariable("webgenexceptions");
	                req.setAttribute("Exception",e);
	                req.setAttribute("Exception-SourceController",controller);
	                controller = (String)exceptionmap.get(e.getClass().toString());
            		/*-TRC-*/if(log.tOn)log.trc(m+"exception handler controller: "+controller);
	                exceptionprocessing = true;
            	}
            	else // exception recovery threw an exception as well! time to give up and crap out...
            	{
            		/*-ERR-*/if(log.tOn)log.trc(m+"Exception recovery controller "+controller+" failed",e,req);
                    throw new RuntimeException("Exception Recovery threw error as well: ",e);
            	}
            }
            
            // what do we do now?
            // -- forward option
            // -- others?
            if (exceptionprocessing) {
            	// exception being processed, so don't break
        		/*-TRC-*/if(log.tOn)log.trc(m+"Next action: exception processing requested");
            }
            else if (controllerbinding.getVariables().containsKey("forward"))
            {            	
            	controller = (String)controllerbinding.getVariable("forward");
               	controllerbinding.getVariables().remove("forward");
        		/*-TRC-*/if(log.tOn)log.trc(m+"Next action: forward to another controller detected: "+controller);
            }
            else
            {
            	// no more controllers to loop toward, stop processing
            	// for now, last controller should provide view output, but we'll add a view mappings confgi
        		/*-TRC-*/if(log.tOn)log.trc(m+"Next action: cease processing, no forwards detected");
            	break;
            }
            
    	}
		Date finishrequest = new Date();
		/*-INF-*/if(log.iOn)log.inf(m+"setting successful response - "+(finishrequest.getTime()-startrequest.getTime()));
        resp.setContentType("text/html");
        resp.setStatus(HttpServletResponse.SC_OK);

	}

}
