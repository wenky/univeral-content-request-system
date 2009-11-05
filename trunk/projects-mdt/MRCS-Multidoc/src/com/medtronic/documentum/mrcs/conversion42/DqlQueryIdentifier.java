package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;

// a general utility class that takes a DQL query and outputs the result
// - assumes the DQL query has the r_object_id as a column in the result set

public class DqlQueryIdentifier 
{

	public static List executeQuery(IDfSession session, String dql)
	{
		
		List output = new ArrayList();
        /*-DEBUG-*/System.out.println("executeQuery- top");
		
		try { 
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	String curid = myObj1.getString("r_object_id");
	        	DocbaseObjectRecord docobject = new DocbaseObjectRecord();
	        	docobject.objectid = curid;
	        	output.add(docobject);
	            /*-DEBUG-*/System.out.println("executeQuery- "+new Date()+"add id "+docobject.objectid);
	        }
	        	
	        myObj1.close();
		} catch (DfException dfe) {
			throw new RuntimeException(dfe);
		}
        /*-DEBUG-*/System.out.println("executeQuery- done");
		
		return output;
	}
	
	// assumes query has:
	// - r_object_id, object_name
	public static List executeNameIdQuery(IDfSession session, String dql)
	{
		List output = new ArrayList();
		
		try { 
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	String curid = myObj1.getString("r_object_id");
	        	DocbaseObjectRecord docobject = new DocbaseObjectRecord();
	        	docobject.objectid = curid;
	        	docobject.name = myObj1.getString("object_name");
	        	output.add(docobject);
	        }
	        	
	        myObj1.close();
		} catch (DfException dfe) {
			throw new RuntimeException(dfe);
		}
		
		return output;
	}
	
	

}
