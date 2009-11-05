/*
 * Created on Apr 12, 2005
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

 Filename       $RCSfile: MrcsWFTaskActions.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:50 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import java.util.Map;

import com.documentum.fc.common.DfException;

/**
 * @author prabhu1
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public interface MrcsWFTaskActions {
    /**
     * @param params
     * @throws DfException
     */
    public void executeWFAction(Map params) throws DfException;
}