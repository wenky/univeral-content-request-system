package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

public class SetAllRetObsFlagsOnAllVersions 
{
	public static void main(String[] args)
	{
		IDfSession session = BaseClass.getSession();
		Map results = processObsoleteAndRetired(session);
		BaseClass.sMgr.release(session);
		
		List success = (List)results.get("success");
		List error = (List)results.get("error");
		List input = (List)results.get("input");
		
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-touchallRETOBS-INPUT["+input.size()+"].xml",input);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-touchallRETOBS-SUCCESS["+success.size()+"].xml",success);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-touchallRETOBS-ERROR["+error.size()+"].xml",error);
	}
	
	public static Map processObsoleteAndRetired(IDfSession session)
	{
		// look up ALL versions of ALL m_mrcs_document
        /*-DEBUG-*/System.out.println("processObsoleteAndRetired- top");
		List doclist = DqlQueryIdentifier.executeNameIdQuery(session,"SELECT r_object_id, object_name FROM m_mrcs_document(ALL)");
		
        List success = new ArrayList();
        List error = new ArrayList();
		
		// iterate
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
	        /*-DEBUG-*/System.out.println("processObsoleteAndRetired- "+new Date()+"processing "+i+" objid "+dor.objectid);
			try { 
				IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
				doc.setBoolean("obsolete",doc.getBoolean("obsolete"));
				doc.setBoolean("retired",doc.getBoolean("retired"));
				doc.save();
	        	success.add(dor);				
			} catch (Exception e) {
		        /*-ERROR-*/System.out.println("processObsoleteAndRetired- ERROR for id "+dor.objectid);e.printStackTrace();
				dor.error = e;
				dor.note = e.getMessage();
				error.add(dor);
			}
		}		
        /*-DEBUG-*/System.out.println("processObsoleteAndRetired- done");
		Map results = new HashMap();
		results.put("input",doclist);
		results.put("success",success);
		results.put("error",error);
		return results;
	}
	

}
