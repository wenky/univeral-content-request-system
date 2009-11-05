package com.medtronic.ecm.documentum.qad.reassigntask;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Tab;
import com.documentum.web.form.control.Tabbar;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.util.DfcUtils;
import com.documentum.webcomponent.library.propertysheetwizardcontainer.PropertySheetWizardContainer;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;

/**
 * This class is contained which holds all 4 components
 * Reassign From user,tasks,to user and Confirmation.
 * @author molakr1
 * 
 */
public class MdtReassignTaskContainer extends PropertySheetWizardContainer {
	   /**
	    * Component initialization
	    */
	   public void onInit(ArgumentList args)
	   {
	      super.onInit(args);

	      // number the tabs and set the first tab say 'create'
	      // have first tab say Create not component name
	      Tabbar tabs = (Tabbar)getControl(TABBAR_CONTROL_NAME);
	      Tab tab = tabs.getSelectedTab();
	      tab.setValue(getString("MSG_CREATE"));
	      numberTabs(tabs);
	      //Disable the tab selection
	      tabs.setEnabled(false);
	      
	      // set the title text
	      Component component = getContainedComponent();
	      m_strTitle = component.getString("MSG_TITLE");

	      Label labelTitle = (Label)getControl("title", Label.class);
	      labelTitle.setLabel(m_strTitle + ": " + getString("MSG_CREATE"));            
	   }

	   /**
	    * Handle the onRender event.
	    * @param
	    */
	   public void onRender()
	   {
	      super.onRender();

	      Label labelTitle = (Label)getControl("title", Label.class);
	      String strTabTitle = getContainedComponent().getString("MSG_TITLE");
	      if (strTabTitle.equals(m_strTitle) == true)
	      {
	         strTabTitle = getString("MSG_CREATE");
	      }
	      labelTitle.setLabel(m_strTitle + ": " + strTabTitle);
	   }
 
	   /**
	    * Called when the user pressed the 'next' button
	    * @param    Control
	    * @param 	ArgumentList 
	    * @return Whether the component was switched
	    */
	   public boolean onNextComponent(Control control, ArgumentList args)
	   {
	      boolean bSuccess = false;
	      Component component = getContainedComponent();
	      component.validate();
	      if (component.getIsValid())
	      {
	         if (onNewComponentTab())
	         {
		          // disable new component tab to prevent switching back
	        	 Tab tabNew = getNewTab();
	         }
	         bSuccess = super.onNextComponent(control, args);
	      }
	      //process data in Components when user clicks Next button.
	      processComponents(control, args);
	      return bSuccess;
	   }
	   
	 /**
	 * Called from onNextComponent()
	 * Get the list of all the Components in the Container 
	 * and get the info based the Component
     * @param    Control
     * @param 	ArgumentList 
	 */
	public void processComponents(Control control, ArgumentList args){
			//Get all the Components
			ArrayList components = getContainedComponents();
			for (int i = 0; i < components.size(); i++) {
				Component comp = (Component) components.get(i);
				// if the component is Task From
				if (comp.getComponentId().equals("mdtReassignTaskFrom")) {
					MdtReassignTaskFrom reassignTaskFrom = (MdtReassignTaskFrom) comp;
					//Re initialize
					reassignTaskFrom.onInit(args);
					//Get the Selected users
					lstSelectedUserFrom = reassignTaskFrom.getSelectedUserFromList();
				} 
				//if the component is Tasks list
				if (comp.getComponentId().equals("mdtTasksTobeReassigned")) {
					Component comp1 = (Component) components.get(0);
					MdtReassignTaskFrom reassignTaskFrom = (MdtReassignTaskFrom) comp1;
					lstSelectedUserFrom = reassignTaskFrom.getSelectedUserFromList();
					
					MdtTasksTobeReassigned reassignTaskobeReassigned = (MdtTasksTobeReassigned) comp;
					reassignTaskobeReassigned.onInit(args);
					lstSelectedTask = reassignTaskobeReassigned.getSelectedTaskList();
				} 
				//if the component is Tasks to user
				if (comp.getComponentId().equals("mdtReassignTaskTo")) {
					MdtReassignTaskTo reassignTaskToUser = (MdtReassignTaskTo) comp;
					reassignTaskToUser.onInit(args);
					lstSelectedToUser = reassignTaskToUser.getSelectedUserToList();
				} 
				//if the component is Confirm
				if (comp.getComponentId().equals("mdtReassignTaskConfirmation")) {
					MdtReassignTaskConfirmation reassignTaskConfirm = (MdtReassignTaskConfirmation) comp;
					reassignTaskConfirm.onInit(args);
				} 
			}
	   }
		/**
		 * @return selected from user
		 */
		public String getTaskFromUser() {
			return strSelectedFromUser;
		}
		/**
		 * Get task name
		 * @param
		 * @return selected task name
		 */
		public String getTaskName() {
			Iterator iterSelectedTask = lstSelectedTask.iterator();
			if(iterSelectedTask.hasNext()) {
			    Object[] strTaskName = (Object[])iterSelectedTask.next();
			    strSelectedTaskName = (String)strTaskName[0];
			}
			return strSelectedTaskName;
		}
		/**
		 * Get Task to User Name
		 * @param
		 * @return to user name
		 */
		public String getTasktoUserName() {
			Iterator iterSelectedToUser = lstSelectedToUser.iterator();
			if(iterSelectedToUser.hasNext()) {
			    Object[] strToUser = (Object[])iterSelectedToUser.next();
			    strSelectedToUser = (String)strToUser[0];
			}
			return strSelectedToUser;
		}
		
