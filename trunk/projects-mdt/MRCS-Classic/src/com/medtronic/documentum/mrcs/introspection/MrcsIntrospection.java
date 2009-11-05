package com.medtronic.documentum.mrcs.introspection;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocbaseMap;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfPackage;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.client.IDfVersionLabels;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;

// helper class to generate and process dumps for introspeciton of MRCS...

public class MrcsIntrospection
{
	String username = null, password = null, docbase = null;
	String baseurl = null;
    IDfClientX clientx = new DfClientX();
	IDfSessionManager sMgr;
   	IDfClient client;
	
	
	public String getHeader()
	{
		String url = baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+(username==null?"":username)+"&password="+(password==null?"":password)+"&docbase="+(docbase==null?"":docbase);				
		
		String header = "<a href='"+url+"&page=docsearchbyname'>DocByName</a>";
		header+= " - <a href='"+url+"&page=docsearchbyid'>DocById</a>";
		header+= " - <a href='"+url+"&page=wfsearchbypkgname'>WorkflowByPkgName</a>";
		header+= " - <a href='"+url+"&page=activeworkflows'>WorkflowReport</a>";
		header+= " - <a href='"+url+"&page=activeworkflowsandtasks'>WorkflowTaskReport</a>";
		header+= " - <a href='"+url+"&page=activeworkflowauditevents'>WorkflowAuditReport</a>";
		header+= " - <a href='"+url+"&page=msdcasactiveworkflows'>CASrpt</a>";
		header+= " - <a href='"+url+"&page=osnameinbox'>InboxByOSName</a>";
		header+= " - <a href='"+url+"&page=userinbox'>InboxByUser</a>";
		header+= " - <a href='"+url+"&page=cabinetlist'>Cabinet List</a>";
		header+= " - <a href='"+url+"&page=querypage'>DQL query</a>";
		header+= " - <a href='"+url+"&page=apigetpage'>API-get</a>";
		header+= " - <a href='"+url+"&page=basiclogin'>Change Login</a>";
		header+= " - User: "+username+" Docbase: "+docbase+"<hr>";
		return header;
	}
	
	public String perform(HttpServletRequest request) throws Exception
	{
		username = request.getParameter("username");
		password = request.getParameter("password");
		docbase = request.getParameter("docbase");
		
		baseurl = request.getServletPath().substring(1);
				
		String page = request.getParameter("page");
		
		if ("home".equals(page)) 
			return getHeader(); 
		if ("dm_document".equals(page)) 
			return dumpDocumentById(request); 
		if ("dm_document_byname".equals(page)) 
			return dumpDocumentByName(request); 
		if ("objdump".equals(page)) 
			return dumpObject(request);
		if ("mrcs_application".equals(page))
			return dumpMrcsApplication(request);
		if ("mrcs_document_config".equals(page))
			return dumpMrcsDocumentType(request);
		if ("mrcs_folder_config".equals(page))
			return dumpMrcsFolderType(request);
		if ("documentworkflow".equals(page))
			return dumpDocumentWorkflowListById(request);
		if ("documentworkflowbyname".equals(page))
			return dumpDocumentWorkflowListByName(request);
		if ("dm_relation".equals(page))
			return relationshipList(request);
		if ("dm_childrelation".equals(page))
			return childRelationshipList(request);
		if ("documentversiontree".equals(page))
			return documentVersionList(request);
		if ("docsearchbyname".equals(page))
			return documentSearchByNamePage(request);
		if ("docsearchbyid".equals(page))
			return documentSearchById(request);
		if ("wfsearchbypkgname".equals(page))
			return workflowSearchByDocName(request);
		if ("basiclogin".equals(page))
			return loginPage(request);
		if ("cabinetlist".equals(page))
			return listCabinets(request);
		if ("querypage".equals(page))
			return getQueryPage(request);
		if ("execquery".equals(page))
			return doDqlQuery(request);
		if ("apigetpage".equals(page))
			return apiGetPage(request);
		if ("apiget".equals(page))
			return doApiGet(request);
		if ("userinbox".equals(page))
			return userInboxPage(request);
		if ("douserinbox".equals(page))
			return doUserInbox(request);
		if ("osnameinbox".equals(page))
			return osNamePage(request);
		if ("doosnameinbox".equals(page))
			return doOSNameInbox(request);
		if ("modifyattrs".equals(page))
			return manipulateAttributes(request);
		if ("activeworkflows".equals(page))
			return activeWorkflowReport(request);
		if ("activeworkflowsandtasks".equals(page))
			return activeWorkflowTaskReport(request);
		if ("activeworkflowauditevents".equals(page))
			return activeWorkflowAuditReport(request);
		if ("msdcasactiveworkflows".equals(page))
			//return msdcasTaskReport(request);
			return msdcasHistoricalWorkflowReport(request);
			
		return "";
	}
	
	public String manipulateAttributes(HttpServletRequest request) throws Exception
	{
		// NOT WELL TESTED!!!!! 
		String html = getHeader();
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=modifyattrs";
		String baselink2 = baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=modifyattrs";
		
		// get attribute list from object id
		String objectid = request.getParameter("objectid");
		
		IDfSession session = this.getSession();
		try {
			IDfSysObject sysobj = (IDfSysObject)session.getObject(new DfId(objectid));
			String selectedattr = request.getParameter("attr");
			

			// modification operation?
			String operation = request.getParameter("operation");
			try { 
				if (operation != null)
				{				
					if ("appendvalue".equals(operation)) {
						String newvalue = request.getParameter("newvalue");
						IDfAttr attr = sysobj.getAttr(sysobj.findAttrIndex(selectedattr));
						switch(attr.getDataType())
						{
							case IDfType.DF_BOOLEAN: boolean val = Boolean.valueOf(newvalue).booleanValue(); sysobj.appendBoolean(selectedattr,val); sysobj.save();  html += "appended Boolean value "+val+" to repeating attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_DOUBLE: double d = Double.parseDouble(newvalue); sysobj.appendDouble(selectedattr,d); sysobj.save();  html += "appended double value "+d+" to repeating attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_ID: sysobj.appendId(selectedattr,new DfId(newvalue)); sysobj.save();  html += "appended DFID value "+newvalue+" to repeating attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_INTEGER: sysobj.appendInt(selectedattr,Integer.parseInt(newvalue)); sysobj.save();  html += "appended integer value "+newvalue+" to repeating attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_STRING: sysobj.appendString(selectedattr,newvalue); sysobj.save();  html += "appended string value "+newvalue+" to repeating attribute "+selectedattr+"<BR>"; break; 
							case IDfType.DF_TIME: DfTime dft = new DfTime(newvalue); sysobj.appendTime(selectedattr,dft); html += "appended time value "+newvalue+" to repeating attribute "+dft.asString(DfTime.DF_TIME_PATTERN_DEFAULT)+"<BR>"; break;
						}
					}
					if ("removevalue".equals(operation)) {
						IDfAttr attr = sysobj.getAttr(sysobj.findAttrIndex(selectedattr));
						int index = Integer.parseInt(request.getParameter("index"));
						sysobj.remove(selectedattr,index);
						sysobj.save();
					}
					if ("setvalue".equals(operation)) {
						String newvalue = request.getParameter("newvalue");
						IDfAttr attr = sysobj.getAttr(sysobj.findAttrIndex(selectedattr));
						switch(attr.getDataType())
						{
							case IDfType.DF_BOOLEAN: boolean val = Boolean.valueOf(newvalue).booleanValue(); sysobj.setBoolean(selectedattr,val); sysobj.save();  html += "set Boolean value "+val+" to attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_DOUBLE: double d = Double.parseDouble(newvalue); sysobj.setDouble(selectedattr,d); sysobj.save();  html += "SET double value "+d+" to attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_ID: sysobj.setId(selectedattr,new DfId(newvalue)); sysobj.save();  html += "set DFID value "+newvalue+" to attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_INTEGER: sysobj.setInt(selectedattr,Integer.parseInt(newvalue)); sysobj.save();  html += "SET integer value "+newvalue+" to attribute "+selectedattr+"<BR>"; break;
							case IDfType.DF_STRING: sysobj.setString(selectedattr,newvalue); sysobj.save();  html += "set string value "+newvalue+" to attribute "+selectedattr+"<BR>"; break; 
							case IDfType.DF_TIME: DfTime dft = new DfTime(newvalue); sysobj.setTime(selectedattr,dft); html += "SET time value "+newvalue+" to attribute "+dft.asString(DfTime.DF_TIME_PATTERN_DEFAULT)+"<BR>"; break;
						}
					}
				}
			}catch (Exception e) {
				html += "Exception in attribute operation - probably an immutable attribute: "+e+"<BR>";
			}
			
			//get attr list
			Enumeration attrs = sysobj.enumAttrs();
			List attrlist = new ArrayList();
			String selectorlist = "<FORM><SELECT name=\"attrlist\" onchange=\"location.href=\'"+baselink2+"&objectid="+objectid+"&operation=displayattr&attr=\'+form.attrlist.options[form.attrlist.selectedIndex].value\">";
			while (attrs.hasMoreElements())
			{
				IDfAttr attr = (IDfAttr)attrs.nextElement();
				attrlist.add(attr);
			}
			Collections.sort(attrlist, new Comparator() { public int compare(Object o1, Object o2) { return ((IDfAttr)o1).getName().compareTo(((IDfAttr)o2).getName());}});
			for (int i=0; i < attrlist.size(); i++)
			{
				IDfAttr attr = (IDfAttr)attrlist.get(i);
				String selected = (attr.getName().equals(selectedattr) ? " SELECTED" : "");
				selectorlist += "<OPTION value='"+attr.getName()+"'"+selected+">"+attr.getName();
				switch(attr.getDataType())
				{
					case IDfType.DF_BOOLEAN: selectorlist += " - boolean"; break;
					case IDfType.DF_DOUBLE: selectorlist += " - double"; break;
					case IDfType.DF_ID: selectorlist += " - object id"; break;
					case IDfType.DF_INTEGER: selectorlist += " - integer"; break;
					case IDfType.DF_STRING: selectorlist += " - char["+attr.getLength()+"]";break; 
					case IDfType.DF_TIME: selectorlist += " - time"; break;
				}
				if (attr.isRepeating()) selectorlist += " - REPEATING";
			}
			selectorlist += "</SELECT></FORM>";
			html += selectorlist;
			
			if (selectedattr != null)
			{
				// display attr value
				IDfAttr attr = sysobj.getAttr(sysobj.findAttrIndex(selectedattr));
				html+= "type: ";
				switch(attr.getDataType())
				{
					case IDfType.DF_BOOLEAN: html += "boolean"; break;
					case IDfType.DF_DOUBLE: html+= "double"; break;
					case IDfType.DF_ID: html+= "object id"; break;
					case IDfType.DF_INTEGER: html+= "integer"; break;
					case IDfType.DF_STRING: html+= "char["+attr.getLength()+"]";break; 
					case IDfType.DF_TIME: html+= "time"; break;
				}
				html +="<BR>";
				
				if (attr.isRepeating())
				{
					for (int j=0; j < sysobj.getValueCount(selectedattr); j++)
					{
						html += baselink+"&objectid="+objectid+"&attr="+selectedattr+"&operation=removevalue&index="+j+"\">X</a> - ";
						html += "value #"+j+": ";
						switch(attr.getDataType())
						{
							case IDfType.DF_BOOLEAN: html += sysobj.getRepeatingBoolean(selectedattr,j); break;
							case IDfType.DF_DOUBLE: html+= sysobj.getRepeatingDouble(selectedattr,j); break;
							case IDfType.DF_ID: html+= sysobj.getRepeatingId(selectedattr,j).getId(); break;
							case IDfType.DF_INTEGER: html+= sysobj.getRepeatingInt(selectedattr,j); break;
							case IDfType.DF_STRING: html+= sysobj.getRepeatingString(selectedattr,j);break; 
							case IDfType.DF_TIME: html+= sysobj.getRepeatingTime(selectedattr,j).toString(); break;
						}
						html += "<BR>";
					}
					html+="<FORM METHOD=\"POST\" ACTION=\""+baselink2+"&objectid="+objectid+"&attr="+selectedattr+"&operation=appendvalue\">Add Value: <input type=\"text\" name=\"newvalue\"> <input type=\"submit\" value=\"add value\"></FORM>";
					
				} else {
					html += "<FORM METHOD=\"POST\" ACTION=\""+baselink2+"&objectid="+objectid+"&attr="+selectedattr+"&operation=setvalue\">value : <input name=\"newvalue\" type=\"text\" value=\"";
					switch(attr.getDataType())
					{
						case IDfType.DF_BOOLEAN: html += sysobj.getBoolean(selectedattr); break;
						case IDfType.DF_DOUBLE: html+= sysobj.getDouble(selectedattr); break;
						case IDfType.DF_ID: html+= sysobj.getId(selectedattr).getId(); break;
						case IDfType.DF_INTEGER: html+= sysobj.getInt(selectedattr); break;
						case IDfType.DF_STRING: html+= sysobj.getString(selectedattr);break; 
						case IDfType.DF_TIME: html+= sysobj.getTime(selectedattr).toString(); break;
					}
					html+="\"> <input type=\"submit\" value=\"set value\"></FORM>";
					
				}
				
				
			}
			
		
			String dump = this.processDump(objectid);
			html +="<hr>"+dump;
		} catch (Exception e) {
			html = "Error in manipulate attributes page "+e;
		} finally {
			sMgr.release(session);
		}
		return html;
		
	}

