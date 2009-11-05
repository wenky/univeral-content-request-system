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

 Filename       $RCSfile: MrcsScheduledPromoteAction.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2006/09/20 18:54:46 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.PromoteAction;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

public class MrcsScheduledPromoteAction extends PromoteAction {

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context,Component component) 
    {
    	/*-CONFIG-*/String m="queryExecute ~~ ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"~~ MrcsScheduledPromoteAction ~~ ",null,null);
        boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS:Super.QueryExecute:  " + flag,null,null);
        try {
            if (flag) {
                String id = argumentlist.get("objectId");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~Doc Id " + id,null,null);
                IDfDocument doc = (IDfDocument) component.getDfSession().getObject(new DfId(id));
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~ checking access permissions", null, null);
                int permission = doc.getPermit();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~ permission level: "+permission, null, null);
                if (permission < 4)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~ permission level must be >= 4 (RELATE)", null, null);
                    return false;
                }
                
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(m+" ~~DOC " + doc);
                String appName = doc.getString("mrcs_application");
                String currState = doc.getCurrentStateName();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~appName " + appName,null,null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~currState " + currState,null,null);

                StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
                
                // check legacy 4.1.2 lifecycles versus 4.2+ lifecycles
                if (config.isLegacyLCWF(appName)) {
                
	                if ((currState != null) && (currState.length() > 0)) {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : config " + config, null, null);
	                    StateInfo stInfo = config.getStateInfo(appName, currState);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : stInfo " + stInfo, null, null);
	                    String PromotionType = stInfo.getPromotionType();
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : PromotionType " + PromotionType, null, null);
	
	                    if ("SCHEDULED".equalsIgnoreCase(PromotionType))
	                        flag = true;
	                    else
	                        flag = false;
	                } else {
	                    flag = false;
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Document does not have any Lifeccyle attached " , null, null);
	                }
                } else {
                	// MRCS 4.2+
	                if ((currState != null) && (currState.length() > 0)) {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : getting mrcslifecycle", null, null);	
	                	String mrcslifecycle = config.getMrcsLifecycleFromSystemLifecycleName(appName,doc.getString("mrcs_config"),doc.getString("mrcs_folder_config"),doc.getPolicyName());
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : getting lcstate for mrcslifecycle "+mrcslifecycle+" and state "+currState, null, null);	
	                	MrcsLifecycleState lcstate = (MrcsLifecycleState)config.getLifecycleState(appName,mrcslifecycle,currState);
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" : PromotionType " + lcstate.PromotionType, null, null);	
	                    if ("SCHEDULED".equalsIgnoreCase(lcstate.PromotionType))
	                        flag = true;
	                    else
	                        flag = false;
	                } else {
	                	flag = false;
	                }
                	
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": Can manually promote " + flag,null,null);
            }
        } catch (Exception exception) {
            /*-ERROR-*/DfLogger.error(this, "MRCS:MrcsScheduledPromoteAction.queryExecute - Exception :" + exception,null,exception);
            //throw new RuntimeException("MRCS Schedule Promote error",exception);
            return false;
        }
        return flag;
    }
}