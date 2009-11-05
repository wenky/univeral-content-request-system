package com.medtronic.documentum.mrcs.server.interfaces;

import com.documentum.fc.client.IDfDocument;

public interface IMrcsPromotionService 
{
	// don't know if we can pass the actual object...might need to pass the docid, docbase, and mrcsapp, and reconstruct a session and retrieve the doc that way...
	public void promoteMrcsDocument(IDfDocument mrcsdocument);
}
