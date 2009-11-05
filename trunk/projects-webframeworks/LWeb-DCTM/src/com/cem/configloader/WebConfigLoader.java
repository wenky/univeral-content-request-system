package com.cem.configloader;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.servlet.ServletContext;


import com.cem.base.EEx;
import com.cem.lweb.loadresource.ResourceLoader;
import com.thoughtworks.xstream.XStream;

// chars to play with: !@#$%^&()_-+=;,.~{}[]

// first character of key is @ means system flag (explicit type, etc)

// key names can use: A-Z, a-z, 0-9, -, _, .  
// [] indicates the key is in a list (ArrayList) and the numeric value inside the [] is the index (starts at zero)
// {} indicates the key is in a map (HashMap) and the A-Z/a-z/0-9/-/_/. key is the storage key in the map

// Operators for the value translation depends on first character of trimmed value expression (after the '=' character):
//  A-Z, a-z, 0-9, -, _, .       literal value 
//  ~ or @file                   file contents (assumes text file)
//  & or @xstream                XStream decode of file contents
//  # or @props                  properties compile of file contents
//  @<pluginname>                execute plugin (in context under "Plugins")
// TODO: figure out what to do with %#$, which I feel COMPELLED to use
// TODO: make exception tolerance a little better, and logging...

public class WebConfigLoader 
{
    class Key
    {
        public String name = null;
        public boolean isList = false;
        public boolean isMap = false;
        public int index = -1;
        public String mapkey = null;
        
        public Key(String keyexpression)
        {
            compilekey(keyexpression);            
        }
        public void compilekey(String keyexpression)
        {
            if (keyexpression.indexOf('[') != -1) {
                isList = true;
                String indexstr = keyexpression.substring(keyexpression.indexOf('[')+1,keyexpression.indexOf(']')).trim();
                index = Integer.parseInt(indexstr);
                name = keyexpression.substring(0,keyexpression.indexOf('[')).trim();
            } else if (keyexpression.indexOf('{') != -1) { 
                isMap = true;
                mapkey = keyexpression.substring(keyexpression.indexOf('{')+1,keyexpression.indexOf('}')).trim();
                name = keyexpression.substring(0,keyexpression.indexOf('{')).trim();
            } else { 
                name = keyexpression.trim();
            }
        }
        
    }
    
    public Object loadConfig(ServletContext servletcontext, String path, Map context) 
    {
        Object returnvalue = null;
        // get Set of objects+folders for current base path
        Set paths = servletcontext.getResourcePaths(path);
        
        // TODO: check for explicit type to instantiate for the current path's info (default is a HashMap)
        String configtype = getExplicitType(paths,path);
        if (configtype != null) {
            try { 
                returnvalue = Class.forName(configtype).newInstance();
            } catch (Exception e) {
                throw EEx.create("FileSysConfigLoad-createConfigType","Could not instantiate %s",configtype,e);                
            }
        } else {
            returnvalue = new HashMap();
        }
        
        Map filemap = null;
        // if returnvalue is a map, set the basepath system key
        if (returnvalue instanceof Map) {
            ((Map)returnvalue).put("@Path", path);
            filemap = new HashMap();
            ((Map)returnvalue).put("@Files", filemap);
        }
        
        // iterate through path objects - do inline keys first
        Iterator i = paths.iterator();
        while (i.hasNext()) {
            String item = (String)i.next();
            item = item.substring(path.length());
            if (item.indexOf('=') != -1) {
                // key-value inline specifier
                String keyexpr = item.substring(0,item.indexOf('=')).trim();
                Key key = new Key(keyexpr);
                String valexpr = item.substring(item.indexOf('=')+1,item.length()).trim();
                Object itemvalue = execValueExpression(returnvalue,key,valexpr,servletcontext,path,context,item);
                setValue(returnvalue,key,itemvalue);
            } 
        }
        
        // iterate again, processing subfolders 
        i = paths.iterator();
        while (i.hasNext()) {
            String item = (String)i.next();
            item = item.substring(path.length());
            if (item.charAt(item.length()-1) == '/') {
                // subfolder identified, get destination key
                Key key = new Key(item.substring(0,item.length()-1));
                // RECURSE
                Object value = loadConfig(servletcontext,path+item,context);
                // apply value
                setValue(returnvalue,key,value);
            } else if (filemap != null && item.indexOf('=') == -1) {
                filemap.put(item,null);
            }
        }        
        
        return returnvalue;
    }
    
