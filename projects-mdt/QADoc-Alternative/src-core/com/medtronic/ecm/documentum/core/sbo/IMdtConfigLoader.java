package com.medtronic.ecm.documentum.core.sbo;

import java.util.Map;

import com.documentum.fc.client.IDfService;

/**
 * TODO: Add description 
 * 
 * @author $Author: dentrf1 $
 * @version $Revision: 1.4 $
 * 
 */ 
public interface IMdtConfigLoader extends IDfService {
	
	/**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param docbase <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param application <font color="#0000FF"><b>(String)</b></font> TODO:
    * @param configfile <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>Map</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
	public Map loadConfig(String docbase, String application, String configfile);
}
