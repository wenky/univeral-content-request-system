package com.medtronic.documentum.ajax;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfAttr;

public class doQuery implements GatewayPlugin
{
	public Map execute(IDfSession session, Map parameters) throws Exception
	{
		String query = (String)parameters.get("query");

		//  <script language="javascript">
		//    var columnDefs = {
		//        Invoice  : { id          : { type: "String" },
		//                     total       : { type: "Number" },	
		//                     custId      : { type: "String" } },
		//        Customer : { id          : { type: "String" },
		//                     acctBalance : { type: "Number" } }
		//    };
		//  </script>

        IDfQuery qry = new DfQuery();
        qry.setDQL(query);
        IDfCollection myObj1 = null; 
        try {
        	Map resultmap = new HashMap();
	        List columns = new ArrayList();
	    	List results = new ArrayList();
	    	Map tqmetadata = new HashMap();
	    	resultmap.put("columns",columns);
	    	resultmap.put("metadata",tqmetadata);
	    	resultmap.put("results",results);
        	try { 
        		myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
        	} catch (NullPointerException npe) {
        		// empty result set == NullPointerException, another sterling design decision by DCTM
        		return resultmap;
        	}
            // get metadata and trimquery column definitions
	    	for (int i=0; i < myObj1.getAttrCount(); i++)
	    	{
	    		Map columndata = new HashMap();
	    		columndata.put("name",myObj1.getAttr(i).getName());
	    		columndata.put("datatype",new Integer(myObj1.getAttr(i).getDataType()));	    		
	    		columndata.put("repeating",new Boolean(myObj1.getAttr(i).isRepeating()));	    		
	    		columns.add(columndata);
	    		HashMap tqmetacol = new HashMap();
	    		tqmetacol.put("type","String");
	    		tqmetadata.put(myObj1.getAttr(i).getName(),tqmetacol);
	    	}
	    	// get result rows
	        while (myObj1.next()) {
	        	Map row = new HashMap();
	        	for (int i=0; i < myObj1.getAttrCount(); i++)
	        	{
	        		if (myObj1.getAttr(i).isRepeating()) {
	        			List repeatingvalues = new ArrayList();
	        			for (int j=0; j < myObj1.getValueCount(myObj1.getAttr(i).getName()); j++)
	        			{
	        				myObj1.getRepeatingValue(myObj1.getAttr(i).getName(),j);
			        		switch (myObj1.getAttr(i).getDataType()) {
			        			case IDfAttr.DM_BOOLEAN : repeatingvalues.add(new Boolean(myObj1.getRepeatingBoolean(myObj1.getAttr(i).getName(),j))); break;  
			        			case IDfAttr.DM_DOUBLE  : repeatingvalues.add(new Double(myObj1.getRepeatingDouble(myObj1.getAttr(i).getName(),j))); break;  
			        			case IDfAttr.DM_ID      : repeatingvalues.add(myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId()); break;  
			        			case IDfAttr.DM_INTEGER : repeatingvalues.add(new Integer(myObj1.getRepeatingInt(myObj1.getAttr(i).getName(),j))); break;  
			        			case IDfAttr.DM_TIME    : repeatingvalues.add(myObj1.getRepeatingTime(myObj1.getAttr(i).getName(),j).getDate()); break;
			        			case IDfAttr.DM_STRING  : repeatingvalues.add(myObj1.getRepeatingString(myObj1.getAttr(i).getName(),j)); break;
			        		}
	        			}
	        			row.put(((Map)columns.get(i)).get("name"),repeatingvalues);
	        		} else {
		        		switch (myObj1.getAttr(i).getDataType()) {
		        			case IDfAttr.DM_BOOLEAN : row.put(((Map)columns.get(i)).get("name"),new Boolean(myObj1.getBoolean(myObj1.getAttr(i).getName()))); break;  
		        			case IDfAttr.DM_DOUBLE  : row.put(((Map)columns.get(i)).get("name"),new Double(myObj1.getDouble(myObj1.getAttr(i).getName()))); break;  
		        			case IDfAttr.DM_ID      : row.put(((Map)columns.get(i)).get("name"),myObj1.getId(myObj1.getAttr(i).getName()).getId()); break;  
		        			case IDfAttr.DM_INTEGER : row.put(((Map)columns.get(i)).get("name"),new Integer(myObj1.getInt(myObj1.getAttr(i).getName()))); break;  
		        			case IDfAttr.DM_TIME    : row.put(((Map)columns.get(i)).get("name"),myObj1.getTime(myObj1.getAttr(i).getName()).getDate()); break;
		        			case IDfAttr.DM_STRING  : row.put(((Map)columns.get(i)).get("name"),myObj1.getString(myObj1.getAttr(i).getName())); break;
		        		}
	        		}
	        	}
	        	results.add(row);
	        }
	        myObj1.close();
	    	
	    	return resultmap;
        } finally {if (myObj1 != null) myObj1.close();}
		
	}

}
