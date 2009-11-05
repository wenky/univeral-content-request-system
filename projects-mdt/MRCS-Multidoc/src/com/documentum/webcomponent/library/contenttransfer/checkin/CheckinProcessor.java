/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   CheckinProcessor.java

package com.documentum.webcomponent.library.contenttransfer.checkin;

import com.documentum.fc.common.DfException;
import com.documentum.operations.IDfCheckinNode;
import com.documentum.services.subscriptions.ISubscriptions;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.IServiceOperation;
import com.documentum.webcomponent.library.subscription.SubscriptionsHttpBinding;

public class CheckinProcessor extends com.documentum.web.contentxfer.impl.CheckinProcessor
{

    public CheckinProcessor()
    {
        subscribe = false;
        subscriptionService = (new SubscriptionsHttpBinding()).getSubscriptionsService();
    }

    public void postProcess(IServiceOperation op)
        throws ContentTransferException
    {
        super.postProcess(op);
        try
        {
            doSubscribeObject();
        }
        catch(DfException e)
        {
            throw new ContentTransferException(e);
        }
    }

    protected void doSubscribeObject()
        throws DfException, ContentTransferException
    {
        com.documentum.operations.IDfOperationNode node = getDfOperationNode();
        if(node != null && (node instanceof IDfCheckinNode))
        {
            IDfCheckinNode cnode = (IDfCheckinNode)node;
            String newId = cnode.getNewObjectId().toString();
            ISubscriptions subscriptions = getSubscriptions();
            String docbaseName = getContext().getDfSession().getDocbaseName();
            if(isSubscribe())
                subscriptions.subscribe(docbaseName, newId);
            else
                subscriptions.unsubscribe(docbaseName, newId);
        }
    }

    public boolean isSubscribe()
    {
        return subscribe;
    }

    public void setSubscribe(boolean subscribe)
    {
        this.subscribe = subscribe;
    }

    protected ISubscriptions getSubscriptions()
    {
        return subscriptionService;
    }

    private boolean subscribe;
    private ISubscriptions subscriptionService;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/checkin/CheckinProcessor.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/