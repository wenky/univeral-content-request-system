package com.cem.lweb.core;

import groovy.lang.Binding;
import groovy.util.GroovyScriptEngine;
import groovy.util.ResourceException;
import groovy.util.ScriptException;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.cem.base.EEx;
import com.cem.base.Lg;
import com.cem.configloader.WebConfigLoader;
import com.cem.contextmap.ContextMap;
import com.cem.contextmap.IContextItem;
import com.cem.lweb.core.interfaces.IAuthenticator;
import com.cem.lweb.core.interfaces.ITaskHandler;
import com.cem.lweb.core.interfaces.IViewProcessor;
import com.cem.lweb.core.servletinit.IConfigXStream;
import com.cem.lweb.core.util.CfgUtils;
import com.cem.lweb.core.util.Is;
import com.cem.lweb.loadresource.ResourceLoader;
import com.thoughtworks.xstream.XStream;

public class BaseController extends HttpServlet 
{

	public static final String TASKLISTKEY = "task";
	public static final String SKIPVIEWKEY = "skipview";
	
	public static final String TASKPACKAGEROOTKEY = "[global defaults][tasks][taskpackages]";
	public static final String AUTHPACKAGEROOTKEY = "[global defaults][authentication][authpackages]";
    
    // ------------------------ BEGIN Configuration Load / Servlet Initialization -------------------------
    
    
    /**
     * Load action mappings <br>
     *
     * @throws ServletException if an error occurs
     */
    String ConfigurationBaseDirectory = null;
    static Map WebConfig = null;
    public void init() throws ServletException
    {
        /*-INFO-*/Lg.inf("initialization of servlet %s",this.getServletName());
        ConfigurationBaseDirectory = this.getInitParameter("configdirectory");
        if (ConfigurationBaseDirectory == null) ConfigurationBaseDirectory = "/lw-config/";
        /*-trc-*/Lg.trc(" -- config dir: %s",ConfigurationBaseDirectory);
                
        /*-trc-*/Lg.trc("Filesystem load");
        WebConfigLoader cfgldr = new WebConfigLoader();
        Map configloadcontext = new HashMap();
        // setup config load context with XStream, etc???                
        WebConfig = (Map)cfgldr.loadConfig(getServletContext(), ConfigurationBaseDirectory, configloadcontext);
    }
    
    public XStream getXStreamWithConfigAliases(String configxstream)
    {
        /*-INFO-*/Lg.inf("Get XStream with LiteServ Config Aliases");
        XStream xs = new XStream();
        /*-trc-*/Lg.trc("check if custom xstream configurator was specified in servlet init params");
        try { 
            if (configxstream != null) {
                /*-trc-*/Lg.trc("specified, instantiate and execute");
                IConfigXStream cfgxs = (IConfigXStream)PluginLoader.loadPlugin(configxstream,IConfigXStream.class);
                xs = cfgxs.setupXStream(xs);
                /*-trc-*/Lg.trc("Done with custom xstream configurator execution ");
            }
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("Error in instantiation or execution of XStream configurator %s", configxstream,e);
            throw EEx.create("LSInit-XStreamCfgInstanceException", "Error in instantiation or execution of XStream configurator %s", configxstream,e);                        
        }        
        /*-trc-*/Lg.trc("returning XStream object");
        return xs;
    }
    
    public void reloadConfig()
    {
        WebConfigLoader cfgldr = new WebConfigLoader();
        Map configloadcontext = new HashMap();
        // setup config load context with XStream, etc???                
        WebConfig = (Map)cfgldr.loadConfig(getServletContext(), ConfigurationBaseDirectory, configloadcontext);
    }
    
    
    // ------------------------ END Configuration Load / Servlet Initialization -------------------------
    
        

