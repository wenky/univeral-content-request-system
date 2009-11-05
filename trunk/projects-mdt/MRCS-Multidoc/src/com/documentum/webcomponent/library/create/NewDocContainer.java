/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   NewDocContainer.java

package com.documentum.webcomponent.library.create;

import com.documentum.fc.client.*;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.*;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Tab;
import com.documentum.web.form.control.Tabbar;
import com.documentum.web.formext.action.ActionService;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.Context;
import com.documentum.web.formext.session.IHttpSessionManagerUnboundListener;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.webcomponent.common.WebComponentErrorService;
import java.util.ArrayList;
import java.util.Iterator;

// Referenced classes of package com.documentum.webcomponent.library.create:
//            NewContainer, NewDocument

public class NewDocContainer extends NewContainer
    implements IHttpSessionManagerUnboundListener
{

    public NewDocContainer()
    {
        m_newComponent = null;
        m_bEditAfterCreation = true;
    }

    public void onInit(ArgumentList args)
    {
        String editAfterCreate = args.get("editAfterCreate");
        if(editAfterCreate != null && editAfterCreate.equalsIgnoreCase("false"))
            setEditAfterCreation(false);
        super.onInit(args);
    }

    public boolean onPrevPage()
    {
        Component component = getContainedComponent();
        component.validate();
        if(component.hasPrevPage() && component.getIsValid())
            return super.onPrevPage();
        else
            return false;
    }

    public void onTabSelected(Tab tabSelected, ArgumentList args)
    {
        Component component = getContainedComponent();
        component.validate();
        if(component.getIsValid())
            super.onTabSelected(tabSelected, args);
        else
            cancelTabSelection();
    }

    protected void cancelTabSelection()
    {
        Tabbar tabs = (Tabbar)getControl("tabs");
        if(tabs != null)
        {
            String componentId = getContainedComponentId();
            setCurrentComponent(componentId);
            tabs.setValue(componentId);
        }
    }

    public void onOk(Control button, ArgumentList args)
    {
        Component containedComponent = getContainedComponent();
        containedComponent.validate();
        if(containedComponent.getIsValid() && canCommitChanges() && onCommitChanges() && validateNameChange())
        {
            SessionManagerHttpBinding.removeHttpSessionUnboundListener(this);
            ArrayList components = getContainedComponents();
            NewDocument component = (NewDocument)components.get(0);
            String strNewObjectId = component.getNewObjectId();
            boolean bNewObjectHasContent = component.isFormatAndTemplateSupplied();
            setReturnValue("newObjectId", strNewObjectId);
            if(strNewObjectId != null)
            {
                if(bNewObjectHasContent && m_bEditAfterCreation)
                {
                    String strType = component.getNewType();
                    Context context = getContext();
                    ArgumentList componentArgs = new ArgumentList();
                    componentArgs.add("objectId", strNewObjectId);
                    context.set("type", strType);
                    context.set("objectId", strNewObjectId);
                    ActionService.execute("editafternew", componentArgs, context, this, null);
                } else
                {
                    try
                    {
                        IDfPersistentObject obj = getDfSession().getObject(new DfId(strNewObjectId));
                        obj.getValidator().validateAll(null, false);
                        setComponentReturn();
                    }
                    catch(DfException e)
                    {
                        Object params[] = new Object[1];
                        params[0] = getDetailsMessage(e);
                        WebComponentErrorService.getService().setNonFatalError(this, "MSG_VALIDATION_ERROR", params, null);
                    }
                }
            } else
            {
                setComponentReturn();
            }
        }
    }

    protected void setEditAfterCreation(boolean bEditAfterCreation)
    {
        m_bEditAfterCreation = bEditAfterCreation;
    }

    protected boolean createObject()
    {
        NewDocument component = m_newComponent = (NewDocument)getContainedComponent();
        boolean bCreateSuccess = component.createNewObject();
        if(bCreateSuccess)
        {
            SessionManagerHttpBinding.addHttpSessionUnboundListener(this);
            updateComponentParams();
            Tabbar tabs = (Tabbar)getControl("tabs", com.documentum.web.form.control.Tabbar.class);
            Iterator it = tabs.getTabs();
            int nIdx = 0;
            do
            {
                if(it == null || !it.hasNext())
                    break;
                Tab tab = (Tab)it.next();
                nIdx++;
                String strComponentId = tab.getName();
                if(!strComponentId.equals(getNewComponentName()))
                {
                    setCurrentComponent(strComponentId);
                    Component comp = getContainedComponent();
                    removeFromNameIndex(comp);
                    comp = getContainedComponent();
                    String strComponentLabel = null;
                    if(comp.stringExists("MSG_TITLE"))
                        strComponentLabel = comp.getString("MSG_TITLE");
                    else
                        strComponentLabel = strComponentId;
                    numberTab(tab, strComponentLabel, nIdx);
                }
            } while(true);
        }
        return bCreateSuccess;
    }

    private void updateComponentParams()
    {
        ArgumentList args = getContainedComponentArgs();
        ArrayList components = getContainedComponents();
        NewDocument component = (NewDocument)components.get(0);
        String strNewObjectId = component.getNewObjectId();
        String strType = component.getNewType();
        args.replace("objectId", strNewObjectId);
        args.replace("type", strType);
        setContainedComponentArgs(args);
        Context context = getContext();
        context.set("objectId", strNewObjectId);
        context.set("type", strType);
    }

    protected String getNewComponentName()
    {
        String newComponentName = lookupString("newcomponentname");
        if(newComponentName == null || newComponentName.length() == 0)
            newComponentName = "newdocument";
        return newComponentName;
    }

    protected boolean validateNameChange()
    {
        boolean fNameValid = true;
        if(m_newComponent != null && getContainedComponent() != m_newComponent)
        {
            String strObjId = m_newComponent.getNewObjectId();
            try
            {
                IDfSysObject cabobj = (IDfSysObject)getDfSession().getObject(new DfId(strObjId));
                String strObjName = cabobj.getObjectName();
                if(strObjName == null || strObjName.trim().length() == 0)
                {
                    fNameValid = false;
                    WebComponentErrorService.getService().setNonFatalError(m_newComponent, "MSG_MUST_HAVE_NAME", null);
                }
            }
            catch(DfException e)
            {
                throw new WrapperRuntimeException(e);
            }
        }
        return fNameValid;
    }

    public boolean onCancelChanges()
    {
        boolean fCan = super.onCancelChanges();
        if(fCan)
            SessionManagerHttpBinding.removeHttpSessionUnboundListener(this);
        return fCan;
    }

    public void unbound(IDfSessionManager manager)
    {
        if(m_newComponent != null)
            m_newComponent.onCancelChanges(manager);
    }

    private static final String DEFAULT_NEW_DOCUMENT_COMPONENT = "newdocument";
    NewDocument m_newComponent;
    private boolean m_bEditAfterCreation;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/create/NewDocContainer.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/