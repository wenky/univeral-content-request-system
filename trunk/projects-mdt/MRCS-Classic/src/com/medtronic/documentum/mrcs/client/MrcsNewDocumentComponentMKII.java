/*
 * Created on Mar 18, 2005
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

 Filename       $RCSfile: MrcsNewDocumentComponentMKII.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.4 $
 Modified on    $Date: 2006/08/21 04:57:04 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.services.subscriptions.ISubscriptions;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.IReturnListener;
import com.documentum.web.form.control.BooleanInputControl;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Link;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.webcomponent.library.subscription.SubscriptionsHttpBinding;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsCustomPageProcessorPlugin;

/**
* @author muellc4
*
* TODO To change the template for this generated type comment go to
* Window - Preferences - Java - Code Style - Code Templates
*/
public class MrcsNewDocumentComponentMKII 
      extends Component 
      implements IReturnListener /* for the post-attribute checkout action*/
{
	
	
	
	
   String m_mrcsapp;
   String m_parentfolderid;
   String m_gftype;
   MrcsDocumentConfigFactory m_docconfig;
   DropDownList m_doctypescontrol;
   DropDownList m_formatscontrol;
   DataDropDownList m_templatescontrol;
   Checkbox m_subscribecontrol;
   boolean m_bShowOptions;
   
   String m_newdocid;
   String m_newdoctype;

   
   public void onInit(ArgumentList argumentlist)
   {

	   // get document config service
       // get the ConfigBroker service
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - getting document config service",null,null);
       try { m_docconfig = MrcsDocumentConfigFactory.getDocumentConfig();        
       } catch (Exception e) {
           setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
           ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
           setComponentReturn();
           return; // I think we escape out...don't we?            
       }
       
       // get the mrcs app and gf type from the current folder...
       // current folder objectid should be in arglist...
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - looking up parent folder",null,null);
       IDfFolder parentfolder = null;
       String mrcsapplication = null;
       String objectid = null;
       String gftype = null;
       boolean isMrcsFolder = false;
       try { 
           objectid = argumentlist.get("objectId");
           m_parentfolderid = objectid;
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - objectid: "+objectid,null,null);
           IDfSession session = getDfSession();
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - composing idfid",null,null);
           IDfId newid = new DfId(objectid);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - looking up folder object",null,null);
           parentfolder = (IDfFolder)session.getObject(newid);
           
           // check permission
           int permission = parentfolder.getACL().getPermit(session.getLoginUserName());
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - user's permission level: "+permission,null,null);
           if (IDfACL.DF_PERMIT_WRITE > permission)
           {
               /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewDocumentContainer.onInit - Insufficient permit level to create MRCS documents in this folder");
               Exception e = new Exception("Insufficient permit level to create MRCS docs in this folder");
               setReturnError("MSG_MRCS_DOC_CREATE_PERMIT_ERROR", null, e);
               ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_PERMIT_ERROR", e);
               setComponentReturn();
               return;               
           }
           
           if (parentfolder.hasAttr("mrcs_application"))
           {
               isMrcsFolder = true;
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - current folder is a mrcs folder!",null,null);
               mrcsapplication = parentfolder.getString("mrcs_application");
               m_mrcsapp = mrcsapplication;
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - mrcsapp: "+mrcsapplication,null,null);
               gftype = parentfolder.getString("mrcs_config");
               m_gftype = gftype;
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - gftype: "+gftype,null,null);
           }
       } catch (Exception dfe) {
           setReturnError("MSG_MRCS_DOC_CREATE_FOLDER_ATTR_ERROR", null, dfe);
           ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_FOLDER_ATTR_ERROR", dfe);
           setComponentReturn();
           return; // I think we escape out...don't we?
       }
       
       // determine if a UI screen needs to be displayed, or if we can proceed with document creation w/o input
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - getting allowable doctypes",null,null);
       List doctypes = m_docconfig.getAllowableDocumentTypes(mrcsapplication,gftype);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.OnInit - number of types: "+doctypes.size(),null,null);
       
       // this shouldn't happen, but it needs to be accounted for...make sure we are in an Mrcs Folder
       if (!isMrcsFolder)
       {
           Exception e = new Exception("Mrcs document creation attempted in a non-MRCS folder, please check the scoping of the MRCS new document component");
           /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewDocumentContainer.OnInit - Mrcs document creation attempted in a non-MRCS folder, please check the scoping of MrcsNewDocumentContainer",e);
           setReturnError("MSG_MRCS_DOC_CREATE_NOT_IN_MRCS_FOLDER_ERROR", null, e);
           ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_NOT_IN_MRCS_FOLDER_ERROR", e);
           setComponentReturn();
           return; 
       }
       
       // initialize doctype component
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onInit - getting refs to the three dropdown controls",null,null);
       m_doctypescontrol  = (DropDownList)getControl("doctypes",DropDownList.class);
       m_formatscontrol   = (DropDownList)getControl("formats",DropDownList.class);
       m_templatescontrol = (DataDropDownList)getControl("templateList",DataDropDownList.class);
       m_subscribecontrol = (Checkbox)getControl("subscribe",Checkbox.class);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onInit - configuring doctype dropdown",null,null);
       for (int i=0; i < doctypes.size(); i++)
       {
           Option newopt = new Option();
           newopt.setLabel((String)doctypes.get(i));  // should I do a decode to a description attribute from config?
           newopt.setValue((String)doctypes.get(i));
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onInit - adding option "+newopt.getLabel(),null,null);
           m_doctypescontrol.addOption(newopt);
       }
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onInit - populating formats",null,null);        
       populateFormats(m_doctypescontrol.getValue());
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onInit - populate templates",null,null);        
       setupTemplateList(m_docconfig.getDocumentSystemType(m_mrcsapp,m_gftype,m_doctypescontrol.getValue()), m_formatscontrol.getValue());
       //setupTemplateList("dm_document", m_formatscontrol.getValue());
       // hide the subscribe panel to start...
       Panel subscribepanel = (Panel)this.getControl("optionspanel",Panel.class);
       subscribepanel.setVisible(false);
  
   }
   
   public void onSelectType(DropDownList dropdownlist, ArgumentList argumentlist)
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectType - doctype selected, need to configure format and template dropdowns...",null,null);
       String doctype = m_doctypescontrol.getValue();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectType - selected doctype: "+doctype,null,null);
       populateFormats(doctype);
       // clear the template widget
       m_templatescontrol.setValue(null);
   }
   
   public void populateFormats(String doctype)
   {
       // get the format list!
       List formats = m_docconfig.getDocumentFormats(m_mrcsapp,m_gftype,doctype);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - format clause for DQL format decode statement",null,null);
       String formatclause = "";
       for (int i=0; i < formats.size(); i++)
       {
           if (i > 0) formatclause += ",";
           formatclause += "'"+(String)formats.get(i) +"'";
       }
       // populate the list
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - clearing format options",null,null);
       m_formatscontrol.setMutable(true);
       m_formatscontrol.clearOptions();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - formatclause: "+formatclause,null,null);
       String dqlquery = "SELECT name,description,dos_extension FROM dm_format "+
                         "WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') AND "+
                         "name IN ("+formatclause+") ORDER BY description";
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - dql query: "+dqlquery,null,null);    
       DfQuery dfquery = new DfQuery();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - setting dql query",null,null);    
       dfquery.setDQL(dqlquery);

       IDfCollection idfcollection = null;
       try {
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - executing dql query",null,null);
           idfcollection = dfquery.execute(getDfSession(), 0); 
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - iterating through result set",null,null);
           while (idfcollection.next()) 
           {
               String formatname = idfcollection.getString("name");
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - format name: "+formatname,null,null);
               String formatdesc = idfcollection.getString("description");
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - format desc: "+formatdesc,null,null);
               String formatextension = idfcollection.getString("dos_extension");
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - format ext: "+formatextension,null,null);
               
               // add list option!
               Option newopt = new Option();
               newopt.setLabel(formatdesc);
               newopt.setValue(formatname);
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - adding option " + newopt.getLabel(),null,null);
               m_formatscontrol.addOption(newopt);
           }

       } catch (DfException dfexception) {
           /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewDocumentContainer.populateFormats - exception thrown during DQL execution",dfexception);
           setReturnError("MSG_MRCS_DOC_CREATE_FORMAT_LOOKUP_ERROR", null, dfexception);
           ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_FORMAT_LOOKUP_ERROR", dfexception);
           throw new WrapperRuntimeException("Unable to query format types from docbase!", dfexception);
       } finally {
           try {
               if (idfcollection != null)
                   idfcollection.close();
           } catch (DfException dfexception1) {
           }
       }
//       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - populating format options",null,null);
//       for (int i=0; i < formats.size(); i++)
//       {
//           Option newopt = new Option();
//           newopt.setLabel((String)formats.get(i));
//           newopt.setValue((String)formats.get(i));
//           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.populateFormats - adding option "+newopt.getLabel(),null,null);
//           m_formatscontrol.addOption(newopt);            
//       }        
   }
   
   public void onSelectFormat(DropDownList dropdownlist, ArgumentList argumentlist)
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectFormat - top of method",null,null);
       String doctype = m_doctypescontrol.getValue();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectFormat - selected doctype: "+doctype,null,null);
       String docsystemtype = m_docconfig.getDocumentSystemType(m_mrcsapp,m_gftype,doctype);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectFormat - docsystemtype: "+docsystemtype,null,null);
       String format = m_formatscontrol.getValue();        
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectFormat - selected format: "+format,null,null);
       
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectFormat - about to call setupTemplateList",null,null);
       setupTemplateList(docsystemtype, format);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onSelectFormat - setupTemplateList completed",null,null);
   }

   public void onGotoNextComponent(Control button, ArgumentList argumentlist)
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - submit button clicked",null,null);
       // validate fields?
       
       // determine if optional component specified, if so, jump (?nest?) to that component
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - determining if custom component specified",null,null);
       String component = m_docconfig.getDocumentComponent(m_mrcsapp,m_gftype,m_doctypescontrol.getValue());
       if (component == null)
       {
           // create doc
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - no component specified, proceeding with doc create",null,null);
           try {
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - instantiating document SBO service",null,null);
//               final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
//               IMrcsDocumentSBO docsbo = (IMrcsDocumentSBO)DfClient.getLocalClient().new-Service(IMrcsDocumentSBO.class.getName(), manager);
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - invoking doc creation service",null,null);
               IDfSession session = getDfSession();
               String docbase = SessionManagerHttpBinding.getCurrentDocbase();
               MrcsDocumentSBO docsbo = MrcsDocumentSBO.getService();
               m_newdocid = docsbo.createDocument(session,
                                                  m_parentfolderid, m_mrcsapp, m_gftype, 
                                                  m_doctypescontrol.getValue(), m_formatscontrol.getValue(),m_templatescontrol.getValue(), null);
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - new doc created successfully: "+m_newdocid,null,null);
           } catch (Exception e) {
               setReturnError("MSG_MRCS_DOC_CREATE_ERROR", null, e);
               ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_ERROR", e);
               setComponentReturn();
               return;
           }
                      
           // jump to attributes control
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - jumping to attibutes component",null,null);
           ArgumentList args = new ArgumentList();
           args.add("objectId", m_newdocid);
           setComponentNested("mrcsdocattributes",args,getContext(),this);            
       }
       else // go to component
       {
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onGotoNextComponent - jumping to custom component",null,null);
           setComponentPage(component);            
       }
   }
   
   // ripped from com.documentum.webcomponent.library.create.NewDocument (suppose I COULD subclass...)
   private void setupTemplateList(String docsystemtype, String format)
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.setupTemplateList - setup templates invoked",null,null);
       
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.setupTemplateList - getting MRCS doc config",null,null);
       MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.setupTemplateList - getting configured allowable templates",null,null);
       List allowabletemplates = docconfig.getDocumentFormatTemplates(m_mrcsapp, m_gftype, m_doctypescontrol.getValue(),format);
              
       m_templatescontrol.setMutable(true);
       m_templatescontrol.setValue(null);
       boolean flag = false;
       boolean formatcheck = (format != null && !format.equals(""));
       if(docsystemtype != null && docsystemtype.length() > 0 && formatcheck)
       {
           String dql = "SELECT r_object_id, object_name FROM dm_sysobject "+
                        "WHERE FOLDER('/Templates',DESCEND) "+
                        "AND r_object_type='"+docsystemtype+"'"+" AND a_content_type='"+format+"'"; 
           IDfCollection idfcollection = null;
           DfQuery dfquery = new DfQuery();
           dfquery.setDQL(dql);
           TableResultSet tableresultset = new TableResultSet(new String[] {"r_object_id", "object_name"});
           try
           {
               idfcollection = dfquery.execute(getDfSession(), 0);
               boolean firsttemplate = true;
               for(int i = 0; idfcollection.next(); i++)
               {
                   String templateid = idfcollection.getString("r_object_id");
                   String templatename = idfcollection.getString("object_name");
                   // see if templatename is in allowable template list
                   boolean templatefound = false;
                   // if allowabletemplates == null, then that implies that we should use all available templates located via DQL
                   if (allowabletemplates == null) templatefound = true;
                   else {
                       for (int j=0; j < allowabletemplates.size(); j++)
                       {
                           if (allowabletemplates.get(j).equals(templatename))
                           {
                               templatefound = true;
                               break;
                           }
                       }
                   }
                   if (templatefound)
                   {                      
                       tableresultset.add(new String[] {templateid, templatename});
                       flag = true;
                       if(firsttemplate) {
                           firsttemplate = false;
                           m_templatescontrol.setValue(templateid);
                       }
                   }
               }

           }
           catch(DfException dfexception)
           {
               setReturnError("MSG_MRCS_DOC_CREATE_TEMPLATE_LOOKUP_ERROR", null, dfexception);
               ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_TEMPLATE_LOOKUP_ERROR", dfexception);
               throw new WrapperRuntimeException(getString("MSG_TEMPLATE_QUERY_FAILED"), dfexception);
           }
           finally
           {
               try
               {
                   if(idfcollection != null)
                       idfcollection.close();
               }
               catch(DfException dfexception1) { }
           }
           m_templatescontrol.getDataProvider().setScrollableResultSet(tableresultset);
       }
       Panel panel = (Panel)getControl("template_panel", com.documentum.web.form.control.Panel.class);
       panel.setVisible(flag);
       // enable/disable next/ok button if templates are/are not found
       Button nextbutton = (Button)getControl("Submit", com.documentum.web.form.control.Button.class);
       nextbutton.setEnabled(flag);
       Label notemplates = (Label)getControl("template_not_found", com.documentum.web.form.control.Label.class);
       notemplates.setVisible(!flag);
       
   }

   public void onClickShowHideOptions(Link link, ArgumentList argumentlist)
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentContainer.onClickShowHideOptions - subscribed toggle clicked",null,null);
       m_bShowOptions = !m_bShowOptions;
       Panel panel = (Panel)getControl("optionspanel", com.documentum.web.form.control.Panel.class);
       if(m_bShowOptions)
       {
           panel.setVisible(true);
           link.setLabel(getString("MSG_HIDE_OPTIONS"));
       } else
       {
           panel.setVisible(false);
           link.setLabel(getString("MSG_SHOW_OPTIONS"));
       }
   }

   public Map getCustomData()
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - scraping customdata",null,null);
       HashMap customdata = new HashMap();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - getting component iterator",null,null);
       Iterator formcontrols = getContainedControls();
       while (formcontrols.hasNext())
       {
           Control currentcontrol = (Control)formcontrols.next();
           // what is the name?
           String controlname = currentcontrol.getName();
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - current control name: "+controlname,null,null);
           // see if the control name matches the custom control naming convention
           String namingconventionprefix = "MrcsCustom";
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - checking if name matches prefix MrcsCustom",null,null);
           if (controlname.length() > namingconventionprefix.length() && controlname.substring(0, namingconventionprefix.length()).compareTo(namingconventionprefix) == 0)
           {
               // get the key
               String customkey = controlname.substring(namingconventionprefix.length());
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - prefix matched, key: "+customkey,null,null);
               // get the value
               // -- is it a text?
               if (currentcontrol instanceof StringInputControl )
               {
                   String customvalue = ((StringInputControl)currentcontrol).getValue();
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - string input control, value: "+customvalue,null,null);
                   customdata.put(customkey,customvalue);
               }
               else if (currentcontrol instanceof BooleanInputControl )
               {
                   Boolean custombool = new Boolean(((BooleanInputControl)currentcontrol).getValue());
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - boolean input control, value: "+custombool,null,null);
                   customdata.put(customkey,custombool);
               }
               else
               {
                   // log something about the unknown control type - for now, we assume that all custom fields should be StringInputControls
                   /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.getCustomData - unknown control type: "+currentcontrol.getClass(),null,null);                    
               }
           }            
       }
       return customdata;
   }
   
   public void cancelNewDocument(Control button, ArgumentList argumentlist)
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.cancelNewDocument - custom component data submit invoked",null,null);
       setComponentReturn();
   }
   
   public void submitCustomData(Control button, ArgumentList argumentlist)
   {
       // scrape the custom data 
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.submitCustomData - custom component data submit invoked",null,null);                    
       Map customdata;
       MrcsPlugin pageprocessor = m_docconfig.getDocumentComponentProcessor(m_mrcsapp, m_gftype, m_doctypescontrol.getValue());
       if (pageprocessor == null) {
           // use the default handler
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - no page processor defined, performing default processing",null,null);
           customdata = this.getCustomData();
       } else {
           try {
               // instantiate and execute custom handler - do this in an SBO? no...
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - custom processor defined, instantiating "+pageprocessor.PluginClassName,null,null);
               MrcsCustomPageProcessorPlugin customprocessor = (MrcsCustomPageProcessorPlugin)Class.forName(pageprocessor.PluginClassName).newInstance();
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - executing processor",null,null);
               customdata = customprocessor.process(this,pageprocessor.PluginConfiguration);
           } catch (Exception e) {
               setReturnError("MSG_MRCS_DOC_CREATE_CUSTOM_DATA_PROCESSING_ERROR", null, e);
               ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_CUSTOM_DATA_PROCESSING_ERROR", e);
               /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.processCustomPage - exception encountered during custom page processing");
               throw new WrapperRuntimeException("Exception thrown while executing Mrcs Custom Page Processor "+pageprocessor.PluginClassName,e);
           }
       }
       // create the document
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.submitCustomData - getting document SBO service",null,null);                    
       try {
//           final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
//           IMrcsDocumentSBO docsbo = (IMrcsDocumentSBO)DfClient.getLocalClient().new-Service(IMrcsDocumentSBO.class.getName(), manager);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.submitCustomData - invoking document creation service",null,null);                    
           IDfSession session = getDfSession();
           String docbase  = SessionManagerHttpBinding.getCurrentDocbase();
           String doctype  = m_doctypescontrol.getValue();
           String format   = m_formatscontrol.getValue();
           String template = m_templatescontrol.getValue();
           MrcsDocumentSBO docsbo = MrcsDocumentSBO.getService();
           m_newdocid = docsbo.createDocument(session,m_parentfolderid, m_mrcsapp, m_gftype, doctype, format, template, customdata);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.submitCustomData - new doc created: "+m_newdocid,null,null);                    
       } catch (Exception e) {
           /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsNewDocumentCustomComponent.submitCustomData - exception occurred while attempting document creation",e);                    
           setReturnError("MSG_MRCS_DOC_CREATE_ERROR", null, e);
           ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_DOC_CREATE_ERROR", e);    
           return;
       }
       
       // handle subscription
       handleSubscription();       
       // jump to attributes control
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.submitCustomData - jumping to attributes component",null,null);                    
       ArgumentList args = new ArgumentList();
       args.add("objectId", m_newdocid);
       //setComponentJump("mrcsdocattributes",args,getContext());
       setComponentNested("mrcsdocattributes", args, getContext(), this);
   }
   
   // nested attributes component returns to this method (this is the listener)
   public void onReturn(Form form, Map map)
   {
       // until we resolve checkout problems...
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.onReturn - Nested call to attributes returning",null,null);                    
       Context context = getContext();
       ArgumentList argumentlist1 = new ArgumentList();
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.onReturn - doc id to checkout: "+m_newdocid,null,null);                    
       argumentlist1.add("objectId", m_newdocid);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.onReturn - setting doctype: "+m_doctypescontrol.getValue(),null,null);                    
       context.set("type", m_doctypescontrol.getValue());
       context.set("objectId", m_newdocid);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.onReturn - executing action",null,null);
       ActionService.execute("mrcseditafternew", argumentlist1, context, this, null);
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.onReturn - returned from action execute",null,null);                    
       //setComponentReturn();
   }

   
   private void handleSubscription()
   {
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.handleSubscription - top of call",null,null);                    
       Checkbox checkbox = (Checkbox)getControl("subscribe", com.documentum.web.form.control.Checkbox.class);
       boolean bSubscribe = checkbox.getValue();
       if(bSubscribe)
       {
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.handleSubscription - subscribe box checked",null,null);                    
           ISubscriptions isubscriptions = (new SubscriptionsHttpBinding()).getSubscriptionsService();
           String s = SessionManagerHttpBinding.getCurrentDocbase();
           if(isubscriptions.isInstalled(s))
           {
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.handleSubscription - subscribing...",null,null);                    
               isubscriptions.subscribe(s, m_newdocid);
           } else
           {
               /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.handleSubscription - subscriptions not installed",null,null);                    
               setReturnError("MSG_SUBSCRIPTIONS_NOT_INSTALLED_ERROR", null, null);
               ErrorMessageService.getService().setNonFatalError(this, "MSG_SUBSCRIPTIONS_NOT_INSTALLED_ERROR", null);
           }
       }
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsNewDocumentCustomComponent.handleSubscription - done",null,null);                    
   }
   

}   
