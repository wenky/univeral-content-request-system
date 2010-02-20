package com.uhg.ewp.common.gotcha.util.log.idef;

import java.util.Map;

public interface ObjectLogger 
{
    public Class getMatchingClass();    
    public String replaceObject(Object object, Map context);
}
