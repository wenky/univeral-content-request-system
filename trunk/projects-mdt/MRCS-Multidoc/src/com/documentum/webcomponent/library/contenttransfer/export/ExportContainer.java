/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ExportContainer.java

package com.documentum.webcomponent.library.contenttransfer.export;

import java.util.Map;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.IContentTransport;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.http.HttpContentTransport;
import com.documentum.web.contentxfer.impl.ExportService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.util.ExceptionUtils;
import com.documentum.webcomponent.library.contenttransfer.ContentTransferServiceContainer;
import com.documentum.webcomponent.library.messages.MessageService;

public abstract class ExportContainer extends ContentTransferServiceContainer
{

    public ExportContainer()
    {
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        initContainedComponents();
    }

    public void onOk(Control button, ArgumentList args)
    {
        validate();
        if(getIsValid() && canCommitChanges() && onCommitChanges())
            try
            {
                invokeService();
            }
            catch(ContentTransferException e)
            {
                throw new WrapperRuntimeException(e);
            }
    }

    protected void handleOnReturnFromProgressSuccess(Form form, Map map, JobAdapter job)
    {
        try
        {
            if(job.getService() instanceof ExportService)
            {
                ExportService service = (ExportService)job.getService();
                Map paths = service.getNewClientPaths();
                if(paths != null)
                    setReturnValue("newClientPaths", paths);
            }
            addFinalSuccessMessage();
            super.handleOnReturnFromProgressSuccess(form, map, job);
        }
        catch(ContentTransferException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void handleOnReturnFromProgressFailure(Form form, Map map, JobAdapter job)
    {
        if(job.getThrowable() instanceof ContentTransferException)
        {
            ContentTransferException e = (ContentTransferException)job.getThrowable();
            boolean throwException = true;
            if(ExceptionUtils.anyCauseMessageContains(e, "[DM_API_E_ACCESS]"))
            {
                throwException = false;
                ErrorMessageService.getService().setNonFatalError(this, "MSG_OPERATION_FAILED_REMOTE_OBJECT_NOT_ACCESSIBLE", e);
            }
            if(ExceptionUtils.anyCauseMessageContains(e, "[DM_FOREIGN_E_REFERENCE_NONE]"))
            {
                throwException = false;
                ErrorMessageService.getService().setNonFatalError(this, "MSG_OPERATION_FAILED_REMOTE_OBJECT_CANNOT_EXPORT", e);
            }
            if(ExceptionUtils.anyCauseMessageContains(e, "[DM_FOREIGN_E_COULD_NOT_CONNECT]"))
            {
                throwException = false;
                ErrorMessageService.getService().setNonFatalError(this, "MSG_REMOTE_DOCBASE_COULD_NOT_CONNECT", e);
            }
            if(!throwException)
            {
                setComponentReturn();
                return;
            }
        }
        super.handleOnReturnFromProgressFailure(form, map, job);
    }

    protected void addFinalSuccessMessage()
    {
        MessageService.addMessage(this, "MSG_OPERATION_SUCCESSFUL");
    }

    protected IContentTransport createTransport()
    {
        IContentTransport transport = super.createTransport();
        if(transport instanceof HttpContentTransport)
            ((HttpContentTransport)transport).setMode(4);
        return transport;
    }

    public static final String NEW_CLIENT_PATHS = "newClientPaths";
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/export/ExportContainer.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/