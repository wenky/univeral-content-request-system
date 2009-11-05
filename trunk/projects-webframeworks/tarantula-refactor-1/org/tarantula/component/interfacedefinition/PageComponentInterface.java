package org.tarantula.component.interfacedefinition;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

public interface PageComponentInterface 
{
	public void init() throws Exception;	
	public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception;
}
