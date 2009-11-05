package org.tarantula.component;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

// applies a regular expression substitution 

public class RegExReplaceAll implements PageComponentInterface {

    public String getNickname() { return "RegExReplaceAll"; }

    public void init()
    {
        // do nothing for now...
    }
    
    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String regex = (String)arguments.get("Match");
        String replace = (String)arguments.get("Replace");
        String source = (String)arguments.get("Source");
        Pattern p = Pattern.compile(regex);
        Matcher m = p.matcher(source);
        String result = m.replaceAll(replace);
        return result;
    }

}
