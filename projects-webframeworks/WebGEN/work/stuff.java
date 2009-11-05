
public class stuff {
	
	void From_ContentTransferServiceContainer_WDK_Component()
	{
		//-------------
		// this is a denormalized/deobfuscated call flow for the invokeService call made from MrcsImportContainerUCF.onOk()
		//-------------
		
		// for import it should be: com.documentum.web.contentxfer.impl.ImportService
        Class serviceClass = getServiceClass();                                         //<-- class is set somewhere, 
        service = (ContentTransferService)serviceClass.newInstance();                   //<-- simple newinstance, not a factory or singleton...
        
        // transport class names -- 
        // - Http: com.documentum.web.contentxfer.http.HttpContentTransport
        // - UCF:  com.documentum.web.contentxfer.ucf.UcfContentTransport
        Class transportClass = getTransportClass();
        IContentTransport transport = (IContentTransport)transportClass.newInstance();  //<-- simple newinstance here too...
        service.setTransport(transport);                                        //<-- abstracted transport (we will use UCF)
        
        service.setServiceProcessors(getServiceProcessors());
        
        // MrcsImportContentUCF components use this code to init the Import Service Processor (1 sp object per component/file) 
//	     IServiceProcessor sp = createServiceProcessor();
//	     if(sp instanceof ImportProcessor)
//	     {
//	         ImportProcessor proc = (ImportProcessor)sp;
//	         if(getLocalFilePath() != null)
//	             proc.setServerFilePath(getLocalFilePath());
//	         else
//	             proc.setClientFilePath(getFilePathSelection());
//	         proc.setDirectory(false);
//	         proc.setClientParentPath(getParentPathSelection());
//	         proc.setNewObjectFormat(getFormatSelection());
//	         proc.setNewObjectName(getObjectNameSelection());
//	         proc.setNewObjectType(getTypeSelection());
//	         proc.setNewObjectAttributes(getObjectAttributes());
//	     }
//	     return sp;        
        
        // this next code may be the actual physical file transfer object/procedure/service/code/blah...
        JobAdapter job = new JobAdapter(service, getString("MSG_PROGRESS_TITLE"), getNlsClass(), LocaleService.getLocale());
        // TODO -- check out AsyncJobManager and AsyncJobManager.executeJob
        
        long maxinactive = getPageContext().getSession().getMaxInactiveInterval() * 1000; // presumably this is a constant/XML config param
        job.setRequestInputTimeout(maxinactive);
        setServiceJobWrapper(job); // I think this back-points the service object to the job object (the job already has a ref to the svc)
                                   // it is a simple bean-style set of a property
        String jobId = job.getId(); // uses system.millis for a crude one. no biggie here. Don't think this has anything to do with UCFSessionId
        job.hold(getPageContext().getSession());  // this is a plain old HttpSession -- no WDK needed?
        
        // here's the WDK component jump to the monitor. So presumably, the above code TRIGGERED THE UPLOAD!
        // hmmm, actually, I think the job.hold HOLDs the transfer until the monitor is in place (that's lame) and it is triggered later
        ArgumentList args = new ArgumentList();
        args.add("jobId", jobId);
        args.add("pending", "true");
        setComponentNested("jobprogressmonitor", args, getContext(), new FormActionReturnListener(this, "onReturnFromProgress"));
		
	}
	
	// For UCF handling for me, I think we need to:
	// - prep Package Processor for the desired operation, like in UcfContentTransport (see below)
	// - 
	protected IPackageProcessor createPackageProcessor() throws ContentTransferException
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

	
	
	

}
