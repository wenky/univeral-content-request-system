/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   NewContainer.java

package com.documentum.webcomponent.library.create;

import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.*;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.webcomponent.library.propertysheetwizardcontainer.PropertySheetWizardContainer;
import java.util.ArrayList;
import java.util.Iterator;

public abstract class NewContainer extends PropertySheetWizardContainer
{

    public NewContainer()
    {
        m_bNeedCreate = true;
        m_strTitle = "";
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        String strFolderId = args.get("objectId");
        String strFormattedPath = getCurrentDocbase();
        if(strFolderId != null && strFolderId.length() > 0)
        {
            String strPath = FolderUtil.getPrimaryFolderPath(strFolderId, true);
            if(strPath != null)
                strFormattedPath = strFormattedPath + FolderUtil.formatFolderPath(strPath);
        }
        Label labelDocbasePath = (Label)getControl("docbasePath", com.documentum.web.form.control.Label.class);
        labelDocbasePath.setLabel(strFormattedPath);
        Tabbar tabs = (Tabbar)getControl("tabs");
        Tab tab = tabs.getSelectedTab();
        tab.setValue(getString("MSG_CREATE"));
        numberTabs(tabs);
        Component component = getContainedComponent();
        m_strTitle = component.getString("MSG_TITLE");
        Label labelTitle = (Label)getControl("title", com.documentum.web.form.control.Label.class);
        labelTitle.setLabel(m_strTitle + ": " + getString("MSG_CREATE"));
        setTabEnableState();
    }

    public void onRender()
    {
        super.onRender();
        Label labelTitle = (Label)getControl("title", com.documentum.web.form.control.Label.class);
        String strTabTitle = getContainedComponent().getString("MSG_TITLE");
        if(strTabTitle.equals(m_strTitle))
            strTabTitle = getString("MSG_CREATE");
        labelTitle.setLabel(m_strTitle + ": " + strTabTitle);
        setTabEnableState();
    }

    public boolean onNextComponent(Control control, ArgumentList args)
    {
        boolean bSuccess = false;
        Component component = getContainedComponent();
        component.validate();
        if(component.getIsValid())
        {
            if(onNewComponentTab())
                if(m_bNeedCreate)
                {
                    boolean bCreateSuccess = createObject();
                    if(bCreateSuccess)
                    {
                        Tab tabNew = getNewTab();
                        if(tabNew != null)
                            tabNew.setEnabled(false);
                        m_bNeedCreate = false;
                    } else
                    {
                        return false;
                    }
                } else
                {
                    return false;
                }
            bSuccess = super.onNextComponent(control, args);
        }
        return bSuccess;
    }

    public void onTabSelected(Tab tabSelected, ArgumentList args)
    {
        Tabbar tabs = (Tabbar)getControl("tabs");
        if(m_bNeedCreate)
        {
            ArrayList components = getContainedComponents();
            Component componentCreate = (Component)components.get(0);
            componentCreate.validate();
            if(componentCreate.getIsValid())
            {
                boolean bCreateSuccess = createObject();
                if(bCreateSuccess)
                {
                    Tab tabNew = getNewTab();
                    if(tabNew != null)
                        tabNew.setEnabled(false);
                    m_bNeedCreate = false;
                    super.onTabSelected(tabSelected, args);
                } else
                {
                    Tab tabCreate = getNewTab();
                    tabs.setValue(tabCreate.getName());
                }
            } else
            {
                Tab tabCreate = getNewTab();
                tabs.setValue(tabCreate.getName());
            }
        } else
        {
            super.onTabSelected(tabSelected, args);
        }
    }

    public boolean hasPrevComponent()
    {
        Tabbar tabs = (Tabbar)getControl("tabs");
        Tab tabSelected = tabs.getSelectedTab();
        Iterator iter = tabs.getTabs();
        int index = -1;
        int indexSelected = -1;
        do
        {
            if(!iter.hasNext())
                break;
            Tab tab = (Tab)iter.next();
            index++;
            if(!tab.equals(tabSelected))
                continue;
            indexSelected = index;
            break;
        } while(true);
        if(indexSelected < 2)
            return false;
        else
            return super.hasPrevComponent();
    }

    protected String getDetailsMessage(Exception exception)
    {
        return DocbaseUtils.getValidationExceptionMsg(exception);
    }

    protected Tab getNewTab()
    {
        Tab tabNew = null;
        Tabbar tabs = (Tabbar)getControl("tabs");
        Iterator iter = tabs.getTabs();
        do
        {
            if(!iter.hasNext())
                break;
            Tab tab = (Tab)iter.next();
            String strComponentId = tab.getName();
            if(!strComponentId.equals(getNewComponentName()))
                continue;
            tabNew = tab;
            break;
        } while(true);
        return tabNew;
    }

    protected boolean onNewComponentTab()
    {
        boolean bNewTab = false;
        Tabbar tabs = (Tabbar)getControl("tabs");
        if(tabs != null)
        {
            Tab tab = tabs.getSelectedTab();
            if(tab != null)
            {
                String strComponentId = tab.getName();
                if(strComponentId.equals(getNewComponentName()))
                    bNewTab = true;
            }
        }
        return bNewTab;
    }

    protected abstract boolean createObject();

    protected abstract String getNewComponentName();

    private void setTabEnableState()
    {
        Tabbar tabs = (Tabbar)getControl("tabs");
        Tab tabSelected = tabs.getSelectedTab();
        ArrayList listTabs = new ArrayList(10);
        int idxSelected = 0;
        int nTabs = 0;
        Object tab;
        for(Iterator iter = tabs.getTabs(); iter.hasNext(); listTabs.add(tab))
        {
            tab = iter.next();
            if(tab == tabSelected)
                idxSelected = nTabs;
            nTabs++;
        }

        if(tabSelected != null)
            tabSelected.setEnabled(true);
        boolean fDoneEnable = false;
        for(int idxTab = idxSelected - 1; idxTab > 1; idxTab--)
        {
            Tab taby = (Tab)listTabs.get(idxTab);
            if(!fDoneEnable && taby.isVisible())
            {
                taby.setEnabled(true);
                fDoneEnable = true;
            } else
            {
                taby.setEnabled(false);
            }
        }

        fDoneEnable = false;
        for(int idxTab = idxSelected + 1; idxTab < nTabs; idxTab++)
        {
            Tab taby = (Tab)listTabs.get(idxTab);
            if(!fDoneEnable && taby.isVisible())
            {
                taby.setEnabled(true);
                fDoneEnable = true;
            } else
            {
                taby.setEnabled(false);
            }
        }

    }

    protected boolean m_bNeedCreate;
    protected static final String CONFIG_NEW_COMPONENT_NAME = "newcomponentname";
    private String m_strTitle;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/create/NewContainer.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/