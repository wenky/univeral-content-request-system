package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfRelationType;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;

public class RebuildApprovedCopiesProcessor {
	
	public static Map backupExistingApprovedCopies(IDfSession session, String folder, String newACL, List doclist) throws Exception
	{
		// doclist is the list of approved copy documents to backup
		// folder is the folder to move them to
		// - we will move all the documents in doclist to the specified folder
		// - we will copy the previous folder path of the approved Copy in the "subject" attribute of the approved copy
		// - we will disable all relationships, but the relationship will have the old parent/child id mappings
		//   in the relation's description field in the format 'p[parentid] c[childid]'
		// - relation disabling will be done for all versions in the approved copy version tree
		// - after this has run, make sure all the source docs that will have reconstructed approved copies do not have any relations 
		//   besides dm_subscription, to ensure there aren't broken relations on that end...
		List error = new ArrayList();
		List success = new ArrayList();

        /*-DEBUG-*/System.out.println("backupExistingApprovedCopies- top");
        
		String systemdomain = session.getServerConfig().getString("operator_name");
		IDfACL hideacl = session.getACL(systemdomain,newACL);
        
		
		for (int i=0; i < doclist.size(); i++)
		{
        	DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
        	String approvedcopyid = dor.objectid;
    	    // move it!
    		boolean tranfailed = false;
    		BaseClass.sMgr.beginTransaction();
    		try {
        		// look up approved copy document
        		IDfDocument doc = (IDfDocument)session.getObject(new DfId(approvedcopyid));
        		// look up folder to which we're moving the approved copies
        		IDfFolder destfolder = session.getFolderByPath(folder);
        		// get current Approved folder path of the document
        		String currentfolder = ((IDfFolder)session.getObject(doc.getFolderId(0))).getFolderPath(0);
    			// move it to the new folder
    			doc.unlink(currentfolder);
    			doc.link(destfolder.getFolderPath(0));
    			// save old path in subject attribute 
    			doc.setString("subject",currentfolder);
    			doc.setACL(hideacl);
    			doc.save();
    	        /*-DEBUG-*/DfLogger.debug(RebuildApprovedCopiesProcessor.class, "backupExistingApprovedCopies- "+new Date()+" backed up #"+i+" - moved docid "+dor.objectid, null, null);
    			// look for relations
    			// select * from dm_relation where child_id in (select r_object_id from dm_document(all) where i_chronicle_id = 'doc.getChronicleId());
    			String dql = "select * from dm_relation where child_id in (select r_object_id from dm_document(all) where i_chronicle_id = '"+doc.getChronicleId()+"')";
    	        IDfQuery qry = new DfQuery();
    	        qry.setDQL(dql);

    	        // hide/unlink relations from source
    	        IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
    	        while (myObj1.next()) 
    	        {
    	        	String relationid = myObj1.getString("r_object_id");
    	        	IDfRelation relation = (IDfRelation)session.getObject(new DfId(relationid));
    	        	relation.setDescription("p["+relation.getParentId()+"] c["+relation.getChildId()+"]");
    	        	// point the parent to the child/approved copy
    	        	relation.setParentId(DfId.DF_NULLID);
    	        	relation.save();
        	        /*-DEBUG-*/System.out.println("backupExistingApprovedCopies- "+new Date()+"  backed up #"+i+" - purged relation "+relationid);
    	        	// apply an ACL to hide it from searches?
    	        }
    	        myObj1.close();
    			
    		} catch (Exception e) {
    	        /*-ERROR-*/System.out.println("backupExistingApprovedCopies- error in backup #"+i+" - ERROR IN id "+dor.objectid); e.printStackTrace();
    			dor.note = e.toString();
    			dor.error = e;
    			BaseClass.sMgr.abortTransaction();
    			error.add(dor);
    			tranfailed = true;
    		}
    		if (!tranfailed) {
    			BaseClass.sMgr.commitTransaction();
    			success.add(dor);
    		}
		}
        /*-DEBUG-*/System.out.println("backupExistingApprovedCopies- done");        	
		Map results = new HashMap();
		results.put("error",error);
		results.put("success",success);
		return results;
	}
	
