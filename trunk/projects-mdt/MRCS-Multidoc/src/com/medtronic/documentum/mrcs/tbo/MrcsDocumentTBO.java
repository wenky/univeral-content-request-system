/*
 * Created on Jan 31, 2005
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

 Filename       $RCSfile: MrcsDocumentTBO.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:26 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.tbo;


import java.util.Date;
import java.util.HashMap;
import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.DfDocument;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfRelationType;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.common.MrcsPreConditions;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.plugin.ICopyToLocationPlugin;
import com.medtronic.documentum.mrcs.plugin.IStateTransitionPlugin;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * The MRCSDocument TBO
 */
public class MrcsDocumentTBO extends DfDocument implements IMrcsDocumentTBO {

    private String vendorString = "Medtronics";

    private static final String version = "4.0";

    private IDfClientX clientx = new DfClientX();

    private IDfClient client = null;

    private String applicationName = null;

    private String appDocType = null;

    private DfTime docEffectivityDate = null;

    private IDfRelation docRelation = null;

    private IDfRelationType relationType = null;

    private StateInfo stInfo = null;

    private StateInfo nextstInfo = null;

    private StateInfo prevstInfo = null;

    private boolean isCopy = false;

    public static final String MRCS_APPLICATION_PROPERTY = "mrcs_application";

    private IDfSessionManager _sMgr = null;

    private IDfSessionManager _OrigDocSMgr = null;

    private IDfSession _OrigDocSession = null;

    private IDfSession _session = null;

    private String _docBase = null;

    private IDfId _newObjId = null;

    public MrcsDocumentTBO() {
        super();
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("********MrcsDocumentTBO**********");
    }