		/**
		 *  Get the Selected workitem ids and add to list
		 *  @param  
		 * @return list of workitem ids
		 */
		public List getWorkItemIdtobeDelegated() {
			List lstWorkItemIds = new ArrayList();

			//Iterate thru selected workitems and add to the list
			Iterator iterSelectedTask = lstSelectedTask.iterator();
			while(iterSelectedTask.hasNext()) {
			    Object[] strWorkItemId = (Object[])iterSelectedTask.next();
			    strSelectedWorkItemId = (String)strWorkItemId[0];
			    lstWorkItemIds.add(strSelectedWorkItemId);
			}
			return lstWorkItemIds;
		}
		/**
		 *  Method called from Tasks Component 
		 *  Get the list of tasks of related selected from user and populated in Task list box
		 *  @param 
		 * @return List of selected users tasks. 
		 */
		public List getUsersTaskList() {		
			// create a custom result set, so that we can figure out the format
			// to select and use it for templates query
			StringBuffer sbFormatQuery = new StringBuffer(280);
			//Get Component 	
			ArrayList components = getContainedComponents();
			Component comp = (Component) components.get(0);
			MdtReassignTaskFrom reassignTaskFrom = (MdtReassignTaskFrom) comp;
			lstSelectedUserFrom = reassignTaskFrom.getSelectedUserFromList();

			//Get the selected From User
			Iterator iterSelectedUser = lstSelectedUserFrom.iterator();
			if(iterSelectedUser.hasNext()) {
			    Object[] userdata = (Object[])iterSelectedUser.next();
			    strSelectedFromUser = (String)userdata[0];
			}
			
			//Run the Query to get task_name,r_object_id
			sbFormatQuery.append("select item_id,task_name,router_id from dmi_queue_item " );
			sbFormatQuery.append(" where event='dm_startedworkitem' and (task_state='dormant' or task_state='acquired')");
			sbFormatQuery.append(" and item_id like '4a%' and name='"+ strSelectedFromUser +"'");

			//Set String Query to query object
			IDfCollection dfCollection = null;
			IDfQuery query = DfcUtils.getClientX().getQuery();
			query.setDQL(sbFormatQuery.toString());
			
			// columns to add to the ArrayList
			String strTaskName 		= "";
			String strTaskObjectId 	= "";
			String strWorkFlowId 	= "";
			String strObjectName    = "";
			String strWorkflowName  = "";
			
			try {
				//Add Task Name to Array and then add to List.
				List m_TaskMap = new ArrayList();
				dfCollection = query.execute(getDfSession(), IDfQuery.READ_QUERY);
				while (dfCollection.next()) {
				    // validate workitem id
				    
					strTaskName 	= dfCollection.getString("task_name");
					strTaskObjectId = dfCollection.getString("item_id");
                    strWorkFlowId   = dfCollection.getString("router_id");

                    boolean goodwi = true;
                    try {
                        IDfWorkitem wi = (IDfWorkitem)getDfSession().getObject(new DfId(strTaskObjectId));
                        if (wi == null)
                            goodwi = false;
                        IDfWorkflow wf = (IDfWorkflow)getDfSession().getObject(new DfId(strWorkFlowId));
                        strWorkflowName = wf.getObjectName();
                    } catch (DfException e) {
                        goodwi = false;
                    }
					if (goodwi) {
    					strObjectName   = getObjectNamesInWorkflow(strTaskObjectId);
    					strObjectNameForConfirmation = strWorkflowName+" - "+strTaskName+" ("+strObjectName+")";
    					if(strTaskName != null) {
    						String arr[] = new String[2];
    						arr[0] = strTaskObjectId;
    						arr[1] = strObjectNameForConfirmation ;
    						m_TaskMap.add(arr);
    					}
					}
				}
				dfCollection.close();
				return m_TaskMap;
			}
			catch (DfException e) {
				throw new WrapperRuntimeException(getString("MSG_FORMAT_QUERY_FAILED"), e);
			}
			finally {
				try {if (dfCollection != null) dfCollection.close();}catch (DfException e){}
			}
		}
		/**
		 *  Called from Confirmation page
		 * @return
		 */
		public String getObjectNameForConfirmation(){
			return strObjectNameForConfirmation;
		}
		/**
		 * Get the Object Names as Comma Delimited in workflow process
		 * @param strWorkFlowId
		 * @return
		 */
		public String getObjectNamesInWorkflow(String strWorkItemId) throws DfException{
			String strObjectName = null;
			IDfWorkitem wi = (IDfWorkitem)getDfSession().getObject(new DfId(strWorkItemId));
			
	    	List lDocuments = getMdtAttachments(wi);
	    	/*-dbg-*/Lg.wrn("Loop through list of documents");
	    	for(int i=0; i < lDocuments.size(); i++ )    	{
	    		/*-dbg-*/Lg.wrn("Getting the document");
	    		IDfSysObject sysObj = (IDfSysObject) lDocuments.get(i);
				if(i == 0) { 
					strObjectName = sysObj.getObjectName();
				} else {
					strObjectName = strObjectName + "," + sysObj.getObjectName();;
				}
	    	}
	    	/*-dbg-*/Lg.dbg("Object Name  %s",strObjectName);
			return strObjectName;
		}
	    /**
	     * @param workitem
	     * @return
	     * @throws DfException
	     */
	    public List getMdtAttachments(IDfWorkitem workitem) throws DfException
	    {
	        IDfCollection packages = null;
	        List attachmentlist = new ArrayList();
	        try {
	            /*-dbg-*/Lg.wrn("get packages");
	            packages = workitem.getPackages(""); // returns visible and invisible. What's an invisible package?
	            /*-dbg-*/Lg.wrn("iterate pkgs");
	            while(packages.next())
	            {
	                /*-dbg-*/Lg.wrn("check if single or mult");
	                if (packages.isAttrRepeating("r_component_id"))
	                {
	                    /*-dbg-*/Lg.wrn("multiple r_component_ids, iterate");
	                    for (int i=0; i < packages.getValueCount("r_component_id"); i++)
	                    {
	                        /*-dbg-*/Lg.wrn("r_comp #%d",i);
	                        String compid = packages.getRepeatingString("r_component_id", i);
	                        /*-dbg-*/Lg.wrn("r_comp is %s",compid);
	                        IDfSysObject pkgdoc = null;
	                        try { 
	                            pkgdoc = (IDfSysObject)workitem.getSession().getObject(new DfId(compid));
	                        } catch (DfException dfe) {
	                            // log only
	                            /*-WARN-*/Lg.wrn("lookup of component %s failed",pkgdoc);
	                        }
	                        if (pkgdoc != null) {
    	                        /*-dbg-*/Lg.wrn("check if %s is a mdt_document",pkgdoc);
                                IDfSysObject recentdoc = (IDfSysObject)workitem.getSession().getObjectByQualification("dm_document where i_chronicle_id = '"+pkgdoc.getChronicleId().getId()+"'");
                                /*-dbg-*/Lg.wrn("--most recent retrieved: %s",recentdoc);
                                attachmentlist.add(recentdoc);
	                        }
	                    }
	                } else {
	                    /*-dbg-*/Lg.wrn("get single valued component id");
	                    String compid = packages.getString("r_component_id");
	                    /*-dbg-*/Lg.wrn("  SINGLE rcomp: %s",compid);
                        /*-dbg-*/Lg.wrn("r_comp is %s",compid);
                        IDfSysObject pkgdoc = null;
                        try { 
                            pkgdoc = (IDfSysObject)workitem.getSession().getObject(new DfId(compid));
                        } catch (DfException dfe) {
                            // log only
                            /*-WARN-*/Lg.wrn("lookup of component %s failed",pkgdoc);
                        }
                        if (pkgdoc != null) {
                            /*-dbg-*/Lg.wrn("check if %s is a mdt_document",pkgdoc);
                            IDfSysObject recentdoc = (IDfSysObject)workitem.getSession().getObjectByQualification("dm_document where i_chronicle_id = '"+pkgdoc.getChronicleId().getId()+"'");
                            /*-dbg-*/Lg.wrn("--most recent retrieved: %s",recentdoc);
                            attachmentlist.add(recentdoc);
                        }
	                }
	            }
	        } finally {
	            try { if (packages != null)packages.close(); } catch (Exception e) {}
	        }        
	        ///*-dbg-*/MdtLog.wrn("done");
	        return attachmentlist;
	    }
		
