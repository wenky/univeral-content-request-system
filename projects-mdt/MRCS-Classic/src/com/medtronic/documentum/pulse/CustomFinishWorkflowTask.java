package com.medtronic.documentum.pulse;


import com.documentum.fc.client.IDfUser;
import com.documentum.fc.common.DfException;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.Text;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.workflow.taskmanager.WorkflowTaskComponent;

//Referenced classes of package com.documentum.webcomponent.library.workflow.taskmanager:
//         WorkflowTaskComponent

public class CustomFinishWorkflowTask extends WorkflowTaskComponent
{

    public void onInit(ArgumentList arg)
    {
        super.onInit(arg);
	    try
	    {
	    	if(!getWorkflowTask().isPerformerAssignmentRequired())
	    		setComponentPage("finish");
	    	else
	    		setComponentPage("assignperformers");
	    	updateControls();
	     }
	     catch(DfException e)
	     {
	         throw new WrapperRuntimeException(e);
	     }
    }

    // CEM: mimick the Forward task customization in Finish task...
    public boolean onCommitChanges()
    {
    	IWorkflowTask task = null;
    	boolean retValue;
    	try { 
    		if (checkLoginName())
    		{
    			if((retValue = super.onCommitChanges()))
    			{
    				task = getWorkflowTask();
    				if(task.isSignOffRequired())
    					task.setSignoffPassword(readSignOffPasswordValue());
    				task.completeTask();
    				setMessage("MSG_FINISH_SUCCESS", new Object[] {super.getString("MSG_OBJECT")});
    			}
    		} else {
    			setErrorMessage("MSG_INCORRECT_USER_OS_NAME");
    			return false;
    		}
	        
    		return retValue;
    	} catch (DfException e) {
    		try {
            	if(task.getCompleteErrStatus() != 4) {
		            setErrorMessage("MSG_INCORRECT_PASSWORD");
		            return false;
	            }
    		} catch (DfException e2) {
    			setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] {e2.getMessage()}, e2);
    			throw new WrapperRuntimeException(e2);
    		}
    	}
    	return true;
    }
    
    public boolean checkLoginName()
    {
        boolean boolLoginName = false;
        /**
         * custom code WF
         */
        try
        {
            String strUserName = getDfSession().getLoginUserName();
            System.out.println("User Name:" + strUserName);
            
            IDfUser idfuser = getDfSession().getUser(strUserName);
            String strUserOSName = idfuser.getUserOSName();
            System.out.println("User OS name:" + strUserOSName);
                        
            Text txtUserId = (Text) getControl("__USERID_CONTROL_NAME",com.documentum.web.form.control.Text.class);
            String strUserEnteredId = txtUserId.getValue();            
            System.out.println("User Entered Login Name:" + strUserEnteredId);
            
            if (strUserEnteredId != null)
            {
                if (strUserEnteredId.equals(strUserOSName))
                {
                    System.out.println("Login Name is TRUE");
                    boolLoginName = true;
                }
            }
        }
        catch (DfException exp)
        {
            throw new WrapperRuntimeException(exp);
        }

        /**
         * end of custom code
         */
        return boolLoginName;
    }


    public boolean hasNextPage()
    {
        String page = getComponentPage();
        boolean retValue;
        if(page.equals("assignperformers"))
            retValue = true;
        else
            retValue = false;
        return retValue;
    }

    public boolean hasPrevPage()
    {
        boolean retValue;
        try { 
   	        String page = getComponentPage();
   	        if(page.equals("assignperformers"))
   	            retValue = false;
   	        else
   	            retValue = getWorkflowTask().isPerformerAssignmentRequired();
   	        return retValue;
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    public boolean onNextPage()
    {
        String page = getComponentPage();
        boolean retValue;
        if(page.equals("assignperformers"))
        {
            setComponentPage("finish");
            retValue = true;
        } else
        {
            retValue = false;
        }
        if(retValue)
            updateControls();
        return retValue;
    }

    public boolean onPrevPage()
    {
        boolean retValue;
        try { 
   	        String page = getComponentPage();
   	        if(page.equals("assignperformers"))
   	            retValue = false;
   	        else
   	        if((retValue = getWorkflowTask().isPerformerAssignmentRequired()))
   	            setComponentPage("assignperformers");
   	        if(retValue)
   	            updateControls();
   	        return retValue;
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    public void onRefreshData()
    {
        super.onRefreshData();
        updateControls();
    }

    protected void updateControls()
    {
        String page = getComponentPage();
        if(page.equals("assignperformers"))
            updateDynamicPerformersGrid();
        else
            updateSignoffPanel();
    }

    private static final String PAGE_FINISH = "finish";
    private static final String PAGE_ASSIGN_PERFORMERS = "assignperformers";
    
    
    void setErrorMessage(String id)
    {
        setReturnError(id, null, null);
        WebComponentErrorService.getService().setNonFatalError(this, id, null);
    }

    void setErrorMessage(String id, Object params[])
    {
        setReturnError(id, params, null);
        WebComponentErrorService.getService().setNonFatalError(this, id, params, null);
    }

    void setErrorMessage(String id, Exception e)
    {
        setReturnError(id, null, e);
        WebComponentErrorService.getService().setNonFatalError(this, id, e);
    }

    void setErrorMessage(String id, Object params[], Exception e)
    {
        setReturnError(id, params, e);
        WebComponentErrorService.getService().setNonFatalError(this, id, params, e);
    }

    void setMessage(String id)
    {
        MessageService.addMessage(this, id, null);
    }

    void setMessage(String id, Object params[])
    {
        MessageService.addMessage(this, id, params);
    }

}





/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/workflow/taskmanager/FinishWorkflowTask.class


	TOTAL TIME: 297 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't fully decompile method onCommitChanges
Couldn't resolve all exception handlers in method onCommitChanges
Couldn't fully decompile method hasPrevPage
Couldn't resolve all exception handlers in method hasPrevPage
Couldn't fully decompile method onPrevPage
Couldn't resolve all exception handlers in method onPrevPage

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

********************************/