    /*
     * (non-Javadoc)
     *
     * @see com.documentum.fc.client.IDfBusinessObject#getVersion()
     */
    public String getVersion() {
        return version;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.documentum.fc.client.IDfBusinessObject#getVendorString()
     */
    public String getVendorString() {
        return vendorString;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.documentum.fc.client.IDfBusinessObject#isCompatible(java.lang.String)
     */
    public boolean isCompatible(String arg0) {
        int i = arg0.compareTo(getVersion());
        if (i <= 0)
            return true;
        else
            return false;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.documentum.fc.client.IDfBusinessObject#supportsFeature(java.lang.String)
     */
    public boolean supportsFeature(String f) {
        String strFeatures = "promote";

        if (strFeatures.indexOf(f) == -1)
            return false;

        return true;
    }


    public void save() throws DfException {

        Date now = new Date();
        DfLogger.debug("tracing", now + " save() called", null, null);

        //  promote

        //  demote

        super.save();
    }


    public void saveLock() throws DfException {

        Date now = new Date();
        DfLogger.debug(this, now + " saveLock() called", null, null);

        //  promote
        //  demote

        super.saveLock();

    }


    public IDfId checkinEx(boolean b, String s, String s1, String s2, String s3, String s4) throws DfException {

        Date now = new Date();
        DfLogger.debug(this, now + " checkinEx() called", null, null);

        //  set the relation object on checkin
        //if (isCopy())
        return super.checkinEx(b, s, s1, s2, s3, s4);
    }


    public boolean canPromote() throws DfException {

        //MrcsPluginDTO preCheckClass = null;
        //MrcsPreConditions preCheck = null;
        //initStateInfo();
        boolean canPromote = super.canPromote();
        //if (canPromote) {
        //    try {
        //        //Need to eliminate the usage of this try catch block by
        //        //better exception handling mechanizsm at Config broker
        //        MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
        //        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" > onCommitChanges config > " + config);
        //        preCheckClass = config.getPreconditionPlugin(getAppName(), "StateTransition");
        //        preCheck = (MrcsPreConditions) Class.forName(preCheckClass.ClassName).newInstance();
        //    } catch (Exception e) {
        //        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Exception occurred while trying obtain Workflow configuartions" + e);
        //    }
//
        //    HashMap map = new HashMap();
        //    map.put("IDfSession", getSession());
        //    map.put("IDfDocument", this);
        //    map.put("StateInfo", getCurrentStateInfo());
        //    canPromote = preCheck.isTaskEffectual(map,preCheckClass.Config);
        //}
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("canPromote  :" + canPromote);
        return canPromote;
    }


    public boolean canDemote() throws DfException {
        MrcsPlugin preCheckClass = null;
        MrcsPreConditions preCheck = null;
        initStateInfo();
        boolean canDemote = super.canDemote();
        if (canDemote) {
            try {
                //Need to eliminate the usage of this try catch block by
                //better exception handling mechanizsm at Config broker
                MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" > onCommitChanges config > " + config);
                preCheckClass = config.getPreconditionPlugin(getAppName(), "StateTransition");
                preCheck = (MrcsPreConditions) Class.forName(preCheckClass.PluginClassName).newInstance();
            } catch (Exception e) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Exception occurred while trying obtain Workflow configuartions" + e);
            }

            HashMap map = new HashMap();
            map.put("IDfSession", getSession());
            map.put("IDfDocument", this);
            map.put("StateInfo", getCurrentStateInfo());
            canDemote = preCheck.isTaskEffectual(map,preCheckClass.PluginConfiguration);
        }
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("canDemote  :" + canDemote);
        return canDemote;
    }


    private void initStateInfo() {
        try {
            //Need to eliminate the usage of this try catch block by
            //better exception handling mechanizsm at Config broker
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            ///*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" > config > " + config);
            stInfo = config.getStateInfo(getAppName(), getCurrentStateName());
            nextstInfo = config.getStateInfo(getAppName(), stInfo.getNextState());
            prevstInfo = config.getStateInfo(getAppName(), stInfo.getDemoteState());
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" > stInfo > " + stInfo);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" > nextstInfo > " + nextstInfo);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" > prevstInfo > " + prevstInfo);
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Exception @ initStateInfo" + e);
        }
    }


    private StateInfo getCurrentStateInfo() {
        return stInfo;
    }


    private StateInfo getNextStateInfo() {
        return nextstInfo;
    }


    private void demoteLabel() throws DfException {
        //On successful Demote, attach Lifecycle State label
        String labeltoAttach = prevstInfo.getLabel();
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("  attachlabel() ~~~~~~labeltoAttach~~~~~~~      " + labeltoAttach);
        if (getVersionLabelCount() > 2)
            unmark(getVersionLabel(2));
        mark(labeltoAttach);
        save();
    }


    private void attachlabel(String action) throws DfException {

        String labeltoAttach = "";
        String ignoreLabel = "";
        boolean makeCurrent = false;
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" Action " + action);
        IDfDocument doc = (IDfDocument) _session.getObject(getObjectId());

        if (action.equalsIgnoreCase("PROMOTE")) {
            //On successful promotion, attach Lifecycle State label
            makeCurrent = nextstInfo.getCurrentLabel();
            labeltoAttach = nextstInfo.getLabel();
            //if(prevstInfo != null) ignoreLabel = prevstInfo.getLabel();
            if (stInfo != null)ignoreLabel = stInfo.getLabel();
        } else if (action.equalsIgnoreCase("DEMOTE")) {
            //On successful Demote, attach Lifecycle State label
            makeCurrent = prevstInfo.getCurrentLabel();
            labeltoAttach = prevstInfo.getLabel();
            ignoreLabel = stInfo.getLabel();
        }else if (action.equalsIgnoreCase("SUSPEND")) {
            //On successful promotion, attach Lifecycle State label
            makeCurrent = stInfo.getCurrentLabel();
            labeltoAttach = stInfo.getExceptionState();
            //if(prevstInfo != null) ignoreLabel = prevstInfo.getLabel();
            if (stInfo != null)ignoreLabel = stInfo.getLabel();
        }

        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("  attachlabel()~~~~~~labeltoAttach~~~" + labeltoAttach);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("  attachlabel()~~~~~~ignoreLabel~~~~" + ignoreLabel);

        String label = "";
        for (int i = 0; i < doc.getVersionLabelCount(); i++) {
            label = doc.getVersionLabel(i);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Label " + i + " : " + label);
            if (i > 0) {
                if (label.equalsIgnoreCase(ignoreLabel))
                    doc.unmark(label);
            }
        }
        doc.mark(labeltoAttach);
        if (makeCurrent)
            doc.mark("CURRENT");
    }


    private void labelVersionChange() throws DfException {
        try {
            setSysSession();
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Check for the last State");
            //if promotion is successful then change the version [VersionToNext]
            String nextLabel = null;
            /*
             * if (nextstInfo.getNextState().equals("")) { nextLabel =
             * nextstInfo.getVersionToNext(); } else { nextLabel = stInfo.getVersionToNext(); }
             */
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - top");
            IDfId curid = getObjectId();
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - current id: "+curid.getId());
            IDfDocument doc = (IDfDocument) _session.getObject(curid);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - looked up docid: "+doc.getObjectId().getId());

            //nextLabel = nextstInfo.getVersionToNext();
            nextLabel = stInfo.getVersionToNext();
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - looking up getVersionToNext: "+nextLabel);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("If promotion is successful then [VersionToNext]> nextVersion " + nextLabel);
            String verLabel = null;
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - looking up version policy");
            IDfVersionPolicy verPolicy = doc.getVersionPolicy();
            if (nextLabel.equalsIgnoreCase("MAJOR")) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - applying MAJOR version");
                verLabel = verPolicy.getNextMajorLabel();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Next MAJOR verLabel  " + verLabel);
                if (!doc.isCheckedOut())
                    doc.checkout();
                attachlabel("PROMOTE");
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - MAJOR checking in doc");
                _newObjId = doc.checkin(false, verLabel);
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - MAJOR new doc id: "+_newObjId.getId());
            } else if (nextLabel.equalsIgnoreCase("MINOR")) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - applying MINOR version");
                verLabel = verPolicy.getNextMinorLabel();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Next MINOR verLabel  " + verLabel);
                if (!doc.isCheckedOut())
                    doc.checkout();
                attachlabel("PROMOTE");
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - MINOR checking in doc");
                _newObjId = doc.checkin(false, verLabel);
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - MINOR new doc id: "+_newObjId.getId());
            } else if (nextLabel.equalsIgnoreCase("BRANCH")) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - applying BRANCH version");
                verLabel = verPolicy.getBranchLabel();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Next BRANCH verLabel  " + verLabel);
                if (!doc.isCheckedOut())
                    doc.checkout();
                attachlabel("PROMOTE");
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - BRANCH checking in doc");
                _newObjId = doc.checkin(false, verLabel);
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - BRANCH new doc id: "+_newObjId.getId());
            } else if (nextLabel.equalsIgnoreCase("SAME")) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - SAME version");
                verLabel = verPolicy.getSameLabel();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Next SAME verLabel  " + verLabel);
                attachlabel("PROMOTE");
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - SAME: saving doc");
                doc.save();
                _newObjId = doc.getObjectId();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - SAME: doc checked out? "+doc.isCheckedOut());
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.labelVersionChange - SAME: new doc id: "+_newObjId.getId());
            }
        } catch (DfException e) {
        } finally {
            releaseSysSession();
        }

    }


    private void copyRelate() throws DfException {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("if promotion is successful then create the Link [ReferToOriginal]");
        //if promotion is successful then create the Link [ReferToOriginal]
        if (nextstInfo.getReferToOriginal()) {
            //create a copy of the document and link to original
            createCopy(_newObjId);
        }
    }


    private void applyDocACL(String docACL) throws DfException {
        try {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("applyDocACL docACL " + docACL);
            setSysSession();
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("applyDocACL _session " + _session);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("applyDocACL DFSession " + getSession());
            //IDfACL newACL = _session.getACL(_session.getLoginUserName(), stInfo.getDocACL());
            StateTransitionConfigFactory config = null;
            config = StateTransitionConfigFactory.getSTConfig();

            String systemdomain = _session.getServerConfig().getString("operator_name");
            IDfACL newACL = _session.getACL(systemdomain, docACL);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("newACL " + newACL);
            IDfDocument doc = (IDfDocument) _session.getObject(_newObjId);
            doc.setACL(newACL);
            doc.save();
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("applyDocACL doc checked out? " + doc.isCheckedOut());
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.applyDocACL - Error encountered while applying ACL ;" + e);
        } finally {
            releaseSysSession();
        }

    }

    private void createRendition() throws DfException{
            IDfDocument doc = (IDfDocument) getSession().getObject(_newObjId);
            doc.queue("dm_autorender_win31", "rendition", 0, false, null, "rendition_req_ps_pdf");
    }

    private void YpostPromote() throws DfException {

        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" ~~~~~~~~~~~~~~~~~~~~~~~~ postPromote begins ~~~~~~~~~~~~~~~~~~~~~~~~");
        labelVersionChange(); // <-- this also initializes _newObjId
        //copyRelate(); <-- plugin does this now...
        applyDocACL(nextstInfo.getDocACL());
        createRendition();

        // CEM - post-promote plugin layer
        List plugins = stInfo.getPostPromotePlugins();
        IDfDocument newdoc = (IDfDocument)_session.getObject(_newObjId);
        if (plugins != null)
        {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - plugins defined, executing");
            try {
                HashMap scratchpad = new HashMap();
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - executing plugin list");
                for (int i=0; i < plugins.size(); i++)
                {
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - executing plugin #"+i);
                    MrcsPlugin curplugin = (MrcsPlugin)plugins.get(i);
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - getting Class for plugin: "+curplugin.PluginClassName);
                    Class pluginclass = Class.forName(curplugin.PluginClassName);
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - instantiating");
                    IStateTransitionPlugin plugin = (IStateTransitionPlugin)pluginclass.newInstance();
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - executing current plugin");
                    plugin.execute(_sMgr,_docBase,stInfo,getAppName(),this,newdoc,curplugin.PluginConfiguration, scratchpad);
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote - current plugin executed");
                }
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.postPromote() - Error in postpromote plugin execution",e);
                //throw new WrapperRuntimeException("Error in postpromote plugin",e);
            }
        }
        // CEM - end post-promote plugin layer

        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" ~~~~~~~~~~~~~~~~~~~~~~~~ postPromote ends ~~~~~~~~~~~~~~~~~~~~~~~~");
    }


    private IDfSession getOriginalDocSession() {
        return _OrigDocSession;
    }


    private IDfSessionManager getOriginalDocSMgr() {
        return _OrigDocSMgr;
    }


    private void saveOriginalDocSession() throws DfException {

        _OrigDocSMgr = getSessionManager();
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("_OrigDocSMgr : " + _OrigDocSMgr);

        // _OrigDocSMgr = SessionManagerHttpBinding.getSessionManager();
        _OrigDocSession = getSession();

        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("--------------ORIGINAL~~~~~~~~~~~~~~~~~~~~~~~~~~");
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("_OrigDocSMgr : " + _OrigDocSMgr);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("_OrigDocSession : " + _OrigDocSession);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("idfsession : " + getSession());
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("User : " + getSession().getLoginInfo().getUser());
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Pswd : " + getSession().getLoginInfo().getPassword());
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Domain : " + getSession().getLoginInfo().getDomain());
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("--------------~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~~");

        //_OrigDocSMgr.release(_OrigDocSession);
    }


    //Get the Session
    private void setSysSession() throws DfException {
        saveOriginalDocSession();

        //if (_session == null) {

        //create Client object
        IDfClient client = new DfClient();
        //create a Session Manager object and replace the original

        _sMgr = client.newSessionManager();
        // see the code for Create Session Manager
        StateTransitionConfigFactory config = null;
        try {
            config = StateTransitionConfigFactory.getSTConfig();
        } catch (Exception e) {
            /*-ERROR-DfLogger.getRootLogger().error("MRCS:CopyToLocationPlugin.getSession - Error encountered while getting System user session",e);*/
            System.out
                    .println("MRCS:CopyToLocationPlugin.getSession - Error encountered while getting System user session"
                            + e);
        }

        //create an IDfLoginInfo object named loginInfoObj
        IDfLoginInfo loginInfoObj = new DfLoginInfo();
        loginInfoObj.setUser(config.getSystemUsername(getAppName()));
        loginInfoObj.setPassword(config.getSystemPassword(getAppName()));
        loginInfoObj.setDomain(null);

        _docBase = config.getApplicationDocbase(getAppName());

        //bind the Session Manager to the login info
        _sMgr.setIdentity(_docBase, loginInfoObj);
        //use the following to use the same loginInfObj for all Docbases (DFC 5.2 or later)
        // _sMgr.setIdentity( * , loginInfoObj );

        // setSessionManager(_sMgr);
        _session = _sMgr.getSession(_docBase);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("--------------NEWSESSION~~~~~~~~~~~~~~~~~~~~~~~~~~");
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("_sMgr : " + _sMgr);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("session : " + _session);
        //}

        // return _session;
    }


    private void releaseSysSession() throws DfException {
        try {
            _sMgr.release(_session);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" ~~ ReleaseSysSession ~~  " + getSession());
            //setSessionManager(_OrigDocSMgr);
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Error@ReleaseSysSession   " + e);
        }

    }


