package com.medtronic.ecm.documentum.mdtworkflow.method;

import java.io.OutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.mthdservlet.IDmMethod;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtScheduledAction;
import com.medtronic.ecm.documentum.mdtworkflow.util.AttachmentUtils;
import com.medtronic.ecm.documentum.mdtworkflow.util.WorkflowUtils;

//configurable method plugin executor for scheduled jobs, with the implication that these are workflow jobs.
// technically, though, this should be able to execute almost anything

//This method executes IMdtScheduledAction plugins 


public class MdtConfigurableScheduledTask implements IDmMethod
{
    public void execute(Map parameters, OutputStream outputstream) throws Exception
    {
        // login/get system user
        // scan for uncompleted+triggered jobs
        // execute jobs
        /*-INFO-*/Lg.wrn("Configurable Wizard Job Monitor has been invoked");
        /*-dbg-*/Lg.wrn("dumping method invocation parameters");
        /*-dbg-*/if (Lg.wrn())try {Iterator i = parameters.keySet().iterator(); while (i.hasNext())Lg.wrn("paramkey: %s",(String)i.next());} catch (Exception e) {}     

        String docbase = ((String[])parameters.get("docbase_name"))[0];
        /*-dbg-*/Lg.wrn("  ~~docbase: %s",docbase);
        
        /*-dbg-*/Lg.wrn("getting dbo session");
        IDfSessionManager sessionmgr = MethodUtils.doTrustedLogin(docbase);
        executeMonitor(sessionmgr,docbase,parameters);
        /*-dbg-*/Lg.wrn("method invocation complete");              
    }
    
