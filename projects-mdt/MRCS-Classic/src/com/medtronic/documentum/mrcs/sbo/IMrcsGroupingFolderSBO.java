/*
 * Created on Feb 10, 2005
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

 Filename       $RCSfile: IMrcsGroupingFolderSBO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:00 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import java.util.Map;

import com.documentum.fc.client.IDfService;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfLoginInfo;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface IMrcsGroupingFolderSBO extends IDfService 
{
    public String createGroupingFolder(IDfLoginInfo logininfo, String parentfolderid, String mrcsapplication, String gftype, Map customdata, IDfSessionManager sMgr) throws Exception;
    public String createSubfolder(IDfLoginInfo logininfo, String parentfolderid, String mrcsapplication, String gftype, Map customdata, IDfSessionManager sMgr) throws Exception;
    public String createSubfolder(IDfLoginInfo logininfo, String parentfolderid, String mrcsapplication, String gftype, Map customdata, boolean execplugins, IDfSessionManager sMgr) throws Exception;
    public String createSystemSubfolder(String foldername, String foldertype, String folderACL, String parentfolderid, String mrcsapplication, String gftype, Map customdata, IDfSessionManager sMgr) throws Exception;    
}
