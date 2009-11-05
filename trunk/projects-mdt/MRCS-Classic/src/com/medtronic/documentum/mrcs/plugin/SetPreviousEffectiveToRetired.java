/*
 * Created on Oct 12, 2005
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

 Filename       $RCSfile: SetPreviousEffectiveToRetired.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/09/20 19:13:26 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author muellc4
 *
 * This is very similar to ThereCanOnlyBeOne, but the process() function will
 * demote matches rather than do arbitrary state assignment to a specified state.
 * - getDQL() is the same as ThereCanOnlyBeOne...
 * - State needs to be specified so we can look up the state's label in config
 */
public class SetPreviousEffectiveToRetired extends ThereCanOnlyBeOne
{

    public void process(IDfCollection match, IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "SetPreviousEffectiveToRetired.process()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);

        // lookup lifecycle to attach in config, and get LC object from docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"look up lifecycle name to attach from plugin config", null, null);
        String lifecyclename  = (String)configdata.get("Lifecycle");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"- configured lifecycle to apply: "+lifecyclename, null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up lifecycle policy object", null, null);
        IDfSysObject lifecycle = (IDfSysObject) session.getObjectByQualification("dm_sysobject where object_name ='" + lifecyclename + "'");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"-id of retrieved LC: "+lifecycle.getObjectId().getId(), null, null);

        // lookup LC state to assign on attachment
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"look up state to apply from plugin config", null, null);
        String state = (String)configdata.get("State");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"- configured state to attach with: "+state, null, null);

        // extract document id from current match (assumes docid is the only string in the match's attribute colleciton), lookup doc in docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get match's object id", null, null);
        String id = getMatchObjectId(match);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up document", null, null);
        IDfDocument doc = (IDfDocument)session.getObject(new DfId(id));

        if (!sMgr.isTransactionActive())
        {
            sMgr.beginTransaction();
        }
        else
        {
            /*-ERROR-*/DfLogger.error(this, m+"SetPreviousEffectiveToRetired - transaction cannot be active for this plugin, use PostTransactionPromotePlugin layer", null, null);
            throw new RuntimeException("SetPreviousEffectiveToRetired - transaction cannot be active for this plugin, use PostTransactionPromotePlugin layer");
        }

        try {
            // make object mutable, and attach the new lifecycle (policy)
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"begin attach and detach of new lifecycle", null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"make document mutable", null, null);
            // user should be SUPERUSER in order to play with r_immutable_flag
            doc.setString("r_immutable_flag", "FALSE");
            doc.save();
            doc.fetch(doc.getTypeName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"forcing suspending of document", null, null);
            doc.suspend(null,true,false);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"document suspended", null, null);
            doc.fetch(doc.getTypeName());
            //doc.save();

            // apply new LC state's label
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting label for new LC and state", null, null);
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up state info from MRCS config for state "+state, null, null);
            StateInfo newStInfo = config.getStateInfo(mrcsapp, state);
            StateInfo excStInfo = config.getStateInfo(mrcsapp,newStInfo.getExceptionState());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"newStInfo: " + newStInfo, null, null);
            String labeltoAttach = excStInfo.getLabel();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"labeltoAttach: " + labeltoAttach, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scan document's current labels", null, null);
            for (int i = 0; i < doc.getVersionLabelCount(); i++) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--examining label #"+i, null, null);
                String label = doc.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--cur label: "+ label, null, null);
                if (i == 0)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"0th label, appending new state's label...", null, null);
                    labeltoAttach = label+"_"+labeltoAttach;
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"new label: "+labeltoAttach, null, null);
                }
            }

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attaching label: "+labeltoAttach, null, null);
            doc.mark(labeltoAttach);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting document back to immutable", null, null);
            // user should be SUPERUSER in order to play with r_immutable_flag
            doc.setString("r_immutable_flag", "TRUE");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"fetching postversioned doc", null, null);
            PostVersionedDoc.fetch(null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"remarking postversioned to current", null, null);
            PostVersionedDoc.mark("CURRENT");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving postversioned", null, null);
            PostVersionedDoc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"committing transaction", null, null);
            sMgr.commitTransaction();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done!", null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"exception in suspending of previous version, aborting transaction", null, e);
            sMgr.abortTransaction();
            /*-ERROR-*/DfLogger.error(this, m+"transaction aborted, rethrowing", null, null);
            throw new RuntimeException("SetPreviousEffectiveToRetired - error in previous version suspend transaction",e);
        }
    }


}
