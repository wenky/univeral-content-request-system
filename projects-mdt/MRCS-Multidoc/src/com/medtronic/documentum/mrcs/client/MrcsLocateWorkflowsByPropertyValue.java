/*
 * Created on Apr 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.client;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.web.form.query.ParsedExpression;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsLocateWorkflowsByPropertyValue extends MrcsCustomWorkflowTemplateLocatorQuery
{
    private Map config;
    private String attrvalue;
    public boolean queryInit(IDfSession session, String mrcsapp, String[] val, Map configdata) throws Exception
    {
        // store config ref
        config = configdata;
        // get the first doc's qadoc_alias_code value
        String propname = (String)config.get("Property");
        IDfSysObject doc = (IDfSysObject)session.getObject(new DfId(val[0]));
        attrvalue = doc.getString(propname);
        if (attrvalue == null || "".equals(attrvalue)) attrvalue = "Default";
        // check if we're in the appropriate state for overriding the wf selection
        String validstate = (String)config.get("State");
        if (doc.getCurrentStateName().equals(validstate))
            return true;
        else
            return false;
 
    }
    
    protected void getNonContainerStatement(StringBuffer statement, String param)
    {
        // determine
        String wfname = (String)config.get(attrvalue);
        addWhereClause("WHERE", new ParsedExpression(" object_name = '" + wfname + "'"));
        super.getNonContainerStatement(statement, param);
   }
    
}
