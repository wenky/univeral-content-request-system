package com.medtronic.documentum.pulse;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.formext.action.*;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.webcomponent.library.renditions.Renditions;
import com.documentum.webcomponent.navigation.vdm.VDMList;
import com.documentum.webtop.app.AppSessionContext;

import java.io.ByteArrayOutputStream;
import java.text.Format;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

public class SwordViewAction implements IActionExecution, IActionPrecondition, IActionCompleteListener {
	
    private Context m_actionContext;
	private ArgumentList m_actionArgs;    	 
	private IDfSession m_session;

    public SwordViewAction()
    {
    }

    public String[] getRequiredParams()
    {
        return (new String[] {
            "objectId"
        });
    }

    public boolean queryExecute(String strAction, IConfigElement config, ArgumentList args, Context context, Component component)
    {
        boolean bExecute = false;
        String strObjectId = args.get("objectId");
        if(FolderUtil.isFolderType(strObjectId))
        {
            bExecute = true;
        } else
        if(!(component instanceof VDMList) && !(component instanceof Renditions) && isVDMAaction(args, context, component))
        {
            String strView = AppSessionContext.getView();
            if(strView != null && strView.equals("classic"))
            {
                bExecute = ActionService.queryExecute("vdmlist", args, context, component);
            } else
            {
                bExecute = ActionService.queryExecute("vdmliststreamline", args, context, component);
            }
        } else
        {
            Context webcomponentContext = new Context(context);
            webcomponentContext.set("application", "webcomponent");
            com.documentum.web.formext.action.ActionService.ActionDef actionDef = ActionService.getActionDef("view", args, webcomponentContext);
            if(actionDef != null)
            {
                bExecute = ActionService.queryExecute(actionDef, args, context, component);
            }
        }
        return bExecute;
    }

    public boolean execute(String strAction, IConfigElement config, ArgumentList args, Context context, Component component, Map completionArgs)
    {
        String strDQL = "";
        String strReportObjectId = "";
        boolean bExecute = false;
        com.documentum.web.formext.action.ActionService.ActionDef actionDef = null;
        Context webcomponentContext = null;
        int intWorkflowCount = 0;
        
        String strObjectId = args.get("objectId");        
        m_session = component.getDfSession();
        
        // Execute a query to find all workflow's that are related to the selected
        // document.  Generate a eSignature report for each workflow to display the 
        // Users signatures.  If there is no workflow associated dispaly a blank report.
        try {
    		strDQL = "SELECT DISTINCT workflowid from dm_dbo.mdt_workflow_audittrail"
    			+ " WHERE documentid = '" + strObjectId + "'";
    		IDfCollection objCol = executeQuery(strDQL, component.getDfSession());
    		while (objCol.next()){
    			String strWorkflowId = objCol.getTypedObject().getString("workflowid");
    			
    			// Generate a report object based on the workflow and selected object.
    	        strReportObjectId = createEsigReport(strObjectId, strWorkflowId, component.getDfSession());

    	        // Setup the arguments to be passed to the view Action Service.
    	        String strFormat = "crtext";
    	        m_actionArgs = new ArgumentList();
    	        m_actionArgs.add("objectId", strReportObjectId);
    	        m_actionArgs.add("contentType", strFormat);

    	        // Setup the context to be passed to the view Action Service.
    	        m_actionContext = new Context();
    			m_actionContext.set("objectId", strReportObjectId);

    	        webcomponentContext = new Context(m_actionContext);
                webcomponentContext.set("application", "webcomponent");
                actionDef = ActionService.getActionDef("view", m_actionArgs, webcomponentContext);
                if(actionDef != null)
                {
                	bExecute = ActionService.execute(actionDef, m_actionArgs, m_actionContext, component, this);
                }
                intWorkflowCount++;
    		}
    		objCol.close();
    		objCol = null;

    		// Check to see if there was atleast one workflow associated to the selected document
    		// If there are no workflows associated create a blank report.
    		if (intWorkflowCount == 0){
    			strReportObjectId = createBlankEsigReport(strObjectId, component.getDfSession());
    	        String strFormat = "crtext";
    	        m_actionArgs = new ArgumentList();
    	        m_actionArgs.add("objectId", strReportObjectId);
    	        m_actionArgs.add("contentType", strFormat);
    	        
    	        m_actionContext = new Context();
    			m_actionContext.set("objectId", strReportObjectId);

    	        webcomponentContext = new Context(m_actionContext);
                webcomponentContext.set("application", "webcomponent");
                actionDef = ActionService.getActionDef("view", m_actionArgs, webcomponentContext);

                if(actionDef != null)
                {
                	bExecute = ActionService.execute(actionDef, m_actionArgs, m_actionContext, component, this);
                }
    		}
    		
          } catch (DfException e) {
        	  throw new WrapperRuntimeException(e);
          }

        return bExecute;
    }

