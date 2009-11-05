/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ContentTransferServiceContainer.java

package com.documentum.webcomponent.library.contenttransfer;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.beanutils.ConstructorUtils;

import com.documentum.debug.Trace;
import com.documentum.fc.common.IDfException;
import com.documentum.job.StatusState;
import com.documentum.operations.IDfOperationError;
import com.documentum.ucf.common.AbortAllProcessingException;
import com.documentum.ucf.common.UCFException;
import com.documentum.ucf.common.notification.INotificationMonitor;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.LocaleService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.AbortProgressException;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.ContentTransferService;
import com.documentum.web.contentxfer.IContentTransport;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.PromptEvent;
import com.documentum.web.contentxfer.http.HttpContentTransport;
import com.documentum.web.form.Form;
import com.documentum.web.form.FormActionReturnListener;
import com.documentum.web.form.Util;
import com.documentum.web.formext.common.BackDetector;
import com.documentum.web.formext.component.ComboContainer;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.component.IComponentNavigationHook;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.util.ExceptionUtils;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.messages.MessageService;

// Referenced classes of package com.documentum.webcomponent.library.contenttransfer:
//            UcfNavHook, IContentTransferComponent

public abstract class ContentTransferServiceContainer extends ComboContainer
{

    public ContentTransferServiceContainer()
    {
        UcfNavHook hook = new UcfNavHook(getNavigationHook());
        super.setNavigationHook(hook);
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        setReturnValue("success", Boolean.FALSE.toString());
    }

    public void onRender()
    {
        ((UcfNavHook)getNavigationHook()).cleanup();
        super.onRender();
    }

    public void onExit()
    {
        ((UcfNavHook)getNavigationHook()).cleanup();
        super.onExit();
    }

    protected List getPreProcessors()
    {
        IConfigElement serviceElem = lookupElement("service");
        ArrayList processors = new ArrayList();
        addLocalProcessors(serviceElem, "pre-processor-class", processors);
        return processors;
    }

    protected List getPostProcessors()
    {
        IConfigElement serviceElem = lookupElement("service");
        ArrayList processors = new ArrayList();
        addLocalProcessors(serviceElem, "post-processor-class", processors);
        return processors;
    }

    private List getServiceProcessors()
    {
        ArrayList processors = new ArrayList();
        processors.addAll(getPreProcessors());
        ArrayList components = getContainedComponents();
        for(int i = 0; i < components.size(); i++)
        {
            Object comp = components.get(i);
            if(comp instanceof IContentTransferComponent)
                processors.add(((IContentTransferComponent)comp).getServiceProcessor());
        }

        processors.addAll(getPostProcessors());
        return processors;
    }

    private void addLocalProcessors(IConfigElement serviceElem, String elementName, ArrayList processors)
    {
        if(serviceElem != null)
        {
            IServiceProcessor proc;
            for(Iterator elems = serviceElem.getChildElements(elementName); elems.hasNext(); processors.add(proc))
            {
                IConfigElement procElem = (IConfigElement)elems.next();
                Class cl = getClassFromString(procElem.getValue());
                if(cl == null)
                    throw new WrapperRuntimeException("Failed to find processor class " + procElem.getValue());
                try
                {
                    proc = (IServiceProcessor)cl.newInstance();
                }
                catch(IllegalAccessException e)
                {
                    throw new WrapperRuntimeException("Could not instantiate processor", e);
                }
                catch(InstantiationException e)
                {
                    throw new WrapperRuntimeException("Could not instantiate processor", e);
                }
            }

        }
    }

    protected void initContainedComponents()
    {
        int current = getCurrentComponent();
        int size = getComponentCount();
        for(int i = 0; i < size; i++)
        {
            setCurrentComponent(i);
            initCurrentComponent();
        }

        setCurrentComponent(current);
    }

    public boolean canComponentsCommitChanges()
    {
        boolean canCommit = true;
        int size = getComponentCount();
        int i = 0;
        do
        {
            if(i >= size)
                break;
            setCurrentComponent(i);
            Component comp = getContainedComponent();
            if(comp.isInitialized() && !comp.canCommitChanges())
            {
                canCommit = false;
                break;
            }
            i++;
        } while(true);
        return canCommit;
    }

