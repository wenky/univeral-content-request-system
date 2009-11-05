package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

public class AllVersionsIdentifier {

	// getting all versions of documents in supplied document list
	public static List getAllVersions(IDfSession session, List doclist) throws Exception
	{
		ArrayList allversions = new ArrayList();
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
			IDfDocument doc = (IDfDocument)session.getObject(new DfId(dor.objectid));
			dor.version = doc.getVersionLabel(0);
			while (true)
			{
				allversions.add(dor);
				if (doc.getObjectId().equals(doc.getChronicleId()))
				{
					break;
				} else {
					doc = (IDfDocument)session.getObject(doc.getAntecedentId());
					DocbaseObjectRecord newdor = new DocbaseObjectRecord();
					newdor.name = dor.name;
					newdor.version = doc.getVersionLabel(0);
					newdor.chronid = dor.chronid;
					newdor.state = doc.getCurrentStateName();
					dor = newdor;
				}
			} // loop
		}
		
		return allversions;
	}

}
