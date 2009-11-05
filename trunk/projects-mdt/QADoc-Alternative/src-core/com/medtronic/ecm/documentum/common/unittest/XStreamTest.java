package com.medtronic.ecm.documentum.common.unittest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.thoughtworks.xstream.XStream;

public class XStreamTest 
{
	
	public static void main(String[] args) throws Exception
	{
		XStream xs = new XStream();
		xs.alias("Plugin", MdtPlugin.class);

    	//Map docconfig = (Map)((Map)applicationconfiguration).get("DocumentTypes");
    	//Map typeconfig = (Map)docconfig.get(doctype);
    	//List plugins = (List)typeconfig.get("NamingPlugins");

		Map map = new HashMap();
		Map doctypes = new HashMap();
		Map typeconfig = new HashMap();
		List plugins = new ArrayList();
		MdtPlugin p1 = new MdtPlugin();
		p1.classname = com.medtronic.ecm.documentum.core.plugins.naming.MdtUserSpecifiedNaming.class.getName();
		MdtPlugin p2 = new MdtPlugin();
		p2.classname = com.medtronic.ecm.documentum.core.plugins.naming.MdtConstantNaming.class.getName();
		Map p2context = new HashMap();
		p2context.put("Name"," -gendoc");
		p2.context = p2context;
		plugins.add(p1);
		plugins.add(p2);
		typeconfig.put("NamingPlugins", plugins);
		doctypes.put("cem_basedocument", typeconfig);
		map.put("DocumentTypes", doctypes);
		
		String output = xs.toXML(map);
		
		Map fromxml = (Map)xs.fromXML(output);
		
		int i=1;
		i++;
		

	}

}
