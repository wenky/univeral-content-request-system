package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfLogger;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.documentum.mrcs.server.MrcsPromotionServiceModule;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowActionPlugin;

public class MrcsWorkflowClientPromote implements IMrcsWorkflowActionPlugin {

	public void execute(IDfSessionManager sMgr, String docbase, IWorkflowTask task, IDfWorkflow workflow, String mrcsapp, Map config, Map context)
	{
		/*-CONFIG-*/String m="execute-";
		try { 
			IDfSession session = sMgr.getSession(docbase);
			// get attachment...
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"GET ATTACHED DOCUMENT FROM TASK", null, null);
            IDfDocument attachedDoc = (IDfDocument)MrcsScheduleJob.getSignableDocument(session,task);
            
			// get promotion service module from 
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"prepare to retrieve promotion service module from registry", null, null);
			//IDfClient client = session.getClient();
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"determining global registry docbase", null, null);
			//String registrydocbase = client.getModuleRegistry().getRegistryHostName();
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"global registry docbase: "+registrydocbase, null, null);
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retrieving promotion service module from registry", null, null);
		    //IDfModule promoteservicemodule = client.newModule(registrydocbase,"MrcsPromoteServiceModule",sMgr);
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"module retrieved? "+(promoteservicemodule != null), null, null);
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"casting module to promotion service interface", null, null);
		    //IMrcsPromotionService promotionservice = (IMrcsPromotionService)promoteservicemodule; 
		    ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"invoking promote", null, null);
			//promotionservice.promoteMrcsDocument(attachedDoc);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Instantiating workflow service client-side", null, null);
        	MrcsPromotionServiceModule promotionservice = new MrcsPromotionServiceModule();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"invoking promote from workflow client-side", null, null);
            promotionservice.promoteMrcsDocument(attachedDoc);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"promote returned successfully", null, null);
		    
		} catch (Exception e) {
		    /*-ERROR-*/DfLogger.error(this, m+"ERROR in client-side workflow promote", null, e);
		    throw new RuntimeException(e);
		}
	}

}
