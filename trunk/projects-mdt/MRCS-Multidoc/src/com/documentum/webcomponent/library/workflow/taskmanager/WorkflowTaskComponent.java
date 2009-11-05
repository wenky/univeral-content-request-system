/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   WorkflowTaskComponent.java

package com.documentum.webcomponent.library.workflow.taskmanager;

import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.services.workflow.common.IPerformerAssignment;
import com.documentum.services.workflow.inbox.IWorkflowTask;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.*;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.databound.DataProvider;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.webcomponent.library.locator.LocatorItemResultSet;
import com.documentum.webcomponent.library.workflow.ScrollableResultSetAdapter;
import java.util.Map;

// Referenced classes of package com.documentum.webcomponent.library.workflow.taskmanager:
//            TaskComponent, DynamicPerformersResultSet

public abstract class WorkflowTaskComponent extends TaskComponent
{

    public WorkflowTaskComponent()
    {
        m_activityName = null;
        m_nextActivities = null;
        m_nextActivitiesPortNames = null;
    }

    protected IWorkflowTask getWorkflowTask()
    {
        return (IWorkflowTask)getTask();
    }

    public void onRender()
    {
        super.onRender();
        if(m_activityName != null)
        {
            updateDynamicPerformersGrid();
            m_activityName = null;
        }
    }

    public boolean canCommitChanges()
    {
        return isDynamicPerformersAssigned();
    }

