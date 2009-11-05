package com.medtronic.documentum.pulse.lockitdown;

import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.webcomponent.library.create.NewFolderContainer;

public class PulseLockNewFolderContainer extends NewFolderContainer
{
	public void onInit(ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"PulseLockDown - NewFolder",null,null);
		setReturnError("MSG_FOLDER_CREATION_DISABLED",null,null);
        ErrorMessageService.getService().setNonFatalError(this, "MSG_FOLDER_CREATION_DISABLED", null);		
		setComponentReturn(); // no, you can't do anything...
    }

}
