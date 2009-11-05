/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   FinishWorkflowTask.java

package com.documentum.webcomponent.library.workflow.taskmanager;

import com.documentum.fc.common.DfException;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;

// Referenced classes of package com.documentum.webcomponent.library.workflow.taskmanager:
//            WorkflowTaskComponent

public class FinishWorkflowTask extends WorkflowTaskComponent
{

    public FinishWorkflowTask()
    {
    }

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

    public boolean onCommitChanges()
    {
        IWorkflowTask task = null;
        boolean retValue;
        try { 
	        if((retValue = super.onCommitChanges()))
	        {
	            task = getWorkflowTask();
	            if(task.isSignOffRequired())
	                task.setSignoffPassword(readSignOffPasswordValue());
	            task.completeTask();
	            setMessage("MSG_FINISH_SUCCESS", new Object[] {
	                super.getString("MSG_OBJECT")
	            });
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