    public void onSelectDynamicPerformers(Control control, ArgumentList args)
    {
        try
        {
            m_activityName = args.get("activityName");
            IDfList groupNames = null;
            int performerType = -1;
            IPerformerAssignment perf = findPerformerAssignment(m_activityName);
            if(perf != null)
            {
                performerType = perf.getPerformerType();
                groupNames = perf.getPerformerGroups();
            }
            if(performerType != -1)
                launchUserLocatorForDynamicPerformerSelection(performerType, groupNames);
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    private IDfId getActIdFromActName(String actName)
        throws DfException
    {
        IWorkflowTask task = getWorkflowTask();
        IDfId processId = task.getProcessId();
        IDfSession session = getDfSession();
        IDfProcess processObj = (IDfProcess)session.getObject(processId);
        int index = processObj.findString("r_act_name", actName);
        return processObj.getActivityDefId(index);
    }

    private int findActNameIndexInList(IDfList actTypedObjList, String actName)
        throws DfException
    {
        IDfId actId = getActIdFromActName(actName);
        int count = actTypedObjList.getCount();
        for(int i = 0; i < count; i++)
        {
            IDfTypedObject actObj = (IDfTypedObject)actTypedObjList.get(i);
            if(actObj.getId("r_object_id").equals(actId))
                return i;
        }

        return -1;
    }

    protected boolean isDynamicPerformersAssignedHelper(IDfList nextAct)
    {
        boolean retval;
        try {
            retval = true;
            IWorkflowTask task = getWorkflowTask();
            if(!task.isPerformerAssignmentRequired())
                return retval;
            IDfList selectAct = readPathsSelectedActivities(nextAct);
            if(selectAct.getCount() > 0 || m_nextActivities == null)
                m_nextActivities = selectAct;
            else
                selectAct = m_nextActivities;
            IDfList allNextAct = new DfList(1024);
            IDfList forwardAct = task.getNextForwardActivities();
            int count = forwardAct.getCount();
            for(int i = 0; i < count; i++)
                allNextAct.append(forwardAct.get(i));

            IDfList rejectAct = task.getNextRejectActivities();
            count = rejectAct.getCount();
            for(int i = 0; i < count; i++)
                allNextAct.append(rejectAct.get(i));

            IDfEnumeration e = task.getRequiredPerformerAssignments();
            IDfList list;
            do
            {
                IPerformerAssignment perf;
                String perfActName;
                do
                {
                    if(!e.hasMoreElements())
                        return retval;
                    perf = (IPerformerAssignment)e.nextElement();
                    perfActName = perf.getTargetActivityName();
                } while(findActNameIndexInList(selectAct, perfActName) < 0 && findActNameIndexInList(allNextAct, perfActName) >= 0);
                list = perf.getAssignedPerformers();
            } while(list != null && list.getCount() != 0);
            retval = false;
            return retval;
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    protected boolean isDynamicPerformersAssigned()
    {
        boolean retval;
		try {
            retval = true;
            IWorkflowTask task = getWorkflowTask();
            if(!task.isPerformerAssignmentRequired())
                return retval;

            IDfEnumeration e = task.getRequiredPerformerAssignments();
            IDfList list;
            do
            {
                if(!e.hasMoreElements())
                    return retval;

                IPerformerAssignment perf = (IPerformerAssignment)e.nextElement();
                list = perf.getAssignedPerformers();
            } while(list != null && list.getCount() != 0);
            retval = false;
            return retval;
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    private IDfList readPathsSelectedHelper(IDfList actList, boolean bGetPortName, boolean bGetActId)
    {
        DfList list;
        try { 
	        int count = actList.getCount();
	        list = null;
	        if(bGetPortName || bGetActId)
	            list = new DfList(2, count);
	        else
	        {
	            list = new DfList(1024, count);
	        }
	        for(int i = 0; i < count; i++)
	        {
	            IDfTypedObject act = (IDfTypedObject)actList.get(i);
	            String actId = act.getString("r_object_id");
	            Checkbox checkbox = (Checkbox)getControl("__NEXT_TASKS_CHECKBOX_CONTROL_NAME" + actId);
	            if(checkbox == null || !checkbox.getValue())
	                continue;
	            if(bGetPortName)
	            {
	                list.appendString(m_nextActivitiesPortNames.get(i).toString());
	                continue;
	            }
	            if(bGetActId)
	                list.appendString(actId);
	            else
	                list.append(act);
	        }
	
	        return list;
        } catch (DfException e) {
        	throw new WrapperRuntimeException(e);
        }
    }

    protected IDfList readPathsSelectedActivities(IDfList actList)
    {
        return readPathsSelectedHelper(actList, false, false);
    }

    protected IDfList readPathsSelected(IDfList actList)
    {
        return readPathsSelectedHelper(actList, false, true);
    }

    protected IDfList readPathsSelectedActivitiesByPortNames(IDfList actList, IDfList portList)
    {
        m_nextActivitiesPortNames = portList;
        return readPathsSelectedHelper(actList, true, false);
    }

    protected void updateSignoffPanel()
    {
        try
        {
            IWorkflowTask wfTask = getWorkflowTask();
            Panel panelCtrl = (Panel)getControl("__SIGNOFF_PANEL_CONTROL_NAME", com.documentum.web.form.control.Panel.class);
            if(!wfTask.isSignOffRequired())
                hideAndDisableControl(panelCtrl);
            else
                unhideAndEnableControl(panelCtrl);
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void updateDynamicPerformersGrid()
    {
        try
        {
            IWorkflowTask wfTask = getWorkflowTask();
            if(wfTask.isPerformerAssignmentRequired())
            {
                Datagrid gridCtrl = (Datagrid)getControl("__DYNAMIC_PERF_GRID_CONTROL_NAME", com.documentum.web.form.control.databound.Datagrid.class);
                if(gridCtrl != null)
                    gridCtrl.getDataProvider().setScrollableResultSet(new ScrollableResultSetAdapter(new DynamicPerformersResultSet(getWorkflowTask())));
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    public void onReturnFromDynamicPerformerSelection(Form form, Map map)
    {
        try
        {
            if(map != null)
            {
                LocatorItemResultSet setLocatorSelections = (LocatorItemResultSet)map.get("_locator_sel");
                if(setLocatorSelections != null && setLocatorSelections.first())
                {
                    IDfList list = new DfList(2);
                    do
                        if(setLocatorSelections.getObject("r_object_id").toString().startsWith("12"))
                            list.append(setLocatorSelections.getObject("group_name").toString());
                        else
                            list.append(setLocatorSelections.getObject("user_name").toString());
                    while(setLocatorSelections.next());
                    IPerformerAssignment perfAssgn = findPerformerAssignment(m_activityName);
                    if(perfAssgn != null)
                        perfAssgn.assignPerformers(list);
                }
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected IPerformerAssignment findPerformerAssignment(String activityName)
        throws DfException
    {
        IPerformerAssignment perfAssign = null;
        IWorkflowTask task = getWorkflowTask();
        IDfEnumeration e = task.getRequiredPerformerAssignments();
        do
        {
            if(!e.hasMoreElements())
                break;
            IPerformerAssignment perf = (IPerformerAssignment)e.nextElement();
            if(!perf.getTargetActivityName().equals(activityName))
                continue;
            perfAssign = perf;
            break;
        } while(true);
        return perfAssign;
    }

    /**
     * @deprecated Method launchUserLocatorForDynamicPerformerSelection is deprecated
     */

    protected void launchUserLocatorForDynamicPerformerSelection(int performerType, String groupName)
    {
        try
        {
            DfList groupNames = new DfList();
            if(groupName != null && groupName.length() > 0)
                groupNames.append(groupName);
            launchUserLocatorForDynamicPerformerSelection(performerType, ((IDfList) (groupNames)));
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void launchUserLocatorForDynamicPerformerSelection(int performerType, IDfList groupNames)
    {
        ArgumentList args = new ArgumentList();
        args.add("repository", getWorkflowSourceDocbase());
        switch(performerType)
        {
        case 0: // '\0'
        case 1: // '\001'
        case 2: // '\002'
        default:
            break;

        case 3: // '\003'
            setComponentNested("wfuseronlylocatorcontainer", args, getContext(), new FormActionReturnListener(this, "onReturnFromDynamicPerformerSelection"));
            break;

        case 4: // '\004'
        case 5: // '\005'
        case 6: // '\006'
        case 7: // '\007'
            setComponentNested("wfgrouponlylocatorcontainer", args, getContext(), new FormActionReturnListener(this, "onReturnFromDynamicPerformerSelection"));
            break;

        case 8: // '\b'
        case 9: // '\t'
            int count = groupNames.getCount();
            try
            {
                for(int index = 0; index < count; index++)
                    args.add("groupName", groupNames.getString(index));

            }
            catch(DfException e)
            {
                throw new WrapperRuntimeException(e);
            }
            args.add("multiselect", "true");
            if(performerType == 9)
                args.add("arrangeselect", "true");
            setComponentNested("wfuserorgroupfromgrouplocatorcontainer", args, getContext(), new FormActionReturnListener(this, "onReturnFromDynamicPerformerSelection"));
            break;

        case 10: // '\n'
            setComponentNested("workqueuelocatorcontainer", args, getContext(), new FormActionReturnListener(this, "onReturnFromDynamicPerformerSelection"));
            break;
        }
    }

    protected boolean hasTransitionMaxOutputCnt()
    {
        IDfSessionManager smgr = null;
        IDfSession dfsession = null;
        IWorkflowTask task;
        boolean found = false;
        try { 
	        smgr = SessionManagerHttpBinding.getSessionManager();
	        dfsession = null;
	        task = getWorkflowTask();
	        IDfId id = null;
	        IDfPersistentObject perObj = null;
	        found = false;
	        dfsession = smgr.getSession(SessionManagerHttpBinding.getCurrentDocbase());
	        id = task.getActivityId();
	        perObj = dfsession.getObject(id);
	        found = perObj.hasAttr("transition_max_output_cnt");
	        return found;
        } catch (DfException e) {
            e.printStackTrace();        	
            return false;
        } finally {
	        if(dfsession != null)
	            smgr.release(dfsession);
        }
    }

    protected int getMaxSelectValue()
    {
        IDfSessionManager smgr = null;
        IDfSession dfsession = null;
        IWorkflowTask task;
        int maxValCount;
        try { 
	        smgr = SessionManagerHttpBinding.getSessionManager();
	        dfsession = null;
	        task = getWorkflowTask();
	        IDfId id = null;
	        IDfPersistentObject perObj = null;
	        maxValCount = 0;
	        dfsession = smgr.getSession(SessionManagerHttpBinding.getCurrentDocbase());
	        id = task.getActivityId();
	        perObj = dfsession.getObject(id);
	        maxValCount = perObj.getInt("transition_max_output_cnt");
	        if(maxValCount == 0)
	            maxValCount = task.getNextForwardActivities().getCount() + task.getNextRejectActivities().getCount();
	        return maxValCount;
        } catch (DfException e) {
            e.printStackTrace();        	
            return 0;
        } finally {
	        if(dfsession != null)
	            smgr.release(dfsession);
        }
    }

    protected String m_activityName;
    private IDfList m_nextActivities;
    private IDfList m_nextActivitiesPortNames;
    public static final String DYNAMIC_PERF_GRID_CONTROL_NAME = "__DYNAMIC_PERF_GRID_CONTROL_NAME";
    public static final String NEXT_TASKS_CHECKBOX_CONTROL_NAME = "__NEXT_TASKS_CHECKBOX_CONTROL_NAME";
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/workflow/taskmanager/WorkflowTaskComponent.class


	TOTAL TIME: 78 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't fully decompile method isDynamicPerformersAssignedHelper
Couldn't resolve all exception handlers in method isDynamicPerformersAssignedHelper
Couldn't fully decompile method isDynamicPerformersAssigned
Couldn't resolve all exception handlers in method isDynamicPerformersAssigned
Couldn't fully decompile method readPathsSelectedHelper
Couldn't resolve all exception handlers in method readPathsSelectedHelper
Overlapped try statements detected. Not all exception handlers will be resolved in the method hasTransitionMaxOutputCnt
Couldn't fully decompile method hasTransitionMaxOutputCnt
Couldn't resolve all exception handlers in method hasTransitionMaxOutputCnt
Overlapped try statements detected. Not all exception handlers will be resolved in the method getMaxSelectValue
Couldn't fully decompile method getMaxSelectValue
Couldn't resolve all exception handlers in method getMaxSelectValue

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/