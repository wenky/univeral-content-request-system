package com.medtronic.ecm.documentum.monitoring.notifiers;

import java.util.Date;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfTime;
import com.medtronic.ecm.documentum.monitoring.IMonitor;
import com.medtronic.ecm.documentum.monitoring.INotifier;

public class DctmQueueEventOnObject implements INotifier 
{

    public void exec(IMonitor monitor, Map notifierconfig, Map context, Map globalconfig) throws Exception
    {
        IDfSession session = (IDfSession)context.get("IDfSession");
        String qualification = (String)notifierconfig.get("QueueObjectQualification"); // because you at least need an object to queue on...why?
        List userlist = (List)notifierconfig.get("UserList");
        IDfSysObject so = (IDfSysObject)session.getObjectByQualification(qualification);
        String event = (String)notifierconfig.get("Event");
        String message = (String)notifierconfig.get("Message");
        IDfTime t = new DfTime(new Date());
        for (int i=0; i < userlist.size(); i++) {
            String user = (String)userlist.get(i);
            so.queue(user, event, 1,true,t,message);
        }
    }

}
