package com.medtronic.documentum.mrcs.introspection;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfAuthenticationException;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.DfPrincipalException;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;

// experiment/exercise with EXT-JS to convert some MrcsIntrospection to DWR+EXT

public class ExtIntrospection {

	static IDfClientX clientx = new DfClientX();
	public IDfSessionManager sMgr = null;

	public boolean authenticate(String user, String pass, String base)
	{
		IDfSession session = getSession(user,pass,base);
		if (session == null) 
			return false;
		
		sMgr.release(session);
		return true;
	}

	public List getAvailableDocbases() throws Exception
	{		
       	IDfDocbaseMap map = clientx.getDocbrokerClient().getDocbaseMap();
       	List doclist = new ArrayList();
       	for (int i =0; i < map.getDocbaseCount(); i++)
       	{
       		doclist.add(map.getDocbaseName(i));
       	}
       	return doclist;
	}

	public IDfSession getSession(String user, String pass, String base)
	{
		try {
			if (sMgr == null) {
		       	IDfClient client = clientx.getLocalClient();
		       	sMgr = client.newSessionManager();
		       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		        loginInfoObj.setUser(user);
		        loginInfoObj.setPassword(pass);
		        loginInfoObj.setDomain(null);
		        sMgr.setIdentity(base, loginInfoObj);
			}
	        return sMgr.getSession(base);
        } catch (DfIdentityException ie){
        	return null;
        } catch (DfAuthenticationException ae){
        	return null;
        } catch (DfPrincipalException pe){
        	return null;
        } catch (DfException dfe){
        	throw new RuntimeException(dfe);
        }
	}
	