	   /**
	    * called when the user selects one of the tabs
	    *
	    * @param tabSelected The tab control
	    * @param args        The arguments
	    */
	   public void onTabSelected(Tab tabSelected, ArgumentList args)
	   {
	      Tabbar tabs = (Tabbar)getControl(TABBAR_CONTROL_NAME);
	      tabs.setAccessible(false);
	      
	      if (m_bNeedCreate == true)
	      {
	         ArrayList components = getContainedComponents();
	         Component componentCreate = (Component)components.get(0);
	         componentCreate.validate();

	         if (componentCreate.getIsValid())
	         {
	           super.onTabSelected(tabSelected, args);            
	         }
	         else
	         {
	            // don't switch page since info on create page no ok yet
	            // and reslect create tab
	            Tab tabCreate = getNewTab();
	            tabs.setValue(tabCreate.getName());
	         }
	      }
	      else
	      {
	         super.onTabSelected(tabSelected, args);
	      }
	   }


	   /**
	    * Called to determine whether there is a previous component
	    *
	    * @return Whether there is a previous component
	    */
	   public boolean hasPrevComponent()
	   {
	      Tabbar tabs = (Tabbar)getControl(TABBAR_CONTROL_NAME);
	      Tab tabSelected = tabs.getSelectedTab();
	      Iterator iter = tabs.getTabs();
	      int index = -1;
	      int indexSelected = -1;
	      while (iter.hasNext() == true)
	      {
	         Tab tab = (Tab)iter.next();
	         index += 1;
	         if (tab.equals(tabSelected))
	         {
	            indexSelected = index;
	            break;
	         }
	      }
	      // no previous if on first (index 0) or second (index 1) tab
	      if (indexSelected < 1)
	      {
	         return false;
	      }
	      else
	      {
	         return super.hasPrevComponent();
	      }
	   }
	   /**
	    * Get details message from the exception object
	    * @param   exception        The exception object.
	    * @return  error messages
	    */
	   protected String getDetailsMessage(Exception exception)
	   {
	      return DocbaseUtils.getValidationExceptionMsg(exception);
	   }

