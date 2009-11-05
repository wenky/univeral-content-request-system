package com.medtronic.documentum.mrcs.method;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsGroupingFolderAllowableDocument;
import com.medtronic.documentum.mrcs.config.MrcsGroupingFolderType;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsDocumentCreationPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsDocumentNamingFormatPlugin;

public class PulseMigrationImportService {
	
	// utility method for migrating Pulse docbase applications that autoimport documents
	// Old Pulse didn't do anything with a document, but the newer MRCS-based pulse 
	// must have imported documents processed so they are MRCS compatible.
	// 
	// IDfSession - session of MRCS system user or similar capability
	// docid - document if of already-created but uninitialized imported document
	// mrcsapp - mrcsapplication (if not provided, the first linked folder's mrcsapp is used)
	// mrcsdoctype - mrcs doc type (if not provided, the first allowable document of the first linked folder)
	// userspecifiedname = name fragment used if the desired document uses UserSpecified in the naming plugins
	
	public static void initializeMrcsDocument(IDfSession session, IDfId folderid,IDfId docid, String mrcsapp, String mrcsfoldertype, String mrcsdoctype, Map namingcontext) throws Exception
	{
       	/*-CONFIG-*/String m="PulseMigrationImportService.initializeMrcsDocument - "; Class c = PulseMigrationImportService.class;

       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"top" , null, null);
		// get uninitialized document
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"lookup docid "+docid , null, null);
		IDfSysObject doc = (IDfSysObject)session.getObject(docid);
		// get MrcsApplication config
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"get doc config factory" , null, null);
		MrcsDocumentConfigFactory stconfig = MrcsDocumentConfigFactory.getDocumentConfig();
		// check if mrcsapp
		if (folderid == null)
       	    for (int f=0; f < doc.getFolderIdCount(); f++) {
       	    	String tmpfolderid = doc.getFolderId(0).getId();
       	    	try { 
       	    		IDfSysObject folder = (IDfSysObject)session.getObject(new DfId(tmpfolderid));
       	    		if (folder.hasAttr("mrcs_application")) {
       	    			folderid = new DfId(tmpfolderid);
       	    			f = doc.getFolderIdCount();
       	    		}
       	    	} catch (DfException dfe) {
               	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"llinked folder lookup for id "+tmpfolderid+" failed, ignoring", null, dfe);
       	    	}
       	    }
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"lookup folderid "+folderid == null ? null : folderid.getId() , null, null);
		IDfSysObject folder = (IDfSysObject)session.getObject(folderid);

		if (mrcsapp == null) {
			mrcsapp = folder.getString("mrcs_application");				
		}
		if (mrcsfoldertype == null) {
			mrcsfoldertype = folder.getString("mrcs_config");
		}
		if (mrcsdoctype == null)
		{
			MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)stconfig.getApplication(mrcsapp).GroupingFolderTypes.get(mrcsfoldertype);
			MrcsGroupingFolderAllowableDocument doctype  = (MrcsGroupingFolderAllowableDocument)gftype.AllowableDocumentTypes.get(0);
			mrcsdoctype = doctype.DocumentType;
		}


		// set mrcsapp, mrcs folder config, and mrcs doctype
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"set mrcsapp: "+mrcsapp + " mrcsgf: "+mrcsfoldertype+" mrcsdoc: "+mrcsdoctype , null, null);
		doc.setString("mrcs_application",mrcsapp);
		doc.setString("mrcs_folder_config",mrcsfoldertype);
		doc.setString("mrcs_config",mrcsdoctype);		
		
		// generate document name
		//Map namingcontext = new HashMap();
		//namingcontext.put("ObjectName",userspecifiedname);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"gen docname" , null, null);
		String objectname = generateDocumentName(stconfig,session,mrcsapp,mrcsfoldertype,mrcsdoctype,folder.getObjectId().getId(),namingcontext);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"set docname: "+objectname , null, null);
		doc.setObjectName(objectname);
		doc.save();
		
		// lifecycle, acl, etc
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"apply ACL" , null, null);
		applyDocumentACL(doc,stconfig,session,mrcsapp,mrcsfoldertype,mrcsdoctype);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"exec creation plugins" , null, null);
        executePlugins(doc,stconfig,session,mrcsapp,mrcsfoldertype,mrcsdoctype,namingcontext);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"attach lifecycle" , null, null);
		attachLifecycle(doc,stconfig,session,mrcsapp,mrcsfoldertype,mrcsdoctype);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(c))DfLogger.debug(c, m+"DONE" , null, null);
		
		
	}

    public static String generateDocumentName(MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, String parentfolderid, Map customdata) throws Exception
    {
        String docname = null;
        String namegenerator = docconfig.getGroupingFolderDocumentNamingFormatPlugin(mrcsapp,gftype,doctype);
        Map    configdata    = docconfig.getGroupingFolderDocumentNamingFormatConfig(mrcsapp,gftype,doctype);
        MrcsDocumentNamingFormatPlugin namingplugin = (MrcsDocumentNamingFormatPlugin)Class.forName(namegenerator).newInstance();
        docname = namingplugin.generateName(session,mrcsapp,gftype,doctype,parentfolderid,configdata,customdata);
        return docname;
    }

    public static void applyDocumentACL(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype) throws Exception
    {
        // apply the acl...
        // look up the associated ACL
        String ACL = docconfig.getDocumentACL(mrcsapp,gftype,doctype);
        String systemdomain = session.getServerConfig().getString("operator_name");
        IDfACL newACL = session.getACL(systemdomain, ACL);
        if (newACL == null)
        {
            Exception e = new Exception("MRCS could not locate ACL for your user. ACL: "+ACL+" user: "+session.getLoginUserName());
            throw e;
        }
        newdoc.setACL(newACL);
        newdoc.save();
    }

    public static void attachLifecycle(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype) throws Exception
    {
        String lifecyclename = docconfig.getDocumentLifecycle(mrcsapp,gftype,doctype);
        IDfSysObject lifecycle = (IDfSysObject)session.getObjectByQualification("dm_sysobject where object_name ='" + lifecyclename + "'");
        newdoc.attachPolicy(lifecycle.getObjectId(), "", "");
        newdoc.save();
    }
	
    public static void executePlugins(IDfSysObject newdoc, MrcsDocumentConfigFactory docconfig, IDfSession session, String mrcsapp, String gftype, String doctype, Map customdata) throws Exception
    {
        // exec post-creation plugin (custom attr assignment, etc.)
        List plugins = docconfig.getDocumentCreationPlugins(mrcsapp,gftype,doctype);
        if (plugins != null)
        {
            try {
                for (int i=0; i < plugins.size(); i++)
                {
                    MrcsPlugin plugin = (MrcsPlugin)plugins.get(i);
                    MrcsDocumentCreationPlugin postcreate = (MrcsDocumentCreationPlugin)Class.forName(plugin.PluginClassName).newInstance();
                    postcreate.processDocument(session,mrcsapp,gftype,doctype,newdoc,plugin.PluginConfiguration,customdata);
                }
            } catch (Exception e) {
                //log it...
                throw (e);
            }
        }
    }

}

