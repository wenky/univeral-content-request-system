package org.tarantula.component;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.RequestDispatcher;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpServletResponseWrapper;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

// executes specified JSP, returning JSP output in context...

public class ExecJSP implements PageComponentInterface 
{
    public String getNickname() { return "ExecJSP"; }
    
    public void init(){}

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String dispatchtarget = (String)arguments.get("JSP");
        
        // invoke the flow via Servlet API's RequestDispatcher
        RequestDispatcher dispatch = req.getRequestDispatcher(dispatchtarget);
        OutputInterceptor outint = new OutputInterceptor(resp);
        dispatch.include(req,outint);
        String out = outint.getStringWriter().toString();
        
        return out;
        
    }
    
    class OutputInterceptor extends HttpServletResponseWrapper
    {
        StringWriter swriter = null;
        PrintWriter writer = null;
        public OutputInterceptor(HttpServletResponse resp) 
        {             
            super(resp);
            swriter = new StringWriter();
            writer = new PrintWriter(swriter);
        }
        
        public PrintWriter getWriter() { return writer; }
        
        public StringWriter getStringWriter() { return swriter; }
    }

}
