package com.liteserv.dctm.objectlogger;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.liteserv.plugindefs.ILSObjectLogger;

public class LSDctmObjectLogger  implements ILSObjectLogger 
{
    /**
     * TODO: ADD DESCRIPTION     
     *  
     * @param dfcobj <font color="#0000FF"><b>(IDfTypedObject)</b></font> TODO: 
     * 
     * @return <font color="#0000FF"><b>String</b></font> - TODO:
     */     
    public String replaceObject(Object object, Map context) 
    {
        IDfTypedObject dfcobj = (IDfTypedObject) object;
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

    public Class getMatchingClass() {
        return IDfTypedObject.class;
    }

    

}
