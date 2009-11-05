/*
 * Created on Mar 10, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: PdfAnnotate.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/09/12 17:40:02 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Enumeration;
import java.util.Map;
import java.util.StringTokenizer;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class PdfAnnotate extends HttpServlet {

    /**
     * 
     */
    public PdfAnnotate() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * Initialize the counter to 0.
     */
    public void init(ServletConfig config) throws ServletException {
      super.init(config);
      count = 0;
    }

    /* (non-Javadoc)
     * @see com.documentum.web.formext.action.IActionExecution#execute(java.lang.String, com.documentum.web.formext.config.IConfigElement, com.documentum.web.common.ArgumentList, com.documentum.web.formext.config.Context, com.documentum.web.formext.component.Component, java.util.Map)
     */
    public boolean execute(String arg0, IConfigElement arg1, ArgumentList arg2, Context arg3, Component arg4, Map arg5) {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:PdfAnnotate.execute - execute called, returning TRUE, not doing anything else..",null,null);        
        return true;
    }


    /* (non-Javadoc)
     * @see com.documentum.web.formext.action.IActionPrecondition#getRequiredParams()
     */
    public String[] getRequiredParams() {
        return (new String[] {
                "objectId"
            });
    }


    /* (non-Javadoc)
     * @see com.documentum.web.formext.action.IActionPrecondition#queryExecute(java.lang.String, com.documentum.web.formext.config.IConfigElement, com.documentum.web.common.ArgumentList, com.documentum.web.formext.config.Context, com.documentum.web.formext.component.Component)
     */
    public boolean queryExecute(String arg0, IConfigElement arg1, ArgumentList arg2, Context arg3, Component arg4) {
        return true;
    }


    // a simplified version that (hopefully) works on load-balanced environments
    public void service(HttpServletRequest httpservletrequest, HttpServletResponse httpservletresponse)
        throws ServletException, IOException, IllegalArgumentException
    {
        /*-CONFIG-*/String m="service-";
        Enumeration e = httpservletrequest.getHeaderNames();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"headers----------------------------------",null,null);            
        while (e.hasMoreElements())
        {
            String header = (String)e.nextElement();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--header["+header+"] = ["+httpservletrequest.getHeader(header)+"]",null,null);            
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"headers end-------------------------------",null,null);            
    
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top",null,null);
        String path = httpservletrequest.getContextPath();
        String basePath = httpservletrequest.getScheme()+"://"+httpservletrequest.getServerName()+":"+httpservletrequest.getServerPort()+path+"/";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"service: path "+path,null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"service: basePath "+basePath,null,null);        
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting HTTP headers",null,null);        
        httpservletresponse.setHeader("Pragma","no-cache"); //HTTP 1.0
        httpservletresponse.setHeader("Expires", "0");
        //httpservletresponse.setHeader("Cache-Control","no-cache, no-store, must-revalidate, post-check=0, pre-check=0");
        httpservletresponse.setHeader("Pragma", "public");
        httpservletresponse.setDateHeader ("Expires",0); //prevents caching at the proxy server

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting objectid of document from the httprequest parameters",null,null);        
        String objectid = httpservletrequest.getParameter("objectId");
        String mrcsapp = httpservletrequest.getParameter("mrcsapp");
        String acroformat = httpservletrequest.getParameter("acro");
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"connecting to DCTM",null,null);        
        FileInputStream fileinputstream = null;
        IDfSessionManager sMgr = null;
        IDfSession session = null;
        try
        {

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up MRCS config",null,null);        
            StateTransitionConfigFactory states = StateTransitionConfigFactory.getSTConfig();
            
            // get access to DCTM
            // connect to documentum...
            //create Client objects
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get DCTM session: instantiating ClientX",null,null);        
            IDfClientX clientx = new DfClientX();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get DCTM session: getting local client",null,null);        
            IDfClient client = clientx.getLocalClient();

            //create a Session Manager object
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get DCTM session: creating session manager",null,null);        
            sMgr = client.newSessionManager();

            //create an IDfLoginInfo object named loginInfoObj
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get DCTM session: populating logininfo",null,null);        
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get DCTM session: user is "+states.getSystemUsername(mrcsapp),null,null);        
            loginInfoObj.setUser(states.getSystemUsername(mrcsapp));
            loginInfoObj.setPassword(states.getSystemPassword(mrcsapp));
            loginInfoObj.setDomain(null);
            sMgr.setIdentity(states.getApplicationDocbase(mrcsapp), loginInfoObj);

            // get a session...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get DCTM session: getting a session",null,null);        
            session = sMgr.getSession(states.getApplicationDocbase(mrcsapp));
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"-- objectid: "+objectid,null,null);        
            if(objectid == null || objectid.length() == 0)
                throw new IllegalArgumentException("ObjectID not in parameter list");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting document object from docbase",null,null);        
            IDfSysObject idfsysobject = (IDfSysObject)session.getObject(new DfId(objectid));
            if(idfsysobject == null)
                throw new RuntimeException("Object does not exist: "+objectid);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up pdf",null,null);        
            //String rendition = session.apiGet("id", "dmr_content where any i_rendition !=0 and full_format='pdf' and any parent_id = '" + objectid + "'");
            String rendition  = "";
            boolean isAcroFile = false;
            if ("true".equalsIgnoreCase(acroformat)) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up acro document",null,null);        
                isAcroFile = true;
                rendition = session.apiGet("id", "dmr_content where full_format='acro' and any parent_id = '" + objectid + "'");
            } else
                rendition = session.apiGet("id", "dmr_content where full_format='pdf' and any parent_id = '" + objectid + "'");
            boolean isPdfFile = false;
            if(rendition == null || rendition.length() == 0)
            {
                // no rendition, see if the base document is of type PDF...
                IDfFormat fmt = idfsysobject.getFormat();
                if ("application/pdf".equals(fmt.getMIMEType()))
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"base document is a pdf, so we can proceed",null,null);        
                    isPdfFile = true;
    
                }
                else
                {
                    /*-ERROR-*/DfLogger.getRootLogger().error(m+"no annotatable pdf doc found...");        
                    throw new IllegalArgumentException("No PDF Rendition available.\n");
                }
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"rendition: "+rendition,null,null);        
            
            //String s2 = "text/plain";
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"defaulting mime type to application/pdf",null,null);        
            String mimetype = "application/pdf";            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up dm_format for pdf...",null,null);        
            IDfFormat idfformat = (IDfFormat)session.getObjectByQualification("dm_format where name = 'pdf'");
            if(idfformat != null)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format found, getting mime type",null,null);        
                String formatmime = idfformat.getMIMEType();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format mime: "+formatmime,null,null);        
                if(formatmime.length() > 0)
                    mimetype = formatmime;
            }
    
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting HTTP response MIME to: "+mimetype,null,null);        
            httpservletresponse.setContentType(mimetype);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attempting to get pdf file",null,null);
            String s4 = null;
            if (isPdfFile || isAcroFile)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling getFile for "+idfsysobject.getObjectName(),null,null);
                s4 = idfsysobject.getFile(idfsysobject.getObjectName());                
            }
            else
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling getFileEx",null,null);
                s4 = idfsysobject.getFileEx(null, idfformat.getName(), 0, false);                
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"file ex: "+s4,null,null);        
            StringTokenizer sbfr = new StringTokenizer(s4,File.separator);
            String fileName = "";
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"tokenizing filename",null,null);        
            while(sbfr.hasMoreTokens()){
                fileName=sbfr.nextToken();
                // if mime-type is pdf, make sure filename has .pdf extension
                if ("application/pdf".equals(mimetype))
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"making sure pdf filename has .pdf extension, which cranky Acrobat plugins often need...",null,null);        
                    // make sure there's room to look for the .pdf extension
                    if (fileName.length() > 4)
                    {
                        String extension = fileName.substring(fileName.length()-4);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"extracted extnsion: "+extension,null,null);        
                        if (!".pdf".equals(extension))
                        {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"appending .pdf extension",null,null);        
                            fileName += ".pdf";
                        }
                    }
                }
            }
            fileName = "inline; filename="+fileName;
            //fileName = "attachment; filename="+fileName;
                        
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"parsed filename: "+fileName,null,null);        
            
            File file = new File(s4);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting http response content disposition to file: "+fileName,null,null);        
            httpservletresponse.setHeader("Content-disposition", fileName);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"file size: "+file.length(),null,null);        
            httpservletresponse.setContentLength((int)file.length());
    //        httpservletresponse.setHeader("Content-length",""+file.length());
    //        httpservletresponse.setHeader("Accept-ranges","bytes");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"writing pdf rendition file contents to HttpResponse",null,null);        
            fileinputstream = new FileInputStream(file);
            ServletOutputStream servletoutputstream = httpservletresponse.getOutputStream();
            
            
            byte[] abyte0 = new byte[65536];
            long tracker = 0;
            for(int i = fileinputstream.read(abyte0); i >= 0; i = fileinputstream.read(abyte0))
            {
                tracker += i;
                servletoutputstream.write(abyte0, 0, i);
            }
            servletoutputstream.flush();
                
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"writing count...what is this for? URL distinction?",null,null);
            // this bombs because of the getOutputStream() call above - apparently you can only do this once...
    //        PrintWriter out = httpservletresponse.getWriter();
    //        out.println(getCurrentCount());
    
            
        }
        catch(Exception exception1)
        {
            //throw new ServletException(exception1);
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"exception thrown",exception1);        
            PrintWriter out = httpservletresponse.getWriter();
            out.println("<html><body><br><br>");
            out.println("Error Occurred: Cannot Annotate<br>");
            out.println("<I>" +exception1.getMessage()+"</I>");   
            out.println("<br></body></html>");
            
        }
        finally
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"finally clause, releasing session",null,null);        
            sMgr.release(session);
            if(fileinputstream != null)
                try
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"closing rendition file input stream",null,null);        
                    fileinputstream.close();
                    //servletoutputstream.close();
                    //dwappsession.remove(this);
                }
                catch(IOException _ex) { 
                    /*-ERROR-*/DfLogger.getRootLogger().error(m+"exception thrown during rendition file stream closing",_ex);        
                }
        }
    }
    
    /**
     * Increments the count value and returns the result.
     */
    private synchronized int getCurrentCount() {
      count++;
      return count;
    }
    
    private int count;

}
