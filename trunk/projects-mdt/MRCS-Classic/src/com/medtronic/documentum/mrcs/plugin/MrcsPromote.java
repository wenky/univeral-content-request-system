/*
 * Created on Jun 2, 2005
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

 Filename       $RCSfile: MrcsPromote.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:35:57 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.plugin;


import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsPromote implements IMrcsPromotePlugin {

    private IDfDocument docToPromote = null;
    private StateInfo stInfo = null;
    private StateInfo nextstInfo = null;
    private StateInfo prevstInfo = null;
    public static final String MRCS_APPLICATION_PROPERTY = "mrcs_application";
    private String appName ="";

    /**
     * 
     */
    public MrcsPromote() {
        super();
        // TODO Auto-generated constructor stub
    }


  
    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.plugin.IMrcsPromote#mrcsPromote(java.util.Map)
     */
    public void mrcsPromote(Map params) throws DfException {
        docToPromote = (IDfDocument)params.get("DocToPromote");
        appName = docToPromote.getString(MRCS_APPLICATION_PROPERTY);
        
        Boolean override = (Boolean)params.get("override");
        boolean bOverride = override.booleanValue();
        
        Boolean testOnly = (Boolean)params.get("testOnly");
        boolean bTestOnly = testOnly.booleanValue();
        
        
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:mrcsPromote : Promoting Document...", null, null);

            //**Initialize the State Information from Configuration**//
            initStateInfo();
            String PromotionType = stInfo.getPromotionType();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:mrcsPromote : DOCSTATE BEFORE PROMOTE PromotionType : " + PromotionType, null, null);
            if (!PromotionType.equalsIgnoreCase("SCHEDULE")){
                // setSysSession();
                docToPromote.promote(stInfo.getNextState(), false, bTestOnly);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:mrcsPromote : DOCSTATE BEFORE POST PROMOTE : " + docToPromote.getCurrentStateName(), null, null);
            }
            
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MrcsPromote:mrcsPromote : Promote Error ", null, e);
            throw new RuntimeException("Error performing MRCS document promote");
        } 

}
    

    private void initStateInfo() {
        try {
            //Need to eliminate the usage of this try catch block by
            //better exception handling mechanizsm at Config broker
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, " > config > " + config);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:initStateInfo :  >**** CURRENT STATE Info ****> " + docToPromote.getCurrentStateName(), null, null);
            stInfo = config.getStateInfo(appName, docToPromote.getCurrentStateName());
            nextstInfo = config.getStateInfo(appName, stInfo.getNextState());
            prevstInfo = config.getStateInfo(appName, stInfo.getDemoteState());
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:initStateInfo :  > stInfo > " + stInfo, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:initStateInfo :  > nextstInfo > " + nextstInfo, null, null);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsPromote:initStateInfo :  > prevstInfo > " + prevstInfo, null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "MrcsPromote:initStateInfo : Exception Occurred : ", null, e);
            throw new RuntimeException("error getting current state information from config");
        }
    }

}