    public Object execValueExpression(Object returnvalue,Key key,String valueexpression, ServletContext servletcontext, String basepath, Map context, String item)
    {
        // determine expression - what's the first character?
        if (valueexpression == null) return null;
        if ("".equals(valueexpression)) return "";
        if (valueexpression.charAt(0) == '~') {
            // read in the contexts of the file (assumes it is a text file)
            String filecontents = ResourceLoader.loadWebResource(servletcontext,basepath+item);
            return filecontents;
        } else if (valueexpression.charAt(0) == '&') {
            // XStream decode
            String filecontents = ResourceLoader.loadWebResource(servletcontext,basepath+item);
            XStream xs = new XStream();
            Object returnval = xs.fromXML(filecontents);
            return returnval;
        } else if (valueexpression.charAt(0) == '#') {
            // properties file compile            
            String filecontents = ResourceLoader.loadWebResource(servletcontext,basepath+item);
            InputStream stream = new ByteArrayInputStream(filecontents.getBytes());
            Properties properties = new Properties();
            try { 
                properties.load(stream);                
            } catch (Exception e) {
                throw EEx.create("FileSysConfigLoad-execValEx","Could not load properties data for config item %s",basepath+item,e);
            }
            return properties;
        } else if (valueexpression.charAt(0) == '@') {            
            // TODO: plugins such as SQL, DQL, etc.
            return null;
        } else {
            // assume it is a literal
            return valueexpression;
        }
    }
            
    public void setValue(Object returnvalue, Key key, Object value)
    {
        if (returnvalue instanceof Map) {
            Map map = (Map)returnvalue;
            if (key.isList) {
                List list = (List)map.get(key.name);
                if (list == null) {
                    list = new ArrayList();
                    map.put(key.name, list);
                }
                if (list.size() <= key.index) {
                	int listsize = list.size();
                    for (int i=0; i <= key.index - listsize; i++) {
                        list.add(null);
                    }
                }
                list.set(key.index, value);
            } else if (key.isMap) {
                Map submap = (Map)map.get(key.name);
                if (submap == null) {
                    submap = new HashMap();
                    map.put(key.name, submap);
                }
                submap.put(key.mapkey,value);                
            } else {
                map.put(key.name, value);
            }
        } else {
            // try to explicitly find the field TODO ::: set method detection...
            Field[] flds = returnvalue.getClass().getDeclaredFields();
            for (int i=0; i < flds.length; i++) {
                Field f = flds[i];
                if (f.getName().equalsIgnoreCase(key.name)) {
                    try { 
                        if (key.isList) {
                            List list = (List)f.get(returnvalue);
                            if (list == null) {
                                list = new ArrayList();
                                f.set(returnvalue, list);
                            }
                            list.set(key.index, value);
                        } else if (key.isMap) {
                            Map submap = (Map)f.get(returnvalue);
                            if (submap == null) {
                                submap = new HashMap();
                                f.set(returnvalue, submap);
                            }
                            submap.put(key.mapkey,value);                
                        } else {
                            f.set(returnvalue, value);
                        }
                    } catch (Exception e) {
                        throw EEx.create("FileSysConfigLoad-setValue-Reflect","Could not set via reflection field %s for key %s",f.getName(),key.name,e);                        
                    }
                }
            }
        }
    }
    
    public String getExplicitType(Set pathobjects, String basepath)
    {
        Iterator i = pathobjects.iterator();
        while (i.hasNext()) {
            String item = (String)i.next();
            item = item.substring(basepath.length()+1);            
            if (item.indexOf("@type") == 0) {
                // leading @type key found!
                String type = item.substring(item.indexOf('=')+1).trim();
                return type;
            }
        }
        return null;
    }

}
