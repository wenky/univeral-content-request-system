package com.medtronic.documentum.mrcs.docbase.install;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;

public class ObsoleteAndRetiredConversion {

	public void convert() throws Exception
	{
		
		// select all documents and look for retired and obsolete versions
		// eff = 3 obs = 4 ret = 6 for NPP
		// app = 2 for efs?
		String query = "SELECT r_object_id FROM m_mrcs_efs_central_document WHERE r_current_state = 4 OR r_current_state = 6";  
		
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
		
		// get list of objects that are pdfs...
		
        IDfQuery qry = new DfQuery();
        qry.setDQL(query);

        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
        
        IDfTypedObject serverConfig = session.getServerConfig();
        String aclDomain = serverConfig.getString("operator_name");
		IDfACL obsoleteacl = session.getACL(aclDomain,"mrcs_central_archived");
		IDfACL retiredacl = session.getACL(aclDomain,"mrcs_central_retired_doc");


        while (myObj1.next()) 
        {
        	String curid = myObj1.getString("r_object_id");
    		// look up object
    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
    		
    		// check that it's not a popped off copy (i_folder_id[0] != 'Approved') seems to be the only indicator of this...
    		IDfFolder folder = (IDfFolder)session.getObject(doc.getFolderId(0));
    		String foldername = folder.getObjectName();
    		
    		// don't do this for approved copies...
    		if (!"Approved".equals(foldername))
    		{
	    		
	    		// check it's state
	    		String statename = doc.getCurrentStateName();
	    		String chronicleid = doc.getChronicleId().getId();
	    		// if In-Progress: simply switch the content type
	    		if ("Obsolete".equals(statename))
	    		{
	    			String previousquery = "SELECT r_object_id FROM dm_document(all) where i_chronicle_id = '"+chronicleid+"'";
	    	        IDfQuery prevqry = new DfQuery();
	    	        prevqry.setDQL(query);
	    	        IDfCollection previousversions = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	    	        while (previousversions.next()) 
	    	        {
	    	        	
	    	        	String previd = previousversions.getString("r_object_id");
	    	    		// look up object
	    	    		IDfDocument prevdoc = (IDfDocument)session.getObject(new DfId(previd));
	    	    		//unlock 
	    	    		sMgr.beginTransaction();
	    	    		try { 
		    	            prevdoc.setString("r_immutable_flag", "FALSE");
		    	            prevdoc.save();
		    	            
		    	            prevdoc.fetch(prevdoc.getTypeName()); // necessary?	    	    		
			    			// set flags obsolete = true,retired = false
		    	    		prevdoc.setBoolean("retired",false);
		    	    		prevdoc.setBoolean("obsolete",true);
			    			// set acl
		    	    		prevdoc.setACL(obsoleteacl);
		    	            prevdoc.setString("r_immutable_flag", "TRUE");
		    	    		prevdoc.save();
	    	    		} catch (Exception e) {
	    	    			sMgr.abortTransaction();
	    	    			throw e;
	    	    		}
	    	    		sMgr.commitTransaction();
	    	        }
	    	        previousversions.close();
	    		}
	    		
	    		if ("Effective".equals(statename))
	    		{
	    			String previousquery = "SELECT r_object_id FROM dm_document(all) where i_chronicle_id = '"+chronicleid+"'";
	    	        IDfQuery prevqry = new DfQuery();
	    	        prevqry.setDQL(query);
	    	        IDfCollection previousversions = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	    	        while (previousversions.next()) 
	    	        {
	    	        	
	    	        	String previd = previousversions.getString("r_object_id");
	    	    		// look up object
	    	    		IDfDocument prevdoc = (IDfDocument)session.getObject(new DfId(previd));
	    	    		//unlock 
	    	    		String currentstate = prevdoc.getCurrentStateName();
	    	    		if ("Retired".equals(currentstate))
	    	    		{
		    	    		sMgr.beginTransaction();
		    	    		try { 	    	    			
			    	            prevdoc.setString("r_immutable_flag", "FALSE");
			    	            prevdoc.save();
			    	            prevdoc.fetch(prevdoc.getTypeName()); // necessary?	    	    		
				    			// set flags obsolete = true,retired = false
			    	    		prevdoc.setBoolean("retired",true);
			    	    		prevdoc.setBoolean("obsolete",false);
				    			// set acl
			    	    		prevdoc.setACL(retiredacl);
			    	            prevdoc.setString("r_immutable_flag", "TRUE");
			    	    		prevdoc.save();
		    	    		} catch (Exception e) {
		    	    			sMgr.abortTransaction();
		    	    			throw e;
		    	    		}
		    	    		sMgr.commitTransaction();
	    	    		}
	    	        }
	    	        previousversions.close();
	    		}
	    		
    		}
        }
        myObj1.close();
        
        sMgr.release(session);
	}

}
