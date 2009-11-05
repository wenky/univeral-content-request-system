package com.medtronic.ecm.documentum.monitoring;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

// initiates monitor tasks, and emails/notifies 

public class MdtMonitor 
{
    static Timer timer = new Timer();    
    public List monitors = new ArrayList();
    public Map config;
    
    public void init(Map configref) throws Exception
    {
        config = configref;
        List monitordefs = (List)config.get("Monitors");
        
        // start timer tasks
        for (int i=0; i < monitordefs.size(); i++)
        {
            Map monitordef = (Map)monitordefs.get(i);
            String clazz = (String)monitordef.get("Class");
            Integer interval = (Integer)monitordef.get("Interval");
            IMonitor monitor = (IMonitor)Class.forName(clazz).newInstance();
            monitor.init(config, this);
            monitors.add(monitor);
            timer.schedule((TimerTask)monitor,interval);            
        }
    }
    
    public void notify(IMonitor monitor, List notifiers, Map context) throws Exception
    {
        for (int i=0; i < notifiers.size(); i++) {
            Map notifiercfg = (Map)notifiers.get(i);
            String clazz = (String)notifiercfg.get("Class");                     
            INotifier notifier = (INotifier)Class.forName(clazz).newInstance();
            notifier.exec(monitor, notifiercfg,context,config);
        }
        
    }

}
