package com.medtronic.documentum.mrcs.server;

import java.util.HashMap;
import java.util.List;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfModule;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.documentum.mrcs.config.GenericConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsPromotionService;

public class MrcsPromotionServiceModule implements IDfModule, IMrcsPromotionService 
{
	public IDfSessionManager getSystemSession(GenericConfigFactory config, String mrcsapp)
	{
		/*-CONFIG-*/String m="getSystemSession";
		try { 
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"creating new system-user session manager" , null, null);
		    IDfClientX clientx = new DfClientX();
		    IDfClient client = clientx.getLocalClient();
		    IDfSessionManager sMgr = client.newSessionManager();
		    IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		    loginInfoObj.setUser(config.getSystemUsername(mrcsapp));
		    loginInfoObj.setPassword(config.getSystemPassword(mrcsapp));
		    loginInfoObj.setDomain(null);
		    sMgr.setIdentity(config.getApplicationDocbase(mrcsapp), loginInfoObj);
		    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"new system-user session manager created" , null, null);
		    return sMgr;
		} catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Error encountered getting system session for MRCS ; ", null, e);
            throw new RuntimeException("Could not switch to MRCS system user session for mrcsapp "+mrcsapp+", check configuration");
        }
	}
		
	
    public IDfId performVersionChange(MrcsLifecycleState targetstate, IDfDocument docObject) throws DfException 
    {
    	/*-CONFIG-*/String m="performVersionChange";
        IDfId _newObjId = null;
        try {
            
            IDfId _ObjId = docObject.getObjectId();            
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Check for the last State", null, null);
            //if promotion is successful then change the version [VersionToNext]
            String nextLabel = null;
            
            nextLabel = targetstate.VersionType;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"If promotion is successful then [VersionToNext]> nextVersion " + nextLabel, null, null);
            String verLabel = null;
            IDfVersionPolicy verPolicy = docObject.getVersionPolicy();
            if (nextLabel.equalsIgnoreCase("MAJOR")) {
                verLabel = verPolicy.getNextMajorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Next MAJOR verLabel  " + verLabel, null, null);
                if (!docObject.isCheckedOut())
                    docObject.checkout();
                docObject.mark("CURRENT");
                _newObjId = docObject.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("MINOR")) {
                verLabel = verPolicy.getNextMinorLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Next MINOR verLabel  " + verLabel, null, null);
                if (!docObject.isCheckedOut())
                    docObject.checkout();
                docObject.mark("CURRENT");
                _newObjId = docObject.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("BRANCH")) {
                verLabel = verPolicy.getBranchLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Next BRANCH verLabel  " + verLabel, null, null);
                if (!docObject.isCheckedOut())
                    docObject.checkout();
                docObject.mark("CURRENT");
                _newObjId = docObject.checkin(false, verLabel);
            } else if (nextLabel.equalsIgnoreCase("SAME")) {
                verLabel = verPolicy.getSameLabel();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Next SAME verLabel  " + verLabel, null, null);
                docObject.mark("CURRENT");
                docObject.save();
                _newObjId = docObject.getObjectId();
            }
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Error encountered  - labelVersionChange ; ", null, e);
            throw new RuntimeException("Error performing document versioning during promote/demote");
        }
        return _newObjId;
    }
	
	
	// server and webtop accessible utility service for promoting a document
	public void promoteMrcsDocument(IDfDocument mrcsdocument)
	{
		/*-CONFIG-*/String m="PromoteMrcsDocument-";
		
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP" , null, null);
		
		
		// step 0: get session, config properties
		StateTransitionConfigFactory config = null;
		String mrcsapp = null;
		MrcsLifecycleState targetstate;
		MrcsLifecycleState currentstate;
		IDfSession session;
		try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting session from document" , null, null);
			session = mrcsdocument.getSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"session user: "+session.getLoginUserName() , null, null);
	        mrcsapp = mrcsdocument.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"mrcs application attribute value of document: "+mrcsapp , null, null);
	        String currentstatename = mrcsdocument.getCurrentStateName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current state: "+currentstatename , null, null);
	        
			// step 1: access configuration
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting STConfig" , null, null);
	        config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting MRCS Lifecycle info (MRCS4.2)" , null, null);
	        String mrcslifecycle = config.getMrcsLifecycleFromSystemLifecycleName(mrcsapp,mrcsdocument.getString("mrcs_config"),mrcsdocument.getString("mrcs_folder_config"), mrcsdocument.getPolicyName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting state config for current state "+currentstatename , null, null);
	        currentstate = config.getLifecycleState(mrcsapp, mrcslifecycle, currentstatename);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting state config for target state "+mrcsdocument.getNextStateName() , null, null);
	        targetstate = config.getLifecycleState(mrcsapp, mrcslifecycle, mrcsdocument.getNextStateName());
		} catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+"exception/error in config retrieval before promote attempt",null,e);
            throw new RuntimeException("PromoteService failed on getting document configuration details",e);
		}
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"begin promote" , null, null);
        
        try {
    		// step 2: create snapshot for rollback recovery
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"snapshotting document for rollback" , null, null);
        	createDocumentSnapshot(session,mrcsdocument);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+"exception/error in snapshot creation before promote",null,e);
            // perform mrcs manual rollback
            boolean rollbackflag = MrcsRollbackService.performRollback(mrcsdocument,"lifecycle");
            throw new RuntimeException("PromoteService failed on snapshotting of document",e);
        }
        
        try {
			// step 3: version the document if necessary
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"performing version change - preversioned docid: "+mrcsdocument.getObjectId().getId() , null, null);
	        IDfId versioneddocid = performVersionChange(targetstate,mrcsdocument);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"  -- -- postversioned docid: "+versioneddocid.getId() , null, null);
	        mrcsdocument = (IDfDocument)session.getObject(versioneddocid);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"versioning complete" , null, null);
	        
			// step 4: initiate promotion
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing promote call on IDfDocument, pre: "+mrcsdocument.getCurrentStateName() , null, null);
	        mrcsdocument.promote(null,false,false);
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"promote call on IDfDocument complete, post: "+mrcsdocument.getCurrentStateName() , null, null);
        } catch (Exception e) {
            // rollback 
            /*-ERROR-*/DfLogger.error(this,"MRCS:MrcsPromoteAction: exception/error in versioning or promotion, rolling back...",null,e);
            // perform mrcs manual rollback
            boolean rollbackflag = MrcsRollbackService.performRollback(mrcsdocument,"lifecycle");
            throw new RuntimeException("PromoteService failed, rollback attempted on doc ",e);
        }
        
        // until we can get IDfLifecycleUserAction and IDfLifecyclePostActions working, we have to do it the old fashioned way...
        // transactionalized promote plugins
        IDfSessionManager sMgr = session.getSessionManager();
        try { 
	        boolean bNewtran = false;
	        if (!sMgr.isTransactionActive())
	        {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->TRANSACTION BEGIN<--",null,null);
	            bNewtran = true;
	            sMgr.beginTransaction();
	        }	        

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": get transactioned promote plugins from target state",null,null);
            List transactionplugins = targetstate.ServerActionPlugins;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": execute TRANSACTIONED promote plugins",null,null);
	        executeLifecyclePlugins(sMgr,config.getApplicationDocbase(mrcsapp),transactionplugins,mrcsdocument,mrcsapp,targetstate);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": TRANSACTIONED promote plugins executed successfully",null,null);
	        
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": check if we need to commit (tranaction is local to this method)",null,null);
	        if (bNewtran && sMgr.isTransactionActive()) 
	        {
	            // commit if the transaction is local to this method (might not be if this is part of a plugin sequence to, say, new GF)
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": transaction is local -->TRANSACTION COMMIT<--",null,null);
	            sMgr.commitTransaction();
	        }
        } catch (Exception e) {
            // rollback 
            /*-ERROR-*/DfLogger.error(this,m+": exception/error in TRANSACTIONED promotion plugins, ABORTing transaction...",null,e);
            if (sMgr.isTransactionActive()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": transaction abort -->ABORT TRANSACTION ABORT<--",null,null);
                try { 
                	sMgr.abortTransaction();
                } catch (Exception anothere) {
                    /*-ERROR-*/DfLogger.error(this,m+": exception/error in rollback of transactioned promote plugins",null,anothere);
                }
            }
            // perform mrcs manual rollback
            /*-ERROR-*/DfLogger.error(this,m+": exception/error in TRANSACTIONED promotion plugins, attempting mrcs ROLLBACK",null,e);
            boolean rollbackflag = MrcsRollbackService.performRollback(mrcsdocument,"lifecycle");
            throw new RuntimeException("PromoteService failed, rollback attempted on doc ",e);
        }

        try { 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": get post-promote plugins from target state (no transaction on these)",null,null);
            List transactionplugins = targetstate.ServerPostPlugins;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": execute post-promote plugins",null,null);
	        executeLifecyclePlugins(sMgr,config.getApplicationDocbase(mrcsapp),transactionplugins,mrcsdocument,mrcsapp,targetstate);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": post-promote plugins executed successfully",null,null);
	        
        } catch (Exception e) {
            // perform mrcs manual rollback
            /*-ERROR-*/DfLogger.error(this,m+": exception/error in post-promotion plugins, attempting mrcs ROLLBACK",null,e);
            boolean rollbackflag = MrcsRollbackService.performRollback(mrcsdocument,"lifecycle");
            throw new RuntimeException("PromoteService failed, rollback attempted on doc ",e);
        }

        
	}
	
	public void executeLifecyclePlugins(IDfSessionManager sMgr, String docbase, List plugins, IDfSysObject mrcsdocument, String mrcsapp, MrcsLifecycleState targetstate) throws Exception
	{		
		/*-CONFIG-*/String m="executeLifecyclePlugins-";
        if (plugins != null)        	
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute plugins defined, executing",null,null);
            try {
                HashMap scratchpad = new HashMap();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute executing plugin list",null,null);
                for (int i=0; i < plugins.size(); i++)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute executing plugin #"+i,null,null);
                    MrcsPlugin curplugin = (MrcsPlugin)plugins.get(i);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute getting Class for plugin: "+curplugin.PluginClassName,null,null);
                    Class pluginclass = Class.forName(curplugin.PluginClassName);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute instantiating",null,null);
                    IMrcsLifecyclePlugin plugin = (IMrcsLifecyclePlugin)pluginclass.newInstance();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute executing current plugin",null,null);
                    plugin.execute(sMgr,docbase,targetstate,mrcsapp,mrcsdocument,curplugin.PluginConfiguration, scratchpad);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" :execute current plugin executed",null,null);
                }
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this,m+" :execute Error in lifecycle plugin execution",null,e);
                throw e;
            }
        }
	}

	// this should probably be part of RollbackService...relocate if possible
	public void createDocumentSnapshot(IDfSession session, IDfDocument mrcsdocument) throws DfException
	{
		/*-CONFIG-*/String m="createDocumentSnapshot-";
        // backup the current document in a snapshot in case we need to roll back. 
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"backing up doc with mrcs document snapshot to prep for rollback" , null, null);
        IDfPersistentObject attachment = session.newObject("mrcs_document_snapshot");
        attachment.setId("doc_chronicle_id", mrcsdocument.getChronicleId());
        attachment.setString("doc_version_summary", mrcsdocument.getVersionPolicy().getVersionSummary(","));
        attachment.setId("doc_object_id", mrcsdocument.getObjectId());
        attachment.setId("doc_acl_id", mrcsdocument.getACL().getObjectId());
        attachment.setTime("doc_creation_date", mrcsdocument.getCreationDate());
        attachment.setId("doc_policy_id", mrcsdocument.getPolicyId());
        attachment.setString("doc_state", mrcsdocument.getCurrentStateName());
        attachment.setString("transaction_type","lifecycle");
        attachment.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+":~~ Document backup snapshot saved ~~   ", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->backup ChronicleId() " + mrcsdocument.getChronicleId(), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->backup Version Summary  " + mrcsdocument.getVersionPolicy().getVersionSummary(","), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->backup ObjectId()  " +  mrcsdocument.getObjectId(), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->backup CreationDate() " + mrcsdocument.getCreationDate(), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->backup PolicyId() " + mrcsdocument.getPolicyId(), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+": -->backup CurrentStateName() " + mrcsdocument.getCurrentStateName(), null,null);		
	}
}
