/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   UcfCheckin.java

package com.documentum.webcomponent.library.contenttransfer.checkin;

import com.documentum.operations.contentpackage.IDfClientServerFile;
import com.documentum.operations.contentpackage.IDfContentPackageFactory;
import com.documentum.ucf.common.UCFException;
import com.documentum.ucf.common.contentregistry.IRegistryService;
import com.documentum.ucf.server.transport.IServerSession;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.contentxfer.ucf.UcfSessionManager;
import com.documentum.web.contentxfer.ucf.UcfTransportManager;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.validator.RequiredFieldValidator;
import com.documentum.web.util.DfcUtils;
import com.documentum.webcomponent.common.WebComponentErrorService;

// Referenced classes of package com.documentum.webcomponent.library.contenttransfer.checkin:
//            Checkin, CheckinProcessor

public class UcfCheckin extends Checkin
{

    public UcfCheckin()
    {
    }

    protected void initAdditionalOptionsControls()
    {
        super.initAdditionalOptionsControls();
        showHideOptions(false);
        if(isObjectXmlDoc() && isObjectVirtualDoc() && isXmlVdmCheckedoutWithInlineDescendents())
        {
            Checkbox checkbox = getWithDescendantsCheckboxControl(true);
            checkbox.setValue(true);
            checkbox.setEnabled(false);
        }
    }

    public void onRender()
    {
        super.onRender();
        Checkbox keepLocal;
        if((keepLocal = getKeepLocalFileControl(false)) != null && getCheckinFromFileSelection())
        {
            keepLocal.setEnabled(false);
            keepLocal.setValue(true);
        }
    }

    public void updateStateFromRequest()
    {
        super.updateStateFromRequest();
        RequiredFieldValidator validator;
        Checkbox checkbox;
        if((validator = getValidateFilebrowseControl(false)) != null && (checkbox = getCheckinFromFileCheckboxControl(false)) != null)
        {
            checkbox.updateStateFromRequest();
            validator.setEnabled(getCheckinFromFileSelection());
        }
    }

    public IServiceProcessor getServiceProcessor()
    {
        IServiceProcessor sp = super.getServiceProcessor();
        if(sp instanceof CheckinProcessor)
        {
            CheckinProcessor proc = (CheckinProcessor)sp;
            if(getCheckinFromFileSelection())
            {
                IDfContentPackageFactory pfactory = DfcUtils.getClientX().getContentPackageFactory();
                IDfClientServerFile csfile = pfactory.newClientServerFile();
                String checkinFromFilePath = getCheckinFromFilePathSelection();
                if(checkinFromFilePath != null && checkinFromFilePath.length() > 0)
                {
                    csfile.setClientFile(pfactory.newClientFile(checkinFromFilePath));
                    proc.setFile(csfile);
                }
            } else
            if(isObjectContentless())
                proc.setFormat(null);
        }
        return sp;
    }

    public boolean onCommitChanges()
    {
        if(getCheckinFromFileSelection())
        {
            String checkinFromFilePath = getCheckinFromFilePathSelection();
            if(!isClientPathValid(checkinFromFilePath))
            {
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_FILENAME_INVALID", null);
                return false;
            }
        }
        return super.onCommitChanges();
    }

    private boolean isXmlVdmCheckedoutWithInlineDescendents()
    {
        UcfSessionManager sessionManager = null;
        IServerSession ucfsession = null;
        boolean coWithInlineDesc;
        try { 
	        sessionManager = UcfTransportManager.getManager().getSessionManager();
	        ucfsession = sessionManager.getSession();
	        try
	        {
	            IRegistryService regService = ucfsession.getClientProxyFactory().getRegistryService();
	            coWithInlineDesc = regService.isCheckedOutWithInlineDescendants(getObjectId());
	        }
	        catch(UCFException e)
	        {
	            throw new WrapperRuntimeException(e);
	        }
	        if(ucfsession != null)
	            sessionManager.release(ucfsession);
        } catch (Exception exception) {
	        if(ucfsession != null)
	            sessionManager.release(ucfsession);
	        throw new WrapperRuntimeException(exception);
        }
        return coWithInlineDesc;
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/checkin/UcfCheckin.class


	TOTAL TIME: 0 ms


	JAD REPORTED MESSAGES/ERRORS:

Overlapped try statements detected. Not all exception handlers will be resolved in the method isXmlVdmCheckedoutWithInlineDescendents
Couldn't fully decompile method isXmlVdmCheckedoutWithInlineDescendents
Couldn't resolve all exception handlers in method isXmlVdmCheckedoutWithInlineDescendents

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/