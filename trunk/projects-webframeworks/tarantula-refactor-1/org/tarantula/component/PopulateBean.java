package org.tarantula.component;

import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

// simplifies assignment of members of a "bean" kinda like struts form beans
// for now, is simply a hashmap in the session...
public class PopulateBean implements PageComponentInterface {

    public String getNickname() { return "PopulateBean"; }

    public void init()
    {
        // do nothing for now...
    }
    
    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String beankey = (String)arguments.get("FormBean");
        Map bean = null;
        if (beankey != null)
            bean = (Map)req.getSession(false).getAttribute(beankey);
        else
            // if no bean specified default to scratchpad context
            bean = scratchpad;
        
        Iterator i = arguments.keySet().iterator(); 
        while (i.hasNext())
        {
            
            String key = (String)i.next();
            String value = (String)arguments.get(key);
            // put in anything but the FormBean argument
            if (!"FormBean".equals(key))
                bean.put(key,value);
        }        
        return "";
    }


}
