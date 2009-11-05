package com.medtronic.ecm.documentum.core.sbo;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfService;

/**
 * TODO: Add description 
 * 
 * @author $Author: dentrf1 $
 * @version $Revision: 1.4 $
 * 
 */ 
public interface IMdtConfigService extends IDfService 
{

	/**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param appname <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>List</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
	public List getTypesForApplication(String appname);

	/**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param type <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>String</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
	public String getMdtApplicationName(String type);

	/**
    *
    * TODO: ADD DESCRIPTION
    * 
    * @param application <font color="#0000FF"><b>(String)</b></font> TODO:
    * @return <font color="#0000FF"><b>Object</b></font> - TODO:
    * 
    * @since 1.0
    *  
    */
	public Map getAppConfig(String application);
	
}