/*
 * Created on Apr 26, 2005
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

 Filename       $RCSfile: MrcsArgumentExistsPrecondition.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2007/12/12 17:39:32 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.List;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.webcomponent.library.actions.ArgumentExistsPrecondition;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

/**
 * @author prabhu1
 *
 */
public class MrcsArgumentExistsPrecondition extends ArgumentExistsPrecondition {

    /**
     *
     */
    public MrcsArgumentExistsPrecondition() {
        super();
        // TODO Auto-generated constructor stub
    }


    public String[] getRequiredParams() {
        return s_requiredArgs;
    }


    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context,
            Component component) {
        boolean flag = super.queryExecute(s, iconfigelement, argumentlist, context, component);
        try {
            if (flag) {
                String id = argumentlist.get("objectId");
                String wfid = argumentlist.get("startworkflowId");

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : Doc Id " + id, null, null);
                IDfDocument doc = (IDfDocument) component.getDfSession().getObject(new DfId(id));
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : DOC " + doc, null, null);
                String appName =  doc.getString("mrcs_application");
                String currState = doc.getCurrentStateName();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : appName " + appName, null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : Document : currState " + currState, null, null);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : Document : lifecycle " + doc.getPolicyName(), null, null);
                StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();

                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : config " + config, null, null);

                MrcsPreconditionsFactory preCdnConfig = MrcsPreconditionsFactory.getPreConditionsConfig();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : preCdnConfig > " + preCdnConfig, null, null);
                MrcsPlugin preCheckClass = preCdnConfig.getPreconditionPlugin(appName, "StateTransition");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : preCheckClass > " + preCheckClass, null, null);
                MrcsPreConditions preCheck = (MrcsPreConditions) Class.forName(preCheckClass.PluginClassName).newInstance();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : preCheck > " + preCheck, null, null);
                HashMap map = new HashMap();
                map.put("mrcsapp",appName);
                map.put("IDfDocument", doc);
                flag = preCheck.isTaskEffectual(map, preCheckClass.PluginConfiguration);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : Document Already in WF > " + (!flag), null, null);

            }
        } catch (Exception dfe) {
            /*-DEBUG-*/if (DfLogger.isErrorEnabled(this))DfLogger.error(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : EXCEPTION Occurred " + dfe, null, null);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsArgumentExistsPrecondition:queryExecute.queryExecute : **- Allowed to Start WF? -** " + flag, null, null);
        return flag;
    }

    private static final String s_requiredArgs[] = new String[0];
}