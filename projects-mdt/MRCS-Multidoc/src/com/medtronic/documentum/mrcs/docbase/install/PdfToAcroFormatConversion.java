package com.medtronic.documentum.mrcs.docbase.install;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfLoginInfo;

public class PdfToAcroFormatConversion 
{
	public static void main(String[] args) throws Exception
	{
		PdfToAcroFormatConversion inst = new PdfToAcroFormatConversion();
		inst.convert();
	}
	
	public void convert() throws Exception
	{
		// EFS:
		//  r_current_state: 2 == In-Approval, so 3 == Approved, 4 == ?Effective?
		//  a_content_type = 'pdf'
		
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
		
		// don't want to convert approved/effective copies...crap...
		//  - must not be in an "Approved" or "Effective" folder?
		
		// SELECT count(*) FROM m_mrcs_central_document WHERE a_content_type = 'pdf' AND any i_folder_id NOT IN (SELECT r_object_id FROM dm_folder WHERE object_name = 'Effective');
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " ", null, null);
		
        session = sMgr.getSession("MRCS_Dev");
		
		// get list of objects that are pdfs...
        // - should try folder clauses too...
        // - previous versions????
		String pdfdocs = "SELECT r_object_id FROM m_mrcs_central_document WHERE a_content_type = 'pdf' and object_name like '--test--%'";
		
        IDfQuery qry = new DfQuery();
        qry.setDQL(pdfdocs);

        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);

        while (myObj1.next()) 
        {
            sMgr.beginTransaction();
            try { 
	        	String curid = myObj1.getString("r_object_id");
	    		// look up object
	    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
	    		
	    		String objname = doc.getObjectName();
	    		String fullpath = doc.getPath(0);
	    		String format = doc.getFormat().getName();
	    		String doctype = doc.getTypeName();
	
	    		// check that it's not a popped off copy (i_folder_id[0] != 'Approved') seems to be the only indicator of this...
	    		IDfFolder folder = (IDfFolder)session.getObject(doc.getFolderId(0));
	    		String foldername = folder.getObjectName();
	
	    		if (!"Approved".equals(foldername))
	    		{
	
		    		// check it's state
		    		String statename = doc.getCurrentStateName(); // Approved or Effective are only active docs with renditions
	                String filename = doc.getFile(null);
		    		// if In-Progress: simply switch the content type
		    		if ("In-Progress".equals(statename) || "Approved".equals(statename) || "Effective".equals(statename))
		    		{
		    			// change format - make sure this doesn't "zero out" the content or something else weird.
		    			doc.setContentType("acro");
		    			doc.setFile(filename);
		    		}
	
		    		// clone content as rendition for approved and effective
		    		if ("Approved".equals(statename) || "Effective".equals(statename)) // In-Approval too?
		    		{
		                doc.addRendition(filename,"pdf");
		    		}
		    		
		    		doc.save();
	    		}
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
