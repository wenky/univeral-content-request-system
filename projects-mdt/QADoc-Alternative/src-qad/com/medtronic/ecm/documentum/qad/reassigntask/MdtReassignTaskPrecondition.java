package com.medtronic.ecm.documentum.qad.reassigntask;

import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

/**
 * Enable Reassign task menu item to users in particular group
 * @author molakr1
 *
 */
public class MdtReassignTaskPrecondition implements IActionPrecondition{
    /**
     * 
     */
	public String[] getRequiredParams()    {
    	return null;
    }
	/**
	 * Check if the user can able to Reassign Tasks
	 * Make sure user in TaskRassignmentAdmin groups from config file. 
	 * @param 
	 */
    public boolean queryExecute(String strAction, IConfigElement config, ArgumentList arg, Context context, Component component)  {
        boolean bExecute = true;
		try {
			MdtReassignReadConfigValues readConfig = new MdtReassignReadConfigValues();
			//Check the user in config taskRassignmentAdmin Group 
			bExecute = readConfig.isUserExistsInTaskReassignmentAdminGroup(component.getDfSession());
		}catch(DfException e) {
			//TO DO
		}
        return bExecute;
    }    
}
