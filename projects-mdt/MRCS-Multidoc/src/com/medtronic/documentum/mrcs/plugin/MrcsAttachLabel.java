/*
 * Created on May 24, 2005
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

 Filename       $RCSfile: MrcsAttachLabel.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:24 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsAttachLabel extends MrcsBasePlugin implements IAttachLabelPlugin
{

    public void attachlabel(IDfSessionManager sMgr, String mrcsapp, Map params) throws DfException 
    {
        try{
            IDfDocument docObject = (IDfDocument)params.get("ApplyToObject");
            IDfId _ObjId =docObject.getObjectId();
            String labeltoAttach = (String)params.get("LabelToAttach");
            String ignoreLabel = (String)params.get("LabelToIgnore");
            Boolean makeCurrentB = (Boolean)params.get("MakeCurrent");
            boolean makeCurrent = makeCurrentB.booleanValue();
            
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            String docbase = config.getApplicationDocbase(mrcsapp);
            boolean bSessionManagerCreated = (sMgr == null);
            if (bSessionManagerCreated) {            
                sMgr = createSessionManager(docbase, config.getSystemUsername(mrcsapp), config.getSystemPassword(mrcsapp));            
            }
            IDfSession session = sMgr.getSession(docbase);
            IDfDocument doc = (IDfDocument) session.getObject(_ObjId);
           
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsAttachLabel:attachlabel : labeltoAttach : " + labeltoAttach, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsAttachLabel:attachlabel : Label Summary : " + doc.getVersionPolicy().getVersionSummary(","), null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsAttachLabel:attachlabel : ignoreLabel : " + ignoreLabel, null, null);
    
            String label = "";
            for (int i = 0; i < doc.getVersionLabelCount(); i++) {
                label = doc.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsAttachLabel:attachlabel : Label " + i + " : " + label, null, null);
                if (i > 0) {
                    if (label.equalsIgnoreCase(ignoreLabel))
                        doc.unmark(label);
                }
            }
            doc.mark(labeltoAttach);
            if (makeCurrent)doc.mark("CURRENT");
            doc.save();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MrcsAttachLabel:releaseSysSession : Exception Occurred : ", null, e);
            throw new RuntimeException("error in post promote label attachment");       
        }
        
    }

}
