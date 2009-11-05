package com.medtronic.ecm.documentum.mdtworkflow.webtop.common;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtSignatureService;

public class MdtSignatureService implements IMdtSignatureService 
{	

	public void sign(IDfSessionManager mgr, String docbase, String mrcsapp, String processname, IWorkflowTask task, IDfDocument primarypackage,String username, String password, String reason, Map context) 
	{		
    	// get the session
    	IDfSession session = null;	    
	    IDfCollection attachments = null;
	    try {
		    /*-dbg-*/Lg.dbg("get session");
	    	session = mgr.getSession(docbase);
	    	
		    /*-dbg-*/Lg.dbg("lookup workitem");
			IDfWorkitem workitem = (IDfWorkitem)session.getObject(task.getId("item_id"));
			
			// sign primary package
		    /*-dbg-*/Lg.dbg("-- performing TCS pdf-based signature process");
		    boolean primaryflag = signDocument(session,primarypackage,username,password,reason);
		    /*-dbg-*/if(Lg.dbg())Lg.dbg("-- signing result: "+primaryflag);			
	    	
		    /*-dbg-*/Lg.dbg("getting ATTACHMENT collection for workitem");
	    	attachments =  workitem.getAttachments();
			while(attachments.next()) 
			{
			    /*-dbg-*/Lg.dbg("--NEXT ATTACHMENT--");
			    /*-trc-*/if(Lg.trc())for (int i = 0; i < attachments.getAttrCount(); i++) 
			    /*-trc-*/{
			    	/*-trc-*/Lg.trc("attr:    "+attachments.getAttr(i).getName());
		            /*-trc-*/Lg.trc("type:    "+attachments.getAttr(i).getDataType());
		            /*-trc-*/Lg.trc("repeats: "+attachments.getAttr(i).isRepeating());		            
		            /*-trc-*/Lg.trc("val:     "+attachments.getValueAt(i).asString());
		        /*-trc-*/}
			    String compid = attachments.getString("r_component_id");
			    /*-dbg-*/Lg.dbg("--ATTACHMENT id: "+compid);
	    		if (compid != null) {
				    /*-dbg-*/Lg.dbg("--getting ATTACHMENT from docbase");
	    			IDfDocument attacheddoc = (IDfDocument)session.getObject(new DfId(compid));
				    /*-dbg-*/Lg.dbg("--pkgdoc retrieved: %s",attacheddoc);
				    
				    /*-dbg-*/Lg.dbg("--getting most recent version of attachment");
	    			IDfDocument pkgdoc = (IDfDocument)session.getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
				    /*-dbg-*/Lg.dbg("--most recent retrieved: %s ",pkgdoc);
				    
				    /*-dbg-*/Lg.dbg("-- performing TCS pdf-based signature process");
				    boolean flag = signDocument(session,pkgdoc,username,password,reason);
				    /*-dbg-*/if(Lg.dbg())Lg.dbg("-- signing result: "+flag);
	    		}
	    	}
	    } catch (DfException e) {
    	    /*-ERROR-*/Lg.err("error in promoting workitem attachments...",e);
    	    throw EEx.create("SignatureService","error in promoting workitem attachments...",e);
	    } finally {
		    /*-dbg-*/Lg.dbg("finally, close the attachment collection");
	    	try{if (attachments != null)attachments.close();} catch (Exception e){}
	    	try{mgr.release(session);}catch(Exception e){Lg.wrn("Unable to release session",e);}
	    }
		

	}
	
	public boolean signDocument(IDfSession session, IDfDocument docObject, String username, String password, String reason) throws DfException
	{
	    
    	boolean canCommit = false;    
    	try {
        
    		/*-dbg-*/Lg.dbg("usrName %s",username);
    		/*-dbg-*/Lg.dbg("reasonTxt %s",reason);
    		/*-dbg-*/Lg.dbg("calling add eSign for docObject %s",docObject);
    		IDfId auditRec = docObject.addESignature(username,password,reason,"pdf","","","","","","");
            /*-dbg-*/if(Lg.dbg())Lg.dbg("eSign audit record: %s",(auditRec == null ? null : auditRec.getId()));
            
            try {
                /*-dbg-*/Lg.dbg("verifying esignature");                
                docObject.verifyESignature();
            } catch (Exception e) {
                /*-ERROR-*/Lg.err("verifyESignature on docObject %s detected error",docObject,e);
                throw EEx.create("SignatureVerify","verifyESignature on docObject %s detected error",docObject,e);
            }
    		canCommit = true;
    
        }catch(DfException e1){
        	canCommit = false;
        	/*-ERROR-*/Lg.err("Exception Occurred in eSign service on doc %s",docObject,e1);
            throw EEx.create("SignatureError","Signature of docObject %s errored",docObject,e1);
    	}
        return canCommit;
	}	
	

}
