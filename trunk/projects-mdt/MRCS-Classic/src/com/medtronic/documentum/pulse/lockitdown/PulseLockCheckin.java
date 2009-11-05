package com.medtronic.documentum.pulse.lockitdown;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.webcomponent.library.contenttransfer.checkin.UcfCheckin;

public class PulseLockCheckin extends UcfCheckin //UcfCheckin
{
    public void onInit(ArgumentList argumentlist) 
    {
    	super.onInit(argumentlist);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"PulseLockDown - Checkin",null,null);
    	// check if new
    	String objid = argumentlist.get("objectId");
    	boolean initialcheckin = false;
    	try { 
    		IDfSysObject doc = (IDfSysObject)getDfSession().getObject(new DfId(objid));
    		// check for _NEW_label
    		for (int i=0; i < doc.getVersionLabelCount(); i++) {
    			String label = doc.getVersionLabel(i);
    	        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"PulseLockDown - Labelcheck: "+label,null,null);
    			if ("_NEW_".equals(label))
    				initialcheckin = true;
    		}
    	} catch (DfException dfe) {}
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"PulseLockDown - isnew: "+initialcheckin,null,null);    	
    	if (!initialcheckin) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"PulseLockDown - no SAME",null,null);    	
            getSameVersionRadioControl(true).setVisible(false);
            getSameVersionLabelControl(true).setVisible(false);
    	}
    }
	

}
