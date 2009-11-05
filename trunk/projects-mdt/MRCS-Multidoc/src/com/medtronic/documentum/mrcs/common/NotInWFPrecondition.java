/*
 * Created on Jul 8, 2005
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

 Filename       $RCSfile: NotInWFPrecondition.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:23 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;

/**
 * @author muellc4
 * 
 * TODO To change the template for this generated type comment go to Window -
 * Preferences - Java - Code Style - Code Templates
 */
public class NotInWFPrecondition extends STPromotePreCondition
{
    public boolean isTaskEffectual(Map objectParams, Map pluginConfig) throws DfException {

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotInWFPrecondition.isTaskEffectual ...", null, null);
        /*
         * Note that Preconditions are specified on Per Application Basis Also
         * note that A single application can have any no of Lifecycles, but a
         * document will have atleast a Lifecycle attached upon creation.The
         * entire application can only have unique Lifecycle states defined.
         */

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotInWFPrecondition.isTaskEffectual : Document : " + (IDfDocument) objectParams.get("IDfDocument"), null, null);
        if ((IDfDocument) objectParams.get("IDfDocument") == null)
            return false;
        
        IDfDocument document = (IDfDocument) objectParams.get("IDfDocument");
        String mrcsapp = (String)objectParams.get("mrcsapp");
        boolean canPromote = false;

        /*
         * Check if not a version of the document is in the workflow
         */
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotInWFPrecondition.isTaskEffectual: canPromote (Before preChecks): " + canPromote, null, null);
        canPromote = documentNotInWF(document);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:NotInWFPrecondition.isTaskEffectual: canPromote after documentNotInWF : " + canPromote, null, null);
        
        return canPromote;
    }

}