	public String execDqlQuery(String query) throws Exception
	{
		// this will just be a string that we will eval on the browser. Not very DWR-ish...
		boolean first = true;
		
		StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));
		htmlbuffer.write("{"); // begin object
		IDfSession session = getSession();
		try { 
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(query);
	        
	        JSONObject griddata = new JSONObject();
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        {
	        	JSONArray jarr = new JSONArray();
	        	JSONObject zero = new JSONObject();
	        	zero.put("name","#");
	        	jarr.add(zero);
	        	
	        	for (int i=0; i < myObj1.getAttrCount(); i++)
	        	{
		        	JSONObject jobj = new JSONObject();
	        		String title = myObj1.getAttr(i).getName();
        			if (title == null) title = "&nbsp;";
        			else if ("".equals(title)) title = "&nbsp;";
        			jobj.put("name",title);
        			int dtype = myObj1.getAttr(i).getDataType();
        			if (dtype == IDfAttr.DM_DOUBLE) jobj.put("type","float");
        			jarr.add(jobj);
	        	}
	        }
	        int count = 0;
	        try {
	        JSONArray rows = new JSONArray();	
	        while (myObj1.next()) {
	        	count++;
	        	JSONArray rowdata = new JSONArray();
	        	rowdata.add(""+count);
	        	for (int i=0; i < myObj1.getAttrCount(); i++)
	        	{
	        		if (myObj1.getAttr(i).isRepeating()) {
	        			String output = "";
	        			for (int j=0; j < myObj1.getValueCount(myObj1.getAttr(i).getName()); j++)
	        			{
	        				if (first) first = false; else output += ", ";
	        				myObj1.getRepeatingValue(myObj1.getAttr(i).getName(),j);
			        		switch (myObj1.getAttr(i).getDataType()) {
			        			case IDfAttr.DM_BOOLEAN : output += myObj1.getRepeatingBoolean(myObj1.getAttr(i).getName(),j); break;  
			        			case IDfAttr.DM_DOUBLE : output += myObj1.getRepeatingDouble(myObj1.getAttr(i).getName(),j); break;  
			        			case IDfAttr.DM_ID : output += myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId(); break;  
			        			case IDfAttr.DM_INTEGER: output += myObj1.getRepeatingInt(myObj1.getAttr(i).getName(),j); break;  
			        			case IDfAttr.DM_TIME: output += myObj1.getRepeatingTime(myObj1.getAttr(i).getName(),j).asString(IDfTime.DF_TIME_PATTERN18); break;
			        			case IDfAttr.DM_STRING:
			        				// see if it's an ID anyway (ID type detection doesn't seem to work...)
			        				String value = myObj1.getRepeatingString(myObj1.getAttr(i).getName(),j);
			        				boolean isId = false;
			        				if (value.length() == 16) {
			        					try {
			        						Long.parseLong(value,16);
			        						isId = true;
			        					} catch (NumberFormatException nfe) { isId = false; }
			        				}
			        				if ("0000000000000000".equals(value)) isId = false;
			        				if (isId)
				        				output += baselink+"&id="+value+"\">"+value+"</a>";  
			        				else
			        					output += value; 
			        			    break;
			        		}
	        			}
	        			if (output == null) output = "&nbsp;";
	        			else if ("".equals(output)) output = "&nbsp;";
		        		rowhtml += "<td>"+output+"</td>";	        			
	        		} else {
		        		switch (myObj1.getAttr(i).getDataType()) {
		        			case IDfAttr.DM_BOOLEAN : rowhtml += "<td>"+myObj1.getBoolean(myObj1.getAttr(i).getName())+"</td>"; break;  
		        			case IDfAttr.DM_DOUBLE : rowhtml += "<td>"+myObj1.getDouble(myObj1.getAttr(i).getName())+"</td>"; break;  
		        			case IDfAttr.DM_ID : rowhtml += "<td>"+baselink+"&id="+myObj1.getId(myObj1.getAttr(i).getName()).getId()+"\">"+myObj1.getId(myObj1.getAttr(i).getName()).getId()+"</td>"; break;  
		        			case IDfAttr.DM_INTEGER: rowhtml += "<td>"+myObj1.getInt(myObj1.getAttr(i).getName())+"</td>"; break;  
		        			case IDfAttr.DM_TIME: rowhtml += "<td>"+myObj1.getTime(myObj1.getAttr(i).getName()).asString(IDfTime.DF_TIME_PATTERN18)+"</td>"; break;
		        			case IDfAttr.DM_STRING:
		        				// see if it's an ID anyway (ID type detection doesn't seem to work...)
		        				String value = myObj1.getString(myObj1.getAttr(i).getName());
		        				boolean isId = false;
		        				if (value.length() == 16) {
		        					try {
		        						Long.parseLong(value,16);
		        						isId = true;
		        					} catch (NumberFormatException nfe) { isId = false; }
		        				}
		        				if ("0000000000000000".equals(value)) isId = false;
		        				if (isId)
			        				rowhtml += "<td>"+baselink+"&id="+value+"\">"+value+"</td>";  
		        				else {
		    	        			if (value == null) value = "&nbsp;";
		    	        			else if ("".equals(value)) value = "&nbsp;";
		        					rowhtml += "<td>"+value+"</td>";
		        				}
		        			    break;
		        			default: rowhtml+= "--";
		        		}
	        		}
	        	}
	        	rowhtml += "</tr>";
	        	htmlbuffer.write(rowhtml);
	        }
	        } catch (Exception e) {
	        	htmlbuffer.write("</table><BR><BR><BR>ERROR during query execution: "+e+"<BR>record #: "+count+"<BR>Stacktrace:<BR><table>");
	        	e.printStackTrace(htmlbuffer);
	        } catch (Throwable t) {
	        	int a=1;
	        	a++;
	        } finally { 
	        	myObj1.close();
	        }
		} catch (Exception e) {
			htmlbuffer.write("</table>Error or Bad Query<BR>"+e.getMessage()+"<table>");
		}
        htmlbuffer.write("</table>");
        htmlbuffer.close();
		
        sMgr.release(session);
        return sw.toString();
		
	}
	
	
}
