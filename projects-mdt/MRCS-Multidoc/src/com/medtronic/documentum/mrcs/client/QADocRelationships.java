/**
 ******************************************************************************
 *
 * Confidential Property of Documentum, Inc.
 * (c) Copyright Documentum, Inc. 2001.
 * All Rights reserved.
 * May not be used without prior written agreement
 * signed by a Documentum corporate officer.
 *
 ******************************************************************************
 *
 * Project        Component Library
 * File           History.java
 * Description    Generic History Component Implementation
 * Created on     23 August 2001
 * Tab width      3
 *
 ******************************************************************************
 *
 * VCS Maintained Data
 *
 * Revision       $Revision: 11$
 * Modified on    $Date: 5/9/2005 12:54:43 PM$
 *
 ******************************************************************************
 */
package com.medtronic.documentum.mrcs.client;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfRelation;
import com.documentum.fc.client.IDfRelationType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.util.StringUtil;
import com.documentum.webcomponent.library.locator.LocatorItemResultSet;

/**
 * Generic History Component Implementation. This component ren ders the
 * audit trail for a given sysobject id. It also provides a combo box
 * for viewing the audit for other versions associated with this object.
 * Required argument parameter is <b>objectId</b>.
 */
public class QADocRelationships /*extends ObjectGrid*/ extends Component
{
   /**
    * Component Initialisation
    * @param         args    Component initialisation arguments.
    */
	String mrcsapp = null;
	String mrcsdoctype = null;
	String mrcsgftype = null;
	String relateddocid = null;
	
   public void onInit(ArgumentList args)
   {
      //first set the source object id for this component
      m_strObjectId = args.get("objectId");
      if(m_strObjectId == null)
      {
         throw new IllegalArgumentException(getString("MSG_MISSING_MANDATORY_PARAMS"));
      }
      
      // CEM: determine MRCS configuration-specific info on relationship types that can be created...
      //      - we have scoped this component to m_mrcs_document, so we should be okay assuming the passed objectID is a m_mrcs_document
      try { 
	      IDfDocument mrcsdoc = (IDfDocument)getDfSession().getObject(new DfId(m_strObjectId));
	      mrcsapp = mrcsdoc.getString("mrcs_application");
	      mrcsdoctype = mrcsdoc.getString("mrcs_config");
	      mrcsgftype = mrcsdoc.getString("mrcs_folder_config");
      } catch (Exception e) {
    	  throw new RuntimeException("Relationships can't get mrcs document config properties",e);
      }
      
      // CEM: TODO - pare down relationship type name based on MRCS config values...
      //           - based on selected document type, restrict types of documents between which relationships can be established...

     //get the query for selecting the version labels
     //every label from 1.0 --> CURRENT
     String relationTypesQry = getRelationTypesComboQuery(m_strObjectId);
     System.out.println("ComboQuery::" + relationTypesQry);
     //get at versions combo
     DataDropDownList versionsCombo = (DataDropDownList)getControl(STR_VERSIONS_FILTER, DataDropDownList.class);
     versionsCombo.getDataProvider().setQuery(relationTypesQry);
     versionsCombo.getDataProvider().setDfSession(getDfSession());
     //set the selected option to the source object id
     //versionsCombo.setValue(m_strObjectId);

      // get the customisable query condition
      String strQueryConditionFormat =  this.lookupString("queryconditionstring");
      if (strQueryConditionFormat != null && strQueryConditionFormat.trim().length() > 0)
      {
         m_strQueryConditionFormat = strQueryConditionFormat;
      }
      
      // get relations datagrid
      Datagrid relationdisplaygrid = (Datagrid)getControl(GRID_NAME,Datagrid.class);      
      relationdisplaygrid.getDataProvider().setQuery("select relation_name, 'PARENT' as reltype, child_id as rellink, dm_document.object_name as docname from dm_relation, dm_document where dm_document.r_object_id = dm_relation.child_id and parent_id = '"+m_strObjectId+"' "+
    		                                         "UNION select relation_name, 'CHILD' as reltype, parent_id as rellink, dm_document.object_name as docname from dm_relation, dm_document where dm_document.r_object_id = dm_relation.parent_id and child_id = '"+m_strObjectId+"'");
      relationdisplaygrid.getDataProvider().setDfSession(getDfSession());
      
      //now call the super init
      super.onInit(args);

   }


   /**
    * Event handler for selecting a option to show the audit for a selected version
    * or all versions of a sysobject.
    * @param         control        dropdown list where the action was originated from
    * @param         args           argument list
    */
   public void onSelectVersionFilter(DataDropDownList control, ArgumentList args)
   {
      //get the selected option from the dropdown list
      //i.e., object id for the selected version
      m_strSelectedVersionObjectId = control.getValue();
   }
   
