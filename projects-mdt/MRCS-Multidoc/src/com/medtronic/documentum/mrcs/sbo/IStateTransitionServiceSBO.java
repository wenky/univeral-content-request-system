/*
 * Created on Jan 31, 2005
 *
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

 Filename       $RCSfile: IStateTransitionServiceSBO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:00 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfService;
import com.documentum.fc.common.DfException;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * The State Transition SBO Contract
 */

public interface IStateTransitionServiceSBO extends IDfService {
	 
	
    /**
     * The pre condition check made before the execution of Promote 
     * 
     * @param docObject
     * @param stInfo
     * @return
     * @throws DfException
     */
    public boolean preCheckPromote(IDfDocument docObject) throws DfException;
	
	/**
	 * Promote the LifeCycle State
	 * 
	 * @param docObject
	 * @param state
	 * @param override
	 * @param fTestOnly
	 * @throws DfException
	 */
	public void promote(IDfDocument docObject, StateInfo stInfo) throws DfException;
	
	
	/**
	 * Demote the LifeCycle State
	 * 
	 * @param state
	 * @param toBase
	 * @throws DfException
	 */
	public void demote(IDfDocument docObject, java.lang.String state,
            boolean toBase) throws DfException;
}