    /**
     * Process handles GET and POST requests to LiteServ's controller. This is typically invoke for full-page or (yuck) HTML-Frames page requests. 
     * First it checks if the request requires authentication based on the specified action. 
     * 
     * If so, it invokes the global and handler-specific authenticators usually for credential authentication such as:<br>
     * - cookie-based credentials <br>
     * - credentials in the request parameters l1=, l2=, etc. <br>
     * In general, authenticators should be capable of handling both types (the former is an existing login entered, the latter is probably
     * credentials in a shortcut, for REST compatibility)
     * 
     * If none is provided or the authentication fails, the ErrorAction action is invoked (either global or authenticator-specific), along 
     * with pass-thru data to reprocess the current request once the credentials have been obtained.
     *
     * Once login has succeeded/authenticated, the action handler is looked up and invoked, as per the usual MVC process. For
     * documentum we will likely implement action handlers which perform WDK-style scoping mechanisms. But the framework is intended
     * for generic near-stateless web clients in general. 
     * 
     * @param request the request send by the client to the server
     * @param response the response send by the server to the client
     * @throws ServletException if an error occurred
     * @throws IOException if an error occurred
     */
    public String process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
reloadConfig();
                
        ContextMap actioncontext = ActionContextMap.getActionContext(getServletContext(), request, null/*argmap*/, null/*pluginconfig*/, WebConfig, null);

        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        // threadlocal the request and response so they are accessible at all times by plugins, etc
        /*-trc-*/Lg.trc("set threadlocal references");
        ThreadData.setHttpObjects(request, response, getServletContext(), WebConfig, actioncontext);        
        // get action
        /*-trc-*/Lg.trc("get action from request");
        String fullaction = getActionFromRequest(request); // currently using extra path info
        
        /*-trc-*/Lg.trc("get config for action: %s",fullaction);        
        Map actioncfg = (Map)CfgUtils.lookup(WebConfig,fullaction);
        actioncontext.setNamedContext(actioncfg,"actionconfig");
        String responsediv = executeAction(fullaction, actioncontext,actioncfg);
                
