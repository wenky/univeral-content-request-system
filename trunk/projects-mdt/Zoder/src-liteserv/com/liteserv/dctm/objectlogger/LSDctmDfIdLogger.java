package com.liteserv.dctm.objectlogger;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.liteserv.plugindefs.ILSObjectLogger;

public class LSDctmDfIdLogger  implements ILSObjectLogger 
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
        // TODO: use context to get a session and lookup the object's details 
        IDfId dfcid = (IDfId) object;
        return dfcid.getId();
    }

    public Class getMatchingClass() {
        return IDfId.class;
    }

    

}
