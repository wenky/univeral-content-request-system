package com.medtronic.documentum.mrcs.method;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.documentum.mrcs.server.MrcsRollbackService;

// This needs to do additional work above and beyond the current MrcsPromoteMethod:
// - apply promotions to multiple packages (uhhh, we'll TODO that sucker for now)
// - promoted packages need to be refreshed in the active process task (A NO-OP task after this task will refresh it...)


public class MrcsMultiDocRollbackMethod extends MrcsConfigurableMethod implements IDmMethod
{
    public void execute(Map parameters, OutputStream output) throws Exception
    {
       	/*-CONFIG-*/String m="MrcsRollbackMethod.execute - ";
    	// what is it passing us for arguments?
    	// answer: docbase, ticket, user, packageId, mode
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))try {Iterator i = parameters.keySet().iterator(); while (i.hasNext()){DfLogger.debug(this, m+"paramkey: "+(String)i.next() , null, null);}} catch (Exception e) {}

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting docbase from JSM parameters" , null, null);
    	String[] paramvals = (String[])parameters.get("docbase_name");
    	String docbase = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~docbase: "+docbase , null, null);
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting packageId from JSM parameters" , null, null);
    	paramvals = (String[])parameters.get("packageId");
    	String packageid = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~packageId: "+packageid, null, null);
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting 'mode' from JSM parameters" , null, null);
    	paramvals = (String[])parameters.get("mode");
    	String mode = paramvals[0];
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"  ~~MODE: "+mode , null, null);

   	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting mrcs system session", null, null);
	    IDfSessionManager sessionmgr = getMrcsSystemUserSessionFromFirstAttachment(parameters);
	    IDfSession session = sessionmgr.getSession(docbase);
       	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session DMCL id: "+session.getDMCLSessionId(), null, null);

    	// get workitem
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"retreive workitem (the 'packageid')", null, null);
	    IDfWorkitem workitem = (IDfWorkitem)session.getObject(new DfId(packageid));
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"...retrieved workitem "+packageid+"? "+(workitem !=null), null, null);

	    // acquire if not mode 0. ?what? - it's what the docbasic thingy does
	    if ("0".equals(mode))
	    {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mode is 0, acquiring workitem...", null, null);
	    	workitem.acquire();
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"...acquired", null, null);
	    }
	    IDfCollection packages = null;
	    try {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting package collection for workitem", null, null);
	    	packages = workitem.getPackages(""); // returns visible and invisible. What's an invisible package?
			while(packages.next())
			{
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--NEXT PACKAGE--", null, null);
			    for (int i = 0; i < packages.getAttrCount(); i++)
			    {
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attr:    "+packages.getAttr(i).getName(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"type:    "+packages.getAttr(i).getDataType(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"repeats: "+packages.getAttr(i).isRepeating(), null, null);
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"val:     "+packages.getValueAt(i).asString(), null, null);
			    }
			    String compid = packages.getString("r_component_id");
			    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--package id: "+compid, null, null);
	    		if (compid != null) {
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--getting package from docbase", null, null);
	    			IDfDocument pkgdoc = (IDfDocument)session.getObject(new DfId(compid));
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--pkgdoc retrieved: "+pkgdoc.getObjectName(), null, null);
	    			// rollback
	    	        try {
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"invoking rollback from workflow method", null, null);
    		            MrcsRollbackService.performRollback(pkgdoc,"workflow");
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"rollback returned successfully", null, null);
	    	        } catch (Exception e) {
	    	    	    /*-ERROR-*/DfLogger.error(this, m+"error while loading or executing promotion service module",null,e);
	    	            throw e;
	    	        }
	    		}
	    	}
	    } catch (Exception e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem packages...",null,e);
    	    sessionmgr.release(session);
    	    throw e;
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the package collection", null, null);
	    	if (packages != null)
	    		packages.close();
	    }

	    // okey-dokey: now check for additional attachments to the workflow...
	    IDfCollection attachments = null;
	    try {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"getting ATTACHMENT collection for workitem", null, null);
	    	attachments =  workitem.getAttachments();
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


	    			// rollback
	    	        try {
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"invoking ROLLBACK from workflow", null, null);
    		            MrcsRollbackService.performRollback(pkgdoc,"workflow");
    		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"rollback returned successfully", null, null);
	    	        } catch (Exception e) {
	    	    	    /*-ERROR-*/DfLogger.error(this, m+"error while loading or executing promotion service module",null,e);
	    	            throw e;
	    	        }
	    		}
	    	}
	    } catch (Exception e) {
    	    /*-ERROR-*/DfLogger.error(this, m+"error in promoting workitem attachments...",null,e);
    	    sessionmgr.release(session);
    	    throw e;
	    } finally {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"finally, close the attachment collection", null, null);
	    	if (attachments != null)
	    		attachments.close();
	    }

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"completing workitem...", null, null);
	    workitem.complete();

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"releasing session", null, null);
    	sessionmgr.release(session);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"session released", null, null);
    }

}