    protected void tryCommitChanges()
    {
        validate();
        if(getIsValid() && canComponentsCommitChanges() && canCommitChanges() && onCommitChanges())
            try
            {
                invokeService();
            }
            catch(ContentTransferException e)
            {
                throw new WrapperRuntimeException(e);
            }
    }

    protected void invokeService()
        throws ContentTransferException
    {
        ContentTransferService service = createService();
        invokeService(service);
    }

    protected void invokeService(ContentTransferService service)
        throws ContentTransferException
    {
        service.setTransport(createTransport());
        service.setServiceProcessors(getServiceProcessors());
        JobAdapter job = new JobAdapter(service, getString("MSG_PROGRESS_TITLE"), getNlsClass(), LocaleService.getLocale());
        long maxinactive = getPageContext().getSession().getMaxInactiveInterval() * 1000;
        job.setRequestInputTimeout(maxinactive);
        setServiceJobWrapper(job);
        String jobId = job.getId();
        job.hold(getPageContext().getSession());
        ArgumentList args = new ArgumentList();
        args.add("jobId", jobId);
        args.add("pending", "true");
        setComponentNested("jobprogressmonitor", args, getContext(), new FormActionReturnListener(this, "onReturnFromProgress"));
    }

    public void onReturnFromProgress(Form form, Map map)
    {
        String jobId = (String)map.get("JOB_ID_RETURN_ARG");
        if(jobId != null)
        {
            JobAdapter job = getServiceJobWrapper();
            StatusState state = job.getJobProgressEvent().getState();
            if(state == StatusState.JOB_FINISHED || state == StatusState.JOB_ABORTED)
                handleOnReturnFromProgressSuccess(form, map, job);
            else if(state == StatusState.JOB_REQUESTING_INPUT)
                handleOnReturnFromProgressRequestInput(form, map, job);
            else
                handleOnReturnFromProgressFailure(form, map, job);
        } else
        {
            throw new IllegalStateException("Failed to retrieve job");
        }
    }

    public void onReturnFromDFCPrompt(Form form, Map map)
    {
        String strButton = (String)map.get("button");
        JobAdapter job = getServiceJobWrapper();
        int nValue;
        if(BackDetector.clickBackButton(map))
            nValue = -1;
        else
        if(job.getPromptEvent().getState() == 0)
        {
            if(strButton.equals("yes"))
                nValue = 1;
            else
            if(strButton.equals("no"))
                nValue = 0;
            else
                nValue = -1;
        } else
        if(strButton.equals("continue"))
            nValue = 1;
        else
            nValue = -1;
        job.setPromptValue(nValue);
        ArgumentList args = new ArgumentList();
        args.add("jobId", job.getId());
        setComponentNested("jobprogressmonitor", args, getContext(), new FormActionReturnListener(this, "onReturnFromProgress"));
    }

    public void onReturnFromUCFPrompt(Form form, Map map)
    {
        JobAdapter job = getServiceJobWrapper();
        String promptValue;
        if(BackDetector.clickBackButton(map))
        {
            promptValue = INotificationMonitor.ABORT_ALL_PROCESSING;
        } else
        {
            String strButton = (String)map.get("button");
            promptValue = getPromptValueMapping(strButton);
        }
        job.setPromptValue(promptValue);
        ArgumentList args = new ArgumentList();
        args.add("jobId", job.getId());
        setComponentNested("jobprogressmonitor", args, getContext(), new FormActionReturnListener(this, "onReturnFromProgress"));
    }

    protected void handleOnReturnFromProgressSuccess(Form form, Map map, JobAdapter job)
    {
        setReturnValue("success", Boolean.TRUE.toString());
        setComponentReturn();
    }