	public static Map rebuildApprovedCopies(IDfSession session, List doclist, Map approvalstates, String copydocumenttype, List copyattrs, String renditionformat,
			                                String approvedfoldername, String relationdescription, String relationtypename, String copyaclname) throws Exception
	{
		// biiigggg momma! here we go, rebuilds approved copies from docs listed in doclist
		// - doclist is a list of SOURCE documents, not approved copy documents
		// - doclist does not include previous versions of source documents, only CURRENT documents. This method will descend source trees as part
		//   of the approved copy reconstruction process (actually we will ascend from the bottom up from the source chronicle id)
		// - assumes the approved folders have been cleared out already (see backupExistingApprovedCopies method above to clear them out)
		// - assumes the source documents have been checked/scrubbed for dangling relationships
		
		List error = new ArrayList();
		List success = new ArrayList();
        /*-DEBUG-*/System.out.println("rebuildApprovedCopies- top");        	
		
		Map folderidcache = new HashMap();
		
		for (int i=0; i < doclist.size(); i++)
		{
        	DocbaseObjectRecord dor = (DocbaseObjectRecord)doclist.get(i);
            /*-DEBUG-*/System.out.println("rebuildApprovedCopies- processing #"+i+" - id "+dor.objectid);        	
        	String sourceid = dor.objectid;
    		boolean tranfailed = false;
    		String successmsg = "no approved versions"; // default
    		int approvedversionscount = 0; 
    		BaseClass.sMgr.beginTransaction();
    		try {
    			// look up source document
    			IDfDocument sourcedoc = (IDfDocument)session.getObject(new DfId(sourceid));
    			// get current folder
    			// -- optimizing this lookup with a hashmap cache
    			String folderid = sourcedoc.getFolderId(0).getId();
    			String currentfolder = null;
    			if (folderidcache.containsKey(folderid))
    			{   // cache value found in hashmap
    				currentfolder = (String)folderidcache.get(folderid);
    			} else { // not cached, lookup and cache
    				currentfolder = ((IDfFolder)session.getObject(sourcedoc.getFolderId(0))).getFolderPath(0); 
    				folderidcache.put(folderid,currentfolder);
    			}
        		// calculate approved copies folder using currentfolder as a basis
    			String id = sourcedoc.getChronicleId().getId();
    			// go up the document tree
    			// - need to figure out a way to add all the copied subversions to the success list...
    			IDfDocument copydoc = null;
				IDfDocument treedoc = (IDfDocument)session.getObject(new DfId(id));
    			while (true)
    			{
    				// check if this is an approvalstate
    				if (approvalstates.containsKey(treedoc.getCurrentStateName())) 
    				{
    					approvedversionscount++; successmsg = "approved count: "+approvedversionscount;
    					boolean isNew = false;
    					if (copydoc == null)
    					{
    						// create the copy    						
    						if (copydocumenttype == null) {
    							// the copy will be the same type as the source doc
    							copydoc = (IDfDocument)session.newObject(treedoc.getTypeName());
    						} else {
    							copydoc = (IDfDocument)session.newObject(copydocumenttype);
    						}
    						isNew = true;
    					} else {
    						// check it out in preparation for versioning
    						copydoc.checkout();
    						isNew = false;
    					}
    					// copy attributes
    					copydoc.setObjectName(treedoc.getObjectName());
    					copyAttributes (session,treedoc,copydoc,copyattrs);
    			        // version copy
    			        
    					if (renditionformat == null) // source copy
    					{
    			            String filePath = treedoc.getFile(""); //getPath(0);
    						copydoc.setFileEx(filePath, treedoc.getFormat().getName(), 0, null);
    					} else { // copy rendition
    						IDfTypedObject rendition = null;
    		                IDfCollection myColl = treedoc.getRenditions( "full_format" );
    		                while ( myColl.next() ) {
    		                    String currentformat = myColl.getString("full_format");
    		                    if (currentformat != null && currentformat.equals(renditionformat))
    		                    {
    		                        rendition = myColl.getTypedObject();
    		                        break;
    		                    }
    		                }
    		                myColl.close();
    	                    String filePath = treedoc.getFileEx(null, renditionformat, 0, false);    	                    
    	                    copydoc.setFileEx(filePath, renditionformat, 0, null);
    					}
    					// calculate and link to folder
    			        IDfFolder basefolder = getGroupingFolderBase(session,treedoc);
    			        String targetpath = basefolder.getFolderPath(0)+'/'+approvedfoldername;    	
    			        IDfFolder destFolder = null;
    			        IDfId postversionedid = null;;

    			        destFolder = session.getFolderByPath(targetpath);
    			        //IDfVersionPolicy verPolicy = treedoc.getVersionPolicy();
    			        //String label = verPolicy.getSameLabel();
    			        String label = treedoc.getVersionLabels().getImplicitVersionLabel();

    			        // Save the document in the Docbase
    			        if (isNew) {
    			            copydoc.link(destFolder.getObjectId().toString());
    			            copydoc.mark(label);
    			            copydoc.mark("CURRENT");
    			            copydoc.save();
    			            postversionedid = copydoc.getObjectId();
    			            /*-DEBUG-*/System.out.println("rebuildApprovedCopies- processing #"+i+" - saved new copy"+postversionedid.getId());        	    			            
    			        } else {
    			            copydoc.mark("CURRENT");
    			            postversionedid = copydoc.checkin(false, label);
    			            /*-DEBUG-*/System.out.println("rebuildApprovedCopies- processing #"+i+" - versioning copy "+postversionedid.getId());        	    			            
    			        }
    			        
    			        // refetch?
    			        copydoc = (IDfDocument)session.getObject(postversionedid);
    			        
    					
    					// apply the copy's acl
    			        String systemdomain = session.getServerConfig().getString("operator_name");
    			        IDfACL copyACL = session.getACL(systemdomain, copyaclname);
    			        copydoc.setACL(copyACL);
    			        // saving...
    			        copydoc.save();
    			        
    			        // relation...
    			        setRelation(session,relationtypename,treedoc,postversionedid,relationdescription);
    				}
    				if (treedoc.getObjectId().getId().equals(sourcedoc.getObjectId().getId())) {
    					// we may be able to use isCurrent()...
    					break; // we are at the top, time to stop looping
    				} else {
    					// loop up next doc up the chain
    					treedoc = (IDfDocument)session.getObjectByQualification("dm_document(all) where i_antecedent_id = '"+treedoc.getObjectId().getId()+"'");
    				}
    			}
    		} catch (Exception e) {
	            /*-ERROR-*/System.out.println("rebuildApprovedCopies- ERROR in  #"+i+" - id "+dor.objectid);e.printStackTrace();        	    			            
    			dor.note = e.toString();
    			dor.error = e;
    			BaseClass.sMgr.abortTransaction();
    			error.add(dor);
    			tranfailed = true;
    		}
    		if (!tranfailed) {
    			BaseClass.sMgr.commitTransaction();
    			dor.note = successmsg;
    			success.add(dor);
    		}
		}
        /*-DEBUG-*/System.out.println("rebuildApprovedCopies- done");        	    			            
		Map results = new HashMap();
		results.put("error",error);
		results.put("success",success);
		return results;
	}
	
