/*
 **********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.2
 Description	Precondition to restrict checking out a previous vers.
 Created on		11/14/2006

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: NotPreviousVersion.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/11/14 17:34:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;

import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;

public class NotPreviousVersion  extends STPromotePreCondition
{
    public boolean isTaskEffectual(Map objectParams, Map pluginConfig) throws DfException {

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotPreviousVersion.isTaskEffectual ...", null, null);
        /*
         * Note that Preconditions are specified on Per Application Basis Also
         * note that A single application can have any no of Lifecycles, but a
         * document will have atleast a Lifecycle attached upon creation.The
         * entire application can only have unique Lifecycle states defined.
         */

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotPreviousVersion.isTaskEffectual : Document : " + (IDfDocument) objectParams.get("IDfDocument"), null, null);
        if ((IDfDocument) objectParams.get("IDfDocument") == null)
            return false;
        
        IDfDocument document = (IDfDocument) objectParams.get("IDfDocument");
        String mrcsapp = (String)objectParams.get("mrcsapp");
        boolean canPromote = false;

        /*
         * Check if not a version of the document is in the workflow or a previous version
         */
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotPreviousVersion.isTaskEffectual: canPromote (Before preChecks): " + canPromote, null, null);
        canPromote = documentNotInWF(document);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotPreviousVersion.isTaskEffectual: canPromote after documentNotInWF : " + canPromote, null, null);
        if (canPromote) {
        	//MJH:  check to see if the document is a previous version if its not in a workflow
        	canPromote = document.getHasFolder();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotPreviousVersion.isTaskEffectual: canPromote after getHasFolder : " + canPromote, null, null);
        }
        return canPromote;
    }

}
