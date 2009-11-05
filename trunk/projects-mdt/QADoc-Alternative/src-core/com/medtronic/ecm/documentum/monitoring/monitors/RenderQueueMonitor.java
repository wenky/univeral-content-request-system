package com.medtronic.ecm.documentum.monitoring.monitors;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.monitoring.IMonitor;
import com.medtronic.ecm.documentum.monitoring.MdtMonitor;

public class RenderQueueMonitor extends TimerTask implements IMonitor 
{    
    IDfSessionManager configmonitormanager = null;
    String docbase = null;
    String user = null;
    String pass = null;
    IDfClientX clientx = null;
    IDfClient client = null;
    MdtMonitor monitorserver = null;
    Map config = null;
    public static Boolean running = new Boolean(false); 
    
    public static final String pingmethod = "MdtPing";
    
    public void init(Map configref, MdtMonitor monitorserverref) 
    {
        monitorserver = monitorserverref;
        config = configref;
        user = (String)config.get("SysUser");
        pass = (String)config.get("SysPass");
        docbase = (String)config.get("Docbase");
        
        /*-dbg-*/if(Lg.dbg())Lg.dbg("instantiate clientx");
        clientx = new DfClientX();
        try { 
            /*-dbg-*/if(Lg.dbg())Lg.dbg("get local client");
            client = clientx.getLocalClient();
            /*-dbg-*/if(Lg.dbg())Lg.dbg("create new session manager");
            configmonitormanager = client.newSessionManager();
            IDfLoginInfo logininfo = clientx.getLoginInfo();
            logininfo.setUser(user);
            logininfo.setPassword(pass);
            logininfo.setDomain(null);
            /*-dbg-*/if(Lg.dbg())Lg.dbg("set smgr identity");
            configmonitormanager.setIdentity(docbase, logininfo);
            /*-dbg-*/if(Lg.dbg())Lg.dbg("done with ConfigMonitorTask instantiation");
            running = true;
        } catch (DfException dfe) {
            throw EEx.create("ConfigMonitor-new", "ConfigMonitorTask was unable to create its own sessionmanager",dfe);
        }        
    }
    
    public void run()
    {
        /*-INFO-*/Lg.inf("run() invoked for render queue monitor");
        boolean loginrefresh = false;
        try {
            IDfSession session = null;
            try {
                /*-dbg-*/if(Lg.dbg())Lg.dbg("get session");
                try {
                    session = configmonitormanager.getSession(docbase);
                } catch (DfIdentityException dfie) {
                    loginrefresh = true;
                    // logininfo has "expired" - lets refresh the session manager
                    /*-WARN-*/Lg.wrn("login info has timed out - refreshing session manager");
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("create new session manager");
                    IDfSessionManager configmonitormanager = client.newSessionManager();
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("create login info object");
                    IDfLoginInfo newlogininfo = clientx.getLoginInfo();
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("set user/pass/domain");
                    newlogininfo.setUser(user);
                    newlogininfo.setPassword(pass);
                    newlogininfo.setDomain(null);
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("set session manager to newly created logininfo identity");
                    configmonitormanager.setIdentity(docbase, newlogininfo);
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("try to get a session again");
                    session = configmonitormanager.getSession(docbase);                        
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("getsession reattempt succeeded");
                }

                /*-dbg-*/if(Lg.dbg())Lg.dbg("check status of render queue");
                
                // check number of items in queue
                
                // select count
                String dql = "select count(*) as queuecount from dmi_queue_item where name = 'dm_autorender_win31' and dequeued_date is NULLDATE";
                IDfCollection queuecount = null;
                IDfQuery qry = new DfQuery(dql);
                int count = -1;
                try { 
                    queuecount = qry.execute(session, IDfQuery.DF_READ_QUERY);
                    count = queuecount.getInt("queuecount");
                } finally {
                    try {queuecount.close();}catch(Exception e){}
                }
                
                String countconfig = (String)config.get("QueueCountThreshold");
                int countthreshold = Integer.parseInt(countconfig);
                boolean countflag = count > countthreshold;
                
                // compute duration between date_sent and current date
                long datediff = -1;
                dql = "select MIN(date_sent) AS mindate from dmi_queue_item where name = 'dm_autorender_win31' and dequeued_date is NULLDATE";
                IDfCollection c = null;
                try { 
                    c = qry.execute(session, IDfQuery.DF_READ_QUERY);
                    IDfTime datesent = c.getTime("mindate");
                    Date queuedate = datesent.getDate();
                    Date currentdate = new Date();
                    datediff = currentdate.getTime() - queuedate.getTime();
                } finally {
                    try {c.close();}catch(Exception e){}
                }
                
                String delayconfigvalue = (String)config.get("MillisecondDelayThreshold");
                long delaythreshold = Long.parseLong(delayconfigvalue);
                boolean delayflag = datediff > delaythreshold;
                
                if (countflag || delayflag) {
                    // add session to context
                    Map context = new HashMap();
                    context.put("IDfSession",session); 
                    // queue an event on an object
                    List notifiers = (List)config.get("Notifiers");
                    monitorserver.notify(this, notifiers, context);
                }

            } catch (Exception e) {
                /*-ERROR-*/Lg.err("error in method server monitor, [loginrefreshed? %b]",loginrefresh,e);
            } finally {
                /*-dbg-*/Lg.dbg("release session in finally clause...");
                try {if (session != null)configmonitormanager.release(session);} catch (Exception ee) {Lg.wrn("session could not be released in catch");}
            }
        } catch (Throwable t) {
            /*-WARN-*/Lg.wrn("error in method server monitor, [loginrefreshed? %b]",loginrefresh,t);
        }
    }
    
    

}
