/*
 * Created on Feb 3, 2005
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

 Filename       $RCSfile: StateTransitionConfigFactory.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2006/09/22 16:07:45 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.config;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;
import com.medtronic.documentum.mrcs.sbo.dto.WFTaskInfo;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StateTransitionConfigFactory extends GenericConfigFactory {

	private static StateTransitionConfigFactory _StateTrnsnConfig = null;

	/**
	 * @throws Exception
	 */
	private StateTransitionConfigFactory()
    {
		super();
	}

	/**
	 * Obtain the StateTransition ConfigFactory
	 * @return
	 * @throws Exception
	 */
	public static StateTransitionConfigFactory getSTConfig()
    {

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(StateTransitionConfigFactory.class))DfLogger.debug(StateTransitionConfigFactory.class, "MRCS:StateTransitionConfigFactory.getSTConfig - check if we need to instantiate", null,null);
        // our factory method for configbroker
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(StateTransitionConfigFactory.class))DfLogger.debug(StateTransitionConfigFactory.class, "MRCS:StateTransitionConfigFactory.getSTConfig - check if we need to instantiate", null,null);
        if (_StateTrnsnConfig == null) {
        	synchronized (StateTransitionConfigFactory.class) {
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(StateTransitionConfigFactory.class))DfLogger.debug(StateTransitionConfigFactory.class, "MRCS:StateTransitionConfigFactory.getSTConfig - need to instantiate and load config", null,null);
                _StateTrnsnConfig = new StateTransitionConfigFactory();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(StateTransitionConfigFactory.class))DfLogger.debug(StateTransitionConfigFactory.class, "MRCS:StateTransitionConfigFactory.getSTConfig - config loaded", null,null);
            }
        }

        return _StateTrnsnConfig;
    }

    /**
     * Get the configured State Information for the current application.
     *
     * @deprecated
     * @param application
     * @return
     * @throws Exception
     */
    public StateInfo getStateInfo(String application, String state) 
    {
    	// we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getStateInfo : app " + app, null,null);
        StateInfo stInf =(StateInfo) app.MrcsLifecycleState.get(state);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getStateInfo : stInf" + stInf, null,null);
        return stInf;
    }


    /**
     * Get the configured State Information for the current application.
     *
     * @deprecated
     * @param application
     * @return
     * @throws Exception
     */
    public WFTaskInfo getWFTaskInfo(String application, String task)
    {
    	// we know the app from the Document DCTM object, passed in as the application parameter
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : application : "+ application, null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : task : "+ task, null,null);
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : app : "+ app, null,null);
        WFTaskInfo tskInf =(WFTaskInfo) app.MrcsWFTasks.get(task);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : tskInf : "+ tskInf, null,null);
        //if(tskInf != null){
        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : Action : "+ tskInf.getAction(), null,null);
        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : Name : " +tskInf.getName(), null,null);
        ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:StateTransitionConfigFactory.getWFTaskInfo : plugin : " + tskInf.getMrcsPlugin(), null,null);
        //}
        return tskInf ;
    }
    
    /**
     * @deprecated
     * @param application
     * @param task
     * @return
     */
    public List getWFTaskValidations(String application, String task)
    {
        MrcsApplication app = getApplication(application);
        WFTaskInfo tskInf =(WFTaskInfo) app.MrcsWFTasks.get(task);
        if (tskInf.Validations != null)
        {
            List validations = new ArrayList();
            for (int i=0; i < tskInf.Validations.size(); i++)
            {
                MrcsPlugin curplug = (MrcsPlugin)tskInf.Validations.get(i);
                validations.add(curplug);
            }
            return validations;
        }
        return null;
    }    
    
    /**
     * @deprecated
     * @param application
     * @param task
     * @return
     */
    public List getWFTaskAnalysis(String application, String task)
    {
        MrcsApplication app = getApplication(application);
        WFTaskInfo tskInf =(WFTaskInfo) app.MrcsWFTasks.get(task);
        if (tskInf.Analysis != null)
        {
            List analysis = new ArrayList();
            for (int i=0; i < tskInf.Analysis.size(); i++)
            {
                MrcsPlugin curplug = (MrcsPlugin)tskInf.Analysis.get(i);
                analysis.add(curplug);
            }
            return analysis;
        }
        return null;
    }
    
    public List getMrcsWorkflowTaskValidations(String application, String workflow, String task)
    {
        MrcsWorkflowTask mrcstask = (MrcsWorkflowTask)getMrcsWorkflowTask(application,workflow,task);
        return mrcstask.Validations;
    }
    
    public List getMrcsWorkflowTaskAnalysis(String application, String workflow, String task)
    {
        MrcsWorkflowTask mrcstask = (MrcsWorkflowTask)getMrcsWorkflowTask(application,workflow,task);
        return mrcstask.Analysis;
    }
    
    public MrcsWorkflowTask getMrcsWorkflowTask(String application, String workflow, String task)
    {
    	/*-CONFIG-*/String m="getMrcsWorkflowTask-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        MrcsApplication app = getApplication(application);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"look up wf", null,null);
        MrcsWorkflow mrcswf = (MrcsWorkflow)app.MrcsWorkflows.get(workflow);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"wf found? "+(mrcswf != null), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"find task in task list, task list exist? "+(mrcswf.Tasks != null), null,null);
        for (int i=0; i < mrcswf.Tasks.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"processing task #"+i, null,null);
        	MrcsWorkflowTask curtask = (MrcsWorkflowTask)mrcswf.Tasks.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"curtask name: "+curtask.Name, null,null);
        	if (curtask.Name.equals(task)) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"task match found, returning", null,null);
        		return curtask;
        		
        	}
        }
        return null;
    }
    
    public boolean isMrcsWorkflow(String docbase,String workflow)
    {    	
        Iterator apps = getApplications().keySet().iterator();
        while (apps.hasNext())
        {
            MrcsApplication app = getApplication((String)apps.next());
            // check for legacy lc/wf (same thing that isLegacyLCWF does, except we bypass an extra hashmap lookup and log statements this way...
            if (app.MrcsLifecycles == null)
            {
            	// legacy WF/LC impl pre MRCS 4.2 - for compatibility
	            if (app.DocBase.equals(docbase))
	            {
	                if (app.MrcsWFTemplates.containsKey(workflow))
	                    return true;
	            }
            } else {
            	// post-MRCS 4.2
            	if (app.DocBase.equals(docbase))
            	{
            		if (app.MrcsWorkflows.containsKey(workflow))
            			return true;
            	}
            }
        }
        return false;
    }
    
    /**
     * @deprecated
     * @param mrcsapp
     * @param lifecycle
     * @return
     */
    public StateInfo getFirstStateInfo (String mrcsapp, String lifecycle)
    {
        MrcsApplication app = getApplication(mrcsapp);
        Lifecycle lc = (Lifecycle)app.MrcsDocumentLifecycles.get(lifecycle);
        MrcsLifecycleState state = (MrcsLifecycleState)lc.LifecycleStates.get(0);
        String statename = (String)state.State.get(0);
        StateInfo stinfo = (StateInfo)app.MrcsLifecycleState.get(statename);        
        return stinfo;
    }
            
    public MrcsLifecycleState getLifecycleState(String mrcsapp, String lifecycle, String statename)
    {
    	/*-CONFIG-*/String m="getLifecycleState-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app found: "+(app != null), null,null);
        MrcsLifecycle lc = (MrcsLifecycle)app.MrcsLifecycles.get(lifecycle);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"lc found: "+lifecycle+" - "+(lc != null), null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"look for state "+statename, null,null);
        for (int i=0; i < lc.States.size(); i++)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iteration - "+i, null,null);
        	MrcsLifecycleState state = (MrcsLifecycleState)lc.States.get(i);
        	if (state.Name.equals(statename))
        	{
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"state found", null,null);
        		return state;
        	}
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"state not found", null,null);
        return null;
    }
    
    public String getMrcsLifecycleFromSystemLifecycleName(String mrcsapp,String docconfig,String gfconfig,String systemlifecycle)
    {
    	/*-CONFIG-*/String m="getMrcsLifecycleFromSystemLifecycleName-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"locate gf def", null,null);
        MrcsGroupingFolderType gf = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gfconfig);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"locate doctype in allowable docs", null,null);
        for (int i=0; i < gf.AllowableDocumentTypes.size(); i++)
        {
        	MrcsGroupingFolderAllowableDocument gfad = (MrcsGroupingFolderAllowableDocument)gf.AllowableDocumentTypes.get(i);
        	if (gfad.DocumentType.equals(docconfig))
        	{
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Allowable document match found, mrcs lifecycle is "+gfad.Lifecycle, null,null);
        		return gfad.Lifecycle;
        	}
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Allowable document match NOT FOUND!!", null,null);
        return null;
    }
    
    public List getWorkflowsForCurrentState(String application, String docconfig, String gfconfig, String systemlifecycle, String currentstate)
    {
    	StateTransitionConfigFactory stconfig = StateTransitionConfigFactory.getSTConfig();
    	String mrcslc = stconfig.getMrcsLifecycleFromSystemLifecycleName(application,docconfig,gfconfig,systemlifecycle);
        MrcsLifecycleState lcconfig = stconfig.getLifecycleState(application,mrcslc,currentstate);
        return lcconfig.AllowableWorkflows;
    }

    
    public List getLifecyclePostPluginList(String mrcsapp, String docconfig, String gfconfig, String systemlifecycle, String targetstate)
    {
    	/*-CONFIG-*/String m="getLifecyclePostPluginList-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app found: "+(app != null), null,null);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get mrcs lifecycle name for lifecycle "+systemlifecycle, null,null);
        String lifecycle = getMrcsLifecycleFromSystemLifecycleName(mrcsapp,docconfig,gfconfig,systemlifecycle);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS lifecycle "+lifecycle, null,null);

        List pluginlist = new ArrayList();

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"find lc state", null,null);
        MrcsLifecycleState lcstate = getLifecycleState(mrcsapp, lifecycle, targetstate);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"state found? "+lcstate, null,null);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding lcstate's plugins", null,null);
        pluginlist.add(lcstate.ServerPostPlugins);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking for additional defined types in GFAllowableDocs", null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gfconfig);
        for (int i=0; i < gftype.AllowableDocumentTypes.size(); i++)
        {
        	MrcsGroupingFolderAllowableDocument gfdoc = (MrcsGroupingFolderAllowableDocument)gftype.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current gfdoctype: "+gfdoc.DocumentType, null,null);
        	if (gfdoc.DocumentType.equals(docconfig))
        	{
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype found, adding gf-specific plugins", null,null);
        		pluginlist.add(gfdoc.LifecyclePostPlugins);
        	}
        }
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning plugin list", null,null);
    	return pluginlist;
    }
    
    public List getLifecycleActionPluginList(String mrcsapp, String docconfig, String gfconfig, String systemlifecycle, String targetstate)
    {
    	/*-CONFIG-*/String m="getLifecycleActionPluginList-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app found: "+(app != null), null,null);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get mrcs lifecycle name for lifecycle "+systemlifecycle, null,null);
        String lifecycle = getMrcsLifecycleFromSystemLifecycleName(mrcsapp,docconfig,gfconfig,systemlifecycle);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS lifecycle "+lifecycle, null,null);
        
        List pluginlist = new ArrayList();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"find lc state", null,null);
        MrcsLifecycleState lcstate = getLifecycleState(mrcsapp, lifecycle, targetstate);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"state found? "+lcstate, null,null);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding lcstate's plugins", null,null);
        pluginlist.add(lcstate.ServerActionPlugins);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking for additional defined types in GFAllowableDocs", null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gfconfig);
        for (int i=0; i < gftype.AllowableDocumentTypes.size(); i++)
        {
        	MrcsGroupingFolderAllowableDocument gfdoc = (MrcsGroupingFolderAllowableDocument)gftype.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current gfdoctype: "+gfdoc.DocumentType, null,null);
        	if (gfdoc.DocumentType.equals(docconfig))
        	{
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype found, adding gf-specific plugins", null,null);
        		pluginlist.add(gfdoc.LifecycleActionPlugins);
        	}
        }
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning plugin list", null,null);
    	return pluginlist;
    }

    public List getLifecycleEntryPluginList(String mrcsapp, String docconfig, String gfconfig, String systemlifecycle, String targetstate)
    {
    	/*-CONFIG-*/String m="getLifecycleEntryPluginList-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app found: "+(app != null), null,null);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"get mrcs lifecycle name for lifecycle "+systemlifecycle, null,null);
        String lifecycle = getMrcsLifecycleFromSystemLifecycleName(mrcsapp,docconfig,gfconfig,systemlifecycle);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"MRCS lifecycle "+lifecycle, null,null);
        
        List pluginlist = new ArrayList();
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"find lc state", null,null);
        MrcsLifecycleState lcstate = getLifecycleState(mrcsapp, lifecycle, targetstate);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"state found? "+lcstate, null,null);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding lcstate's plugins", null,null);
        pluginlist.add(lcstate.ServerEntryPlugins);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking for additional defined types in GFAllowableDocs", null,null);
        MrcsGroupingFolderType gftype = (MrcsGroupingFolderType)app.GroupingFolderTypes.get(gfconfig);
        for (int i=0; i < gftype.AllowableDocumentTypes.size(); i++)
        {
        	MrcsGroupingFolderAllowableDocument gfdoc = (MrcsGroupingFolderAllowableDocument)gftype.AllowableDocumentTypes.get(i);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current gfdoctype: "+gfdoc.DocumentType, null,null);
        	if (gfdoc.DocumentType.equals(docconfig))
        	{
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype found, adding gf-specific plugins", null,null);
        		pluginlist.add(gfdoc.LifecycleEntryPlugins);
        	}
        }
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"returning plugin list", null,null);
    	return pluginlist;
    }

    
    public boolean doesWorkflowTaskAllowMultiplePaths(String mrcsapp, String workflow, String task)
    {
    	/*-CONFIG-*/String m="doesWorkflowTaskAllowMultiplePaths-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app name: "+mrcsapp, null,null);
        MrcsApplication app = getApplication(mrcsapp);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"app found: "+(app != null), null,null);
        if (this.isLegacyLCWF(mrcsapp))
        {
        	// pre MRCS4.2 workflow - legacy compatibility only
            WFTaskInfo tskInfo = getWFTaskInfo(mrcsapp, task);
            return tskInfo.AllowMultipleForwardPaths;
        } else {
        	MrcsWorkflowTask mrcstask = this.getMrcsWorkflowTask(mrcsapp,workflow,task);
        	return mrcstask.AllowMultipleForwardPaths;
        }
    }
    
    public Map getUniqueWorkflowTaskMethodConfiguration(String workflow, String task)
    {
    	/*-CONFIG-*/String m="getUniqueWorkflowTaskConfiguration-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"top", null,null);
    	Iterator apps = this.getApplications().keySet().iterator();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterating mrcsapps", null,null);
    	while (apps.hasNext())
    	{
    		String curapp = (String)apps.next();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current app: "+curapp, null,null);
    		MrcsApplication app = this.getApplication(curapp);
    		if (app.MrcsWorkflows.containsKey(workflow))
    		{
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"workflow found: "+workflow, null,null);
    			MrcsWorkflow flow = (MrcsWorkflow)app.MrcsWorkflows.get(workflow);
    			for (int i=0; i < flow.Tasks.size(); i++)
    			{
    				MrcsWorkflowTask wftask = (MrcsWorkflowTask)flow.Tasks.get(i);
    				if (wftask.Name.equals(task)) {
    	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"task found: "+task, null,null);    					
    					return wftask.MethodConfiguration;
    				}
    			}
    		}
    	}
    	throw new RuntimeException("MRCS task configuration for task "+task+" in workflow "+workflow+" not found");
    }
    
}
