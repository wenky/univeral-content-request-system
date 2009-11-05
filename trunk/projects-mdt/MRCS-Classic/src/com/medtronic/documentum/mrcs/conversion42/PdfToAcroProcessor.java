package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;

public class PdfToAcroProcessor
{
	public static Map convertPdfToAcro(IDfSession session, Map stateswithrenditions, Map statesnorenditions, List doclist) throws Exception
	{
		
		// get the lookup map that has all states that have a rendition 
		List error = new ArrayList();
		List success = new ArrayList();
		
        /*-DEBUG-*/System.out.println("convertPdfToAcro- top");
		for (int i=0; i < doclist.size(); i++)
		{
        	DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
        	String curid = dor.objectid;

    		boolean tranfailed = false;
    		BaseClass.sMgr.beginTransaction();
    		try {

        		// look up object
        		IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
        		
        		dor.name = doc.getObjectName();
        		dor.path = doc.getPath(0);
        		dor.state = doc.getCurrentStateName();
        		dor.chronid = doc.getChronicleId().getId();
        		dor.format = doc.getFormat().getName();
        		dor.type = doc.getTypeName();

	    		// check it's state
	    		String statename = dor.state;
	    		// get the document from the content server
	            String filename = doc.getFile(null);
	            
	    		if (statesnorenditions.containsKey(statename))
	    		{
	    			// change format - make sure this doesn't "zero out" the content or something else weird.
	    			doc.setContentType("acro");
	    			doc.setFile(filename);
	    			dor.note = "acro";
	    	        /*-DEBUG-*/System.out.println("convertPdfToAcro-  "+new Date()+"  #"+i+" - changed format - id "+dor.objectid);
	    		} else if (stateswithrenditions.containsKey(statename)) {
		    		// clone content as rendition for approved and effective
	    			doc.setContentType("acro");
	    			doc.setFile(filename);
	    			doc.save();
	                doc.addRendition(filename,"pdf");
	    			dor.note = "acro+rendition";
	    	        /*-DEBUG-*/System.out.println("convertPdfToAcro-  "+new Date()+"  #"+i+" - rendition+changed format - id "+dor.objectid);
	    		} else {
	    	        /*-DEBUG-*/System.out.println("convertPdfToAcro- #"+i+" - No Change "+dor.objectid);
	    			dor.note = "NO CHANGE";
	    		}
    		
	    		doc.save();
    		} catch (Exception e) {
    	        /*-ERROR-*/System.out.println("convertPdfToAcro- #"+i+" - ERROR in id "+dor.objectid);e.printStackTrace();
    			dor.note = e.toString();
    			dor.error = e;
    			BaseClass.sMgr.abortTransaction();
    			error.add(dor);
    			tranfailed = true;
    		}
    		if (!tranfailed) {
    			BaseClass.sMgr.commitTransaction();
    			success.add(dor);
    		}
			
		}
				
		Map returncodes = new HashMap();
		returncodes.put("success",success);
		returncodes.put("error",error);
		
		return returncodes;
		
		
	}
	

	public static Map hackUnconvertPdfToAcro(IDfSession session, Map stateswithrenditions, Map statesnorenditions, List doclist) throws Exception
	{
		
		// get the lookup map that has all states that have a rendition 
		List error = new ArrayList();
		List success = new ArrayList();
		
		for (int i=0; i < doclist.size(); i++)
		{
        	DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
        	String curid = dor.objectid;
    		// look up object
    		IDfDocument doc = (IDfDocument)session.getObject(new DfId(curid));
    		
    		dor.name = doc.getObjectName();
    		dor.path = doc.getPath(0);
    		dor.state = doc.getCurrentStateName();
    		dor.chronid = doc.getChronicleId().getId();
    		dor.format = doc.getFormat().getName();
    		dor.type = doc.getTypeName();

    		boolean tranfailed = false;
    		BaseClass.sMgr.beginTransaction();
    		try {

	    		// check it's state
	    		String statename = dor.state;
	    		// get the document from the content server
	            String filename = doc.getFile(null);
	            
	    		if (statesnorenditions.containsKey(statename))
	    		{
	    			// change format - make sure this doesn't "zero out" the content or something else weird.
	    			doc.setContentType("pdf");
	    			doc.setFile(filename);
	    			dor.note = "pdf";
	    		}
	
	    		// clone content as rendition for approved and effective
	    		if (stateswithrenditions.containsKey(statename))
	    		{
	                doc.addRendition(filename,"pdf");
	    			doc.setContentType("acro");
	    			doc.setFile(filename);
	    			dor.note = "acro+rendition";
	    		}
    		
	    		doc.save();
    		} catch (Exception e) {
    			dor.note = e.toString();
    			dor.error = e;
    			BaseClass.sMgr.abortTransaction();
    			error.add(dor);
    			tranfailed = true;
    		}
    		if (!tranfailed) {
    			BaseClass.sMgr.commitTransaction();
    			success.add(dor);
    		}
			
		}
				
		Map returncodes = new HashMap();
		returncodes.put("success",success);
		returncodes.put("error",error);
		
		return returncodes;
		
		
	}

	
}
