package com.medtronic.ecm.documentum.core.plugins.lifecycle;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtLifecycleAction;

public class MdtPortalVersionLabel implements IMdtLifecycleAction {

	public void execute(String mdtapp, IDfSysObject idfsysobject,
			String username, String targetstate, Map pluginconfig) {

		/*-INFO-*/Lg.inf("MdtPortalVersionLabel plugin");
        /*-dbg-*/Lg.dbg("Application : %s",mdtapp);
        /*-dbg-*/Lg.dbg("UserName : %s",username);
        /*-dbg-*/Lg.dbg("TargetState: %s",targetstate);
        
        try {
            String publishproperty = (String)((Map)pluginconfig).get("PublishProperty");
            /*-dbg-*/Lg.dbg("PublishProperty: %s",publishproperty);
        	boolean publish = idfsysobject.getBoolean(publishproperty);        	
        	/*-dbg-*/Lg.dbg("PublishPropertyValue: %s",publish);
	        if (publish) {
	        	/*-INFO-*/Lg.inf("constant/static name plugin called");
	            String staticname = (String)((Map)pluginconfig).get("Label");
	            /*-dbg-*/Lg.dbg("StaticLabel: %s",staticname);
	        	idfsysobject.appendString("r_version_label", staticname);
	        	idfsysobject.save();
	        	/*-dbg-*/Lg.dbg("SaveVersionLabel: %s",staticname);
	        }
	        
		} catch (DfException e) {
			/*-dbg-*/Lg.err("Failed to set version label: %s",e.getMessage());
		}		
		
	}

}
