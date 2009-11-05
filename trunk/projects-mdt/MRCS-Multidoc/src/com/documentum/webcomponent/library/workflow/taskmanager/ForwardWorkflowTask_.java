/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ForwardWorkflowTask.java

package com.documentum.webcomponent.library.workflow.taskmanager;

import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.*;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.webcomponent.library.workflow.ScrollableResultSetAdapter;
import java.text.MessageFormat;

// Referenced classes of package com.documentum.webcomponent.library.workflow.taskmanager:
//            WorkflowTaskComponent, NextTasksResultSet, TaskComponentContainer

public class ForwardWorkflowTask_ extends WorkflowTaskComponent
    implements TaskComponentContainer.ITerminateEventHandler
{

    public ForwardWorkflowTask_()
    {
        m_nextPaths = null;
        m_theOnlyNextPortNames = null;
        m_checkToSkipForwardPage = true;
    }

    public void onInit(ArgumentList arg)
    {
        super.onInit(arg);
        try
        {
            if(m_checkToSkipForwardPage && shouldSkipForwardPage())
            {
                skipForwardPage();
                return;
            }
	        if(getWorkflowTask().isPerformerAssignmentRequired())
	            setComponentPage("assignperformers");
	        else
	            setComponentPage("forward");
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
        IDfList nextPaths;
        if(!(retValue = super.onCommitChanges()))
        {
            //break MISSING_BLOCK_LABEL_223;
        	return retValue;
        }
        task = getWorkflowTask();
        try { 
	        if(getComponentPage().equals("forward"))
	            nextPaths = readPathsSelectedActivitiesByPortNames(task.getNextForwardActivities(), task.getNextForwardPortNames());
	        else
	            nextPaths = m_nextPaths;
	        if(nextPaths != null && nextPaths.getCount() != 0)
	        {
	            //break MISSING_BLOCK_LABEL_76;
		        setErrorMessage("MSG_FORWARD_PATH_MUST_BE_SELECTED");
		        return false;
	        }
	        String actId;
	        boolean isSequential;
	        actId = task.getActivityId().toString();
	        isSequential = task.isSequentialActivity();
	        if(!isSequential || nextPaths.findStringIndex(actId) < 0 || nextPaths.getCount() == 1)
	        {
	            //break MISSING_BLOCK_LABEL_129;
		        setErrorMessage("MSG_SEQUENTIAL_FORWARD_PATH_EXCLUSION");
		        return false;
	        }
	        if(!isSequential || nextPaths.findStringIndex(actId) < 0)
	            task.setNextActivitiesByPortNames(nextPaths);
	        if(task.isSignOffRequired())
	            task.setSignoffPassword(readSignOffPasswordValue());
	        if(!hasTransitionMaxOutputCnt() || getTotalSelectValue() <= getMaxSelectValue())
	        {
	            //break MISSING_BLOCK_LABEL_198;
		        setErrorMessage("MSG_MAX_SELECT_FORWARD_COUNT");
		        return false;
	        }
	        task.completeTask();
	        setMessage("MSG_FORWARD_SUCCESS", new Object[] {
	            super.getString("MSG_OBJECT")
	        });
	        return retValue;
        } catch (DfException e) {
        	try { 
	            if(task.getCompleteErrStatus() != 4)
	            {
	            	//break MISSING_BLOCK_LABEL_246;
		  	        setErrorMessage("MSG_INCORRECT_PASSWORD");
		  	        return false;
	            } else {
		            setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] {e.getMessage()});
		            return false;
	            }
        	} catch (DfException dfe) {
        		throw new WrapperRuntimeException(dfe);        		
        	}
        }
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
            setComponentPage("forward");
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
	        {
	            retValue = false;
	        } else
	        {
	            IWorkflowTask task = getWorkflowTask();
	            if((retValue = task.isPerformerAssignmentRequired()))
	            {
	                m_nextPaths = readPathsSelected(task.getNextForwardActivities());
	                setComponentPage("assignperformers");
	            }
	        }
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
        {
            super.updateDynamicPerformersGrid();
        } else
        {
            Datagrid gridCtrl = (Datagrid)getControl("__NEXT_TASKS_GRID_CONTROL_NAME", com.documentum.web.form.control.databound.Datagrid.class);
            if(gridCtrl != null)
                gridCtrl.getDataProvider().setScrollableResultSet(new ScrollableResultSetAdapter(new NextTasksResultSet(getWorkflowTask(), null, 0)));
            super.updateSignoffPanel();
            updateCurrentSettingControl();
        }
    }

    protected boolean isDynamicPerformersAssigned()
    {
    	try { 
	        IWorkflowTask task = getWorkflowTask();
	        // CEM: fix???	        
	        //return isDynamicPerformersAssignedHelper(task.getNextForwardPortNames());
	        return isDynamicPerformersAssignedHelper(task.getNextForwardActivities());
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    public void onRender()
    {
        super.onRender();
        if(getComponentPage().equals("forward"))
            updateNextPathCheckboxes(m_nextPaths, "__NEXT_TASKS_CHECKBOX_CONTROL_NAME");
    }

    public void fire(String eventName)
    {
        try
        {
            IWorkflowTask wfTask = getWorkflowTask();
            if(m_theOnlyNextPortNames != null)
                wfTask.setNextActivitiesByPortNames(m_theOnlyNextPortNames);
            wfTask.completeTask();
            setMessage("MSG_FORWARD_SUCCESS", new Object[] {
                super.getString("MSG_OBJECT")
            });
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void skipForwardPage()
    {
        ((TaskComponentContainer)getContainer()).fireTerminateEvent(this, "onForwardOnlyTask");
    }

    protected boolean shouldSkipForwardPage()
    {
    	try { 
	        IWorkflowTask wfTask = getWorkflowTask();
	        if(wfTask.isPerformerAssignmentRequired() || wfTask.isSignOffRequired())
	            return false;
	        IDfList nextTasks;
	        IDfList nextPorts;
	        nextTasks = wfTask.getNextForwardActivities();
	        nextPorts = wfTask.getNextForwardPortNames();
	        if(nextTasks.getCount() != 1)
	            return false;
	        IDfTypedObject act = (IDfTypedObject)nextTasks.get(0);
	        String actId = act.getString("r_object_id");
	        if(!wfTask.getActivityId().toString().equals(actId))
	        {
	            m_theOnlyNextPortNames = new DfList(2, 1);
	            m_theOnlyNextPortNames.appendString(nextPorts.get(0).toString());
	        }
	        return true;
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    protected void setCheckToSkipForwardPageFlag(boolean flag)
    {
        m_checkToSkipForwardPage = flag;
    }

    protected void updateCurrentSettingControl()
    {
        Label labelCtrl = (Label)getControl("__MSG_FORWARD_CONTROL_NAME", com.documentum.web.form.control.Label.class);
        String name = null;
        if(hasTransitionMaxOutputCnt())
            name = getString("MSG_FORWARD_ACTION_HDR_NEW");
        else
            name = getString("MSG_FORWARD_ACTION_HDR");
        labelCtrl.setLabel(name);
    }

    public String getString(String stringId)
    {
        String rv = null;
        if(stringId.equals("MSG_FORWARD_ACTION_HDR_NEW"))
        {
            rv = super.getString("MSG_FORWARD_ACTION_HDR_NEW");
            rv = MessageFormat.format(rv, new Object[] {
                Integer.toString(getMaxSelectValue())
            });
        } else
        {
            rv = super.getString(stringId);
        }
        return rv;
    }

    protected int getTotalSelectValue()
    {
        int currentSelectionCount = 0;
        IWorkflowTask task = getWorkflowTask();
        IDfList pathCount = null;
        try
        {
            pathCount = readPathsSelected(task.getNextForwardActivities());
            if(pathCount != null)
                currentSelectionCount = pathCount.getCount();
        }
        catch(DfException ex)
        {
            ex.printStackTrace();
        }
        return currentSelectionCount;
    }

    private static final String PAGE_FORWARD = "forward";
    private static final String PAGE_ASSIGN_PERFORMERS = "assignperformers";
    private IDfList m_nextPaths;
    public static final String NEXT_TASKS_GRID_CONTROL_NAME = "__NEXT_TASKS_GRID_CONTROL_NAME";
    public static final String MSG_FORWARD_CONTROL_NAME = "__MSG_FORWARD_CONTROL_NAME";
    private static final String EVENT_ONFORWARDONLYTASK = "onForwardOnlyTask";
    private DfList m_theOnlyNextPortNames;
    private boolean m_checkToSkipForwardPage;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/workflow/taskmanager/ForwardWorkflowTask.class


	TOTAL TIME: 31 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't resolve all exception handlers in method onInit
Couldn't fully decompile method onCommitChanges
Couldn't resolve all exception handlers in method onCommitChanges
Couldn't fully decompile method hasPrevPage
Couldn't resolve all exception handlers in method hasPrevPage
Couldn't fully decompile method onPrevPage
Couldn't resolve all exception handlers in method onPrevPage
Couldn't fully decompile method isDynamicPerformersAssigned
Couldn't resolve all exception handlers in method isDynamicPerformersAssigned
Couldn't fully decompile method shouldSkipForwardPage
Couldn't resolve all exception handlers in method shouldSkipForwardPage

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/