/*
 * Created on Jan 21, 2005
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

 Filename       $RCSfile: MrcsConfigObjects.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/06/30 22:53:23 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.config;


import java.util.List;
import java.util.Map;

import org.apache.regexp.RE;

import com.medtronic.documentum.mrcs.common.MrcsCheckInInfo;
import com.medtronic.documentum.mrcs.util.REFactory;

/**
 * @author muellc4
 *
 * This lists the data structs (methodless java classes) that are deserialized by the ConfigBroker using xstream.
 * These objects are currently scoped as package-level, so that only ConfigFactory classes in com.medtronic.documentum.mrcs.config
 * can peruse the POJO config object tree, thus preventing SBOs, TBOs, and webtop components from accessing config
 * without a ConfigFactory.
 *
 * These classes all violate usual java coding conventions in their instance var names since it makes the xstream XML look pretty
 *
 */

//data structures for config information

class MrcsRootFolder
{
    MrcsRootFolder(){}
    // since these regular expressions will be used frequently, it will
    // behoove us to reuse compiled versions. The transient specifier
    // prevents XStream from deserializing out the messy jakarta regexp
    // objects. The accessors handle compilation and initialization upon
    // first use.
    //public RE getDocBaseCompiledRE() { if (DocBaseRE == null) DocBaseRE = REFactory.createRE(DocBase); return DocBaseRE; }
    public RE getFolderPathCompiledRE() { if (FolderPathRE == null) FolderPathRE = REFactory.createRE(FolderPath); return FolderPathRE; }

    //private transient RE DocBaseRE;
    private transient RE FolderPathRE;
    //public String DocBase;
    public String FolderPath;
    public List GroupingFolderTypes; // list of strings...references the corresponding key in the GroupingFolderTypes Map
    public List MrcsDocumentLifecycles; // list of strings...references the corresponding key in the MrcsDocumentLifecycles Map
    public List MrcsLifecycleState; // list of strings...references the corresponding key in the MrcsLifecycleState Map
    public List MrcsWFTemplates;
    public List MrcsWFTasks;
    public List MrcsPreconditions;
}

class Lifecycle extends Object{
	public String LifecycleID;
	public String LifecycleSystemName;
	public List LifecycleStates;
}

class MrcsWFTemplate extends Object{
	public String WFID;
    public List Validations; // MrcsPlugin list
	public List Tasks;
}

class MrcsWFTasks{
	public List Task;
}

class MrcsPreconditions
{
    MrcsPreconditions(){}
    public List Preconditions;
}

/*
class PreCheckInfo {
    PreCheckInfo(){}
    public String FeatureName;
    public String PreconditionRule;
}
*/

// a dummy public object
public class MrcsConfigObjects {}
