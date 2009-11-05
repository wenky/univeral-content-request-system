/*
 * Created on Jan 12, 2005
 *
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

 Filename       $RCSfile: ESignServiceSBO.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2007/01/10 16:30:08 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfList;
import com.documentum.services.workflow.common.IWorkflowTaskAttachment;
import com.documentum.services.workflow.inbox.IRouterTask;
import com.documentum.services.workflow.inbox.ITask;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.documentum.mrcs.client.ESignHelper;
import com.medtronic.documentum.mrcs.sbo.dto.ESignDTO;

/**
 * @author prabhu1
 *
 * The ESignature Service Business Object
 */
public class ESignServiceSBO extends DfService implements IESignServiceSBO {

	private String vendorString =  "Medtronic, Inc.";
	private static final String version = "4.0";

	/* (non-Javadoc)
	 * @see com.documentum.fc.client.IDfService#getVendorString()
	 */
	public String getVendorString() {
			return vendorString;
	}
	/* (non-Javadoc)
	 * @see com.documentum.fc.client.IDfService#getVersion()
	 */
	public String getVersion() {
		return version;
	}
	/* (non-Javadoc)
	 * @see com.documentum.fc.client.IDfService#isCompatible(java.lang.String)
	 */
	public boolean isCompatible(String arg0) {
        int i = arg0.compareTo(getVersion() );
        if(i <= 0 )
            return true;
        else
            return false;
	}

/* (non-Javadoc)
 * @see com.medtronic.documentum.mrcs.sbo.IESignServiceSBO#signDocument(com.medtronic.documentum.mrcs.sbo.dto.ESignDTO, com.documentum.services.workflow.inbox.ITask, com.documentum.fc.client.IDfSession, java.lang.String)
 */
	public boolean signDocument(IDfDocument docObject, ESignDTO sign ) throws DfException
    {
    	boolean canCommit = false;
    
    	try{
    
    		String usrName = sign.getUsr();
    		String pswd = sign.getPswd();
    		String reasonTxt = sign.getReason();
    
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument : usrName "+usrName, null, null);
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument : reasonTxt "+reasonTxt, null, null);
    
    		//com.documentum.fc.client.IDfDocument docObject = ESignHelper.getSignableDocument(itask,taskInfo);
    		/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : calling add eSign for docObject "+docObject, null, null);
    		com.documentum.fc.common.IDfId auditRec = docObject.addESignature(usrName,pswd,reasonTxt,"pdf","","","","","","");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : eSign audit record id: "+(auditRec == null ? null : auditRec.getId()), null, null);
            
            //try {
            //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : -->attempting auditrec dump", null, null);
            //    IDfSession sess = this.getSession(docObject.getMasterDocbase());
            //    String dump = (auditRec == null ? null : sess.apiGet("dump",auditRec.getId()));
            //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : -->dump,c: "+dump, null, null);
            //} catch (Exception e) {
            //    // CEM - this is intentionally DEBUG'd...
            //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : -->could not execute dump command",null,e);                
            //}
            try {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignServiceSBO.signDocument  : verifying esignature",null,null);                
                docObject.verifyESignature();
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this, "MRCS:ESignServiceSBO.signDocument  : verifyESignature on docObject "+docObject.getObjectId().getId()+" detected error",null,e);
                throw new RuntimeException("MRCSError in verification of signature (ESignServiceSBO)");
            }
    		//signoff(java.lang.String user,java.lang.String os_password, java.lang.String reason)
    		canCommit = true;
    
        }catch(DfException e1){
        	canCommit = false;
        	/*-ERROR-*/DfLogger.error(this, "MRCS:ESignServiceSBO.signDocument : Exception Occurred in eSign service", null, e1);
        	throw e1;
    	}
        return canCommit;
	}

}
