package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowActionPlugin;

public class MrcsWorkflowClientVersion implements IMrcsWorkflowActionPlugin 
{

	public void execute(IDfSessionManager sMgr, String docbase, IWorkflowTask task, IDfWorkflow workflow, String mrcsapp, Map pluginconfig, Map context)
	{
		/*-CONFIG-*/String m="execute-";
		try { 
			IDfSession session = sMgr.getSession(docbase);
			// get attachment...
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"GET ATTACHED DOCUMENT FROM TASK", null, null);
            IDfSysObject doc = MrcsScheduleJob.getSignableDocument(session,task);
            
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"look up desired version mode from config", null, null);
            String nextLabel = (String)pluginconfig.get("versiontype");
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"PERFORM versioning: "+nextLabel, null, null);
            String verLabel = null;
            IDfVersionPolicy verPolicy = doc.getVersionPolicy();
            IDfId newid = null;
            if (nextLabel.equalsIgnoreCase("MAJOR")) {
                verLabel = verPolicy.getNextMajorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next MAJOR verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                newid = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("MINOR")) {
                verLabel = verPolicy.getNextMinorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next MINOR verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                newid = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("BRANCH")) {
                verLabel = verPolicy.getBranchLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next BRANCH verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                newid = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("SAME")) {
                verLabel = verPolicy.getSameLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : Next SAME verLabel  " + verLabel, null, null);
                doc.save();
                newid = doc.getObjectId();
            }
            // putting newid in context, in case it's needed
            context.put("NewDocumentId",newid);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+" : Error encountered  - labelVersionChange ; ", null, e);
            throw new RuntimeException("Error performing document version change during promote/demote");
        }
	}

}
