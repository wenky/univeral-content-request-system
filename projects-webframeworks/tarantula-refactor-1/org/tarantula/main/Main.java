package org.tarantula.main;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.InputStreamReader;
import java.io.StringWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.ServletConfig;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.tarantula.component.interfacedefinition.PageComponentInterface;
import org.tarantula.util.TarantulaLogger;

import com.thoughtworks.xstream.XStream;

public class Main extends HttpServlet 
{
	AppConfig appconfig;
    Map pci_cache = new HashMap();
	
	public void init(ServletConfig config)
	{        
        // get main system config file
        String configaliasfile = config.getInitParameter("TarantulaAliasFile");
        String tarantulaconfig = config.getInitParameter("TarantulaConfigFile");
        File aliasfile  = new File(configaliasfile); 
        File configfile = new File(tarantulaconfig);

        // prep config XStream deserialization processor 
        XStream configdeserializer = new XStream();
        
		// configure config XStream deserializer (class aliases, etc)
        Map aliases = null;
        try { 
            aliases = (Map)configdeserializer.fromXML(new BufferedReader(new FileReader(aliasfile)));
        } catch (FileNotFoundException fnf) {
            throw new NullPointerException("Invalid Tarantula alias file: "+configaliasfile);
        }
		// startup - deserialize config with XStream
        String alias = null, classname = null;
        try {
            Iterator i = aliases.keySet().iterator();
            while (i.hasNext())
            {
                alias = (String)i.next();
                classname = (String)aliases.get(alias);
                configdeserializer.alias(alias, Class.forName(classname));
            }
        } catch (ClassNotFoundException cnf) {
            throw new NullPointerException("Invalid class alias in config file: ["+alias+"] - ["+classname+"]");                        
        }
		// deserialize configuration
        try { 
            appconfig = (AppConfig)configdeserializer.fromXML(new BufferedReader(new FileReader(configfile)));
        } catch (FileNotFoundException fnf) {
            throw new NullPointerException("Tarantula config file not found: "+tarantulaconfig);            
        } catch (Exception e) {
            throw new NullPointerException("Invalid Tarantula config file: "+tarantulaconfig);            
            
        }
        // initialize system components (security managers, data sources, etc)
        
        // initialize cached pagecomponents
        Iterator i = appconfig.PageComponents.keySet().iterator();
        while (i.hasNext())
        {
            String key = (String)i.next();
            PageComponentInterface p = (PageComponentInterface)appconfig.PageComponents.get(key);
            try {
                p.init();
            } catch (Exception e) {
                NullPointerException npe = new NullPointerException("Page Component initialization error");
                npe.initCause(e);
                throw npe;
            }
            pci_cache.put(key,p);
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
		// decode request
		String pagename = req.getPathInfo().substring(1);        
		processPageRequest(req,resp,pagename);
	}
	
	public void processPageRequest(HttpServletRequest req, HttpServletResponse resp, String page)
	{

        // prep session
        HttpSession session = null;
        if ((session = req.getSession(false)) == null)
        {
            // init session
            session = req.getSession(true);
        }
		// execute request
		// get page's config
		Page requestedpage = (Page)appconfig.PageMap.get(page);
        // check security
        // TODO
        HashMap scratchpad = null;
        Object inputdoc = "";
		try {
			scratchpad = new HashMap();
            scratchpad.put("AppConfig",appconfig.GlobalConfig);
			scratchpad.put("HttpArguments",req.getParameterMap());
            scratchpad.put("Session",session);
            scratchpad.put("Request",req);
            scratchpad.put("Response",resp);
			for (int i=0; i < requestedpage.PageComponents.size(); i++)
			{
				// get next pagecomponent
                HashMap arguments = new HashMap();
				PageComponent step = (PageComponent)requestedpage.PageComponents.get(i);
				// prep component inputs
				if (step.Inputs != null)
				{
					Iterator inputs = step.Inputs.iterator();
					while (inputs.hasNext())
					{
						Input input = (Input)inputs.next();
						if (input.Type == null || "literal".equals(input.Type))
						{
							arguments.put(input.Name, input.Value);							
						}
                        // arg comes from http parameter
						else if ("http".equals(input.Type))
						{
							arguments.put(input.Name,req.getParameter(input.Value));
						}
                        // arg comes from scratchpad context
						else if ("context".equals(input.Type))
						{
							arguments.put(input.Name, scratchpad.get(input.Value));
						}
                        // arg comes from global application config map
						else if ("config".equals(input.Type))
						{
							arguments.put(input.Name, appconfig.GlobalConfig.get(input.Value));
						}
                        // arg comes from user's httpsession
                        else if ("session".equals(input.Type))
                        {
                            arguments.put(input.Name,req.getSession(false).getAttribute(input.Value));
                        }
                        // arg comes from previous pagecomponent's output
                        else if ("input".equals(input.Type))
                        {
                            arguments.put(input.Name,inputdoc);
                        }
                        // arg is a file to be loaded
                        else if ("file".equals(input.Type))
                        {
                            // load file contents as String as key's value
                            StringWriter filecontents = new StringWriter();
                            BufferedReader frdr = new BufferedReader(new FileReader(input.Value));
                            String line = null;
                            while ((line = frdr.readLine()) != null)
                            {
                                filecontents.write(line);
                            }
                            arguments.put(input.Name,filecontents.toString());
                        }
                        // arg is a file to be loaded
                        else if ("relativefile".equals(input.Type))
                        { 
                            // determine actual path of server root
                            File f = new File(".");
                            String basepath = f.getAbsolutePath();
                            // load file contents as String as key's value
                            StringWriter filecontents = new StringWriter();
                            BufferedReader frdr = new BufferedReader(new FileReader(input.Value));
                            String line = null;
                            while ((line = frdr.readLine()) != null)
                            {
                                filecontents.write(line);
                            }
                            arguments.put(input.Name,filecontents.toString());
                        }
                        // arg is the response of the given url
						else if ("url".equals(input.Type))
						{
                            String surl = req.getScheme()+"://"+req.getServerName()+":"+req.getServerPort()+"/"+input.Value+"";
							URL url = new URL(surl);
                            
                            HttpURLConnection connection = (HttpURLConnection)url.openConnection();
                            connection.setRequestMethod("GET");
                            connection.connect();
                            int responseCode = connection.getResponseCode();
                            if (responseCode != HttpURLConnection.HTTP_OK)
                            {
                                throw new NullPointerException("URL error: HTTP response not successful: "+responseCode);
                            }
                            StringBuffer buffer = new StringBuffer();
                            BufferedReader bufrdr = new BufferedReader(new InputStreamReader(connection.getInputStream()));
                            String line = null;
                            while ((line = bufrdr.readLine()) != null) {
                               buffer.append(line);
                               buffer.append('\n');
                            }
                            bufrdr.close();
                            
                            String urlcontents = buffer.toString();
                            
							arguments.put(input.Name,urlcontents);
						}
                        // literal map...
                        else if ("map".equals(input.Type))
                        {
                            arguments.put(input.Name, input.ValueMap);                         
                        }
                        // literal list...
                        else if ("list".equals(input.Type))
                        {
                            arguments.put(input.Name, input.ValueList);                         
                        }
                        
                        // TODO: make further customizable/configurable...
                        // TODO: deserialize from BASE64?
                        // TODO: deserialize with XStream?
					}
				}
                // for now...just instantiate
                // execute page component (PLU...)
                PageComponentInterface plu = (PageComponentInterface)appconfig.PageComponents.get(step.Component);
                if (plu == null)
                {
                    // try to instantiate dynamically...
                    plu = (PageComponentInterface)Class.forName(step.Component).newInstance();
                }
                Object outputdoc = plu.process(req,resp,arguments,scratchpad);;
                // process output
                // - pipeline the output
                if (step.Output == null)
                {
                    inputdoc = outputdoc;
                }
                // - put in user's http session
                else if ("session".equals(step.Output.Type))
                {
                    session.setAttribute(step.Output.Key,outputdoc);
                }
                // - put in scratchpad context
                else if ("context".equals(step.Output.Type))
                {
                    scratchpad.put(step.Output.Key,outputdoc);
                }            
            }
            
            // write out the response
            resp.getWriter().write(inputdoc.toString());
		} catch (Exception e)
		{
			if (requestedpage.Error != null)
			{
                session.setAttribute("ErrorContext",scratchpad);
				processPageRequest(req,resp,requestedpage.Error);				
			}
			else
            {
                // TODO
                
            }
		}
        
	}
    
}
