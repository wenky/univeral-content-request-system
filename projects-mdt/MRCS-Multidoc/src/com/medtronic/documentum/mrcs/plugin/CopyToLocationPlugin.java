/*
 * Created on Mar 15, 2005
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

 Filename       $RCSfile: CopyToLocationPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:53 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CopyToLocationPlugin implements ICopyToLocationPlugin {
    IDfSessionManager sMgr = null;
    /**
     *
     */
    public CopyToLocationPlugin() {
        super();
        // TODO Auto-generated constructor stub
    }



    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.ICopyToLocationPlugin#getCopyToFolder(com.medtronic.documentum.mrcs.sbo.dto.StateInfo)
     */
    public IDfFolder getCopyToFolder(IDfSession session, String appName, IDfDocument parentDoc, String copylocation) throws DfException {
//    public IDfFolder getCopyToFolder(String appName, IDfDocument parentDoc, String copylocation) throws DfException {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:CopyToLocationPlugin : getCopyToFolder : GI getFolderIdCount "+parentDoc.getFolderIdCount(), null, null);
        String strDestFolderPath = "";
        IDfFolder destFolder = null;
        //IDfSession session = getSession(appName);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:CopyToLocationPlugin.getCopyToFolder : IDfSession session : "+session, null, null);
        for(int i=0; i<parentDoc.getFolderIdCount(); i++){
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:CopyToLocationPlugin : getCopyToFolder : Parent Doc FolderId "+i+" "+parentDoc.getFolderId(i), null, null);
            IDfFolder parenstFolder = (IDfFolder)session.getObject(parentDoc.getFolderId(i));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:CopyToLocationPlugin : getCopyToFolder : Parent Folder Name : "+parenstFolder.getObjectName(), null, null);

            for(int j=(parenstFolder.getAncestorIdCount()-1); j>=0; j--){
            //for(int j=0; j<parenstFolder.getAncestorIdCount(); j++){
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:CopyToLocationPlugin : getCopyToFolder : Parent getAncestorId: "+parenstFolder.getAncestorId(j), null, null);
                IDfFolder newParentFolder = (IDfFolder)session.getObject(new DfId(parenstFolder.getAncestorId(j)));
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:CopyToLocationPlugin : getCopyToFolder : Parent Folder Name : "+newParentFolder.getObjectName(), null, null);
                strDestFolderPath = strDestFolderPath+"/"+newParentFolder.getObjectName();
           }
        }

        strDestFolderPath= strDestFolderPath+"/"+copylocation;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:CopyToLocationPlugin : getCopyToFolder : DESTINATION FOLDER PATH "+strDestFolderPath, null, null);
        // if(parentDoc.getFolderId(0).toString().equals(destFolder.getAncestorId(0)))

        destFolder = session.getFolderByPath(strDestFolderPath);
        if (destFolder == null) {
            destFolder = session.getFolderByPath(copylocation);
            if (destFolder == null) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:CopyToLocationPlugin : getCopyToFolder : Destination folder or cabinet " + copylocation + " does not exist in the Docbase!", null, null);
                return null;
               }
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:CopyToLocationPlugin : getCopyToFolder-return : "+destFolder, null, null);
        //To DO: need to release the session
       // releaseSession(session);
        return destFolder;
    }

}