    public static void copyAttributes(IDfSession session, IDfDocument parentDoc, IDfDocument childDoc, List attrlist) throws Exception
    {
        // copy MRCS attrs if possible
        if (parentDoc.hasAttr("mrcs_application"))
        {
            childDoc.setString("mrcs_application",parentDoc.getString("mrcs_application"));
        }
        if (parentDoc.hasAttr("mrcs_config"))
        {
            childDoc.setString("mrcs_config",parentDoc.getString("mrcs_config"));
        }
        // process custom attr list        
        for (int i=0; i < attrlist.size(); i++)
        {
            String attrname = (String)attrlist.get(i);
            boolean isRepeating = parentDoc.isAttrRepeating(attrname);
            if (isRepeating) 
            {
                for (int r=0; r < parentDoc.getValueCount(attrname); r++)
                {
                    IDfValue v = parentDoc.getRepeatingValue(attrname,r);
                    childDoc.setRepeatingValue(attrname,r,v);
                }                
            } else {
                IDfValue attrvalue = parentDoc.getValue(attrname);
                childDoc.setValue(attrname,attrvalue);                
            }
        }
    }
    
    public static void setRelation(IDfSession session, String relation_name, IDfDocument parentDoc, IDfId childDoc, String description) throws Exception 
	{        
	    // get relation type, create if necessary (this may need recoding...)
	    IDfRelationType relationType = session.getRelationType(relation_name);    
	    IDfRelation docRelation = null;
	    docRelation = parentDoc.addChildRelative(relationType.getRelationName(), childDoc, null, false,description);
	    docRelation.save(); 
	}

    public static IDfFolder getGroupingFolderBase(IDfSession session, IDfDocument doc) throws Exception 
    {
        // look up the grouping folder base
        IDfId parentfolderid = doc.getFolderId(0);        
        IDfId basefolderid = null;
        IDfSysObject parentfolder = (IDfSysObject)session.getObject(parentfolderid);
        // is this a mrcs subfolder?
        IDfFolder gfroot = null;
        if (parentfolder.hasAttr("mrcs_grouping_folder_root"))
        {
            basefolderid = new DfId(parentfolder.getString("mrcs_grouping_folder_root"));
        }
        // is this the root of the current grouping folder?
        else if (parentfolder.hasAttr("mrcs_config"))
        {
            // this is the root (since it isn't a subfolder, but is an MRCS folder)
            basefolderid = parentfolderid;
        }
        else
        {
            throw new Exception("folder is not an MRCS folder");
        }
    
        IDfFolder basefolder = (IDfFolder)session.getObject(basefolderid);
        return basefolder;
    }
	
    
    
}
