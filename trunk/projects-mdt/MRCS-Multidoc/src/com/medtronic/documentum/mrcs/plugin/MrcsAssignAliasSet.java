/*
 * Created on Apr 10, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfAliasSet;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.medtronic.documentum.mrcs.config.StartWorkflowConfigFactory;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

/*
   <MrcsPlugin>
        <PluginClassName>com.medtronic.documentum.mrcs.plugin.AssignAliasSet</PluginClassName>
        <PluginConfiguration>
            <entry><string>Property</string><string>Doctype</string></entry>
            <entry><string>Value1</string><string>manufacturing</string></entry>
            <entry><string>Set1</string><string>QADoc_ManufacturingApprovalAliasSet</string></entry>
            <entry><string>Value2</string><string>clinical</string></entry>
            <entry><string>Set2</string><string>QADoc_ClinicalApprovalAliasSet</string></entry>
        </PluginConfiguration>
  </MrcsPlugin>
*/

public class MrcsAssignAliasSet extends MrcsBasePlugin implements IMrcsWorkflowValidation

{
    public boolean validate(IDfSessionManager sMgr, String docbase, IDfDocument doc, String mrcsapp, String wfname, List errmsgs, Map configdata, Map customdata) throws Exception
    {
        /*-CONFIG-*/String m="validate-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Top of Assign Alias Set plugin", null, null);
        // get id of doc (since we're going to look it up via the system user
        String docid = doc.getObjectId().getId();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"docid of doc to set alias on: "+docid, null, null);
        // change to system user
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"switching to system user", null, null);
        StartWorkflowConfigFactory config = StartWorkflowConfigFactory.getWorkflowConfig();
        String syspass = config.getSystemPassword(mrcsapp);
        String sysuser = config.getSystemUsername(mrcsapp);
        IDfSessionManager syssessmgr = createSessionManager(docbase,sysuser,syspass);
        IDfSession syssession = syssessmgr.getSession(docbase);
        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"re-retrieving document with system rights", null, null);
        IDfDocument sysdoc = (IDfDocument)syssession.getObject(new DfId(docid));
        
        // get property to analyze
        String attrname = (String) configdata.get("Property");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Property to analyze: "+attrname, null, null);
        // get property value
        String propvalue = sysdoc.getString(attrname);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Property value: "+propvalue, null, null);
        // compare with config'd alias mappings
        int i = 1;
        boolean found = false;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"scanning config for value match", null, null);
        while (true)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checking next plugin config key", null, null);
            if (configdata.containsKey("Value"+i))
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"looking up Value"+i, null, null);
                String value = (String)configdata.get("Value"+i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"value: "+value, null, null);
                String aliassetname = (String)configdata.get("Set"+i);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"assigned alias set for value: "+aliassetname, null, null);
                if (value.equals(propvalue))
                {
                    // should try to speed the qualification by restricting search to alias sets. How?
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locating alias set", null, null);
                    IDfAliasSet aliasset = (IDfAliasSet)syssession.getObjectByQualification("dm_alias_set where object_name = '"+aliassetname+"'");
                    if (aliasset != null)
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"alias set found", null, null);
                        found = true;
                        IDfId objid = aliasset.getObjectId();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"alias set id: "+objid.getId(), null, null);
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"assigning alias set id to r_alias_set_id", null, null);
                        sysdoc.setId("r_alias_set_id",objid);
                        sysdoc.save();
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"assigned and saved", null, null);
                        break;
                    }
                    else 
                    {
                        throw new RuntimeException("Unable to locate aliaset "+aliassetname+" in docbase");
                    }
                }
                i++;
            }
            else
            {
                throw new RuntimeException("no aliasset association found for property "+attrname+" value "+propvalue+" in configuration");
            }
        }
        
        return found;
        
    }
}