    protected void handleOnReturnFromProgressRequestInput(Form form, Map map, JobAdapter job)
    {
        ArgumentList args = new ArgumentList();
        args.add("dontshowagain", "false");
        PromptEvent promptEvent = job.getPromptEvent();
        IDfOperationError dfOperationError = promptEvent.getOperationError();
        if(dfOperationError == null)
        {
            callPromptComponent(promptEvent, args);
        } else
        {
            IDfException exception;
            if(promptEvent.getState() == 0)
            {
                args.add("title", getString("MSG_CONFIRM_TITLE"));
                args.add("icon", "question");
                args.add("button", new String[] {
                    "yes", "no", "cancel"
                });
            } else
            if(isContinueAllowedInPrompt(dfOperationError))
            {
                args.add("title", getString("MSG_WARNING_TITLE"));
                args.add("icon", "warning");
                args.add("button", new String[] {
                    "continue", "cancel"
                });
            } else
            {
                args.add("title", getString("MSG_ERROR_TITLE"));
                args.add("icon", "stop");
                args.add("button", new String[] {
                    "cancel"
                });
                exception = dfOperationError.getException();
                if(exception != null && (exception instanceof Throwable))
                    Trace.error(this, "WARNING: unexpected error occured", (Throwable)exception);
            }
            exception = dfOperationError.getException();
            String msg;
            if(exception != null && exception.getMessage() != null && exception.getMessage().indexOf(dfOperationError.getMessage()) == -1)
            {
                String exmsg = exception.getMessage();
                if(exmsg.startsWith("["))
                {
                    StringTokenizer stok = new StringTokenizer(exmsg, "[");
                    StringBuffer exmsgBuf = new StringBuffer();
                    String t;
                    int index;
                    for(; stok.hasMoreTokens(); exmsgBuf.append(t.substring(index + 1)))
                    {
                        t = stok.nextToken();
                        index = t.indexOf(':');
                    }

                    exmsg = exmsgBuf.toString();
                }
                msg = getString("MSG_PROMPT_ARG_MESSAGE_FULL", new Object[] {
                    String.valueOf(dfOperationError.getErrorCode()), dfOperationError.getMessage(), exmsg
                });
            } else
            {
                msg = getString("MSG_PROMPT_ARG_MESSAGE", new Object[] {
                    String.valueOf(dfOperationError.getErrorCode()), dfOperationError.getMessage()
                });
            }
            args.add("message", msg);
            BackDetector.setComponentNested(this, "prompt", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromDFCPrompt"));
        }
    }

    protected boolean isContinueAllowedInPrompt(IDfOperationError error)
    {
        return false;
    }

    protected void handleOnReturnFromProgressFailure(Form form, Map map, JobAdapter job)
    {
        Throwable t = job.getThrowable();
        if(t != null)
        {
            for(Throwable cause = t; cause != null; cause = ExceptionUtils.getNextCause(cause))
            {
                if((cause instanceof AbortAllProcessingException) || (cause instanceof AbortProgressException))
                {
                    MessageService.addMessage(this, "MSG_OPERATION_ABORTED");
                    setComponentReturn();
                    return;
                }
                if(!(cause instanceof UCFException))
                    continue;
                String errorCode = ((UCFException)cause).getErrorCode();
                if("UCF_E_ABORT_ALL_PROCESSING".equals(errorCode))
                {
                    MessageService.addMessage(this, "MSG_OPERATION_ABORTED");
                    setComponentReturn();
                    return;
                }
                if("XML_CA_E_XML_PARSE_EXCEPTION".equals(errorCode))
                {
                    WebComponentErrorService.getService().setNonFatalError(this, "MSG_USER_XML_PARSE_ERROR", new Object[] {
                        ((UCFException)cause).getLocalizedMessage()
                    }, null);
                    setComponentReturn();
                    return;
                }
                String strMessagePropId = (String)s_UCFErrorCodeMessageMap.get(errorCode);
                if(strMessagePropId != null && !"".equals(strMessagePropId))
                {
                    WebComponentErrorService.getService().setNonFatalError(this, strMessagePropId, t);
                    setComponentReturn();
                    return;
                }
            }

            if(t instanceof IDfException)
            {
                String localizedMessage = t.getLocalizedMessage();
                WebComponentErrorService.getService().setNonFatalError(this, localizedMessage, t);
                Trace.error(com.documentum.debug.Trace.class, "Operation failed", t);
                setComponentReturn();
                return;
            }
            if((t instanceof ContentTransferException) && t.getCause() != null && (t.getCause() instanceof IDfException))
            {
                String localizedMessage = t.getCause().getLocalizedMessage();
                WebComponentErrorService.getService().setNonFatalError(this, localizedMessage, t.getCause());
                Trace.error(com.documentum.debug.Trace.class, "Operation failed", t);
                setComponentReturn();
                return;
            } else
            {
                throw new WrapperRuntimeException("Operation failed", t);
            }
        } else
        {
            throw new WrapperRuntimeException("Operation failed for unknown reason " + job.getService());
        }
    }

    protected ContentTransferService createService()
    {
        Class serviceClass;
        try { 
	        serviceClass = getServiceClass();
	        if(serviceClass == null)
	            throw new WrapperRuntimeException("Could not create transport");
	        return (ContentTransferService)serviceClass.newInstance();
        } catch (IllegalAccessException e) {
        	throw new WrapperRuntimeException("Could not create transport - illegal access error", e);
        } catch (InstantiationException e) {
        	throw new WrapperRuntimeException("Could not create transport - instantiation error", e);
        }
    }

    protected IContentTransport createTransport()
    {
        Class transportClass = getTransportClass();
        if(transportClass == null)
            throw new WrapperRuntimeException("Could not create transport");
        IContentTransport transport;
        try
        {
            if((com.documentum.web.contentxfer.http.HttpContentTransport.class).isAssignableFrom(transportClass))
            {
                HttpContentTransport httpTranp = (HttpContentTransport)ConstructorUtils.invokeExactConstructor(transportClass, new Object[] {
                    this
                }, new Class[] {
                    com.documentum.web.form.Form.class
                });
                transport = httpTranp;
            } else
            {
                transport = (IContentTransport)transportClass.newInstance();
            }
        }
        catch(IllegalAccessException e)
        {
            throw new WrapperRuntimeException("Could not create transport", e);
        }
        catch(NoSuchMethodException e)
        {
            throw new WrapperRuntimeException("Could not create transport", e);
        }
        catch(InvocationTargetException e)
        {
            throw new WrapperRuntimeException("Could not create transport", e);
        }
        catch(InstantiationException e)
        {
            throw new WrapperRuntimeException("Could not create transport", e);
        }
        return transport;
    }

    protected Class getServiceClass()
    {
        return getClassFromString(lookupString("service.service-class"));
    }

    protected Class getTransportClass()
    {
        return getClassFromString(lookupString("service.transport-class"));
    }

    private Class getClassFromString(String clName)
    {
        Class cl;
        if(clName != null && clName.length() > 0)
            try
            {
                cl = Class.forName(clName);
            }
            catch(ClassNotFoundException e)
            {
                cl = null;
            }
        else
            cl = null;
        return cl;
    }

    protected JobAdapter getServiceJobWrapper()
    {
        return m_serviceJobWrapper;
    }

    protected void setServiceJobWrapper(JobAdapter job)
    {
        m_serviceJobWrapper = job;
    }

    protected String getNoficationCode(PromptEvent promptEvent)
    {
        String strNotificationCode = null;
        if(promptEvent != null)
        {
            IDfOperationError dfOperationError = promptEvent.getOperationError();
            if(dfOperationError == null)
                strNotificationCode = promptEvent.getNotificationCode();
            else
                strNotificationCode = String.valueOf(dfOperationError.getErrorCode());
        }
        return strNotificationCode;
    }

    protected String getNoficationMsg(PromptEvent promptEvent)
    {
        String strNotificationMsg = null;
        if(promptEvent != null)
        {
            IDfOperationError dfOperationError = promptEvent.getOperationError();
            if(dfOperationError == null)
                strNotificationMsg = promptEvent.getNotificationMsg();
            else
                strNotificationMsg = dfOperationError.getMessage();
        }
        return strNotificationMsg;
    }

    public void onReturnFromLocateFile(Form form, Map map)
    {
        String strFileName = (String)map.get("returnFileName");
        String strPromptValue = null;
        if(BackDetector.clickBackButton(map) || strFileName == null)
            strPromptValue = INotificationMonitor.ABORT_ALL_PROCESSING;
        else
        if(strFileName.trim().length() > 0)
            strPromptValue = strFileName.trim();
        JobAdapter job = getServiceJobWrapper();
        job.setPromptValue(strPromptValue);
        callJobProgressMonitor();
    }

    public void onReturnFromPromptInput(Form form, Map map)
    {
        String strPromptRetValue = (String)map.get("returnPromptValue");
        String strPromptValue = null;
        if(BackDetector.clickBackButton(map) || strPromptRetValue == null)
            strPromptValue = INotificationMonitor.ABORT_ALL_PROCESSING;
        else
        if(strPromptRetValue.length() > 0)
            strPromptValue = strPromptRetValue;
        JobAdapter job = getServiceJobWrapper();
        job.setPromptValue(strPromptValue);
        callJobProgressMonitor();
    }

    protected void callJobProgressMonitor()
    {
        JobAdapter job = getServiceJobWrapper();
        ArgumentList args = new ArgumentList();
        args.add("jobId", job.getId());
        setComponentNested("jobprogressmonitor", args, getContext(), new FormActionReturnListener(this, "onReturnFromProgress"));
    }

    public void setNavigationHook(IComponentNavigationHook navHook)
    {
        UcfNavHook hook = (UcfNavHook)super.getNavigationHook();
        hook.setWrappedHook(navHook);
    }

    private void callPromptComponent(PromptEvent promptEvent, ArgumentList args)
    {
        int nPromptType = promptEvent.getPromptType();
        switch(nPromptType)
        {
        case 1: // '\001'
        case 3: // '\003'
            args.add("input", "text");
            args.add("message", promptEvent.getNotificationCode() + ": " + promptEvent.getNotificationMsg());
            args.add("component", "promptinput");
            BackDetector.setComponentNested(this, "promptinputcontainer", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromPromptInput"));
            break;

        case 2: // '\002'
            args.add("message", promptEvent.getNotificationCode() + ": " + promptEvent.getNotificationMsg());
            args.add("title", getString("MSG_YES_NO"));
            args.add("icon", "question");
            args.add("button", new String[] {
                "yes", "no"
            });
            BackDetector.setComponentNested(this, "prompt", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromUCFPrompt"));
            break;

        case 5: // '\005'
            args.add("message", promptEvent.getNotificationCode() + ": " + promptEvent.getNotificationMsg());
            String listValues[] = promptEvent.getListValues();
            String strValues = "";
            if(listValues != null)
            {
                StringBuffer buf = new StringBuffer(128);
                Util.buildAttrList(listValues, 0, listValues.length, buf);
                strValues = buf.toString();
            }
            args.add("input", "dropdown");
            args.add("values", strValues);
            args.add("component", "promptinput");
            BackDetector.setComponentNested(this, "promptinputcontainer", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromPromptInput"));
            break;

        case 4: // '\004'
        default:
            args.add("message", promptEvent.getNotificationCode() + ": " + promptEvent.getNotificationMsg());
            args.add("title", getString("MSG_WARNING_TITLE"));
            args.add("icon", "warning");
            args.add("button", new String[] {
                "ok"
            });
            BackDetector.setComponentNested(this, "prompt", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromUCFPrompt"));
            break;
        }
    }

    private String getPromptValueMapping(String strButton)
    {
        String strRet = null;
        if(strButton != null && strButton.length() > 0)
            if(strButton.equals("ok"))
                strRet = INotificationMonitor.ABORT_ALL_PROCESSING;
            else
            if(strButton.equals("yes"))
                strRet = INotificationMonitor.YES;
            else
            if(strButton.equals("no"))
                strRet = INotificationMonitor.NO;
        return strRet;
    }

    private JobAdapter m_serviceJobWrapper;
    private static Map s_UCFErrorCodeMessageMap;

    static 
    {
        s_UCFErrorCodeMessageMap = new HashMap(2);
        s_UCFErrorCodeMessageMap.put("UCF_E_REMOTE_HOSTS_FAILED_ALL", "MSG_TRANSPORT_ERROR_ACS");
        s_UCFErrorCodeMessageMap.put("UCF_E_CANNOT_PROCESS_ATTMNT_AS_FILE", "MSG_TRANSPORT_ERROR");
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/ContentTransferServiceContainer.class


	TOTAL TIME: 31 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't fully decompile method createService
Couldn't resolve all exception handlers in method createService

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/