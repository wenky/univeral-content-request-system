package com.medtronic.documentum.pulse.lockitdown;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

public class PulseStartWorkflowPrecondition implements IActionPrecondition
{
    public String[] getRequiredParams() 
    {
    	return new String[0];
    }

	
    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component) 
    {
        String id = argumentlist.get("objectId");
        try { 
        	IDfSysObject doc = (IDfSysObject)component.getDfSession().getObject(new DfId(id));
        } catch (DfException dfe) {
            /*-ERROR-*/DfLogger.error(this, "Error in determining if doc "+id+" is in a workflow", null,null);
        	return false;
        }
    	
    	return true;
    }
	

    protected boolean documentNotInWF(IDfSysObject document) throws DfException {
        double numWF = 0;
        String docId = document.getObjectId().getId();
        String qualification = "select count(r_workflow_id) from dmi_package " +
        		"where any r_component_id in " +
        		"(select r_object_id from dm_sysobject where i_chronicle_id in " +
        		"(select i_chronicle_id from dm_sysobject where r_object_id = '" +docId +"'))";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "PulseStartWorkflowPrecondition.documentNotInWF: qualification : "+qualification, null,null);

        IDfQuery qry = new DfQuery();
        qry.setDQL(qualification);
        IDfCollection myObj1 = null;
        try { 
        	myObj1 = qry.execute(document.getSession(),IDfQuery.DF_READ_QUERY);
			while(myObj1.next()) {
			    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
					IDfAttr attr = myObj1.getAttr(i);
					if (attr.getDataType() == attr.DM_DOUBLE) {
					    numWF = myObj1.getDouble(attr.getName());
					    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "PulseStartWorkflowPrecondition.documentNotInWF: No of WF involved :" +numWF, null,null);
					}
	                if (attr.getDataType() == attr.DM_INTEGER) {
	                    numWF = (double)myObj1.getInt(attr.getName());
	                }
			    }
			}
        } finally {
        	try {myObj1.close();} catch (Exception e) {}
        }
		if(numWF > 0)
        {
            return false;
        }
		else return true;
    }

}
