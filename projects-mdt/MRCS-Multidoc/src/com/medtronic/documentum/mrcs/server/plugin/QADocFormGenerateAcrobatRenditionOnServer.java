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
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.plugin.GenerateAcrobatRendition;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsWorkflowServerPlugin;

public class QADocFormGenerateAcrobatRenditionOnServer implements IMrcsWorkflowServerPlugin
{
	public void execute(IDfSessionManager sMgr, String docbase, IDfWorkitem task, IDfWorkflow workflow, IDfProcess process, String mrcsapp, Map config, Map context)
	{		
       	/*-CONFIG-*/String m="QADocFormGenerateAcrobatRenditionOnServer.execute - ";
       	GenerateAcrobatRendition gar = new GenerateAcrobatRendition();
       	IDfSession session = null;
       	try {
       		session = sMgr.getSession(docbase);
			// get attachment...
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workitem", null, null);
	    	IDfCollection attachments =  task.getAttachments();
			while(attachments.next()) 
			{
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT ATTACHMENT--", null, null);
			    for (int i = 0; i < attachments.getAttrCount(); i++) 
			    {
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attr:    "+attachments.getAttr(i).getName(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"type:    "+attachments.getAttr(i).getDataType(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"repeats: "+attachments.getAttr(i).isRepeating(), null, null);		            
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"val:     "+attachments.getValueAt(i).asString(), null, null);
			    }
			    String compid = attachments.getString("r_component_id");
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--ATTACHMENT id: "+compid, null, null);
	    		if (compid != null) {
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting ATTACHMENT from docbase", null, null);
	    			IDfDocument attacheddoc = (IDfDocument)session.getObject(new DfId(compid));
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--pkgdoc retrieved: "+attacheddoc.getObjectName() + " - "+attacheddoc.getObjectId().getId(), null, null);
				    
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting most recent version of attachment", null, null);
	    			IDfDocument pkgdoc = (IDfDocument)session.getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--most recent retrieved: "+pkgdoc.getObjectName() + " - "+pkgdoc.getObjectId().getId(), null, null);
				    
	    	        try {
	    		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"CALLING render plugin", null, null);
	    	       		gar.render(session,pkgdoc,config);
	    	       		// need to save...this may cause deadlock errors if in a transaction?
	    		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving doc changes", null, null);
	    		        pkgdoc.save();
	    		        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"doc save complete", null, null);
	    	        	
	    	        } catch (Exception e) {
	    	    	    /*-ERROR-*/DfLogger.error(this, m+"error while loading or executing promotion service module",null,e);
	    	            throw e;        	
	    	        }
	    		}
	    	}
       		
		} catch (Exception dfe) {
	       	/*-ERROR-*/DfLogger.error(this, m+"ERROR in generate rendtion on server event" , null, dfe);
			throw new RuntimeException("Error in workflow plugin - generate rendition on server event plugin",dfe);
       	} finally { sMgr.release(session); }
	}

}
