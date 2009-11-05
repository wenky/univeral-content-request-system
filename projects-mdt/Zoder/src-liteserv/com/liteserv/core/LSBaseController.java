package com.liteserv.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lbase.EEx;
import lbase.Lg;

import com.liteserv.config.LSActionHandler;
import com.liteserv.config.LSArgument;
import com.liteserv.config.LSAuthenticator;
import com.liteserv.config.LSConfiguration;
import com.liteserv.config.LSPlugin;
import com.liteserv.config.LSResource;
import com.liteserv.config.namedprocessors.ExecXPath;
import com.liteserv.config.namedprocessors.LoadResource;
import com.liteserv.config.namedprocessors.VelocityTemplate;
import com.liteserv.core.utils.Is;
import com.liteserv.core.utils.LSErrorService;
import com.liteserv.core.utils.LSResourceLoader;
import com.liteserv.plugindefs.ILSAuthenticator;
import com.liteserv.plugindefs.ILSConfigLoader;
import com.liteserv.plugindefs.ILSConfigXStream;
import com.liteserv.plugindefs.ILSProgit;
import com.thoughtworks.xstream.XStream;

public class LSBaseController extends HttpServlet 
{

    
    // ------------------------ BEGIN Configuration Load / Servlet Initialization -------------------------
    
    
    /**
     * Load action mappings <br>
     *
     * @throws ServletException if an error occurs
     */
    String configurationfile = null;
    static LSConfiguration Configuration = null;
    public void init() throws ServletException
    {
        if (Configuration == null) 
            synchronized (Configuration)
            {
                /*-INFO-*/Lg.inf("initialization of servlet %s",this.getServletName());
                configurationfile = this.getInitParameter("configfile");
                /*-trc-*/Lg.trc(" -- config file: %s",configurationfile);
                        
                /*-trc-*/Lg.trc("Load webapp's LiteServ configuration");
                if (this.getInitParameter("configloader") == null)
                {
                    /*-trc-*/Lg.trc("performing default load of configuration (monolithic XStream deserialization of LSConfiguration)");
                    String configxstream = this.getInitParameter("configxstream");
                    /*-trc-*/Lg.trc(" -- config xstream (optional): %s",configxstream);
                    loadConfig(configxstream);
                    /*-trc-*/Lg.trc("done with default Liteserv configuration load");
                } else {
                    /*-trc-*/Lg.trc("get configuration loading class");
                    String lsconfigloader = this.getInitParameter("configloader");
                    /*-trc-*/Lg.trc("instantiate");
                    ILSConfigLoader cfgldr = (ILSConfigLoader)LSPluginLoader.loadPlugin(lsconfigloader);
                    /*-trc-*/Lg.trc("EXECUTE custom config loader");
                    Configuration = cfgldr.loadConfig(this);
                    /*-trc-*/Lg.trc("DONE with custom config loading");
                }
            }
    }
    