    private boolean isVDMAaction(ArgumentList arg, Context context, Component component)
    {
        String strLinkCount = arg.get("linkCount");
        double dLinkCount = 0.0D;
        boolean bIsVirtual = false;
        if(strLinkCount != null)
        {
            try
            {
                dLinkCount = Double.parseDouble(strLinkCount);
                if(dLinkCount > 0.59999999999999998D)
                {
                    bIsVirtual = true;
                }
            }
            catch(Exception e) { }
        }
        String strIsVirtual = arg.get("isVirtualDoc");
        if(!bIsVirtual && strIsVirtual != null)
        {
            if(strIsVirtual.equals("true"))
            {
                bIsVirtual = true;
            } else
            {
                try
                {
                    if(Double.parseDouble(strIsVirtual) > 0.59999999999999998D)
                    {
                        bIsVirtual = true;
                    }
                }
                catch(Exception e) { }
            }
        }
        if(!bIsVirtual && (strLinkCount == null || strIsVirtual == null))
        {
            String strObjectId = arg.get("objectId");
            try
            {
                IDfSession dfSession = component.getDfSession();
                com.documentum.fc.client.IDfPersistentObject iobj = dfSession.getObject(new DfId(strObjectId));
                if((iobj instanceof IDfSysObject) && ((IDfSysObject)iobj).isVirtualDocument())
                {
                    bIsVirtual = true;
                }
            }
            catch(Exception e)
            {
                throw new WrapperRuntimeException(e);
            }
        }
        return bIsVirtual;
    }

