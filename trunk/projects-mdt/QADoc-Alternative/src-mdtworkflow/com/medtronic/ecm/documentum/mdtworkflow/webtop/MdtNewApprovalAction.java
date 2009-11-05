package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import java.util.Iterator;
import java.util.Map;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.IActionExecution;
import com.documentum.web.formext.action.IActionPrecondition;
import com.documentum.web.formext.action.LaunchComponent;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;
import com.medtronic.ecm.common.Lg;

public class MdtNewApprovalAction extends LaunchComponent implements IActionExecution, IActionPrecondition
{
    public boolean queryExecute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component) 
    {
        return true;
    }
    public boolean execute(String s, IConfigElement iconfigelement, ArgumentList argumentlist, Context context, Component component, Map map) 
    {
        /*-INFO-*/Lg.inf("Top - CALL NewDocument.onInit()");
        Iterator i = argumentlist.nameIterator(); 
        /*-dbg-*/if(Lg.dbg())while (i.hasNext()) { String argname = (String)i.next();Lg.dbg(argname + " : " + argumentlist.get(argname));}
        argumentlist.add("objectIds", "multi");
        
        return super.execute(s,iconfigelement,argumentlist,context,component,map);
        
    }


}
