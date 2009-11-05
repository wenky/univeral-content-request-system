/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   EditContainer.java

package com.documentum.webcomponent.library.contenttransfer.edit;

import com.documentum.web.contentxfer.IContentTransport;
import com.documentum.web.contentxfer.http.HttpContentTransport;
import com.documentum.webcomponent.library.contenttransfer.checkout.CheckoutContainer;

public class EditContainer extends CheckoutContainer
{

    public EditContainer()
    {
    }

    protected IContentTransport createTransport()
    {
        IContentTransport transport = super.createTransport();
        if(transport instanceof HttpContentTransport)
            ((HttpContentTransport)transport).setMode(2);
        return transport;
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/edit/EditContainer.class


	TOTAL TIME: 0 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/