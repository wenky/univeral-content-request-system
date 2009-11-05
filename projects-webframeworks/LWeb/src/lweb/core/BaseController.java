package lweb.core;

import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lbase.EEx;
import lbase.Lg;
import lconfig.GenericFileSystemConfigLoader;
import lcontext.Context;
import lcontext.IContextItem;
import lweb.core.configload.IConfigXStream;
import lweb.core.interfaces.IAuthenticator;
import lweb.core.interfaces.IProgit;
import lweb.core.util.CfgUtils;
import lweb.loadresource.RequestContext;
import lweb.loadresource.ResourceLoader;

import com.thoughtworks.xstream.XStream;

public class BaseController extends HttpServlet 
{

	public static final String TASKLISTKEY = "task";
    
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
                
        Map processingcontext = new HashMap();
        Context actioncontext = ActionContext.getActionContext(getServletContext(), request, null/*argmap*/, processingcontext, null/*pluginconfig*/, WebConfig);

        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        // threadlocal the request and response so they are accessible at all times by plugins, etc
        /*-trc-*/Lg.trc("set threadlocal references");
        RequestContext.setHttpObjects(request, response, getServletContext(), WebConfig, actioncontext);        
        // get action
        /*-trc-*/Lg.trc("get action from request");
        String fullaction = getActionFromRequest(request); // currently using extra path info
        String actionname = getActionName(fullaction);
        String actioninstruction = getActionInstruction(fullaction);
        
        String responsediv = executeAction(actionname, actioncontext);
        
        // check for .new instruction
        if (".frame".equalsIgnoreCase(actioninstruction)) {
        	// put the output of the action generation into the request context 
        	request.setAttribute("actionresponse", responsediv);
        	// determine the frame
        	String frameaction = (String)actioncontext.get("frameaction");        	
        	// execute the framing action
        	responsediv = executeAction(frameaction,actioncontext);
        }
        
