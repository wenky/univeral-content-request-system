package com.liteserv.dctm.objectlogger;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfId;
import com.liteserv.plugindefs.ILSObjectLogger;

public class LSDctmSessionLogger  implements ILSObjectLogger 
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
        try { 
            // TODO: use context to get a session and lookup the object's details
            IDfSession session = (IDfSession)object;
            String argvalue = "[sessionid: " + session.getSessionId() + " - user: " + session.getLoginUserName()+ " - docbase: " + session.getDocbaseName()+"]";
            
            return argvalue;
        } catch (Exception e) {
            return "[Error in DfSession Expansion]";
        }
    }

    public Class getMatchingClass() {
        return IDfSession.class;
    }

    

}
