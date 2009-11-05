/*
 * Created on Feb 3, 2005
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

 Filename       $RCSfile: StaticFolders.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:59 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StaticFolders implements MrcsFolderCreationPlugin {

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsSubfolderTopologyPlugin#generateFolders(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     */
    public void processFolder(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception 
    {
        // we can do this with instance vars since plugin classes are reinstantiated every time they are called.
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.generateFolders - top of StaticFolders folder topology plugin",null,null);
        m_app = mrcsapp; 
        m_type = gftype;
        m_gfroot = objectid; // the grouping folder we are creating the system subfolders in, our "root" directory
        // recursive M A D N E S S ! ! ! - build static subfolder structure recursively
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.generateFolders - initial call to recursive function process()",null,null);
        process(session, 1,"",objectid,configdata);
    }
    
    String m_app, m_type, m_gfroot;
    
    void process(IDfSession session, int currentindex, String prefix, String currentsubfolderid,Map configdata) throws Exception
    {
        // check if there's a directory to be created...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - BEGIN - check for "+prefix+currentindex,null,null);
        if (configdata.containsKey(prefix+currentindex))
        {
            // there is, create it...
            String foldername = (String)configdata.get(prefix+currentindex);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - creating subfolder "+foldername,null,null);
            String acl = (String)configdata.get("ACL");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - subfolder acl is "+acl,null,null);
            String type = (String)configdata.get("SystemType");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - subfolder type is "+type,null,null);
            // check for overrides
            if (configdata.containsKey(prefix+currentindex+".ACL"))
            {
                acl = (String)configdata.get(prefix+currentindex+".ACL");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - acl override: "+acl,null,null);
            }
            if (configdata.containsKey(prefix+currentindex+".TYPE"))
            {
                type = (String)configdata.get(prefix+currentindex+".TYPE");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - type override: "+type,null,null);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - setting up session and creating new sysobject",null,null);
            IDfSysObject newFolderDctmObj = (IDfSysObject)session.newObject(type);
            String newId = newFolderDctmObj.getObjectId().getId();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - looking up acl via operator name",null,null);
            String systemdomain = session.getServerConfig().getString("operator_name");
            IDfACL newACL = session.getACL(systemdomain, acl);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - setting acl: "+acl,null,null);
            newFolderDctmObj.setACL(newACL);
            newFolderDctmObj.setObjectName(foldername);
            newFolderDctmObj.link(currentsubfolderid);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - saving the sysobject",null,null);
            newFolderDctmObj.save();            
            
            // see if there is another level down...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - recursing down one directory level: "+prefix+currentindex+"-1",null,null);
            process(session, 1,""+prefix+currentindex+"-",newId,configdata); // recurse...
            // see if there is another dir at this level...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - recursing to next directory at current level: "+prefix+(currentindex+1),null,null);
            process(session, currentindex+1,prefix,currentsubfolderid,configdata);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.process - END (pop!)",null,null);        
    }

}
