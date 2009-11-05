package com.medtronic.ecm.documentum.mdtworkflow.method.common;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class VelocityExecute 
{
    public static VelocityEngine ve = null;
    static { 
        try { 
            /*-trc-*/Lg.trc("VelocityExecute classload - instantiate shared velocity engine");
            ve = new VelocityEngine(); 
            /*-trc-*/Lg.trc("VelocityExecute classload - init");
            ve.init(); 
            /*-trc-*/Lg.trc("VelocityExecute classload - done");
        } catch (Exception e) { 
            /*-ERROR-*/Lg.err("VelocityExecute was unable to instantiate VelocityEngine on class load",e);
            throw EEx.create("VelocityInitialization", "VelocityExecute was unable to instantiate VelocityEngine on class load",e); 
        } 
    }     
    
    public static String generateHTML(VelocityContext vctx, String template)
    {
        /*-dbg-*/Lg.wrn("perform substitution");
        String result = null;
        StringWriter buff = new StringWriter();
        try { 
            ve.evaluate(vctx, buff, null, new StringReader(template));
            result = buff.toString();
        } catch (ParseErrorException pee) {
            /*-WARN-*/Lg.wrn("Velocity Processing syntax exception [%s] - [%d:%d]",pee.getInvalidSyntax(),pee.getLineNumber(),pee.getColumnNumber(),pee);
            result = "Parse Exception in template <BR>\n"+
                     "Invalid syntax: "+pee.getInvalidSyntax()+"<BR>\n"+
                     "on Line "+pee.getLineNumber()+" Column "+pee.getColumnNumber()+"<BR>\n"+
                     "Exception message: "+pee.getMessage();
        } catch (MethodInvocationException mie) {
            /*-WARN-*/Lg.wrn("Velocity Processing method invocation error [%s] - [%d:%d]",mie.getMethodName(),mie.getLineNumber(),mie.getColumnNumber(),mie);
            result = "Method Invocation Exception in template<BR>\n"+
                     "Method Name: "+mie.getMethodName()+"<BR>\n"+
                     "on Line "+mie.getLineNumber()+" Column "+mie.getColumnNumber()+"<BR>\n"+
                     "Exception message: "+mie.getMessage();
        } catch (ResourceNotFoundException rnfe) {
            /*-WARN-*/Lg.wrn("Velocity Processing resource not found error",rnfe);
            result = "Resource Not Found Exception in template<BR>\n"+"Exception message: "+rnfe.getMessage();                
        } catch (IOException ioe) {
            /*-WARN-*/Lg.wrn("Velocity Processing IO Exception",ioe);
            result = "IO Exception in template<BR>\n"+"Exception message: "+ioe.getMessage();                            
        }
        return result;
    }
    
    // pass in args as "key1",value1,"key2",value2, a lot like the CollUtils function
    public static VelocityContext createContext(Object... args)
    {
        VelocityContext veloctx = new VelocityContext();
        for (int i=0; i < args.length/2; i++) {
            String key = (String)args[i*2];
            Object value = args[i*2+1];
            veloctx.put(key, value);
        }
        return veloctx;        
    }

}
