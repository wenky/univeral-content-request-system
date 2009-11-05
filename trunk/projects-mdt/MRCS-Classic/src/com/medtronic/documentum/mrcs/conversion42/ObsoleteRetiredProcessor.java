package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

public class ObsoleteRetiredProcessor
{

	public static Map processObsoleteAndRetired(IDfSession session, String obsoletestate, String retiredstate, List doclist) throws Exception
	{
		// iterates through all versions of each listed document and flips ret/obs flags if either of the states are matched in the document version history
		        
        List success = new ArrayList();
        List error = new ArrayList();
		
		// iterate
        /*-DEBUG-*/System.out.println("processObsoleteAndRetired- top");
        
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
			try { 
	    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
	    		String statename = doc.getCurrentStateName();
	    		String chronicleid = doc.getChronicleId().getId();
				
				String previousquery = "SELECT r_object_id FROM dm_document(all) where i_chronicle_id = '"+chronicleid+"'";
		        IDfQuery prevqry = new DfQuery();
		        prevqry.setDQL(previousquery);
		        IDfCollection previousversions = (IDfCollection) prevqry.execute(session, IDfQuery.DF_READ_QUERY);
		        boolean erred = false;
		        dor.list = new ArrayList();
		        dor.map = new HashMap();
		        while (previousversions.next()) 
		        {
		        	
		        	String previd = previousversions.getString("r_object_id");
		    		// look up object
		    		IDfDocument prevdoc = (IDfDocument)session.getObject(new DfId(previd));
		    		String prevdocstate = prevdoc.getCurrentStateName();
		    		//unlock 
					if (obsoletestate.equals(prevdoc.getCurrentStateName()))
					{
	    	    		boolean tranfailed = false;
	    	    		BaseClass.sMgr.beginTransaction();
	    	    		try { 
			    			// set flags obsolete = true,retired = false
		    	    		prevdoc.setBoolean("retired",false);
		    	    		prevdoc.setBoolean("obsolete",true);
		    	    		prevdoc.save();
		    	            /*-DEBUG-*/System.out.println("processObsoleteAndRetired- "+new Date()+" #"+i+" - ret=F obs=T for id "+dor.objectid);
	    	    		} catch (Exception e) {
	    	    			BaseClass.sMgr.abortTransaction();
	    	    			// append this to the error case?
	    	    			DocbaseObjectRecord errdor = new DocbaseObjectRecord();
	    	    			errdor.objectid = prevdoc.getObjectId().getId();
	    	    			errdor.version = prevdoc.getVersionLabels().getImplicitVersionLabel();
	    	    			errdor.note = e.getMessage();
	    	    			errdor.error = e;
	    	    			dor.list.add(errdor);
	    	    			tranfailed = true;
	    	    			erred = true;
		    	            /*-ERROR-*/System.out.println("processObsoleteAndRetired- #"+i+" - ERROR(obs) "+dor.objectid);e.printStackTrace();
	    	    		}
	    	    		if (!tranfailed) {
	    	    			BaseClass.sMgr.commitTransaction();
	    	    			DocbaseObjectRecord sucdor = new DocbaseObjectRecord();
	    	    			sucdor.objectid = prevdoc.getObjectId().getId();
	    	    			sucdor.version = prevdoc.getVersionLabels().getImplicitVersionLabel();
	    	    			sucdor.note = "obsolete";
	    	    			dor.map.put(sucdor.version,sucdor);
	    	    		}
					} else if (retiredstate.equals(prevdoc.getCurrentStateName())) {
	    	    		boolean tranfailed = false;
	    	    		BaseClass.sMgr.beginTransaction();
	    	    		try { 
			    			// set flags obsolete = true,retired = false
		    	    		prevdoc.setBoolean("retired",true);
		    	    		prevdoc.setBoolean("obsolete",false);
		    	    		prevdoc.save();
		    	            /*-DEBUG-*/System.out.println("processObsoleteAndRetired- "+new Date()+" #"+i+" - obs=F ret=T for id "+dor.objectid);
	    	    		} catch (Exception e) {
	    	    			BaseClass.sMgr.abortTransaction();
	    	    			// append this to the error case?
	    	    			// append this to the error case?
	    	    			DocbaseObjectRecord errdor = new DocbaseObjectRecord();
	    	    			errdor.objectid = prevdoc.getObjectId().getId();
	    	    			errdor.version = prevdoc.getVersionLabels().getImplicitVersionLabel();
	    	    			errdor.note = e.getMessage();
	    	    			errdor.error = e;
	    	    			dor.list.add(errdor);
	    	    			tranfailed = true;
	    	    			erred = true;
		    	            /*-ERROR-*/System.out.println("processObsoleteAndRetired- #"+i+" - ERROR(ret) "+dor.objectid);e.printStackTrace();
	    	    		}
	    	    		if (!tranfailed) {
	    	    			BaseClass.sMgr.commitTransaction();
	    	    			DocbaseObjectRecord sucdor = new DocbaseObjectRecord();
	    	    			sucdor.objectid = prevdoc.getObjectId().getId();
	    	    			sucdor.version = prevdoc.getVersionLabels().getImplicitVersionLabel();
	    	    			sucdor.note = "retired";
	    	    			dor.map.put(sucdor.version,sucdor);
	    	    		}
						
					}
	
		        }
		        previousversions.close();
		        if (erred) {
		        	error.add(dor);    	        	
		        } else {
		        	success.add(dor);
		        }
			} catch (Exception e) {
	            /*-ERROR-*/System.out.println("processObsoleteAndRetired- #"+i+" - ERROR(UNKNOWN) "+dor.objectid);e.printStackTrace();
				dor.error = e;
				dor.note = e.getMessage();
				error.add(dor);
			}
		}		
        /*-DEBUG-*/System.out.println("processObsoleteAndRetired- done");
		Map results = new HashMap();
		results.put("success",success);
		results.put("error",error);
		return results;
	}
}
