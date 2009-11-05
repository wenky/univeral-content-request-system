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

 Filename       $RCSfile: MrcsVersionChange.java,v $
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
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsVersionChange extends MrcsBasePlugin implements IVersionChangePlugin {    

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.IVersionChangePlugin#performVersionChange(java.util.Map)
     */
    public IDfId performVersionChange(IDfSessionManager sMgr, String mrcsapp, Map params) throws DfException 
    {
        IDfId _newObjId = null;
        boolean bNewSessionManager = (sMgr == null);
        IDfSession session = null;
        try {
            // get config
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            String docbase = config.getApplicationDocbase(mrcsapp); 
            if (bNewSessionManager)
            {
                sMgr = createSessionManager(docbase,config.getSystemUsername(mrcsapp),config.getSystemPassword(mrcsapp));
                // MrcsPlugin convention: null sMgr implies that we must manage our own session manager (for transactions)
            }
            session = sMgr.getSession(docbase);
            
            StateInfo stInfo = (StateInfo) params.get("StateInfo");
            IDfDocument docObject = (IDfDocument)params.get("ApplyToObject");
            IDfId _ObjId =docObject.getObjectId();            
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Check for the last State", null, null);
            //if promotion is successful then change the version [VersionToNext]
            String nextLabel = null;
            /*
             * if (nextstInfo.getNextState().equals("")) { nextLabel =
             * nextstInfo.getVersionToNext(); } else { nextLabel = stInfo.getVersionToNext(); }
             */
            IDfDocument doc = (IDfDocument) session.getObject(_ObjId);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Found Current Document!!!" + doc, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Doc ACL  " + doc.getACLName(), null, null);
            
            nextLabel = stInfo.getVersionToNext();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : If promotion is successful then [VersionToNext]> nextVersion " + nextLabel, null, null);
            String verLabel = null;
            IDfVersionPolicy verPolicy = doc.getVersionPolicy();
            if (nextLabel.equalsIgnoreCase("MAJOR")) {
                verLabel = verPolicy.getNextMajorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Next MAJOR verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                //lblAttach.attachlabel(lblParams);
                _newObjId = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("MINOR")) {
                verLabel = verPolicy.getNextMinorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Next MINOR verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                //lblAttach.attachlabel(lblParams);
                _newObjId = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("BRANCH")) {
                verLabel = verPolicy.getBranchLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Next BRANCH verLabel  " + verLabel, null, null);
                if (!doc.isCheckedOut())
                    doc.checkout();
                //lblAttach.attachlabel(lblParams);
                _newObjId = doc.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("SAME")) {
                verLabel = verPolicy.getSameLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsVersionChange:performVersionChange : Next SAME verLabel  " + verLabel, null, null);
                //lblAttach.attachlabel(params);
                doc.save();
                _newObjId = doc.getObjectId();
            }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MrcsVersionChange:performVersionChange : Error encountered  - labelVersionChange ; ", null, e);
            throw new RuntimeException("Error performing document version change during promote/demote");
        }
        return _newObjId;
    }

}
