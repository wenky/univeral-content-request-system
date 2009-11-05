package com.medtronic.documentum.mrcs.server.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.plugin.GenerateAcrobatRendition;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class GenerateAcrobatRenditionOnServer implements IMrcsWorkflowServerPlugin
{
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{		
       	/*-CONFIG-*/String m="GenerateAcrobatRendition.execute - ";
       	GenerateAcrobatRendition gar = new GenerateAcrobatRendition();
       	IDfSession session = null;
       	try {
       		session = sMgr.getSession(docbase);
			session = task.getSession();
			// get attachment...
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"GET ATTACHED DOCUMENT FROM TASK ", null, null);
			IDfCollection packages = task.getPackages("");
		    IDfDocument doc = null;
		    try { 
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
		    } finally { packages.close(); }
       		
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"CALLING render plugin", null, null);
       		gar.render(session,doc,config);
       		// need to save...this may cause deadlock errors if in a transaction?
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving doc changes", null, null);
       		doc.save();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"doc save complete", null, null);
		} catch (Exception dfe) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in generate rendtion on server event" , null, dfe);
			throw new RuntimeException("Error in workflow plugin - generate rendition on server event plugin",dfe);
       	} finally { sMgr.release(session); }
	}

}
