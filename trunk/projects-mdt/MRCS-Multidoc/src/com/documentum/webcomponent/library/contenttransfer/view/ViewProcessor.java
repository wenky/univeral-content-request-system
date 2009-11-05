/*jadclipse*/// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ViewProcessor.java

package com.documentum.webcomponent.library.contenttransfer.view;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFormat;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.documentum.operations.contentpackage.IDfContentPackage;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.formext.docbase.ObjectCacheUtil;
import com.documentum.web.formext.preferredrenditions.PreferredRenditionsService;

public class ViewProcessor extends com.documentum.web.contentxfer.impl.ViewProcessor
{

    public ViewProcessor()
    {
    }
	
    public void preProcess(IDfContentPackage p)
        throws ContentTransferException
    {
        try
        {
            String primaryFormatName = getFormat();
            if(primaryFormatName == null || primaryFormatName.length() == 0)
            {
                IDfId id = new DfId(getObjectId());
                if(id.isObjectId())
                {
                    IDfSysObject sysObj = (IDfSysObject)ObjectCacheUtil.getObject(getContext().getDfSession(), getObjectId());
                    IDfFormat format = sysObj.getFormat();
                    if(format != null)
                    {
                        primaryFormatName = format.getName();
                        com.documentum.web.formext.preferredrenditions.PreferredRenditionsService.ViewRenditionSetting setting = m_prefService.getViewRenditionSetting(sysObj.getType().getName(), primaryFormatName);
                        if(setting != null)
                        {
                            String renditionFormat = setting.getRenditionFormat();
                            if(renditionFormat != null && renditionFormat.length() > 0 && !renditionFormat.equals(primaryFormatName) && isFormatValid(sysObj, renditionFormat))
                                setFormat(renditionFormat);
                        }
                    }
                }
            }
            super.preProcess(p);
        }
        catch(DfException e)
        {
            throw new ContentTransferException(e);
        }
    }

    private boolean isFormatValid(IDfSysObject sysObj, String renditionFormat)
        throws DfException
    {
        boolean exists;
        IDfCollection coll = null;
        exists = false;
    	try { 
	        coll = sysObj.getRenditions(null);
	        do
	        {
	            if(!coll.next())
	                break;
	            if(!coll.getString("full_format").equals(renditionFormat))
	                continue;
	            exists = true;
	            break;
	        } while(true);
	        coll.close();
	        return exists;
    	} catch (DfException exception) {
    		if (coll != null)coll.close();
    		throw exception;
    	}
    }

    private final PreferredRenditionsService m_prefService = PreferredRenditionsService.getInstance();
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/view/ViewProcessor.class


	TOTAL TIME: 0 ms


	JAD REPORTED MESSAGES/ERRORS:

Overlapped try statements detected. Not all exception handlers will be resolved in the method isFormatValid
Couldn't fully decompile method isFormatValid
Couldn't resolve all exception handlers in method isFormatValid

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/