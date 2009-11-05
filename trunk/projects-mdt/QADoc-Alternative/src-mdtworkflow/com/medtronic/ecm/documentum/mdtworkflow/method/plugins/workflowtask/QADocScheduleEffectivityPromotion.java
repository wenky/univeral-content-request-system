package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.workflowtask;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.mdtworkflow.method.plugins.IMdtWorkflowAction;

public class QADocScheduleEffectivityPromotion implements IMdtWorkflowAction 
{

    public void execute(IDfSessionManager sessionmgr, String docbase,
            String mdtapp, IDfSysObject formobj, List attachments,
            IDfWorkitem workitem, Map methodparameters, Map context) 
    {
        IDfSession session = null;
        try {
            /*-dbg-*/Lg.wrn("get scheduled task name");
            Map config = (Map)context;
            String taskname = (String)config.get("TaskName");
            String aclname = (String)config.get("ACL");
            String jobobjecttype = (String)config.get("JobObjectType");
            String relationtype = (String)config.get("FormRelationType");
            String releaseimmediateflag = (String)config.get("ReleaseImmediatelyAttr");
            String releasedaysattr = (String)config.get("ReleaseDaysAttr");
            /*-dbg-*/Lg.wrn("schedule %s",taskname);
            session = sessionmgr.getSession(docbase);
            // now schedule the effectivity/obsolete/whatever promote...
            /*-dbg-*/Lg.wrn("EFFECTIVITY - get promote date");
            if (formobj.hasAttr(releaseimmediateflag)) {
                if (formobj.getBoolean(releaseimmediateflag)) {
                    Date d = new Date();
                    d.setMinutes(d.getMinutes()+2);
                    IDfTime dt = new DfTime(d);
                    createJobMonitorObject(session,formobj,dt,mdtapp,taskname,aclname,jobobjecttype,relationtype);
                } else {
                    int daysfromnow = formobj.getInt(releasedaysattr);
                    Date now = new Date();
                    now.setDate(now.getDate()+daysfromnow);
                    IDfTime dt = new DfTime(now);
                    createJobMonitorObject(session,formobj,dt,mdtapp,taskname,aclname,jobobjecttype,relationtype);
                }
            }
            // proactive release
            sessionmgr.release(session); session = null;
        } catch (Exception dfe) {
            /*-ERROR-*/Lg.err("Error occurred in scheduling of effectivity promotion",dfe);
            throw EEx.create("ScheduleEffectivePromote","Error occurred in scheduling of effectivity promotion",dfe);
        } finally {
            /*-dbg-*/Lg.wrn("releasing session");
            try {if (session!=null)sessionmgr.release(session);} catch(Exception e) {Lg.wrn("Unable to release session",e);}
            /*-dbg-*/Lg.wrn("session released");
        }
        
    }
    
    public static void createJobMonitorObject(IDfSession session, IDfSysObject docObject, IDfTime dt, String mdtapp, String taskname, String aclname,String jobobjecttype,String relationtype)
    {
        try { 
            /*-dbg-*/Lg.dbg("SchedulePromote promote time %s",dt.toString());
            String nextState = docObject.getNextStateName();
            /*-dbg-*/Lg.dbg("state to promote to: %s",nextState);
            
            /*-dbg-*/Lg.dbg("create new job task");
            IDfSysObject so = (IDfSysObject)session.newObject(jobobjecttype);
            /*-dbg-*/Lg.dbg("calc name");
            String jobname = taskname+"-"+docObject.getObjectName()+"-"+dt.toString();
            /*-dbg-*/Lg.dbg("name: %s",jobname);
            so.setObjectName(jobname);
            so.setString("m_application",mdtapp);
            so.setRepeatingString("m_arguments", 0, docObject.getObjectId().getId());
            so.setRepeatingString("m_arguments", 1, relationtype);
            so.setRepeatingString("m_arguments", 2, nextState);
            so.setTime("m_exec_date",dt);
            so.setString("m_taskname", taskname);  
            so.setString("m_exec_status", "waiting");
            so.setBoolean("m_repeat",false);
            /*-dbg-*/Lg.dbg("set acl");
            IDfACL pSet = session.getACL(session.getServerConfig().getString("operator_name"), aclname);
            so.setACL(pSet);            
            /*-dbg-*/Lg.dbg("saving...");
            so.save();
            /*-dbg-*/Lg.dbg("saved %s",so);
        } catch (DfException dfe){
            /*-ERROR-*/Lg.err("-------startScheduledPromote Exception---------",dfe);
            throw EEx.create("ScheduledJobCreateError","Error occurred in creation of effectivity promotion scheduling",dfe);
        }
    }
    

}
