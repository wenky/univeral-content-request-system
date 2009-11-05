/*
 * Created on Apr 7, 2005
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

 Filename       $RCSfile: MrcsStartWorkflowTemplateLocatorContainer.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:47 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.webcomponent.library.workflow.startworkflow.StartWorkflowTemplateLocatorContainer;

/**
 * @author prabhu1
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class MrcsStartWorkflowTemplateLocatorContainer extends StartWorkflowTemplateLocatorContainer {

    /**
     *  
     */
    public MrcsStartWorkflowTemplateLocatorContainer() {
        super();
        // TODO Auto-generated constructor stub
    }


    public void onInit(ArgumentList argumentlist) {
        System.out.println("MrcsStartWorkflowTemplateLocatorContainer : onInit");
        super.onInit(argumentlist);
    }


    public boolean onCommitChanges() {
        System.out.println("MrcsStartWorkflowTemplateLocatorContainer : onCommitChanges");
        return super.onCommitChanges();
    }
}