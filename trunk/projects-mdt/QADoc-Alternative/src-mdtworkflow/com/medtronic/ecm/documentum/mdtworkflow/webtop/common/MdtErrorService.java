package com.medtronic.ecm.documentum.mdtworkflow.webtop.common;

import java.io.IOException;
import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import org.apache.velocity.VelocityContext;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.MethodInvocationException;
import org.apache.velocity.exception.ParseErrorException;
import org.apache.velocity.exception.ResourceNotFoundException;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.tools.common.LocaleService;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

// mostly intended for validation message formatting, but could be used for mail messaging as well  

public class MdtErrorService 
{

    public static VelocityEngine ve = null;
    static { 
        try { 
            /*-trc-*/Lg.trc("MdtMessaging classload - instantiate shared velocity engine");
            ve = new VelocityEngine(); 
            /*-trc-*/Lg.trc("MdtMessaging classload - init");
            ve.init(); 
            /*-trc-*/Lg.trc("MdtMessaging classload - done");
        } catch (Exception e) { 
            /*-ERROR-*/Lg.err("MdtMessaging was unable to instantiate VelocityEngine on class load",e);
            throw EEx.create("VelocityInitialization", "MdtMessaging was unable to instantiate VelocityEngine on class load",e); 
        } 
    }
    
    public static String renderErrorMessage (String errormessagekey, Map pluginconfig, IDfSessionManager smgr, String docbase, IDfSysObject form, IDfSysObject attachment, Object... args)
    {
        VelocityContext vctx = new VelocityContext();
        IDfSession session = null;
        String result = null;
        try {
            vctx.put("session", session);
            if (form !=null)
                vctx.put("form", form);
            if (attachment !=null)
                vctx.put("attachment", attachment);
            if (attachment !=null)
                vctx.put("config", pluginconfig);
            for (int i=0; i < args.length; i++)
            {
                if (args[i] != null && args[i] instanceof ErrKey) {
                    ErrKey ek = (ErrKey)args[i];
                    vctx.put(ek.key,ek.val);
                }
            }
            
            String template = (String)pluginconfig.get(errormessagekey);
               
            /*-dbg-*/Lg.wrn("perform substitution");
            StringWriter buff = new StringWriter();
            try { 
                ve.evaluate(vctx, buff, null, new StringReader(template));
                result = buff.toString();
            } catch (ParseErrorException pee) {
                /*-WARN-*/Lg.wrn("Velocity Processing syntax exception [%s] - [%s] [%d:%d]",template,pee.getInvalidSyntax(),pee.getLineNumber(),pee.getColumnNumber(),pee);
                result = "Parse Exception in template "+template+"<BR>\n"+
                         "Invalid syntax: "+pee.getInvalidSyntax()+"<BR>\n"+
                         "on Line "+pee.getLineNumber()+" Column "+pee.getColumnNumber()+"<BR>\n"+
                         "Exception message: "+pee.getMessage();
            } catch (MethodInvocationException mie) {
                /*-WARN-*/Lg.wrn("Velocity Processing method invocation error [%s] - [%s] [%d:%d]",template,mie.getMethodName(),mie.getLineNumber(),mie.getColumnNumber(),mie);
                result = "Method Invocation Exception in template "+template+"<BR>\n"+
                         "Method Name: "+mie.getMethodName()+"<BR>\n"+
                         "on Line "+mie.getLineNumber()+" Column "+mie.getColumnNumber()+"<BR>\n"+
                         "Exception message: "+mie.getMessage();
            } catch (ResourceNotFoundException rnfe) {
                /*-WARN-*/Lg.wrn("Velocity Processing resource not found error [%s]",template,rnfe);
                result = "Resource Not Found Exception in template "+template+"<BR>\n"+"Exception message: "+rnfe.getMessage();                
            } catch (IOException ioe) {
                /*-ERROR-*/Lg.err("IO Exception in velocity template processing [%s]",template,ioe);
                throw EEx.create("Velocity-IOE","IO Exception in velocity template processing [%s]",template,ioe);
            }
        } finally {
            try { smgr.release(session); } catch (Exception e) {}
        }
        return result;
        
    }

    public static String getAttributeLabel(IDfSysObject doc, String attrname) throws DfException
    {
        String labelquery = "SELECT DISTINCT label_text,attr_name,LOWER(label_text) FROM dmi_dd_attr_info WHERE type_name = '"+doc.getTypeName()+"' AND nls_key = '"+LocaleService.getLocale().getLanguage()+"' AND attr_name = '"+attrname+"' ORDER BY 3";
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL(labelquery);
        IDfCollection idfcollection = null;
        try { 
            idfcollection = dfquery.execute(doc.getSession(), 0);
            if (idfcollection.next())             
            {
                String label = idfcollection.getString("label_text");
                return label;           
            }
        } finally {
            try { idfcollection.close(); } catch (Exception e) {}
        }
        
        return attrname;
    }

}
