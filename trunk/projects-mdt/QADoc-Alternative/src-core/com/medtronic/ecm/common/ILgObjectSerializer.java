package com.medtronic.ecm.common;

import java.util.ResourceBundle;

public interface ILgObjectSerializer 
{
    public Class getMatchingClass();    
    public String replaceObject(Object object);
    public void setContext(ResourceBundle ctx);
}