	public String listCabinets(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=objdump";				
		String tracedqlcabinet = "select upper(object_name),r_object_id,object_name,r_object_type,owner_name,i_is_replica,i_is_reference  from dm_cabinet where (is_private=0) and a_is_hidden=false Union select upper(object_name),r_object_id,object_name,r_object_type,owner_name,i_is_replica,i_is_reference  from dm_cabinet where (owner_name=USER) and a_is_hidden=false order by 1";
		
		IDfSession session = this.getSession();

		html += "cabinet list for docbase: "+docbase+"<br>";
		try {
			String dql = "select upper(object_name),r_object_id,object_name,r_object_type,owner_name,i_is_replica,i_is_reference  from dm_cabinet where (is_private=0) and a_is_hidden=false Union select upper(object_name),r_object_id,object_name,r_object_type,owner_name,i_is_replica,i_is_reference  from dm_cabinet where (owner_name=USER) and a_is_hidden=false order by 1";
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	IDfId objid = myObj1.getId("r_object_id");
	        	String objlink = baselink + "&id="+objid.getId()+"\">"+myObj1.getString("object_name")+"</a><br>";
	        	html += objlink;
	        }
	        myObj1.close();
		} catch (Exception e) {
			html += "Document Version Tree Error ";
		} finally {
			sMgr.release(session);
		}
		return html;
	}

	public String loginPage(HttpServletRequest request) throws Exception
	{
		String html = "";
        html += "Introspect document - enter docname";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='text' name='username' value='"+(username == null?"":username)+"'></P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='password' name='password' value='"+(password == null?"":password)+"'></P>";
        html += "    <P>docbase:&nbsp;&nbsp;<SELECT name='docbase'>";
       	IDfDocbaseMap map = clientx.getDocbrokerClient().getDocbaseMap();
       	for (int i =0; i < map.getDocbaseCount(); i++)
       	{
       		String curbase = map.getDocbaseName(i);
       		html += "<option value='"+curbase+"'"+(curbase.equals(docbase) ? " SELECTED":"")+">"+curbase+"</option>";
       	}
       	html += "</SELECT></P>";
        html += "    <INPUT type='hidden' name='page' value='home'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        html += "<hr>Available docbases:<BR>";
        return html;
	}

	public String userInboxPage(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "View User's Inbox - enter user:";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>UserName:&nbsp;&nbsp;<INPUT type='text' name='user' ></P>";
        html += "    <INPUT type='hidden' name='page' value='douserinbox'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}
	
	public String doUserInbox(HttpServletRequest request) throws Exception
	{
		// compute union of username plus groups
		String user = request.getParameter("user");
		
		IDfSession session = this.getSession();

		String html = getHeader();
		String list = "'"+user+"'";
		try {
			String dql = "SELECT ALL group_name FROM dm_group WHERE ANY i_all_users_names = '"+user+"'";
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	list += ",'"+myObj1.getString("group_name")+"'";
	        }
	        myObj1.close();
		} catch (Exception e) {
			html+="User Not Found: "+user;
			sMgr.release(session);
			return html;
		}
		sMgr.release(session);
		String getqueueitemsdql  = "SELECT task_name,content_type,date_sent,dependency_type,due_date,event,item_id,item_name,item_type,priority,r_object_id,router_id,sent_by,task_subject,task_number,task_state,source_docbase,source_event FROM dmi_queue_item WHERE ( name IN ("+list+") ) AND delete_flag=0  ORDER BY 3 DESC";
		html += generateQueryTable(getqueueitemsdql);
		return html;
	}

	public String osNamePage(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "View User's Inbox - enter user:";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>UserName:&nbsp;&nbsp;<INPUT type='text' name='user' ></P>";
        html += "    <P>Domain:&nbsp;&nbsp;<INPUT type='text' name='domain' ></P>";
        html += "    <INPUT type='hidden' name='page' value='doosnameinbox'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}
	
	public String doOSNameInbox(HttpServletRequest request) throws Exception
	{
		// compute union of username plus groups
		String osname = request.getParameter("user");
		String domain = request.getParameter("domain");
		
		IDfSession session = this.getSession();

		String user = session.getUserByOSName(osname,domain).getUserName();
		String html = getHeader();
		String list = "'"+user+"'";
		try {
			String dql = "SELECT ALL group_name FROM dm_group WHERE ANY i_all_users_names = '"+user+"'";
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	list += ",'"+myObj1.getString("group_name")+"'";
	        }
	        myObj1.close();
		} catch (Exception e) {
			html+="User Not Found: "+user;
			sMgr.release(session);
			return html;
		}
		sMgr.release(session);
		String getqueueitemsdql  = "SELECT task_name,content_type,date_sent,dependency_type,due_date,event,item_id,item_name,item_type,priority,r_object_id,router_id,sent_by,task_subject,task_number,task_state,source_docbase,source_event FROM dmi_queue_item WHERE ( name IN ("+list+") ) AND delete_flag=0  ORDER BY 3 DESC";
		html += generateQueryTable(getqueueitemsdql);
		return html;
	}

	public String apiGetPage(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "Introspect document - enter docname";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>get cmd:&nbsp;&nbsp;<INPUT type='text' name='cmd' ></P>";
        html += "    <P>ARGS:&nbsp;&nbsp;<INPUT type='text' name='args' ></P>";
        html += "    <INPUT type='hidden' name='page' value='apiget'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}

	public String doApiGet(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
		IDfSession session = this.getSession();
		String cmd = request.getParameter("cmd");
		String args = request.getParameter("args");
		html += "API cmd: "+cmd+"<br>";
		html += "API args: "+args+"<br>";
		try { 
			String result = session.apiGet(cmd,args);
			html += "result:"+"<hr>"+result;
		} catch (Exception e) {
			html += "Error in cmd execute<br>";
		}
		sMgr.release(session);
		return html;
	}

	
	public String documentSearchByNamePage(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "Introspect document - enter docname";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>docname:&nbsp;&nbsp;<INPUT type='text' name='name'></P>";
        html += "    <INPUT type='hidden' name='page' value='dm_document_byname'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}

	public String getQueryPage(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "Exec DQL - enter query";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>query:&nbsp;&nbsp;<TEXTAREA name='query' cols=100 rows=10></TEXTAREA></P>";
        html += "    <INPUT type='hidden' name='page' value='execquery'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}

	public String workflowSearchByDocName(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "Find workflows - enter docname";
        html += "<FORM name='DumpDoc' action='"+baseurl+"p' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>docname:&nbsp;&nbsp;<INPUT type='text' name='name'></P>";
        html += "    <INPUT type='hidden' name='page' value='documentworkflowbyname'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}
	

	public String documentSearchById(HttpServletRequest request) throws Exception
	{
		String html = getHeader();
        html += "Introspect document - enter docid";
        html += "<FORM name='DumpDoc' action='"+baseurl+"' method='GET'>";
        html += "    <INPUT type='hidden' name='FILLERARG' value='alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj'>";
        html += "    <P>username:&nbsp;&nbsp;<INPUT type='hidden' name='username' value='"+(username == null?"":username)+"'>"+username+"</P>";
        html += "    <P>password:&nbsp;&nbsp;<INPUT type='hidden' name='password' value='"+(password == null?"":password)+"'>password</P>";
        html += "    <P>docbase:&nbsp;&nbsp;<INPUT type='hidden' name='docbase' value='"+(docbase == null?"":docbase)+"'>"+docbase+"</P>";
        html += "    <P>doc id:&nbsp;&nbsp;<INPUT type='text' name='id'></P>";
        html += "    <INPUT type='hidden' name='page' value='dm_document'/>";
        html += "    <INPUT type='submit' name='go' value='go'/></FORM>";
        return html;
	}
	
	
	
	public String relationshipList(HttpServletRequest request) throws Exception
	{
		
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=objdump";				
		String docid = request.getParameter("id");
		
		IDfSession session = this.getSession();

		String html = getHeader();
		IDfSysObject doc = (IDfSysObject)session.getObject(new DfId(docid));
		html += "PARENT Relationship list for object..."+"<br>";
		html += "Object Name: "+doc.getObjectName()+"<br>";
		html += "Object ID: "+docid+"<br>";
		html += "Version: "+doc.getVersionLabels().getImplicitVersionLabel()+"<br>";
		try {
			String dql = "SELECT r_object_id, parent_id, child_id, relation_name from dm_relation where parent_id = '"+docid+"' order by r_object_id desc";
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        html += "<table border=1><tr><td><b>relation id</b></td><td><b>parent docid</b></td><td><b>child docid</b></td><td><b>relation</b></td></tr>";
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	String objlink = baselink + "&id="+myObj1.getId("r_object_id")+"\">"+myObj1.getId("r_object_id")+"</a>"; 
	        	String parlink = baselink + "&id="+myObj1.getId("parent_id")+"\">"+myObj1.getId("parent_id")+"</a>"; 
	        	String chlink = baselink + "&id="+myObj1.getId("child_id")+"\">"+myObj1.getId("child_id")+"</a>"; 
	        	html += "<tr><td>"+objlink+"</td><td>"+parlink+"</td><td>"+chlink+"</td><td>"+myObj1.getString("relation_name")+"</td></tr>";
	        }
	        html +="</table>";
	        myObj1.close();
		} catch (Exception e) {
			html += "Relationship Error ";
		}
		
		sMgr.release(session);
		
		return html;
	}

	public String childRelationshipList(HttpServletRequest request) throws Exception
	{
		
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=objdump";				
		String docid = request.getParameter("id");
		
		IDfSession session = this.getSession();

		String html = getHeader();
		IDfSysObject doc = (IDfSysObject)session.getObject(new DfId(docid));
		html += "CHILD Relationship list for object..."+"<br>";
		html += "Object Name: "+doc.getObjectName()+"<br>";
		html += "Object ID: "+docid+"<br>";
		html += "Version: "+doc.getVersionLabels().getImplicitVersionLabel()+"<br>";
		try {
			String dql = "SELECT r_object_id, parent_id, child_id, relation_name from dm_relation where child_id = '"+docid+"' order by r_object_id desc";
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        html += "<table border=1><tr><td><b>relation id</b></td><td><b>parent docid</b></td><td><b>child docid</b></td><td><b>relation</b></td></tr>";
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	String objlink = baselink + "&id="+myObj1.getId("r_object_id")+"\">"+myObj1.getId("r_object_id")+"</a>"; 
	        	String parlink = baselink + "&id="+myObj1.getId("parent_id")+"\">"+myObj1.getId("parent_id")+"</a>"; 
	        	String chlink = baselink + "&id="+myObj1.getId("child_id")+"\">"+myObj1.getId("child_id")+"</a>"; 
	        	html += "<tr><td>"+objlink+"</td><td>"+parlink+"</td><td>"+chlink+"</td><td>"+myObj1.getString("relation_name")+"</td></tr>";
	        }
	        html +="</table>";
	        myObj1.close();
		} catch (Exception e) {
			html += "Relationship Error ";
		}
		
		sMgr.release(session);
		
		return html;
	}

	public String documentVersionList(HttpServletRequest request) throws Exception
	{
		
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=objdump";				
		String docid = request.getParameter("id");
		
		IDfSession session = this.getSession();

		String html = getHeader();
		IDfSysObject doc = (IDfSysObject)session.getObject(new DfId(docid));
		html += "Version Tree for document: "+doc.getObjectName()+"<br>";
		html += "Document ID: "+docid+"<br>";
		html += "Version: "+doc.getVersionLabels().getImplicitVersionLabel()+"<br>";
		try {
			String dql = "SELECT r_object_id FROM dm_document(ALL) where i_chronicle_id = '"+doc.getChronicleId().getId()+"' order by r_object_id desc";
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        html += "<table border=1><tr><td><b>objid</b></td><td><b>version</b></td><td><b>modified</b></td></tr>";
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	IDfId objid = myObj1.getId("r_object_id");
	        	IDfDocument curdoc = (IDfDocument)session.getObject(objid);
	        	
	        	String objlink = baselink + "&id="+objid.getId()+"\">"+myObj1.getId("r_object_id")+"</a>";
	    		String lockowner = doc.getLockOwner();
	        	html += "<tr><td>"+objlink+"</td><td>"+curdoc.getVersionLabels().getImplicitVersionLabel()+(!"".equals(lockowner)?" (Locked by "+lockowner+")":"")+"</td><td>"+curdoc.getModifier()+" ("+curdoc.getModifyDate().asString(IDfTime.	DF_TIME_PATTERN18)+")"+"</td></tr>";	        	
	        }
	        html +="</table>";
	        myObj1.close();
		} catch (Exception e) {
			html += "Document Version Tree Error ";
		}
		
		sMgr.release(session);
		
		return html;
	}

	public String dumpDocumentWorkflowListByName(HttpServletRequest request) throws Exception
	{
		IDfSession session = this.getSession();
		String docname = request.getParameter("name");
		IDfPersistentObject doc = null;
		try {
			doc = session.getObjectByQualification("dm_sysobject where object_name = '"+docname+"'");
		} catch (Exception e) {}
		sMgr.release(session);
		String header = dumpDocumentWorkflowList(doc.getObjectId().getId());
		return getHeader()+header+"<hr>";
	}
	
	

	public String dumpDocumentWorkflowListById(HttpServletRequest request) throws Exception	
	{
		return (getHeader()+dumpDocumentWorkflowList(request.getParameter("id")));
	}
	
	public String dumpDocumentWorkflowList(String docid) throws Exception
	{
		
		String baselink = " <a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;				
		
		IDfSession session = this.getSession();
		
		// get list of workflows that have this as a package
		// get document
		IDfSysObject obj = (IDfSysObject)session.getObject(new DfId(docid));
		
		String html ="List Workflows that have a package in the version tree of this object<br>Object Name: "+obj.getObjectName() +"<br>";
		html += "Object Id: "+baselink+"&page=dm_document&id="+docid+"\">"+docid+"</a>"+"<br>";
		// get labels...
		IDfVersionLabels labels = obj.getVersionLabels();
		String labelstring = "";
		String implicitlabel = labels.getImplicitVersionLabel();
		html += "Implicit Version: "+implicitlabel+"<br>";
		for (int i=0; i<labels.getVersionLabelCount(); i++)
		{
			labelstring += '['+labels.getVersionLabel(i)+']';
		}
		html += "Labels: "+labelstring+"<br>";
		
		// get chronicle id
		String chronid = obj.getChronicleId().getId();
		
		// find workflows that have that chronicle id as the package
		String workflows = "select distinct r_workflow_id from dmi_package where any r_component_chron_id = '"+chronid+"' order by r_workflow_id desc";
		
        IDfQuery qry = new DfQuery();
        qry.setDQL(workflows);
        
        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
        html += "<table border='1'>";
        html+="<tr><td><b>Name</b></td><td><b>supervisor</b></td><td><b>RuntimeState</b></td><td><b>PkgVer</b></td><td><b>Dump</b></td></tr>";
        while (myObj1.next()) 
        {
        	IDfId wfid = myObj1.getId("r_workflow_id");
        	// look up each workflow
        	IDfWorkflow workflow = (IDfWorkflow)session.getObject(wfid);
        	// look up the version labels of the relevant package
        	String packagedql = "select r_component_id from dmi_package where r_workflow_id = '"+wfid.getId()+"' and any r_component_chron_id = '"+chronid+"'";
            IDfQuery qpkry = new DfQuery();
            qpkry.setDQL(packagedql);
            IDfCollection pkgs = (IDfCollection) qpkry.execute(session, IDfQuery.DF_READ_QUERY);
            String plabel = "";
            String pid = "";
            String packagelink = "";
            while (pkgs.next()) {
            	IDfId pdocid = pkgs.getId("r_component_id");
            	IDfSysObject pkg = (IDfSysObject)session.getObject(pdocid);
            	if (pkg.getChronicleId().getId().equals(chronid))
            	{
            		packagelink = baselink+"&page=dm_document&id="+pid+"\">"+pid+"</a>";
            		
            		pid = pdocid.getId();
            		plabel = pkg.getVersionLabels().getImplicitVersionLabel();
            	}
            }
            pkgs.close();
            	
        	String wflink = baselink+"&page=objdump&id="+wfid.getId()+"\">"+wfid.getId()+"</a>";
        	html+= "<tr><td>"+workflow.getObjectName()+"</td><td>"+workflow.getSupervisorName()+"</td><td>"+workflow.getRuntimeState()+"</td><td>"+packagelink +" - "+plabel+"</td><td>"+wflink+"</td></tr>";
        }
        html += "</table>";
        
        myObj1.close();
		sMgr.release(session);
		
		return html;
	}
	
	public String doDqlQuery(HttpServletRequest request) throws Exception
	{
		String query = request.getParameter("query");
        String html = getHeader();
        html += "Query: "+query+"<hr>";
        
        html += generateQueryTable(query);
		
        return html;
		
	}

	public String generateQueryTable(String query) throws Exception
	{
		StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=objdump";				
		htmlbuffer.write("<table border=1>");
		IDfSession session = this.getSession();
		try { 
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(query);
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        {
	        	String rowhtml = "<tr><td>#</td>";
	        	for (int i=0; i < myObj1.getAttrCount(); i++)
	        	{
	        		String title = myObj1.getAttr(i).getName();
        			if (title == null) title = "&nbsp;";
        			else if ("".equals(title)) title = "&nbsp;";
	        		rowhtml += "<td>"+title+"</td>";
	        	}
	        	rowhtml += "</tr>";
	        	htmlbuffer.write(rowhtml);
	        }
	        int count = 0;
	        try { 
	        while (myObj1.next()) {
	        	count++;
	        	String rowhtml = "<tr><td>"+count+"</td>";
	        	for (int i=0; i < myObj1.getAttrCount(); i++)
	        	{
	        		if (myObj1.getAttr(i).isRepeating()) {
	        			String output = "";
	        			boolean first = true;
	        			for (int j=0; j < myObj1.getValueCount(myObj1.getAttr(i).getName()); j++)
	        			{
	        				if (first) first = false; else output += ", ";
	        				myObj1.getRepeatingValue(myObj1.getAttr(i).getName(),j);
			        		switch (myObj1.getAttr(i).getDataType()) {
			        			case IDfAttr.DM_BOOLEAN : output += myObj1.getRepeatingBoolean(myObj1.getAttr(i).getName(),j); break;  
			        			case IDfAttr.DM_DOUBLE : output += myObj1.getRepeatingDouble(myObj1.getAttr(i).getName(),j); break;  
			        			case IDfAttr.DM_ID : output += baselink+"&id="+myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId()+"\">"+myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId() + "</a>"; break;  
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
			        			default: rowhtml+= "--";
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
	
	

	public String dumpDocumentByName(HttpServletRequest request) throws Exception
	{
		IDfSession session = this.getSession();
		String docname = request.getParameter("name");
		IDfPersistentObject doc = session.getObjectByQualification("dm_sysobject where object_name = '"+docname+"'");
		sMgr.release(session);
		String header = dumpDocument(doc.getObjectId().getId());
		String dump = this.processDump(doc.getObjectId().getId());
		return getHeader()+header+"<hr>"+dump;
	}

	public String dumpDocumentById(HttpServletRequest request) throws Exception
	{
		String docid = request.getParameter("id");
		String header = dumpDocument(docid);
		String dump = this.processDump(docid);
		return getHeader()+header+"<hr>"+dump;
	}
	
	public IDfACL getACL(String aclname) throws Exception
	{
		IDfSession session = this.getSession();		
        String systemdomain = session.getServerConfig().getString("operator_name");
        IDfACL newACL = session.getACL(systemdomain, aclname);
        
		sMgr.release(session);
		
		return newACL;
	}

	public String dumpObject(HttpServletRequest request) throws Exception
	{
		IDfSession session = this.getSession();
		
		String id = request.getParameter("id");
		
		IDfPersistentObject po = session.getObject(new DfId(id));
		
		IDfType objtype = po.getType();

		String html = "";
		if ("dm_document".equals(objtype.getName()) || objtype.isSubTypeOf("dm_document"))
		{
			html = dumpDocument(id);
		}
		if ("dm_folder".equals(objtype.getName()) || objtype.isSubTypeOf("dm_folder"))
		{
			html = dumpFolder(id);
		}
		if ("dm_acl".equals(objtype.getName()) || objtype.isSubTypeOf("dm_acl"))
		{
			html = dumpACL(id);
		}
		if ("dm_user".equals(objtype.getName()) || objtype.isSubTypeOf("dm_user"))
		{
			html = dumpUser(id);
		}
		if ("dm_group".equals(objtype.getName()) || objtype.isSubTypeOf("dm_group"))
		{
			html = dumpUser(id);
		}
		if ("dm_workflow".equals(objtype.getName()) || objtype.isSubTypeOf("dm_workflow"))
		{
			html = dumpWorkflow(id);
		}
		if ("dm_process".equals(objtype.getName()) || objtype.isSubTypeOf("dm_process"))
		{
			html = dumpProcess(id);
		}
		
		String dump = processDump(id);
		
		sMgr.release(session);
		
		return getHeader()+html+"<hr>"+dump;
	}
	
	public String dumpProcess(String wfid) throws Exception
	{
		String html = "";

		// get activity definitions
		html += "Activity Definitions --<BR>";
		html += generateQueryTable("SELECT r_object_id, object_name, r_package_name, r_package_id, exec_method_id FROM dm_activity WHERE r_object_id in (SELECT r_act_def_id FROM dm_process WHERE r_object_id = '"+wfid+"') order by r_object_id");
		html += "<hr>";

		return html;
		
	}
	
	public String activeWorkflowReport (HttpServletRequest request) throws Exception
	{
		String baselink = " <a href='"+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;				
		// get list of workflows
		StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));		
		htmlbuffer.write(getHeader());
		IDfSession session = this.getSession();
		try {
			// header row
			htmlbuffer.write("<table border=1>");
			htmlbuffer.write("<tr><td>#</td><td>wfid</td><td>workflow name</td><td>supervisor</td><td>start time</td><td>document</td><td>document state</td><td>docid</td><td>mrcs config</td></tr>");
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL("SELECT w.r_object_id, w.object_name, w.supervisor_name FROM dm_workflow w ORDER BY w.supervisor_name, w.r_start_date DESC");
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        try { 
		        int count = 0;
		        while (myObj1.next()) 
		        {
		        	count++;
		        	String rowhtml = "<tr><td>"+count+"</td>";
		        	String workflowid = myObj1.getString("r_object_id");
		        	rowhtml += "<td>"+baselink+"&page=objdump&id="+workflowid+"'>"+workflowid+"</a></td>";
		        	String workflowname = myObj1.getString("object_name");
		        	rowhtml += "<td>"+workflowname+"</td>";
		        	String supervisor = myObj1.getString("supervisor_name");
		        	// workflow name, supervisor, when started
		        	rowhtml += "<td>"+supervisor+"</td>";;
		        	IDfWorkflow workflowobj = (IDfWorkflow)session.getObject(new DfId(workflowid));
		        	rowhtml += "<td>"+workflowobj.getStartDate().asString(IDfTime.DF_TIME_PATTERN44)+"</td>";;
		        	// package name
		        	try { 
			        	IDfPackage pkgobj = (IDfPackage)session.getObjectByQualification("dmi_package WHERE r_workflow_id = '"+workflowid+"'");
			        	String subhtml = "";
			        	try { 
			        		IDfSysObject packagedoc = (IDfSysObject)session.getObject(pkgobj.getComponentId(0));
				        	subhtml += "<td>"+packagedoc.getObjectName()+"</td>";
				        	subhtml += "<td>"+packagedoc.getCurrentStateName()+" ("+packagedoc.getCurrentState()+")</td>";
				        	subhtml += "<td>"+baselink+"&page=objdump&id="+packagedoc.getObjectId().getId()+"'>"+packagedoc.getObjectId().getId()+"</a></td>";
				        	if (packagedoc.hasAttr("mrcs_application")) {
				        		subhtml += "<td>"+packagedoc.getString("mrcs_application")+"</td>";
				        	} else subhtml += "<td>NOT MRCS</td>";
			        	} catch (Exception e) {
			        		subhtml = "";
				        	subhtml += "<td><font color='RED'>INVALID DOCUMENT</font></td>";
				        	subhtml += "<td>null</td>";
				        	subhtml += "<td>"+pkgobj.getComponentId(0)+" - INVALID</td>";
				        	subhtml += "<td>null</td>";
			        	}
			        	rowhtml += subhtml;
		        	} catch (Exception e) {
			        	rowhtml += "<td><font color='RED'>INVALID PACKAGE</font></td>";
			        	rowhtml += "<td>null</td>";
			        	rowhtml += "<td>null</td>";	        		
			        	rowhtml += "<td>null</td>";
		        	}
		        	rowhtml += "</tr>";
		        	htmlbuffer.write(rowhtml);
		        }
	        } finally {
		        myObj1.close();
	        }
	        htmlbuffer.write("</table>");

		} catch(Exception e) {
			htmlbuffer.write("Exception in workflow report: "+e+"<BR>");
			htmlbuffer.write("<hr>");
			e.printStackTrace(htmlbuffer);
			htmlbuffer.write("<hr>");
		}
		
		sMgr.release(session);
		htmlbuffer.close();
		
		return sw.toString();
		
	}

	public String activeWorkflowTaskReport (HttpServletRequest request) throws Exception
	{
		String baselink = " <a href='"+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;				
		// get list of workflows
		StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));		
		htmlbuffer.write(getHeader());
		IDfSession session = this.getSession();
		try {
			// header row
	        htmlbuffer.write("<table border=1>");
	        htmlbuffer.write("<tr><td>#</td><td>wfid</td><td>workflow name</td><td>process name</td><td>supervisor</td><td>start time</td><td>document</td><td>document state</td><td>docid</td><td>mrcs config</td></tr>");
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL("SELECT w.r_object_id, w.object_name, w.supervisor_name FROM dm_workflow w ORDER BY w.supervisor_name, w.r_start_date DESC");
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        int count = 0;
	        try { 
		        while (myObj1.next()) 
		        {
		        	count++;
		        	String rowhtml = "<tr><td>"+count+"</td>";
		        	String workflowid = myObj1.getString("r_object_id");
		        	IDfWorkflow workflowobj = (IDfWorkflow)session.getObject(new DfId(workflowid));
		        	rowhtml += "<td>"+baselink+"&page=objdump&id="+workflowid+"'>"+workflowid+"</a></td>";
		        	String workflowname = myObj1.getString("object_name");
		        	rowhtml += "<td>"+workflowname+"</td>";
		        	IDfProcess processobj = (IDfProcess)session.getObject(workflowobj.getProcessId());
		        	String processname = processobj.getObjectName();
		        	rowhtml += "<td>"+processname+"</td>";
		        	String supervisor = myObj1.getString("supervisor_name");
		        	// workflow name, supervisor, when started
		        	rowhtml += "<td>"+supervisor+"</td>";;
		        	rowhtml += "<td>"+workflowobj.getStartDate().asString(IDfTime.DF_TIME_PATTERN44)+"</td>";;
		        	// package name
		        	try { 
			        	IDfPackage pkgobj = (IDfPackage)session.getObjectByQualification("dmi_package WHERE r_workflow_id = '"+workflowid+"'");
			        	String subhtml = "";
			        	try { 
			        		IDfSysObject packagedoc = (IDfSysObject)session.getObject(pkgobj.getComponentId(0));
				        	subhtml += "<td>"+packagedoc.getObjectName()+"</td>";
				        	subhtml += "<td>"+packagedoc.getCurrentStateName()+" ("+packagedoc.getCurrentState()+")</td>";
				        	subhtml += "<td>"+baselink+"&page=objdump&id="+packagedoc.getObjectId().getId()+"'>"+packagedoc.getObjectId().getId()+"</a></td>";
				        	if (packagedoc.hasAttr("mrcs_application")) {
				        		subhtml += "<td>"+packagedoc.getString("mrcs_application")+"</td>";
				        	} else subhtml += "<td>NOT MRCS</td>";
			        	} catch (Exception e) {
			        		subhtml = "";
				        	subhtml += "<td><font color='RED'>INVALID DOCUMENT</font></td>";
				        	subhtml += "<td>null</td>";
				        	subhtml += "<td>"+pkgobj.getComponentId(0)+" - INVALID</td>";
				        	subhtml += "<td>null</td>";
			        	}
			        	rowhtml += subhtml;
		        	} catch (Exception e) {
			        	rowhtml += "<td><font color='RED'>INVALID PACKAGE</font></td>";
			        	rowhtml += "<td>null</td>";
			        	rowhtml += "<td>null</td>";	        		
			        	rowhtml += "<td>null</td>";
		        	} 
		        	rowhtml += "</tr>";	
		        	rowhtml += "<tr><td>-</td><td colspan='9'>";
		        	// get activity list
			        String taskhtml = "<table><tr><td><b>seq</b></td><td><b>taskid</b></td><td><b>taskname</b></td><td><b>performer</b></td><td><b>acceptance date</b></td>"+
		        	"<td><b>date sent</b></td>"+
		        	"<td><b>start</b></td>"+
		        	"<td><b>dequeued</b></td>"+
		        	"<td><b>signoff</b></td>"+
		        	"<td><b>sent by</b></td>"+
		        	"<td><b>sent to</b></td>"+
		        	"<td><b>queue id</b></td>"+
		        	"</tr>";
		        	try { 
				        IDfQuery actqry = new DfQuery();
				        actqry.setDQL("select w.r_object_id,w.r_act_seqno, w.r_performer_name,w.r_sign_off_req, a.object_name, p.i_acceptance_date, w.r_queue_item_id from dmi_workitem w, dm_activity a, dmi_package p where w.r_act_def_id = a.r_object_id and w.r_workflow_id = '"+workflowid+"' and p.r_workflow_id = w.r_workflow_id and p.r_act_seqno = w.r_act_seqno order by w.r_act_seqno");
				        IDfCollection acts = (IDfCollection) actqry.execute(session, IDfQuery.DF_READ_QUERY);
				        //String taskhtml = "<table><tr><td><b>seq</b></td><td><b>taskid</b></td><td><b>taskname</b></td><td><b>performer</b></td><td><b>pending</b><td><b>signoff task</b></td></tr>";
				        try { 
					        while (acts.next())
					        {
					        	taskhtml += "<tr>";
					        	int seq = acts.getInt("r_act_seqno");
					        	taskhtml += "<td>"+seq+"</td>";
					        	String itemid = acts.getString("r_object_id");
					        	taskhtml += "<td>"+baselink+"&page=objdump&id="+itemid+"'>"+itemid+"</a></td>";
					        	String actname = acts.getString("object_name");
					        	taskhtml += "<td>"+actname+"</td>";
					        	String performer = acts.getString("r_performer_name");
					        	taskhtml += "<td>"+performer+"</td>";
					        	IDfTime time = acts.getTime("i_acceptance_date");
					        	taskhtml += "<td>"+time.toString()+"</td>";
					        	String queueitemid = acts.getId("r_queue_item_id").toString();
					        	if (queueitemid != null && !"0000000000000000".equals(queueitemid)) {
							        IDfQuery queueqry = new DfQuery();
							        String qqry = "SELECT date_sent, actual_start_date, dequeued_date, sign_off_date, sent_by, name FROM dmi_queue_item WHERE r_object_id = '"+queueitemid+"'";
							        queueqry.setDQL(qqry);
							        IDfCollection qis = (IDfCollection) queueqry.execute(session, IDfQuery.DF_READ_QUERY);
							        try {
								        qis.next();
								        String queuehtml = "";
							        	IDfTime senttime = qis.getTime("date_sent");
								        queuehtml += "<td bgcolor='#AAAAAA'>"+senttime.toString()+"</td>";
							        	IDfTime stdate = qis.getTime("actual_start_date");
							        	queuehtml += "<td bgcolor='#AAAAAA'>"+stdate.toString()+"</td>";
							        	IDfTime dqdate = qis.getTime("dequeued_date");
							        	queuehtml += "<td bgcolor='#AAAAAA'>"+dqdate.toString()+"</td>";
							        	IDfTime sodate = qis.getTime("sign_off_date");
							        	queuehtml += "<td bgcolor='#AAAAAA'>"+sodate.toString()+"</td>";
							        	String sentby = qis.getString("sent_by");
							        	queuehtml += "<td bgcolor='#AAAAAA'>"+sentby+"</td>";
							        	String sentto = qis.getString("name");
							        	queuehtml += "<td bgcolor='#AAAAAA'>"+sentto+"</td>";
							        	queuehtml += "<td>"+baselink+"&page=objdump&id="+queueitemid+"'>"+queueitemid+"</a></td>";
								        taskhtml += queuehtml;
							        } catch (Exception e) {
						        		taskhtml += "<td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td>-</td><td><font color='red'>"+queueitemid+"</font></td>";							        	
							        } finally {
							        	qis.close();
							        }
					        	} else {
					        		// no queueitem
					        		taskhtml += "<td></td><td></td><td></td><td></td><td></td><td></td><td></td>";
					        		
					        	}
					        	boolean ispending = false;
					        	for (int j=0; j < workflowobj.getValueCount("r_perf_act_name"); j++)
					        	{
					        		if (actname.equals(workflowobj.getRepeatingString("r_perf_act_name",j)))
					        			ispending = true;
					        	}
					        	if (ispending) 
						        	;//taskhtml += "<td><font color='red'>Pending...</font></td>";
					        	else
						        	;//taskhtml += "<td>Complete</td>";
					        	boolean signoff = acts.getBoolean("r_sign_off_req");
					        	//taskhtml += "<td>"+signoff+"</td>";
					        	taskhtml += "</tr>";
					        }
				        } finally {
					        acts.close();			        	
				        }
				        taskhtml += "</table>";
		        	} catch (Exception e) {
		        		taskhtml = "ERROR IN TASK SUBQUERY";
		        	}
			        rowhtml += taskhtml + "</td></tr>";
			        htmlbuffer.write(rowhtml);
		        }
	        } finally {
		        myObj1.close();
	        }
	        htmlbuffer.write("</table>");
		} catch(Exception e) {
			htmlbuffer.write("Exception in workflow report: "+e+"<BR>");
			e.printStackTrace(htmlbuffer);
		}
		
		sMgr.release(session);
		
		htmlbuffer.close();
		
		return sw.toString();
	}

	public String activeWorkflowAuditReport (HttpServletRequest request) throws Exception
	{
		String baselink = " <a href='"+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;				
		// get list of workflows
		StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));		
		htmlbuffer.write(getHeader());
		IDfSession session = this.getSession();
		try {
			// header row
	        htmlbuffer.write("<table border=1>");
	        htmlbuffer.write("<tr><td>#</td><td>wfid</td><td>workflow name</td><td>process name</td><td>supervisor</td><td>start time</td><td>document</td><td>document state</td><td>docid</td><td>mrcs config</td></tr>");
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL("SELECT w.r_object_id, w.object_name, w.supervisor_name FROM dm_workflow w ORDER BY w.supervisor_name, w.r_start_date DESC");
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        int count = 0;
	        try { 
		        while (myObj1.next()) 
		        {
		        	count++;
		        	String rowhtml = "<tr><td>"+count+"</td>";
		        	String workflowid = myObj1.getString("r_object_id");
		        	IDfWorkflow workflowobj = (IDfWorkflow)session.getObject(new DfId(workflowid));
		        	rowhtml += "<td>"+baselink+"&page=objdump&id="+workflowid+"'>"+workflowid+"</a></td>";
		        	String workflowname = myObj1.getString("object_name");
		        	rowhtml += "<td>"+workflowname+"</td>";
		        	IDfProcess processobj = (IDfProcess)session.getObject(workflowobj.getProcessId());
		        	String processname = processobj.getObjectName();
		        	rowhtml += "<td>"+processname+"</td>";
		        	String supervisor = myObj1.getString("supervisor_name");
		        	// workflow name, supervisor, when started
		        	rowhtml += "<td>"+supervisor+"</td>";;
		        	rowhtml += "<td>"+workflowobj.getStartDate().asString(IDfTime.DF_TIME_PATTERN44)+"</td>";;
		        	// package name
		        	try { 
			        	IDfPackage pkgobj = (IDfPackage)session.getObjectByQualification("dmi_package WHERE r_workflow_id = '"+workflowid+"'");
			        	String subhtml = "";
			        	try { 
			        		IDfSysObject packagedoc = (IDfSysObject)session.getObject(pkgobj.getComponentId(0));
				        	subhtml += "<td>"+packagedoc.getObjectName()+"</td>";
				        	subhtml += "<td>"+packagedoc.getCurrentStateName()+" ("+packagedoc.getCurrentState()+")</td>";
				        	subhtml += "<td>"+baselink+"&page=objdump&id="+packagedoc.getObjectId().getId()+"'>"+packagedoc.getObjectId().getId()+"</a></td>";
				        	if (packagedoc.hasAttr("mrcs_application")) {
				        		subhtml += "<td>"+packagedoc.getString("mrcs_application")+"</td>";
				        	} else subhtml += "<td>NOT MRCS</td>";
			        	} catch (Exception e) {
			        		subhtml = "";
				        	subhtml += "<td><font color='RED'>INVALID DOCUMENT</font></td>";
				        	subhtml += "<td>null</td>";
				        	subhtml += "<td>"+pkgobj.getComponentId(0)+" - INVALID</td>";
				        	subhtml += "<td>null</td>";
			        	}
			        	rowhtml += subhtml;
		        	} catch (Exception e) {
			        	rowhtml += "<td><font color='RED'>INVALID PACKAGE</font></td>";
			        	rowhtml += "<td>null</td>";
			        	rowhtml += "<td>null</td>";	        		
			        	rowhtml += "<td>null</td>";
		        	} 
		        	rowhtml += "</tr>";	
		        	rowhtml += "<tr><td>-</td><td colspan='9'>";
		        	// get activity list
			        String taskhtml = "<table><tr>"+
		        	"<td><b>audit id</b></td>"+
		        	"<td><b>event</b></td>"+
		        	"<td><b>user</b></td>"+
		        	"<td><b>timestamp</b></td>"+
		        	"<td><b>s1</b></td>"+
		        	"<td><b>s2</b></td>"+
		        	"<td><b>s3</b></td>"+
		        	"<td><b>s4</b></td>"+
		        	"<td><b>s5</b></td>"+
		        	"<td><b>id1</b></td>"+
		        	"<td><b>id2</b></td>"+
		        	"<td><b>name</b></td>"+
		        	"</tr>";
		        	try { 
				        IDfQuery actqry = new DfQuery();
				        actqry.setDQL("select r_object_id, workflow_id, event_name, user_name, time_stamp, string_1, string_2, string_3,string_4,string_5, id_1, id_2, object_name from dm_audittrail where event_source = 'workflow' and workflow_id = '"+workflowid+"' order by time_stamp ASC");
				        IDfCollection acts = (IDfCollection) actqry.execute(session, IDfQuery.DF_READ_QUERY);
				        //String taskhtml = "<table><tr><td><b>seq</b></td><td><b>taskid</b></td><td><b>taskname</b></td><td><b>performer</b></td><td><b>pending</b><td><b>signoff task</b></td></tr>";
				        try { 
					        while (acts.next())
					        {
					        	taskhtml += "<tr>";
					        	String auditid = acts.getString("r_object_id");
					        	taskhtml += "<td>"+baselink+"&page=objdump&id="+auditid+"'>"+auditid+"</a></td>";
					        	String event = acts.getString("event");
					        	taskhtml += "<td>"+event+"</td>";
					        	String user = acts.getString("user_name");
					        	taskhtml += "<td>"+user+"</td>";
					        	IDfTime time = acts.getTime("time_stamp");
					        	taskhtml += "<td>"+time.toString()+"</td>";
					        	String s1 = acts.getString("string_1");
					        	taskhtml += "<td>"+s1+"</td>";
					        	String s2 = acts.getString("string_2");
					        	taskhtml += "<td>"+s2+"</td>";
					        	String s3 = acts.getString("string_3");
					        	taskhtml += "<td>"+s3+"</td>";
					        	String s4 = acts.getString("string_4");
					        	taskhtml += "<td>"+s4+"</td>";
					        	String s5 = acts.getString("string_5");
					        	taskhtml += "<td>"+s5+"</td>";
					        	String id1 = acts.getString("id_1");
					        	taskhtml += "<td>"+baselink+"&page=objdump&id="+id1+"'>"+id1+"</a></td>";
					        	String id2 = acts.getString("id_2");
					        	taskhtml += "<td>"+baselink+"&page=objdump&id="+id2+"'>"+id2+"</a></td>";
					        	String name = acts.getString("object_name");
					        	taskhtml += "</tr>";
					        }
				        } finally {
					        acts.close();			        	
				        }
				        taskhtml += "</table>";
		        	} catch (Exception e) {
		        		taskhtml = "ERROR IN AUDIT SUBQUERY: "+e.getMessage();
		        	}
			        rowhtml += taskhtml + "</td></tr>";
			        htmlbuffer.write(rowhtml);
		        }
	        } finally {
		        myObj1.close();
	        }
	        htmlbuffer.write("</table>");
		} catch(Exception e) {
			htmlbuffer.write("Exception in workflow report: "+e+"<BR>");
			e.printStackTrace(htmlbuffer);
		}
		
		sMgr.release(session);
		
		htmlbuffer.close();
		
		return sw.toString();
	}
	
	
	public String csvEncodeToken(String token)
	{
		if (token == null) return "";
		// check if the token has a ,
		if (token.indexOf(',') != -1 || token.indexOf('"') != -1)
		{
			// encode the double-quotes, and wrap token in double-quotes
			return '"'+token.replaceAll("\"","\"\"")+'"';
		}
		return token;
	}

	public String msdcasTaskReport (HttpServletRequest request) throws Exception
	{
		String baselink = " <a href='"+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;				
		// get list of workflows
		IDfSession session = this.getSession();
		String csv = "";
		try {
			csv = "wfid,workflow name,process name,supervisor,start time,document name,docid,task seq #,taskname,performer,package accept date,date sent,start,dequeued,signoff,sent by,sent to\n";
			// header row
	        //html += "<table border=1>";
			//html += "<tr><td>#</td><td>wfid</td><td>workflow name</td><td>process name</td><td>supervisor</td><td>start time</td><td>document</td><td>document state</td><td>docid</td><td>mrcs config</td></tr>";
		
	        IDfQuery qry = new DfQuery();
	        qry.setDQL("SELECT w.r_object_id, w.object_name, w.supervisor_name FROM dm_workflow w ORDER BY w.supervisor_name, w.r_start_date DESC");
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        int count = 0;
	        while (myObj1.next()) 
	        {
	        	count++;
	        	String wfcsv = "";
	        	String workflowid = myObj1.getString("r_object_id");
	        	wfcsv += workflowid+',';
	        	IDfWorkflow workflowobj = (IDfWorkflow)session.getObject(new DfId(workflowid));
	        	String workflowname = myObj1.getString("object_name");
	        	wfcsv += csvEncodeToken(workflowname)+",";
	        	IDfProcess processobj = (IDfProcess)session.getObject(workflowobj.getProcessId());
	        	String processname = processobj.getObjectName();
	        	wfcsv += csvEncodeToken(processname)+',';
	        	String supervisor = myObj1.getString("supervisor_name");
	        	// workflow name, supervisor, when started
	        	wfcsv += csvEncodeToken(supervisor)+',';
	        	wfcsv += csvEncodeToken(workflowobj.getStartDate().asString(IDfTime.DF_TIME_PATTERN44))+',';
	        	// package name
	        	String doccsv = "";
	        	try { 
		        	IDfPackage pkgobj = (IDfPackage)session.getObjectByQualification("dmi_package WHERE r_workflow_id = '"+workflowid+"'");
		        	try { 
		        		IDfSysObject packagedoc = (IDfSysObject)session.getObject(pkgobj.getComponentId(0));
		        		doccsv += csvEncodeToken(packagedoc.getObjectName())+',';
		        		doccsv += csvEncodeToken(packagedoc.getObjectId().getId())+',';
		        	} catch (Exception e) {
		        		doccsv += ""+',';
		        		doccsv += ""+',';
		        	}
	        	} catch (Exception e) {
	        		doccsv += ""+',';
	        		doccsv += ""+',';
	        	}
	        	wfcsv += doccsv;
	        	
	        	// get activity list
		        IDfQuery actqry = new DfQuery();
		        actqry.setDQL("select w.r_object_id,w.r_act_seqno, w.r_performer_name,w.r_sign_off_req, a.object_name, p.i_acceptance_date, w.r_queue_item_id from dmi_workitem w, dm_activity a, dmi_package p where w.r_act_def_id = a.r_object_id and w.r_workflow_id = '"+workflowid+"' and p.r_workflow_id = w.r_workflow_id and p.r_act_seqno = w.r_act_seqno order by w.r_act_seqno");
		        IDfCollection acts = (IDfCollection) actqry.execute(session, IDfQuery.DF_READ_QUERY);
		        //String taskhtml = "<table><tr><td><b>seq</b></td><td><b>taskid</b></td><td><b>taskname</b></td><td><b>performer</b></td><td><b>pending</b><td><b>signoff task</b></td></tr>";
		        while (acts.next())
		        {
			        String taskcsv = "";
		        	int seq = acts.getInt("r_act_seqno");
		        	taskcsv += csvEncodeToken(""+seq)+',';
		        	String actname = acts.getString("object_name");
		        	taskcsv += csvEncodeToken(actname)+',';
		        	String performer = acts.getString("r_performer_name");
		        	taskcsv += csvEncodeToken(performer)+',';
		        	IDfTime time = acts.getTime("i_acceptance_date");
		        	taskcsv += csvEncodeToken(time.toString())+',';
		        	String queueitemid = acts.getId("r_queue_item_id").toString();
		        	if (queueitemid != null && !"0000000000000000".equals(queueitemid)) {
				        IDfQuery queueqry = new DfQuery();
				        String qqry = "SELECT date_sent, actual_start_date, dequeued_date, sign_off_date, sent_by, name FROM dmi_queue_item WHERE r_object_id = '"+queueitemid+"'";
				        queueqry.setDQL(qqry);
				        IDfCollection qis = (IDfCollection) queueqry.execute(session, IDfQuery.DF_READ_QUERY);
				        qis.next();
			        	IDfTime senttime = qis.getTime("date_sent");
			        	taskcsv += csvEncodeToken(senttime.toString())+',';
			        	IDfTime stdate = qis.getTime("actual_start_date");
			        	taskcsv += csvEncodeToken(stdate.toString())+',';
			        	IDfTime dqdate = qis.getTime("dequeued_date");
			        	taskcsv += csvEncodeToken(dqdate.toString())+',';
			        	IDfTime sodate = qis.getTime("sign_off_date");
			        	taskcsv += csvEncodeToken(sodate.toString())+',';
			        	String sentby = qis.getString("sent_by");
			        	taskcsv += csvEncodeToken(sentby)+',';
			        	String sentto = qis.getString("name");
			        	taskcsv += csvEncodeToken(sentto)+',';
			        	qis.close();
		        		
		        	} else {
		        		// no queueitem
		        		taskcsv += ",,,,,,";
		        	}
		        	boolean ispending = false;
		        	for (int j=0; j < workflowobj.getValueCount("r_perf_act_name"); j++)
		        	{
		        		if (actname.equals(workflowobj.getRepeatingString("r_perf_act_name",j)))
		        			ispending = true;
		        	}
		        	if (ispending) 
			        	;//taskhtml += "<td><font color='red'>Pending...</font></td>";
		        	else
			        	;//taskhtml += "<td>Complete</td>";
		        	boolean signoff = acts.getBoolean("r_sign_off_req");
		        	//taskhtml += "<td>"+signoff+"</td>";
		        	csv += wfcsv+taskcsv+'\n';
		        }
		        acts.close();
	        }
	        myObj1.close();
		} catch(Exception e) {
			csv += "Exception in workflow report: "+e;
		}
		
		sMgr.release(session);
		
		return "<![CDATA["+csv+"]]>";
		
	}

	public String msdcasHistoricalWorkflowReport (HttpServletRequest request) throws Exception
	{
		// run the BFG-9000 query
		IDfSession session = this.getSession();
		String csv = "";
		try {
			csv = "wfid,workflow name,process name,supervisor,start time,document name,task seq #,taskname,performer,task start,task selected,task completed\n";

	        IDfQuery qry = new DfQuery();
	        // this query only selects workflows with a dm_finishworkflow event, which presumably filters out aborted or unfinished workflows
	        // we can probably add: and workflow_id in (select workflow_id from dm_audittrail where event_name = 'dm_createworkflow' and time_stamp > [a value]) to filter out 
	        // workflows based on time. TBD...
	        qry.setDQL("select r_object_id, workflow_id, event_name, user_name, time_stamp, string_1, string_2, string_3,string_4,string_5, id_1, id_2, object_name "+
	        		   "from dm_audittrail where event_source = 'workflow' and event_name != 'dm_changestateactivity' " +
	        	
	        		   "and workflow_id in (select workflow_id from dm_audittrail where event_name = 'dm_finishworkflow') "+
	        		   "order by workflow_id, string_1 ");
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        int count = 0;
	        
	        // define inner class for compact holding of information
	        class wfdata {
	        	public String auditid, workflowid, event, user, s1,s2,s3,s4,s5, id1,id2, workflow;
	        	public IDfTime time_stamp;
	        }
	        Map workflows = null;
	        class castsk {
	        	int seq;
	        	String taskname, performer;
	        	IDfTime start,selected,completed;
	        }
	        
	        class caswf {
	        	String wfid, name, process, supervisor, docname; 
	        	IDfTime createtime;
	        	castsk[] tasks = new castsk[50];
	        }
	        
	        String primary = "mrcs_msd_cas_primary_approval";
	        String adhoc = "mrcs_msd_cas_adhoc_2";
	        String alicia = "Alicia Dennis";
	        String jma = "Jeff Mitchell [Admin]";
	        { // this block is to help the JVM with indicating the auditrecs object can be GC'd, so we avoid out-of-memory issues
		        List auditrecs = new ArrayList();
		        
		        while (myObj1.next()) 
		        {
		        	wfdata rec = new wfdata();
		        	rec.auditid = myObj1.getId("r_object_id").getId();
		        	rec.workflowid = myObj1.getId("workflow_id").getId();
		        	rec.event = myObj1.getString("event_name");
		        	rec.id1 = myObj1.getId("id_1").getId();
		        	rec.id1 = myObj1.getId("id_2").getId();
		        	rec.s1 = myObj1.getString("string_1");
		        	rec.s2 = myObj1.getString("string_2");
		        	rec.s3 = myObj1.getString("string_3");
		        	rec.s4 = myObj1.getString("string_4");
		        	rec.s5 = myObj1.getString("string_5");
		        	rec.workflow = myObj1.getString("object_name");
		        	if (primary.equals(rec.workflow)) rec.workflow = primary; // try to eliminate some of the overhead...
		        	if (adhoc.equals(rec.workflow)) rec.workflow = adhoc; // try to eliminate some of the overhead...
		        	rec.user = myObj1.getString("user_name");
		        	if (alicia.equals(rec.user)) rec.user = alicia;
		        	if (jma.equals(rec.user)) rec.user = jma;
		        	rec.time_stamp = myObj1.getTime("time_stamp");
		        	auditrecs.add(rec);
		        }
		        myObj1.close();
		        
		        
		        // create datastore for wf objs
		        workflows = new HashMap();
		        
		        // create caswf objs/populate top-level data
		        int i;
		        for (i=0; i < auditrecs.size(); i++)
		        {
		        	wfdata wfd = (wfdata)auditrecs.get(i);
		        	if ("dm_createworkflow".equals(wfd.event))
		        	{
		        		caswf newone = new caswf();
		        		newone.supervisor = wfd.s4;
		        		newone.name = wfd.s5;
		        		newone.createtime = wfd.time_stamp;
		        		newone.process = wfd.workflow;
		        		newone.wfid = wfd.workflowid;
		        		workflows.put(newone.wfid,newone);
		        	}
		        	
		        }
		        // populate docname from dm_addpackage events
		        // -- I validated that select count(*) from dm_audittrail where event_name = 'dm_addpackage' equals select count(*) from dm_audittrail where event_name = 'dm_createworkflow'
		        for (i=0; i < auditrecs.size(); i++)
		        {
		        	wfdata wfd = (wfdata)auditrecs.get(i);
		        	// dm_addpackage has the attachment/document name
		        	try {
			        	if ("dm_addpackage".equals(wfd.event))
			        	{
			        		caswf newone = (caswf)workflows.get(wfd.workflowid);
			        		newone.docname = wfd.s3;
			        	}
			        	if ("dm_startedworkitem".equals(wfd.event))
			        	{
			        		caswf newone = (caswf)workflows.get(wfd.workflowid);
			        		// parse seq_no
			        		int seqno = Integer.parseInt(wfd.s1);
			        		if (newone.tasks[seqno] == null) newone.tasks[seqno] = new castsk();
			        		newone.tasks[seqno].start = wfd.time_stamp;
			        		//newone.tasks[seqno].performer = wfd.user;
			        		//newone.tasks[seqno].taskname = wfd.s2;
			        	}
			        	if ("dm_selectedworkitem".equals(wfd.event))
			        	{
			        		caswf newone = (caswf)workflows.get(wfd.workflowid);
			        		// parse seq_no
			        		int seqno = Integer.parseInt(wfd.s1);
			        		if (newone.tasks[seqno] == null) newone.tasks[seqno] = new castsk();
			        		newone.tasks[seqno].selected = wfd.time_stamp;
			        		newone.tasks[seqno].performer = wfd.user;
			        		newone.tasks[seqno].taskname = wfd.s2;
			        	}
			        	if ("dm_completedworkitem".equals(wfd.event))
			        	{
			        		caswf newone = (caswf)workflows.get(wfd.workflowid);
			        		// parse seq_no
			        		int seqno = Integer.parseInt(wfd.s1);
			        		if (newone.tasks[seqno] == null) newone.tasks[seqno] = new castsk();
			        		newone.tasks[seqno].completed = wfd.time_stamp;	        		
			        	}
		        	} catch (NullPointerException npe) {
		        		int a=1;
		        		a=2;
		        	}
		        }
	        }
	        
	        //			csv = "wfid,workflow name,process name,supervisor,start time,document name,task seq #,taskname,performer,task start,task selected,task completed\n";
	        // iterate through the hashmap
	        try { 
	        	DfLogger.debug(this,"--[ "+csv,null,null);
		        Iterator iter = workflows.keySet().iterator();
		        while (iter.hasNext())
		        {
		        	String id = (String)iter.next();
		        	caswf wf = (caswf)workflows.get(id);
		        	String tskhdr = wf.wfid+','+csvEncodeToken(wf.name)+','+csvEncodeToken(wf.process)+','+csvEncodeToken(wf.supervisor)+','+wf.createtime.toString()+','+wf.docname+',';
		        	for (int t=0; t < wf.tasks.length; t++)
		        	{
		        		if (wf.tasks[t] != null)
		        		{
		        			String taskrow = tskhdr;
		        			taskrow += ""+t+','+csvEncodeToken(wf.tasks[t].taskname)+','+csvEncodeToken(wf.tasks[t].performer)+',';
		        			taskrow += (wf.tasks[t].start == null ? "" : wf.tasks[t].start.toString())+',';
		        			taskrow += (wf.tasks[t].selected == null ? "" : wf.tasks[t].selected.toString())+',';
		        			taskrow += (wf.tasks[t].completed == null ? "" : wf.tasks[t].completed.toString());
		    	        	DfLogger.debug(this,"--[ "+taskrow,null,null);
			        		//csv += taskrow;
		        		}
		        	}
		        	// try to clean up as we go, although the iterator may hold a reference too... 
		        	workflows.put(id,null);
		        }
	        } catch (Throwable t) {
	        	int a=1;
	        	a=a+2;
	        	csv += "   --- throwable ---   "+t;
	        }
	        
		} catch(Exception e) {
			csv += "Exception in workflow report: "+e;
		}
		finally {
			try { 
				sMgr.release(session);
			} catch (Exception e) {
	        	int a=1;
	        	a=a+2;
	        	// whatever...
			}
		}
		
		return "DONE! <![CDATA["+csv+"]]>";
		
	}

	
	public String dumpWorkflow(String wfid) throws Exception
	{
		String html = "";

		// get workitems+denormalization
		html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"workitems\").style.display = (document.getElementById(\"workitems\").style.display==\"block\"?\"none\":\"block\")'>Work Items / Tasks --<BR><div id='workitems' style='display:none'>";
		html += generateQueryTable("SELECT wi.r_object_id, wi.r_workflow_id, wi.r_act_seqno,wi.r_performer_name,wi.r_act_def_id, act.object_name,wi.r_queue_item_id FROM dmi_workitem wi, dm_activity act WHERE act.r_object_id = wi.r_act_def_id and wi.r_workflow_id = '"+wfid+"'"); 
		html += "</div><hr>";

		// get activity definitions
		html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"activities\").style.display = (document.getElementById(\"activities\").style.display==\"block\"?\"none\":\"block\")'>Activity Definitions --<BR><div id='activities' style='display:none'>";
		html += generateQueryTable("SELECT r_object_id, object_name, r_package_name, r_package_id, exec_method_id FROM dm_activity WHERE r_object_id in (SELECT r_act_def_id FROM dm_process WHERE r_object_id IN (SELECT process_id FROM dm_workflow WHERE r_object_id = '"+wfid+"')) order by r_object_id");
		html += "</div><hr>";

		// get packages
		html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"packages\").style.display = (document.getElementById(\"packages\").style.display==\"block\"?\"none\":\"block\")'>Packages --<BR><div id='packages' style='display:none'>";
		html += generateQueryTable("SELECT r_object_id,r_workflow_id,r_component_id,r_component_chron_id,r_act_seqno,r_port_name,r_package_name,r_package_type FROM dmi_package WHERE r_workflow_id = '"+wfid+"'");
		html += "package document names/versions:<br>";
		html += generateQueryTable("SELECT r_object_id,object_name,r_version_label FROM dm_document(all) WHERE r_object_id in (SELECT DISTINCT r_component_id FROM dmi_package WHERE r_workflow_id = '"+wfid+"') order by r_object_id"); 
		html += "</div><hr>";

		// ?Attachments?
		html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"attachments\").style.display = (document.getElementById(\"attachments\").style.display==\"block\"?\"none\":\"block\")'>Attachments --<BR><div id='attachments' style='display:none'>";
		html += generateQueryTable("SELECT r_object_id,r_workflow_id,r_component_id,r_component_name,r_component_type,r_creator_name,r_creation_date FROM dmi_wf_attachment WHERE r_workflow_id = '"+wfid+"' order by r_creation_date");  
		html += "</div><hr>";

		// snapshot/rollback records
		// workflow/lifecycle snapshots
		html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"snapshots\").style.display = (document.getElementById(\"snapshots\").style.display==\"block\"?\"none\":\"block\")'>WF/LC Snapshots --<BR><div id='snapshots' style='display:none'>";
		html += generateQueryTable("select * from mrcs_document_snapshot where doc_chronicle_id in (select r_component_chron_id from dmi_package where r_workflow_id = '" + wfid +"') ORDER BY r_object_id DESC"); //and transaction_type  = 'workflow'  
		html += "</div><hr>";
		
		// queue items
		html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"queueitems\").style.display = (document.getElementById(\"queueitems\").style.display==\"block\"?\"none\":\"block\")'>Queue Items --<BR><div id='queueitems' style='display:none'>";
		html += generateQueryTable("SELECT * FROM dmi_queue_item WHERE r_object_id in (SELECT r_queue_item_id FROM dmi_workitem WHERE r_workflow_id = '" + wfid +"')");  
		html += "</div><hr>";
		
		

		// audit events
		//html += "<img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"auditevents\").style.display = (document.getElementById(\"auditevents\").style.display==\"block\"?\"none\":\"block\")'>Audit Events --<BR><div id='auditevents' style='display:none'>";
		//html += generateQueryTable("SELECT * FROM dm_audittrail WHERE workflow_id = '" + wfid +"'");  
		//html += "</div>";
		
		
		return html;
		
	}

	public String dumpACL(String aclid) throws Exception
	{
		String baselink = " <a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;		
		String[] permits = {"null","NONE","BROWSE","READ","RELATE","VERSION","WRITE","DELETE"};
		IDfSession session = this.getSession();
		IDfACL acl = (IDfACL)session.getObject(new DfId(aclid));
		String html = "";
		html += "Name: "+acl.getObjectName()+"<BR>";
		html += "Domain: "+acl.getDomain()+"<BR>";
		html += "<hr>";
		// iterate through the users/groups on the accessors
		boolean first = true;
		for (int i=0; i < acl.getAccessorCount(); i++)
		{
        	if (first) first = false; else html += " - ";
			String name = acl.getAccessorName(i);
			int permit = acl.getAccessorPermit(i);
			String pmt = "";
			switch(permit) {
				case IDfACL.DF_PERMIT_NONE: pmt = " N"+permit; break;
				case IDfACL.DF_PERMIT_BROWSE: pmt = " B"+permit; break;
				case IDfACL.DF_PERMIT_READ:   pmt = " Rd"+permit; break;
				case IDfACL.DF_PERMIT_RELATE: pmt = " Rl"+permit; break;
				case IDfACL.DF_PERMIT_VERSION: pmt = " V"+permit; break;
				case IDfACL.DF_PERMIT_WRITE:  pmt = " W"+permit; break;
				case IDfACL.DF_PERMIT_DELETE: pmt = " D"+permit; break;
			}
			IDfUser user = session.getUser(name);
			if (user != null) {
				String osname = "";
				if (user.isGroup())
					html += "<img src='wdk/theme/documentum/icons/type/t_dm_group_16.gif' length=16 width=16 border=0>";
				else {
					html += "<img src='wdk/theme/documentum/icons/type/t_dm_user_16.gif' length=16 width=16 border=0>";
					osname = " ["+user.getUserOSName()+"]";
				}
				
				html += baselink+"&page=objdump&id="+user.getObjectId().getId()+"\">"+name+"</a>"+osname+"<font color=\'red\'>"+pmt+"</font>";
			} else {
				html += name+"<font color=\'red\'>"+pmt+"</font>";
			}
		}
		html += "<hr>";
		sMgr.release(session);
		return html;
	}

	public String dumpFolder(String folderid) throws Exception
	{
		IDfSession session = this.getSession();
		IDfFolder folder = (IDfFolder)session.getObject(new DfId(folderid));	
				
		// this is SOOOOO 1998 !!!
		String baselink = " <a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;		
		String html = "Folder Name: "+folder.getObjectName()+"<br>";
		html += "Folder Id: "+folderid+"<br><hr>";
		//html += "Path[0]: "+folder.getFolderPath(0) + "<BR>";
		// folder (first folder in i_folder attr) of the document
		try {
			
			for (int i=0; i < folder.getFolderPathCount(); i++) {
				IDfId parentfolderid = folder.getFolderId(0);
				IDfFolder parentfolder = (IDfFolder)session.getObject(parentfolderid);
				html += "Folder["+i+"]: "+baselink+"&page=objdump&id="+parentfolderid+"\">"+parentfolder.getFolderPath(0)+"</a><BR>";
			}
		} catch (Exception e) {
			html += " No folder found";			
		}
		
		// attached ACL
		try {
			String acllink = "View ACL "+baselink+"&page=objdump&id="+folder.getACL().getObjectId().getId()+"\">"+folder.getACLName()+"</a><BR>"; 
			html += acllink;
		} catch (Exception e) {
			html += " no ACL attached ";						
		}
		// MRCS attributes
		if (folder.hasAttr("mrcs_application"))
		{
			String mrcsapp = folder.getString("mrcs_application");
			String mrcsapplink = "View MRCS Application "+baselink+"&page=mrcs_application&name="+mrcsapp+"\">"+mrcsapp+"</a><BR>";
			html += mrcsapplink;
		}
		if (folder.hasAttr("mrcs_config"))
		{
			String mrcsapp = folder.getString("mrcs_application");
			String mrcsconfig = folder.getString("mrcs_config");
			String mrcsapplink = "View MRCS Folder config "+baselink+"&page=mrcs_folder_config&name="+mrcsapp+"&config="+mrcsconfig+"\">"+mrcsconfig+"</a><BR>";
			html += mrcsapplink;
		}
		html += "<hr>Folder Contents:<hr>";
		
		// folder contents - same query that webtop does....
		String dql = "SELECT 1,upper(object_name),r_object_id,object_name,r_object_type,r_lock_owner,owner_name,"+
		                    "r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,r_assembled_from_id,"+
		                    "r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id,title,r_modify_date,thumbnail_url,'1' as isfolder "+
		             "FROM dm_folder WHERE a_is_hidden=false and any i_folder_id='"+folderid+"'"+
		             "UNION "+
		             "SELECT 2,upper(object_name),r_object_id,object_name,r_object_type,r_lock_owner,owner_name,r_link_cnt,"+
		                    "r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,r_assembled_from_id,r_has_frzn_assembly,"+
		                    "a_compound_architecture,i_is_replica,r_policy_id,title,r_modify_date,thumbnail_url,'0' as isfolder " +
		             "FROM dm_document where a_is_hidden=false and any i_folder_id='"+folderid+"' order by 1,2";
        IDfQuery qry = new DfQuery();
        qry.setDQL(dql);
        
        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
        boolean first = true;
        while (myObj1.next()) 
        {
        	if (first) first = false; else html += " - ";
        	http://hansom5-d6:56880/webtop2/
        	if (myObj1.getInt("isfolder") == 1) html += "<img border=0 width=16 height=16 src='wdk/theme/documentum/icons/type/t_dm_folder_16.gif'>";
        	else html += "<img border=0 width=16 height=16 src='wdk/theme/documentum/icons/format/f_text_16.gif'>";
        	html += baselink+"&page=objdump&id="+myObj1.getId("r_object_id").getId()+"\">"+myObj1.getString("object_name")+"</a>";
        }
        myObj1.close();
		
        sMgr.release(session);
		return html;
	}
	
	public String dumpUser(String userid) throws Exception
	{
		String html = "";
		String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;				
		IDfSession session = this.getSession();
		IDfPersistentObject userobj = session.getObject(new DfId(userid));
		if (userobj instanceof IDfGroup) {
			IDfGroup group = (IDfGroup)userobj;
			html += "Group Name: "+group.getGroupName() + "<BR>";
			html += "Group Members:<BR>";
			for (int i=0; i < group.getUsersNamesCount(); i++)
			{
				if (i!=0) html += " - ";
				String name = group.getUsersNames(i);
				IDfUser subuser = session.getUser(name);
				if (subuser != null) {
					String osname = "";
					if (subuser.isGroup())
						html += "<img src='wdk/theme/documentum/icons/type/t_dm_group_16.gif' length=16 width=16 border=0>";
					else {
						html += "<img src='wdk/theme/documentum/icons/type/t_dm_user_16.gif' length=16 width=16 border=0>";
						osname = " ["+subuser.getUserOSName()+"]";
					}
					
					html += baselink+"&page=objdump&id="+subuser.getObjectId().getId()+"\">"+name+"</a>"+osname;
				}
			}
			// groups they belong to?
			html += "<hr>";
			html += "Group Membership:<br>";
			html += generateQueryTable("SELECT r_object_id, group_name, group_address, description FROM dm_group WHERE any groups_names = '"+group.getGroupName()+"' order by group_name"); 
			html += "<hr>";
		} else if (userobj instanceof IDfUser && ((IDfUser)userobj).isGroup()) {
			// since a group is sometimes returned as an IDfUser, we need to do this crap
			IDfGroup group = session.getGroup(((IDfUser)userobj).getUserName());
			html += "Group Name: "+group.getGroupName() + "<BR>";
			html += "Group Members:<BR>";
			for (int i=0; i < group.getUsersNamesCount(); i++)
			{
				if (i!=0) html += " - ";
				String name = group.getUsersNames(i);
				IDfUser subuser = session.getUser(name);
				if (subuser != null) {
					String osname = "";
					if (subuser.isGroup())
						html += "<img src='wdk/theme/documentum/icons/type/t_dm_group_16.gif' length=16 width=16 border=0>";
					else {
						html += "<img src='wdk/theme/documentum/icons/type/t_dm_user_16.gif' length=16 width=16 border=0>";
						osname = " ["+subuser.getUserOSName()+"]";
					}
					
					html += baselink+"&page=objdump&id="+subuser.getObjectId().getId()+"\">"+name+"</a>"+osname;
				}
			}
			// groups they belong to?
			html += "<hr>";
			html += "Group Membership:<br>";
			html += generateQueryTable("SELECT r_object_id, group_name, group_address, description FROM dm_group WHERE any groups_names = '"+group.getGroupName()+"' order by group_name"); 
			html += "<hr>";
			
		} else {
			IDfUser user = (IDfUser)userobj;
			html += "Username: "+user.getUserName() + "<BR>";
			html += "OSName: "+user.getUserOSName()+"<BR>";
			if (user.getUserState() == IDfUser.DF_USER_INACTIVE) html += "<font color='RED'>DISABLED</font><br>";
			if (user.getUserState() == IDfUser.DF_USER_LOCKED)   html += "<font color='RED'>LOCKED</font><br>";
			if (user.getUserState() == IDfUser.DF_USER_LOCKED_INACTIVE) html += "<font color='RED'>LOCKED and DISABLED</font><br>";
			html += "<hr>";
			html += "Privleges: <b>";
			switch(user.getUserPrivileges())
			{
				case IDfUser.DF_PRIVILEGE_NONE : html += "NONE (0)"; break;
				case IDfUser.DF_PRIVILEGE_CREATE_TYPE : html += "Create Type (1)"; break;
				case IDfUser.DF_PRIVILEGE_CREATE_CABINET : html += "Create Cabinet (2)"; break;
				case IDfUser.DF_PRIVILEGE_CREATE_GROUP : html += "Create Group (4)"; break;
				case IDfUser.DF_PRIVILEGE_SYSADMIN : html += "System Administrator (8)"; break;
				case IDfUser.DF_PRIVILEGE_SUPERUSER : html += "<font color='RED'>SuperUser (16)</font>"; break;
			}
			html += "</b><BR>";
			user.getUserXPrivileges();
			html += "Extended Privleges: <b>";
			boolean trigger = false; String sep = ""; 
			if ((user.getUserXPrivileges() & IDfUser.DF_XPRIVILEGE_VIEW_AUDIT) > 0) { html += "View Audit"; trigger = true; }
			if ((user.getUserXPrivileges() & IDfUser.DF_XPRIVILEGE_CONFIG_AUDIT) > 0) { if (trigger) sep = " - "; html += sep+"Config Audit"; trigger = true; }
			if ((user.getUserXPrivileges() & IDfUser.DF_XPRIVILEGE_PURGE_AUDIT) > 0) { if (trigger) sep = " - "; html += sep+"Purge Audit"; }
			html += "</b><BR>";
			String clientcapability = "unknown client capability";
			switch(user.getClientCapability()){
			    case IDfUser.DF_CAPABILITY_NONE         : clientcapability = "none"; break;
			    case IDfUser.DF_CAPABILITY_CONSUMER     : clientcapability = "Consumer"; break;
			    case IDfUser.DF_CAPABILITY_CONTRIBUTOR  : clientcapability = "Contributor"; break;
			    case IDfUser.DF_CAPABILITY_COORDINATOR  : clientcapability = "Coordinator"; break;
			    case IDfUser.DF_CAPABILITY_SYSTEM_ADMIN : clientcapability = "System Administrator"; break;
			}
			html += "Client Capability: <b>"+clientcapability+" ("+user.getClientCapability()+")</b><BR>";
			html += "<hr>";
			html += "Default Group: "+user.getUserGroupName()+"<BR>"; 
			html += "Default ACL: "+user.getString("acl_name")+"<BR>";
			// PRIVLEDGES
			// extended privledges
			html += "<hr>";
			html += "Group Membership:<br>";
			html += generateQueryTable("SELECT r_object_id, group_name, group_address, description FROM dm_group WHERE any users_names = '"+user.getUserName()+"' order by group_name"); 
			html += "<hr>";
			
		}

		sMgr.release(session);
		return html;
		
	}
	
	public String processXML(String dump)
	{
		dump = dump.replaceAll("<","&lt;");
		dump = dump.replaceAll(">","&gt;");
		dump = dump.replaceAll(" ","&nbsp;");
		dump = dump.replaceAll("\n","<br>");
		return "<FONT face='Courier New'>" + dump + "</FONT>";
	}
	
	public String dumpMrcsApplication(HttpServletRequest request) throws Exception
	{
		String mrcsapp = request.getParameter("name");
		IntrospectionFactory config = IntrospectionFactory.getConfig();
		
		String dump = config.getMrcsApplication(mrcsapp);
		dump = processXML(dump);
		return getHeader()+dump;
	}

	public String dumpMrcsDocumentType(HttpServletRequest request) throws Exception
	{
		String mrcsapp = request.getParameter("name");
		String doctype = request.getParameter("config");
		IntrospectionFactory config = IntrospectionFactory.getConfig();
		
		String dump = config.getMrcsDocumentType(mrcsapp,doctype);
		dump = processXML(dump);
		return getHeader()+dump;
	}

	public String dumpMrcsFolderType(HttpServletRequest request) throws Exception
	{
		String mrcsapp = request.getParameter("name");
		String doctype = request.getParameter("config");
		IntrospectionFactory config = IntrospectionFactory.getConfig();
		
		String dump = config.getMrcsFolderType(mrcsapp,doctype);
		dump = processXML(dump);
		return getHeader()+dump;
	}
	
	public String dumpDocument(String docid) throws Exception
	{
		IDfSession session = this.getSession();
		IDfDocument doc = (IDfDocument)session.getObject(new DfId(docid));	
				
		// this is SOOOOO 1998 !!!
		String baselink = " <a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase;		
		String html = "Object Name: "+doc.getObjectName()+"<br>";
		html += "Object Id: "+docid+"<br><hr>";
		String lockowner = doc.getLockOwner();
		html += "Version: "+doc.getVersionLabels().getImplicitVersionLabel()+(!"".equals(lockowner)?" (Locked by "+lockowner+")":"")+"<br>";
		try {
			html += "Current State: "+doc.getCurrentStateName()+" ["+doc.getCurrentState()+"]<BR>";
		} catch (Exception e) {
			html += "No current state for document<BR>";						
		}

		// folder (first folder in i_folder attr) of the document
		try {
			IDfId folderid = doc.getFolderId(0);
			IDfFolder folder = (IDfFolder)session.getObject(folderid);
			
			html += "Folder[0]: "+baselink+"&page=objdump&id="+folderid+"\">"+folder.getFolderPath(0)+"</a><BR>";

		} catch (Exception e) {
			html += " No folder found";			
		}
		// attached policy
		try {
			String policyid = doc.getPolicyId().getId();
			String policylink = "View Lifecycle "+baselink+"&page=objdump&id="+policyid+"\">"+policyid+"</a><BR>";
			html += policylink;
		} catch (Exception e) {
			html += " No Policy Attached ";			
		}
		// attached ACL
		try {
			String acllink = "View ACL "+baselink+"&page=objdump&id="+doc.getACL().getObjectId().getId()+"\">"+doc.getACLName()+"</a><BR>"; 
			html += acllink;
		} catch (Exception e) {
			html += " no ACL attached ";						
		}
		// MRCS attributes
		if (doc.hasAttr("mrcs_application"))
		{
			String mrcsapp = doc.getString("mrcs_application");
			String mrcsapplink = "View MRCS Application "+baselink+"&page=mrcs_application&name="+mrcsapp+"\">"+mrcsapp+"</a><BR>";
			html += mrcsapplink;
		}
		if (doc.hasAttr("mrcs_config"))
		{
			String mrcsapp = doc.getString("mrcs_application");
			String mrcsconfig = doc.getString("mrcs_config");
			String mrcsapplink = "View MRCS Document config "+baselink+"&page=mrcs_document_config&name="+mrcsapp+"&config="+mrcsconfig+"\">"+mrcsconfig+"</a><BR>";
			html += mrcsapplink;
		}
		if (doc.hasAttr("mrcs_folder_config"))
		{
			String mrcsapp = doc.getString("mrcs_application");
			String mrcsconfig = doc.getString("mrcs_folder_config");
			String mrcsapplink = "View MRCS Folder config "+baselink+"&page=mrcs_folder_config&name="+mrcsapp+"&config="+mrcsconfig+"\">"+mrcsconfig+"</a><BR>";
			html += mrcsapplink;
		}
		
		// check for relationships
		try {
			boolean hasnorel = true;
			String dql = "SELECT count(*) as one from dm_relation where parent_id = '"+docid+"'";
			
			int count = 0;
			
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	count = myObj1.getInt("one");
	        }
	        myObj1.close();
	        if (count > 0) {
	        	hasnorel = false;
	        	String relationlink = baselink+"&page=dm_relation&id="+docid+"\">Has Parent Relation</a><BR>";
	        	html += relationlink;
	        }

	        dql = "SELECT count(*) as one from dm_relation where child_id = '"+docid+"'";
			
			count = 0;
			
	        qry.setDQL(dql);
	        
	        myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	count = myObj1.getInt("one");
	        }
	        myObj1.close();
	        if (count > 0) {
	        	hasnorel = false;
	        	String relationlink = baselink+"&page=dm_childrelation&id="+docid+"\">Has Child Relation</a><BR>";
	        	html += relationlink;
	        }

	        if (hasnorel) { 
	        	html += "No Relationships<BR> "; 
        	}
			
		} catch (Exception e) {
			html += "Relationship Error ";
		}
		
		// check for workflows 
		try {
			String dql = "SELECT count(r_workflow_id) as one from dmi_package where any r_component_id = '"+docid+"'";
			
			int count = 0;
			
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	        
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	count = myObj1.getInt("one");
	        }
	        myObj1.close();
	        if (count > 0) {
	        	String relationlink = baselink+"&page=documentworkflow&id="+docid+"\">View Workflows</a><BR>";
	        	html += relationlink;
	        } else { 
	        	html += "No Workflows<BR> "; 
        	}
			
		} catch (Exception e) {
			html += "Workflow Error ";
		}
		// version tree link
		String verlink = baselink+"&page=documentversiontree&id="+docid+"\">View Version Tree </a><BR>"; 
		html += verlink;

		// format
		try { 
			String fmtlink = baselink+"&page=objdump&id="+doc.getFormat().getObjectId().getId()+"\">View Format "+doc.getFormat().getName()+"</a><BR>";
			html += fmtlink;
		} catch (Exception e) {
			html += "null format<BR>";
		}

		// renditions
		html += "<hr><img src='wdk/theme/documentum/images/tree/Tload.gif' " +
				          "onClick='document.getElementById(\"renditions\").style.display = (document.getElementById(\"renditions\").style.display==\"block\"?\"none\":\"block\")'>" +
				"View Renditions --<BR><div id='renditions' style='display:none'>";
		html += generateQueryTable("SELECT full_content_size as content_size,set_time,full_format,r_object_id,owner_name,r_link_cnt,r_is_virtual_doc,r_object_type,r_lock_owner, i_is_reference, name as storage_name, i_is_replica,r_policy_id FROM dm_sysobject (ALL), dmr_content, dm_store WHERE r_object_id = ID('"+docid+"') AND ANY (parent_id=ID('"+docid+"') AND page = 0) and dmr_content.storage_id=dm_store.r_object_id"); 
		html += "</div>";
		
		// rollback/snapshots
		html += "<hr><img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"snapshots\").style.display = (document.getElementById(\"snapshots\").style.display==\"block\"?\"none\":\"block\")'>WF/LC Snapshots --<BR><div id='snapshots' style='display:none'>";
		html += generateQueryTable("select * from mrcs_document_snapshot where doc_chronicle_id ='" +doc.getChronicleId() +"' ORDER BY r_object_id DESC"); 
		html += "</div>";

		// audit trail
		html += "<hr><img src='wdk/theme/documentum/images/tree/Tload.gif' onClick='document.getElementById(\"trail\").style.display = (document.getElementById(\"trail\").style.display==\"block\"?\"none\":\"block\")'>Audit Trail --<BR><div id='trail' style='display:none'>";
		html += generateQueryTable("select * from dm_audittrail where audited_obj_id ='" +docid +"'");
		html += "</div>";
		
		// modify attr link
		html += "<hr>"+baselink+"&page=modifyattrs&objectid="+docid+"\">Modify Attributes</a><hr>";
						
		sMgr.release(session);
		
		return html;
		
	}
	
	String processDump(String id) throws Exception
	{
		IDfSession session = this.getSession();
		//String dump = session.apiGet("dump,"+session.getSessionId()+","+id,null);
		String dump = session.apiGet("dump,c,"+id,null);
		
		dump = dump.replaceAll(" ","&nbsp;");
		dump = dump.replaceAll("\n","<br>");
		dump = "<FONT face='Courier New'>" + dump + "</FONT>";
		
		// try to process id attributes into links
		try { 
			IDfPersistentObject perobj = session.getObject(new DfId(id));
			String type = perobj.getType().getName();
			String header = "Object Type: "+type+"<br>";
			dump = header + dump; 
			ArrayList idattrs = new ArrayList();
			String baselink = "<a href=\""+baseurl+"?fillerargument=alksdfjlkasdjlfjasldfjasdifjioawejjsdfkljflsdkajflksdjlasdkjflkasdjflasdjfklajldfkjasdlfaslkdfjlaskdjfklasjdfljasdlkfjlaskdjflkajsdlfjasldflasjdlfjasldkfjklasdjflkajsdlfjlasdfjlkasjdfkljasldkj&username="+username+"&password="+password+"&docbase="+docbase+"&page=objdump";				
			for (int i=0; i < perobj.getAttrCount(); i++)
			{
				IDfAttr attr = perobj.getAttr(i);
				if (attr.getDataType() == IDfAttr.DM_ID)
				{
					if (attr.isRepeating())
					{
						for (int j=0; j < perobj.getValueCount(attr.getName()); j++)
						{
							IDfId anid = perobj.getRepeatingId(attr.getName(),j);
							if (!anid.getId().equals("0000000000000000")) {
								String search = "&nbsp;"+anid.getId();
								String repl = "&nbsp;"+baselink+"&id="+anid.getId()+"\">"+anid.getId()+"</a>";
								int pos = dump.indexOf(search);
								if (pos != -1)
								{
									dump = dump.substring(0,pos) + repl + dump.substring(pos+search.length()); 
								}
							}
						}
					}
					else
					{
						IDfId anid = perobj.getId(attr.getName());
						if (!anid.getId().equals("0000000000000000")) {
							String search = "&nbsp;"+anid.getId();
							String repl = "&nbsp;"+baselink+"&id="+anid.getId()+"\">"+anid.getId()+"</a>";
							int pos = dump.indexOf(search);
							if (pos != -1)
							{
								dump = dump.substring(0,pos) + repl + dump.substring(pos+search.length()); 
							}
						}
					}
				}
			}
		}catch (Exception e) {}
		sMgr.release(session);
		return dump;
		
	}
	
	
	public IDfSession getSession()
	{
		try {
			if (sMgr == null) {
		       	client = clientx.getLocalClient();
		       	sMgr = client.newSessionManager();
		       	IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
		        loginInfoObj.setUser(username);
		        loginInfoObj.setPassword(password);
		        loginInfoObj.setDomain(null);
		        sMgr.setIdentity(docbase, loginInfoObj);
			}
	        return sMgr.getSession(docbase);
        }
        catch (DfException dfe){
        	throw new RuntimeException(dfe);
        }
	}
	
}
