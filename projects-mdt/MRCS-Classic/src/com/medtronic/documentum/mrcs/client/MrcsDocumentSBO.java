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

 Filename       $RCSfile: MrcsDocumentSBO.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/08/21 04:57:04 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.webcomponent.library.create.CreateService;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.MrcsDocumentCreationPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsDocumentNamingFormatPlugin;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsDocumentSBO //extends MrcsBaseSBO //implements IMrcsDocumentSBO
{
    protected static MrcsDocumentSBO service;
    static { service = new MrcsDocumentSBO(); } // static initializer

    public static MrcsDocumentSBO getService()
    {
        return service;
    }

    public String createDocument(IDfSession session, String parentfolderid, String mrcsapp, String gftype, String doctype, String format, String template, Map customdata) throws Exception
    {
        boolean newtransaction = false;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - top of method call",null,null);
        // get docconfig (may want to wrap this in try..catch)
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - getting docconfig",null,null);
        MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();

//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - get docbase session",null,null);
//        IDfSession session = createSessionManager(docbase, docconfig.getSystemUsername(mrcsapp), docconfig.getSystemPassword(mrcsapp)).getSession(docbase);

        // CEM - transactionalize new document... start transaction!
        IDfSessionManager sessmgr = session.getSessionManager();

        // main transaction rollback try...catch block
        String newdocid = null;
        IDfSysObject newdoc = null;
        try {
            // invoke naming plugin
            String docname = generateDocumentName(docconfig,session,mrcsapp,gftype,doctype,parentfolderid,customdata);

            // perform document sysobject creation...
            // - don't know if this will be contained in our transaction in rollback due to the fact we are using the DwNewDocCommand...
            // look up the documentum type
            String dctmtype = docconfig.getDocumentSystemType(mrcsapp,gftype,doctype);
            boolean bCreateSuccess = false;

            // -->>-- AS OF Webtop 5.3, DwNewDocCOmmand is no longer in the API, there is a new way of doing this --<<--
            // get name, type, format, template
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - preparing to invoke doc create wdk COMMAND",null,null);
            //DwCommandParameters dwcmdargs = new DwCommandParameters();
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - folderid: "+parentfolderid,null,null);
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - docname: "+docname,null,null);
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - doctype: "+dctmtype,null,null);
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - format:  "+format,null,null);
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - template: "+template,null,null);
            //dwcmdargs.add("folderId",               parentfolderid);
            //dwcmdargs.add("attribute_object_name",  docname);
            //dwcmdargs.add("type",                   dctmtype);
            //dwcmdargs.add("format",                 format);
            //dwcmdargs.add("templateId",             template);
            //DwNewDocCommand newdoccmd = new DwNewDocCommand();
            //newdoccmd.setCommandParameters(dwcmdargs);
            //DwDocbaseSession dwdocbasesession = null;

            //  -->>-- the new way uses the CreateService object in com.documentum.webcomponent.library.create
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - instantiating new (for Webtop5.3) CreateService object",null,null);
            CreateService createSvc = new CreateService();


            newdocid = null;
            // create in DCTM
            try
            {
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - getting dwdocbase session",null,null);
                //dwdocbasesession = DwDocbaseSession.attachToSession(session);
                //newdoccmd.setDocbaseSession(dwdocbasesession);
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - locking...",null,null);
                //dwdocbasesession.lock();
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - executing create doc command",null,null);
                //DwExecutionResult dwexecutionresult = newdoccmd.execute();
                //newdocid = dwexecutionresult.getRetValue("objectId");
                ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - doc created: "+newdocid,null,null);

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - creating doc with createservice",null,null);
                newdocid = createSvc.createDocument(parentfolderid,template,dctmtype,format,docname,false);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - doc created: "+newdocid,null,null);

                bCreateSuccess = true;
            }
            catch(Exception e)
            {
                //log it
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentSBO.createDocument - error occurred executing create doc WDK command",e);
                // throw...
                throw (e);
            }
            // -->>-- this finally is no longer needed since we are not using dwdocbasesession anymore (removed from API as of Webtop 5.3) --<<--
            //finally
            //{
            //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - unlocking the session",null,null);
            //    if(dwdocbasesession != null)
            //        dwdocbasesession.unlock();
            //}
            
            

            // postprocessing
            // CEM: WT53 is bombing here, presumably due to ?transaction deadlock?, yikes...
            newdoc = (IDfSysObject)session.getObject(new DfId(newdocid));

            // CEM: WT53, trying to fix ?transaction deadlock?
            if (sessmgr.isTransactionActive())
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - transaction already active on current session manager",null,null);
                // do nothing
            }
            else
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - staring transaction for new document",null,null);
                sessmgr.beginTransaction();
                newtransaction = true;
            }

            // set attributes
            setMrcsDocumentAttributes(newdoc,docconfig,session,mrcsapp,gftype,doctype,newdocid);

            // apply the document acl
            applyDocumentACL(newdoc,docconfig,session,mrcsapp,gftype,doctype,newdocid);

            // execute post-creation plugins
            executePlugins(newdoc,docconfig,session,mrcsapp,gftype,doctype,newdocid,customdata);

            // lifecycle attachment
            attachLifecycle(newdoc,docconfig,session,mrcsapp,gftype,doctype,newdocid);


            // checkout the doc
            //try {
    // disable checkout lock since we are having problems with post-doc-create checkout action
    //            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - checking out new document",null,null);
    //            IDfSysObject idfsysobject = (IDfSysObject)session.getObject(new DfId(newdocid));
    //            idfsysobject.checkout(); // this just locks it while the action copies it
    //            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - checkout completed",null,null);
            //}
            //catch(Exception e)
            //{
            //    //log it
            //    /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentSBO.createDocument - error occurred executing document checkout",e);
            //    // throw...
            //    throw (e);
            //}
        } catch (Exception e) {
            // check if we need to rollback
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentSBO.createDocument - error occurred during document creation transaction",e);
            if (newtransaction)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - rolling back transaction for this new document because of caught exception",null,e);
                sessmgr.abortTransaction();
            }
            // destroy the new doc - it is hell's spawn!
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - destroying the new document - it is all badness!",null,e);
            newdoc.destroy();
            throw e;
        }

        if (newtransaction)
        {
            // commit the transaction!
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - committing transaction for this new document",null,null);
            sessmgr.commitTransaction();
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - done with document creation",null,null);
        return newdocid;
    }

    private void applyDocumentACL(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, String newdocid) throws Exception
    {
        // apply the acl...
        // look up the associated ACL
        String ACL = docconfig.getDocumentACL(mrcsapp,gftype,doctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyDocumentACL - locating ACL: "+ACL,null,null);
        String systemdomain = session.getServerConfig().getString("operator_name");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyDocumentACL - system domain: "+systemdomain,null,null);
        IDfACL newACL = session.getACL(systemdomain, ACL);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyDocumentACL - ACL found?: "+(newACL != null),null,null);
        if (newACL == null)
        {
            Exception e = new Exception("MRCS could not locate ACL for your user. ACL: "+ACL+" user: "+session.getLoginUserName());
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentSBO.applyDocumentACL - unable to look up acl: "+ACL,e);
            throw e;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyDocumentACL - applying ACL: "+ACL,null,null);
        newdoc.setACL(newACL);
        newdoc.save();
    }

    private void attachLifecycle(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, String newdocid) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - getting lifecycle",null,null);
        String lifecyclename = docconfig.getDocumentLifecycle(mrcsapp,gftype,doctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - looking up the policy object: "+lifecyclename,null,null);
        IDfSysObject lifecycle = (IDfSysObject)session.getObjectByQualification("dm_sysobject where object_name ='" + lifecyclename + "'");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - attaching policy",null,null);
        newdoc.attachPolicy(lifecycle.getObjectId(), "", "");
        // MRCS 4.1.2 legacy compatibility
        StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();
        if (sts.isLegacyLCWF(mrcsapp)) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - Legacy MRCS4.1.2 - mark label",null,null);
            newdoc.mark(sts.getStateInfo(mrcsapp,newdoc.getCurrentStateName()).getLabel());        	
        }
        newdoc.save();
        // since attachment of policy doesn't seem to set the initial label, and the APIs for IDfVersionPolicy aren't much more help, we have
        // to rely on the StateInfo's label from config
        // new change: make sure LC state is labelled on doc
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - make sure initial state's label is there",null,null);
//        StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - looking up initial state",null,null);
//        StateInfo initialstate = stconfig.getFirstStateInfo(mrcsapp,docconfig.getDocumentMrcsLifecycleName(mrcsapp,gftype,doctype));
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - looking up state's label (may NPE here if the state wasn't found)",null,null);
//        String ignorelabel = initialstate.getLabel(); //Just in case it has put the label there, we set ignore label to our desired label
//        String statelabel = initialstate.getLabel();
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - attaching label: "+initialstate.getLabel(),null,null);
//        for (int i = 0; i < newdoc.getVersionLabelCount(); i++)
//        {
//            String label = newdoc.getVersionLabel(i);
//            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsDocumentSBO.applyLifecycle - Scanning Label currently on doc" + i + " : " + label, null, null);
//            if (i > 0)
//            {
//                if (label.equalsIgnoreCase(ignorelabel))
//                {
//                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsDocumentSBO.applyLifecycle - label was already there, unmarking to make sure", null, null);
//                    newdoc.unmark(label);
//                }
//            }
//        }
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:MrcsDocumentSBO.applyLifecycle - officially marking document with initial state label", null, null);
//        newdoc.mark(statelabel);
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.applyLifecycle - policy lifecycle attachment complete",null,null);
//        newdoc.save();
    }

    private void setMrcsDocumentAttributes(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, String newdocid) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.setMrcsDocumentAttributes - setting mrcs attributes",null,null);
        newdoc.setString("mrcs_application",mrcsapp);
        newdoc.setString("mrcs_folder_config",gftype);
        newdoc.setString("mrcs_config",doctype);
        newdoc.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.setMrcsDocumentAttributes - app and config attrs set",null,null);
    }

    private String generateDocumentName(MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, String parentfolderid, Map customdata) throws Exception
    {
        String docname = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName - get doc naming format and config",null,null);
        String namegenerator = docconfig.getGroupingFolderDocumentNamingFormatPlugin(mrcsapp,gftype,doctype);
        Map    configdata    = docconfig.getGroupingFolderDocumentNamingFormatConfig(mrcsapp,gftype,doctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName - instantiating plugin: "+namegenerator,null,null);
        MrcsDocumentNamingFormatPlugin namingplugin = (MrcsDocumentNamingFormatPlugin)Class.forName(namegenerator).newInstance();
        docname = namingplugin.generateName(session,mrcsapp,gftype,doctype,parentfolderid,configdata,customdata);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName - generated name: "+docname,null,null);
        return docname;
    }

    public String generateDocumentName(IDfSession session, String mrcsapp, String gftype, String mrcsdoctype, String parentfolderid, Map customdata) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName(public) - getting document config",null,null);
        MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName(public) - get docbase session",null,null);
//        IDfSession session = createSessionManager(docbase, docconfig.getSystemUsername(mrcsapp), docconfig.getSystemPassword(mrcsapp)).getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName(public) - generating name with private generateName() call",null,null);
        String newname = generateDocumentName(docconfig,session,mrcsapp,gftype,mrcsdoctype,parentfolderid,customdata);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.generateName(public) - generated name: "+newname,null,null);
        return newname;
    }

    public void processImportedDocument(IDfSession session, String mrcsapp, String gftype, String mrcsdoctype, String newdocid, Map customdata) throws Exception
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - getting document config",null,null);
        MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
