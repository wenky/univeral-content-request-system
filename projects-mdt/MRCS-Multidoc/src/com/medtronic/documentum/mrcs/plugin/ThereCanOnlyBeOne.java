/*
 * Created on Sep 14, 2005
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

 Filename       $RCSfile: ThereCanOnlyBeOne.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.6 $
 Modified on    $Date: 2006/11/14 19:33:29 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Date;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

/**
 * @author muellc4
 *
 * This is a state transition plugin that sets all other versions of this
 * document to a specified lifecycle state. Used in situations such as 
 * policy/procedure repositories where only one version of a policy is 
 * designated as "effective", or if a particular policy is deemed obsolete,
 * then all other versions of the document are set to obsolete as well. 
 * 
 * alternate plugins should extend this class and override the getDQL() 
 * method if the plugin needs a different query, and/or override the
 * process() method that mucks with a query result if you want to do something
 * else...
 */
public class ThereCanOnlyBeOne implements IStateTransitionPlugin, IMrcsLifecyclePlugin
{
    // EXTEND ME! if another plugin needs a different lookup DQL, just extend this class and override this method
    public String getDQL(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "ThereCanOnlyBeOne.getDQL()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get postversioned document's new id", null, null);
        String docid = PostVersionedDoc.getObjectId().getId();
        int curstate = PostVersionedDoc.getCurrentState();
               
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for looking up both newer and older versions of the current doc", null, null);
        String qualification = "select r_object_id from dm_sysobject (ALL) "+
                               "where NOT r_object_id = '"+docid+"' AND r_current_state = "+curstate+" AND i_chronicle_id in " +
                                    "(select i_chronicle_id from dm_sysobject where r_object_id = '"+docid + "')";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"composed DQL: "+qualification, null, null);
        return qualification;
        
    }
    
    // EXTEND ME! override this if you want query results to be processed differently - this does arbitrary LC detach and reattach...
    public void process(IDfCollection match, IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "ThereCanOnlyBeOne.process()-";
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
        
        try { 
            // make object immutable, and attach the new lifecycle (policy)
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"begin attach and detach of new lifecycle", null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"make document mutable", null, null);
            // user should be SUPERUSER in order to play with r_immutable_flag
            doc.setString("r_immutable_flag", "FALSE");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"refetching", null, null);
            doc.fetch(doc.getTypeName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"detach current lifecycle", null, null);
            doc.detachPolicy();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attach new lifecycle", null, null);
            doc.attachPolicy(lifecycle.getObjectId(), state, "");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"lifecycle applied, saving changes", null, null);
    
            // apply new LC state's label
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting label for new LC and state", null, null);        
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up state info from MRCS config for state "+state, null, null);
            StateInfo newStInfo = config.getStateInfo(mrcsapp, state);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"newStInfo: " + newStInfo, null, null);
            String labeltoAttach = newStInfo.getLabel();            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"labeltoAttach: " + labeltoAttach, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scan document's current labels", null, null);
            for (int i = 0; i < doc.getVersionLabelCount(); i++) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--examining label #"+i, null, null);
                String label = doc.getVersionLabel(i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--cur label: "+ label, null, null);
                if (i > 0) 
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--see if we've found 'CURRENT'", null, null);
                    if (!label.equalsIgnoreCase("CURRENT"))
                    {
                        try {
                            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--'CURRENT' found, unmarking...", null, null);
                            doc.unmark(label);
                        } catch (DfException e1) {
                            /*-ERROR-*/DfLogger.error(this, m+"Error when removing 'CURRENT' label from older document "+doc.getObjectId().getId(), null, e1);
                            throw e1;
                        }
                    }
                } else {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"0th label, appending new state's label...", null, null);                
                    labeltoAttach = label+"_"+labeltoAttach;
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"new label: "+labeltoAttach, null, null);                
                }
            }
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"attaching label: "+labeltoAttach, null, null);                
            doc.mark(labeltoAttach);
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"setting document back to immutable", null, null);
            doc.setString("r_immutable_flag", "TRUE");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"saving", null, null);
            doc.save();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"refetching...", null, null);
            doc.fetch(doc.getTypeName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done!", null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.debug(this, m+"Exception in arbitrary state attachment for doc"+id, null, e);
            throw e;
        }
        sMgr.release(session);
    }
    
    // utility method to get the objectid from the match's collection of attributes
    // ...only works if the query only selects one single field, the objectid
    public String getMatchObjectId(IDfCollection match) throws Exception
    {
        /*-CFG-*/String m = "ThereCanOnlyBeOne.getMatchObjectId()-";
        String id = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>processing match's attributes", null, null);
        for (int i = 0; i < match.getAttrCount(); i++) 
        { 
            IDfAttr attr = match.getAttr(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>match attr: "+attr.getName(), null, null);
            if (attr.getDataType() == attr.DM_STRING) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>doc attr is a DM_STRING, must be a docid", null, null);
                id = match.getString(attr.getName());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+">>object id: "+id, null, null);
            }
        }
        return id;
    }   
    
	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
        /*-CFG-*/String m="execute(new)-";        
		try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"4.2 call invoked, calling 4.1.2 plugin",null,null);
			execute2(sMgr,docbase,mrcsapp,(IDfDocument)mrcsdocument,config,context);
		} catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Exception occurred in ThereCanOnlyBeOne base class execute method", null, e);
            RuntimeException re = new RuntimeException("Exception occurred in ThereCanOnlyBeOne base class execute method",e);
            throw re;			
		}
	}
    
    public void execute(IDfSessionManager sMgr, String docbase, StateInfo currentstate, String mrcsapp, IDfDocument PreVersionedDoc, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m="execute(old)-";        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"4.1.2 call invoked, calling 4.1.2 plugin",null,null);
		execute2(sMgr,docbase,mrcsapp,PostVersionedDoc,configdata,customdata);
    	
    }

    // a simple method that asks for a DQL statement with getDQL(), iterates through the result sets, calling process() for each of them.    
    public void execute2(IDfSessionManager sMgr, String docbase, String mrcsapp, IDfDocument PostVersionedDoc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m = "ThereCanOnlyBeOne.execute()-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"preparing to perform search + process ", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"reacquire session", null, null);
        IDfSession session = sMgr.getSession(docbase);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get postversioned document's new id", null, null);
        String docid = PostVersionedDoc.getObjectId().getId();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"compose DQL for search", null, null);
        String qualification = getDQL(sMgr,docbase,mrcsapp,PostVersionedDoc,configdata,customdata);
        try {

            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Prepare to exec DQL query", null, null);
            qry.setDQL(qualification);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"executing query", null, null);
            Date start = new Date();
            IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
            Date finish = new Date();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"query exec time: "+(finish.getTime()-start.getTime()), null, null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"iterating through results", null, null);
            while (myObj1.next()) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--process match: "+myObj1.getObjectId(), null, null);
                process(myObj1,sMgr,docbase,mrcsapp,PostVersionedDoc,configdata,customdata);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"--match processed, next!", null, null);
            }
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done with search, closing result set", null, null);            
            myObj1.close();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Exception occurred while iterating through search results "+docid, null, e);
            RuntimeException re = new RuntimeException("Exception occurred while looking up all versions of document "+docid,e);
            throw re;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done!", null, null);            
        
    }

}
