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
 Version        4.2.1
 Description
 Created on
 Bug: PDCTM00000479 - caused NPE when copying source document

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: CopyToSystemFolder.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2007/08/14 04:15:07 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.ArrayList;
import java.util.Date;
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
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;
import com.medtronic.documentum.mrcs.config.MrcsLifecycleState;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.server.interfaces.IMrcsLifecyclePlugin;

/**
 * @author usha prabhakar, converted to plugin by carl mueller
 *
 * NOTE: location relative path must not begin with a /, I do this for you...
 *
 * Config for this plugin:
 *   RelationTypeName - DCTM object name of RelationType to use for relating the child copy to the parent doc
 *   RelationDescription - whatever
 *   CopyDocLabel - used in labelling while versioning the copy
 *   CopyDocACL - acl to apply to child copies
 *   CopyToProperty or CopyToLocation - specify if the destination system folder is hardcoded in config (CopyToLocation),
 *                                      or dynamically assigned based on the value of a property of the parent document (CopyToProperty)
 */
public class CopyToSystemFolder implements IStateTransitionPlugin, IMrcsLifecyclePlugin
{

	public void execute(IDfSessionManager sMgr, String docbase, MrcsLifecycleState targetstate,String mrcsapp,IDfSysObject mrcsdocument,Map config, Map context)
	{
        /*-CFG-*/String m="execute(new)-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"4.2 call invoked, calling 4.1.2 plugin",null,null);
        try {
        	execute(sMgr,docbase,null,mrcsapp,null,(IDfDocument)mrcsdocument,config,context);
        }catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this,m+"error",null,e);
        	throw new RuntimeException ("Error in CopyToSystemFolder plugin: "+e,e);
        }
	}


    public void execute(IDfSessionManager smgr, String docbase, StateInfo currentstate, String mrcsapp, IDfDocument preversioned, IDfDocument postversioned, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m="execute(old)-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting session for plugin (paranoid coding)",null,null);
        IDfSession session = smgr.getSession(docbase);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting relation config",null,null);
        String relationtypename = (String)configdata.get("RelationTypeName");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relation: "+relationtypename,null,null);
        String relationdescription = (String)configdata.get("RelationDescription");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relation: "+relationdescription,null,null);

        try {
            //IDfDocument parentDoc = preversioned;

	        //Removed by MJH - PDCTM00000479 - caused NPE when copying source document
            /*-DEBUG-*///if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting child id for preversioned doc checked out? " + preversioned.isCheckedOut(),null,null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting child id for postversioned doc checked out? " + postversioned.isCheckedOut(),null,null);
            IDfDocument parentDoc = postversioned;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting child id for postversioned doc: "+postversioned.getObjectId().getId(),null,null);
            IDfId childId = getChildId(session,parentDoc.getObjectId().getId(),relationtypename);

            //Removed by MJH - PDCTM00000479 - caused NPE when copying source document
            /*-DEBUG-*///if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"preversioned id: "+preversioned.getObjectId().getId(),null,null);
            /*-DEBUG-*///if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"preversioned chronicle id: "+preversioned.getId("i_chronicle_id"),null,null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"postversioned id: "+postversioned.getObjectId().getId(),null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"postversioned chronicle id: "+postversioned.getId("i_chronicle_id"),null,null);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting relationship parent Doc to postversioned: "+postversioned.getObjectId(),null,null);
            IDfDocument childDoc = null;
            boolean isNew = false;

            // Create a dm_document object and set properties
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
                childDoc.save();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"new child doc saved, checked out? "+childDoc.isCheckedOut(),null,null);
            }

            // copy attrs as needed
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting object name",null,null);
            List attrlist = getAttrsToCopy(configdata);
            copyAttributes(session,parentDoc,childDoc,attrlist);

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting object name",null,null);
            childDoc.setObjectName(parentDoc.getObjectName());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting content type",null,null);
            childDoc.setContentType(parentDoc.getContentType());

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting content file path",null,null);
            String filePath = parentDoc.getFile(""); //getPath(0);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting format name",null,null);
            String formatName = parentDoc.getFormat().getName();

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--filePath " + filePath,null,null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--formatName " + formatName,null,null);
            childDoc.setFileEx(filePath, formatName, 0, null);

            // Specify the folder in which to create the document
            IDfFolder destFolder = null;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting destination folder",null,null);
            String destFolderPath = getCopyToFolder(session, postversioned, configdata, customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--dest folder: "+destFolderPath,null,null);

            // version and label the child doc and link it to the system subfolder
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving object",null,null);
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

    public List getAttrsToCopy(Map configdata)
    {
        /*-CFG-*/String m="getAttrsToCopy-";
        ArrayList list = new ArrayList();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"determining configured attrs-to-copy list",null,null);
        int i=1;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up CopyAttr"+i+" in plugin config",null,null);
        while (configdata.containsKey("CopyAttr-"+i))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"CopyAttr-"+i+" is ["+configdata.get("CopyAttr"+i)+"]",null,null);
            list.add(configdata.get("CopyAttr-"+i));
            i++;
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning list of attrs to copy from original to copy",null,null);
        return list;
    }

    public void copyAttributes(IDfSession session, IDfDocument parentDoc, IDfDocument childDoc, List attrlist) throws DfException
    {
        // copy MRCS attrs if possible
        /*-CFG-*/String m="copyAttributes-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking for mrcs_application attr",null,null);
        if (parentDoc.hasAttr("mrcs_application"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copying mrcs_application attr",null,null);
            childDoc.setString("mrcs_application",parentDoc.getString("mrcs_application"));
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking for mrcs_config attr",null,null);
        if (parentDoc.hasAttr("mrcs_config"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copying mrcs_config attr",null,null);
            childDoc.setString("mrcs_config",parentDoc.getString("mrcs_config"));
        }
        // process custom attr list
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing custom attr copy list",null,null);
        for (int i=0; i < attrlist.size(); i++)
        {
            String attrname = (String)attrlist.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attribute to copy: "+attrname,null,null);
            boolean isRepeating = parentDoc.isAttrRepeating(attrname);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--repeating? "+isRepeating,null,null);
            if (isRepeating)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterate through repeating values and copy each",null,null);
                for (int r=0; r < parentDoc.getValueCount(attrname); r++)
                {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--looking up value #"+r,null,null);
                    IDfValue v = parentDoc.getRepeatingValue(attrname,r);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--setting value ["+v.asString()+"]",null,null);
                    childDoc.setRepeatingValue(attrname,r,v);
                }
            } else {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"performing single-valued copy",null,null);
                IDfValue attrvalue = parentDoc.getValue(attrname);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"--setting value ["+attrvalue.asString()+"]",null,null);
                childDoc.setValue(attrname,attrvalue);
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attr copy complete",null,null);
    }

    public IDfACL getCopyDocACL(IDfSession session, Map configdata) throws Exception
    {
        /*-CFG-*/String m="getCopyDocACL-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting ACL ",null,null);
        String systemdomain = session.getServerConfig().getString("operator_name");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"operator name: "+systemdomain,null,null);
        String copyDocACLName = (String)configdata.get("CopyDocACL");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"copydoc ACL object name: "+copyDocACLName,null,null);
        IDfACL copyDocACL = session.getACL(systemdomain, copyDocACLName);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"ACL found? "+(copyDocACL != null),null,null);
        return copyDocACL;
    }

    public String getCopyToFolder(IDfSession session, IDfDocument doc, Map configdata, Map customdata) throws Exception
    {
        /*-CFG-*/String m="getCopyToFolder-";
        // look up relative directory to copy to
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top",null,null);
        String location = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"check config",null,null);
        if (configdata.containsKey("CopyToLocation"))
        {
            // a constant/hardcoded path
            location = (String)configdata.get("CopyToLocation");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"CopyToLocation: "+location,null,null);
        }
        else if (configdata.containsKey("CopyToProperty"))
        {
            // path is stored in an attribute of the document
            String attr = (String)configdata.get("CopyToProperty");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"CopyToProperty: "+attr,null,null);
            location = doc.getString(attr);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"CopyToProperty's location: "+location,null,null);
        }
        else
        {
            /*-ERROR-*/DfLogger.getRootLogger().debug("MRCS:CopyToSystemFolder.getCopyToProperty - improper configuration of plugin!");
            throw new Exception(m+"plugin not properly configured, can't determine where to copy to");
        }

        IDfFolder basefolder = getGroupingFolderBase(session,doc);

        // navigate to the system subfolder
        String targetpath = basefolder.getFolderPath(0)+'/'+location;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"target path for copyto: "+targetpath,null,null);
        return targetpath;
    }

    public IDfFolder getGroupingFolderBase(IDfSession session, IDfDocument doc) throws Exception
    {
        /*-CFG-*/String m="getGroupingFolderBase-";
        // look up the grouping folder base
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get folder that owns this file (hope its the only folder)",null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"number of linked folders (should be one only): "+doc.getFolderIdCount(),null,null);
        IDfId parentfolderid = doc.getFolderId(0);
        IDfId basefolderid = null;
        IDfSysObject parentfolder = (IDfSysObject)session.getObject(parentfolderid);
        // is this a mrcs subfolder?
        IDfFolder gfroot = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"is this a mrcs subfolder, or the gf root?",null,null);
        if (parentfolder.hasAttr("mrcs_grouping_folder_root"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"this is a subfolder, so return the value of the gf root id attr: "+parentfolder.getString("mrcs_grouping_folder_root"),null,null);
            basefolderid = new DfId(parentfolder.getString("mrcs_grouping_folder_root"));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"gf root attr value: "+basefolderid,null,null);
        }
        // is this the root of the current grouping folder?
        else if (parentfolder.hasAttr("mrcs_config"))
        {
            // this is the root (since it isn't a subfolder, but is an MRCS folder)
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"this is the gf root already: "+parentfolder.getObjectName(),null,null);
            basefolderid = parentfolderid;
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"base folder id (the gf root): "+basefolderid,null,null);
        }
        else
        {
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"neither mrcs base grouping folder or mrcs subfolder detected - this is an error!");
            throw new Exception(m+"folder is not an MRCS folder");
        }

        IDfFolder basefolder = (IDfFolder)session.getObject(basefolderid);
        return basefolder;
    }



    // looks to see if main document has a relationship with a child document already, and then gets the child doc id if it does.
    // may want to further refine this with relationshiptype...
    public IDfId getChildId(IDfSession session, String docId, String relationname) throws Exception
    {
        /*-CFG-*/String m="getChildId-";
        IDfId childId = null;
        IDfCollection myObj1 = null;
        try{
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"composing qualification",null,null);
            //String qualification = "select r_object_id from dm_sysobject where r_object_id in " +
            //        "(select child_id from dm_relation where relation_name = '"+relationname+"' and parent_id in " +
            //        "(select r_object_id from dm_sysobject (ALL) where i_chronicle_id in " +
            //        "(select i_chronicle_id from dm_sysobject where r_object_id = '" +docId+"')) )";
            String qualification =
                    "select child_id from dm_relation where relation_name = '"+relationname+"' and parent_id in " +
                    "(select r_object_id from dm_sysobject (ALL) where i_chronicle_id in " +
                    "(select i_chronicle_id from dm_sysobject where r_object_id = '" +docId+"')) ";
            IDfQuery qry = new DfQuery();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"qualification : "+qualification,null,null);
            qry.setDQL(qualification);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"prequery: "+(new Date()).getTime() ,null,null);
            myObj1 = (IDfCollection)qry.execute(session,IDfQuery.DF_READ_QUERY);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"postquery: "+(new Date()).getTime() ,null,null);
            // look up the object we just located (should only be one or none?)
            while(myObj1.next())
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"next retrieved object, scanning attributes",null,null);
                // look up the attribute we just located (should only be one?)
                for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"examining attr #"+i,null,null);
                    IDfAttr attr = myObj1.getAttr(i);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current attribute: "+attr.getName(),null,null);
                    if (attr.getDataType() == IDfAttr.DM_STRING) {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attr is a string",null,null);
                        String id = myObj1.getString(attr.getName());
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"child id : --->>" + id + "<<",null,null);
                        childId = new DfId(id);
                    }
                }
            }
            myObj1.close();

        }catch(Exception e){
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"Error while looking up child id",e);
            throw e;
        } finally {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"closing collection",null,null);
            if (myObj1 != null){ myObj1.close(); }
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning childid: " + childId ,null,null);
        return childId;
    }

    public void setRelation(IDfSession session, String relation_name, IDfDocument parentDoc, IDfId childId, String description)
        throws DfException
    {
        // get relation type, create if necessary (this may need recoding...)
        /*-CFG-*/String m="setRelation-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top, looking up relationtype named "+relation_name,null,null);
        IDfRelationType relationType = session.getRelationType(relation_name);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"-- found? "+(relationType != null ? relationType.getRelationName() : null),null,null);

        IDfRelation docRelation = null;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding relation to parent for childid: "+childId,null,null);
        docRelation = parentDoc.addChildRelative(relationType.getRelationName(), childId, null, false,description);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving parent-child relation",null,null);
        docRelation.save();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"created relation: " + docRelation.getId("r_object_id").getId(),null,null);
    }

    // this links, versions, and labels the child document to the destination subfolder, but doesn't set up the relationship
    public IDfId processChildDoc(IDfSession session, IDfDocument parentDoc, IDfDocument childDoc, boolean isNew, String destinationFolderPath, String copyDocLabel) throws Exception
    {
        // Specify the folder in which to create the document
        /*-CFG-*/String m="processChildDoc-";
        IDfFolder destFolder = null;
        IDfId childId = null;;

        destFolder = session.getFolderByPath(destinationFolderPath);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"dest folder opened? "+(destFolder != null),null,null);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting vesion policy",null,null);
        IDfVersionPolicy verPolicy = parentDoc.getVersionPolicy();
        String label = verPolicy.getSameLabel();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"version policy label: "+label,null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"verPolicy Summary  " + verPolicy.getVersionSummary(">"),null,null);

        // Save the document in the Docbase
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving object",null,null);
        if (isNew) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"new object..link",null,null);
            childDoc.link(destFolder.getObjectId().toString());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"label with policy label",null,null);
            childDoc.mark(label);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"label with custom label "+copyDocLabel,null,null);
            childDoc.mark(copyDocLabel);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"label with 'CURRENT'",null,null);
            childDoc.mark("CURRENT");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving...",null,null);
            childDoc.save();
            childId = childDoc.getObjectId();
        } else {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"label with custom label "+copyDocLabel,null,null);
            childDoc.mark(copyDocLabel);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"label with 'CURRENT'",null,null);
            childDoc.mark("CURRENT");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"not a SAME, so checkin...",null,null);
            childId = childDoc.checkin(false, label);
        }

        return childId;

    }

