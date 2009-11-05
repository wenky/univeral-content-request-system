package com.medtronic.documentum.mrcs.method;

import java.io.OutputStream;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.mthdservlet.IDmMethod;

public class PulseMigrationImportMethod extends MrcsConfigurableMethod implements IDmMethod {

	// implement the default configurable method - execute listed actions in workflow task
	public void execute(Map parameters, OutputStream outputstream) throws Exception
	{
       	/*-CONFIG-*/String m="PulseMigrationImportMethod.execute - ";
       	
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"---params---" , null, null);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))try {Iterator i = parameters.keySet().iterator(); while (i.hasNext()){DfLogger.debug(this, m+"paramkey: "+(String)i.next() , null, null);}} catch (Exception e) {}
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"---done---" , null, null);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting docbase from JSM parameters" , null, null);
    	String[] paramvals = (String[])parameters.get("docbase_name");
    	String docbase = paramvals[0];
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--docbase: "+docbase , null, null);

       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting folderid" , null, null);
       	String folderid = null;
    	if (parameters.containsKey("folder_id")) {
	    	paramvals = (String[])parameters.get("folder_id");
	    	folderid = paramvals[0];
    	}
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--FOLDERID: "+folderid , null, null);

       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting docid" , null, null);
    	paramvals = (String[])parameters.get("document_id");
    	String docid = paramvals[0];
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--docid: "+docid , null, null);

       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcs doctype" , null, null);
    	String mrcsdoctype = null;
    	if (parameters.containsKey("mrcs_doctype")) {
        	paramvals = (String[])parameters.get("mrcs_doctype");
    		mrcsdoctype = paramvals[0];
    	}
    	
    	Map context = new HashMap();
    	
    	Iterator params = parameters.keySet().iterator();
    	while (params.hasNext())
    	{
    		String key = (String)params.next();
    		paramvals = (String[])parameters.get(key);
    		if (paramvals != null && paramvals.length == 1)
    		{
    	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"insert singleval key "+key+" value "+paramvals[0],null,null);
    			context.put(key,paramvals[0]);    			
    		} else {    			
    			context.put(key,paramvals);
    		}
    	}
    	// get MRCS folder if not passed (so we can use it to get the system session)
    	if (folderid == null) {
       	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"folderid not passed, assume it is the first linked folder of the doc", null, null);
    		IDfSessionManager usersmgr = super.getUserSession(parameters);
    		IDfSession usrsess = null;
    		try {
           	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get user session", null, null);
    			usrsess = usersmgr.getSession(docbase);
           	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get doc "+docid, null, null);
    			IDfSysObject doc = (IDfSysObject)usrsess.getObject(new DfId(docid));
           	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get first linked mrcs folder", null, null);
           	    for (int f=0; f < doc.getFolderIdCount(); f++) {
           	    	String tmpfolderid = doc.getFolderId(0).getId();
           	    	try { 
           	    		IDfSysObject folder = (IDfSysObject)usrsess.getObject(new DfId(tmpfolderid));
           	    		if (folder.hasAttr("mrcs_application")) {
           	    			folderid = tmpfolderid;
           	    			f = doc.getFolderIdCount();
           	    		}
           	    	} catch (DfException dfe) {
                   	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"llinked folder lookup for id "+tmpfolderid+" failed, ignoring", null, dfe);
           	    	}
           	    }
           	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- folderid: "+folderid, null, null);
    		} finally {
    			usersmgr.release(usrsess);
    		}
    	}

   	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcs system session", null, null);
	    IDfSessionManager sessionmgr = super.getMrcsSystemUserSessionFromObjectId(parameters,folderid);	    
	    IDfSession session = null;
	    try {
	    	session = sessionmgr.getSession(docbase);
	       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"call import postprocess method service",null,null);
	    	PulseMigrationImportService.initializeMrcsDocument(session,folderid == null ? null : new DfId(folderid),new DfId(docid),null,null,mrcsdoctype,context);
	    } finally {
	    	sessionmgr.release(session);
	    }
	}

}
