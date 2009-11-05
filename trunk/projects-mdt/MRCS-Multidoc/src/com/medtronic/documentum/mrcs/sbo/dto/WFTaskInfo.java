/*
 * Created on Feb 27, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: WFTaskInfo.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:01 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo.dto;


import java.util.List;

import com.medtronic.documentum.mrcs.config.MrcsPlugin;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class WFTaskInfo {

    public String Name;
    public String action;
    //public List TaskExecutionInfo;
    public MrcsPlugin TaskExecutionInfo;
    public List Validations;
    public List Analysis;
    public boolean AllowMultipleForwardPaths;

    /**
     * 
     */
    public WFTaskInfo() {
        super();
        // TODO Auto-generated constructor stub
    }

    /**
     * @return Returns the action.
     */
    public String getAction() {
        return action;
    }
    /**
     * @param action The action to set.
     */
    public void setAction(String action) {
        this.action = action;
    }
    /**
     * @return Returns the name.
     */
    public String getName() {
        return Name;
    }
    /**
     * @param name The name to set.
     */
    public void setName(String name) {
        Name = name;
    }
    
    /**
     * @return Returns the mrcsPlugin.
     
    public List getMrcsPlugin() {
        return TaskExecutionInfo;
    }
    /**
     * @param mrcsPlugin The mrcsPlugin to set.
     
    public void setMrcsPlugin(List mrcsPlugin) {
        TaskExecutionInfo = mrcsPlugin;
    }
    */
    
    /**
     * @return Returns the mrcsPlugin.
     */
    public MrcsPlugin getMrcsPlugin() {
        return TaskExecutionInfo;
    }
    /**
     * @param mrcsPlugin The mrcsPlugin to set.
    */ 
    public void setMrcsPlugin(MrcsPlugin mrcsPlugin) {
        TaskExecutionInfo = mrcsPlugin;
    }
    
    
}
