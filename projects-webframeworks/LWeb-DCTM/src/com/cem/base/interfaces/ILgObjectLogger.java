package com.cem.base.interfaces;

import java.util.Map;

public interface ILgObjectLogger 
{
    public Class getMatchingClass();    
    public String replaceObject(Object object, Map context);

}
