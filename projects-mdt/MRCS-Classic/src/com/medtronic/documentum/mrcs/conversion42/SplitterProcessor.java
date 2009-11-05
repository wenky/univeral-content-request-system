package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

// a utility processor to split a long list of IDs into files that are more manageable from a transaction perspective.

public class SplitterProcessor 
{
	
	public static Map splitList(int splitsize, List doclist) throws Exception
	{
		
		int pagecounter = 0;
		int splitcounter = 0;
		Map results = new HashMap();
		List newfile = null;
		for (int i=0; i < doclist.size(); i++)
		{
			if (splitcounter==0)
			{
				newfile = new ArrayList();
				pagecounter++;
			} 
			newfile.add(doclist.get(i));
			if (splitcounter == splitsize)
			{
				results.put("output"+pagecounter,newfile);
				splitcounter = 0;
			} else {
				splitcounter++;
			}
		}
		results.put("output"+pagecounter,newfile);
		
		return results;
		
	}
	

}
