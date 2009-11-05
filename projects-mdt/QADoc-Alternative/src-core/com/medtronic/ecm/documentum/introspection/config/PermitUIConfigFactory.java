/*
 * Created on May 18, 2005
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

 Filename       $RCSfile: PermitUIConfigFactory.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:39 $

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
public class PermitUIConfigFactory extends GenericConfigFactory {

	private static PermitUIConfigFactory _PermitUIConfig = null;

	/**
	 * @throws Exception
	 */
	private PermitUIConfigFactory()
    {
		super();
	}

	/**
	 * Obtain the StateTransition ConfigFactory
	 * @return
	 * @throws Exception
	 */
	public static PermitUIConfigFactory getPermitUIConfig()
    {

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(PermitUIConfigFactory.class))DfLogger.debug(PermitUIConfigFactory.class, "MRCS:PermitUIConfigFactory.getConfigBroker - check if we need to instantiate", null, null);
        // our factory method for configbroker
        if (_PermitUIConfig == null) {
        	synchronized (PermitUIConfigFactory.class) {
        		/*-DEBUG-*/if (DfLogger.isDebugEnabled(PermitUIConfigFactory.class))DfLogger.debug(PermitUIConfigFactory.class, "MRCS:PermitUIConfigFactory.getConfigBroker - need to instantiate and load config", null, null);
                _PermitUIConfig = new PermitUIConfigFactory();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(PermitUIConfigFactory.class))DfLogger.debug(PermitUIConfigFactory.class, "MRCS:PermitUIConfigFactory.getConfigBroker - config loaded", null, null);
            }
        }

        return _PermitUIConfig;
    }

    /**
     * Get the configured State Information for the current application.
     *
     * @param application
     * @return
     * @throws Exception
     */
    public boolean getPermitUIInfo(String application) throws Exception
    {
    	// we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);
        boolean showUI = app.DisplayPermitUI;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MRCS:PermitUIConfigFactory.getPermitUIInfo : Show PermitUI : "+showUI, null, null);
        return showUI;
    }





}
