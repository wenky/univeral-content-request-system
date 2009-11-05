package com.medtronic.documentum.mrcs.method;

import java.io.ByteArrayInputStream;
import java.io.OutputStream;
import java.io.StringWriter;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.exolab.castor.xml.XMLException;
import org.w3c.dom.Document;
import org.w3c.dom.DocumentType;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.mthdservlet.IDmMethod;

public class QADocFormCreateAttachmentsMethod extends QADocFormConfigurableMethod implements IDmMethod
{
    public void execute(Map parameters, OutputStream output) throws Exception
    {
       	/*-CONFIG-*/String m="QADocFormCreateAttachmentsMethod.execute -- ";
    	
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting docbase from JSM parameters",null,null);
    	String[] paramvals = (String[])parameters.get("docbase_name");
    	String docbase = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~docbase: "+docbase,null,null);
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting packageId from JSM parameters",null,null);
    	paramvals = (String[])parameters.get("packageId"); // OOTB docbasic promote method thinks this is workitemid...
    	String packageid = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~packageId: "+packageid,null,null);
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting 'mode' from JSM parameters",null,null);
    	paramvals = (String[])parameters.get("mode"); 
    	String mode = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~MODE: "+mode,null,null);
	    
	    IDfSessionManager usermgr = this.getUserSession(parameters);
	    IDfSession usersession = null;
	    
	    try {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~getting user session",null,null);
	    	usersession = usermgr.getSession(docbase);

	    	// get workitem
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retreive workitem (the 'packageid')",null,null);
		    IDfWorkitem workitem = (IDfWorkitem)usersession.getObject(new DfId(packageid));
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"...retrieved workitem "+packageid+"? "+(workitem !=null),null,null);
		    
		    // acquire if not mode 0. ?what? - it's what the docbasic thingy does
		    if ("0".equals(mode))
		    {
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mode is 0, acquiring workitem...",null,null);
		    	workitem.acquire();
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"...acquired",null,null);
		    }
	    	
			// 1. get the package (form)
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~getting first package - the form",null,null);
	    	IDfSysObject form = getFirstPackage(usermgr, parameters);
			// 2. get the xml content of the package -- FUN WITH STREAMS!
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~getting inputstreams",null,null);
	    	ByteArrayInputStream rawbytes = form.getContent();
	    	//BufferedReader reader = new BufferedReader(new InputStreamReader(rawbytes,"UTF-8"));
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~read from inputstream",null,null);
		    
			// 3. parse out the attachment list - use ootb java xml apis (DOM)
		    Document document = null;
		    try {
			    DocumentBuilder builder = null;
			    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			    builder = factory.newDocumentBuilder();
			    document = builder.parse(rawbytes);
		    } catch (Exception e) {		    	
			    /*-ERROR-*/DfLogger.error(this, m+"  ~~error in parsing of document body" , null, e);
			    throw new RuntimeException(e);
		    }
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~parsed xmldoc: "+toXMLString(document,false),null,null);
		    // use crude parsing to get id list
		    String xmldoc = toXMLString(document,false);
		    // clear out everything before <Attachments>
		    String attachmentstring = xmldoc.substring(xmldoc.indexOf("ttachments"));
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~front strip: "+attachmentstring,null,null);
		    // parse into ids (digits only)
		    while (true) {
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~begin next loop, attstr is: "+attachmentstring,null,null);
		    	int idx090 = attachmentstring.indexOf("090");
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~090 idx: "+idx090,null,null);
		    	if (idx090 > 0) {
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~subst curid",null,null);
			    	String curid = attachmentstring.substring(idx090,idx090+16);
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~add attachment curid: "+curid,null,null);
			    	workitem.addAttachment("m_mrcs_document",new DfId(curid));
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~trimming attachstring",null,null);
			    	attachmentstring = attachmentstring.substring(idx090+16);
		    	} else {
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~no more attachment ids, we're done",null,null);
		    		break;
		    	}
		    }
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"completing workitem...",null,null);	    
		    workitem.complete();
		    
	    } finally {
	    	if (usersession != null) usermgr.release(usersession);
	    }
    }
    
    public static String toXMLString(Document doc, boolean outputDocType)
    {
       StringWriter writer = new StringWriter();
       Transformer transformer = null;
       try
       {
         transformer = TransformerFactory.newInstance().newTransformer();
         if (outputDocType)
         {
           DocumentType type = doc.getDoctype();
           if (type != null)
           {
             String publicId = type.getPublicId();
             if (publicId != null)
             {
               transformer.setOutputProperty(OutputKeys.DOCTYPE_PUBLIC, publicId);
             }
             String systemId = type.getSystemId();
             if (systemId != null)
             {
               transformer.setOutputProperty(OutputKeys.DOCTYPE_SYSTEM, systemId);
             }
           }
         }
       }
       catch (TransformerConfigurationException e)
       {
    	   return e.toString();
       }
       try
       {
         transformer.transform(new DOMSource(doc), new StreamResult(writer));
       }
       catch (TransformerException e)
       {
    	   return e.toString();
       }
       return writer.toString();
    }
 
}
