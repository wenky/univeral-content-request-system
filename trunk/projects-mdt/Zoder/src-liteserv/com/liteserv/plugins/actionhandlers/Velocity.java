package com.liteserv.plugins.actionhandlers;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import javax.servlet.http.HttpServletRequest;

import lbase.EEx;
import lbase.Lg;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.liteserv.config.LSPlugin;
import com.liteserv.config.namedprocessors.VelocityTemplate;
import com.liteserv.core.LSActionContext;
import com.liteserv.core.utils.LSResourceLoader;
import com.liteserv.plugindefs.ILSProgit;

public class Velocity implements ILSProgit
{    
    public static VelocityEngine ve = null;
    static { 
        try { 
            /*-trc-*/Lg.trc("classload - instantiate shared velocity engine");
            ve = new VelocityEngine(); 
            /*-trc-*/Lg.trc("classload - init");
            ve.init(); 
            /*-trc-*/Lg.trc("classload - done");
        } catch (Exception e) { 
            /*-ERROR-*/Lg.err("Unable to instantiate VelocityEngine on class load",e);
            throw EEx.create("VelocityInitialization", "Unable to instantiate VelocityEngine on class load",e); 
        } 
    } 
    
    public String execute(final LSPlugin progitdef, HttpServletRequest req, LSActionContext context, String response) 
    {
        VelocityTemplate templatedef = (VelocityTemplate)progitdef;        
        String template = LSResourceLoader.loadResource(templatedef.TemplateLocalUrl,templatedef.TemplateUrl, templatedef.TemplateFile, templatedef.TemplateResource);
        
        // setup context: request, incoming generated page fragment, config, context
        VelocityContext velctx = new VelocityContext();
        velctx.put("HttpRequest", req);
        velctx.put("HttpParams", req.getParameterMap());
        velctx.put("Context", context);
        velctx.put("PageResponse", response);
        
        String result = null;
        StringWriter buff = new StringWriter();
        try { 
            ve.evaluate(velctx, buff, null, new StringReader(template));
            result = buff.toString();
        } catch (ParseErrorException pee) {
            /*-ERROR-*/Lg.err("Velocity Processing syntax exception - [%s] [%d:%d]",pee.getInvalidSyntax(),pee.getLineNumber(),pee.getColumnNumber(),pee);
            throw EEx.create("Velocity-ParseError", "Velocity Template could not be parsed", pee);
        } catch (MethodInvocationException mie) {
            /*-ERROR-*/Lg.err("Velocity Processing method invocation error - [%s] [%d:%d]",mie.getMethodName(),mie.getLineNumber(),mie.getColumnNumber(),mie);
            throw EEx.create("Velocity-InvocationError", "Method Invocation error during Velocity template processing", mie);
        } catch (ResourceNotFoundException rnfe) {
            /*-ERROR-*/Lg.err("Velocity Processing resource not found error",rnfe);
            throw EEx.create("Velocity-ResourceNotFound", "Velocity template could not be found", rnfe);                            
        } catch (IOException ioe) {
            /*-ERROR-*/Lg.err("I/O error in Velocity template processing",ioe);
            throw EEx.create("Velocity-IOError", "I/O error in Velocity template processing", ioe);                            
        }
        
        return result;
    }

}
