package org.webgen.dataprovider;

import groovy.lang.Binding;

import java.util.List;
import java.util.Map;

public class Static 
{
	public List getDataList(Binding grvscript, Object bean, Map beandef, Map config)
	{
		return (List)config.get("values");
	}

}