    public void executeMonitor(IDfSessionManager sessionmgr, String docbase, Map parameters) throws Exception
    {
        IDfSession session = null;
        try {
            /*-dbg-*/Lg.wrn("acquire session");
            session = sessionmgr.getSession(docbase);
            // scan for uncompleted triggered jobs
            // SELECT r_object_id FROM mdt_wizard_job WHERE m_exec_status = 'waiting' AND m_exec_date <= DATE(NOW)

            // get queue selection DQL 
            //   Example: "SELECT r_object_id FROM mdt_wizard_job WHERE m_exec_status = 'waiting' AND m_exec_date <= DATE(NOW)"
            String dql = MethodUtils.getSingleValueParameter(parameters, "dql");
            /*-dbg-*/Lg.wrn("exec %s",dql);
            IDfQuery qry = new DfQuery();
            qry.setDQL(dql);
            IDfCollection myObj1 = null;
            try { 
                myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
                while (myObj1.next()) 
                {
                    /*-dbg-*/Lg.wrn("get next task");
                    IDfSysObject jobtaskobj = (IDfSysObject)session.getObject(myObj1.getId("r_object_id"));
                    /*-dbg-*/Lg.wrn("-- JOB TASK: %s",jobtaskobj);
                    try {
                        // exec task
                        /*-dbg-*/Lg.wrn("set exec status to executing, and exec date");
                        jobtaskobj.setString("m_exec_status", "executing");
                        jobtaskobj.setTime("m_last_execution", new DfTime(new Date()));
                        jobtaskobj.save();
                        /*-dbg-*/Lg.wrn("get mdtapp, taskname, and psidocid from arguments");
                        String mdtapp = jobtaskobj.getString("m_application");
                        String taskname = jobtaskobj.getString("m_taskname");
                        String psidocid = jobtaskobj.getValueCount("m_arguments") > 0 ?jobtaskobj.getRepeatingString("m_arguments",0) : null;
                        String relationtype = jobtaskobj.getValueCount("m_arguments") > 1 ?jobtaskobj.getRepeatingString("m_arguments",1) : null;
                        /*-dbg-*/Lg.wrn("getting task configuration");
                        Map taskconfig = WorkflowUtils.getJobConfig(sessionmgr, docbase, mdtapp, taskname);
                        /*-dbg-*/Lg.wrn("task config retrieved");
                        IDfSysObject psidoc = null;
                        /*-dbg-*/Lg.wrn("getting psidoc");
                        psidoc = (IDfSysObject)session.getObject(new DfId(psidocid));
                        /*-dbg-*/Lg.wrn(" -- object: %s",psidoc);
                        List attachments = null;
                        if (psidoc != null) {
                            /*-dbg-*/Lg.wrn("getting attachments");
                            attachments = AttachmentUtils.getAttachmentsByRelationship(session, psidoc,relationtype);
                        }
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
                                    IMdtScheduledAction customaction = (IMdtScheduledAction)MdtPluginLoader.loadPlugin(plugin,sessionmgr);
                                    /*-dbg-*/Lg.wrn(" --exec");
                                    customaction.execute(sessionmgr, docbase, mdtapp, psidoc, attachments,jobtaskobj, parameters, plugin.context);                        
                                    /*-dbg-*/Lg.wrn(" --action done");
                                }
                                
                            } catch (Exception e) {
                                /*-ERROR-*/Lg.err("Error occurred in method plugin processing",e);
                                throw EEx.create("MdtCfgWizMeth-MethodPlugins","Error in method plugin processing",e);
                            }
                        }
                        // calculate "next exec" if m_repeat = true;
                        /*-dbg-*/Lg.wrn("checking for repeat processing");
                        if (jobtaskobj.getBoolean("m_repeat")) {
                            /*-dbg-*/Lg.wrn("this task repeats, setting status back to waiting");
                            jobtaskobj.setString("m_exec_status", "waiting");
                            /*-dbg-*/Lg.wrn("calculating next execution date");
                            jobtaskobj.setTime("m_exec_date", new DfTime(new Date(jobtaskobj.getTime("m_exec_date").getDate().getTime() + (long)jobtaskobj.getInt("m_repeat_interval"))));
                            /*-dbg-*/Lg.wrn("saving");
                            jobtaskobj.save();
                            /*-dbg-*/Lg.wrn("save of repeat exec done");
                        } else {
                            /*-dbg-*/Lg.wrn("set status to finished/success");
                            jobtaskobj.setString("m_exec_status", "finished");
                            jobtaskobj.save();
                            /*-dbg-*/Lg.wrn("done");
                        }
                        
                    } catch (Exception e) {
                        try {
                            /*-ERROR-*/Lg.err("ERROR OCCURRED -- saving message and stack trace in execution instance",e);
                            /*-dbg-*/Lg.wrn("set status to failure");
                            jobtaskobj.setString("m_exec_status","FAILURE");
                            /*-dbg-*/Lg.wrn("clear exec errors attr");
                            jobtaskobj.removeAll("m_exec_errors");
                            /*-dbg-*/Lg.wrn("setting message");
                            String merr = e.getMessage(); if (merr == null) merr = "";
                            jobtaskobj.setRepeatingString("m_exec_errors", 0, merr.length() > 248 ? merr.substring(0,248) : merr);
                            /*-dbg-*/Lg.wrn("get trace");
                            StackTraceElement[] errstac = e.getStackTrace();
                            for (int i=0; i < errstac.length; i++) {
                                String stackerr = errstac[i].getClassName() + '-' + errstac[i].getMethodName() + ':' + errstac[i].getLineNumber();
                                /*-dbg-*/Lg.wrn("appending %s",stackerr);
                                jobtaskobj.setRepeatingString("m_exec_errors", i+1, stackerr.length() > 248 ? stackerr.substring(0,248) : stackerr);                                
                            }
                            /*-dbg-*/Lg.wrn("save error report");
                            jobtaskobj.save();
                            /*-dbg-*/Lg.wrn("done");
                        } catch (Exception e2) {
                            /*-ERROR-*/Lg.err("Failure in execution of mdt wizard job %s",jobtaskobj,e2);                            
                        }
                    }
                }
                myObj1.close(); myObj1 = null;
            } finally {
                if (myObj1 !=null)try{myObj1.close();}catch(Exception e){}
            }
            sessionmgr.release(session); session = null;

        } finally {
            /*-dbg-*/Lg.wrn("releasing session");
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {Lg.wrn("Unable to release session",e);}
            /*-dbg-*/Lg.wrn("session released");
        }
    }
    
    
    public static void main(String[] args)
    {
        // test the job process, even if the f'ing job scheduler isn't working at all...
        try { 
            MdtConfigurableScheduledTask wizmon = new MdtConfigurableScheduledTask();
            IDfClientX clientx = new DfClientX();
            IDfClient client = clientx.getLocalClient();
            IDfSessionManager sMgr = client.newSessionManager();
            IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
            loginInfoObj.setUser("ecsadmin");
            loginInfoObj.setPassword("spring2005");
            loginInfoObj.setDomain(null);
            sMgr.setIdentity("mqadoc_dev", loginInfoObj);             
            Map context = new HashMap();
                            
        } catch (Exception ez) {
            int i = 1;
            i++;
            
        }        
        
    }
    
}
