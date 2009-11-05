/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   Edit.java

package com.documentum.webcomponent.library.contenttransfer.edit;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.webcomponent.library.contenttransfer.checkout.Checkout;

public class Edit extends Checkout
{

    public Edit()
    {
    }

    protected void initVirtualDocOptions()
    {
        boolean isObjectAlreadyCheckedOut = false;
        try
        {
            com.documentum.fc.client.IDfPersistentObject pobj = getDfSession().getObject(new DfId(getObjectId()));
            if(pobj instanceof IDfSysObject)
            {
                IDfSysObject dfSysObject = (IDfSysObject)pobj;
                String lockOwner = dfSysObject.getLockOwner();
                if(lockOwner != null && lockOwner.length() > 0)
                    isObjectAlreadyCheckedOut = true;
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(getString("MSG_ERROR_CHECKING_OUT"), e);
        }
        if(isObjectAlreadyCheckedOut)
        {
            getVdmOptionsPanelControl(true).setVisible(false);
            getXmlVdmOptionsPanelControl(true).setVisible(false);
            setIgnoreDescendants(true);
        } else
        {
            super.initVirtualDocOptions();
        }
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/edit/Edit.class


	TOTAL TIME: 15 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/