	   /**
	    * Get the tab for the new component (doc,folder,cabinet)
	    *
	    * @return Tab object for the new component tab
	    */
	   protected Tab getNewTab()
	   {
	      Tab tabNew = null;
	      Tabbar tabs = (Tabbar)getControl(TABBAR_CONTROL_NAME);
	      Iterator iter = tabs.getTabs();
	      while (iter.hasNext() == true)
	      {
	         Tab tab = (Tab)iter.next();
	         String strComponentId = tab.getName();
	         /*if (strComponentId.equals(getNewComponentName()))
	         {
	            tabNew = tab;
	            break;
	         }*/
	      }
	      return tabNew;
	   }

	   /**
	    * Determines if on the new doc/folder/cabinet tab
	    *
	    * @return true in on the new operation tab
	    */
	   protected boolean onNewComponentTab()
	   {
	      boolean bNewTab = false;

	      Tabbar tabs = (Tabbar)getControl(TABBAR_CONTROL_NAME);
	      if (tabs != null)
	      {
	         Tab tab = tabs.getSelectedTab();
	         if (tab != null)
	         {
	            String strComponentId = tab.getName();
	            /*if (strComponentId.equals(getNewComponentName()))
	            {
	               bNewTab = true;
	            }*/
	         }
	      }
	      return bNewTab;
	   }
	   	
