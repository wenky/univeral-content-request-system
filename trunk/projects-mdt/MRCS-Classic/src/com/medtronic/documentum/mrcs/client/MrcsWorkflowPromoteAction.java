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

 Filename       $RCSfile: MrcsWorkflowPromoteAction.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2007/01/09 20:59:44 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.PromoteAction;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

public class MrcsWorkflowPromoteAction extends PromoteAction {


    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context,
            Component component) {
    	/*-CONFIG-*/String m="queryExecute ~ ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~ ", null, null);
        boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"super flag:  " + flag, null, null);
        try {
            if (flag) {
                String id = argumentlist.get("objectId");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~Doc Id " + id, null, null);
                IDfDocument doc = (IDfDocument) component.getDfSession().getObject(new DfId(id));
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~checking access permissions", null, null);
                int permission = doc.getPermit();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~permission level: "+permission, null, null);
                if (permission < 4)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~permission level must be >= 4 (RELATE)", null, null);
                    return false;
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~DOC " + doc, null, null);
                StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~config " + config, null, null);
                String appName = doc.getString("mrcs_application");
                String currState = doc.getCurrentStateName();
                if (currState == null || "".equals(currState.trim()))
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~CURRSTATE IS EMPTY, RETURNING false", null, null);
                	return false;
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~appName " + appName, null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~currState " + currState, null, null);
                // legacy 4.1.2 impl, or are we on MRCS 4.2+?
                if (config.isLegacyLCWF(appName))
                {
	                StateInfo stInfo = config.getStateInfo(appName, currState);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~stInfo " + stInfo, null, null);
	                String PromotionType = stInfo.getPromotionType();
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~PromotionType " + PromotionType, null, null);
	                if ("WORKFLOW".equalsIgnoreCase(PromotionType))
	                    flag = (!doc.isCheckedOut() && documentNotInWF(doc.getObjectId().getId(), doc.getSession()) && doc.getHasFolder());
	                else
	                    flag = false;
                } else {
                	// MRCS 4.2+ LCs and WFs
                	String mrcslifecycle = config.getMrcsLifecycleFromSystemLifecycleName(appName,doc.getString("mrcs_config"),doc.getString("mrcs_folder_config"),doc.getPolicyName());
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": getting lcstate for mrcslifecycle "+mrcslifecycle+" and state "+currState, null, null);	
                	MrcsLifecycleState lcstate = (MrcsLifecycleState)config.getLifecycleState(appName,mrcslifecycle,currState);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": PromotionType " + lcstate.PromotionType, null, null);	
                    if ("WORKFLOW".equalsIgnoreCase(lcstate.PromotionType))
                        flag = (!doc.isCheckedOut() && documentNotInWF(doc.getObjectId().getId(), doc.getSession()) && doc.getHasFolder());
                    else
                        flag = false;
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Can manually promote " + flag, null, null);
            }
        } catch (Exception exception) {
            /*-ERROR-*/DfLogger.error(this,m+"- Exception :" + exception, null, exception);
            //throw new RuntimeException("WorkflowPromoteAction precondition error",exception);
        }
        return flag;
    }

    private boolean documentNotInWF(String docId, IDfSession session) throws DfException {
        double numWF = 0;
        //String docId = docObject.getObjectId().getId();
        String qualification = "select count(r_workflow_id) from dmi_package " +
        		"where any r_component_id in " +
        		"(select r_object_id from dm_sysobject where i_chronicle_id in " +
        		"(select i_chronicle_id from dm_sysobject where r_object_id = '" +docId +"'))";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "STPromotePreCondition.documentNotInWF: qualification : "+qualification, null, null);

        IDfQuery qry = new DfQuery();
        qry.setDQL(qualification);
		IDfCollection myObj1 = (IDfCollection)qry.execute(session,IDfQuery.DF_READ_QUERY);

		while(myObj1.next()) {
		    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
				IDfAttr attr = myObj1.getAttr(i);
				if (attr.getDataType() == attr.DM_DOUBLE) {
				    numWF = myObj1.getDouble(attr.getName());
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "No of WF involved (attr type double) :" +numWF, null, null);
				}
				if (attr.getDataType() == attr.DM_INTEGER) {
				    numWF = (double)myObj1.getInt(attr.getName());
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "No of WF involved (attr type int):" +numWF, null, null);
				}
				
		    }
		}
		myObj1.close();
		if(numWF > 0) return false;
		else return true;
    }
}