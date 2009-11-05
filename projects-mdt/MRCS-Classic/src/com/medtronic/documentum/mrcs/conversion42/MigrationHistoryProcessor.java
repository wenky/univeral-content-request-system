package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

public class MigrationHistoryProcessor {

	public static Map setMigrationHistory(IDfSession session, List doclist, String historyvalue) throws Exception
	{
		List error = new ArrayList();
		List success = new ArrayList();
		
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
			try {
				IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
				doc.setRepeatingString("mrcs_migration_history",0,historyvalue);
				doc.save();
				success.add(dor);
			} catch (Exception e) {
				dor.note = e.toString();
				dor.error = e;
				error.add(dor);
			}
		}
		
		HashMap results = new HashMap();
		results.put("error",error);
		results.put("success",success);
		
		return results;
		
		
	}

}
