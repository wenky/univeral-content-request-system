/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   Export.java

package com.documentum.webcomponent.library.contenttransfer.export;

import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.contentxfer.impl.ExportProcessor;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.formext.control.docbase.DocbaseIcon;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.webcomponent.library.contenttransfer.SysobjectContentTransferComponent;

public class Export extends SysobjectContentTransferComponent
{

    public Export()
    {
        m_ignoreDescendants = false;
    }

    public void onInit(ArgumentList args)
    {
        setContentType(args.get("contentType"));
        setPageModifier(args.get("pageModifier"));
        setIgnoreDescendants(Boolean.valueOf(args.get("isIgnoreDescendents")).booleanValue());
        super.onInit(args);
        initControls();
    }

    protected void initControls()
    {
        initHeaderControls();
        initStandardOptionsControls();
    }

    protected void initFromSysobject(IDfSysObject obj)
    {
        try
        {
            String formatName = getContentType();
            if(formatName != null && formatName.length() > 0)
            {
                IDfFormat format = obj.getFormat();
                if(format != null && format.getName().equals(formatName))
                    setContentType(null);
            }
            super.initFromSysobject(obj);
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void initHeaderControls()
    {
        DocbaseObject docbaseObject = getDocbaseObjectControl(true);
        docbaseObject.setObjectId(getObjectId());
        DocbaseIcon icon = getObjectDocbaseIconControl(true);
        icon.setFormat(getObjectContentType());
        icon.setType(getObjectType());
    }

    protected void initStandardOptionsControls()
    {
        if(!isObjectVirtualDoc() || isIgnoreDescendents())
            getVdmDownloadDescCheckboxControl(true).setVisible(false);
    }

    public IServiceProcessor getServiceProcessor()
    {
        IServiceProcessor sp = createServiceProcessor();
        if(sp instanceof ExportProcessor)
        {
            ExportProcessor proc = (ExportProcessor)sp;
            proc.setObjectId(getObjectId());
            proc.setFormat(getContentType());
            proc.setPageModifier(getPageModifier());
            if(!isIgnoreDescendents())
                proc.setDownloadDescendants(getDownloadDescendantsSelection());
            proc.setVirtualDocRootObjectId(getInitArgs().get("vdmRootObjectId"));
            proc.setVirtualDocNodeId(getInitArgs().get("nodeId"));
        }
        return sp;
    }

    protected boolean getDownloadDescendantsSelection()
    {
        if(isObjectVirtualDoc())
        {
            Checkbox downloadDesc = getVdmDownloadDescCheckboxControl(false);
            return downloadDesc != null && downloadDesc.isVisible() && downloadDesc.getValue();
        } else
        {
            return false;
        }
    }

    public boolean isIgnoreDescendents()
    {
        return m_ignoreDescendants;
    }

    protected void setIgnoreDescendants(boolean ignoreDescendants)
    {
        m_ignoreDescendants = ignoreDescendants;
    }

    public boolean canCommitChanges()
    {
        return hasRendered() || !isObjectVirtualDoc() || isObjectVirtualDoc() && isIgnoreDescendents();
    }

    protected String getPageModifier()
    {
        return pageModifier;
    }

    protected void setPageModifier(String pageModifier)
    {
        this.pageModifier = pageModifier;
    }

    protected String getContentType()
    {
        return contentType;
    }

    protected void setContentType(String contentType)
    {
        this.contentType = contentType;
    }

    protected DocbaseIcon getObjectDocbaseIconControl(boolean create)
    {
        return (DocbaseIcon)getControl0("obj_icon", create, com.documentum.web.formext.control.docbase.DocbaseIcon.class);
    }

    protected Checkbox getVdmDownloadDescCheckboxControl(boolean create)
    {
        return (Checkbox)getControl0("downloadDescCheckbox", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected DocbaseObject getDocbaseObjectControl(boolean create)
    {
        return (DocbaseObject)getControl0("object", create, com.documentum.web.formext.control.docbase.DocbaseObject.class);
    }

    private Control getControl0(String name, boolean create, Class cl)
    {
        return create ? getControl(name, cl) : getControl(name);
    }

    private String contentType;
    private String pageModifier;
    private boolean m_ignoreDescendants;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/export/Export.class


	TOTAL TIME: 16 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/