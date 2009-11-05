package com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.wftresolver;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.documentum.mdtworkflow.webtop.plugins.IMdtWorkflowTemplateResolver;

public class MdtBooleanAttributes implements IMdtWorkflowTemplateResolver
{

    public String resolveWFT(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj, Map context) throws DfException 
    {
        Map config = (Map)context;
        List boolattrs = (List)config.get("BooleanAttributes");
        String result = null;
        for (int i=0; i < boolattrs.size(); i++)
        {
            String attrname = (String)boolattrs.get(i);
            boolean b = formobj.getBoolean(attrname);
            result += b;
        }        
        return (String)config.get(result);        
    }

}
