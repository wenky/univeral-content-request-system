package com.uhg.ewp.common.gotcha.contentsource.data.impl;

import java.util.ArrayList;
import java.util.List;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;

public class BaseContentMetaData implements ContentMetaData
{
    
    protected boolean repeating = false;
    protected String  name = null;
    protected Class type = null;
    protected Object singleValue = null;
    protected List repeatingValues = null;
    
    
    public Object getValue()
    {
        if (repeating == false) {
            return singleValue;
        } else {
            return repeatingValues;
        }
    }
    
    public void setSingleValue(Object value) 
    {
        singleValue = value;
        repeatingValues = null;
        repeating = false;
    }
    
    public List getRepeatingValues()
    {
        return repeatingValues;
    }
    
    public void setRepeatingValues(List values)
    {
        singleValue = null;
        repeatingValues = values;
        repeating = true;
    }
    
    public boolean isRepeating()
    {
        return repeating;
    }
    public void setRepeating(boolean repeating)
    {
        this.repeating = repeating;
    }
    public String getName()
    {
        return name;
    }
    public void setName(String name)
    {
        this.name = name;
    }
    public Class getType()
    {
        return type;
    }
    public void setType(Class type)
    {
        this.type = type;
    }
    
}
