package com.medtronic.documentum.mrcs.client;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.action.LaunchComponentWithPermitCheck;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.config.IConfigElement;

public class MrcsEditPreprocess extends LaunchComponentWithPermitCheck {

	    /**
	     *The renditions requested for viewing are contained as the argument
	     *'contentType' in the ArgumentList object. The value of this argument is
	     *replaced with the value for the content type of Acrobat PDF.
	     */
	    public boolean execute(String strAction,IConfigElement config,ArgumentList args, Context context, Component component,java.util.Map map)
	    {
	    	String m = "MrcsEditPreprocess.execute - ";
	        if(args != null)
	        {
	        	try {
		            String objectId = args.get("objectId");
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get object", null, null);
		            IDfSysObject docObject = (IDfSysObject)component.getDfSession().getObject(new DfId(objectId));
		            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"isCheckedOut()", null, null);
		            if(!docObject.isCheckedOut()) {
		            	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"checkout", null, null);
		            	docObject.checkout();
		            	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"cancel checkout", null, null);
		            	docObject.cancelCheckout();
		            	/*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done", null, null);
		            }
	        	} catch (DfException dfe) {
	    	        /*-ERROR-*/DfLogger.error(this,m+"exception in checkout cancelcheckout UCF cache hack bug", null, dfe);
	        	}
	        }

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"exec super", null, null);
	        boolean executeResult = super.execute(strAction,config,args,context,component,map);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"done", null, null);
	        return executeResult;

	    }

	}
