// Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) 
// Source File Name:   UcfContentTransport.java

import com.documentum.operations.IDfCancelCheckoutOperation;
import com.documentum.operations.IDfCheckinOperation;
import com.documentum.operations.IDfCheckoutOperation;
import com.documentum.operations.IDfExportOperation;
import com.documentum.operations.IDfImportOperation;
import com.documentum.operations.contentpackage.IDfCancelCheckoutPackage;
import com.documentum.operations.contentpackage.IDfCheckinPackage;
import com.documentum.operations.contentpackage.IDfCheckoutPackage;
import com.documentum.operations.contentpackage.IDfExportPackage;
import com.documentum.operations.contentpackage.IDfImportPackage;
import com.documentum.ucf.common.UCFException;
import com.documentum.ucf.common.notification.INotificationMonitor;
import com.documentum.ucf.server.contentpackage.IPackageProcessor;
import com.documentum.ucf.server.contentpackage.IPackageProcessorFactory;
import com.documentum.ucf.server.transport.IServerSession;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.ContentTransportSupport;
import com.documentum.web.contentxfer.IPromptEventListener;
import com.documentum.web.contentxfer.IPromptEventProvider;
import com.documentum.web.contentxfer.IServiceOperation;
import com.documentum.web.contentxfer.IStepProgressListener;
import com.documentum.web.contentxfer.IStepProgressProvider;

// Referenced classes of package com.documentum.web.contentxfer.ucf:
//            NotificationMonitorSupport, UcfTransportManager, UcfSessionManager

public class UcfContentTransport extends ContentTransportSupport
    implements IStepProgressProvider, IPromptEventProvider
{

    public UcfContentTransport()
    {
        m_monitorSupport = new NotificationMonitorSupport(this, this);
        IServerSession session = getUcfSession();
    }

    public void preProcess()
        throws ContentTransferException
    {
        try
        {
            IPackageProcessor pproc = getPackageProcessor();
            if(pproc != null)
            {
                getUcfSession().setNotificationMonitor(getProgressMonitor());
                pproc.preProcess();
            }
        }
        catch(UCFException e)
        {
            throw new ContentTransferException(e);
        }
    }

    public void postProcess()
        throws ContentTransferException
    {
        try
        {
            IPackageProcessor pproc = getPackageProcessor();
            if(pproc != null)
            {
                getUcfSession().setNotificationMonitor(getProgressMonitor());
                pproc.postProcess();
            }
        }
        catch(UCFException e)
        {
            throw new ContentTransferException(e);
        }
    }

    public void cleanup()
    {
        releaseUcfSession();
    }

    protected IPackageProcessor getPackageProcessor()
        throws ContentTransferException
    {
        if(m_processor == null)
            m_processor = createPackageProcessor();
        return m_processor;
    }

    protected IPackageProcessor createPackageProcessor()
        throws ContentTransferException
    {
        IServiceOperation sop = getContext().getOperation();
        if(sop != null)
        {
            IServerSession ucfSession = getUcfSession();
            if(ucfSession == null)
                throw new ContentTransferException("Failed to obtain UCF session");
            com.documentum.operations.IDfOperation dfOp = sop.getDfOperation();
            com.documentum.operations.contentpackage.IDfContentPackage pkg = getPackage();
            if(dfOp != null && pkg != null)
            {
                IPackageProcessorFactory pf = ucfSession.getPackageProcessorFactory();
                if(dfOp instanceof IDfCheckoutOperation)
                    return pf.newCheckoutPackageProcessor((IDfCheckoutOperation)dfOp, (IDfCheckoutPackage)pkg);
                if(dfOp instanceof IDfCheckinOperation)
                    return pf.newCheckinPackageProcessor((IDfCheckinOperation)dfOp, (IDfCheckinPackage)pkg);
                if(dfOp instanceof IDfImportOperation)
                    return pf.newImportPackageProcessor((IDfImportOperation)dfOp, (IDfImportPackage)pkg);
                if(dfOp instanceof IDfExportOperation)
                    return pf.newExportPackageProcessor((IDfExportOperation)dfOp, (IDfExportPackage)pkg);
                if(dfOp instanceof IDfCancelCheckoutOperation)
                    return pf.newCancelCheckoutPackageProcessor((IDfCancelCheckoutOperation)dfOp, (IDfCancelCheckoutPackage)pkg);
            }
        }
        return null;
    }

    protected IServerSession getUcfSession()
    {
        if(m_ucfSession == null && m_ucfSessionManager != null)
            m_ucfSession = m_ucfSessionManager.getSession();
        return m_ucfSession;
    }

    private void releaseUcfSession()
    {
        if(m_ucfSession != null)
        {
            m_ucfSession.setNotificationMonitor(null);
            m_ucfSessionManager.release(m_ucfSession);
            m_ucfSession = null;
        }
    }

    protected void finalize()
        throws Throwable
    {
    	try { 
	        releaseUcfSession();
	        super.finalize();
    	} catch (Exception exception) {
    		super.finalize();
    		throw exception;
    	}
    }

    public void setStepProgressListener(IStepProgressListener listener)
    {
        m_monitorSupport.setStepProgressListener(listener);
    }

    public void setPromptEventListener(IPromptEventListener listener)
    {
        m_monitorSupport.setPromptEventListener(listener);
    }

    public int getStepCount()
    {
        return m_monitorSupport.getStepCount();
    }

    protected INotificationMonitor getProgressMonitor()
    {
        return m_monitorSupport;
    }

    private IPackageProcessor m_processor;
    private final UcfSessionManager m_ucfSessionManager = UcfTransportManager.getManager().getSessionManager();
    private IServerSession m_ucfSession;
    private NotificationMonitorSupport m_monitorSupport;
}
