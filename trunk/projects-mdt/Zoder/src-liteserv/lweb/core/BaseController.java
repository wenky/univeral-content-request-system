package lweb.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lbase.EEx;
import lbase.Lg;
import lconfig.GenericFileSystemConfigLoader;
import lresourceloading.RequestContext;
import lresourceloading.ResourceLoader;
import lweb.core.configload.IConfigXStream;
import lweb.core.interfaces.IAuthenticator;
import lweb.core.interfaces.IProgit;
import lweb.core.structs.Arg;
import lweb.core.util.CfgUtils;
import lweb.core.util.Is;

import com.thoughtworks.xstream.XStream;

public class BaseController extends HttpServlet 
{

    
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
        if (ConfigurationBaseDirectory == null) ConfigurationBaseDirectory = "/liteserv-config/";
        /*-trc-*/Lg.trc(" -- config dir: %s",ConfigurationBaseDirectory);
                
        /*-trc-*/Lg.trc("Filesystem load");
        GenericFileSystemConfigLoader cfgldr = new GenericFileSystemConfigLoader();
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
                IConfigXStream cfgxs = (IConfigXStream)PluginLoader.loadPlugin(configxstream);
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
        GenericFileSystemConfigLoader cfgldr = new GenericFileSystemConfigLoader();
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
        
        String responsediv = null;
        
        Map processingcontext = new HashMap();
        ActionContext actioncontext = new ActionContext(getServletContext(), request, null/*argmap*/, processingcontext, null/*pluginconfig*/, WebConfig);
        request.setAttribute("ActionContext", actioncontext);

        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        // threadlocal the request and response so they are accessible at all times by plugins, etc
        /*-trc-*/Lg.trc("set threadlocal references");
        RequestContext.setHttpObjects(request, response, getServletContext(), WebConfig, actioncontext);        
        // get action
        /*-trc-*/Lg.trc("get action from request");
        String actionname = getActionFromRequest(request); // currently using extra path info        
        /*-trc-*/Lg.trc("get config for action: %s",actionname);        
        Map actioncfg = (Map)CfgUtils.lookup(WebConfig,"[actions]["+actionname+']');
        // scoping...TBD
        
        // determine handler security
        
        /*-trc-*/Lg.trc("check action security");
        String authfailure = null;
        if (!"unsecured".equalsIgnoreCase((String)actioncfg.get("security"))) {
            // authenticate
            authfailure = authenticateAction(actionname, actioncfg);
        }
        if (authfailure == null) // no news is good news
        {
            // determine if there is controller processing, or we are simply forwarding to a jsp/vtl/html
            if (actioncfg.containsKey("Processing")) {
                // complicated
                // exec div generation action
                /*-trc-*/Lg.trc("EXEC requested action %s",actionname);
                try { 
                    responsediv = executeAction(actionname,actioncfg,"",actioncontext);
                } catch (EEx lse) {
                    // perform error action handling
                }                
            } else {
                // simple/default processing: look for jsp, vtl or html in config map to exec (groovy? etc?)
                //get files map
                Map files = (Map)actioncfg.get("@Files");
                Iterator i = files.keySet().iterator();
                String jsp = null;
                String vtl = null;
                String html = null;
                while (i.hasNext()) {
                    String key = (String)i.next();
                    if (key.length() > 4 && key.indexOf('.') != -1) {
                        if (".jsp".equalsIgnoreCase(key.substring(key.length()-4))) {
                            jsp = key;
                        }
                    }
                    if (key.length() > 4 && key.indexOf('.') != -1) {
                        if (".vtl".equalsIgnoreCase(key.substring(key.length()-4))) {
                            vtl = key;
                        }
                    }
                    if (key.length() > 4 && key.indexOf('.') != -1) {
                        if (".htm".equalsIgnoreCase(key.substring(key.length()-4))) {
                            html = key;
                        }
                    }
                    if (key.length() > 5 && key.indexOf('.') != -1) {
                        if (".html".equalsIgnoreCase(key.substring(key.length()-5))) {
                            html = key;
                        }
                    }
                    if (key.length() > 3 && key.indexOf('.') != -1) {
                        if (".js".equalsIgnoreCase(key.substring(key.length()-3))) {
                            html = key;
                        }
                    }
                }
                if (jsp != null)
                {
                    String resourcepath = (String)actioncfg.get("@Path") + jsp; 
                    responsediv = ResourceLoader.callActiveUrl(getServletContext(), request, response, resourcepath);
                } else if (vtl != null) {
                    // output vtl
                    responsediv = "Velocity support coming soon";
                } else if (html != null) {
                    String resourcepath = (String)actioncfg.get("@Path") + html; 
                    responsediv = ResourceLoader.loadWebResource(getServletContext(),resourcepath);
                    // output html
                }
            
            }
            
        } else {
            /*-trc-*/Lg.trc("EXEC authorization failure action %s",authfailure);
        }
        /*-trc-*/Lg.trc("postprocess");
        responsediv = postprocess(request,response,responsediv);
        /*-trc-*/Lg.trc("done");
        
