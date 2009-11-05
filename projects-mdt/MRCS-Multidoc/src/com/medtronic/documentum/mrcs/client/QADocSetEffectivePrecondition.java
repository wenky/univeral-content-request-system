package com.medtronic.documentum.mrcs.client;

import java.util.List;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.config.MrcsPreconditionsFactory;

public class QADocSetEffectivePrecondition implements IActionPrecondition
{
	// essentially, this is a state-based precondition - may generalize eventually...
    public String[] getRequiredParams()
    {
        return (new String[] {
            "objectId"
        });
    }
	
    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component) 
    {
    	/*-CONFIG-*/String m="queryExecute ~ ";
    	try { 
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~top~ ", null, null);
	        String id = argumentlist.get("objectId");
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~Doc Id " + id, null, null);
	        IDfDocument doc = (IDfDocument) component.getDfSession().getObject(new DfId(id));
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~get current state", null, null);
	        String state = doc.getCurrentStateName();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~get MRCS app", null, null);
            String appName = doc.getString("mrcs_application");
	        MrcsPreconditionsFactory config = MrcsPreconditionsFactory.getPreConditionsConfig();
	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~lookup mrcsapp: "+appName+" state: "+state, null, null);
	        MrcsPlugin seteffplugin = config.getPreconditionPlugin(appName,"QADocSetEffectivePrecondition");
	        List validstates = (List)seteffplugin.PluginConfiguration.get("ValidStates");
	        if (validstates.contains(state)) {
	        	return true;
	        }
    	} catch (DfException dfe) {
            /*-ERROR-*/DfLogger.error(this,m+"- Exception :" + dfe, null, dfe);
    	} catch (NullPointerException npe) {
	        /*-ERROR-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" ~~SetEffective CONFIG ERROR, feature not enabled", null, null);    		
    	}
        return false;
    }
	

}
