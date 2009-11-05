package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

public class ReattachPolicyProcessor 
{
	
	public static Map reattachPolicies(IDfSession session, List doclist) throws Exception
	{
		
        /*-DEBUG-*/System.out.println("reattachPolicies- top");
		
		Map lccache = new HashMap(); 

		List error = new ArrayList();
		List success = new ArrayList();

		for (int i=0; i< doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
    		
    		boolean tranfailed = false;
    		BaseClass.sMgr.beginTransaction();
    		try { 
        		IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
        		IDfSysObject lifecycleobj = null;
    			dor.lifecycle = doc.getPolicyName();
        		if (lccache.containsKey(dor.lifecycle))
        		{
        			lifecycleobj = (IDfSysObject)lccache.get(dor.lifecycle);
        		} else {
        			lifecycleobj = (IDfSysObject)session.getObjectByQualification("dm_policy WHERE object_name = '"+dor.lifecycle+"'");
        			lccache.put(dor.lifecycle,lifecycleobj);
        		}
        		String statename = doc.getCurrentStateName();
        		int statenum = doc.getCurrentState();
    			dor.state = statename;
    			//doc.detachPolicy();
    			//doc.save();
    			doc.attachPolicy(lifecycleobj.getObjectId(),statename,"");
    	        /*-DEBUG-*/System.out.println("reattachPolicies- "+new Date()+" attachment #"+i+" - id "+dor.objectid+" - policy "+dor.lifecycle+" - state "+statename);
    			doc.save();
    		} catch (Exception e) {
    			BaseClass.sMgr.abortTransaction();    			
    			// append this to the error case?
    	        /*-ERROR-*/System.out.println("reattachPolicies-  error for object "+dor.objectid);e.printStackTrace();
    			dor.note = e.toString();
    			dor.error = e;
    			error.add(dor);
    			tranfailed = true;
    		}
    		if (!tranfailed) {
    			BaseClass.sMgr.commitTransaction();
    			success.add(dor);
    		}
		}

        /*-DEBUG-*/System.out.println("reattachPolicies- done");

		Map results = new HashMap();
		results.put("success",success);
		results.put("error",error);
		return results;
		
		
	}

	public static Map attachPolicy(IDfSession session, String lifecycle,List doclist) throws Exception
	{
		
		Map lccache = new HashMap(); 

		List error = new ArrayList();
		List success = new ArrayList();

		for (int i=0; i< doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
    		IDfSysObject lifecycleobj = null;
			dor.lifecycle = lifecycle;//doc.getPolicyName();
    		if (lccache.containsKey(dor.lifecycle))
    		{
    			lifecycleobj = (IDfSysObject)lccache.get(dor.lifecycle);
    		} else {
    			lifecycleobj = (IDfSysObject)session.getObjectByQualification("dm_policy WHERE object_name = '"+dor.lifecycle+"'");
    			lccache.put(dor.lifecycle,lifecycleobj);
    		}
    		String statename = doc.getCurrentStateName();
    		int statenum = doc.getCurrentState();
			dor.state = statename;
    		
    		boolean tranfailed = false;
    		BaseClass.sMgr.beginTransaction();
    		try { 
    			//doc.detachPolicy();
    			//doc.save();
    			doc.attachPolicy(lifecycleobj.getObjectId(),statename,"");
    			doc.save();
    		} catch (Exception e) {
    			BaseClass.sMgr.abortTransaction();    			
    			// append this to the error case?
    			dor.apicmd1 = "attach,c,"+doc.getObjectId()+","+lifecycleobj.getObjectId()+",0";
    			dor.apicmd2 = "save,c,"+doc.getObjectId();
    			dor.note = "["+doc.getPermit()+"] -- ";
    			dor.note += e.toString();
    			dor.error = e;
    			error.add(dor);
    			tranfailed = true;
    		}
    		if (!tranfailed) {
    			BaseClass.sMgr.commitTransaction();
    			success.add(dor);
    		}
		}
		
		Map results = new HashMap();
		results.put("success",success);
		results.put("error",error);
		return results;
		
		
	}
		

}
