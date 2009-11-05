package com.medtronic.ecm.documentum.core.plugins.lifecycle;

import java.util.Map;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.plugins.IMdtLifecycleAction;

public class MdtPromotePreviousVersionForward implements IMdtLifecycleAction {

	public void execute(String mdtapp, IDfSysObject idfsysobject, String username, String targetstate, Map configdata) {
        /*-INFO-*/Lg.inf("MdtPromoteEffectiveToRetired plugin");
        String triggerstate = (String)configdata.get("TriggerState");
        /*-dbg-*/Lg.dbg("Application : %s",mdtapp);
        /*-dbg-*/Lg.dbg("UserName : %s",username);
        /*-dbg-*/Lg.dbg("TargetState: %s",targetstate);
        
        IDfSession session = idfsysobject.getSession();
        try {
			IDfCollection col = idfsysobject.getVersions(null);
			while(col.next()){
				String objectId = col.getTypedObject().getString("r_object_id");
				/*-dbg-*/Lg.dbg("ObjectId: %s", objectId);
				/*-dbg-*/Lg.dbg("ObjectName: %s",col.getTypedObject().getString("object_name"));
				IDfSysObject sysObject = (IDfSysObject)session.getObject(new DfId(objectId));
				String currentStateName = sysObject.getCurrentStateName();
				if (sysObject.getObjectId().getId() != idfsysobject.getObjectId().getId()) {
					if (currentStateName != null && triggerstate.equalsIgnoreCase(currentStateName)){
						/*-INFO-*/Lg.inf("Found Previous %s Version",triggerstate);
						/*-dbg-*/Lg.dbg("Promote Object: %s to Forward", objectId);
						sysObject.promote(null, false, false);
						break;
					}
				}
			}
			col.close();
		} catch (DfException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

}
