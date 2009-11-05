/*
 * Created on Feb 24, 2005
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

 Filename       $RCSfile: MrcsCheckoutPreCondition.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:22 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.common;


import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfAttr;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsCheckoutPreCondition implements MrcsPreConditions {

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.common.MrcsPreConditions#isTaskEffectual()
     */
    public boolean isTaskEffectual(Map objectParams, Map pluginConfig) throws DfException {

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsCheckoutPreCondition.isTaskEffectual ...", null,null);
        /*
         * Note that Preconditions are specified on Per Application Basis
         * Also note that A single application can have any no of Lifecycles, but a document will have atleast
         * a Lifecycle attached upon creation.The entire application can only have unique Lifecycle states defined.
         */

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsCheckoutPreCondition.isTaskEffectual : Document : "+(IDfDocument)objectParams.get("IDfDocument"), null,null);
        if((IDfDocument)objectParams.get("IDfDocument") == null) return false;
        
        IDfDocument doc = (IDfDocument)objectParams.get("IDfDocument");
        
        boolean canPromote = false;

         /*
         * Check if not a version of the document is in the workflow
         */
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsCheckoutPreCondition.isTaskEffectual: canPromote (Before preChecks): "+canPromote, null,null);

          canPromote = documentNotInWF(doc);

          /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsCheckoutPreCondition.isTaskEffectual: canPromote after documentNotInWF : "+canPromote, null,null);
          /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsCheckoutPreCondition.isTaskEffectual: Final canPromote before return: "+canPromote, null,null);
        return canPromote;
    }


    private boolean documentNotInWF(IDfDocument docObject) throws DfException {
        double numWF = 0;
        String docId = docObject.getObjectId().getId();
        String qualification = "select count(r_workflow_id) from dmi_package " +
        		"where any r_component_id in " +
        		"(select r_object_id from dm_sysobject where i_chronicle_id in " +
        		"(select i_chronicle_id from dm_sysobject where r_object_id = '" +docId +"'))";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "Mrcs:MrcsCheckoutPreCondition.documentNotInWF: qualification : "+qualification, null,null);

        IDfQuery qry = new DfQuery();
        qry.setDQL(qualification);
		IDfCollection myObj1 = (IDfCollection)qry.execute(docObject.getSession(),IDfQuery.DF_READ_QUERY);

		while(myObj1.next()) {
		    for (int i = 0; i < myObj1.getAttrCount(); i++) { //For attribute
				IDfAttr attr = myObj1.getAttr(i);
				if (attr.getDataType() == attr.DM_DOUBLE) {
				    numWF = myObj1.getDouble(attr.getName());
				    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,  "Mrcs:MrcsCheckoutPreCondition.documentNotInWF: No of WF involved :" +numWF, null,null);
				}
		    }
		}
		myObj1.close();
		if(numWF > 0) return false;
		else return true;
    }
}
