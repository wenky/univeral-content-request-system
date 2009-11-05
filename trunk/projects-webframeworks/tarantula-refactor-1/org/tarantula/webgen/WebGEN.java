package org.tarantula.webgen;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

public class WebGEN implements PageComponentInterface {

    private Map WebGEN_Config; // <-- this config is loaded in the tarantula main servlet as part of cached PCI loading
    
    public void init() throws Exception 
    {
        // do nothing for now...
    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        // TODO Auto-generated method stub
        return null;
    }

}
