package com.medtronic.documentum.mrcs.conversion42;

import java.util.List;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

public class DocumentStateIdentifier {
	
	public static List populateDocumentState(IDfSession session,List doclist) throws Exception
	{
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
			IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
			dor.name = doc.getObjectName();
			dor.state = doc.getCurrentStateName();
			dor.path = ((IDfFolder)session.getObject(doc.getFolderId(0))).getFolderPath(0);
			//dor.path = doc.getPath(0);
			dor.lifecycle = doc.getPolicyName();
			dor.chronid = doc.getChronicleId().getId();
		}
		return doclist;
	}

}
