package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.HashMap;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class MdtObsoleteAndHidePreviousVersions extends MdtProcessAttachments
{

    public void processAttachment(IDfSessionManager smgr, String docbase,
            String mdtapp, IDfWorkitem workitem, IDfSysObject controldoc,
            IDfSysObject attachdoc, Map context) 
    {
        IDfSession session = null;
        try {
            session = smgr.getSession(docbase);
            Map configdata = (Map)context;
            /*-dbg-*/Lg.wrn("get obs state from config ");
            String obsoletestate = (String)configdata.get("ObsoleteState");            
            String obsoleteacl = (String)configdata.get("PreviousVersionObsoleteACL");
            /*-dbg-*/Lg.wrn("obs state: %s acl: %s",obsoletestate,obsoleteacl);
            IDfTypedObject serverConfig = session.getServerConfig();
            String aclDomain = serverConfig.getString("operator_name");
            IDfACL obsacl = session.getACL(aclDomain, obsoleteacl);
            IDfSysObject curobj = attachdoc;            
            /*-dbg-*/Lg.wrn("get current state of %s",curobj);
            String curstate = curobj.getCurrentStateName();
            // make sure it isn't suspended
            /*-dbg-*/Lg.wrn("unsuspending document %s",curobj);
            if (curobj.isSuspended())
            {
                /*-dbg-*/Lg.wrn("unsuspending document %s",curobj);
                curobj.resume(curstate,false,true,false);
                /*-dbg-*/Lg.wrn("unsuspended");
            }
            // obsolete current document
            /*-dbg-*/Lg.wrn("obsoleting %s",curobj);
            obsoleteDocumentDirectAttach(curobj,obsoletestate);
            while (true) {
                // check if we are at the bottom of the document version history (== chronicle id)
                /*-dbg-*/Lg.wrn("check if we are at bottom of doc versions");
                if (curobj.getObjectId().equals(curobj.getChronicleId())) {
                    /*-dbg-*/Lg.wrn("cur obj equals chronicle id, so we're done stepping down the tree");
                    break;
                } else {
                    /*-dbg-*/Lg.wrn("not at bottom yet, get antecedent");
                    curobj = (IDfSysObject)session.getObject(curobj.getAntecedentId());
                    /*-dbg-*/Lg.wrn("antecedent: %s",curobj);
                }                
                curobj.setACL(obsacl);
                curobj.save();
            }           
            /*-dbg-*/Lg.wrn("...done obsoleting %s",attachdoc);
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Error occurred in promotion of document %s",attachdoc,dfe);
            throw EEx.create("JobPromote","Error occurred in promotion of document %s",attachdoc,dfe);
        } finally {
            try { smgr.release(session); }catch(Exception e){}
        }
        
    }
    
    public void obsoleteDocumentDirectAttach(IDfSysObject curobj, String targetstate) throws DfException {
        /*-dbg-*/Lg.wrn("get current policy, alias");
        IDfId curpolicyid = curobj.getPolicyId();
        String curpolicyalias = curobj.getAliasSet();
        if (curpolicyalias == null) curpolicyalias = "";
        /*-dbg-*/Lg.wrn("attach policy %s to state %s with aliasset %s",curpolicyid.getId(),targetstate,curpolicyalias);        
        curobj.attachPolicy(curpolicyid, targetstate, curpolicyalias);
        /*-dbg-*/Lg.wrn("DONE");
    }

    public void obsoleteDocumentStepThroughStates(IDfSysObject curobj, String targetstate) throws DfException {
        /*-dbg-*/Lg.wrn("check if current state matches target state");
        while (!curobj.getCurrentStateName().equals(targetstate)) {
            /*-dbg-*/Lg.wrn("get current and next state name");
            String curstate = curobj.getCurrentStateName();
            String nextstate = curobj.getNextStateName();
            /*-dbg-*/Lg.wrn("check if curstate %s equals nextstate %s error condition",curstate,nextstate);
            if (curstate.equals(nextstate)) {
                // end of the LC, state wasn't found. throw Exception...
                /*-ERROR-*/Lg.err("ERROR: reach end of lifecycle without encountering target state %s for document %s",curstate,curobj);
                throw EEx.create("ObsDoc-ObsStateNotFound","ERROR: reach end of lifecycle without encountering target state %s for document %s",curstate,curobj);
            }
            /*-dbg-*/Lg.wrn("promote to next state");
            curobj.promote(null, false, false);
            /*-dbg-*/Lg.wrn("promoted, loop and check if we have reached the final state");
        }
        /*-dbg-*/Lg.wrn("DONE");
    }

    public static void main(String[] args) throws Exception
    {
        try { 
            MdtObsoleteAndHidePreviousVersions mdtmsg = new MdtObsoleteAndHidePreviousVersions();
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("dmadmin");
            loginInfoObj.setPassword("test");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("qadoc", loginInfoObj);             
            IDfSession sess = sMgr.getSession("qadoc");
            
            IDfSysObject idfsysobject = (IDfSysObject)sess.getObject(new DfId("09186af580437ce6"));
            
            Map config = new HashMap(); config.put("ObsoleteState","Obsolete"); config.put("PreviousVersionObsoleteACL", "mdt_qad_smo_qdoc_obsolete_prev");
            

            sMgr.release(sess);
        } catch (Exception ez) {
            int i = 1;
            i++;
        }        
        
    }    

}
