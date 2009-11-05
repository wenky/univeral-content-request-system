package com.medtronic.documentum.mrcs.client;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.formext.drl.DRLComponent;

public class MrcsDrlViewFolder extends DRLComponent 
{
    public void onInit(ArgumentList args)
    {
        try { 
            String folderpath = args.get("folderPath");
	        IDfSysObject folderobj = (IDfSysObject)getDfSession().getObjectByPath(folderpath);
	        args.add("objectId",folderobj.getObjectId().getId());
        } catch (DfException dfe) {
        	throw new RuntimeException("DCTM Error looking up provided folder path",dfe);
        } 
        super.onInit(args);
    }

}