//        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - get docbase session",null,null);
//        IDfSession session = createSessionManager(docbase, docconfig.getSystemUsername(mrcsapp), docconfig.getSystemPassword(mrcsapp)).getSession(docbase);

        IDfSessionManager sessmgr = session.getSessionManager();
        boolean newtransaction = false;
        if (sessmgr.isTransactionActive())
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - transaction already active on current session manager",null,null);
            // do nothing
        }
        else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - staring transaction for new document",null,null);
            sessmgr.beginTransaction();
            newtransaction = true;
        }
        try {
            IDfSysObject newdoc = (IDfSysObject)session.getObject(new DfId(newdocid));
            // set attributes
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - setting MRCS attributes",null,null);
            setMrcsDocumentAttributes(newdoc,docconfig,session,mrcsapp,gftype,mrcsdoctype,newdocid);

            // apply the document acl
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - setting document ACL",null,null);
            applyDocumentACL(newdoc,docconfig,session,mrcsapp,gftype,mrcsdoctype,newdocid);

            // execute post-creation plugins
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - post-creation plugins",null,null);
            executePlugins(newdoc,docconfig,session,mrcsapp,gftype,mrcsdoctype,newdocid,customdata);

            // lifecycle attachment
            attachLifecycle(newdoc,docconfig, session, mrcsapp, gftype, mrcsdoctype, newdocid);

            // do we need to save??? - call me paranoid...
            newdoc.save();

        } catch (Exception e) {
            // check if we need to rollback
            if (newtransaction)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - rolling back transaction for this new document because of caught exception",null,e);
                sessmgr.abortTransaction();
            }
            throw e;
        }

        if (newtransaction)
        {
            // commit the transaction!
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.processImportedDocument - committing transaction for this new document",null,null);
            sessmgr.commitTransaction();
        }

    }

    public void executePlugins(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, String newdocid, Map customdata) throws Exception
    {
        // exec post-creation plugin (custom attr assignment, etc.)
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.createDocument - exec post-doc create plugin",null,null);
        List plugins = docconfig.getDocumentCreationPlugins(mrcsapp,gftype,doctype);
        if (plugins != null)
        {
            try {
                for (int i=0; i < plugins.size(); i++)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.executePlugins - executing plugin #"+i,null,null);
                    MrcsPlugin plugin = (MrcsPlugin)plugins.get(i);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.executePlugins - class: "+plugin.PluginClassName,null,null);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.executePlugins - instantiating",null,null);
                    MrcsDocumentCreationPlugin postcreate = (MrcsDocumentCreationPlugin)Class.forName(plugin.PluginClassName).newInstance();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.executePlugins - getting config",null,null);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.executePlugins - calling processDocument()",null,null);
                    postcreate.processDocument(session,mrcsapp,gftype,doctype,newdoc,plugin.PluginConfiguration,customdata);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsDocumentSBO.executePlugins - finished processDocument()",null,null);
                }
            } catch (Exception e) {
                //log it...
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsDocumentSBO.executePlugins - error occurred executing post-creation plugin",e);
                //?rollback?
                //rethrow
                throw (e);
            }
        }
    }

}