//    public void promote(String s1, boolean flag, boolean flag1) throws DfException {
//        try {
//            //**Initialize the State Information from Configuration**//
//            initStateInfo();
//            String PromotionType = stInfo.getPromotionType();
//            if (!PromotionType.equalsIgnoreCase("SCHEDULE")){
//                // setSysSession();
//                super.promote(stInfo.getNextState(), false, flag1);
//                postPromote();
//            }
//            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(">>>>>>DOC Properties  PROMOTE ENDS >>>>>> CURRENTSTATE " + getCurrentStateName());
//
//        } catch (Exception e) {
//            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Promote Error " + e);
//        } finally {
//            //releaseSysSession();
//        }
//    }


    public void demote(String s1, boolean flag) throws DfException {
        try {
            //**Initialize the State Information from Configuration**//
            initStateInfo();
            //super.demote(stInfo.getDemoteState(), true); //demote to base true
            super.demote(stInfo.getDemoteState(), false);
            //Attach the label
            attachlabel("DEMOTE");
            //TO DO: Perform version change
            _newObjId = getObjectId();
            //Apply Document ACL
            applyDocACL(prevstInfo.getDocACL());
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(">>>>>>DOC Properties After Demote>>>>>>CurrentState " + getCurrentStateName());

            // CEM - post-demote plugin layer
            List plugins = stInfo.getPostDemotePlugins();
            if (plugins != null)
            {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - plugins defined, executing");
                try {
                    HashMap scratchpad = new HashMap();
                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - executing plugin list");
                    for (int i=0; i < plugins.size(); i++)
                    {
                        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - executing plugin #"+i);
                        MrcsPlugin curplugin = (MrcsPlugin)plugins.get(i);
                        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - getting Class for plugin: "+curplugin.PluginClassName);
                        Class pluginclass = Class.forName(curplugin.PluginClassName);
                        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - instantiating");
                        IStateTransitionPlugin plugin = (IStateTransitionPlugin)pluginclass.newInstance();
                        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - executing current plugin");
                        // TODO: provide reference to post-demote versioned document, I think this only provides the pre-demote reference
                        plugin.execute(_sMgr,_docBase,stInfo,getAppName(),this,this,curplugin.PluginConfiguration, scratchpad);
                        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote - current plugin executed");
                    }
                } catch (Exception e) {
                    /*-ERROR-*/DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.demote() - Error in post-demote plugin execution",e);
                    throw new DfException();
                }
            }
            // CEM - end post-demote plugin layer

        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Demote Error " + e);
        }
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.tbo.IMrcsDocumentTBO#getAppName()
     */
    public String getAppName() throws DfException {
        //return the mrcs application for this object
        applicationName = getString(MRCS_APPLICATION_PROPERTY);
        return applicationName;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.tbo.IMrcsDocumentTBO#setAppName(java.lang.String)
     */
    public void setAppName(String appName) {
        // TODO Auto-generated method stub
        applicationName = appName;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.documentum.fc.client.IDfPersistentObject#validateAllRules(int)
     */
    public void validateAllRules(int arg0) throws DfException {
        // TODO Auto-generated method stub
        super.validateAllRules(arg0);

    }


    private void setRelationType(String relation_name) {
        try {
            if (relationType == null) {
                relationType = (IDfRelationType) getSession().newObject("dm_relation_type");
                relationType.setRelationName(relation_name);
                relationType.setSecurityType("PARENT");
                relationType.setChildType(getTypeName());
                relationType.setParentType(getTypeName());
                try {
                    relationType.save();
                } catch (DfException excep) {
                    if (excep.getErrorCode() == 100)
                        relationType.revert();
                }
            }
        } catch (DfException e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" Exception Occured at setRelationType " + e);
        }
    }


    private IDfRelationType getRelationType() {
        return relationType;
    }

    //	createRelation
    public void setRelation(String relation_name, IDfId child_id, IDfId parent_id, String description)
            throws DfException {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("setRelation _session " + _session);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("setRelation DFSession " + getSession());

        setRelationType(relation_name);
        if (docRelation == null) {
            IDfDocument parentDoc = (IDfDocument) getSession().getObject(parent_id);
            //docRelation = (IDfRelation) getSession().newObject("dm_relation");
            docRelation = parentDoc.addChildRelative(getRelationType().getRelationName(), child_id, null, false,
                    description); //<--
        }
        //docRelation.setRelationName(relation_name);
        //docRelation.setChildId(new DfId(child_id));
        //docRelation.setParentId(new DfId(parent_id));
        //docRelation.setChildLabel("CURRENT");
        //docRelation.setPermanentLink(false);
        //docRelation.setDescription(description);
        docRelation.save();
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("created relation: " + docRelation.getId("r_object_id").getId());
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.tbo.IMrcsDocumentTBO#getRelation()
     */
    public IDfRelation getRelation() {
        return docRelation;
    }


    private  IDfId getChildId(String docId) throws DfException {
        IDfId childId = null;
        try{
        String qualification = "select r_object_id from dm_sysobject where r_object_id in " +
        		"(select child_id from dm_relation where parent_id in " +
        		"(select r_object_id from dm_sysobject (ALL) where i_chronicle_id in " +
        		"(select i_chronicle_id from dm_sysobject where r_object_id = '" +docId+"')) )";

        IDfQuery qry = new DfQuery();
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("getChildId: qualification : "+qualification);
        qry.setDQL(qualification);

		IDfCollection myObj1 = (IDfCollection)qry.execute(getSession(),IDfQuery.DF_READ_QUERY);
		/*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("getChildId: myObj1 : "+myObj1);
			while(myObj1.next()) {
			    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
					IDfAttr attr = myObj1.getAttr(i);
					if (attr.getDataType() == attr.DM_STRING) {
	                    String id = myObj1.getString(attr.getName());
	                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("---getChildId Copy id : --->>" + id + "<<");
	                    childId = new DfId(id);
	                    /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("---getChildId childId --->>" + childId );
	                    //if(id != null) break;
					}
			    }
			}
			myObj1.close();

        }catch(Exception e){
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("** -- ** Exception @ GetChildId " + e);
        }
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("---getChildId Before return  childId --->>" + childId );
        return childId;
    }


    public IDfDocument createCopy(IDfId parentId) throws DfException {
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("-----createCopy----" + parentId);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("NEW session : " + _session);
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("IDfSession : " + getSession());

        IDfDocument document = null;
        IDfFolder destFolder = null;
        IDfId childId = null;
        boolean isNew = false;
        IDfId origId = null;

        try {
            // Set the Session
            setSysSession();

            IDfDocument originalDoc = (IDfDocument) _session.getObject(getObjectId());
            origId = originalDoc.getObjectId();

            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" origId : "+origId.getId());
            childId = getChildId(origId.getId());
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" newChildId : "+childId);
            if(childId != null)/*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" newChildId : "+childId.getId());
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug(" /****************************newChildId************************/ "+childId);

            IDfDocument parentDoc = (IDfDocument) _session.getObject(parentId);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Create Copy childId : >>" + childId + "<<");

            // Create a dm_document object and set properties
            if (childId != null) {
                document = (IDfDocument) _session.getObject(childId);
                isNew = false;
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Copy document Checked out>>" + document.isCheckedOut());
                document.checkout();
            } else {
                isNew = true;
                //document = (IDfDocument) _session.newObject("dm_document");
                document = (IDfDocument) _session.newObject(getType().getName());
            }

            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Create Copy isNew ? " + isNew);

            document.setObjectName(getObjectName());
            document.setContentType(getContentType());

            //setFileEx parameters (fileName,formatName,pageNumber,otherFile)
            String filePath = parentDoc.getFile(""); //getPath(0);
            String formatName = parentDoc.getFormat().getName();

            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("-----createCopy----filePath " + filePath);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("-----createCopy----formatName " + formatName);

            document.setFileEx(filePath, formatName, 0, null);

            // Specify the folder in which to create the document
            String pfolderPath = "";
            String strDestFolderPath = nextstInfo.getCopyToLocation();

            try {
                ICopyToLocationPlugin copyPlugin = (ICopyToLocationPlugin) Class.forName(nextstInfo.getCopyToPlugin())
                        .newInstance();
                //destFolder = copyPlugin.getCopyToFolder(_session, getAppName(), parentDoc,
                // strDestFolderPath);
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("copyPlugin " + copyPlugin);
                destFolder = copyPlugin.getCopyToFolder(getSession(), getAppName(), parentDoc, strDestFolderPath);
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("destFolder " + destFolder);
            } catch (Exception e) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Exception occurred while retreiving the CopytoPlugin Class : " + e);
            }

            if (destFolder == null) {
                /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Destination folder or cabinet " + strDestFolderPath
                        + " does not exist in the Docbase!");
                return null;
            }

            IDfVersionPolicy verPolicy = parentDoc.getVersionPolicy();
            String label = verPolicy.getSameLabel();
            //String label = parentDoc.getVersionLabel(0);
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Version Label :verPolicy Summary  " + verPolicy.getVersionSummary(">"));
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("-----createCopy----label " + label);

            //set ACL
            String systemdomain = _session.getServerConfig().getString("operator_name");
            IDfACL copyDocACL = _session.getACL(systemdomain, nextstInfo.getCopyDocACL());
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("copyDocACL " + copyDocACL);
            document.setACL(copyDocACL);

            // Save the document in the Docbase
            if (isNew) {
                document.link(destFolder.getObjectId().toString());
                document.mark(label);
                document.mark(nextstInfo.getCopyDocLabel());
                document.mark("CURRENT");
                document.save();
                childId = document.getObjectId();

            } else {
                document.mark(nextstInfo.getCopyDocLabel());
                document.mark("CURRENT");
                childId = document.checkin(false, label);
            }
        } catch (Exception e1) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("Exception e1" + e1);
        } finally {
            releaseSysSession();
        }

        //Create the relation
        try {
            setRelation("ReferToOriginalRelationShip", childId, parentId, "description"); //<--
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("setRelation(ReferToOriginalRelationShip,....) " + e);
        }
        return document;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.tbo.IMrcsDocumentTBO#isCopy()
     */
    public boolean isCopy() {
        return isCopy;
    }


    /*
     * (non-Javadoc)
     *
     * @see com.medtronic.documentum.mrcs.tbo.IMrcsDocumentTBO#setCopy(boolean)
     */
    public void setCopy(boolean isCpy) {
        isCopy = isCpy;
    }

    public void suspend(java.lang.String state, boolean override, boolean fTestOnly) throws DfException{
        try {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("~~~~~suspend~~~~~~~~ ");
            //**Initialize the State Information from Configuration**//
            initStateInfo();
            super.suspend(state,override, fTestOnly );
            setSysSession();
            //set ACL
            String systemdomain = _session.getServerConfig().getString("operator_name");
            IDfACL rejectDocACL = _session.getACL(systemdomain, stInfo.getRejectedDocACL());
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("rejectDocACL " + rejectDocACL);
            IDfDocument doc = (IDfDocument) _session.getObject(getObjectId());
            doc.setACL(rejectDocACL);
            // Set the Label for the docObject
            attachlabel("SUSPEND");
            doc.save();
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO Version Summary of Rejected Document : "+doc.getVersionPolicy().getVersionSummary(">"));
        } catch (Exception e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS:MrcsDocumentTBO.suspend - Error encountered while perfoming Lifecycle suspend ;" + e);
        } finally {
            releaseSysSession();
        }

    }

}