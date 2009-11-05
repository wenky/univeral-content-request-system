package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;

public class ObsoleteRetiredIdentifier 
{
	
	// select all documents and look for retired and obsolete versions
	// eff = 3 obs = 4 ret = 6 for NPP
	// app = 2 for efs?
	// String query = "SELECT r_object_id FROM m_mrcs_efs_central_document WHERE r_current_state = 4 OR r_current_state = 6";  
	
	public static List identifyObsoleteAndRetired(IDfSession session, String query, String effectivestate, String retiredstate) throws Exception
	{
        IDfQuery qry = new DfQuery();
        qry.setDQL(query);
        
        List output = new ArrayList();
        
        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
		
        while (myObj1.next()) 
        {
        	String curid = myObj1.getString("r_object_id");
    		// look up object
    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
    		// check that it's not a popped off copy (i_folder_id[0] != 'Approved') seems to be the only indicator of this...
    		IDfFolder folder = (IDfFolder)session.getObject(doc.getFolderId(0));
    		String foldername = folder.getObjectName();
    		
    		IDfId policyid = doc.getPolicyId(); // use this to test if it is a copy - ??copies don't have policies attached??
    		
    		// don't do this for approved copies... (this should work for EFS and NPP, maybe I can check if it has a policy as a test...)
    		if (!"Approved".equals(foldername))
    		{
	    		String statename = doc.getCurrentStateName();
	    		String chronicleid = doc.getChronicleId().getId();
    			
	    		if ("Obsolete".equals(statename))
	    		{
	    			// implies we will need to do Obsolete Processing on this record
	    			DocbaseObjectRecord obj = new DocbaseObjectRecord();
	    			obj.objectid = doc.getObjectId().getId();
	    			obj.chronid = doc.getChronicleId().getId();
	    			obj.note = "Obsolete";
	    			output.add(obj);
	    		} else {
	    			// see if any of the previous versions are in the retired state (retired processing)
	    			String prevversions = "SELECT r_object_id FROM dm_document(ALL) where i_chronicle_id = '"+chronicleid+"' ORDER BY r_object_id";
	    	        IDfQuery prevqry = new DfQuery();
	    	        prevqry.setDQL(prevversions);
	    	        IDfCollection previds = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	    	        boolean hasRetired = false;
	    	        while (previds.next())
	    	        {
	    	        	String objid = myObj1.getString("r_object_id");
	    	    		IDfDocument prevdoc = (IDfDocument)session.getObject(new DfId(curid));
	    	    		if (prevdoc.getCurrentStateName().equals(retiredstate))
	    	    		{
	    	    			hasRetired = true;
	    	    			break;
	    	    		}
	    	        }
	    	        previds.close();
	    	        if (hasRetired)
	    	        {
		    			DocbaseObjectRecord obj = new DocbaseObjectRecord();
		    			obj.objectid = doc.getObjectId().getId();
		    			obj.note = "Retired";
		    			output.add(obj);
	    	        }
	    		}
    			
    		}
        }
        myObj1.close();
        
        return output;
		
	}

}
