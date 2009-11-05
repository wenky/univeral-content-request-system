/*
 * Created on Apr 19, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.client;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.webcomponent.library.locator.SysObjectLocatorQuery;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public abstract class MrcsCustomWorkflowTemplateLocatorQuery extends SysObjectLocatorQuery
{
    public abstract boolean queryInit(IDfSession session, String mrcsapp, String[] val, Map configdata) throws Exception;
    
    // also must override SysObjectLocatorQuery.getNonContainerStatement
    
}
