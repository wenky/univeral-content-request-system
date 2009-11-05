/*
 * Created on Apr 3, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.plugin;

import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.services.workflow.inbox.ITask;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public interface  MrcsCustomSignatureRedirect
{
    public String getRedirect(IDfSession session, String mrcsapp, ITask workflowtask, String action, Map config);
}
