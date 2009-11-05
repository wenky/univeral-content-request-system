package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;

public class AuditEFSApprovedCopies 
{
	
	public static void main(String[] args) throws Exception
	{
		ConversionOperation op = new ConversionOperation();
		op.username = "mradmin";
		op.password = "mr2006";
		op.docbase = "mrcs";
		op.Configuration = new HashMap();
		op.Configuration.put("SourceDocumentsQuery","SELECT r_object_id FROM m_mrcs_central_document");
		op.Configuration.put("EffectiveState","Approved");
		op.Configuration.put("ApprovedPath","Approved");
		op.Configuration.put("RelationName","CopyToRelationship");
		
		AuditEFSApprovedCopies audit = new AuditEFSApprovedCopies(); 
		
		audit.auditApprovedCopies(null,op);
	}
	
	/**
	 *
	 * operation inputs/config:
	 *   SourceDocumentsQuery - string - query the produces a list of documents to check, 
	 *                                   should produce r_object_id column as first column
	 *   EffectiveState - string - source document state that is copied to the Approved folder
	 *   ApprovedPath - string - path to append to the source document's path to manually locate the approved folder
	 *   RelationName = string - name that all relationships should have
	 */
	public void auditApprovedCopies(IDfSession session,ConversionOperation op) throws Exception
	{
		
		// get a list of files (input query)		
		String dql = (String)op.Configuration.get("SourceDocumentsQuery");  // select 
		String effectivestate = (String)op.Configuration.get("EffectiveState");
		String approvedpath = (String)op.Configuration.get("ApprovedPath");
		String relationname = (String)op.Configuration.get("RelationName");
		
		// exec query
		try { 
			
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
	
	        ArrayList idlist = new ArrayList();
	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        while (myObj1.next()) 
	        {
	        	String curid = myObj1.getString("r_object_id");
	        	// add object to list
	        	idlist.add(curid);
	        }
	        myObj1.close();

	        // iterate through id list
	        for (int i=0; i < idlist.size(); i++)
	        {
	        	String topid = (String)idlist.get(i);
	        	String approvedid = topid;
	        	boolean isapproved = false;
	        	// determine if document isApproved - descend through version tree to find document in Approved state
	        	while (true)
	        	{
		        	IDfSysObject sysobj = (IDfSysObject)session.getObject(new DfId(approvedid));
		        	if (effectivestate.equals(sysobj.getCurrentStateName()))
		        	{
		        		isapproved = true;
		        		break;
		        	}
		        	if (sysobj.getChronicleId().getId().equals(approvedid))
		        	{
		        		// we've reached the bottom of the tree
		        		break;
		        	}
		        	approvedid = sysobj.getAntecedentId().getId();
	        	}
	        	
	        	if (isapproved)
	        	{
	        		// get approvedid
		        	IDfSysObject sysobj = (IDfSysObject)session.getObject(new DfId(approvedid));
		        	
		        	// get approved folder for this object
		        	String sourcepath = ((IDfFolder)session.getObject(sysobj.getFolderId(0))).getFolderPath(0);
		        	String copypath = sourcepath+'/'+approvedpath;
		        	
		        	// find approved copy or copies
		        	Map docstatus = new HashMap();
			        IDfQuery cpyqry = new DfQuery();
			        cpyqry.setDQL("SELECT r_object_id FROM dm_document WHERE object_name = '"+sysobj.getObjectName()+"'");
			        IDfCollection copies = (IDfCollection) cpyqry.execute(session, IDfQuery.DF_READ_QUERY);
			        while (copies.next()) 
			        {
			        	String copyid = copies.getString("r_object_id");
			        	IDfSysObject copyobj = (IDfSysObject)session.getObject(new DfId(copyid));
			        	String path = ((IDfFolder)session.getObject(copyobj.getFolderId(0))).getFolderPath(0);
			        	if (sourcepath.equals(path))
			        	{
			        		// source object - ignore...
			        	} else if (copypath.equals(path)) {			        		
			        		// copy object
			        	}
			        }
			        copies.close();
		        	
		        	// has relation?
			        IDfQuery relqry = new DfQuery();
			        relqry.setDQL("SELECT r_object_id, relation_name, parent_id, child_id " +
			        		      "FROM dm_relation " +
			        		      "WHERE parent_id IN (SELECT r_object_id FROM dm_document(all) " +
			        		                          "WHERE i_chronicle_id = '"+sysobj.getChronicleId().getId()+"') " +
			        		      "ORDER BY parent_id DESC");
			        IDfCollection relations = (IDfCollection) relqry.execute(session, IDfQuery.DF_READ_QUERY);
			        while (relations.next()) 
			        {
			        	String curid = relations.getString("r_object_id");
			        }
			        relations.close();
		        	
	        		
	        		
	        	}
	        	
	        	
	        }
		} catch (DfException dfe) {
			throw new RuntimeException(dfe);
		}
		
		
		

		BaseClass.sMgr.release(session);
		
	}
	

}
