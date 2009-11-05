/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   CheckinContainer.java

package com.documentum.webcomponent.library.contenttransfer.checkin;

import java.util.Collections;
import java.util.Map;

import com.documentum.operations.IDfOperationError;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.impl.CheckinService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.FormActionReturnListener;
import com.documentum.web.form.control.Radio;
import com.documentum.web.formext.common.BackDetector;
import com.documentum.web.util.ExceptionUtils;
import com.documentum.web.util.TopicService;
import com.documentum.webcomponent.library.contenttransfer.ContentTransferServiceContainer;
import com.documentum.webcomponent.library.messages.MessageService;

public class CheckinContainer extends ContentTransferServiceContainer
{

    public CheckinContainer()
    {
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
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

    public void onControlInitialized(Form form, Control control)
    {
        String strControlName = control.getName();
        if(!"attribute_object_name".equals(strControlName) && !"formatlist".equals(strControlName) && !"filebrowse".equals(strControlName) && !(control instanceof Radio))
            super.onControlInitialized(form, control);
    }

    protected void handleOnReturnFromProgressSuccess(Form form, Map map, JobAdapter job)
    {
        Map ids = null;
        try
        {
            if(job.getService() instanceof CheckinService)
            {
                CheckinService service = (CheckinService)job.getService();
                ids = service.getNewObjectIds();
                if(ids != null)
                {
                    setReturnValue("newObjectIds", ids);
                    resetTopics(ids);
                }
            }
            addFinalSuccessMessage();
            super.handleOnReturnFromProgressSuccess(form, map, job);
        }
        catch(ContentTransferException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void handleOnReturnFromProgressRequestInput(Form form, Map map, JobAdapter job)
    {
        ArgumentList args = new ArgumentList();
        com.documentum.web.contentxfer.PromptEvent promptEvent = job.getPromptEvent();
        String strNotificationCode = getNoficationCode(promptEvent);
        String strNotificationMsg = getNoficationMsg(promptEvent);
        if(strNotificationCode.equals("UCF_I_FILE_NOT_FOUND") || strNotificationCode.equals("UCF_I_OBJECT_NOT_CHECKED_OUT_LOCALLY"))
        {
            args.add("fileName", strNotificationMsg);
            args.add("component", "locatecheckinlocalfile");
            BackDetector.setComponentNested(this, "locatecheckinlocalfilecontainer", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromLocateFile"));
        } else
        {
            super.handleOnReturnFromProgressRequestInput(form, map, job);
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
            if(ExceptionUtils.anyCauseMessageContains(e, "[DM_SYOBJECT_E_CANT_UNLOCK]") || ExceptionUtils.anyCauseMessageContains(e, "[DM_FOREIGN_E_REFERENCE_NONE]"))
            {
                throwException = false;
                ErrorMessageService.getService().setNonFatalError(this, "MSG_OPERATION_FAILED_REMOTE_OBJECT_CANNOT_CHECKIN", e);
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

    protected void resetTopics(Map ids)
    {
        TopicService ts = TopicService.getInstance();
        ts.resetTopic("topic", ids.values().iterator());
    }

    protected boolean isContinueAllowedInPrompt(IDfOperationError error)
    {
        int code = error.getErrorCode();
        if(code == 1013 || code == 1069 || code == 1064 || code == 5003)
            return true;
        else
            return super.isContinueAllowedInPrompt(error);
    }

    public static Map retrieveReturnObjectIds(Map onReturnMap)
    {
        Map newObjectIdMap = Collections.EMPTY_MAP;
        Object retNewIds = onReturnMap.get("newObjectIds");
        if(retNewIds != null && (retNewIds instanceof Map))
        {
            newObjectIdMap = (Map)retNewIds;
        } else
        {
            retNewIds = onReturnMap.get("old2newObjectIds");
            if(retNewIds != null && (retNewIds instanceof Map))
                newObjectIdMap = (Map)retNewIds;
        }
        return newObjectIdMap;
    }

    public static final String NEW_OBJECT_IDS = "newObjectIds";
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/checkin/CheckinContainer.class


	TOTAL TIME: 15 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/