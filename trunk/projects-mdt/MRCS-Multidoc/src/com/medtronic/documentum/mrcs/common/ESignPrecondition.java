/*
 * Created on Feb 18, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
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

 Filename       $RCSfile: ESignPrecondition.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/09/22 14:47:32 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.documentum.services.workflow.inbox.ITask;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.medtronic.documentum.mrcs.sbo.dto.ESignDTO;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class ESignPrecondition implements MrcsPreConditions{

    private IDfSession session = null;
    private ITask tsk = null;
    private ESignDTO signature = null;
    private IDfDocument docObject = null;
    private int noSigns = 0;
    
    public int rejectReason = -1;

    /**
     *
     */
    public ESignPrecondition() {
        super();
        }


    public void initialize(IDfSession sesn, IDfDocument doc, ITask task, ESignDTO sign, Integer NoOfSigns) throws DfException{
        session = sesn;
        tsk = task;
        signature = sign;
		docObject = doc;
		noSigns = NoOfSigns.intValue();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.initialize : session "+session, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.initialize : tsk "+tsk, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.initialize : signature "+signature, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.initialize : docObject "+docObject, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.initialize : noSigns "+noSigns, null, null);
    }
/*
 * Same Approver cannot sign the same version of the document more than once.
 * Any pdf version of the document can be signed/routed for signatures only
 * for a max of 6 times.
 * Task State greater than 3 is treated as inappropriate state for signing the
 * document.
 */
    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.common.MrcsPreConditions#isEffectual()
     */
    public boolean isTaskEffectual(Map objectParams, Map pluginConfig) throws DfException {

        if((IDfDocument)objectParams.get("IDfDocument") == null) return false;
        initialize((IDfSession)objectParams.get("IDfSession") , (IDfDocument)objectParams.get("IDfDocument"), (ITask)objectParams.get("ITask"), (ESignDTO)objectParams.get("ESignDTO"), (Integer)objectParams.get("NoOfSigns"));

        boolean canSignDoc = false;
        try{
         if(((IWorkflowTask)tsk).isSignOffRequired()){

             /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.isTaskEffectual :tsk.getTaskState()  "+tsk.getTaskState(), null, null);

 	        //Check for valid task State
 	        if(tsk.getTaskState() < 3)canSignDoc = true;
            //Check for valid rendition
            if (canSignDoc) {
                canSignDoc = isRenditionExist(docObject.getObjectId().getId(), "pdf");
                if (!canSignDoc)rejectReason = 1;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.isTaskEffectual : canSignDoc(isRenditionExist) : "+canSignDoc, null, null);
            }
 	        //Check for no of signatures
 	        if(canSignDoc)
            {
                canSignDoc = numSigned();
                if (!canSignDoc)rejectReason = 2;
            }
 	        //Check for valid signatures
 			if(canSignDoc)
            {
                canSignDoc = isQualifiableForSign();
                if (!canSignDoc)rejectReason = 3;
            }
         }else{
             canSignDoc = true;
         }
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.isTaskEffectual : "+canSignDoc, null, null);
        }catch(Exception e){
            /*-ERROR-*/DfLogger.error(this, "MRCS:ESignPrecondition.isTaskEffectual :Exception occurred : "+e, null, e);
            throw new RuntimeException("Error in determining if document can be eSigned",e);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:ESignPrecondition.isTaskEffectual : canSignDoc(before return) : "+canSignDoc, null, null);
         return canSignDoc;
    }

    private boolean isRenditionExist(String objectId, String format) throws DfException
    {
        /*-CONFIG-*/String m = "isRenditionExist - ";
        boolean flag = false;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        if(objectId == null || format == null)
            throw new IllegalArgumentException("Object ID and format must be valid!");
        IDfCollection idfcollection = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing rendition search", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-- DQL ["+"select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "'"+"]", null, null);        
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL("select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "'");
        idfcollection = dfquery.execute(session, 0);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"check if search returned items, thus a rendition found", null, null);
        flag = idfcollection.next();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"found? "+flag, null, null);
        idfcollection.close();
        return flag;
    }

    private boolean numSigned() throws DfException {
        /*-CONFIG-*/String m = "numSigned - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        
        double numSigned = 0;
        String docId = docObject.getObjectId().getId();

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"numSigned:   "+numSigned, null, null);

        String qualification = "dm_audittrail where " +
		"audited_obj_id='"+docId+"' and " +
		"version_label='"+docObject.getVersionLabel(0)+"'";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing DQL ["+"select count(*) from "+qualification+"]", null, null);

        IDfQuery qry = new DfQuery();
        qry.setDQL("select count(*) from "+qualification);
		IDfCollection myObj1 = (IDfCollection)qry.execute(session,IDfQuery.DF_READ_QUERY);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scanning dql results", null, null);
		while(myObj1.next()) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Begin next result - check its attrs", null, null);
		    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--attrnum: "+i, null, null);
				IDfAttr attr = myObj1.getAttr(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--attrname: "+attr.getName(), null, null);
				if (attr.getDataType() == attr.DM_DOUBLE) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"----double attr found, parsing number of signatures", null, null);
				    numSigned = myObj1.getDouble(attr.getName());
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "MRCS:ESignPrecondition :numSigned: No of times Signed :" +numSigned, null, null);
				}
		    }
		}
		myObj1.close();
		if(numSigned >= noSigns) return false;
		else return true;

    }

    private boolean isQualifiableForSign() throws DfException {

        /*-CONFIG-*/String m = "isQualifiableForSign - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"top", null, null);
        String docId = docObject.getObjectId().getId();

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composing qualification for DQL", null, null);
        String qualification = "dm_audittrail where " +
		"audited_obj_id='"+docId+"' and " +
		"string_1= '"+signature.getUsr()+"' and " +
		"string_2='"+signature.getReason()+"' and " +
		"version_label='"+docObject.getVersionLabel(0)+"'";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  m+"qualification: " +qualification, null, null);

		IDfPersistentObject myObj = session.getObjectByQualification( qualification );

		if( myObj == null ) {
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"dm_audittrail can not be found.", null, null);
            return true;
        } else {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Object named dm_audittrail with object id "+ myObj.getObjectId().toString() + " was found.", null, null);
            return false;
        }
    
    
    }


}
