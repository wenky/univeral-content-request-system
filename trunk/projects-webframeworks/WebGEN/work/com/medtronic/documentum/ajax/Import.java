package com.medtronic.documentum.ajax;

public class Import {
	
	// UCF Imports involve several AJAX calls, I think.
	// 1. Initiate transfers, UCFSessionmanager, and HttpSession
	// 2. Follow-up calls or special applet to get status updates? Go file by file with single Import service requests? 
	// 3. Close down UCFSessionManager/HttpSession
	
	// we will try for a single file import call - I think the WDK is doing a lot of multithreading, but does AJAX need to do that? I think we can go single-threaded on the imports...
	
	public void performUcfImport(Map ajaxparams)
	{
		
		// extract and/or assert input parameters
		String targetFolderId = (String)ajaxparams.get("folderid");
		String localfilename;
		String localmachine;
		String metadata;
		
		
		
		// get DfSession
		IDfSession session = null;
		
		// get UcfSession from UcfSessionManager
		// -- WDK instantiates a UcfContentTransport object for an import operation. UcfContentTransport doesn't seem to necessary, and it seems to 
		//    just create a new UcfSessionManager from a factory method call in UcfTransportManager. So we may be avoiding thread hell...
		NewUcfSessionManager ucfSessionMgr = NewUcfTransportManager.getManager().getSessionManager();
		IServerSession ucfSession = ucfSessionMgr.getSession();		
		// hopefully we can use those to bypass any unnecessary asynchronous threading, at least for now...

		// do DFC object creation (the deconstruction seems to indicate basic, simple object instantiations, nothing too fancy...)
		IDfImportOperation dfOp = DfcUtils.getClientX().getImportOperation(); // IDfOperation is the generic class
		IDfImportPackage pkg = DfcUtils.getClientX().getContentPackageFactory().newImportPackage(); // IDfPackage is the generic class
		pkg.setDestinationFolderId(targetFolderId);
		// from the Inbound service that underlies the Import service
        IDfInboundPackage pkg = (IDfInboundPackage)getPackage();
        if(getClientInfo().isPlatform(ClientInfo.MACOS))
            pkg.setMacResourceForkOption(2);
        else
            pkg.setMacResourceForkOption(1);
		// PRETTY sure that in multiple-file import processes, there is still one "package" that wraps all the files to import. we're going to do it one file at a time for now...
		
		// ImportProcessor stuff...
        
        // invoke operation.execute
        
        //what happens now?
        
        //cleanup...
		

			
	}
	
	
	
	
	
	public void importFiles(Map params)
	{
		// get DfSession with user/pass/base
		
		// get UcfSession (IServerSession) from UcfSessionManager
		
		// prep the DfOperation... (from ImportService.createOperation())
		// -- do we even need the IServiceOperation wrappers/layers?
		// -- -- ANSWER: HELL, NO
		//IServiceOperation importoperation = new DFCOperationSupport(DfcUtils.getClientX().getImportOperation());
		IDfImportOperation dfOp = DfcUtils.getClientX().getImportOperation(); // IDfOperation is the generic class
		
		// get the package - whatever that is
		IDfImportPackage pkg = DfcUtils.getClientX().getContentPackageFactory().newImportPackage(); // IDfPackage is the generic class
		
		// prep the package (from what I can tell from the various code locations --> likely this involves the ImportService and the ServiceProcessors set by MrcsImportContentUCF
		// from Import Service
		pkg.setDestinationFolderId();
		// from the Inbound service that underlies the Import service
        IDfInboundPackage pkg = (IDfInboundPackage)getPackage();
        if(getClientInfo().isPlatform(ClientInfo.MACOS))
            pkg.setMacResourceForkOption(2);
        else
            pkg.setMacResourceForkOption(1);
        // from ContentTransferService, something is going on with the array of Service processors: for(int i = 0; i < procs.length; i++) procs[i].preProcess(getPackage());
        // setup of ImportProcessor in MrcsImportContentUCF, and lots of the values of the accessors for MrcsImportContentUCF comps are set by MrcsImportContainerUCF.initContainedComponents()
        ImportProcessor proc = (ImportProcessor)sp;
        if(getLocalFilePath() != null) 
            proc.setServerFilePath(getLocalFilePath()); //?? don't know what this is doing...
        else
            proc.setClientFilePath(getFilePathSelection()); //?? don't know what this is doing...
        proc.setDirectory(false);
        proc.setClientParentPath(getParentPathSelection());
        proc.setNewObjectFormat(getFormatSelection());
        proc.setNewObjectName(getObjectNameSelection());
        proc.setNewObjectType(getTypeSelection());
        proc.setNewObjectAttributes(getObjectAttributes());
        
        
        // TODO - ImportProcessor does ALOT, with bunches of DFC calls, but a lot of it is probably for VDMs, XML, etc
        IDfClientServerFile csfile = DfcUtils.getClientX().getContentPackageFactory().newClientServerFile();
        IDfContentPackageFactory pfactory = DfcUtils.getClientX().getContentPackageFactory();
        String filePath = getClientFilePath();
        if(filePath != null && filePath.length() > 0)
        {
            csfile.setClientFile(pfactory.newClientFile(filePath));
        } else
        {
            filePath = getServerFilePath();
            csfile.setServerFile(pfactory.newServerFile(filePath));
        }
        
        
        
        
        
        
        // add package to operation - from ContentTransferService.execute()
        dfOp.add(pkg);
        
        // the the oprators each get executed...
        


		
	}
	
