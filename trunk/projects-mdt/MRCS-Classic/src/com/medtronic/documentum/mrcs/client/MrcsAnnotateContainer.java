/*
 * Created on Mar 11, 2005
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

 Filename       $RCSfile: MrcsAnnotateContainer.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/05 20:19:03 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Container;
import com.documentum.webcomponent.library.messages.MessageService;

/**
 * @author prabhu1
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class MrcsAnnotateContainer extends Container {

    /**
     *  
     */
    public MrcsAnnotateContainer() {
        super();
        System.out.println("MrcsAnnotateContainer");
    }


    private void setMessage(String s) {
        MessageService.addMessage(this, s, null);
    }


    public boolean onCommitChanges() {
        return super.onCommitChanges();
    }


    public void onInit(ArgumentList argumentlist) {
        System.out.println("MrcsAnnotateContainer:----onInit---------");
        super.onInit(argumentlist);
        //initialize(argumentlist);
        //setComponentJump("main", argumentlist, getContext() );
        //setComponentReturn();
    }


    public void onRender() {
        try {
            super.onRender();
            System.out.println(" ---------------MrcsAnnotateContainer : onRender-------------------- ");
            //setComponentReturn();
           } catch (Exception e) {
            System.out.println("MrcsAnnotateContainer:----onRender Exception---------" + e);
        }
    }
    


    private void initialize(ArgumentList argumentlist) {
        
        System.out.println("_________Annotate__________");
        IDfSysObject idfsysobject = null;
        HttpServletRequest httpservletrequest = (HttpServletRequest) getPageContext().getRequest();
        HttpServletResponse httpservletresponse = (HttpServletResponse) getPageContext().getResponse();
        System.out.println("_httpservletrequest :" + httpservletrequest);
        System.out.println("httpservletresponse :" + httpservletrequest);
        FileInputStream fileinputstream = null;
        
        try {
            String val[] = argumentlist.getValues("objectId");
            idfsysobject = (IDfSysObject) getDfSession().getObject(new DfId(val[0]));
            System.out.println("idfsysobject :" + idfsysobject);

            if (idfsysobject == null)
                throw new RuntimeException("Object not found: "+val[0]);
            String s1 = getDfSession().apiGet("id",
                    "dmr_content where any i_rendition !=0 and full_format='pdf' and any parent_id = '" + val[0] + "'");
            System.out.println("s1 :" + s1);
            if (s1 == null || s1.length() == 0)
                throw new IllegalArgumentException("No PDF Rendition available.\n");
            String s2 = "text/plain";
            IDfFormat idfformat = (IDfFormat) getDfSession().getObjectByQualification("dm_format where name = 'pdf'");
            System.out.println("idfformat :" + idfformat);
            if (idfformat != null) {
                String s3 = idfformat.getMIMEType();
                System.out.println("s3 :" + s3);
                if (s3.length() > 0)
                    s2 = s3;
                httpservletresponse.setContentType(s2);
                String s4 = idfsysobject.getFileEx(null, idfformat.getName(), 0, false);
                System.out.println("s4 : " + s4);
                File file = new File(s4);
                System.out.println("file : " + file);
                fileinputstream = new FileInputStream(file);
                javax.servlet.ServletOutputStream servletoutputstream = httpservletresponse.getOutputStream();
                System.out.println("servletoutputstream : " + servletoutputstream);
                byte abyte0[] = new byte[2048];
                for (int i = fileinputstream.read(abyte0); i >= 0; i = fileinputstream.read(abyte0))
                    servletoutputstream.write(abyte0, 0, i);
                servletoutputstream.flush();
            }

        } catch (DfException e) {
            System.out.println("MrcsAnnotateContainer:----onInit Exception---------" + e);
            setMessage(e.getMessage());
        } catch (Exception e) {
            System.out.println("MrcsAnnotateContainer:----Exception---------" + e);
            setMessage(e.getMessage());
        } finally {
            if (fileinputstream != null)
                try {
                    fileinputstream.close();
                } catch (IOException _ex) {
                    setMessage(_ex.getMessage());
                }
        }
    }

}