package com.uhg.ovations.portal.partd.simplewebservicetest;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;


public class InitCollectionUtils 
{
	
	public static Map createMap(Object... alternatingkeyvalues)
	{
		Map map = new HashMap();
		return addToMap(map,alternatingkeyvalues);
	}
	
	public static Map addToMap(Map map, Object... alternatingkeyvalues)
	{
		if (map != null)
			if (alternatingkeyvalues != null) 
				for (int i=0; i < alternatingkeyvalues.length/2; i++) {
					Object key = alternatingkeyvalues[i*2];
					Object value = alternatingkeyvalues[i*2+1];
					map.put(key, value);
				}
		return map;
	}
	
	public static Set createSet(Object... members)
	{
		Set set = new HashSet();
		return addToSet(set,members);
	}
	
	public static Set addToSet(Set set, Object... members)
	{
		if (set != null)
			if (members != null) 
				for (Object o : members)
					set.add(o);
		return set;
	}

	public static List createList(Object... members)
	{
		List list = new ArrayList();
		return addToList(list,members);
	}
	
	public static List addToList(List list, Object... members)
	{
		if (list != null)
			if (members != null) 
				for (Object o : members)
					list.add(o);
		return list;
	}

}
