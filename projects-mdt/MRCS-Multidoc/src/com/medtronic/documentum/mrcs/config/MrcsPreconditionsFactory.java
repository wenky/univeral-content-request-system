/*
 * Created on Mar 1, 2005
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

 Filename       $RCSfile: MrcsPreconditionsFactory.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:23 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.config;


import com.documentum.fc.common.DfLogger;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsPreconditionsFactory extends GenericConfigFactory {

	private static MrcsPreconditionsFactory _StateTrnsnConfig = null;

	/**
	 * @throws Exception
	 */
	private MrcsPreconditionsFactory() {
		super();
	}

	/**
	 * Obtain the StateTransition ConfigFactory
	 * @return
	 * @throws Exception
	 */
	public static MrcsPreconditionsFactory getPreConditionsConfig()
    {

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsPreconditionsFactory.class))DfLogger.debug(MrcsPreconditionsFactory.class, "MRCS:MrcsPreconditionsFactory.getConfigBroker - check if we need to instantiate", null, null);
        // our factory method for configbroker
        if (_StateTrnsnConfig == null) {
        	synchronized (MrcsPreconditionsFactory.class) {
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsPreconditionsFactory.class))DfLogger.debug(MrcsPreconditionsFactory.class, "MRCS:MrcsPreconditionsFactory.getConfigBroker - need to instantiate and load config", null, null);
                _StateTrnsnConfig = new MrcsPreconditionsFactory();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsPreconditionsFactory.class))DfLogger.debug(MrcsPreconditionsFactory.class, "MRCS:MrcsPreconditionsFactory.getConfigBroker - config loaded", null, null);
            }
        }

        return _StateTrnsnConfig;
    }

    /**
     * Get the configured State Information for the current application.
     *
     * @param application
     * @return
     * @throws Exception
    public String getPreCheckClass(String application, String featureName) throws Exception
    {
    	// we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);
        PreCheckInfo prchkInf =(PreCheckInfo) app.MrcsPreconditions.get(featureName);
        String chkClass = prchkInf.PreconditionRule;
        return chkClass;
    }
     */

    public MrcsPlugin getPreconditionPlugin(String application, String featureName)
    {
        // we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);
        MrcsPlugin prchkInf =(MrcsPlugin) app.MrcsPreconditions.get(featureName);
        return prchkInf;
    }

}
