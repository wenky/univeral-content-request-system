package com.medtronic.ecm.documentum.common;

import java.util.Map;

/**
 * TODO: ADD DESCRIPTION
 * 
 * @author $Author: dentrf1 $
 * 
 * @version $Revision: 1.8 $
 *
 */
public class MdtPlugin 
{
	public String classname;  // this is almost always required
	public Map context;    // frequently needed, gives settings to the plugin implementation
	public String invocationtype; // SBO, POJO, DFS - for plugin layers that can mix and match types of invocation 
	public Object extendedinfo;   // stratchpad for other information...
}
