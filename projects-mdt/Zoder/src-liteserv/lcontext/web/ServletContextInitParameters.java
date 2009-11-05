package lcontext.web;

import java.util.Enumeration;
import java.util.Iterator;

import javax.servlet.ServletContext;

import lcontext.IContextItem;

public class ServletContextInitParameters implements IContextItem 
{
    ServletContext c = null;
    
    public ServletContextInitParameters(ServletContext srvctx) {
        c = srvctx;
    }

    public Object get(Object key) {
        return c.getInitParameter(key.toString());
    }

    public boolean isEnumerable() {
        
        return true;
    }
    
    public Enumeration enumerate() {
        return c.getInitParameterNames();
    }

    public boolean isIterable() {
        // TODO Auto-generated method stub
        return false;
    }

    public Iterator iterate() {
        return null;
    }

    public Object set(Object key, Object value) {
        return null;
    }

}
