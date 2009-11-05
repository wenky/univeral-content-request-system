/*
 * Created on Apr 22, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: CopyRenditionToSystemFolder.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2006/11/14 19:33:29 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

/**
 * @author carl mueller
 * 
 * Extension of CopyToSystemFolder that copies a rendition, not the base source of the document
 * 
 * NOTE: location relative path must not begin with a /, I do this for you...
 *
 * Config for this plugin:
 *   RelationTypeName - DCTM object name of RelationType to use for relating the child copy to the parent doc
 *                      NOTE: you should use a different RelationType per rendition type you are copying. so if 
 *                            there is a .doc and a .pdf that could be copied to different subfolders, use a 
 *                            differently named RelationType for the two types of renditions
 *   RelationDescription - whatever
 *   RenditionFormat - DCTM format of the renditions that are to be copied
 *   CopyDocLabel - used in labelling while versioning the rendition copy
 *   CopyDocACL - acl to apply to rendition child copies
 *   CopyToProperty or CopyToLocation - specify if the destination system folder is hardcoded in config (CopyToLocation), 
 *                                      or dynamically assigned based on the value of a property of the parent document (CopyToProperty)
 */
public class CopyRenditionToSystemFolder extends CopyToSystemFolder implements IStateTransitionPlugin, IMrcsLifecyclePlugin
{

	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
        /*-CFG-*/String m="execute(new)-";        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"4.2 call invoked, calling 4.1.2 plugin",null,null);
        try { 
        	doexecute(sMgr,docbase,null,mrcsapp,null,(IDfDocument)mrcsdocument,config,context);
        }catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+"error",null,e);
        	throw new RuntimeException ("Error in CopyRenditionToSystemFolder plugin: "+e,e);
        }
	}

    public void execute(IDfSessionManager smgr, String docbase, StateInfo currentstate, String mrcsapp, IDfDocument preversioned, IDfDocument postversioned, Map configdata, Map customdata) throws Exception
    {
        //smgr = useSystemAccount(mrcsapp,smgr,configdata);
        /*-CFG-*/String m="execute(old)-";        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"CALLING doexecute",null,null);
        doexecute(smgr,docbase,currentstate,mrcsapp,preversioned,postversioned,configdata,customdata);
    }

	
    public void doexecute(IDfSessionManager smgr, String docbase, StateInfo currentstate, String mrcsapp, IDfDocument preversioned, IDfDocument postversioned, Map configdata, Map customdata) throws Exception
    {
        //smgr = useSystemAccount(mrcsapp,smgr,configdata);
        /*-CFG-*/String m="execute(old)-";        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting session for plugin (paranoid coding)",null,null);
        IDfSession session = smgr.getSession(docbase);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting relation config",null,null);
        String relationtypename = (String)configdata.get("RelationTypeName");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relation type: "+relationtypename,null,null);
        String relationdescription = (String)configdata.get("RelationDescription");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relation desc: "+relationdescription,null,null);
        String renditionformat = (String)configdata.get("RenditionFormat");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"renditon format: "+renditionformat,null,null);
        
        // get configured format information
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting rendition format object",null,null);
        IDfFormat formatObj = session.getFormat( renditionformat );
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format found? "+(formatObj != null),null,null);
        
        try {
            
            // locate the current document's rendition
            IDfDocument childDoc = null;
            boolean isNew = false; // flags if childdoc is a new document or an update to an existing one
            IDfId childId = null;
            IDfDocument parentDoc = null;

            IDfCollection myColl = null;              
            IDfTypedObject rendition = null;
            try{
                // STEP 1: check for renditions, if not found, return. 
                // locate renditions, matching on  desired rendition format
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get rendition collection for postversioned document",null,null);
                myColl = postversioned.getRenditions( "full_format" );
                while ( myColl.next() ) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing next rendition...",null,null);
                    String currentformat = myColl.getString("full_format");
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current rendition format: "+currentformat,null,null);
                    if (currentformat != null && currentformat.equals(renditionformat))
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current rendition matches configured rendition format to be copied",null,null);
                        // get the id of the current rendition
                        rendition = myColl.getTypedObject();                        
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"rendition id: "+rendition,null,null);
                    }
                }
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"rendition found? "+(rendition!=null),null,null);
                
                if (rendition != null)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"rendition found for format",null,null);
                    
                    // STEP 2: determine if there is already a child relation for the parent doc for this rendition 
                    //         - rendition type name is important here, a different relation type should be created for each format of rendition you want to copy 
                    //         - if you want to copy multiple rendition formats for the same document, they  should have different relation type names
                    parentDoc = postversioned; 
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"look up id of rendition copy (the child id), if it is there",null,null);
                    childId = getChildId(session,parentDoc.getObjectId().getId(),relationtypename);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"rendition copy child id: "+childId,null,null);
                    
                    
                    // STEP 3: create new sysobject for the rendition copy if the child is not found, 
                    //         or get the rendition copy so we can rev it if the child is found
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting relationship parent Doc to postversioned: "+postversioned.getObjectId(),null,null);            
                  
                    // Create a dm_document object for the rendition copy
                    if (childId != null) {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"child id/copy found",null,null);
                        childDoc = (IDfDocument) session.getObject(childId);
                        isNew = false;
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking out current copy",null,null);
                        childDoc.checkout();
                    } else {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"no child/copy found, creating new object of type "+parentDoc.getType().getName(),null,null);
                        isNew = true;
                        childDoc = (IDfDocument) session.newObject(parentDoc.getType().getName());
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"new child doc id: "+childDoc.getObjectId(),null,null);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving new child doc",null,null);
                        //childDoc.save();
                    }

                    // copy attrs as needed
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting object name",null,null);
                    List attrlist = getAttrsToCopy(configdata);
                    copyAttributes(session,parentDoc,childDoc,attrlist);
            
                    // STEP 4: update the rendition copy object with the new rendition's content
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting object name",null,null);
                    childDoc.setObjectName(parentDoc.getObjectName());
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting content type",null,null);
                    childDoc.setContentType(renditionformat);
            
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting content file path for rendition format type "+renditionformat,null,null);
                    String filePath = parentDoc.getFileEx(null, renditionformat, 0, false);
            
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--filePath " + filePath,null,null);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--formatName " + renditionformat,null,null);
                    childDoc.setFileEx(filePath, renditionformat, 0, null);
                }
                else
                {
                    // rendition was not found, we will throw an exception
                    /*-ERROR-*/DfLogger.getRootLogger().error(m+"CopyRenditionToSystemFolder was unable to find a rendition of format "+renditionformat+" for document "+postversioned.getObjectId());
                    throw new Exception("CopyRenditionToSystemFolder was unable to find a rendition of format "+renditionformat+" for document "+postversioned.getObjectId());
                }
            }
            finally {
                if (myColl != null)
                { myColl.close(); }
            }                       
            
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting destination folder",null,null);
            String destFolderPath = getCopyToFolder(session, postversioned, configdata, customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--dest folder: "+destFolderPath,null,null);

            // version doc
            String copyDocLabel = (String)configdata.get("CopyDocLabel");
            childId = processChildDoc(session,parentDoc,childDoc,isNew,destFolderPath,copyDocLabel);

            //set ACL
            IDfACL copyDocACL = getCopyDocACL(session,configdata); 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copydoc ACL: "+copyDocACL,null,null);
            childDoc.setACL(copyDocACL);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copydoc ACL set",null,null);
            childDoc.save();

            // set the relation - we have to pass the childId we just obtained from the document save or checkin, since childDoc has an obsolete docid
            // look up relation name and description
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"creating relationship",null,null);
            setRelation(session,relationtypename,postversioned,childId,relationdescription);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relationship completed",null,null);
            
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"Exception thrown during CopyToSystemFolder plugin execution",e);
            throw e;
        }
        finally {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"releasing session",null,null);
            smgr.release(session);
        }
    }
    
}
