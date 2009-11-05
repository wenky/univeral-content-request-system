package com.medtronic.ecm.documentum.monitoring.monitors;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimerTask;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfIdentityException;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.monitoring.IMonitor;
import com.medtronic.ecm.documentum.monitoring.MdtMonitor;

public class MethodServerMonitor extends TimerTask implements IMonitor 
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
    
    public static final String pingmethod = "QADocPing";
    
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
        /*-INFO-*/Lg.inf("run() invoked for method server monitor");
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

                /*-dbg-*/if(Lg.dbg())Lg.dbg("invoke ping method");
                /*-dbg-*/Lg.wrn("compose MdtPing api call for scs cfg doc %s");
                
                IDfList args = new DfList(); 
                IDfList argsTypes = new DfList(); 
                IDfList argsValues = new DfList();
                
                String callmethodapiargs =  "-docbase_name "+session.getDocbaseName();

                args.appendString("APP_SERVER_NAME"); 
                argsTypes.appendString("S");
                argsValues.append("WebCache");

                args.appendString("TIME_OUT"); 
                argsTypes.appendString("I");
                argsValues.append("120");

                args.appendString("SAVE_RESPONSE"); 
                argsTypes.appendString("I");
                argsValues.append("1");

                args.appendString("LAUNCH_SYNC"); 
                argsTypes.appendString("B");
                argsValues.append("T");

                /*-dbg-*/Lg.wrn("meth args: %s",callmethodapiargs);            
                args.appendString("ARGUMENTS"); 
                argsTypes.appendString("S");
                argsValues.append(callmethodapiargs);

                boolean success = true;
                /*-dbg-*/Lg.wrn("  -- exec Ping Method");            
                
                StringBuffer errreport = new StringBuffer();
                try { 
                    // well, since apiExec always returns true, apparently exec failure is communicated via DfException
                    IDfCollection returnval = session.apply(null, "HTTP_POST", args, argsTypes, argsValues);
                    try { 
                        
                        while(returnval.next()) {
                            int count = returnval.getAttrCount();
                            for (int i=0; i < count; i++) {
                                String name = returnval.getAttr(i).getName();
                                String value = returnval.getValueAt(i).asString();
                                /*-dbg-*/Lg.wrn(" ping return: ["+name+"]["+value+"]");
                                errreport.append(" ping return: ["+name+"]["+value+"]").append('\n');
                            }
                        }
                        
                    } catch (DfException returncode) {
                        /*-dbg-*/Lg.wrn("WARN: error in examining method call return values");            
                    } finally {
                        try { returnval.close();}catch (Exception e) {}
                    }
                } catch (DfException returncode) {
                    /*-dbg-*/Lg.wrn("API EXEC FAILED");            
                    success = false;
                }
                
                if (!success) {
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
