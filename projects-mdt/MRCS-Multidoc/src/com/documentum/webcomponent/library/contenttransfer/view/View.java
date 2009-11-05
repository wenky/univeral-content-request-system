/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   View.java

package com.documentum.webcomponent.library.contenttransfer.view;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.contentxfer.impl.ViewProcessor;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.webcomponent.library.contenttransfer.export.Export;

public class View extends Export
{

    public View()
    {
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
    }

    protected void initFromSysobject(IDfSysObject obj)
    {
        super.initFromSysobject(obj);
        try
        {
            String folder = getParentFolder();
            if(folder == null || folder.length() == 0)
            {
                IDfSession dfsession = getDfSession();
                if(!obj.getHasFolder())
                {
                    IDfId chronId = obj.getChronicleId();
                    obj = (IDfSysObject)dfsession.getObjectByQualification("dm_sysobject where i_chronicle_id='" + chronId.toString() + "'");
                }
                if(obj.getFolderIdCount() > 0)
                {
                    String strFolderPath = FolderUtil.getPrimaryFolderPath(obj.getObjectId().getId());
                    if(strFolderPath != null)
                        setParentFolder(strFolderPath);
                }
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    public IServiceProcessor getServiceProcessor()
    {
        IServiceProcessor sp = super.getServiceProcessor();
        if(sp instanceof ViewProcessor)
        {
            ViewProcessor proc = (ViewProcessor)sp;
            proc.setParentFolder(getParentFolder());
        }
        return sp;
    }

    protected String getParentFolder()
    {
        return parentFolder;
    }

    protected void setParentFolder(String parentFolder)
    {
        this.parentFolder = parentFolder;
    }

    private String parentFolder;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/view/View.class


	TOTAL TIME: 15 ms


	JAD REPORTED MESSAGES/ERRORS:


	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/