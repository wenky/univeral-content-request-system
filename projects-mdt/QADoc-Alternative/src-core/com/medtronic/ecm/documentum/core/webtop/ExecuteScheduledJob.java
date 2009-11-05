package com.medtronic.ecm.documentum.core.webtop;

import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.documentum.fc.common.IDfValue;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.formext.action.IActionExecution;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

public class ExecuteScheduledJob implements IActionExecution, IActionPrecondition
{

    public String[] getRequiredParams() {return (new String[] {"objectId"});}

    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component) 
    {
        String id = argumentlist.get("objectId");
        IDfSession session = component.getDfSession();
        // make sure the document is in state two/Approved? 
        try { 
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId(id));
            if (so.getCurrentState() != 2) return false;
        } catch (Exception e) {
            return false;
        }
        
        String formobjid = null;
        try {
            formobjid = getFormId(s,iconfigelement,argumentlist,context,component,null);
            if (formobjid == null) return false;
        } catch (Exception e) {
            return false;
        }

        try {
            String jobid = getJobId(s,iconfigelement,argumentlist,context,component,null,formobjid);
            if (jobid == null) return false;
        } catch (Exception e) {
            return false;
        }

        return true;        
    }
    
    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map) 
    {
        try { 
            String formobjid = getFormId(s,iconfigelement,argumentlist,context,component,map);
            
            String jobid = getJobId(s,iconfigelement,argumentlist,context,component,map,formobjid);
            
            execScheduledJob(s,iconfigelement,argumentlist,context,component,map,formobjid,jobid);
        } catch (Exception e) {
            throw new WrapperRuntimeException(e);
        }
        return true;
    }

    public String getFormId(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map) throws Exception
    {
        String id = argumentlist.get("objectId");
        IDfSession session = component.getDfSession();
        
        String dql = "select parent_id from dm_relation where relation_name = 'mdt_qad_sys_approval' and child_id = '"+id+"'";
        IDfValue formid = execSingleValueQuery(session,dql);
        
        return formid.asId().getId();
    }

    public String getJobId(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map,String formid) throws Exception
    {
        IDfSession session = component.getDfSession();
        
        // default impl: sigh, assume qadoc relation, TODO: make configurable...
        String dql = "select r_object_id from mdt_scheduled_task where m_exec_status = 'waiting' and any m_arguments = '"+formid+"'";
        IDfValue jobid = execSingleValueQuery(session,dql);
        
        return jobid.asId().getId();
    }

    
    public static IDfValue execSingleValueQuery(IDfSession session, String dql) throws Exception
    {
        IDfCollection c = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            c = query.execute(session, query.DF_READ_QUERY);
            if (c.next()) {
                IDfValue retval = c.getValueAt(0);
                return retval;
            }
        } finally {
            try {c.close();}catch(Exception e){}
        }
        return null;
    }
    
    public void execScheduledJob(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map, String formid, String jobid)throws Exception
    {
        IDfSession session = component.getDfSession();
        
        IDfSysObject so = (IDfSysObject)session.getObject(new DfId(jobid));
        
        // default impl: sigh, assume wizard/qadoc relation
        IDfTime t = new DfTime(new java.util.Date());
        so.setTime("m_exec_date", t);
        so.save(); 
        
    }


}
