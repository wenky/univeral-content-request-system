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

 Filename       $RCSfile: ICopyToLocationPlugin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:55 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface ICopyToLocationPlugin {

    /**
     * Gets the Copy to location.The default implementation supports one 
     * level of subfolder and absolute location of folder to copy
     * 
     * @param location
     * @return
     */
   // public String getCopyToFolder(String parentFolderPath, String copylocation);
    public IDfFolder getCopyToFolder(IDfSession session, String appName, IDfDocument parentDoc, String copylocation) throws DfException ;
    //public IDfFolder getCopyToFolder(String appName, IDfDocument parentDoc, String copylocation) throws DfException ;

}
