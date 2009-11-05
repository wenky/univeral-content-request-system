package com.medtronic.documentum.pulse.lockitdown;

import com.documentum.fc.common.DfLogger;
import com.documentum.webcomponent.library.permissions.Permissions;

public class PulseLockPermissions extends Permissions
{
    protected boolean canUserChangePermissions()
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"PulseLockDown - NoPermitChange",null,null);
    	return false; // no more permission changing...
    }

}
