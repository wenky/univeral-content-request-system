package com.medtronic.ecm.documentum.util;

import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.IDfPersistentObject;

// since base sysobjects often don't provide iterable collections for Velocity, this is a simple class 
// to add to velocity contexts

public class VeloUtils 
{
    public List getRepeatingAttributeValues(IDfPersistentObject sysobj, String attrname) throws Exception
    {
        int valuecount = sysobj.getValueCount(attrname);
        List valuelist = new ArrayList(valuecount);
        for (int i=0; i < valuecount; i++)
        {
            valuelist.add(sysobj.getRepeatingValue(attrname, i));
        }
        return valuelist;
    }
    
        
}
