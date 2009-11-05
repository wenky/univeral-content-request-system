package com.medtronic.documentum.ajax;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.medtronic.documentum.ajax.XStreamUtils.Action;
import com.medtronic.documentum.ajax.XStreamUtils.PageMapping;

public class Controller extends HttpServlet
{
	static HashMap pagemappings = new HashMap();
	static HashMap scopes = new HashMap();
	
	// ajaxify this, servlets are stupid. Have the ajax/dwr request translate/scope/resolve to the next page, pass it back, then set location on the browser.
	public void doAction(String action,Map data)
	{
		// get action def
		PageMapping mapping = (PageMapping)pagemappings.get(action);

		// determine who wins the scoping battle
		Action winner = null;
		for (int i=0; i < mapping.scopes.size(); i++)
		{
			// get next mapping
			Action actiondef = (Action)mapping.scopes.get(i);
			// check scopes to find "dominant" action mapping
			boolean match = actiondef.executeQualifier(data);
			// how do we do precedence/dominance? Last wins? Answer: First match, first win. (least computation/overhead)
			if (match) {
				winner = actiondef;
				break;
			}
		}
		
		if (winner == null) { /*send error message*/ }
		
				
	}

	
	// traditional/initial servlet requests go here.
	public void doAction(HttpServletRequest req, HttpServletResponse resp)
	{
		String action = req.getParameter("action");
		Map data = req.getParameterMap();
		doAction(action,data);
	}

	public void doGet(HttpServletRequest req, HttpServletResponse resp)
	{
		doAction(req,resp);		
	}
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		doAction(req,resp);
	}
		
}

