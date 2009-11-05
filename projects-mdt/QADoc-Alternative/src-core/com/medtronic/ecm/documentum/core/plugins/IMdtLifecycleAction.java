package com.medtronic.ecm.documentum.core.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;

/**
 * TODO: Add description 
 * 
 * @author $Author: dms01 $
 * @version $Revision: 1.5 $
 * 
 */  
public interface IMdtLifecycleAction
{ 
	
	/**
    *
    * TODO: ADD DESCRIPTION
    * For MdtConfigurableLifecycleAction...
    * 
    * @param mdtapp <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param idfsysobject <font color="#0000FF"><b>(IDfSysObject)</b></font> TODO:
    * @param username <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param targetstate <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param config <font color="#0000FF"><b>(Object)</b></font> TODO:
    * 
    * @since 1.0
    *  
    */
	public void execute(String mdtapp, 
						IDfSysObject idfsysobject, 
						String username,
						String targetstate, 
						Map config);	

}