	//-------------------------
	//---interface impl mappings for UCF stuff (ahh, the dark side of abstraction/plugins) FOR IMPORT
	
	// ICommManager             - UCFCommManager            - com.documentum.ucf.server.transport.impl        - ucf-server-impl.jar
	
	// IServerSession           - ServerSession             - com.documentum.ucf.server.transport.impl        - ucf-server-impl.jar
	
	// IPackageProcessorFactory - PackageProcessorFactory   - com.documentum.ucf.server.contentpackage.impl   - ucf-server-impl.jar
	
	// IPackageProcessor        - ImportPackageProcessor    - com.documentum.ucf.server.contentpackage.impl   - ucf-server-impl.jar
	
	// IServiceProcessor        - ImportProcessor           - com.documentum.web.contentxfer.impl             - WDKClasses (eliminate/recode)
	
	// IServiceContext          - ServiceInstanceContextFacade - com.documentum.web.contentxfer               - WDKClasses (eliminate/recode)
	//    ::Nothing of note - just an accessor to get assigned IDfSession, IServiceOperation, ContentTransferService, ClientInfo
	//    ::See ContentTransferService abstract class for the inner class that implements this. ?only impl found?
	
	// these next two classes are COMPLETELY useless wrappers around IDfOperation objects
	// IServiceOperation        - ServiceOperationSupport   - com.documentum.web.contentxfer.impl             - WDKClasses (eliminate/recode)
	//                          - DFCOperationSupport       - com.documentum.web.contentxfer.impl             - WDKClasses (eliminate/recode)
	
	// For WDK independence, we'll need to reimplement:
	// - MrcsImportContentUCF, MrcsImportContainerUCF, ImportProcessor, UcfContentTransport, UcfSessionManager (few changes though...), UcfTransportManager
	//   - MrcsImportContentUCF and MrcsImportContainerUCF be replaced by JS/DHTML screens and AJAX calls as needed. 
	//   - UcfSessionmanager and UcfTransportManager will be minimally changed to strip WDK dependence. 
	//   - the rest will probably be completely stripped away/compacted to singular service-type calls, since they don't seem to actually do anything
	//   - the goal is to achieve UCF transfers on the necessary operations without all this abstraction crap imposed by WDK since they want to be transport-agnostic.
	// - basically, anything residing in WDKClasses will need reimplementation.
	// - we want to be left with DFC-pure stuff, perhaps minimal stuff for accessing UCF Configuration files on the server. 
	// - we want to avoid tomcat sessions if possible and maintain stateless interactions
    
	void ExecuteService() // from ContentTransferService.execute
	{
		
		// preexecute for the service
        IDfOperation dfOp = getDfOperation();
        if(dfOp != null)
        {
            dfOp.setOperationMonitor(getDfOperationMonitor());
            dfOp.setSession(getDfSession());
            if(dfOp instanceof IDfExportOperation)
                ((IDfExportOperation)dfOp).setAcsTransferPreferences(m_acsSessionPreferences);
            else
            if(dfOp instanceof IDfCheckoutOperation)
                ((IDfCheckoutOperation)dfOp).setAcsTransferPreferences(m_acsSessionPreferences);
        }
        // end preexecute
        
        ProgressTracker tracker = new ProgressTracker(getProgressProviders()); // do we care?
        registerAsPromptListener(getPromptEventProviders()); // do we care?
        
        IServiceContext context = getServiceContext();  // pointless wrapper class I think...
        if(transport != null)
            transport.setContext(context);
        
        IServiceProcessor procs[] = null;                          // iterate through the processors
        List listProcs = getServiceProcessors();
        if(listProcs != null && !listProcs.isEmpty())
        {
            procs = new IServiceProcessor[listProcs.size()];
            listProcs.toArray(procs);
            for(int i = 0; i < procs.length; i++)
                procs[i].setContext(context);

            if(getPackage() != null)
            {
                for(int i = 0; i < procs.length; i++)
                    procs[i].preProcess(getPackage()); // add the SAME package to each of them? Really?

            }
        }
        
        // preprocess UCF transport (from UcfContentTransport)
        IPackageProcessor pproc = getPackageProcessor();  // <-- see UcfContentTransport.createPackageProcessor()
        if(pproc != null)
        {
            getUcfSession().setNotificationMonitor(getProgressMonitor()); // do we care?
            pproc.preProcess();  // <-- ucf server impl class call. I think if the UCF server config file is okay, we'll be okay here...
        }
        // preprocess
        
        if(procs != null)
        {
            if(getPackage() != null)
            {
                IDfOperation dfOp = getDfOperation();
                if(dfOp != null)
                    dfOp.add(getPackage());
            }
            IServiceOperation op = context.getOperation();
            boolean suceeded = false;
            if(op != null)
            {
                for(int i = 0; i < procs.length; i++)
                    procs[i].preProcess(op);            // <-- ImportProcessor.preProcess, seems to add the dirty details of the ImportFiles set up in MrcsImportContentUcf/MrcsImportContainerUcf

                suceeded = op.execute();
            }
            if(suceeded && getPackage() != null)
            {
                for(int i = 0; i < procs.length; i++)
                    procs[i].postProcess(getPackage());

            }
            if(transport != null)
                transport.postProcess();
            if(suceeded && op != null)
            {
                for(int i = 0; i < procs.length; i++)
                    procs[i].postProcess(op);

            }
            if(!suceeded)
                handleOperationError();
            else
                postExecute();

		
	}

}
