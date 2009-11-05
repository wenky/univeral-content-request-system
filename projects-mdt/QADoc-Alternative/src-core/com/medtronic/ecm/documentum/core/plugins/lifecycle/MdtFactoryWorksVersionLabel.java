package com.medtronic.ecm.documentum.core.plugins.lifecycle;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtLifecycleAction;

public class MdtFactoryWorksVersionLabel implements IMdtLifecycleAction {

	public void execute(String mdtapp, IDfSysObject idfsysobject, String username, String targetstate, Map pluginconfig) {

        boolean isFW = false;

		/*-INFO-*/Lg.inf("MdtFactoryWorksVersionLabel plugin");
        /*-dbg-*/Lg.dbg("Application : %s",mdtapp);
        /*-dbg-*/Lg.dbg("UserName : %s",username);
        /*-dbg-*/Lg.dbg("TargetState: %s",targetstate);
        
        // Get the current session.
        IDfSession session = idfsysobject.getSession();
        
        try {
	        // is FW? - get list of FW dirs from config
	        List fwdirs = (List)pluginconfig.get("FactoryWorksDirectories");
	        for (int f=0; f < idfsysobject.getFolderIdCount(); f++)
	        {
	            IDfId folderid = idfsysobject.getFolderId(f);
	            IDfFolder folder = (IDfFolder)session.getObject(folderid);
	            String curpath = folder.getFolderPath(0);
	            /*-dbg-*/Lg.dbg("CurrentFolderPath: %s",curpath);
	            for (int fwf = 0; fwf < fwdirs.size(); fwf++) {
	                String folderpath = (String)fwdirs.get(fwf);
	                if (curpath.contains(folderpath)) {
	                    isFW = true;
	                    break;
	                }
	            }
	            if (isFW) break;
	        }
	        
	        if (isFW) {
	        	/*-INFO-*/Lg.inf("constant/static name plugin called");
	            String staticname = (String)((Map)pluginconfig).get("Label");
	            /*-dbg-*/Lg.dbg("StaticLabel: %s",staticname);
	        	idfsysobject.appendString("r_version_label", staticname);
	        	idfsysobject.save();
	        	/*-dbg-*/Lg.dbg("SaveVersionLabel: %s",staticname);
	        }
	        
		} catch (DfException e) {
			// TODO Auto-generated catch block
			/*-dbg-*/Lg.err("Failed to set version label: %s",e.getMessage());
		}		
	}
}
