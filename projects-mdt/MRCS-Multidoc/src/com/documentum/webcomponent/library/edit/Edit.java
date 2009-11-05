/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   Edit.java

package com.documentum.webcomponent.library.edit;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.operations.IDfXMLUtils;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.control.docbase.DocbaseIcon;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.util.DfcUtils;

public class Edit extends Component
{

    public Edit()
    {
        m_strObjectId = null;
        m_bIsVirtualXmlDoc = false;
    }

    public void onInit(ArgumentList arg)
    {
        super.onInit(arg);
        m_strObjectId = arg.get("objectId");
        DocbaseObject docbaseObj = (DocbaseObject)getControl("object", com.documentum.web.formext.control.docbase.DocbaseObject.class);
        docbaseObj.setObjectId(m_strObjectId);
        IDfSysObject sysObj = null;
        boolean bIsVirtualDoc = false;
        try
        {
            IDfSession dfSession = getDfSession();
            sysObj = (IDfSysObject)dfSession.getObject(new DfId(m_strObjectId));
            IDfType dfType = sysObj.getType();
            String strTypeName = dfType.getName();
            String strFormat = sysObj.getContentType();
            DocbaseIcon icon = (DocbaseIcon)getControl("obj_icon", com.documentum.web.formext.control.docbase.DocbaseIcon.class);
            icon.setFormat(strFormat);
            icon.setType(strTypeName);
            bIsVirtualDoc = sysObj.isVirtualDocument();
            icon.setIsVirtualDocument(bIsVirtualDoc);
            if(bIsVirtualDoc)
            {
                IDfXMLUtils xmlUtils = DfcUtils.getClientX().getXMLUtils();
                xmlUtils.setSession(getDfSession());
                com.documentum.fc.client.IDfFormat format = sysObj.getFormat();
                if(format != null && xmlUtils.isXML(format))
                    m_bIsVirtualXmlDoc = true;
            }
        }
        catch(DfException e) { }
        if(!bIsVirtualDoc || m_bIsVirtualXmlDoc)
        {
            Panel panelVdm = (Panel)getControl("vdmoptions", com.documentum.web.form.control.Panel.class);
            panelVdm.setVisible(false);
        }
        if(!m_bIsVirtualXmlDoc)
            getControl("xmlvdmoptions", com.documentum.web.form.control.Panel.class).setVisible(false);
    }

    public boolean getDownloadDescendents()
    {
        if(m_bIsVirtualXmlDoc)
        {
            Radio downloadDesc = (Radio)getControl("xmlrootdownload");
            return downloadDesc != null && downloadDesc.isVisible() && downloadDesc.getValue();
        } else
        {
            return false;
        }
    }

    public boolean getEditDescendants()
    {
        boolean bEditDescendants = false;
        Radio radioDescs = (Radio)getControl("editwithdescendents", com.documentum.web.form.control.Radio.class);
        if(m_bIsVirtualXmlDoc)
        {
            Radio radioAllDescs = (Radio)getControl("xmlcheckoutall", com.documentum.web.form.control.Radio.class);
            if(radioAllDescs.isVisible() && radioAllDescs.getValue())
                bEditDescendants = true;
        } else
        if(radioDescs.isVisible() && radioDescs.getValue())
            bEditDescendants = true;
        return bEditDescendants;
    }

    public String getObjectId()
    {
        return m_strObjectId;
    }

    public boolean onCommitChanges()
    {
        return getIsValid();
    }

    private String m_strObjectId;
    private boolean m_bIsVirtualXmlDoc;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/edit/Edit.class


	TOTAL TIME: 127236 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/