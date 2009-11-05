package com.medtronic.ecm.documentum.mdtworkflow.method;

import java.io.OutputStream;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtJobAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;

// executes more generic jobs (ApprovalSpam will be ported to this)
// job objects with necessary parameters hardcoded should be provided in the job definition
// REQUIRED PARAMS: docbase, mdtapp, task

// This method executes IMdtJobAction plugins 

public class MdtConfigurableJob implements IDmMethod
{
    public void execute(Map parameters, OutputStream outputstream) throws Exception
    {
        /*-dbg-*/Lg.wrn("dumping method invocation parameters");
        /*-dbg-*/if (Lg.wrn())try {Iterator i = parameters.keySet().iterator(); while (i.hasNext())Lg.wrn("paramkey: %s",(String)i.next());} catch (Exception e) {}     

        String docbase = MethodUtils.getSingleValueParameter(parameters,"docbase");
        /*-dbg-*/Lg.wrn("  ~~docbase: %s",docbase);
        String mdtapp = MethodUtils.getSingleValueParameter(parameters,"mdtapp");
        /*-dbg-*/Lg.wrn("  ~~mdtapp: %s",mdtapp);
        String taskname = MethodUtils.getSingleValueParameter(parameters,"task");
        /*-dbg-*/Lg.wrn("  ~~task: %s",taskname);
        
        /*-dbg-*/Lg.wrn("getting dbo session");
        IDfSessionManager sessionmgr = MethodUtils.doTrustedLogin(docbase);
        IDfSession session = null;
        try { 
            /*-dbg-*/Lg.wrn("getting sys session");
            session = sessionmgr.getSession(docbase);
            
            /*-dbg-*/Lg.wrn("getting task configuration");
            Map taskconfig = WorkflowUtils.getJobConfig(sessionmgr, docbase, mdtapp, taskname);
            /*-dbg-*/Lg.wrn("task config retrieved");
                                                            
            /*-dbg-*/Lg.wrn("checking for processing plugins");
            if (taskconfig.containsKey("JobPlugins"))
            {
                try {
                    /*-dbg-*/Lg.wrn("execute PLUGIN processors");
                    List methodplugins = (List)taskconfig.get("JobPlugins");
                    for (int i=0; i < methodplugins.size(); i++)
                    {                    
                        /*-dbg-*/Lg.wrn("execute custom processing");
                        MdtPlugin plugin = (MdtPlugin)methodplugins.get(i);
                        /*-dbg-*/Lg.wrn("exec custom action %s",plugin.classname);
                        IMdtJobAction customaction = (IMdtJobAction)MdtPluginLoader.loadPlugin(plugin,sessionmgr);
                        /*-dbg-*/Lg.wrn(" --exec");
                        customaction.execute(sessionmgr, docbase, mdtapp, taskname, parameters, plugin.context);                        
                        /*-dbg-*/Lg.wrn(" --action done");
                    }
                    
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("Error occurred in method plugin processing",e);
                    throw EEx.create("MdtCfgWizMeth-MethodPlugins","Error in method plugin processing",e);
                }
            }
            
            sessionmgr.release(session); session = null;

        } finally {
            /*-dbg-*/Lg.wrn("releasing session");
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {Lg.wrn("Unable to release session",e);}
            /*-dbg-*/Lg.wrn("session released");
        }
        /*-dbg-*/Lg.wrn("method invocation complete");              
    }
    

}
