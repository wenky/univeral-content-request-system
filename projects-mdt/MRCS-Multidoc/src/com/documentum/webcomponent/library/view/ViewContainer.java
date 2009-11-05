/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ViewContainer.java

package com.documentum.webcomponent.library.view;

import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.operations.IDfXMLUtils;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.LocaleService;
import com.documentum.web.contentxfer.control.ServiceProgressFeedback;
import com.documentum.web.contentxfer.control.UtilApplet;
import com.documentum.web.contentxfer.control.ViewApplet;
import com.documentum.web.contentxfer.server.ContentTransferService;
import com.documentum.web.contentxfer.server.IContentXferServiceListener;
import com.documentum.web.form.Control;
import com.documentum.web.util.DfcUtils;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.contentxfer.ContentTransferContainer;

public class ViewContainer extends ContentTransferContainer
{

    public ViewContainer()
    {
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        m_strLaunchViewer = args.get("launchViewer");
        m_appletView = (ViewApplet)getControl("viewapplet", com.documentum.web.contentxfer.control.ViewApplet.class);
        m_progress = (ServiceProgressFeedback)getControl("serviceprogressfeedback", com.documentum.web.contentxfer.control.ServiceProgressFeedback.class);
        m_appletUtil = (UtilApplet)getControl("utilappletgetuserdir", com.documentum.web.contentxfer.control.UtilApplet.class);
        m_appletUtil.setLocale(LocaleService.getLocale().toString());
        UtilApplet _tmp = m_appletUtil;
        m_appletUtil.setOperation(3);
    }

    public void startService(IContentXferServiceListener listener)
    {
        if(getComponentPage().equals("serviceprogress"))
        {
            ContentTransferService service = new ContentTransferService();
            service.addContentXferServiceListener(listener);
            startView(service);
        }
    }

    protected void startView(ContentTransferService service)
    {
        String strArrayObjectIds[] = getObjectIdArray();
        String strObjectIds = getSourceObjectIds();
        String strDescendantsFlags = null;
        String strContentTypes = getContentTypes();
        String strPageModifiers = getPageModifiers();
        for(int i = 0; i < strArrayObjectIds.length; i++)
        {
            String strDescendantsFlag = "false";
            try
            {
                IDfSysObject sysObj = (IDfSysObject)getDfSession().getObject(new DfId(strArrayObjectIds[i]));
                boolean bIsVirtualDoc = sysObj.isVirtualDocument();
                com.documentum.fc.client.IDfFormat dfFormat = sysObj.getFormat();
                IDfXMLUtils xmlUtils = DfcUtils.getClientX().getXMLUtils();
                xmlUtils.setSession(getDfSession());
                boolean bIsXmlDoc = dfFormat != null && xmlUtils.isXML(dfFormat);
                if(bIsVirtualDoc && bIsXmlDoc)
                    strDescendantsFlag = "true";
            }
            catch(Exception e) { }
            if(i == 0)
                strDescendantsFlags = strDescendantsFlag;
            else
                strDescendantsFlags = strDescendantsFlags + "," + strDescendantsFlag;
        }

        m_strTicket = view(strObjectIds, strDescendantsFlags, strContentTypes, strPageModifiers, service);
    }

    public void onContentTransferServiceComplete(Control control, ArgumentList args)
    {
        if(m_strTicket != null && m_strTicket.length() > 0 && getComponentPage().equals("serviceprogress"))
        {
            m_appletView.setServiceUrl(getContentSenderUrl());
            m_appletView.setContentTicket(m_strTicket);
            m_appletView.setLocale(LocaleService.getLocale().toString());
            if(m_strLaunchViewer != null)
                m_appletView.setLaunchViewer(Boolean.valueOf(m_strLaunchViewer).booleanValue());
            setComponentPage("view");
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

    protected void continueAfterAppletCheck()
    {
        setComponentPage("getuserdir");
    }

    protected void setTicket(String strTicket)
    {
        m_strTicket = strTicket;
    }

    /**
     * @deprecated Method view is deprecated
     */

    protected String view(String strObjectIds, String strDescendantsFlags, String strRenditions, ContentTransferService service)
    {
        String strTicket = null;
        try
        {
            strTicket = service.view(strObjectIds, strDescendantsFlags, getUserDirWin(), strRenditions, getPageContext());
        }
        catch(Exception e)
        {
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_VIEW", e);
        }
        return strTicket;
    }

    protected String view(String strObjectIds, String strDescendantsFlags, String strRenditions, String strPageModifiers, ContentTransferService service)
    {
        String strTicket = null;
        try
        {
            strTicket = service.view(strObjectIds, strDescendantsFlags, getUserDirWin(), strRenditions, strPageModifiers, getPageContext());
        }
        catch(Exception e)
        {
            setReturnError("MSG_ERROR_VIEW", null, e);
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_VIEW", e);
        }
        return strTicket;
    }

    private ViewApplet m_appletView;
    private ServiceProgressFeedback m_progress;
    private String m_strTicket;
    private UtilApplet m_appletUtil;
    private String m_strLaunchViewer;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/view/ViewContainer.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/