    public XStream getXStreamWithConfigAliases(String configxstream)
    {
        /*-INFO-*/Lg.inf("Get XStream with LiteServ Config Aliases");
        XStream xs = new XStream();
        /*-trc-*/Lg.trc("alias the core configuration objects");
        xs.alias("Authenticator", LSAuthenticator.class);
        xs.alias("LiteservConfig", LSConfiguration.class);
        xs.alias("Action", LSActionHandler.class);
        xs.alias("Progit", LSPlugin.class);
        xs.alias("Resource", LSResource.class);
        xs.alias("Arg", LSArgument.class);
        
        // standard/named action handler plugins ?Processor? ?ExecNode? ?ProgIt?
        /*-trc-*/Lg.trc("alias standardized progits");
        xs.alias("ExecXPath", ExecXPath.class);
        xs.alias("LoadResource", LoadResource.class);
        xs.alias("VelocityTemplate", VelocityTemplate.class);
        
        /*-trc-*/Lg.trc("check if custom xstream configurator was specified in servlet init params");
        try { 
            if (configxstream != null) {
                /*-trc-*/Lg.trc("specified, instantiate and execute");
                ILSConfigXStream cfgxs = (ILSConfigXStream)LSPluginLoader.loadPlugin(configxstream);
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
    
    public void loadConfig(String configloadxstream)
    {
        /*-INFO-*/Lg.inf("Load configuration with XStream");
        
        XStream xs = getXStreamWithConfigAliases(configloadxstream);
        
        /*-trc-*/Lg.trc("deserialize configuration");
        try { 
            Configuration = (LSConfiguration)xs.fromXML(LSResourceLoader.loadResource(configurationfile,null,null,null));
            // populate action names so the config file looks pwetty
            if (Configuration.Actions != null)
            {
                Iterator actions = Configuration.Actions.keySet().iterator();
                while (actions.hasNext())
                {
                    String actionname = (String)actions.next();
                    LSActionHandler actionhandler = (LSActionHandler)Configuration.Actions.get(actionname);
                    actionhandler.Action = actionname;
                }
            }
        } catch (EEx lse) {
            /*-ERROR-*/Lg.err("Configuration load %s not successful", configurationfile,lse);
            throw EEx.create("XS-CFG-LoadError","Configuration load %s not successful", configurationfile,lse);
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("Configuration deserialization error in configfile %s", configurationfile,e);
            throw EEx.create("XS-CFG-LoadError","Configuration deserialization error in configfile %s", configurationfile,e);
            
        }
        /*-trc-*/Lg.trc("DONE");
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
        String responsediv = null;

        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        // threadlocal the request and response so they are accessible at all times by plugins, etc
        /*-trc-*/Lg.trc("set threadlocal references");
        LSRequestContext.setHttpObjects(request, response, getServletContext(), Configuration);
        
        // determine handler security
        /*-trc-*/Lg.trc("get action from request");
        String actionname = getActionFromRequest(request);
        /*-trc-*/Lg.trc("lookup definition of action %s",actionname);
        LSActionHandler action = (LSActionHandler)Configuration.Actions.get(actionname);
        
        /*-trc-*/Lg.trc("check action security");
        String authfailure = this.authenticateAction(action);
        if (authfailure == null) // no news is good news
        {
            // exec div generation action
            /*-trc-*/Lg.trc("EXEC requested action %s",actionname);
            try { 
                responsediv = executeAction(action,"");
            } catch (EEx lse) {
                // perform error action handling
                /*-INFO-*/Lg.inf("error occurred in action EXEC %s, checking for error page action",action!=null?action.Action:null,lse);
                /*-trc-*/Lg.trc("get offending progit from request context");
                int sourceprogitindex = (Integer)request.getAttribute("ActionError.ProgitIndex"); 
                /*-trc-*/Lg.trc("looking up progit %d",sourceprogitindex);
                LSPlugin errorprogitdef = (LSPlugin)action.Progits.get(sourceprogitindex);
                /*-trc-*/Lg.trc("determine error action");
                String erroractionname = null;
                if (!Is.empty(errorprogitdef.ErrorAction)) {
                    erroractionname = errorprogitdef.ErrorAction;
                    /*-trc-*/Lg.trc("progit-specific error action specified: %s",erroractionname);
                } else if (!Is.empty(action.ErrorAction)) {
                    erroractionname = action.ErrorAction;
                    /*-trc-*/Lg.trc("action-specific error action specified: %s",action.Action);
                } else if (!Is.empty(Configuration.ErrorAction)) {
                    erroractionname = action.ErrorAction;
                    /*-trc-*/Lg.trc("global erroraction being used: %s",erroractionname);
                } 
                
                if (erroractionname != null) {
                    LSActionHandler erroraction = (LSActionHandler)Configuration.Actions.get(errorprogitdef.ErrorAction);
                    responsediv = executeAction(erroraction,responsediv);
                }
            }
        } else {
            /*-trc-*/Lg.trc("EXEC authorization failure action %s",authfailure);
            LSActionHandler authfailureaction = (LSActionHandler)Configuration.Actions.get(authfailure);
            try { 
                responsediv = executeAction(authfailureaction,"");
            } catch (EEx lse) {
                // perform error action handling
                /*-INFO-*/Lg.inf("error occurred in action EXEC %s, checking for error page action",action!=null?action.Action:null,lse);
                /*-trc-*/Lg.trc("get offending progit from request context");
                int sourceprogitindex = (Integer)request.getAttribute("ActionError.ProgitIndex"); 
                /*-trc-*/Lg.trc("looking up progit %d",sourceprogitindex);
                LSPlugin errorprogitdef = (LSPlugin)action.Progits.get(sourceprogitindex);
                /*-trc-*/Lg.trc("determine error action");
                String erroractionname = null;
                if (!Is.empty(errorprogitdef.ErrorAction)) {
                    erroractionname = errorprogitdef.ErrorAction;
                    /*-trc-*/Lg.trc("progit-specific error action specified: %s",erroractionname);
                } else if (!Is.empty(action.ErrorAction)) {
                    erroractionname = action.ErrorAction;
                    /*-trc-*/Lg.trc("action-specific error action specified: %s",action.Action);
                } else if (!Is.empty(Configuration.ErrorAction)) {
                    erroractionname = action.ErrorAction;
                    /*-trc-*/Lg.trc("global erroraction being used: %s",erroractionname);
                } 
                
                if (erroractionname != null) {
                    LSActionHandler erroraction = (LSActionHandler)Configuration.Actions.get(errorprogitdef.ErrorAction);
                    responsediv = executeAction(erroraction,responsediv);
                }
            }
        }
        /*-trc-*/Lg.trc("done");
        
        // send fully generated page
        return responsediv;
    }
    
    public String executeAction(LSActionHandler action,String responsediv)
    {
        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        HttpServletRequest request = LSRequestContext.getHttpRequest();
        LSActionContext context = new LSActionContext();
        // execute action (action will have been overridden with an erroraction in error/login redirect situations)
        /*-trc-*/Lg.trc("begin progit execution for action %s", action != null ? action.Action : null);
        for (int i=0; i < action.Progits.size(); i++) {
            /*-trc-*/Lg.trc("progit #%d",i);
            LSPlugin progitdef = (LSPlugin)action.Progits.get(i);
            context.config = progitdef.Config;
            ILSProgit progit = null;
            
            // progit class load
            /*-trc-*/Lg.trc("instantiating progit %s",progitdef != null ? progitdef.Class : null);
            try { 
                progit = (ILSProgit)LSPluginLoader.loadPlugin(progitdef.Class);
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("progit load error for %s", progitdef != null ? progitdef.Class : null,e);
                throw EEx.create("CfgErr-ActionClass","Controller Action plugin instantiation error: %s",progitdef.Class,e);
            } 
            
            // progit exec
            /*-trc-*/Lg.trc("EXECUTING");
            try { 
                // process the progit args
                if (progitdef.Args != null) {
                    /*-trc-*/Lg.trc("process progit arguments");
                    Map argmap = new HashMap();
                    for (int arg = 0; arg < progitdef.Args.size(); arg++) {
                        /*-trc-*/Lg.trc("  arg #%d",arg);
                        LSArgument argdef = (LSArgument)progitdef.Args.get(arg);
                        String contextkey = Is.empty(argdef.Key) ? argdef.Name : argdef.Key;
                        if (Is.empty(argdef.Source) || "literal".equals(argdef.Source)) {
                            /*-trc-*/Lg.trc("  literal key: %s val: %s",contextkey,argdef != null ? argdef.Value : null);
                            argmap.put(contextkey, argdef.Value);
                        } else if("http".equals(argdef.Source)) {
                            String[] vals = request.getParameterValues(argdef.Name);
                            if (vals != null && vals.length == 1) { 
                                /*-trc-*/Lg.trc("  http key: %s val: %s",contextkey,vals[0]);
                                argmap.put(contextkey, vals[0]);
                            } else {
                                /*-trc-*/if (Lg.trc()){String s = ""; if (vals!=null)for (int ss=0;ss<vals.length;ss++)s+=vals[ss];Lg.trc("  http key: %s val: ",contextkey,s);}
                                argmap.put(contextkey,vals);
                            }
                        } else if ("input".equals(argdef.Source)) {
                            /*-trc-*/Lg.trc("  input key: %s val: %s",contextkey,responsediv);
                            argmap.put(contextkey,responsediv);
                        } else if ("global".equals(argdef.Source)) {
                            ServletConfig srvcfg = getServletConfig();
                            Object val = srvcfg.getServletContext().getAttribute(argdef.Name);
                            /*-trc-*/Lg.trc("  global key: %s val: %s",contextkey,val.toString());
                            argmap.put(contextkey, val);
                        } else if ("resource".equals(argdef.Source)) {
                            String content = LSResourceLoader.loadResource(argdef.LocalUrl, argdef.Url, argdef.File, argdef.Resource);
                            /*-trc-*/Lg.trc("  resource key: %s val: %s",contextkey,content);                            
                            argmap.put(contextkey, content);
                        } else if ("context".equals(argdef.Source)) { // remap a context key
                            Object o = context.get(argdef.Name);
                            /*-trc-*/Lg.trc("  context key: %s val: %s",contextkey,o);                                                        
                            argmap.put(contextkey, o);
                        } else if ("config".equals(argdef.Source)) { // load a config key 
                            argmap.put(contextkey, progitdef.Config.get(argdef.Name));
                        } else if ("action".equals(argdef.Source)) {
                            // TODO...
                        } else if ("pathinfo".equals(argdef.Source)) {
                            String extrapathinfo = this.getActionPathInfo(request);
                            /*-trc-*/Lg.trc("  pathinfo key: %s val: %s",contextkey,extrapathinfo);                            
                            argmap.put(contextkey, extrapathinfo);
                        }
                    }
                    context.arguments = argmap;
                }
                try { 
                    /*-trc-*/Lg.trc("====EXEC PROGIT====");                                                        
                    responsediv = progit.execute(progitdef,request,context,responsediv);
                    /*-trc-*/Lg.trc("===PROGIT EXEC DONE===");                                                        
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("progit exec error for %s", progitdef != null ? progitdef.Class : null,e);
                    throw LSErrorService.packageActionError(action.Action,i,context,"ActionExecErr","progit exec error for %s", progitdef != null ? progitdef.Class : null,e);                        
                }
                
                // check for jumps/redirects
                if (context.redirectaction != null) {
                    // REDIRECT!!!! -- TODO: may need to do authenticators for the new action, lots of open questions here...
                    /*-trc-*/Lg.trc("redirecting to action %s",context.redirectaction);
                    // god this is sooooo beautifully dangerous and wrong: reset and repoint the for loop...
                   action = (LSActionHandler)Configuration.Actions.get(context.redirectaction);
                   i = -1; // The devil commands it! 
                   context.redirectaction = null;
                }
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("progit load error for %s", progitdef != null ? progitdef.Class : null,e);
                throw LSErrorService.packageActionError(action.Action,i,context,"ActionExecErr","Action exec of %s in action %s threw error",progitdef.Class, action.Action,e);                        
            }
        }
        return responsediv;
        
    }   
    
    public String authenticateAction(LSActionHandler action)
    {
        /*-INFO-*/Lg.inf("Authenticate action %s",action!=null?action.Action:null);
        boolean authenticated = true;
        Map context = new HashMap();        
        HttpServletRequest request = LSRequestContext.getHttpRequest();        
        String failureaction = null;
        
        /*-trc-*/Lg.trc("check if action requires authentication");
        if (action.Secured) 
        {            
            /*-trc-*/Lg.trc("check if we are supposed to execute the global authenticators (ie validate user)");
            if (!"yes".equalsIgnoreCase(action.AuthenticationOverride)) 
            {
                // exec global authenticators
                /*-trc-*/Lg.trc("executing GLOBAL authenticators");
                List authlist = LSRequestContext.getConfiguration().Authenticators;
                if (authlist != null) {
                    for (int i=0; i < authlist.size(); i++)
                    {   
                        /*-trc-*/Lg.trc("exec auth #%d",i);
                        LSAuthenticator auth = (LSAuthenticator)authlist.get(i);
                        ILSAuthenticator authenticator = null;
                        try { 
                            /*-trc-*/Lg.trc("instantiating auth plugin %s",auth!=null?auth.AuthClass:null);
                            authenticator = (ILSAuthenticator)LSPluginLoader.loadPlugin(auth.AuthClass);
                        } catch (Exception e) {
                            /*-ERROR-*/Lg.err("Load of Authenticator plugin threw an error",e);
                            EEx.create("CfgErr-AuthCreateError","Load of Authenticator plugin threw an error",e);                        
                        }
                        
                        try {
                            /*-trc-*/Lg.trc("EXEC AUTH plugin");
                            authenticated = authenticator.authenticate(request,auth.AuthConfig,context);
                            /*-trc-*/Lg.trc("AUTH EXEC result: %b",authenticated);
                            if (!authenticated) {
                                /*-trc-*/Lg.trc("auth failure --> store original action, failing plugin, and context");
                                // use request properties to store/communicate authorization failure state
                                request.setAttribute("AuthenticationFailure.OriginalAction", action);
                                request.setAttribute("AuthenticationFailure.Authenticator", auth);
                                request.setAttribute("AuthenticationFailure.Context", context);

                                if (auth.FailureAction != null) {
                                    failureaction = auth.FailureAction;
                                    /*-trc-*/Lg.trc("auth failure --> redirecting to auth plugin-specific action %s to handle failed authentication",failureaction);
                                } else {
                                    // TODO: ?more granularity?
                                    // use global action
                                    failureaction = Configuration.AuthenticationFailureAction;
                                    /*-trc-*/Lg.trc("auth failure --> redirecting to globally specified action %s to handle failed authentication",failureaction);
                                }
                                    
                                i = authlist.size(); // stop checking authentication
                            }
                        } catch (Exception e) {
                            /*-ERROR-*/Lg.err("Execution of authorization check of %s threw error",auth!=null?auth.AuthClass:null,e);
                            EEx.create("AuthCheckErr","Execution of authorization check of %s threw error",auth!=null?auth.AuthClass:null,e);                                                
                        }
                    }
                }
            }
            
            // exec action-specific authorization plugins
            if (authenticated) 
            {
                /*-trc-*/Lg.trc("see if action-specific authenticators specified");
                List authlist = action.Authenticators;            
                if (authlist != null) 
                {
                    /*-trc-*/Lg.trc("action-specific authenticators specified...");
                    for (int i=0; i < authlist.size(); i++)
                    {   
                        /*-trc-*/Lg.trc("exec auth #%d",i);
                        LSAuthenticator auth = (LSAuthenticator)authlist.get(i);
                        ILSAuthenticator authenticator = null;
                        try { 
                            /*-trc-*/Lg.trc("instantiating auth plugin %s",auth!=null?auth.AuthClass:null);
                            authenticator = (ILSAuthenticator)LSPluginLoader.loadPlugin(auth.AuthClass);
                        } catch (Exception e) {
                            /*-ERROR-*/Lg.err("Load of Authenticator plugin threw an error",e);
                            EEx.create("CfgErr-AuthCreateError","Load of Authenticator plugin threw an error",e);                        
                        }
                        
                        try {
                            /*-trc-*/Lg.trc("EXEC AUTH plugin");
                            authenticated = authenticator.authenticate(request,auth.AuthConfig,context);
                            /*-trc-*/Lg.trc("AUTH EXEC result: %b",authenticated);
                            if (!authenticated) {
                                /*-trc-*/Lg.trc("auth failure --> store original action, failing plugin, and context");
                                // use request properties to store/communicate authorization failure state
                                request.setAttribute("AuthenticationFailure.OriginalAction", action);
                                request.setAttribute("AuthenticationFailure.Authenticator", auth);
                                request.setAttribute("AuthenticationFailure.Context", context);

                                if (auth.FailureAction != null) {
                                    failureaction = auth.FailureAction;
                                    /*-trc-*/Lg.trc("auth failure --> redirecting to auth plugin-specific action %s to handle failed authentication",failureaction);
                                } else {
                                    // TODO: ?more granularity?
                                    // use global action
                                    failureaction = Configuration.AuthenticationFailureAction;
                                    /*-trc-*/Lg.trc("auth failure --> redirecting to globally specified action %s to handle failed authentication",failureaction);
                                }
                                    
                                i = authlist.size(); // stop checking authentication
                            }
                        } catch (Exception e) {
                            /*-ERROR-*/Lg.err("Execution of authorization check of %s threw error",auth!=null?auth.AuthClass:null,e);
                            EEx.create("AuthCheckErr","Execution of authorization check of %s threw error",auth!=null?auth.AuthClass:null,e);                                                
                        }
                    }
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
        if (actionname != null && actionname.indexOf('/') >= 0) {
            actionname = actionname.substring(0,actionname.indexOf('/'));
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