    /**
    *  Attempt reassign.
    *  @param Control ,ArgumentList
    */
	public void onOk(Control button, ArgumentList args) 
	{
	    IDfSessionManager sysmgr = null;
	    String docbase = null;
	    try { 
    	    IDfSession mSession = getDfSession();
    	    docbase = mSession.getDocbaseName(); 
    	    MdtConfigService configsvc = MdtConfigService.getConfigService(mSession.getSessionManager(), docbase);    
            String user = mSession.getLoginUserName();
            /*-dbg-*/Lg.dbg("current loginuser %s",user);
            String  mdtapp = (String)configsvc.getMdtApplicationsFromGroupMembership(user).get(0);		   
            sysmgr = WorkflowUtils.getSystemSessionManager(getDfSession(),mdtapp);
	    } catch (DfException dfe) {
	        throw new WrapperRuntimeException("Unable to get MDT access for reassign",dfe);
	    }
	    IDfSession syssession = null;
	    String wiid = null;
	    String strToUserid = null;
        try {
            syssession = sysmgr.getSession(docbase);

            String strToUserName     = getTasktoUserName();
            String strFromUserName   = getTaskFromUser();
            strToUserid       = syssession.getUser(strToUserName).getUserLoginName();
            String strFromUserid     = syssession.getUser(strFromUserName).getUserLoginName();
            List lstWorkItemId       = getWorkItemIdtobeDelegated();

            for (int i=0; i < lstWorkItemId.size(); i++) {
                wiid = (String)lstWorkItemId.get(i);
                IDfWorkitem wi = (IDfWorkitem)syssession.getObject(new DfId(wiid));
                boolean performreassign = true;
                if (wi.getRuntimeState() == 0)
                {
                    // acquire the dormant task
                    wi.acquire();
                    wi.fetch(null);
                } else if(wi.getRuntimeState() != 1) {
                    // finished or paused, do not reassign
                    performreassign = false;
                }
                if (performreassign) {                    
                    wi.delegateTask(strToUserid); // do I need to save too?                    
                    //wi.complete();
                    syssession.getAuditTrailManager().createAudit(wi.getObjectId(), "mdt_reassign", new String[]{strFromUserName,strToUserName}, null);
                }
            }

            setComponentReturn();
        } catch(DfException e) {
            throw new WrapperRuntimeException("Error reassigning workitem "+wiid+" to user "+strToUserid,e);
        }
	}

	   //-------------------------------------------------------------------------
	   // Protected Data

	   protected boolean m_bNeedCreate = true;
	   protected static final String CONFIG_NEW_COMPONENT_NAME = "newcomponentname";

	   //--------------------------------------------------------------------------
	   // Private Data

	   /**
	    * Dialog title from the component
	    */
	   private String m_strTitle = "";

	   public List lstSelectedUserFrom 		= null;
	   public List lstSelectedToUser    	= null;
	   public List lstSelectedTask			= null;
	   public String strSelectedFromUser 	= null;
	   public String strSelectedToUser 		= null;
	   public String strSelectedTaskName	= null;
	   public String strSelectedWorkItemId  = null;
	   public String strObjectNameForConfirmation = null;
}