   public void onCompleteAddDocument(Button control, ArgumentList args)
   {
	   // create relation (no validations - assume correctness, otherwise don't add
	   try {
	       // get relation type, create if necessary (this may need recoding...)
		   DataDropDownList versionsCombo = (DataDropDownList)getControl(STR_VERSIONS_FILTER, DataDropDownList.class);
		   String relation_typeid = versionsCombo.getValue();
		   IDfRelationType relationType = (IDfRelationType)getDfSession().getObject(new DfId(relation_typeid));
		   IDfRelation docRelation = null;
		   IDfDocument parentDoc = (IDfDocument)getDfSession().getObject(new DfId(m_strObjectId));
		   IDfDocument childDoc = (IDfDocument)getDfSession().getObject(new DfId(relateddocid));
		   docRelation = parentDoc.addChildRelative(relationType.getRelationName(), new DfId(relateddocid), null, false,"");
		   docRelation.save();
		   Datagrid relationdisplaygrid = (Datagrid)getControl(GRID_NAME,Datagrid.class);
		   relationdisplaygrid.getDataProvider().refresh();
		   
	   } catch (Exception e) {
		   // do nothing... TODO: fix post-prototype
	   }
	   
   }
   
   public void onComplete(String strAction, boolean bSuccess, Map completionArgs)
   {
       if("addwfattachment".equals(strAction) && bSuccess)
       {
           if(completionArgs == null)
               return;
           LocatorItemResultSet setLocatorSelections = (LocatorItemResultSet)completionArgs.get("_locator_sel");
           if(setLocatorSelections != null && setLocatorSelections.first())
           {
               String strObjId = setLocatorSelections.getObject("r_object_id").toString();
               relateddocid = strObjId;
               String objType = setLocatorSelections.getObject("r_object_type").toString();
               IDfId objId = new DfId(strObjId);
               String objName = null;
               try { 
	               IDfDocument doc = (IDfDocument)getDfSession().getObject(objId);
	               objName = doc.getObjectName();
               } catch (DfException dfe) {
            	   throw new RuntimeException(dfe);
               }
               
               Label filename = (Label)this.getControl("selectedDocName",Label.class);
               filename.setLabel(objName);
               
               Button addbutton = (Button)this.getControl("AddRelation", Button.class);
           }
       }
       
   }

   /**
    * Supplies the query for the History component.
    * @param      strVisibleAttrs      visible attributes list
    * @param      args                 Argument list
    * @return     String               the dql statement for History component.
    */
   protected String getQuery(String strVisibleAttrs, ArgumentList args)
   {
      if(m_strSelectedVersionObjectId == null)
      {
         m_strSelectedVersionObjectId = m_strObjectId;
      }

      // get the where clause
      String strWhere = m_strQueryConditionFormat;
      strWhere = StringUtil.replace(strWhere, "{r_object_id}",  "'" + m_strSelectedVersionObjectId + "'");

      StringBuffer buf = new StringBuffer(128);
      buf.append(STR_SELECT) ;
      buf.append(strVisibleAttrs);
      buf.append(STR_DUMMY); // dummy is redundant here
      buf.append(STR_FROM_CLAUSE);
      buf.append(strWhere);
      buf.append(STR_ORDER_BY);

      return buf.toString();
   }   

   /**
    * Gets the query string for populating the versions combo box.
    * @param      strObjectId       source object id
    * @return     String            query for versions tree
    */
   private String getRelationTypesComboQuery(String strObjectId)
   {
       String qry = "SELECT * FROM dm_relation_type WHERE relation_name LIKE 'QADoc%' ORDER BY relation_name ASC";
       return qry;
   }

   /** List to hold the visible and internal attributes from the config */
   private HashSet m_visibleAttrsList = new HashSet(13, 1.0f);

   public static String GRID_NAME = "ourdatagridblahblahbl";
   /** Name of the versions filter */
   public static final String STR_VERSIONS_FILTER = "version_filter";
   /** Name of the versions filter panel */
   public static final String STR_VERSIONS_PANEL = "versions_panel";
   /** member variable for storing the source object id */
   private String m_strObjectId = null;
   /** object id for the selected version */
   private String m_strSelectedVersionObjectId = null;
   /** select string */
   private static final String STR_SELECT = "SELECT ";
   /** from clause */
   private static final String STR_FROM_CLAUSE = " FROM dm_audittrail WHERE ";
   /** order by clause */
   private static final String STR_ORDER_BY = " ORDER BY time_stamp DESC";
   /** dummy attribute: getVisibleAttributes() returns a string that ends with a comma */
   private static final String STR_DUMMY = "'1' as dummy";

   /** the where clasuse as a text message format **/
   private String m_strQueryConditionFormat = "audited_obj_id = {r_object_id}";
} // end class History

