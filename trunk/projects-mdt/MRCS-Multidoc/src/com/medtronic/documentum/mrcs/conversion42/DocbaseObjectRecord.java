package com.medtronic.documentum.mrcs.conversion42;

import java.util.List;
import java.util.Map;

public class DocbaseObjectRecord 
{
	public String objectid; // docbase object id
	public String chronid; // docbase object id
	public String name;     // note for logging or simple config
	public String path;     // note for logging or simple config
	public String type;     // note for logging or simple config
	public String mrcstype;     // note for logging or simple config
	public String config;     // note for logging or simple config
	public String lifecycle;
	public String state;     // note for logging or simple config
	public String contentsize;     // note for logging or simple config
	public String format;     // note for logging or simple config
	public String version;
	public String note;      // note for logging or simple config
	public String apicmd1;
	public String apicmd2;
	public String apicmd3;	
	public List list;
	public Map map;  // in case we need more complex notation/configuration  
	public Throwable error;
}
