/*
 * Created on Feb 8, 2005
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

 Filename       $RCSfile: Progress.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:02 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.tbo;


import com.documentum.operations.*;
import com.documentum.fc.common.*;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */

public class Progress implements IDfOperationMonitor {
	public int progressReport( IDfOperation op, int iPercentOpDone,
	        IDfOperationStep step, int iPercentStepDone,
	        IDfOperationNode node ) throws DfException {
	            String strStep = iPercentOpDone + "%   " +
	            iPercentStepDone + "% " + step.getName() + " - " + step.getDescription();
	            
	            String strNode = "     " + node.getId().toString();
	            
	            System.out.println( strStep );
	            System.out.println( strNode );
	            return IDfOperationMonitor.CONTINUE;
	        }
	        
	        public int reportError( IDfOperationError error ) throws DfException {
	            return IDfOperationMonitor.CONTINUE;
	        }
	        
	        public int getYesNoAnswer( IDfOperationError Question ) throws DfException {
	            return IDfOperationMonitor.YES;
	        }


}