        // send fully generated page
        return responsediv;
    }
    
    public String postprocess(HttpServletRequest request, HttpServletResponse response, String responsediv){ return responsediv;}
    
    public static final Map emptymap = new HashMap();
    public String executeAction(String actionname,Map actionconfig,String responsediv,ActionContext actioncontext)
    {
        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        HttpServletRequest request = RequestContext.getHttpRequest();                
        Map progitcontext = new HashMap();

        // execute action (action will have been overridden with an erroraction in error/login redirect situations)
        /*-trc-*/Lg.trc("begin progit execution for action %s", actionname);
        List progits = (List)actionconfig.get("Processing");
        for (int i=0; i < progits.size(); i++) {
            /*-trc-*/Lg.trc("progit #%d",i);
            Map progitdef = (Map)progits.get(i);
            IProgit progit = null;
            
            // progit class load
            String progitclass = (String)progitdef.get("Class");
            /*-trc-*/Lg.trc("instantiating progit %s",progitclass);
            try { 
                progit = (IProgit)PluginLoader.loadPlugin(progitclass);
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("progit load error for %s", progitclass,e);
                throw EEx.create("CfgErr-ActionClass","Controller Action plugin instantiation error: %s",progitclass,e);
            } 
            
            /*-trc-*/Lg.trc("set progit config in actioncontext");
            actioncontext.setPluginConfig((Map)progitdef.get("Config"));
            
            // progit exec
            /*-trc-*/Lg.trc("EXECUTING");
            try { 
                // process the progit args
                Map progitargs = (Map)progitdef.get("Args");
                if (progitargs != null) {
                    /*-trc-*/Lg.trc("process progit arguments");
                    Map argmap = new HashMap();
                    for (int arg = 0; arg < progitargs.size(); arg++) {
                        /*-trc-*/Lg.trc("  arg #%d",arg);
                        Arg argdef = (Arg)progitargs.get(arg);
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
                            String content = ResourceLoader.loadResource(argdef.LocalUrl, argdef.Url, argdef.File, argdef.Resource);
                            /*-trc-*/Lg.trc("  resource key: %s val: %s",contextkey,content);                            
                            argmap.put(contextkey, content);
                        } else if ("context".equals(argdef.Source)) { // remap a context key
                            Object o = actioncontext.getProcessingContextMap().get(argdef.Name);
                            /*-trc-*/Lg.trc("  context key: %s val: %s",contextkey,o);                                                        
                            argmap.put(contextkey, o);
                        } else if ("config".equals(argdef.Source)) { // load a config key 
                            argmap.put(contextkey, ((Map)((Map)progitdef).get("Config")).get(argdef.Name));
                        } else if ("action".equals(argdef.Source)) {
                            // TODO...
                        } else if ("pathinfo".equals(argdef.Source)) {
                            String extrapathinfo = this.getActionPathInfo(request);
                            /*-trc-*/Lg.trc("  pathinfo key: %s val: %s",contextkey,extrapathinfo);                            
                            argmap.put(contextkey, extrapathinfo);
                        }
                    }
                    /*-trc-*/Lg.trc("set argmap in action context");                    
                    actioncontext.setArgMap(argmap);
                } else {
                    actioncontext.setArgMap(null);
                }
                try { 
                    /*-trc-*/Lg.trc("====EXEC PROGIT====");                                                        
                    responsediv = progit.execute(progitdef,actioncontext,responsediv);
                    /*-trc-*/Lg.trc("===PROGIT EXEC DONE===");         
                    actioncontext.setPluginConfig(null);
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("progit exec error for %s", progitclass,e);
                    throw ErrorService.packageActionError(actionname,i,actioncontext,"ActionExecErr","progit exec error for %s", progitclass,e);                        
                }
                
                // check for jumps/redirects
//                if (context.redirectaction != null) {
//                    // REDIRECT!!!! -- TODO: may need to do authenticators for the new action, lots of open questions here...
//                    /*-trc-*/Lg.trc("redirecting to action %s",context.redirectaction);
//                    // god this is sooooo beautifully dangerous and wrong: reset and repoint the for loop...
//                   action = (LSActionHandler)WebConfig.Actions.get(context.redirectaction);
//                   i = -1; // The devil commands it! 
//                   context.redirectaction = null;
//                }
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("progit load error for %s", progitclass,e);
                throw ErrorService.packageActionError(actionname,i,actioncontext,"ActionExecErr","Action exec of %s in action %s threw error",progitclass,e);                        
            }
        }
        
        return responsediv;
        
    }   
    
    public String authenticateAction(String actionname, Map actionconfig)
    {
        HttpServletRequest request = RequestContext.getHttpRequest();        
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
                    authenticator = (IAuthenticator)PluginLoader.loadPlugin((String)auth.get("class"));
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
