package com.medtronic.documentum.mrcs.conversion42;

import java.util.List;

public class ClearErrorProcessor {
	
	public static List clearErrorMessages(List doclist)
	{
		for (int i=0; i < doclist.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
			dor.note = null; dor.error = null;
		}
		return doclist;
		
	}

}
