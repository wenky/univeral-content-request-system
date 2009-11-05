/*
 * Created on Jan 21, 2005
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

 Filename       $RCSfile: ESignatureConfigFactory.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:37 $

***********************************************************************
*/

package com.medtronic.ecm.documentum.introspection.config;


import java.util.ArrayList;
import java.util.List;

import com.documentum.fc.client.DfClient;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.web.util.DfcUtils;


/**
 * @author muellc4
 *
 * The ESignatureConfigFactory to read the ESignature configurations
 */
public class ESignatureConfigFactory extends GenericConfigFactory
{

	private static ESignatureConfigFactory _eSignConfig = null;

	private ESignatureConfigFactory()
    {
        super();
    }


	/**
	 * Obtain the Esignature ConfigFactory
	 * @return
	 * @throws Exception
	 */
	public static ESignatureConfigFactory getESignConfig()
    {

	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class, "MRCS:ESignatureConfigFactory.getConfigBroker - check if we need to instantiate", null, null);
        // our factory method for configbroker
        if (_eSignConfig == null) {
            synchronized (ESignatureConfigFactory.class) {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class, "MRCS:ESignatureConfigFactory.getConfigBroker - need to instantiate and load config", null, null);
                _eSignConfig = new ESignatureConfigFactory();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class, "MRCS:ESignatureConfigFactory.getConfigBroker - config loaded", null, null);
            }
        }
        return _eSignConfig;
    }

    /**
     * Get the configured list of reasons for the current application.
     *
     * @param application
     * @return
     * @throws Exception
     */
    public List getSignatureReasons(String application) throws Exception
    {
    	/*-CONFIG-*/String m="getSignatureReasons - ";
        // we know the app from the Document DCTM object, passed in as the application parameter
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"get MrcsApp data", null, null);
        MrcsApplication app = getApplication(application);
        // add DCTM-based signing reasons option
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"check for query-based signing reasons", null, null);
        if (app.ESignature.SigningReasonsQuery != null)
        {
    	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"use query to look up signing reasons - create DfClient and sessiong manager", null, null);
            IDfClient client = new DfClient();  
            
            //create a Session Manager object
            IDfSessionManager sMgr = client.newSessionManager();
            
    	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"use current app's system user for credentials - "+app.SystemUsername, null, null);
            //create an IDfLoginInfo object named loginInfoObj
            IDfLoginInfo loginInfoObj = new DfLoginInfo();
            loginInfoObj.setUser(app.SystemUsername);
            loginInfoObj.setPassword(app.SystemPassword);
            loginInfoObj.setDomain(null);     
            sMgr.setIdentity(app.DocBase,loginInfoObj);
            
    	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"get system session", null, null);
            IDfSession session = sMgr.getSession(app.DocBase);
            
            List signingreasons = new ArrayList();
            
            // look up signing reasons
            IDfQuery query = null;
            IDfCollection iCollection = null;
        	try { 
        	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"get a query object", null, null);
    	        query = DfcUtils.getClientX().getQuery();
        	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"setting dql query to "+app.ESignature.SigningReasonsQuery, null, null);
    	        query.setDQL(app.ESignature.SigningReasonsQuery);
    	        iCollection = null;
        	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"executing", null, null);
    	        iCollection = query.execute(session, 0);
        	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"get column details (first attribute)", null, null);
    	        IDfAttr attribute = iCollection.getAttr(0);
        	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"attribute name: "+(attribute != null ? attribute.getName() : null), null, null);
    	        while (iCollection.next())
    	        {
            	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"get next signing reason", null, null);
    	        	String reason = iCollection.getString(attribute.getName());
            	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"appending reason to reason list: "+reason, null, null);
    	        	signingreasons.add(reason);
    	        }
        	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"cleanup query/collection", null, null);
    	        if(iCollection != null)
    	            iCollection.close();
        	} catch (DfException e) {
        	    /*-ERROR-*/DfLogger.error(ESignatureConfigFactory.class,m+"error in signing reasons DQL retrieval, cleaning up collection and session", null, null);
                if(iCollection != null)
                    iCollection.close();
            	sMgr.release(session);
        	    /*-ERROR-*/DfLogger.error(ESignatureConfigFactory.class,m+"rethrow as runtime", null, e);
                throw new RuntimeException("MRCS Error in DQL retrieval of singing reasons",e);
        	}
    	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignatureConfigFactory.class))DfLogger.debug(ESignatureConfigFactory.class,m+"release session", null, null);
        	sMgr.release(session);
        	return signingreasons;
        }
        // return the signingreasons
        return app.ESignature.SigningReasons;
    }

    /**
     * Get the configured list of reasons for the current application.
     *
     * @param application
     * @return
     * @throws Exception
     */
    public Integer getNoOfSigsAllowed(String application) throws Exception
    {
        // we know the app from the Document DCTM object, passed in as the application parameter
        MrcsApplication app = getApplication(application);
        // return the signingreasons
        return app.ESignature.NoOfSigns;
    }
}
