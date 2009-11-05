package com.medtronic.ecm.documentum.mdtworkflow.webtop;

import com.documentum.fc.client.IDfUser;
import com.documentum.webcomponent.library.permissions.ExtendedPermissions;
import com.medtronic.ecm.common.Lg;

public class MdtLockPermissions extends ExtendedPermissions
{
    protected boolean canUserChangePermissions()
    {
        /*-dbg-*/Lg.dbg("Check if user is an administrator");
        // administrators only!
        try { 
            String currentuser = getDfSession().getLoginUserName();
            IDfUser user = getDfSession().getUser(currentuser);
            if (user.isSuperUser() || user.isSystemAdmin()) {
                /*-dbg-*/Lg.dbg("Is admin, see what super says");
                boolean b = super.canUserChangePermissions(); 
                /*-dbg-*/Lg.dbg("super says, can you change permits? %b",b);
                return b;
            } else {
                /*-dbg-*/Lg.dbg("returning false - default");
                return false;
            }
        } catch (Exception e) {
            /*-WARN-*/Lg.wrn("error - return false",e);
            return false;
        }        
    }

}
