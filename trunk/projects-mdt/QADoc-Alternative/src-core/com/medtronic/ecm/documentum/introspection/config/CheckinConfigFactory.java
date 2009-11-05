/*
 * Created on Mar 3, 2005
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

 Filename       $RCSfile: CheckinConfigFactory.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:36 $

***********************************************************************
*/

package com.medtronic.ecm.documentum.introspection.config;


import com.documentum.fc.common.DfLogger;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class CheckinConfigFactory extends GenericConfigFactory {


	private static CheckinConfigFactory _checkinConfig = null;

	/**
	 * @throws Exception
	 */
	private CheckinConfigFactory()
    {
		super();
	}

	/**
	 * Obtain the StateTransition ConfigFactory
	 * @return
	 * @throws Exception
	 */
	public static CheckinConfigFactory getCheckinConfig()
    {

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(CheckinConfigFactory.class))DfLogger.debug(CheckinConfigFactory.class, "MRCS:CheckinConfigFactory.getConfigBroker - check if we need to instantiate", null, null);
        // our factory method for configbroker
        if (_checkinConfig == null) {
        	synchronized (CheckinConfigFactory.class) {
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(CheckinConfigFactory.class))DfLogger.debug(CheckinConfigFactory.class, "MRCS:CheckinConfigFactory.getConfigBroker - need to instantiate and load config", null, null);
                _checkinConfig = new CheckinConfigFactory();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(CheckinConfigFactory.class))DfLogger.debug(CheckinConfigFactory.class, "MRCS:CheckinConfigFactory.getConfigBroker - config loaded", null, null);
            }
        }

        return _checkinConfig;
    }

    /**
     * Get the configured State Information for the current application.
     *
     * @param application
     * @return
     * @throws Exception
     */
    public MrcsCheckInInfo getCheckinInfo(String application) throws Exception
    {
    	// we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);

        MrcsCheckInInfo chkinInf = app.CheckinInfo;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:CheckinConfigFactory.getCheckinInfo : Checkin Info : "+chkinInf, null, null);
        return chkinInf;
    }



}
