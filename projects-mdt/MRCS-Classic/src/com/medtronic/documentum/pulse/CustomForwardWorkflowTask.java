package com.medtronic.documentum.pulse;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.*;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.workflow.ScrollableResultSetAdapter;
import java.text.MessageFormat;
import com.documentum.webcomponent.library.workflow.taskmanager.WorkflowTaskComponent;
import com.documentum.webcomponent.library.workflow.taskmanager.TaskComponentContainer;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.fc.client.IDfUser;
import com.documentum.web.form.control.Text;
import com.documentum.webcomponent.library.workflow.taskmanager.NextTasksResultSet;

import java.text.*;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.services.workflow.inbox.*;
import com.documentum.web.common.*;
import com.documentum.web.form.control.*;
import com.documentum.web.form.control.databound.*;
import com.documentum.webcomponent.library.messages.*;
import com.documentum.webcomponent.library.workflow.*;
import com.documentum.webcomponent.library.workflow.taskmanager.*;
import com.documentum.webcomponent.library.workflow.taskmanager.
        TaskComponentContainer.*;

// Referenced classes of package com.documentum.webcomponent.library.workflow.taskmanager:
//            WorkflowTaskComponent, NextTasksResultSet, TaskComponentContainer, TaskComponent

public class CustomForwardWorkflowTask extends WorkflowTaskComponent
    implements TaskComponentContainer.ITerminateEventHandler
{

    public CustomForwardWorkflowTask()
    {
        m_nextPaths = null;
        m_theOnlyNextTask = null;
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
        try
        {
            boolean retValue;
            /**
             * custom logic check to evaluate login name
             */
            if (checkLoginName())
            {

                if ((retValue = super.onCommitChanges())) {
                    task = getWorkflowTask();
                    IDfList nextPaths;
                    if (getComponentPage().equals("forward"))
                        nextPaths = readPathsSelected(task.
                                getNextForwardActivities());
                    else
                        nextPaths = m_nextPaths;
                    if (nextPaths == null || nextPaths.getCount() == 0) {
                        setErrorMessage("MSG_FORWARD_PATH_MUST_BE_SELECTED");
                        return false;
                    }
                    String actId = task.getActivityId().toString();
                    boolean isSequential = task.isSequentialActivity();
                    if (isSequential && nextPaths.findStringIndex(actId) >= 0 &&
                        nextPaths.getCount() != 1) {
                        setErrorMessage("MSG_SEQUENTIAL_FORWARD_PATH_EXCLUSION");
                        return false;
                    }
                    if (!isSequential || nextPaths.findStringIndex(actId) < 0)
                        task.setNextActivities(nextPaths);
                    if (task.isSignOffRequired())
                        task.setSignoffPassword(readSignOffPasswordValue());
                    if (hasTransitionMaxOutputCnt() &&
                        getTotalSelectValue() > getMaxSelectValue()) {
                        setErrorMessage("MSG_MAX_SELECT_FORWARD_COUNT");
                        return false;
                    }
                    task.completeTask();
                    setMessage("MSG_FORWARD_SUCCESS", new Object[] {
                               super.getString("MSG_OBJECT")
                    });
                }
            }

        else
        {
            setErrorMessage("MSG_INCORRECT_USER_OS_NAME");
            return false;
        }

            return retValue;
        }
        catch(DfException e)
        {
            try
            {
                if(task.getCompleteErrStatus() == 4)
                {
                    setErrorMessage("MSG_INCORRECT_PASSWORD");
                    return false;
                } else
                {
                    setErrorMessage("MSG_UNEXPECTED_ERROR", new Object[] {
                        e.getMessage()
                    }, e);
                    return false;
                }
            }
            catch(DfException e2)
            {
                throw new WrapperRuntimeException(e2);
            }
        }
    }//end of onInit

    /**
     * setErrorMessage
     *
     * @param string String
     * @param objects Object[]
     * @param e DfException
     */
    void setErrorMessage(String id, Object params[], Exception e)
    {
        setReturnError(id, params, e);
        ErrorMessageService.getService().setNonFatalError(this, id, params,
                e);
    }

    /**
     * setMessage
     *
     * @param string String
     * @param objects Object[]
     */
    void setMessage(String id, Object params[])
    {
        MessageService.addMessage(this, id, params);
    }

    /**
     * setErrorMessage
     *
     * @param string String
     */
    private void setErrorMessage(String strId)
    {
        setReturnError(strId, null, null);
        ErrorMessageService.getService().setNonFatalError(this, strId, null);
    }

    /**
     * Custom check method to evaluate login name, force the user to enter login name
     * while signing off work flow
     * @return boolean
     */
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
            Text txtUserId = (Text) getControl("__USERID_CONTROL_NAME",
                                               com.documentum.web.form.control.
                                               Text.class);
            String strUserEnteredId = txtUserId.getValue();
            System.out.println("User Entered Login Name:" +
                               strUserEnteredId);
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
        try
        {
            String page = getComponentPage();
            boolean retValue;
            if(page.equals("assignperformers"))
                retValue = false;
            else
                retValue = getWorkflowTask().isPerformerAssignmentRequired();
            return retValue;
        }
        catch(DfException e)
        {
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
        try
        {
            String page = getComponentPage();
            boolean retValue;
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
        }
        catch(DfException e)
        {
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
            if(m_theOnlyNextTask != null)
                wfTask.setNextActivities(m_theOnlyNextTask);
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
        try
        {
            IWorkflowTask wfTask = getWorkflowTask();
            if(wfTask.isPerformerAssignmentRequired() || wfTask.isSignOffRequired())
                return false;
            IDfList nextTasks = wfTask.getNextForwardActivities();
            if(nextTasks.getCount() != 1)
                return false;
            IDfTypedObject act = (IDfTypedObject)nextTasks.get(0);
            String actId = act.getString("r_object_id");
            if(!wfTask.getActivityId().toString().equals(actId))
            {
                m_theOnlyNextTask = new DfList(2, 1);
                m_theOnlyNextTask.appendString(actId);
            }
            return true;
        }
        catch(DfException e)
        {
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
    private DfList m_theOnlyNextTask;
    private boolean m_checkToSkipForwardPage;
}
