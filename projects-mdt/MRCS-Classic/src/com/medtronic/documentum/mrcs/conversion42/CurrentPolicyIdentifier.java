package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;

public class CurrentPolicyIdentifier  
{	
	public static Map getLifecycleMappings(IDfSession session, List doclist) throws Exception
	{
		List validpolicies = new ArrayList();
		List obsoletepolicies = new ArrayList();
		List invalidpolicies = new ArrayList();
		List nopolicies = new ArrayList();

        /*-DEBUG-*/DfLogger.debug(CurrentPolicyIdentifier.class, "getLifecycleMappings- top", null, null);
				
		try { 
						
			Map policycache = new HashMap();

			for (int i=0; i < doclist.size(); i++)
	        {
				DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
	        	String curid = dor.objectid;
	        	IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
	        	dor.name = doc.getObjectName();
	        	IDfId policyid = doc.getPolicyId();
	        	if (!policyid.isNull() && policyid.isObjectId())
	        	{
		        	IDfSysObject policy = null;
		        	boolean validpolicy = true;
		        	if (policycache.containsKey(policyid.getId())) {
		        		policy = (IDfSysObject)policycache.get(policyid.getId());
		        	} else {
		        		try { 
		        			policy = (IDfSysObject)session.getObject(policyid);
		        		} catch (DfException dfe) {
		        			validpolicy = false;
		        		}
		        		if (validpolicy)
		        			policycache.put(policyid.getId(),policy);
		        	}
		        	if (validpolicy) {
			        	String version = policy.getVersionLabel(0);
			        	String name = policy.getObjectName();
			        	if (policy.getLatestFlag())
			        	{
				        	dor.note = "[ "+name+" ][ "+version+" ][ "+policyid.getId()+" ]";
				        	validpolicies.add(dor);
			        	} else {
				        	dor.note = "[ "+name+" ][ "+version+" ][ "+policyid.getId()+" ]";
				        	obsoletepolicies.add(dor);
			        	}
		        	} else {
			        	dor.note = "-invalidpolicy-[ "+policyid.getId()+ " ]";
			        	invalidpolicies.add(dor);
		        	}
		        		
	        	} else {
		        	dor.note = "-none-";
		        	nopolicies.add(dor);
	        	}
	            /*-DEBUG-*/DfLogger.debug(CurrentPolicyIdentifier.class, "getLifecycleMappings- #"+i+" - id "+dor.objectid+ " - note: "+dor.note, null, null);
	        	
	        }	        	
		} catch (DfException dfe) {
			
			throw new RuntimeException(dfe);
		}
		
        /*-DEBUG-*/DfLogger.debug(CurrentPolicyIdentifier.class, "getLifecycleMappings- done", null, null);
		Map amap = new HashMap();
		amap.put("valid",validpolicies);
		amap.put("obsolete",obsoletepolicies);
		amap.put("invalid",invalidpolicies);
		amap.put("none",nopolicies);
		return amap;
		
	}

}
