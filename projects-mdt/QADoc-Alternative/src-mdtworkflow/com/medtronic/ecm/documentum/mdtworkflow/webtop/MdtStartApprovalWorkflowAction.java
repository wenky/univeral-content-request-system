package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfEnumeration;
import com.documentum.fc.client.IDfProcess;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfList;
import com.documentum.services.workflow.common.IPerformerAssignment;
import com.documentum.services.workflow.common.IWorkflowTaskAttachment;
import com.documentum.services.workflow.startworkflow.IAliasAssignment;
import com.documentum.services.workflow.startworkflow.IStartWorkflow;
import com.documentum.services.workflow.startworkflow.IStartWorkflowConfig;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionExecution;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowTemplateResolver;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowValidation;

public class MdtStartApprovalWorkflowAction implements IActionExecution, IActionPrecondition
{

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component) 
    {
        // permit level of user must be relate (or startworkflow extended permit?)
        /*-dbg-*/Lg.dbg("check basic action validations");
        String objid = argumentlist.get("objectId");
        /*-dbg-*/Lg.dbg("form object id: %s",objid);
        
        try {            
            /*-dbg-*/Lg.dbg("look up document");
            IDfSysObject formobject = (IDfSysObject)component.getDfSession().getObject(new DfId(objid));            
            /*-dbg-*/Lg.dbg("form object: %s",formobject);
            if (!formobject.getType().isTypeOf("mdt_workflow_form")) {
                /*-dbg-*/Lg.dbg("not a workflow form (mdt_workflow_form)");
                return false;
            }
            /*-dbg-*/Lg.dbg("check if user has execute extended permit on the form object");
            if (formobject.hasPermission(IDfACL.DF_XPERMIT_EXECUTE_PROC_STR, formobject.getSession().getLoginUserName()))
            {
                /*-dbg-*/Lg.dbg("User Has extended permit - query execute is true");
                return true;
            }
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Exception checking form object's exec proc extended permit ",dfe);
            throw EEx.create("StartApproval-CheckExecPermit","Exception checking form object's exec proc extended permit ",dfe);            
        }
        
        /*-dbg-*/Lg.dbg("User DOES NOT have exec proc permit - query exec failed");
        return false;
    }

    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map) 
    {
        /*-dbg-*/Lg.dbg("Execute Start Approval - get approval form object's id from args");
        String objid = argumentlist.get("objectId");
        /*-dbg-*/Lg.dbg("approval form arg id: %s",objid);
        try {            
            // get main form to be routed
            IDfSysObject formobject = (IDfSysObject)component.getDfSession().getObject(new DfId(objid));
            /*-dbg-*/Lg.dbg("approval form : %s",formobject);
            // get mdt application from form object
            String mdtapp = formobject.getString("m_application");
            /*-dbg-*/Lg.dbg("approval form mdt app: %s",mdtapp);
            MdtConfigService cfgsvc = MdtConfigService.getConfigService(component.getDfSession().getSessionManager(), component.getDfSession().getDocbaseName());
            /*-dbg-*/Lg.dbg("lookup config for mdt app");
            Map mdtcfg = (Map)cfgsvc.getAppConfig(mdtapp);
            // determine workflow/process id to be launched
            /*-dbg-*/Lg.dbg("Default workflow/approval from resolution - get Change Requests map from config");
            Map workflows = (Map)mdtcfg.get("ChangeRequests");
            /*-dbg-*/Lg.dbg("get change request for approval form type");
            Map wfdef = (Map)workflows.get(formobject.getTypeName());
            /*-dbg-*/Lg.dbg("get DCTM wofklow template dm_process name");
            String processname = (String)wfdef.get("WorkflowTemplate");
            if (processname == null) {
                // see if there is a resolver configured
                if (wfdef.containsKey("WorkflowTemplateResolver")) {
                    MdtPlugin resolverplugin = (MdtPlugin)wfdef.get("WorkflowTemplateResolver");
                    IMdtWorkflowTemplateResolver resolver = (IMdtWorkflowTemplateResolver)MdtPluginLoader.loadPlugin(resolverplugin, component.getDfSession().getSessionManager());
                    processname = resolver.resolveWFT(component.getDfSession().getSessionManager(), component.getDfSession().getDocbaseName(), mdtapp, formobject, resolverplugin.context);
                }
            }
            /*-dbg-*/Lg.dbg("get dm_process %s",processname);
            IDfProcess process = (IDfProcess)component.getDfSession().getObjectByQualification("dm_process where object_name = '"+processname+"'");
            
            // validations - i.e. no pdf renditions, attachments are in base state, attachments are not in another workflow, required attributes, form is in proper state, not in-wf,
            // May need to do this in QueryExecute, but that would probably hammer the server
            /*-dbg-*/Lg.dbg("check for workflow validations");
            if (wfdef.containsKey("WorkflowValidations")) {
                // execute workflow validations
                /*-dbg-*/Lg.dbg("get validation list");
                List wfvalidations = (List)wfdef.get("WorkflowValidations");
                List errors = new ArrayList();
                /*-dbg-*/Lg.dbg("iterating");
                for (int i=0; i < wfvalidations.size(); i++)
                {
                    /*-dbg-*/Lg.dbg("plugin #%d",i);
                    MdtPlugin validationplugin = (MdtPlugin)wfvalidations.get(i);
                    /*-dbg-*/Lg.dbg("load validation %s",validationplugin == null ? null : validationplugin.classname);
                    IMdtWorkflowValidation validation = (IMdtWorkflowValidation)MdtPluginLoader.loadPlugin(validationplugin, component.getDfSession().getSessionManager());
                    /*-dbg-*/Lg.dbg("exec");
                    boolean success = validation.validate(component.getDfSession().getSessionManager(), component.getDfSession().getDocbaseName(), mdtapp, formobject, errors, validationplugin.context);
                    if (!success) {
                        //check validation plugin config see if we should stop validating
                        /*-dbg-*/Lg.dbg("validation failure, plugin check for validation stoppage");
                        Map pluginconfig = (Map)validationplugin.context;
                        if (pluginconfig != null && pluginconfig.containsKey("StopOnFailure")) {
                            /*-dbg-*/Lg.dbg("StopOnFailure detected, stopping validation");
                            i = wfvalidations.size();
                        }
                    }
                    
                    /*-dbg-*/Lg.dbg("done");
                }
                
                /*-dbg-*/Lg.dbg("check error count");
                if (errors.size() > 0) {
                    
                    // TODO: error translations? formatting? velocity templating?                    
                    
                    /*-dbg-*/Lg.dbg("errors detected, jumping to display component");
                    ArgumentList jumpargs = new ArgumentList();
                    jumpargs.add("objectId",objid);
                    String[] argarray = new String[errors.size()];
                    for (int a=0; a < errors.size(); a++) argarray[a] = (String)errors.get(a);
                    jumpargs.add("validationerrors", argarray);
                    // validation failed, jump to display component
                    component.setComponentNested("mdtdisplayworkflowvalidationerrors", jumpargs, context,null);
                    return false;
                }
                
            }
                        
            // get workflow "service"            
            /*-dbg-*/Lg.dbg("create crappy wdk service object IStartWorkflow");
            IStartWorkflow swService = (IStartWorkflow)DfClient.getLocalClient().newService((IStartWorkflow.class).getName(), component.getDfSession().getSessionManager());
            /*-dbg-*/Lg.dbg("get IStartWorkflowConfig from IStartWorkflow service object");
            IStartWorkflowConfig swConfig = swService.getConfig(process.getObjectId(), component.getDfSession().getDocbaseName());
            /*-dbg-*/Lg.dbg("set supervisor to current user");
            swConfig.setSupervisor(component.getDfSession().getLoginUserName());            

            // dynamic alias assignment
            /*-dbg-*/Lg.dbg("check if Alias Assignments are needed (usually for aliased task user assignments");
            if(swConfig.isAliasAssignmentRequired())
            {
                /*-dbg-*/Lg.dbg("iterate on alias assignments");
                for(IDfEnumeration e = swConfig.getRequiredAliasAssignments(); e.hasMoreElements();)
                {
                    IAliasAssignment aliasAssg = (IAliasAssignment)e.nextElement();
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("alias name: "+aliasAssg.getAliasName()+" desc: "+aliasAssg.getAliasDescription());
                    // big fat TODO here...
                }
            }
            
            /*-dbg-*/Lg.dbg("get mdt task configurations for current workflow/approval form/changerequest");
            Map tasks = (Map)wfdef.get("Tasks");
            
            // dynamic performers assignment (I assume this is <Workflow Initiation> performer assignment)
            /*-dbg-*/Lg.dbg("check for dynamic perfomer assignment (tasks whose perfomers are assigned at <Workflow Initiation>");
            if(swConfig.isPerformerAssignmentRequired())
            {
                /*-dbg-*/Lg.dbg("performer assignment necessary - iterate through tasks");
                for(IDfEnumeration e = swConfig.getRequiredPerformerAssignments(); e.hasMoreElements();)
                {
                    /*-dbg-*/Lg.dbg("get crappy wrapper for current approver assignment");
                    IPerformerAssignment perfAssg = (IPerformerAssignment)e.nextElement();
                    // get activity name
                    /*-dbg-*/Lg.dbg("get activity name");
                    String targetActName = perfAssg.getTargetActivityName();
                    // from wf config, get form attribute that contains approvers to assign
                    
                    /*-dbg-*/Lg.dbg("get MDT Task Config for activity %s",targetActName);
                    Map taskdef = (Map)tasks.get(targetActName);
                    
                    // TODO - may pluginify this, for now the "PerfomerAttribute" will be the default method
                    /*-dbg-*/Lg.dbg("look up attribute in approval form that has the performers for this task");
                    String performerFormAttribute = (String)taskdef.get("PerformerAttribute");
                    /*-dbg-*/Lg.dbg("get performer list from from atribute %s",performerFormAttribute);
                    // determine if attribute is repeating or single
                    IDfList performers = new DfList();
                    if (formobject.isAttrRepeating(performerFormAttribute))
                    {
                        /*-dbg-*/Lg.dbg("iterate on repeating values");
                        for (int i=0; i < formobject.getValueCount(performerFormAttribute); i++) {
                            String user = formobject.getRepeatingString(performerFormAttribute, i);
                            /*-dbg-*/Lg.dbg("add user %s to performers",user);
                            performers.append(user);
                        }
                    } else {
                        /*-dbg-*/Lg.dbg("get single  value");
                        String user = formobject.getString(performerFormAttribute);
                        /*-dbg-*/Lg.dbg("add user %s to performers",user);
                        performers.append(user);
                    }
                    /*-dbg-*/Lg.dbg("assign performers");
                    perfAssg.assignPerformers(performers);

                    // Start Workflow was doing this. I don't have any idea what is going on here...
                    IDfList groups = perfAssg.getPerformerGroups();
                    int count = groups.getCount();
                    for(int index = 0; index < count; index++)
                    {
                        String performerGroup = groups.getString(index);
                        if(!performerGroup.equals("%__wfm_user__") && !performerGroup.equals("%__wfm_group__") && performerGroup.startsWith("%") && performerGroup.indexOf(".") == -1)
                        {
                            // so IF performer group isn't wfm_user alias, or wfm_group alias, but is an alias, but doesn't have a dot '.' in it, do a default alias assignment?
                            //DefaultAliasState alias = getDefaulAlias(performerGroup.substring(1));
                            //groups.set(index, alias);
                        }
                    }
                    
                }

            }
            
            
            // attach main package
            /*-dbg-*/Lg.dbg("attach approval form as workflow package (crappy WDK calls it a IWorkflowTaskAttachment)");
            IDfList pkgcfglist = swConfig.getAttachments();
            /*-dbg-*/Lg.dbg("iterate through the expected attachments (there should almost always only be one WF package - the Approval form)");;
            int pkgcnt = pkgcfglist.getCount();
            /*-dbg-*/Lg.dbg("begin iterations, expected package count is: %d",pkgcnt);
            for (int i=0; i < pkgcnt; i++) {
                // there REALLY should only be one...
                /*-dbg-*/Lg.dbg("iteration #%d",i);
                IWorkflowTaskAttachment curpkg = (IWorkflowTaskAttachment)pkgcfglist.get(i);
                /*-dbg-*/if(Lg.dbg())Lg.dbg("package name: %s",curpkg.getPackageName());
                IDfList wfpkgs = new DfList();
                /*-dbg-*/if(Lg.dbg())Lg.dbg("append approval form object to package r_component_id list");
                wfpkgs.append(formobject.getObjectId());
                /*-dbg-*/if(Lg.dbg())Lg.dbg("set package to document list");
                curpkg.addDocuments(wfpkgs);
            }
            
            // ?add attachments now? - sure...
            /*-dbg-*/Lg.dbg("add approval form document attachments (as in dmi_attachments table) to workflow");
            if (wfdef.containsKey("AttachmentsAttribute")) {
                String attachmentsattr = (String)wfdef.get("AttachmentsAttribute");
                /*-dbg-*/Lg.dbg("iterate through attachments attribute %s",attachmentsattr);
                for (int i=0; i < formobject.getValueCount(attachmentsattr); i++)
                {
                    /*-dbg-*/Lg.dbg("value #%s",i);
                    String docname = (String)formobject.getRepeatingString(attachmentsattr, i);
                    /*-dbg-*/Lg.dbg("look up document %s",docname);
                    IDfSysObject attachment = AttachmentUtils.lookupAttachmentByName(formobject,docname);
                    /*-dbg-*/Lg.dbg("adding document workflow attachment %s",attachment);
                    swConfig.addWorkflowAttachment(attachment.getTypeName(), attachment.getObjectId());
                }
            }
            
            /*-dbg-*/Lg.dbg("initiate workflow");
            swService.startWorkflow(swConfig);
            /*-dbg-*/Lg.dbg("done");
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("Exception initiating workflow for approval form %s",objid,dfe);
            throw EEx.create("StartApproval-WFInitError","Exception initiating workflow for approval form %s",objid,dfe);                       
        }
        
        return false;
    }

    // requires object id of the form...
    public String[] getRequiredParams() {
        return (new String[] {
                "objectId"
            });
    }

}