	/**
	 * Method to create the report object in the DOcbase based on teh 
	 * selected document.
	 * 
	 * @param String
	 *            SelectedObjectId
	 * @param String
	 *            WorfklowId
	 * @param IDfSession
	 * 			  session
	 * return
	 * 			  New Object ID
	 */ 
	private String createEsigReport(String SelectedObjectId, String WorkflowId, IDfSession session) {
		
		String strNewObjectId = "";
		
		try {
	
			Date date = new Date();
			String reportName = "eSignature-Report-" + date.getTime() + ".txt";
			StringBuffer buffer = generateEsigReport(session, SelectedObjectId, WorkflowId, date);				
			String resultsLine = buffer.toString();
			
			ByteArrayOutputStream newContent = new ByteArrayOutputStream();
			newContent = new ByteArrayOutputStream();
			newContent.write(resultsLine.getBytes());
			
			IDfSysObject myObj = (IDfSysObject)session.newObject("dm_document");
			myObj.setObjectName(reportName);
			myObj.setContentType("crtext");
			myObj.setContent(newContent);
			myObj.link("/ESigReports/2003");
			myObj.save();
			
			strNewObjectId = myObj.getObjectId().toString();
				
		} catch (Exception e) {
			throw new WrapperRuntimeException(e);
		}
		return strNewObjectId;
	}

	
	/**
	 * The following Method is for generating the output for the eSignature Report
	 * 
	 * @param IDfSession session
	 * @param String ObjectID
	 * @param String WorkflowId
	 * @param Date date
	 * @return StringBuffer
	 */
	private StringBuffer generateEsigReport(IDfSession session, String ObjectID, String WorkflowId, Date date){
		
		StringBuffer buffer = new StringBuffer();
		Format formatter;
		String strLineSeparator = "";
		String strDQL = "";
		String strTaskName = "";
		String strApproverName = "";
		String strApproverId = "";
		String strApprovalDate = "";
		String strProcessId = "";
		String strProcessName = "";
		
		int intCount = 0;
		IDfCollection objCol = null;
		
		try {
			IDfSysObject objSys = (IDfSysObject)session.getObject(
					new DfId(ObjectID));
			    
			//strLineSeparator = System.getProperty("line.separator");
			strLineSeparator = "\r\n";
			
		    // Examples with date and time; see also
		    // e316 Formatting the Time Using a Custom Format
		    formatter = new SimpleDateFormat("MM-dd-yyyy HH.mm.ss");
		    String strDate = formatter.format(date);
			buffer.append("E Signature Information for " + objSys.getObjectName() + " generated at " + strDate);
			buffer.append(strLineSeparator).append("=======================================================================");
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Attributes:");

			/*
			 * Document File Name
			 */
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Document File Name                      : ").append(objSys.getObjectName());
			buffer.append(strLineSeparator).append("(object_name)");

			/*
			 * Document Title
			 */
			buffer.append(strLineSeparator);
			if (objSys.getTitle().trim().equals("")){
				buffer.append(strLineSeparator).append("Document Title                          : ").append("(no title)");
			} else {
				buffer.append(strLineSeparator).append("Document Title                          : ").append(objSys.getTitle());	
			}			
			buffer.append(strLineSeparator).append("(title)");
	
			/*
			 * Document Version
			 */
			buffer.append(strLineSeparator);
			for(int i = 0; i < objSys.getVersionLabelCount(); i++)
			{
				if (i == 0){
					buffer.append(strLineSeparator).append("Document Version		     [" + i + "]:");
					buffer.append(" ").append(objSys.getVersionLabel(i));
					buffer.append(strLineSeparator);
				}else{
					buffer.append("(r_version_label)		     [" + i + "]:");
					buffer.append(" ").append(objSys.getVersionLabel(i));
					buffer.append(strLineSeparator);
				}
			}

			/*
			 * Workflow or Router Activity Name
			 */
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Workflow or Router Activity Name	   ");
			intCount = 0;
			strDQL = "";
			strDQL = "SELECT DISTINCT taskname, approvaldateandtime"
				+ " FROM dm_dbo.mdt_workflow_audittrail"
				+ " WHERE workflowid = '" + WorkflowId
				+ "' AND documentid = '" + ObjectID + "' ORDER BY approvaldateandtime";
			objCol = executeQuery(strDQL, session);
			while (objCol.next()){
				strTaskName = objCol.getTypedObject().getString("taskname");
				if (intCount == 0){
					buffer.append(strLineSeparator).append("(r_act_name or task_name             [" + intCount + "]:");
					buffer.append(" ").append(strTaskName);
					buffer.append(strLineSeparator);
				}else{
					buffer.append("                                      [" + intCount + "]:");
					buffer.append(" ").append(strTaskName);
					buffer.append(strLineSeparator);
				}
				intCount++;
			}
			objCol.close();
			objCol = null;	   

			/*
			 * Workflow or Router Approver's Name
			 */
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Workflow or Router Approver's Name 	   ");
			intCount = 0;
			strDQL = "";
			strDQL = "SELECT DISTINCT approvername, approvaldateandtime"
				+ " FROM dm_dbo.mdt_workflow_audittrail"
				+ " WHERE workflowid = '" + WorkflowId
				+ "' AND documentid = '" + ObjectID + "' ORDER BY approvaldateandtime";
			objCol = executeQuery(strDQL, session);
			while (objCol.next()){
				strApproverName = objCol.getTypedObject().getString("approvername");
				if (intCount == 0){
					buffer.append(strLineSeparator).append("(sign_off_user or r_sign_off_user)   [" + intCount + "]:");
					buffer.append(" ").append(strApproverName);
					buffer.append(strLineSeparator);
				}else{
					buffer.append("                                      [" + intCount + "]:");
					buffer.append(" ").append(strApproverName);
					buffer.append(strLineSeparator);
				}
				intCount++;
			}
			objCol.close();
			objCol = null;	   
	

			/*
			 * Approver's Unique ID
			 */
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Approver's Unique ID               	");	
			intCount = 0;
			strDQL = "";
			strDQL = "SELECT DISTINCT approverid, approvaldateandtime"
				+ " FROM dm_dbo.mdt_workflow_audittrail"
				+ " WHERE workflowid = '" + WorkflowId
				+ "' AND documentid = '" + ObjectID + "' ORDER BY approvaldateandtime";
			objCol = executeQuery(strDQL, session);
			while (objCol.next()){
				strApproverId = objCol.getTypedObject().getString("approverid");
				if (intCount == 0){
					buffer.append(strLineSeparator).append("(user_os_name)                       [" + intCount + "]:");
					buffer.append(" ").append(strApproverId);
					buffer.append(strLineSeparator);
				}else{
					buffer.append("                                      [" + intCount + "]:");
					buffer.append(" ").append(strApproverId);
					buffer.append(strLineSeparator);
				}
				intCount++;
			}
			objCol.close();
			objCol = null;	   
			
			/*
			 * Workflow or Router Sign off Date
			 */
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Workflow or Router Sign off Date	");
			
			intCount = 0;
			strDQL = "";
			strDQL = "SELECT DISTINCT approvaldateandtime"
				+ " FROM dm_dbo.mdt_workflow_audittrail"
				+ " WHERE workflowid = '" + WorkflowId
				+ "' AND documentid = '" + ObjectID + "' ORDER BY approvaldateandtime";
			objCol = executeQuery(strDQL, session);
			while (objCol.next()){
				strApprovalDate = objCol.getTypedObject().getString("approvaldateandtime");
				if (intCount == 0){
					buffer.append(strLineSeparator).append("(sign_off_date or r_sign_off_date)   [" + intCount + "]:");
					buffer.append(" ").append(strApprovalDate);
					buffer.append(strLineSeparator);
				}else{
					buffer.append("                                      [" + intCount + "]:");
					buffer.append(" ").append(strApprovalDate);
					buffer.append(strLineSeparator);
				}
				intCount++;
			}
			objCol.close();
			objCol = null;	   
			
			/*
			 * Document File ID Number
			 */
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Document File ID Number	                : ").append(objSys.getObjectId().getId());
			buffer.append(strLineSeparator).append("(r_object_id)");
	
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Workflow or Router ID Number            : ").append(WorkflowId);
			buffer.append(strLineSeparator).append("(r_object_id)");
			
			/*
			 * Workflow or Router Template Name
			 */
			strDQL = "";
			strDQL = "SELECT DISTINCT process_id, approvaldateandtime"
				+ " FROM dm_dbo.mdt_workflow_audittrail"
				+ " WHERE workflowid = '" + WorkflowId
				+ "' AND documentid = '" + ObjectID + "' ORDER BY approvaldateandtime";
			objCol = executeQuery(strDQL, session);
			while (objCol.next()){
				strProcessId = objCol.getTypedObject().getString("process_id");
				if (strProcessId.trim().length() > 0) {
					IDfProcess objProcess = (IDfProcess)session.getObject(new DfId(strProcessId));
					strProcessName = objProcess.getObjectName();					
				} else {
					strProcessName = "(no process_id)";
				}
			}
			objCol.close();
			objCol = null;	   
			buffer.append(strLineSeparator);
			buffer.append(strLineSeparator).append("Workflow or Router Template Name        : ").append(strProcessName);
			buffer.append(strLineSeparator);
	
		} catch (DfException e) {
			throw new WrapperRuntimeException(e);
		}
		return buffer;
	}

