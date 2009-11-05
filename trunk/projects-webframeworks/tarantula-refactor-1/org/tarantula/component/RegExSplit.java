package org.tarantula.component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

public class RegExSplit implements PageComponentInterface {

    public String getNickname() { return "RegExSplit"; }

    public void init()
    {
        // do nothing for now...
    }
    
    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String regex = (String)arguments.get("Match");
        String source = (String)arguments.get("Source");
        Pattern p = Pattern.compile(regex);
        String[] split = p.split(source);
        return split;
    }

}
