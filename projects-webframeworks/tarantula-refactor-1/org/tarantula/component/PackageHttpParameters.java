package org.tarantula.component;

import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;

import javax.servlet.ServletRequest;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

/**
 * @author carl mueller
 *
 * Loads the requested URI (file, web service call, etc) into the specified
 * scope context and key. Default scope is request.
 * &lt;tar:Load source="" scope="" key=""/&gt;
 * - required attributes: source, key
 * 
 */

public class PackageHttpParameters implements PageComponentInterface
{
    public String getNickname() { return "PackageHttpParameters"; }

    public void init(){}
    
    boolean isAlpha(char c)
    {
        if (((int)c >= (int)'a' && (int)c <= (int)'z') ||
            ((int)c >= (int)'A' && (int)c <= (int)'Z'))
        {
            return true;
        }
        // not alpha...
        return false;
    }
    
    boolean isNum(char c)
    {
        if ((int)c >= (int)'0' && (int) c <= (int)'9')
        {
            return true;
        }
        return false;
    }
    
    //  this worked on the FIRST try (aside from bullshit ArrayList not having a setSize() method)
    void packageParameter(ServletRequest req, String parameter, Object collection, int pointer)
    {
        // check if we are at the bottom of the package
        int nextDot = parameter.substring(pointer).indexOf('.');
        // first character: a # or alpha
        char firstchar = parameter.charAt(pointer);
        if (isAlpha(firstchar))
        {
            // this must be a map entry, since this is a key
            Map map = (Map)collection;
            if (nextDot == -1)
            {
                // we've reached the end of this branch, set the key's value and return
                String key = parameter.substring(pointer);
                map.put(key,req.getParameter(parameter));
                return; 
            }
            else
            {
                // another layer to the package tree detected. check if the next layer object is already there...
                String key = parameter.substring(pointer,pointer+nextDot);
                Object nextcollection = map.get(key);
                if (nextcollection == null)
                {
                    // we need to create it: determine type
                    if (isAlpha(parameter.charAt(pointer+nextDot+1)))
                    {
                        nextcollection = new HashMap();
                    }
                    else if (isNum(parameter.charAt(pointer+nextDot+1)))
                    {
                        nextcollection = new Vector();
                    }
                    map.put(key,nextcollection);
                }
                // recurse
                packageParameter(req,parameter,nextcollection,pointer+nextDot+1);
            }
        }
        else if (isNum(firstchar))
        {
            // this must be a list entry, since this is a number
            Vector list = (Vector)collection;
            if (nextDot == -1)
            {
                String key = parameter.substring(pointer);
                int index = Integer.parseInt(key);
                if (list.size() <= index) list.setSize(index+1);
                list.set(index,req.getParameter(parameter));

                return;     
            }
            else
            {
                // another layer to the package tree detected. check if the next layer object is already there...
                String key = parameter.substring(pointer,pointer+nextDot);
                int index = Integer.parseInt(key);
                Object nextcollection = null;
                try {
                    nextcollection = list.get(index);                    
                } catch (ArrayIndexOutOfBoundsException oobe) {nextcollection = null;}
                if (nextcollection == null)
                {
                    // we need to create it: determine type
                    if (isAlpha(parameter.charAt(pointer+nextDot+1)))
                    {
                        nextcollection = new HashMap();
                    }
                    else if (isNum(parameter.charAt(pointer+nextDot+1)))
                    {
                        nextcollection = new ArrayList();
                    }
                    if (list.size() <= index) list.setSize(index+1);
                    list.set(index,nextcollection);
                }
                // recurse
                packageParameter(req,parameter,nextcollection,pointer+nextDot+1);
            }
        }
    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String evalprefix = (String)arguments.get("Prefix");
        String evalkey = (String)arguments.get("Key");
        

        Enumeration parameters = req.getParameterNames();
        // loop through parameters, adding to the tree if we match...
        // --> packageParameter() is a bad ass recursive function <--
        Object collection = null;
        while (parameters.hasMoreElements())
        {
            // initial filter - see if parameter matches current prefix
            String current = (String)parameters.nextElement();
            if (current.length() >= evalprefix.length())
            {
                if (current.substring(0,evalprefix.length()).equals(evalprefix))
                {
                    // we have a prefix match, check if the storage collection has been created
                    if (collection == null)
                    {
                        // determine initial package: map or list
                        char packtype = current.charAt(evalprefix.length()+1);
                        if (isAlpha(packtype))
                        {                   
                            // Map...
                            collection = new HashMap();
                        }
                        else if (isNum(packtype))
                        {
                            // List...
                            collection = new Vector();
                        }
                    }
                    packageParameter(req,current,collection,evalprefix.length()+1);
                }
            }
        }
                
        return collection;
    }
        
}

