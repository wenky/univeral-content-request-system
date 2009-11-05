package com.medtronic.documentum.ajax;

import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.thoughtworks.xstream.XStream;
import com.thoughtworks.xstream.converters.Converter;
import com.thoughtworks.xstream.converters.MarshallingContext;
import com.thoughtworks.xstream.converters.UnmarshallingContext;
import com.thoughtworks.xstream.io.HierarchicalStreamReader;
import com.thoughtworks.xstream.io.HierarchicalStreamWriter;
import com.thoughtworks.xstream.io.json.JettisonMappedXmlDriver;

// yes, we may be able to use generics to distinguish the Map<String,String> but I'm not sold on java generics yet and we aren't in 1.5 yet...

public class XStreamUtils {
	
	// ugly hack to distinguish general maps with our own
	public class StringMap extends HashMap 
	{
		public String get(String key) { return (String)super.get(key); } // autocast convenience method...
	}

	public class StringMapAsAttributesConverter implements Converter {

	    public boolean canConvert(Class clazz) {
	    	//Class[] ifaces = clazz.getInterfaces();
	    	//for (int i=0; i < ifaces.length; i++) {
	    	//	if (ifaces[i] == Map.class)
	    	//		return true;
	    	//}
	    	if (clazz.equals(StringMap.class)) return true;
	    	return false;
		}

	    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) 
	    {
	    	Map map = (Map)obj; // assumes all map keys/values are strings
	    	Iterator keys = map.keySet().iterator();
	    	while (keys.hasNext())
	    	{
	    		String key = (String)keys.next();
	    		String value = (String)map.get(key);
	    		writer.addAttribute(key,value);
	    	}
	    }

	    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) 
	    {
	    	StringMap map = new StringMap();
	    	// dammit, no general getAttributes(), this won't work! oh well...
	    	Iterator attrs = reader.getAttributeNames();
	    	while (attrs.hasNext()) 
	    	{
	    		String name = (String)attrs.next();
	    		String value = reader.getAttribute(name);
	    		map.put(name,value);
	    	}
	    	return map;
		}

	}

	public class StringMapAsTagsConverter implements Converter {

	    public boolean canConvert(Class clazz) {
	    	//Class[] ifaces = clazz.getInterfaces();
	    	//for (int i=0; i < ifaces.length; i++) {
	    	//	if (ifaces[i] == Map.class)
	    	//		return true;
	    	//}
	    	if (clazz.equals(StringMap.class)) return true;
	    	return false;
		}

	    public void marshal(Object obj, HierarchicalStreamWriter writer, MarshallingContext context) 
	    {
	    	Map map = (Map)obj; // assumes all map keys/values are strings
	    	Iterator keys = map.keySet().iterator();
	    	while (keys.hasNext())
	    	{
	    		String key = (String)keys.next();
	    		String value = (String)map.get(key);
	    		writer.startNode(key);
	    		writer.setValue(value);
	    		writer.endNode();
	    	}
	    }

	    public Object unmarshal(HierarchicalStreamReader reader, UnmarshallingContext context) 
	    {
	    	Map map = new StringMap();
	    	// dammit, no general getAttributes(), this won't work! oh well...
	    	while (reader.hasMoreChildren()) 
	    	{
	    		reader.moveDown();
	    		String key = reader.getNodeName();
	    		String value = reader.getValue();
	    		map.put(key,value);
	    		reader.moveUp();
	    	}
	    	return map;
		}

	}
	
	public XStream prepareXmlXStream()
	{
		XStream xst = new XStream();
		xst.alias("smap",StringMap.class);
		xst.alias("PageMapping",PageMapping.class);
		xst.alias("Action",Action.class);
		xst.registerConverter(new StringMapAsTagsConverter());
		//xst.registerConverter(new StringMapAsAttributesConverter()); 
		
		return xst;
	}
	
	public XStream prepareJSONXStream()
	{
		XStream xstream = new XStream(new JettisonMappedXmlDriver());
		xstream.alias("smap",StringMap.class);
		xstream.alias("PageMapping",PageMapping.class);
		xstream.alias("Action",Action.class);
		xstream.registerConverter(new StringMapAsTagsConverter());
		//xstream.registerConverter(new StringMapAsAttributesConverter());
		return xstream;
	}
	
	// datastruct definitions
	public class PageMapping 
	{
		List scopes; //list of actions
	}

	public class Action
	{
		StringMap  scope;       // scoping info for this
		Map definition;
		transient ScopeQualifier cachedref;
		public boolean executeQualifier(Map data)
		{
			// determine if qualifier ref has been cached (speed up so we don't need to process the scope class/type specifier each time)
			if (cachedref == null) {
				// get scope reference
				cachedref = (ScopeQualifier)Controller.scopes.get(scope.get("type"));
			}
			boolean result = cachedref.checkqualification(scope,data);
			return result;
		}
		
	}
	
	public interface ScopeQualifier
	{
		public boolean checkqualification(StringMap scope, Map data);
	}
	
	
	
	
	//-------------------------testing code-----------------------
	public static void main(String[] args)
	{
		//test
		XStreamUtils u = new XStreamUtils();
		u.test();
	}
	
	public void test() 
	{
		XStream xs = prepareXmlXStream();
		StringMap sm = new StringMap();
		sm.put("a","one");
		sm.put("b","---");
		sm.put("c","two");
		sm.put("superduper","travesty");
		
		String serd = xs.toXML(sm);
		
		StringMap fromserial = (StringMap)xs.fromXML(serd);
		
		int a=1;
		a++;
		
	}
	
}
