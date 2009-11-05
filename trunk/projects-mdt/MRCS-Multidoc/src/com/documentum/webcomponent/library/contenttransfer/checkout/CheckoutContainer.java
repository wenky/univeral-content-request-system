/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   CheckoutContainer.java

package com.documentum.webcomponent.library.contenttransfer.checkout;

import java.util.Iterator;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.operations.IDfOperationError;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.IContentTransport;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.PromptEvent;
import com.documentum.web.contentxfer.http.HttpContentTransport;
import com.documentum.web.contentxfer.impl.CheckoutService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.FormActionReturnListener;
import com.documentum.web.formext.common.BackDetector;
import com.documentum.web.formext.config.IPreferenceStore;
import com.documentum.web.formext.config.PreferenceService;
import com.documentum.web.util.ExceptionUtils;
import com.documentum.webcomponent.library.contenttransfer.ContentTransferServiceContainer;
import com.documentum.webcomponent.library.messages.MessageService;

public class CheckoutContainer extends ContentTransferServiceContainer
{

    public CheckoutContainer()
    {
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        initContainedComponents();
        tryCommitChanges();
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

    public static String[] retrieveReturnObjectIds(Map onReturnMap)
    {
        String objectIdArray[] = null;
        objectIdArray = (String[])onReturnMap.get("newObjectIds");
        if(objectIdArray == null)
        {
            Map newClientPaths = (Map)onReturnMap.get("newClientPaths");
            if(newClientPaths != null)
            {
                Set keys = newClientPaths.keySet();
                int numObjectIds = keys.size();
                objectIdArray = new String[numObjectIds];
                Iterator objectIdIter = keys.iterator();
                for(int index = 0; objectIdIter.hasNext(); index++)
                {
                    String objectId = (String)objectIdIter.next();
                    objectIdArray[index] = objectId;
                }

            }
        }
        return objectIdArray;
    }

    protected void handleOnReturnFromProgressSuccess(Form form, Map map, JobAdapter job)
    {
        try
        {
            if(job.getService() instanceof CheckoutService)
            {
                CheckoutService service = (CheckoutService)job.getService();
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

    protected void handleOnReturnFromProgressRequestInput(Form form, Map map, JobAdapter job)
    {
        PromptEvent promptEvent = job.getPromptEvent();
        String strNotificationCode = getNoficationCode(promptEvent);
        if(strNotificationCode.equals("UCF_I_FILE_EXISTS"))
        {
            job.setPromptValue("");
            callJobProgressMonitor();
        } else
        if(strNotificationCode.equals(String.valueOf(1000)))
        {
            if(readCheckoutPreference().equals("Prompt"))
            {
                ArgumentList args = new ArgumentList();
                IDfOperationError dfOperationError = promptEvent.getOperationError();
                args.add("message", String.valueOf(dfOperationError.getErrorCode()) + ": " + dfOperationError.getMessage());
                args.add("component", "checkoutbyother");
                BackDetector.setComponentNested(this, "checkoutbyothercontainer", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromLockedByOther"));
            } else
            {
                job.setPromptValue(1);
                callJobProgressMonitor();
            }
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
            if(ExceptionUtils.anyCauseMessageContains(e, "[DM_SYSOBJECT_E_CANT_LOCK]") || ExceptionUtils.anyCauseMessageContains(e, "[DM_FOREIGN_E_REFERENCE_NONE]"))
            {
                throwException = false;
                ErrorMessageService.getService().setNonFatalError(this, "MSG_OPERATION_FAILED_REMOTE_OBJECT_CANNOT_CHECKOUT", e);
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

    public String getString(String stringId)
    {
        String strValue = super.getString(stringId);
        if(stringId.equals("MSG_OBJECT") && (strValue == null || strValue.length() == 0))
        {
            ArgumentList argList[] = getContainedComponentsArgs();
            if(argList != null)
            {
                ArgumentList componentArgs = argList[getCurrentComponent()];
                String strObjectId = componentArgs.get("objectId");
                com.documentum.fc.client.IDfPersistentObject pobj;
                if(strObjectId != null && strObjectId.length() > 0)
                    try
                    {
                        pobj = getDfSession().getObject(new DfId(strObjectId));
                    }
                    catch(DfException e)
                    {
                        if(e.getMessage().indexOf("[DM_API_E_EXIST]") >= 0)
                            strValue = getString("MSG_UNAVAILABLE_ITEM");
                    }
            }
        }
        return strValue;
    }

    protected void addFinalSuccessMessage()
    {
        MessageService.addMessage(this, "MSG_OPERATION_SUCCESSFUL");
    }

    protected IContentTransport createTransport()
    {
        IContentTransport transport = super.createTransport();
        if(transport instanceof HttpContentTransport)
            ((HttpContentTransport)transport).setMode(1);
        return transport;
    }

    protected boolean isContinueAllowedInPrompt(IDfOperationError error)
    {
        int code = error.getErrorCode();
        if(code == 1000 || code == 1045 || code == 1001 || code == 1083)
            return true;
        else
            return super.isContinueAllowedInPrompt(error);
    }

    public void onReturnFromLockedByOther(Form form, Map map)
    {
        JobAdapter job = getServiceJobWrapper();
        String strButton = (String)map.get("button");
        if(strButton != null)
        {
            writeCheckoutPreference((String)map.get("alwaysContinueReturnValue"));
            int nPromptValue = strButton.equals("continue") ? 1 : -1;
            job.setPromptValue(nPromptValue);
        } else
        {
            job.setPromptValue(-1);
        }
        callJobProgressMonitor();
    }

    private String readCheckoutPreference()
    {
        IPreferenceStore preferenceStore = PreferenceService.getPreferenceStore();
        String strCheckoutPreference = preferenceStore.readString("application.checkout.warning.checkedout");
        if(strCheckoutPreference == null)
        {
            strCheckoutPreference = "Prompt";
            preferenceStore.writeString("application.checkout.warning.checkedout", "Prompt");
        }
        return strCheckoutPreference;
    }

    private void writeCheckoutPreference(String strAlwaysContinue)
    {
        if(strAlwaysContinue != null && strAlwaysContinue.equals("true"))
        {
            IPreferenceStore preferenceStore = PreferenceService.getPreferenceStore();
            preferenceStore.writeString("application.checkout.warning.checkedout", "ReadOnly");
        }
    }

    public static final String NEW_CLIENT_PATHS = "newClientPaths";
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/checkout/CheckoutContainer.class


	TOTAL TIME: 93 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/