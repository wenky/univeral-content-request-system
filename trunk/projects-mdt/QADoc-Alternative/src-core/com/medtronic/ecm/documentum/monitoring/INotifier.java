package com.medtronic.ecm.documentum.monitoring;

import java.util.List;
import java.util.Map;

public interface INotifier 
{
    public void exec(IMonitor monitor, Map notifierconfig, Map context, Map globalconfig) throws Exception;
}