	/**
	   * executeQuery is the method that execute a string query
	   * and return the result as an IDfCollection
	   *
	   * @param strQuery String
	   * @param session IDfSession
	   * @return IDfCollection
	   * @throws DfException
	   */
	  public static IDfCollection executeQuery(String strQuery, IDfSession session) throws DfException {
	    IDfCollection col = null; //Collection for the result
	    IDfClientX clientx = new DfClientX();
	    IDfQuery q = clientx.getQuery(); //Create query object
	    q.setDQL(strQuery); //Give it the query
	    col = q.execute(session, IDfQuery.DF_EXECREAD_QUERY);
	    return col;
	  }

	/* (non-Javadoc)
	 * @see com.documentum.web.formext.action.IActionCompleteListener#onComplete(java.lang.String, boolean, java.util.Map)
	 */
	public void onComplete(String arg0, boolean arg1, Map arg2) {
//		System.out.println("onComplete");
//		if (m_reportCount == 1) {
//			for (Iterator iter = m_objectArray.iterator(); iter.hasNext();)
//			{			
//				String strKey = (String)iter.next();
//				try {
//					IDfSysObject objSys = (IDfSysObject)m_session.getObject(new DfId(strKey));
//					if (!(objSys == null)) {
////						objSys.destroy();
//						System.out.println("Delete Object: " + strKey);
//					}					
//				} catch (DfException e) {
//					
//				}
//			}			
//		}
	}

	/** The method will generate a blank standard eSignature report 
	 * 
	 * @param SelectedObjectId
	 * @param date
	 * @return
	 */
	private StringBuffer generateBlankEsigReport(String SelectedObjectId, Date date){
		
		StringBuffer buffer = new StringBuffer();
		Format formatter;
		String strLineSeparator = "\r\n";
		
	    formatter = new SimpleDateFormat("MM-dd-yyyy HH.mm.ss");
	    String strDate = formatter.format(date);
	    buffer.append(strLineSeparator).append("=======================================================================");
		buffer.append(strLineSeparator).append("E Signature Information for [NO REPORT AVAILABLE] generated at " + strDate);
		buffer.append(strLineSeparator).append("=======================================================================");
		buffer.append(strLineSeparator);
				
		return buffer;
	}

	/** The method will generate a blank standard eSignature report
	 * 
	 * @param SelectedObjectId
	 * @param session
	 * @return String
	 */
	private String createBlankEsigReport(String SelectedObjectId, IDfSession session) {
		
		String strNewObjectId = "";
		
		try {
	
			Date date = new Date();
			String reportName = "eSignature-Report-" + date.getTime() + ".txt";
			StringBuffer buffer = generateBlankEsigReport(SelectedObjectId, date);				
			String resultsLine = buffer.toString();
			
			ByteArrayOutputStream newContent = new ByteArrayOutputStream();
			newContent = new ByteArrayOutputStream();
			newContent.write(resultsLine.getBytes());
	
			IDfSysObject myObj = (IDfSysObject)session.newObject("dm_document");
			myObj.setObjectName(reportName);
			myObj.setContentType("crtext");
			myObj.setContent(newContent);
			myObj.link("/ESigReports/2003");
			myObj.save();
			
			strNewObjectId = myObj.getObjectId().toString();
				
		} catch (Exception e) {
			throw new WrapperRuntimeException(e);
		}
		return strNewObjectId;
	}	
}
