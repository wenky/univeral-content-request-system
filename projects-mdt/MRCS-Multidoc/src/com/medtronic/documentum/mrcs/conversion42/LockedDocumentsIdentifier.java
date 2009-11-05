package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

public class LockedDocumentsIdentifier {
	public static Map findLockedDocuments(IDfSession session, List doclist) throws Exception
	{
		List lockedlist = new ArrayList();
		List unlocked = new ArrayList();
		
		// look up documents to see which are locked
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
			IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
			if (doc.isCheckedOut())
			{
				dor.note = doc.getLockOwner();
				lockedlist.add(dor);
			} else if (doc.getLockOwner() != null && !"".equals(doc.getLockOwner())) {
				dor.note = doc.getLockOwner();
				lockedlist.add(dor);
			}
			unlocked.add(dor);
		}
		
		Map returns = new HashMap();
		returns.put("locked",lockedlist);
		returns.put("unlocked",unlocked);
		
		return returns ;
		
	}

}
