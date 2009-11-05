package com.medtronic.ecm.documentum.common;

import java.util.ResourceBundle;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.ILgObjectSerializer;

public class LgDctmObjectSerializer implements ILgObjectSerializer 
{
    ResourceBundle context;
    public void setContext(ResourceBundle ctx) {context = ctx;}
    
    public Class getMatchingClass() {
        return IDfTypedObject.class;
    }

    public String replaceObject(Object object) {
        IDfTypedObject dfcobj = (IDfTypedObject)object;
        StringBuffer buf = new StringBuffer("");
        // try to get the id
        try { buf.append("[id:"+dfcobj.getObjectId().getId()+']'); } catch (DfException dfe) {}
        // try to get the type
        try { 
            if (dfcobj instanceof IDfSysObject) 
            { 
                buf.append("[type:"+((IDfSysObject)dfcobj).getTypeName()+']');
                buf.append("[name:"+((IDfSysObject)dfcobj).getObjectName()+']');
            }
        } catch (DfException dfe) {}
        // try to get the name
        try { buf.append("[user:"+dfcobj.getSession().getLoginUserName()+']');} catch (DfException dfe) {}
        // try to get the current user
        return buf.toString();
       
    }
   
}
