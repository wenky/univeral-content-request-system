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

 Filename       $RCSfile: StaticMrcsFolders.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:24 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.sbo.IMrcsGroupingFolderSBO;
import com.medtronic.documentum.mrcs.sbo.MrcsGroupingFolderSBO;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StaticMrcsFolders implements MrcsFolderCreationPlugin {

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.MrcsSubfolderTopologyPlugin#generateFolders(java.lang.String, java.lang.String, java.lang.String, java.util.Map, java.util.Map)
     * 
     * This plugin creates MRCS folders (the supplied folder type must descend from m_mrcs_folder so the proper attributes can be set)
     * The other StaticFolders plugin only creates generic documentum folders without the additional attribute configuration.
     * 
     */
    public void processFolder(IDfSession session, String mrcsapp, String gftype, String objectid, Map configdata, Map customdata) throws Exception 
    {
        // we can do this with instance vars since plugin classes are reinstantiated every time they are called.
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.generateFolders - top of StaticMrcsFolders folder topology plugin",null,null);
        m_app = mrcsapp; 
        m_type = gftype;
        m_gfroot = objectid; // the grouping folder we are creating the system subfolders in, our "root" directory

        m_acl = (String)configdata.get("ACL");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.generateFolders - subfolder acl is "+m_acl,null,null);
        m_SFtype = (String)configdata.get("SystemType");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticFolders.generateFolders - subfolder type is "+m_SFtype,null,null);

        // prep the SBo
        try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.generateFolders - getting SBO",null,null);
            m_gfservice = new MrcsGroupingFolderSBO();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:StaticMrcsFolders.generateFolders - error while creating grouping folder",e);
            throw(e);
        }

        // recursive M A D N E S S ! ! ! - build static subfolder structure recursively
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.generateFolders - initial call to recursive function process()",null,null);
        process(session, 1,"",objectid,configdata,customdata);
    }
    
    String m_app, m_type, m_gfroot, m_acl, m_SFtype;
    IMrcsGroupingFolderSBO m_gfservice;    
    
    void process(IDfSession session, int currentindex, String prefix, String currentsubfolderid,Map configdata,Map customdata) throws Exception
    {
        // check if there's a directory to be created...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - BEGIN - check for "+prefix+currentindex,null,null);
        if (configdata.containsKey(prefix+currentindex))
        {
            // there is, create it...
            String acl = m_acl;
            String type = m_SFtype;
            String mrcstype = m_type;
            if (configdata.containsKey(prefix+currentindex+".ACL"))
            {
                acl = (String)configdata.get(prefix+currentindex+".ACL");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - acl override: "+acl,null,null);
            }
            if (configdata.containsKey(prefix+currentindex+".TYPE"))
            {
                type = (String)configdata.get(prefix+currentindex+".TYPE");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - type override: "+type,null,null);
            }
            if (configdata.containsKey(prefix+currentindex+".MRCSTYPE"))
            {
                mrcstype = (String)configdata.get(prefix+currentindex+".MRCSTYPE");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - mrcs type override: "+mrcstype,null,null);
            }
            String foldername = (String)configdata.get(prefix+currentindex);
            // check for complex name generation (copies code in GroupingFolderSBO, with tweaks)
            if (configdata.containsKey(prefix+currentindex+".NAMING"))
            {
                String namegenerator = (String)configdata.get(prefix+currentindex+".NAMING");
                // execute naming plugins...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - setting folder name - instantiating name generator: "+namegenerator,null,null);
                MrcsNamingFormatPlugin namingplugin = (MrcsNamingFormatPlugin)Class.forName(namegenerator).newInstance();
                Map namingconfig  = (Map)configdata.get(prefix+currentindex+".NAMINGCONFIG");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - setting folder name - generating name... ",null,null);
                foldername = namingplugin.generateName(session,m_app,mrcstype,currentsubfolderid,namingconfig,customdata);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - setting folder name - generated grouping folder name: "+foldername,null,null);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - foldername: "+foldername,null,null);
            // check for overrides
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - calling SF creation method on SBO (plugin exec disabled)",null,null);
            String newid = m_gfservice.createSystemSubfolder(foldername,type,acl,currentsubfolderid,m_app,mrcstype,customdata,session.getSessionManager());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - id of new SF: "+newid,null,null);
                       
            // post-process with new root if necessary...
            //if (configdata.containsKey(prefix+currentindex+".NEWROOT"))
            //{
            //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - overriding the mrcs root",null,null);
            //    IDfFolder newfolder = (IDfFolder)session.getObject(new DfId(newid));
            //    newfolder.setString("mrcs_grouping_folder_root",newid);
            //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - mrcs type override: "+mrcstype,null,null);
            //}
            
            // see if there is another level down...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - recursing down one directory level: "+prefix+currentindex+"-1",null,null);
            process(session, 1,""+prefix+currentindex+"-",newid,configdata,customdata); // recurse...
            // see if there is another dir at this level...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - recursing to next directory at current level: "+prefix+(currentindex+1),null,null);
            process(session, currentindex+1,prefix,currentsubfolderid,configdata,customdata); // recurse...
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:StaticMrcsFolders.process - END (pop!)",null,null);        
    }

}