/*
    IDfSessionManager useSystemAccount(String mrcsapp, IDfSessionManager curMgr, Map configdata) throws Exception
    {
        if (Boolean.valueOf((String)configdata.get("UseSystemAccount")).booleanValue())
        {
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();

            //create a Session Manager object
            IDfSessionManager sMgr = client.newSessionManager();

            MrcsFolderConfigFactory config = MrcsFolderConfigFactory.getFolderConfig();

            //create an IDfLoginInfo object named loginInfoObj
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser(config.getSystemUsername(mrcsapp));
            loginInfoObj.setPassword(config.getSystemPassword(mrcsapp));
            loginInfoObj.setDomain(null);
            sMgr.setIdentity(config.getApplicationDocbase(mrcsapp), loginInfoObj);

            return sMgr;
        }
        return curMgr;

    }
*/

// code GRAVEYARD.... R.I.P.

// relation types should not be created on the fly...
//  if (relationType == null)
//  {
//      /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relation type not found, creating",null,null);
//      relationType = (IDfRelationType) session.newObject("dm_relation_type");
//      /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting relation type name: "+relation_name,null,null);
//      relationType.setRelationName(relation_name);
//      /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting relation type security to \"PARENT\"",null,null);
//      relationType.setSecurityType("PARENT");
//      /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting child and parent doctypes for relation type",null,null);
//      relationType.setChildType(parentDoc.getTypeName());
//      relationType.setParentType(childDoc.getTypeName());
//      try {
//          /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving relation",null,null);
//          relationType.save();
//      } catch (DfException excep) {
//          /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"is this a real error? error code = "+excep.getErrorCode(),null,null);
//          if (excep.getErrorCode() == 100)
//          {
//              /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"relation already exists",null,null);
//              relationType.revert();
//          }
//          else
//          {
//              /*-ERROR-*/DfLogger.getRootLogger().error(m+"ERROR in relation type creation",excep);
//              throw (excep);
//          }
//      }
//  }


}
