package com.nighteclipse.zoder.processors;

import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.nighteclipse.zoder.util.DOMUtils;
import com.nighteclipse.zoder.util.ZuluDateFormatter;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;
import com.zoder.util.DctmUtils;

public class MMMStateInfoProcessor extends AbstractVersionProcessor
{

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            getStateInfo(version,so,context);
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
    }
    
    void getStateInfo(Map version, IDfSysObject so, Context context) throws Exception
    {
        Element curobj = (Element)version.get("DOM");
        Node stateListElem = DOMUtils.getmake(curobj,"stateList");
        
        //stateList.put("state", state);
        try {
            Node stateElem = DOMUtils.newElement(stateListElem, "state");
            
            Map statemapping = (Map)context.get("StateMappings");
            DOMUtils.put(stateElem, "name",translate(so.getString("a_status"),statemapping));
            if (DctmUtils.isCurrent(so)) {
                Node curElem = DOMUtils.newElement(stateElem, "current");
                stateElem.appendChild(curElem);
            }
            DOMUtils.put(stateElem, "stateStatus", "enabled");
            // TODO: try to get "actualInfo" --> promotion date? lc attach? creation date? last modified?
            Node actualInfoElem = DOMUtils.newElement(stateElem,"actualInfo");
            stateElem.appendChild(actualInfoElem);
            DOMUtils.put(actualInfoElem, "datetime",ZuluDateFormatter.formatDate(so.getLockDate().getDate()));
            if (DctmUtils.isCurrent(so)) {
                stateElem.appendChild(DOMUtils.newElement(stateElem, "versionable"));
                stateElem.appendChild(DOMUtils.newElement(stateElem, "revisionable"));
            }
        }catch (NullPointerException npe) {}
    }

    public static String translate(String value, Map table) throws Exception
    {        
        // attempt a value lookup table translation
        if (table.containsKey(value))
        {
            return (String)table.get(value);
        }
        // not found? return original value (? or throw exception?)
        return value;        
    }


}
