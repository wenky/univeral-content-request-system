/*
 * Created on Mar 14, 2005
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

 Filename       $RCSfile: MrcsBaseSBO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:00 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfLoginInfo;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsBaseSBO extends DfService 
{
    protected IDfSessionManager createSessionManager(String docbase, String user, String pass) throws Exception {
        //create Client object
        IDfClient client = new DfClient();  
        
        //create a Session Manager object
        IDfSessionManager sMgr = client.newSessionManager();
        
        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(user);
        loginInfoObj.setPassword(pass);
        loginInfoObj.setDomain(null);
        
        //bind the Session Manager to the login info
        sMgr.setIdentity(docbase, loginInfoObj);
        //could also set identity for more than one Docbases:
        // sMgr.setIdentity( strDocbase2, loginInfoObj );
        //use the following to use the same loginInfObj for all Docbases (DFC 5.2 or later)
        // sMgr.setIdentity( * , loginInfoObj );
        return sMgr;
    }

    protected IDfSessionManager createSessionManager(String docbase, IDfLoginInfo logininfo) throws Exception {
        //create Client object
        IDfClient client = new DfClient();
        
        //create a Session Manager object
        IDfSessionManager sMgr = client.newSessionManager();
        
        
        //bind the Session Manager to the login info
        sMgr.setIdentity(docbase, logininfo);
        //could also set identity for more than one Docbases:
        // sMgr.setIdentity( strDocbase2, loginInfoObj );
        //use the following to use the same loginInfObj for all Docbases (DFC 5.2 or later)
        // sMgr.setIdentity( * , loginInfoObj );
        return sMgr;
    }
    
    
    private String vendorString =  "Medtronic, Inc.";
    private static final String version = "4.0";
    
    /* (non-Javadoc)
     * @see com.documentum.fc.client.IDfService#getVendorString()
     */
    public String getVendorString() {
            return vendorString;
    }
    /* (non-Javadoc)
     * @see com.documentum.fc.client.IDfService#getVersion()
     */
    public String getVersion() {
        return version;
    }
    /* (non-Javadoc)
     * @see com.documentum.fc.client.IDfService#isCompatible(java.lang.String)
     */
    public boolean isCompatible(String arg0) {      
        int i = arg0.compareTo(getVersion() );
        if(i <= 0 )
            return true;
        else
            return false;
    }

}
