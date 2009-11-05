/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   EditContainer.java

package com.documentum.webcomponent.library.edit;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.LocaleService;
import com.documentum.web.contentxfer.control.EditApplet;
import com.documentum.web.contentxfer.control.ServiceProgressFeedback;
import com.documentum.web.contentxfer.control.UtilApplet;
import com.documentum.web.contentxfer.server.ContentTransferService;
import com.documentum.web.contentxfer.server.IContentXferServiceListener;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.databound.Datagrid;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.contentxfer.ContentTransferContainer;

// Referenced classes of package com.documentum.webcomponent.library.edit:
//            Edit

public class EditContainer extends ContentTransferContainer
{

    public EditContainer()
    {
        m_bNeedUserInput = false;
        m_appletEdit = null;
        m_progress = null;
        m_strTicket = null;
        m_appletUtil = null;
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        m_appletEdit = (EditApplet)getControl("editapplet", com.documentum.web.contentxfer.control.EditApplet.class);
        m_progress = (ServiceProgressFeedback)getControl("serviceprogressfeedback", com.documentum.web.contentxfer.control.ServiceProgressFeedback.class);
        m_appletUtil = (UtilApplet)getControl("utilappletgetuserdir", com.documentum.web.contentxfer.control.UtilApplet.class);
        m_appletUtil.setLocale(LocaleService.getLocale().toString());
        UtilApplet _tmp = m_appletUtil;
        m_appletUtil.setOperation(3);
        if(anyVirtualDocs())
            m_bNeedUserInput = true;
        getControl("__WARNING_GRID_CONTROL_NAME", com.documentum.web.form.control.databound.Datagrid.class);
    }

    public final void onOk(Control button, ArgumentList args)
    {
        if(canCommitChanges() && onCommitChanges())
        {
            setComponentPage("getuserdir");
            checkAndSetWarningPage();
        }
    }

    public void startService(IContentXferServiceListener listener)
    {
        if(getComponentPage().equals("serviceprogress"))
        {
            ContentTransferService service = new ContentTransferService();
            service.addContentXferServiceListener(listener);
            startEdit(service);
        }
    }

    protected void startEdit(ContentTransferService service)
    {
        StringBuffer objectIds = new StringBuffer();
        StringBuffer checkoutDescendants = new StringBuffer();
        StringBuffer downloadDesc = new StringBuffer();
        if(m_bNeedUserInput)
        {
            ArrayList components = getContainedComponents();
            for(int i = 0; i < components.size(); i++)
            {
                Edit component = (Edit)components.get(i);
                if(i == 0)
                {
                    objectIds.append(component.getObjectId());
                    checkoutDescendants.append(String.valueOf(component.getEditDescendants()));
                    downloadDesc.append(String.valueOf(component.getDownloadDescendents()));
                } else
                {
                    objectIds.append("," + component.getObjectId());
                    checkoutDescendants.append("," + String.valueOf(component.getEditDescendants()));
                    downloadDesc.append("," + String.valueOf(component.getDownloadDescendents()));
                }
            }

        } else
        {
            objectIds.append(getObjectIds());
            checkoutDescendants.append(getDescendantsFlags());
        }
        m_strTicket = edit(objectIds.toString(), checkoutDescendants.toString(), downloadDesc.toString(), service);
    }

    public void onContentTransferServiceComplete(Control control, ArgumentList args)
    {
        if(m_strTicket != null && m_strTicket.length() > 0 && getComponentPage().equals("serviceprogress"))
        {
            m_appletEdit.setServiceUrl(getContentSenderUrl());
            m_appletEdit.setContentTicket(m_strTicket);
            m_appletEdit.setLocale(LocaleService.getLocale().toString());
            m_appletEdit.setEnableSubDirCreation("false");
            setComponentPage("edit");
        } else
        {
            setComponentReturn();
        }
    }

    public void onGetUserDir(Control control, ArgumentList args)
    {
        super.onGetUserDir(control, args);
        m_progress.setServiceMgr(this);
        setComponentPage("serviceprogress");
    }

    public void onContinueAfterWarning(Control control, ArgumentList args)
    {
        setComponentPage(m_strPageBeforeWarning);
    }

    protected void checkAndSetWarningPage()
    {
        Set vdmsWithDesc = getIdsOfVdmsEditedWithDescendants();
        Map vdmWarnings = getVdmWarnings(vdmsWithDesc);
        if(!vdmWarnings.isEmpty())
        {
            m_strPageBeforeWarning = getComponentPage();
            setComponentPage("warningpage");
            Datagrid warningsGrid = (Datagrid)getControl("__WARNING_GRID_CONTROL_NAME", com.documentum.web.form.control.databound.Datagrid.class);
            warningsGrid.getDataProvider().setScrollableResultSet(createLockedVdmDescendentsWarningResultSet(vdmWarnings));
        }
    }

    protected Set getIdsOfVdmsEditedWithDescendants()
    {
        Set allVdmIds = getVdmIds();
        Set ids;
        if(allVdmIds.isEmpty())
        {
            ids = Collections.EMPTY_SET;
        } else
        {
            ids = null;
            ArrayList comps = getContainedComponents();
            int size = comps.size();
            for(int i = 0; i < size; i++)
            {
                Edit comp = (Edit)comps.get(i);
                String objId = comp.getObjectId();
                if(!allVdmIds.contains(objId) || !comp.getEditDescendants())
                    continue;
                if(ids == null)
                    ids = new HashSet();
                ids.add(objId);
            }

            if(ids == null)
                ids = Collections.EMPTY_SET;
        }
        return ids;
    }

    protected boolean anyVirtualDocs()
    {
        boolean bAnyVirtualDocs = !getVdmIds().isEmpty();
        return bAnyVirtualDocs;
    }

    protected void continueAfterAppletCheck()
    {
        if(m_bNeedUserInput)
            setComponentPage("containerstart");
        else
            setComponentPage("getuserdir");
    }

    private String edit(String strObjectIds, String strCheckoutDescendants, String strDownloadDesc, ContentTransferService service)
    {
        String strTicket = null;
        try
        {
            strTicket = service.edit(strObjectIds, strCheckoutDescendants, strDownloadDesc, getUserDirWin(), getPageContext(), new int[] {
                1000, 1045, 1001
            });
        }
        catch(Exception e)
        {
            setReturnError("MSG_ERROR_EDIT", null, e);
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_EDIT", e);
        }
        return strTicket;
    }

    private boolean m_bNeedUserInput;
    private EditApplet m_appletEdit;
    private ServiceProgressFeedback m_progress;
    private String m_strTicket;
    private UtilApplet m_appletUtil;
    private String m_strPageBeforeWarning;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/edit/EditContainer.class


	TOTAL TIME: 151893 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/