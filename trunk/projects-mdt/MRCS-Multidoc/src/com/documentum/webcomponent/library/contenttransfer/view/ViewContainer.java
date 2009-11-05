/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ViewContainer.java

package com.documentum.webcomponent.library.contenttransfer.view;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.contentxfer.IContentTransport;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.http.HttpContentTransport;
import com.documentum.web.form.Form;
import com.documentum.webcomponent.library.contenttransfer.export.ExportContainer;
import java.util.Map;

public class ViewContainer extends ExportContainer
{

    public ViewContainer()
    {
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        tryCommitChanges();
    }

    protected IContentTransport createTransport()
    {
        IContentTransport transport = super.createTransport();
        if(transport instanceof HttpContentTransport)
            ((HttpContentTransport)transport).setMode(3);
        return transport;
    }

    protected void addFinalSuccessMessage()
    {
    }

    protected void handleOnReturnFromProgressRequestInput(Form form, Map map, JobAdapter job)
    {
        com.documentum.web.contentxfer.PromptEvent promptEvent = job.getPromptEvent();
        String strNotificationCode = getNoficationCode(promptEvent);
        if(strNotificationCode.equals("UCF_I_FILE_EXISTS"))
        {
            job.setPromptValue("");
            callJobProgressMonitor();
        } else
        {
            super.handleOnReturnFromProgressRequestInput(form, map, job);
        }
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/view/ViewContainer.class


	TOTAL TIME: 0 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/