package com.medtronic.documentum.mrcs.server.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class VersionAttachedDocument implements IMrcsWorkflowServerPlugin
{
	static int i = 0;
	
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{
		/*-CONFIG-*/String m="execute-"+(i++)+'-';
		IDfSession session = null;
		try { 
			session = task.getSession();
			// get attachment...
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"GET ATTACHED DOCUMENT FROM TASK ", null, null);
			IDfCollection packages = task.getPackages("");
		    IDfDocument doc = null;
			if (packages.next())
			{
		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if there are packages in the r_component_id multivalue attr" , null, null);
				if (packages.getValueCount("r_component_id") > 0)
				{
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting the first component id" , null, null);
					IDfId id = packages.getRepeatingId("r_component_id",0);
			        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving component "+id , null, null);
				    doc = (IDfDocument)session.getObject(id);
				}
			}
			packages.close();
			
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get current log_entry", null, null);
            String logentry = doc.getString("log_entry");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"- logentry: "+logentry, null, null);
			
		                
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"look up desired version mode from config", null, null);
            String nextLabel = (String)config.get("Type");
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"PERFORM versioning: "+nextLabel, null, null);
            String verLabel = null;
            IDfVersionPolicy verPolicy = doc.getVersionPolicy();
            IDfId newid = null;
            if (nextLabel.equalsIgnoreCase("MAJOR")) {
                verLabel = verPolicy.getNextMajorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next MAJOR verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                doc.mark("CURRENT");
                newid = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("MINOR")) {
                verLabel = verPolicy.getNextMinorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next MINOR verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                doc.mark("CURRENT");
                newid = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("BRANCH")) {
                verLabel = verPolicy.getBranchLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next BRANCH verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                doc.mark("CURRENT");
                newid = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("SAME")) {
                verLabel = verPolicy.getSameLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next SAME verLabel  " + verLabel, null, null);
                doc.mark("CURRENT");
                doc.save();
                newid = doc.getObjectId();
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get new doc so we can preserve the logentry", null, null);
            IDfSysObject newdoc = (IDfSysObject)doc.getSession().getObject(newid);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting logentry", null, null);
            newdoc.setString("log_entry",logentry);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            newdoc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saved", null, null);
            
            // putting newid in context, in case it's needed
            context.put("NewDocumentId",newid);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+" : Error encountered  - labelVersionChange ; ", null, e);
            throw new RuntimeException("Error performing document version change during promote/demote");
        }
		
	}

	

}