        return responsediv;
                
    }
        

    public String executeAction(String actionname, Context actioncontext) throws ServletException, IOException 
    {

        String responsediv = null;
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
                    responsediv = executeActionPlugins(actionname,actioncfg,"",actioncontext);
                } catch (EEx lse) {
                    // perform error action handling
                }                
            } else {
                responsediv = executeDefaultAction(actioncfg);            
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
    
    public String executeDefaultAction(Map actioncfg) 
    {
        String responsediv = null;
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
            responsediv = ResourceLoader.callActiveUrl(getServletContext(), RequestContext.getHttpRequest(), RequestContext.getHttpResponse(), resourcepath);
        } else if (vtl != null) {
            // output vtl
            responsediv = "Velocity support coming soon";
        } else if (html != null) {
            String resourcepath = (String)actioncfg.get("@Path") + html; 
            responsediv = ResourceLoader.loadWebResource(getServletContext(),resourcepath);
            // output html
        } //TODO: velocity/freemarker...
        return responsediv;
        
    }
    
    public String postprocess(String responsediv){ return responsediv;}
    
    public static final Map emptymap = new HashMap();
    public String executeActionPlugins(String actionname,Map actionconfig,String responsediv,Context actioncontext)
    {
        /*-INFO-*/Lg.inf("Process request"); //TODO: more in-depth trace logging on the request
        HttpServletRequest request = RequestContext.getHttpRequest();                
        Map progitcontext = new HashMap();

        // execute action (action will have been overridden with an erroraction in error/login redirect situations)
        /*-trc-*/Lg.trc("begin progit execution for action %s", actionname);
        // look for: TASK or Task or task or Tasks
        List progits = (List)actionconfig.get(TASKLISTKEY);
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
            
            // progit exec
            /*-trc-*/Lg.trc("EXECUTING");
            try { 
                /*-trc-*/Lg.trc("prepare actioncontext for plugin exec");
                if (progitdef.containsKey("Config")) {
                	actioncontext.setNamedContext(progitdef.get("Config"),"pluginconfig");
                } else {
                	actioncontext.setNamedContext(progitdef,"pluginconfig");                	
                }
                /*-trc-*/Lg.trc("parse arguments");
                Map pluginarguments = parseArguments(progitdef,responsediv,actioncontext);
                if (pluginarguments != null) {
                    /*-trc-*/Lg.trc("set arguments in context");
                    actioncontext.setNamedContext(pluginarguments,"scratchpad");
                }
                try { 
                    /*-trc-*/Lg.trc("====EXEC PROGIT====");                                                        
                    responsediv = progit.execute(progitdef,actioncontext,responsediv);
                    /*-trc-*/Lg.trc("===PROGIT EXEC DONE===");
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("progit exec error for %s", progitclass,e);
                    EEx progiterr = EEx.create("ProgitExecError", "progit exec error for %s",progitclass,e);
                    Map errctx = progiterr.getErrorContext();
                    errctx.put("Error.ActionName", actionname);
                    errctx.put("Error.ProgitIndex", i);
                    errctx.put("Error.ActionContext", actioncontext);
                    throw progiterr;
                }
                
                /*-trc-*/Lg.trc("cleanup context");
                actioncontext.clearNamedContext("pluginconfig");
                if (pluginarguments != null) {
                    actioncontext.clearNamedContext("scratchpad");
                }
                /*-trc-*/Lg.trc("end plugin exec loop");

                // TODO: jumps/redirects
                
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("progit load error for %s", progitclass,e);
                EEx progiterr = EEx.create("ActionExecError", "Action exec of %s in action %s threw error",progitclass,e);
                Map errctx = progiterr.getErrorContext();
                errctx.put("Error.ActionName", actionname);
                errctx.put("Error.ProgitIndex", i);
                errctx.put("Error.ActionContext", actioncontext);
                throw progiterr;
            }
        }
        
        return responsediv;
        
    }

    public Map parseArguments(Map progitdef, String responsediv, Context actioncontext) 
    {
        // process the progit args: newkey=source:sourcekey
        List progitargs = (List)progitdef.get("Args");        
        if (progitargs != null) {
            /*-trc-*/Lg.trc("process progit arguments");
            Map argmap = new HashMap();
            for (int arg = 0; arg < progitargs.size(); arg++) {
                /*-trc-*/Lg.trc("  arg #%d",arg);
                String argdef = (String)progitargs.get(arg);
                // parse: newkey, source, sourcekey
                String contextkey, source, oldkey;
                try { 
                    int idxeq = argdef.indexOf('=');
                    contextkey = argdef.substring(0,idxeq);
                    int idxcolon = argdef.indexOf('(',idxeq);
                    source = argdef.substring(idxeq+1,idxcolon);
                    oldkey = argdef.substring(idxcolon+1,argdef.length()-1);
                } catch (Exception e) {
                    throw EEx.create("BadProgitArg", "Parsing error of progit arg %s", argdef);
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
                    String[] vals = RequestContext.getHttpRequest().getParameterValues(oldkey);
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
                } else if ("action".equals(source)) {
                    String subdiv = null;
                    Context newactioncontext = ActionContext.getActionContext(getServletContext(), RequestContext.getHttpRequest(), null/*argmap*/, new HashMap(), null/*pluginconfig*/, WebConfig);
                    RequestContext.setActionContext(newactioncontext);
                    try { 
                        subdiv = executeAction(oldkey, newactioncontext);
                    } catch (Exception e) {
                        /*-ERROR-*/Lg.err("progit arg eval of nested action threw an exception %s",argdef,e);
                        throw EEx.create("ParseArg-NestedActionError","progit arg eval of nested action threw an exception %s",argdef,e);                        
                    }
                    RequestContext.setActionContext(actioncontext);
                    argmap.put(contextkey, subdiv);
                } else if ("pathinfo".equals(source)) {
                    String extrapathinfo = this.getActionPathInfo(RequestContext.getHttpRequest());
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
    
    public String getActionInstruction(String action)
    {    	
    	if (action.indexOf('.') != -1) {
    		return action.substring(action.indexOf('.'));
    	} else {
    		return "";
    	}
    }
    
    public String getActionName(String action)
    {
    	if (action.indexOf('.') != -1) {
    		return action.substring(0,action.indexOf('.'));
    	} else {
    		return action;
    	}    	
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