        return responsediv;
                
    }
        

    public String executeAction(String actionname, ContextMap actioncontext, Map actioncfg) 
    {
        String responsediv = null;
        // scoping...TBD
        
        // determine handler security
        /*-trc-*/Lg.trc("check action security");
        String authfailure = null;
        if (!Is.yes(actioncfg.get("unsecured"))) {
            // authenticate
            authfailure = authenticateAction(actionname, actioncfg);
        }
        if (authfailure == null) // no news is good news
        {
            // determine if there is controller processing, or we are simply forwarding to a jsp/vtl/html
            if (actioncfg.containsKey(TASKLISTKEY)) {
                // complicated
                // exec div generation action
                /*-trc-*/Lg.trc("EXEC requested action %s",actionname);
                try { 
                    responsediv = executeActionPlugins(actionname,actioncfg,"",actioncontext);
                } catch (EEx lse) {
                    // perform error action handling... someday
                }                
            } else {
            	// check for groovy script
                responsediv = executeDefaultAction(actionname,actioncontext,actioncfg);            
            }
            
            if (!Is.yes(actioncfg.get(SKIPVIEWKEY))) {
                responsediv = executeDefaultView(actioncfg);                        	
            }
            
        } else {
            /*-trc-*/Lg.trc("EXEC authorization failure action %s",authfailure);
        }
        /*-trc-*/Lg.trc("postprocess");
        responsediv = postprocess(responsediv);
        /*-trc-*/Lg.trc("done");
        
        // send fully generated page
        return responsediv;
    }

    public String executeDefaultAction(String actionname, ContextMap actioncontext, Map actioncfg) 
    {
    	// look for .groovy script
        Map files = (Map)actioncfg.get("@Files");
        Iterator i = files.keySet().iterator();
        String script = null;
        while (i.hasNext()) {
            String key = (String)i.next();
            if (Is.endsIn(key, ".groovy") || Is.endsIn(key, ".gvy")) {
                script = key;
                break;
            }            
        }

        if (script != null) {
        	HttpServletRequest httpreq = ThreadData.getHttpRequest();
	        try { 
	        	String serverpath = httpreq.getScheme()+"://"+httpreq.getServerName()+":"+httpreq.getServerPort()+"/"+httpreq.getContextPath()+"/";	        	
		        String[] resourcepath = {serverpath+(String)actioncfg.get("@Path")};
		        GroovyScriptEngine gse = new GroovyScriptEngine(resourcepath); // how expensive is this?
		        Binding gbind = new Binding();
		        gbind.setVariable("context", actioncontext);
		        gse.run(script,gbind);
	        } catch (IOException ioe) {
	            /*-ERROR-*/Lg.err("Groovy io error trying to exec %s",script,ioe);
	            throw EEx.create("DefaultGroovyActionError","Groovy io error trying to exec %s",script,ioe);        	
	        } catch (ResourceException re) {
	            /*-ERROR-*/Lg.err("Groovy resource error trying to exec %s",script,re);
	            throw EEx.create("DefaultGroovyActionError","Groovy resource error trying to exec %s",script,re);        	
	        } catch (ScriptException se) {
	            /*-ERROR-*/Lg.err("Groovy script error trying to exec %s",script,se);
	            throw EEx.create("DefaultGroovyActionError","Groovy script error trying to exec %s",script,se);        	
	        }
        }
        
        return null;
        
    }

    
    public String executeDefaultView(Map actioncfg) 
    {
        String responsediv = null;
        // simple/default processing: look for jsp, vtl or html in config map to exec (groovy? etc?)
        //get files map
        Map files = (Map)actioncfg.get("@Files");
        Iterator i = files.keySet().iterator();
        String jsp = null;
        String vtl = null;
        String html = null;
        String gsp = null;
        // TODO: formalize configurable detection/view association
        while (i.hasNext()) {
            String key = (String)i.next();
            if (Is.endsIn(key, ".jsp")) {
                jsp = key;                    		
            }
            if (Is.endsIn(key, ".vtl")) {
                vtl = key;
            }
            if (Is.endsIn(key, ".htm") || Is.endsIn(key, ".html") || Is.endsIn(key,".js")) {
                html = key;
            }
            // "gsp" is a made-up extension for groovy-based views, to distinguish between groovy-based action handling
            if (Is.endsIn(key, ".gsp")) {
            	gsp = key;
            }
        }
        String viewprocessorclass = null;
        String resourcepath = null;
        // TODO: formalize configuration of this
        if (jsp != null) {
            resourcepath = (String)actioncfg.get("@Path") + jsp;            
            viewprocessorclass = "com.cem.lweb.core.viewprocessors.JspView";   
        } else if (vtl != null) {
            resourcepath = (String)actioncfg.get("@Path") + vtl;            
        	viewprocessorclass = "com.cem.lweb.core.viewprocessors.VelocityView";
        } else if (html != null) {
            resourcepath = (String)actioncfg.get("@Path") + html; 
        	viewprocessorclass = "com.cem.lweb.core.viewprocessors.StaticContent";
        } else if (gsp != null) {
            resourcepath = (String)actioncfg.get("@Path") + gsp; 
        	viewprocessorclass = "com.cem.lweb.core.viewprocessors.GspView";
        }
        try { 
        	if (viewprocessorclass != null) {
		        IViewProcessor viewprocessor = (IViewProcessor)Class.forName(viewprocessorclass).newInstance();
		        responsediv = viewprocessor.renderView(actioncfg, resourcepath);
        	}
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("View Processor Error",e);
            throw EEx.create("ViewError","View Processor Error",e);
        }
        
        if (actioncfg.containsKey("framed")) {
        	// put the output of the action generation into the request context 
        	ThreadData.getHttpRequest().setAttribute("actionresponse", responsediv);
        	// determine the frame
        	ContextMap actioncontext = ThreadData.getActionContext();
        	String frameaction = (String)actioncontext.get("frameaction");        	
        	// execute the framing action
            /*-trc-*/Lg.trc("get config for FRAME action: %s",frameaction);        
            Map frameactioncfg = (Map)CfgUtils.lookup(WebConfig,frameaction);
            actioncontext.setNamedContext(frameactioncfg, "actionconfig");
        	responsediv = executeAction(frameaction,actioncontext,frameactioncfg);
        }
        
        return responsediv;
        
    }
    
    public String postprocess(String responsediv){ return responsediv;}
    
    public static final Map emptymap = new HashMap();
    public String executeActionPlugins(String actionname,Map actionconfig,String responsediv,ContextMap actioncontext)
    {
        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        HttpServletRequest request = ThreadData.getHttpRequest();                

        // execute action (action will have been overridden with an erroraction in error/login redirect situations)
        /*-trc-*/Lg.trc("begin taskhdlr execution for action %s", actionname);
        // look for: TASK or Task or task or Tasks
        Object taskobj = actionconfig.get(TASKLISTKEY);
        List taskhdlrs = null;
        if (taskobj instanceof String) {
        	taskhdlrs = new ArrayList();
        	Map map = new HashMap();
        	map.put("Class", taskobj);
        	taskhdlrs.add(map);
        } else if (taskobj instanceof List) {
        	taskhdlrs = (List)taskobj;        	
        }
        
        for (int i=0; i < taskhdlrs.size(); i++) {
            /*-trc-*/Lg.trc("Task Handler #%d",i);
            Map taskhdlrdef = (Map)taskhdlrs.get(i);
            ITaskHandler taskhdlr = null;
            
            // taskhdlr class load
            String taskhdlrclass = (String)taskhdlrdef.get("Class");
            /*-trc-*/Lg.trc("instantiating taskhdlr %s",taskhdlrclass);
            try { 
                taskhdlr = (ITaskHandler)PluginLoader.fuzzyLoadPlugin(taskhdlrclass,ITaskHandler.class,(List)CfgUtils.lookup(WebConfig, TASKPACKAGEROOTKEY)); 
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("taskhdlr load error for %s", taskhdlrclass,e);
                throw EEx.create("CfgErr-ActionClass","Controller Action plugin instantiation error: %s",taskhdlrclass,e);
            } 
            
            // taskhdlr exec
            /*-trc-*/Lg.trc("EXECUTING");
            try { 
                /*-trc-*/Lg.trc("prepare actioncontext for plugin exec");
                if (taskhdlrdef.containsKey("Config")) {
                	actioncontext.setNamedContext(taskhdlrdef.get("Config"),"pluginconfig");
                } else {
                	actioncontext.setNamedContext(taskhdlrdef,"pluginconfig");                	
                }
                /*-trc-*/Lg.trc("parse arguments");
                Map pluginarguments = parseArguments(taskhdlrdef,responsediv,actioncontext);
                if (pluginarguments != null) {
                    /*-trc-*/Lg.trc("set arguments in context");
                    actioncontext.setNamedContext(pluginarguments,"scratchpad");
                }
                try { 
                    /*-trc-*/Lg.trc("====EXEC TASK HANDLER====");                                                        
                    responsediv = taskhdlr.execute(taskhdlrdef,actioncontext,responsediv);
                    /*-trc-*/Lg.trc("===TASK HANDLER EXEC DONE===");
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("taskhdlr exec error for %s", taskhdlrclass,e);
                    EEx taskhdlrerr = EEx.create("TaskHandlerExecError", "taskhdlr exec error for %s",taskhdlrclass,e);
                    Map errctx = taskhdlrerr.getErrorContext();
                    errctx.put("Error.ActionName", actionname);
                    errctx.put("Error.TaskHandlerIndex", i);
                    errctx.put("Error.ActionContext", actioncontext);
                    throw taskhdlrerr;
                }
                
                /*-trc-*/Lg.trc("cleanup context");
                actioncontext.clearNamedContext("pluginconfig");
                if (pluginarguments != null) {
                    actioncontext.clearNamedContext("scratchpad");
                }
                /*-trc-*/Lg.trc("end plugin exec loop");

                // TODO: jumps/redirects
                
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("taskhdlr load error for %s", taskhdlrclass,e);
                EEx taskhdlrerr = EEx.create("ActionExecError", "Action exec of %s in action %s threw error",taskhdlrclass,e);
                Map errctx = taskhdlrerr.getErrorContext();
                errctx.put("Error.ActionName", actionname);
                errctx.put("Error.TaskHandlerIndex", i);
                errctx.put("Error.ActionContext", actioncontext);
                throw taskhdlrerr;
            }
        }
        
        return responsediv;
        
    }

    public Map parseArguments(Map taskhdlrdef, String responsediv, ContextMap actioncontext) 
    {
        // process the taskhdlr args: newkey=source:sourcekey
        List taskhdlrargs = (List)taskhdlrdef.get("Args");        
        if (taskhdlrargs != null) {
            /*-trc-*/Lg.trc("process taskhdlr arguments");
            Map argmap = new HashMap();
            for (int arg = 0; arg < taskhdlrargs.size(); arg++) {
                /*-trc-*/Lg.trc("  arg #%d",arg);
                String argdef = (String)taskhdlrargs.get(arg);
                // parse: newkey, source, sourcekey
                String contextkey, source, oldkey;
                try { 
                    int idxeq = argdef.indexOf('=');
                    contextkey = argdef.substring(0,idxeq);
                    int idxcolon = argdef.indexOf('(',idxeq);
                    source = argdef.substring(idxeq+1,idxcolon);
                    oldkey = argdef.substring(idxcolon+1,argdef.length()-1);
                } catch (Exception e) {
                    throw EEx.create("BadTaskHandlerArg", "Parsing error of taskhdlr arg %s", argdef);
                }

                // attempt to match source with named context 
                Object contextitem = actioncontext.getNamedContext(source);
                if (contextitem != null) {
                    if (contextitem instanceof Map) {
                        Map map = (Map)contextitem;
                        argmap.put(contextkey, map.get(oldkey));
                    } else if (contextitem instanceof IContextItem) {
                        IContextItem ctxitem = (IContextItem)contextitem;
                        argmap.put(contextkey, ctxitem.get(oldkey));                        
                    }                        
                } else if("http".equals(source)) {
                    String[] vals = ThreadData.getHttpRequest().getParameterValues(oldkey);
                    if (vals != null && vals.length == 1) { 
                        /*-trc-*/Lg.trc("  http key: %s val: %s",contextkey,vals[0]);
                        argmap.put(contextkey, vals[0]);
                    } else {
                        /*-trc-*/if (Lg.trc()){String s = ""; if (vals!=null)for (int ss=0;ss<vals.length;ss++)s+=vals[ss];Lg.trc("  http key: %s val: ",contextkey,s);}
                        argmap.put(contextkey,vals);
                    }
                } else if ("input".equals(source)) {
                    /*-trc-*/Lg.trc("  input key: %s val: %s",contextkey,responsediv);
                    argmap.put(contextkey,responsediv);
                } else if ("file".equals(source)) {
                    String content = ResourceLoader.loadFile(oldkey);
                    /*-trc-*/Lg.trc("  resource key: %s val: %s",contextkey,content);                            
                    argmap.put(contextkey, content);
                } else if ("url".equals(source)) {
                    String content = ResourceLoader.loadUrl(oldkey);
                    /*-trc-*/Lg.trc("  resource key: %s val: %s",contextkey,content);                            
                    argmap.put(contextkey, content);
                } else if ("localurl".equals(source)) {
                    String content = ResourceLoader.loadWebResource(getServletContext(),oldkey);
                    /*-trc-*/Lg.trc("  resource key: %s val: %s",contextkey,content);                            
                    argmap.put(contextkey, content);
                } else if ("resource".equals(source)) {
                    String content = ResourceLoader.loadKeyedResource(oldkey);
                    /*-trc-*/Lg.trc("  resource key: %s val: %s",contextkey,content);                            
                    argmap.put(contextkey, content);
                } else if ("pathinfo".equals(source)) {
                    String extrapathinfo = this.getActionPathInfo(ThreadData.getHttpRequest());
                    /*-trc-*/Lg.trc("  pathinfo key: %s val: %s",contextkey,extrapathinfo);                            
                    argmap.put(contextkey, extrapathinfo);
                }
            }
            return argmap;
        }
        return null;
    }
        
    
    public String authenticateAction(String actionname, Map actionconfig)
    {
        HttpServletRequest request = ThreadData.getHttpRequest();        
        String failureaction = null;
        boolean authenticated = false;
        
        List authlist = null;
        if (actionconfig.containsKey("Authentication")) {
            //action-specific authenticator
            authlist = (List)CfgUtils.lookup(actionconfig, "[authentication][authenticators]");
        } else {        
            // exec global authenticators
            /*-trc-*/Lg.trc("get GLOBAL authenticators");
            authlist = (List)CfgUtils.lookup(WebConfig, "[global defaults][authentication][authenticator]");
        }
        
        if (authlist != null) {
            for (int i=0; i < authlist.size(); i++)
            {   
                /*-trc-*/Lg.trc("exec auth #%d",i);
                Map auth = (Map)authlist.get(i);
                IAuthenticator authenticator = null;
                try { 
                    /*-trc-*/Lg.trc("instantiating auth plugin %s",auth!=null?auth.get("class"):null);
                    authenticator = (IAuthenticator)PluginLoader.fuzzyLoadPlugin((String)auth.get("class"),IAuthenticator.class,(List)CfgUtils.lookup(WebConfig, AUTHPACKAGEROOTKEY));
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("Load of Authenticator plugin threw an error",e);
                    EEx.create("CfgErr-AuthCreateError","Load of Authenticator plugin threw an error",e);                        
                }
                
                try {
                    /*-trc-*/Lg.trc("EXEC AUTH plugin");
                    authenticated = authenticator.authenticate((Map)auth.get("config"), actionname, actionconfig);
                    /*-trc-*/Lg.trc("AUTH EXEC result: %b",authenticated);
                    if (!authenticated) {
                        /*-trc-*/Lg.trc("auth failure --> store original action, failing plugin, and context");
                        if (auth.containsKey("authfailaction")) {
                            failureaction = (String)auth.get("authfailaction");
                            /*-trc-*/Lg.trc("auth failure --> redirecting to auth plugin-specific action %s to handle failed authentication",failureaction);
                        } else {
                            // TODO: ?more granularity?
                            // use global action
                            failureaction = (String)CfgUtils.lookup(WebConfig, "[global defaults][authentication][authfailaction]");
                            /*-trc-*/Lg.trc("auth failure --> redirecting to globally specified action %s to handle failed authentication",failureaction);
                        }
                            
                        i = authlist.size(); // stop checking authentication
                    }
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("Execution of authorization check of %s threw error",auth!=null?auth.get("AuthClass"):null,e);
                    EEx.create("AuthCheckErr","Execution of authorization check of %s threw error",auth!=null?auth.get("AuthClass"):null,e);                                                
                }
            }
        }
            
        return failureaction;
        
    }
    
    public String getActionFromRequest(HttpServletRequest request)
    {
        // use extended path for action so it doesn't pollute the request params
        String actionname = request.getPathInfo();
        // extra path may look like: [Liteserv Action]/[additional info like a filename]
        // ...we only want the [Liteserv Action] part        
        if (actionname != null && actionname.indexOf('/') == 0) {
            if (actionname.indexOf('/', 1) > 0 )
                actionname = actionname.substring(1,actionname.indexOf('/',1));
            else
                actionname = actionname.substring(1);
        } else { 
            if (actionname.indexOf('/', 1) > 0 )
                actionname = actionname.substring(0,actionname.indexOf('/',1));
            else
                actionname = actionname.substring(0);            
        }
        return actionname;
    }
            
    public static String getActionPathInfo(HttpServletRequest request)
    {
        // use extended path for action so it doesn't pollute the request params
        String pathinfo = request.getPathInfo();
        String actionpathinfo = null;
        // extra path may look like: [Liteserv Action]/[additional info like a filename]
        // ...this strips out the [Liteserv Action]/
        if (pathinfo != null && pathinfo.indexOf('/') >=0) {
            actionpathinfo = pathinfo.substring(pathinfo.indexOf('/'));
        }
        return actionpathinfo;
    }
    

    /* route doGet and doPost to process */
    public void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    { 
        String responsediv = process(request,response);         
        response.getOutputStream().println(responsediv);
        response.getOutputStream().flush();
    }
    public void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    { 
        String responsediv = process(request,response);         
        response.getOutputStream().println(responsediv);
        response.getOutputStream().flush();
    }


    
    
}
