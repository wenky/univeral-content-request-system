package com.medtronic.ecm.documentum.monitoring;

import java.util.Map;

public interface IMonitor 
{
    public void init(Map configref, MdtMonitor monitorserverref);
}
