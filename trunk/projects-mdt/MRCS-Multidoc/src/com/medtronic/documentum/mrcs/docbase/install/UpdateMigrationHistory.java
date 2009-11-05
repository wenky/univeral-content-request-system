package com.medtronic.documentum.mrcs.docbase.install;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;

public class UpdateMigrationHistory {


	public void convert() throws Exception
	{
		
		// this utility is for marking the source of documents that were subjected to migration:
		// - if we can identify that a doc was converted by Wallach for the initial migration, it gets marked pre-EFS
		// - if we can identify that a doc or version was created in 4.1.2, then it gets marked 4.1.2
		// - next time, we'll mark docs without migration histories as 4.2.1
		
		IDfSession session = null;
		IDfSessionManager sMgr = null;
		try {
	        IDfClientX clientx = new DfClientX();

	       	IDfClient client = clientx.getLocalClient();

	       	sMgr = client.newSessionManager();

	       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
	        loginInfoObj.setUser("mradmin");
	        loginInfoObj.setPassword("mr2006");
	        loginInfoObj.setDomain(null);

	        sMgr.setIdentity("MRCS_Dev", loginInfoObj);
        }
        catch (DfException dfe){
        	dfe.printStackTrace();
        }		
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " ", null, null);
        
        String historyvalue = "pre-EFS"; // or MRCS4.1.2 or 
		
		// get list of objects that are migrated in a specific wave: initial EFS MRCS/Wallach conversion, 4.1.2 to 4.2.1 conversion  
		String query = "SELECT r_object_id, i_chronicle_id FROM m_mrcs_efs_central_document(all) WHERE ????";  
		
        IDfQuery qry = new DfQuery();
        qry.setDQL(query);

        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);

        while (myObj1.next()) 
        {
        	String curid = myObj1.getString("r_object_id");
    		// look up object
    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
    		
	    		
    		// check it's state
    		String statename = doc.getCurrentStateName();
    		String chronicleid = doc.getChronicleId().getId();
	        	
    		//unlock 
    		sMgr.beginTransaction();
    		try {
    			boolean immutable = false;
    			if ("TRUE".equals(doc.getString("r_immutable_flag")))
    			{
    				immutable=true;
    	            doc.setString("r_immutable_flag", "FALSE");
    	            doc.save();
    	            
    	            doc.fetch(doc.getTypeName()); // necessary?
    			}
    			
	            // set migration history
    			int count = doc.getValueCount("migration_history");
    			int hashistory = doc.findString("migration_history",historyvalue);
    			
	            if (immutable)
	            {
	            	doc.setString("r_immutable_flag", "TRUE");
	            }
	    		doc.save();
    		} catch (Exception e) {
    			sMgr.abortTransaction();
    			throw e;
    		}
    		sMgr.commitTransaction();
	    		
        }
        myObj1.close();
        
        sMgr.release(session);
	}

}
