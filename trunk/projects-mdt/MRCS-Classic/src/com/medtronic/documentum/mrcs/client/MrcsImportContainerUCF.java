/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   ImportContentContainer.java

package com.medtronic.documentum.mrcs.client;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.collections.set.ListOrderedSet;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.ContentTransferService;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.PromptEvent;
import com.documentum.web.contentxfer.impl.ImportService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.FormActionReturnListener;
import com.documentum.web.form.Util;
import com.documentum.web.form.control.Button;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Tab;
import com.documentum.web.form.control.Tabbar;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.fileselector.FileSelector;
import com.documentum.web.form.control.fileselector.IFile;
import com.documentum.web.formext.common.BackDetector;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.control.docbase.DocbaseFolderTreeNode;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.formext.control.docbase.MultiDocbaseTree;
import com.documentum.web.formext.control.validator.FolderSelectionValidator;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.environment.preferences.searchsources.SearchSourceService;
import com.documentum.webcomponent.library.contenttransfer.ContentTransferServiceContainer;
import com.documentum.webcomponent.library.messages.MessageService;

// Referenced classes of package com.documentum.webcomponent.library.contenttransfer.importcontent:
//            ImportContent

public class MrcsImportContainerUCF extends ContentTransferServiceContainer
{
    private static class DirsFirstComparator
        implements Comparator
    {

        public int compare(Object o1, Object o2)
        {
            ImportFile f1 = (ImportFile)o1;
            ImportFile f2 = (ImportFile)o2;
            if(f1.isDirectory())
                return f2.isDirectory() ? 0 : -1;
            else
                return f2.isDirectory() ? 1 : 0;
        }

        private DirsFirstComparator()
        {
        }

    }

    public static class ImportFile
    {

        private static String parseFileName(String filePath)
        {
            String name = filePath;
            if(filePath != null && filePath.length() > 0)
            {
                int sepIndex = filePath.charAt(0) != '/' ? filePath.lastIndexOf('\\') : filePath.lastIndexOf('/');
                if(sepIndex != -1)
                    name = filePath.substring(sepIndex + 1, filePath.length());
            }
            return name;
        }

        public String getFileName()
        {
            return fileName;
        }

        public String getFilePath()
        {
            return filePath;
        }

        public String getLocalFilePath()
        {
            return localFilePath;
        }

        public String getParentPath()
        {
            return parentPath;
        }

        public boolean isDirectory()
        {
            return directory;
        }

        public String getFormat()
        {
            return format;
        }

        public ArgumentList getAttributes()
        {
            return attributes;
        }

        private String fileName;
        private String parentPath;
        private String filePath;
        private String localFilePath;
        private boolean directory;
        private String format;
        private ArgumentList attributes;

        public ImportFile(String name, String filePath, String localFilePath, String parentFilePath, boolean dir, ArgumentList attributes, String format)
        {
            directory = false;
            this.filePath = filePath;
            fileName = name == null || name.length() <= 0 ? parseFileName(filePath) : name;
            this.localFilePath = localFilePath;
            parentPath = parentFilePath;
            directory = dir;
            this.attributes = attributes;
            this.format = format;
        }

        public ImportFile(String name, String filePath, String localFilePath, String parentFilePath, boolean dir)
        {
            this(name, filePath, localFilePath, parentFilePath, dir, null, null);
        }

        public ImportFile(String filePath, String localFilePath)
        {
            this(null, filePath, localFilePath, null, false);
        }
    }


    public MrcsImportContainerUCF()
    {
        m_setFiles = new HashSet();
        m_compStartedAutoCommit = -1;
        m_fileFolderMix = false;
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        
        /*-CONFIG-*/String m = "onInit - ";        

        // MRCS: detect the gftype and mrcsapp of this folder (it must be a m_mrcs_folder...)
        try { 
            String objectId = args.get("objectId");
            if(objectId != null && objectId.length() > 0)
            {
                setFolderId(objectId);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"objectid: "+getFolderId(),null,null);
	            IDfSession session = getDfSession();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"composing idfid",null,null);
	            IDfId newid = new DfId(getFolderId());
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up folder object",null,null);
	            IDfFolder parentfolder = (IDfFolder)session.getObject(newid);
	
	            // check permission
	            int permission = parentfolder.getPermit();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"user's permission level: "+permission,null,null);
	            if (IDfACL.DF_PERMIT_WRITE > permission)
	            {
	                /*-ERROR-*/DfLogger.getRootLogger().error(m+"Insufficient permit level to import MRCS documents in this folder");
	                Exception e = new Exception("Insufficient permit level to import MRCS documents in this folder");
	                setReturnError("MSG_MRCS_NO_WRITE_PERMIT", null, e);
	                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_NO_WRITE_PERMIT", e);   
	                setComponentReturn();
	                return;               
	            }
	            
	            if (parentfolder.hasAttr("mrcs_application"))
	            {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"current folder is a mrcs folder!",null,null);
	                m_mrcsapp = parentfolder.getString("mrcs_application");
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"mrcsapp: "+m_mrcsapp,null,null);
	                m_gftype = parentfolder.getString("mrcs_config");
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"gftype: "+m_gftype,null,null);
	                // else should we throw an exception???
	            }
            }
        } catch (Exception dfe) {
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"error in getting MRCS state data from folder sysobject");
            setReturnError("MSG_DFC_ERROR", null, dfe);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", dfe);
            setComponentReturn();
            return;
        }
        
        getPreviousButtonControl(true).setEnabled(false); // no previous button in MRCS Import...
        
        String filePathArray[] = args.getValues("filePath");
        String parentPathArray[] = args.getValues("parentPath");
        String isDirectoryArray[] = args.getValues("isDirectory");
        if(filePathArray != null)
        {
            if(parentPathArray == null || isDirectoryArray == null)
                throw new IllegalArgumentException("filePath argument can not be specified without specifying parentPath and isDirectory arguments!");
            if(filePathArray.length != parentPathArray.length || filePathArray.length != isDirectoryArray.length)
                throw new IllegalArgumentException("The number of values specified for filePath, parentPath and isDirectory arguments must match!");
            int itemsToImport = filePathArray.length;
            ArrayList filesList = new ArrayList(itemsToImport);
            for(int itemIndex = 0; itemIndex < itemsToImport; itemIndex++)
            {
                String filePath = filePathArray[itemIndex];
                String parentPath = parentPathArray[itemIndex];
                if(parentPath != null && parentPath.length() == 0)
                    parentPath = null;
                boolean isDirectory = Boolean.valueOf(isDirectoryArray[itemIndex]).booleanValue();
                String initialFormat = args.get("format");
                if(isDirectory)
                    initialFormat = "";
                ImportFile importFile = new ImportFile(null, filePath, null, parentPath, isDirectory, null, initialFormat);
                filesList.add(importFile);
            }

            setFilesToImport(filesList);
        }
        SearchSourceService searchSourceService = SearchSourceService.getInstance();
        MultiDocbaseTree repositoriesSelTree = (MultiDocbaseTree)getControl("folderlocator", com.documentum.web.formext.control.docbase.MultiDocbaseTree.class);
        if(repositoriesSelTree != null && searchSourceService != null)
            repositoriesSelTree.setDocbaseList(searchSourceService.getAvailableSearchSources());
        String objectId = args.get("objectId");
        if(objectId != null && objectId.length() > 0)
            setFolderId(objectId);
        if(getFolderId() == null || getFolderId().length() == 0)
        {
            setComponentPage("folderselection");
        } 
        else if(getFilesToImport() == null || getFilesToImport().size() == 0)
        {
            setComponentPage("fileselection");
            Control nextbutton = getControl("next", com.documentum.web.form.control.Button.class);
            nextbutton.setEnabled(true);
            Control okbutton = getControl("ok", com.documentum.web.form.control.Button.class);
            okbutton.setEnabled(false);
            
        } else {
            initContainedComponents();
            setComponentPage("containerstart");
        }
    }

    public void onOk(Control button, ArgumentList args)
    {
        String previousCurrentDocbase;
        m_pageStartedOnOk = getComponentPage();
        if(!inAutoCommit())
        {
            String strCurPage = getComponentPage();
            if(!strCurPage.equals("containerstart"))
                if(onNextPage())
                {
                    strCurPage = getComponentPage();
                    if(!strCurPage.equals("containerstart"))
                        return;
                } else
                {
                    return;
                }
        }
        validate();
        if(!getIsValid() || !canCommitChanges() || !canCommitComponentsChanges() || !onCommitChanges())
            /* break MISSING_BLOCK_LABEL_144 */;
        else { 
	        previousCurrentDocbase = null;
	        if(getTargetDocbaseName() != null)
	        {
	            previousCurrentDocbase = SessionManagerHttpBinding.getCurrentDocbase();
	            SessionManagerHttpBinding.setCurrentDocbase(getTargetDocbaseName());
	        }
	        try
	        {
	            invokeService();

	        }
	        catch(ContentTransferException e)
	        {
	            throw new WrapperRuntimeException(e);
	        }
	        if(previousCurrentDocbase != null)
	            SessionManagerHttpBinding.setCurrentDocbase(previousCurrentDocbase);
        }
        //break MISSING_BLOCK_LABEL_144;
        //Exception exception;
        //exception;
        //if(previousCurrentDocbase != null)
        //    SessionManagerHttpBinding.setCurrentDocbase(previousCurrentDocbase);
        //throw exception;
    }

    private boolean canCommitComponentsChanges()
    {
        boolean isValid = true;
        List components = getContainedComponents();
        for(int i = 0; i < components.size(); i++)
        {
            Component component = (Component)components.get(i);
            isValid = component.canCommitChanges();
            if(!isValid)
            {
                if(inAutoCommit())
                    stopAutoCommit();
                setCurrentComponent(i);
                return isValid;
            }
        }

        return isValid;
    }

    public String[] getEventNames()
    {
        return Util.concatarray(super.getEventNames(), new String[] {
            "onchangeobjecttype"
        });
    }

    public String getEventHandlerMethod(String strEvent)
    {
        String strMethod = null;
        if(strEvent.equals("onchangeobjecttype"))
            strMethod = "onChangeObjectType";
        else
            strMethod = super.getEventHandlerMethod(strEvent);
        return strMethod;
    }

    public Control getEventHandler(String strEvent)
    {
        Control handler = null;
        if(strEvent.equals("onchangeobjecttype"))
            handler = this;
        else
            handler = super.getEventHandler(strEvent);
        return handler;
    }

    public void onChangeObjectType(Control control, ArgumentList arg)
    {
        String strType = arg.get("type");
        String strObjectName = arg.get("objectName");
        String strFormat = arg.get("format");
        ArgumentList containedArg = getContainedComponentArgs();
        containedArg.replace("docbaseType", strType);
        containedArg.replace("objectName", strObjectName);
        containedArg.replace("format", strFormat);
        Component comp = getContainedComponent();
        remove(comp);
        
        Component currentcomp = getContainedComponent();
        List components = this.getContainedComponents();
        int i=1;
        boolean initialized = currentcomp.isInitialized();
        //initCurrentComponent();
    }

    public void onControlInitialized(Form form, Control control)
    {
        String strControlName = control.getName();
        String strInitialValue = null;
        if(!"formatList".equals(strControlName) && !"attribute_object_name".equals(strControlName))
        {
            if("objectTypeList".equals(strControlName) && (control instanceof DataDropDownList))
                strInitialValue = ((DataDropDownList)control).getValue();
            else
            if("docbaseObj".equals(strControlName) && (control instanceof DocbaseObject))
                onDocbaseObjectInitialized((DocbaseObject)control);
            super.onControlInitialized(form, control);
        }
        if("objectTypeList".equals(strControlName) && (control instanceof DataDropDownList))
        {
            String strValue = ((DataDropDownList)control).getValue();
            if(strValue != null && !strValue.equals(strInitialValue))
            {
                ArgumentList eventArgs = new ArgumentList();
                eventArgs.add("initType", strInitialValue);
                eventArgs.add("newType", strValue);
                Component comp = getContainedComponent();
                comp.fireEvent("onsynctype", eventArgs);
            }
        }
    }

    private void onDocbaseObjectInitialized(DocbaseObject docbaseObj)
    {
        int index = getCurrentComponent();
        if(index > 0)
        {
            ArgumentList containedArg = getContainedComponentArgs();
            String docbaseType = containedArg.get("docbaseType");
            if(docbaseType == null || docbaseType.length() == 0)
                docbaseType = getPrevType();
            if(docbaseType != null && docbaseType.length() > 0)
                docbaseObj.setType(docbaseType);
        }
    }

    private String getPrevType()
    {
        boolean isCurrentDir = isCurrenFileToImportDirectory();
        int current = getCurrentComponent();
        String docbaseType = null;
        for(int i = current - 1; i >= 0; i--)
        {
            setCurrentComponent(i);
            if(isCurrentDir != isCurrenFileToImportDirectory() || !(getContainedComponent() instanceof MrcsImportContentUCF)) //TODO - change to MrcsImportContent53
                continue;
            MrcsImportContentUCF prevComponent = (MrcsImportContentUCF)getContainedComponent();
            DocbaseObject prevDocbaseObj = prevComponent.getDocbaseObjectControl(false);
            if(prevDocbaseObj == null)
                continue;
            docbaseType = prevDocbaseObj.getType();
            break;
        }

        setCurrentComponent(current);
        return docbaseType;
    }

    // this bypass/hack allows us to control the enabling of the Next button in Import, which the 
    // WDK framework is quite reticent to allow right now...
    public boolean onMrcsNextPage(Control control, ArgumentList argumentlist)
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage(control,arglist) - mrcsnext button clicked, invoking standard onNextPage",null,null);
        return onNextPage();    
    }
    

    public boolean onNextPage()
    {
        MessageService.clear(this);
        String strCurPage = getComponentPage();
        boolean bRet;
        if(strCurPage.equals("fileselection"))
            bRet = processOnNextPageFromFileSelection();
        else
        if(strCurPage.equals("folderselection"))
            bRet = processOnNextPageFromFolderSelection();
        else
            bRet = processOnNextPageFromImport();
        return bRet;
    }

    public boolean onPrevPage()
    {
        Component component = getContainedComponent();
        component.validate();
        return super.onPrevPage();
    }

    protected boolean processOnNextPageFromImport()
    {
    	/*
        if(isFilesToImportSorted() && inAutoCommit() && m_pageStartedOnOk != null && m_pageStartedOnOk.equals("containerstart") && getCurrentComponent() != m_compStartedAutoCommit && isCurrenFileToImportDirectory() != isPreviousFileToImportDirectory())
        {
            stopAutoCommit();
            return false;
        }
        String strType = "";
        if(inAutoCommit())
        {
            ArgumentList arg = getContainedComponentArgs();
            strType = arg.get("type");
        }
        boolean bRet = super.onNextPage();
        if(bRet && inAutoCommit())
        {
            ArgumentList arg = new ArgumentList(getContainedComponentArgs());
            arg.replace("type", strType);
            setContainedComponentArgs(arg);
        }
        return bRet;
        */

        // not the initial screens for file selection, so process MrcsImportContent pages
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - handle MrcsImportContent combo/wizard/container flow",null,null);        
        // check the page state of the contained component
        String containedpage = getContainedComponentPage();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - current contained component page: "+containedpage,null,null);        
        MrcsImportContentUCF importcomp = (MrcsImportContentUCF)getContainedComponent();        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - current contained component: "+importcomp.getFilePathSelection(),null,null);
        
        // setup flags for the serpentine conditionals we will be perfoming
        boolean generateName = false; 
        boolean gotoNextFile = false;

        // TODO figure out what the hell xmlappdetect does, and if I need to customize it somehow
        // - FYI ImportComponent onInit() initializes to null or "xmlappdetect" (je pense...)
        
        // check the current page the contained MrcsImportContent component is on and do appropriate processing
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - checking page state of MrcsImportContent",null,null);        
        if (containedpage == null || "start".equals(containedpage) || "xmlappdetect".equals(containedpage))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - initial page identified for current contained component",null,null);        
            // this is the first/zeroth page of the current MrcsImportContent component
            // need to validate here...
            validate();
            boolean validation = getIsValid();
            if (!validation) return validation;

            // assert the system and mrcs document type on the import component
            // -- this is to fix a multi-import bug. since the import components are init'd after file select but before the attr value sets, it's possible that the
            // -- system type and mrcs doctype are still the defaults. since the NEXT button triggers here, we have to force the import component to set it's necessary
            // -- member vars at this point, since this is the last action issued by the import component/form...
            importcomp.forceMrcsDocumentType();            
            // is there a custom page for this component?
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - check if there are custom pages to process",null,null);        
            if (importcomp.hasCustomPage())
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - process next custom page",null,null);        
                importcomp.setOnCustomPage(true);
                String custompage = importcomp.setComponentCustomPage();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - next page: "+custompage,null,null);        
                setContainedComponentPage(custompage);
                generateName = false;
                gotoNextFile = false;
            }
            else
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - no custom page is up",null,null);        
                // no custom component specified, so we can go ahead and generate the name...
                generateName = true;
                gotoNextFile = true;
            }
        }
        
        // check if the contained page is a custom processing page...
        else if (importcomp.getOnCustomPage())
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - current page is a custom page, invoke handler",null,null);                    
            // invoke custom page handler
            Map customdata = importcomp.processCustomPage();
            //Text objname = (Text)importcomp.getControl("MrcsCustomObjectName", Text.class);
            //if we want to get more sophisticated with multiple custom pages, we will need another importcomp.hasMoreCustomPages() call here
            generateName = true;
            gotoNextFile = true;            
        }
        
        if (generateName)
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - invoking name generator",null,null);
            String name = importcomp.generateMrcsName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - generated name: "+name,null,null);            
        }
        
        if (gotoNextFile)
        {
            // check if there is another component in the contained component list...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - check if there is another component to go to",null,null);
            if (hasNextPage())
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - moving onto next component...",null,null);
                // move on to the next contained component/file
                boolean flag = super.onNextPage();
                Control nextbutton = getControl("next", com.documentum.web.form.control.Button.class);
                nextbutton.setEnabled(true);
                Control okbutton = getControl("ok", com.documentum.web.form.control.Button.class);
                okbutton.setEnabled(false);
                
                // ? fix format refresh bug?
                MrcsImportContentUCF nextcomp = (MrcsImportContentUCF)getContainedComponent();
                
                nextcomp.m_mrcsdoctype = importcomp.m_mrcsdoctype;
                nextcomp.m_systemtype = importcomp.m_systemtype;                
                nextcomp.forceMrcsInitFormats();
                
            } else {
                // call onOk(), we can use nulls since onOk() doesn't use either of its parameters
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - completing component flow, being importing...",null,null);
                onOk(null,null);
            }
        }
        return true;    	
    }

    protected void startAutoCommit()
    {
        m_compStartedAutoCommit = getCurrentComponent();
        super.startAutoCommit();
    }

    protected boolean processOnNextPageFromFileSelection()
    {
        Collection files = getUploadedFilesFromRequest();
        if(files == null || files.size() == 0)
        {
            setReturnError("MSG_NO_FILES_SELECTED", null, null);
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_NO_FILES_SELECTED", null);
            return false;
        }
        Integer maxCount;
        if((maxCount = lookupInteger("max-import-file-count")) != null && files.size() > maxCount.intValue())
        {
            Object params[] = {
                new Integer(files.size()), maxCount
            };
            setReturnError("MSG_MAX_IMPORT_FILE_COUNT_EXCEEDED", params, null);
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_MAX_IMPORT_FILE_COUNT_EXCEEDED", params, null);
            return false;
        }
        ImportFile duplicateFolder;
        if((duplicateFolder = findDuplicateFolder(files)) != null)
        {
            Object params[] = {
                duplicateFolder.getFileName()
            };
            setReturnError("MSG_DUPLICATE_FOLDERS_SELECTED", params, null);
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_DUPLICATE_FOLDERS_SELECTED", params, null);
            return false;
        }
        setFilesToImport(files);
        if(getFolderId() == null || getFolderId().length() == 0)
        {
            setComponentPage("folderselection");
        } else
        {
            initContainedComponents();
            setComponentPage("containerstart");
        }
        return true;
    }

    public void onRender()
    {
        super.onRender();
        if(getComponentPage().equals("containerstart"))
            updateFolderFileIndicator();
    }

    protected void updateFolderFileIndicator()
    {
        Panel panel = getFolderFileIndicatorPanel(true);
        panel.setVisible(m_fileFolderMix);
        if(m_fileFolderMix)
        {
            Tabbar tabbar = getFolderFileIndicatorTabbar(false);
            if(tabbar != null)
            {
                Tab toBeSelected = isCurrenFileToImportDirectory() ? getFolderIndicatorTab(true) : getFileIndicatorTab(true);
                tabbar.setValue(toBeSelected.getName());
                Tab selected = tabbar.getSelectedTab();
                selected.setEnabled(true);
                Iterator iter = tabbar.getTabs();
                do
                {
                    if(!iter.hasNext())
                        break;
                    Tab tab = (Tab)iter.next();
                    if(tab != selected)
                        tab.setEnabled(false);
                } while(true);
            }
        }
    }

    private ImportFile findDuplicateFolder(Collection files)
    {
        Map map = new HashMap();
        for(Iterator iter = files.iterator(); iter.hasNext();)
        {
            ImportFile file = (ImportFile)iter.next();
            if(file.isDirectory())
            {
                ArrayList key = new ArrayList(2);
                key.add(file.getFileName());
                key.add(file.getParentPath());
                ImportFile duplicate;
                if((duplicate = (ImportFile)map.put(key, file)) != null)
                    return duplicate;
            }
        }

        return null;
    }


    protected boolean processOnNextPageFromFolderSelection()
    {
        FolderSelectionValidator folderValidator = getFolderValidatorControl(true);
        folderValidator.validate();
        if(!folderValidator.getIsValid())
            return false;
        MultiDocbaseTree repositoriesSelTree = getFolderLocatorControl(true);
        if(repositoriesSelTree.getSelectedNode() == null)
            return false;
        DocbaseFolderTreeNode selectedNode = (DocbaseFolderTreeNode)repositoriesSelTree.getSelectedNode();
        String folderId = selectedNode.getId();
        setFolderId(folderId);
        String docbaseName = selectedNode.getDocbaseName();
        setTargetDocbaseName(docbaseName);
        ArgumentList arg = getContainedComponentArgs();
        arg.replace("objectId", folderId);
        if(getFilesToImport() == null || getFilesToImport().size() == 0)
        {
            setComponentPage("fileselection");
        } else
        {
            initContainedComponents();
            setComponentPage("containerstart");
        }
        return true;
    }

    protected void initContainedComponents()
    {
        Collection filesToImport = getFilesToImport();
        ArgumentList containedComponentsArgs[] = new ArgumentList[filesToImport.size()];
        ArgumentList compArgs = getContainedComponentArgs();
        int i = 0;
        for(Iterator iter = filesToImport.iterator(); iter.hasNext();)
        {
            ArgumentList args = new ArgumentList(compArgs);
            Object o = iter.next();
            if(o instanceof ImportFile)
            {
                ImportFile ifile = (ImportFile)o;
                if(ifile.getFilePath() != null)
                    args.add("filenameWithPath", ifile.getFilePath());
                if(ifile.getLocalFilePath() != null)
                    args.add("localFilePath", ifile.getLocalFilePath());
                if(ifile.getParentPath() != null)
                    args.add("parentPath", ifile.getParentPath());
                args.add("isDirectory", String.valueOf(ifile.isDirectory()));
                if(ifile.getAttributes() != null && !ifile.getAttributes().isEmpty())
                    args.add("defaultAttributesValues", ArgumentList.encode(ifile.getAttributes()));
                if(ifile.getFormat() != null)
                    args.add("format", ifile.getFormat());
            } else
            {
                args.add("filenameWithPath", o.toString());
            }
            containedComponentsArgs[i] = args;
            i++;
        }

        setContainedComponentsArgs(containedComponentsArgs);
        super.initContainedComponents();
        setCurrentComponent(0);
    }
    
    public void processImportedDocument(String newdocid) 
    {
    	/*-CONFIG-*/String m = "processImportedDocument - ";
        // we are at post-import of the content, need to perform MRCS post-processing
        // invoke document SBO to perform the name generation
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"post-document import, need to attach lifecycle and execute plugins",null,null);
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting current user/pass",null,null);
            IDfSession session = getDfSession();
            IDfDocument newdoc = (IDfDocument)session.getObject(new DfId(newdocid));
            String mrcsapp = newdoc.getString("mrcs_application");
            String mrcsdoctype = newdoc.getString("mrcs_config");
            String mrcsgftype = newdoc.getString("mrcs_folder_config");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"invoking imported document processing service",null,null);
            MrcsDocumentSBO docsbo = MrcsDocumentSBO.getService();
            Map customdata = new HashMap();
            docsbo.processImportedDocument(session, mrcsapp, mrcsgftype, mrcsdoctype, newdocid, customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doc name generated successfully",null,null);
                        
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error(m+"exception thrown during postprocessing of Mrcs Document",e);
            setReturnError("MSG_MRCS_IMPORT_POSTPROCESS_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_POSTPROCESS_ERROR", e);
            // ?? delete newly imported document??
            try { 
                IDfSession session = getDfSession();
                IDfSysObject newdoc = (IDfSysObject)session.getObject(new DfId(newdocid));
                newdoc.destroy();
            } catch (Exception ee) {
                /*-ERROR-*/DfLogger.getRootLogger().error(m+"unable to rollback/destroy newly imported document",ee);                
            }
            throw new WrapperRuntimeException("Error in postprocessing of MRCS document import!",e);
        }
    }


    // CEM: Override this to execute post-creation plugins and jump to properties component
    protected void handleOnReturnFromProgressSuccess(Form form, Map map, JobAdapter job)
    {
    	/*-CONFIG-*/String m = "handleOnReturnFromProgressSuccess - ";
        try
        {
            if(job.getService() instanceof ImportService)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting import service",null,null);
                ImportService importService = (ImportService)job.getService();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting list of new docids for newly imported docs objects from import service",null,null);
                List ids = importService.getNewObjectIds();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting corresponding processors list so we can assert the format...",null,null);
                List processors = importService.getServiceProcessors();                
                // CEM - post-process the MRCS documents
                if (ids != null)
	                for (int i=0; i < ids.size(); i++)
	                {
	                	
	                	String docid = (String)ids.get(i);
	                	// CEM - do we stop if one throws error, or rollback all of the new documents????
	                	// TODO - rollback/delete remaining docs if error occurs
	                	// TODO - rollback as MRCS system user?
	                	processImportedDocument(docid);
	                }
                
                if(ids != null)
                    setReturnValue("newObjectIds", ids);
            }
            addFinalSuccessMessage();
            //super.handleOnReturnFromProgressSuccess(form, map, job);
            setReturnValue("success", Boolean.TRUE.toString());
            setComponentReturn();
        }
        catch(ContentTransferException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void handleOnReturnFromProgressRequestInput(Form form, Map map, JobAdapter job)
    {
        ArgumentList args = new ArgumentList();
        PromptEvent promptEvent = job.getPromptEvent();
        String strNotificationCode = getNoficationCode(promptEvent);
        String strNotificationMsg = getNoficationMsg(promptEvent);
        if(strNotificationCode.equals("XML_CA_I_SPECIFY_XML_APP"))
        {
            args.add("message", strNotificationCode + ": " + strNotificationMsg + "\n\n" + getString("MSG_SELECT_XML_APP_FROM_LIST"));
            String listValues[] = promptEvent.getListValues();
            String strValues = "";
            if(listValues != null)
            {
                StringBuffer buf = new StringBuffer(128);
                Util.buildAttrList(listValues, 0, listValues.length, buf);
                strValues = buf.toString();
            }
            args.add("input", "dropdown");
            args.add("values", strValues);
            args.add("component", "promptinput");
            BackDetector.setComponentNested(this, "promptinputcontainer", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromPromptInput"));
        } else
        if(strNotificationCode.equals("UCF_I_FILE_NOT_FOUND"))
        {
            args.add("fileName", strNotificationMsg);
            args.add("component", "locateimportlocalfile");
            BackDetector.setComponentNested(this, "locateimportlocalfilecontainer", null, args, getContext(), new FormActionReturnListener(this, "onReturnFromLocateFile"));
        } else
        {
            super.handleOnReturnFromProgressRequestInput(form, map, job);
        }
    }

    protected void addFinalSuccessMessage()
    {
        MessageService.addMessage(this, "MSG_OPERATION_SUCCESSFUL");
    }

    protected void invokeService(ContentTransferService service)
        throws ContentTransferException
    {
        if(service instanceof ImportService)
        {
            ImportService importService = (ImportService)service;
            importService.setDestinationFolderId(getFolderId());
            super.invokeService(service);
        } else
        {
            super.invokeService(service);
        }
    }

    protected Collection getFilesToImport()
    {
        return m_setFiles;
    }

    protected void setFilesToImport(Collection files)
    {
        Set f = ListOrderedSet.decorate(new HashSet());
        boolean mixed = false;
        if(files != null)
        {
            Object a[] = files.toArray();
            if(isFilesToImportSorted())
                Arrays.sort(a, getFilesToImportComparator());
            ImportFile prev = null;
            for(int i = 0; i < a.length; i++)
            {
                f.add(a[i]);
                if(mixed)
                    continue;
                ImportFile curr = (ImportFile)a[i];
                if(prev != null)
                    mixed = prev.isDirectory() != curr.isDirectory();
                prev = curr;
            }

        }
        m_fileFolderMix = mixed;
        m_setFiles = f;
    }

    protected boolean isFilesToImportSorted()
    {
        return true;
    }

    protected Comparator getFilesToImportComparator()
    {
        return DIRS_FIRST;
    }

    private boolean isCurrenFileToImportDirectory()
    {
        int current = getCurrentComponent();
        ArgumentList args[] = getContainedComponentsArgs();
        return Boolean.valueOf(args[current].get("isDirectory")).booleanValue();
    }

    private boolean isPreviousFileToImportDirectory()
    {
        int current = getCurrentComponent();
        if(current > 0)
        {
            ArgumentList args[] = getContainedComponentsArgs();
            return Boolean.valueOf(args[current - 1].get("isDirectory")).booleanValue();
        } else
        {
            return false;
        }
    }

    protected String getFolderId()
    {
        return m_folderId;
    }

    protected void setFolderId(String folderId)
    {
        m_folderId = folderId;
    }

    protected void setTargetDocbaseName(String docbaseName)
    {
        m_targetDocbaseName = docbaseName;
    }

    protected String getTargetDocbaseName()
    {
        return m_targetDocbaseName;
    }

    protected Button getOkButtonControl(boolean create)
    {
        return (Button)getControl0("ok", create, com.documentum.web.form.control.Button.class);
    }

    protected Button getPreviousButtonControl(boolean create)
    {
        return (Button)getControl0("prev", create, com.documentum.web.form.control.Button.class);
    }

    protected FolderSelectionValidator getFolderValidatorControl(boolean create)
    {
        return (FolderSelectionValidator)getControl0("folderValidator", create, com.documentum.web.formext.control.validator.FolderSelectionValidator.class);
    }

    protected MultiDocbaseTree getFolderLocatorControl(boolean create)
    {
        return (MultiDocbaseTree)getControl0("folderlocator", create, com.documentum.web.formext.control.docbase.MultiDocbaseTree.class);
    }

    protected Panel getFolderFileIndicatorPanel(boolean create)
    {
        return (Panel)getControl0("folderFileIndicatorPanel", create, com.documentum.web.form.control.Panel.class);
    }

    protected Tabbar getFolderFileIndicatorTabbar(boolean create)
    {
        return (Tabbar)getControl0("folderFileIndicatorTabbar", create, com.documentum.web.form.control.Tabbar.class);
    }

    protected Tab getFolderIndicatorTab(boolean create)
    {
        return (Tab)getControl0("folderIndicatorTab", create, com.documentum.web.form.control.Tab.class);
    }

    protected Tab getFileIndicatorTab(boolean create)
    {
        return (Tab)getControl0("fileIndicatorTab", create, com.documentum.web.form.control.Tab.class);
    }

    Control getControl0(String name, boolean create, Class cl)
    {
        return create ? getControl(name, cl) : getControl(name);
    }

    public static List retrieveReturnObjectIds(Map onReturnMap)
    {
        List newObjectIdList = Collections.EMPTY_LIST;
        Object retNewIds = onReturnMap.get("newObjectIds");
        if(retNewIds != null && (retNewIds instanceof List))
        {
            newObjectIdList = (List)retNewIds;
        } else
        {
            retNewIds = onReturnMap.get("newObjectIds");
            if(retNewIds != null && (retNewIds instanceof String[]))
            {
                String ids[] = (String[])retNewIds;
                newObjectIdList = new ArrayList(ids.length);
                for(int i = 0; i < ids.length; i++)
                    newObjectIdList.add(ids[i]);

            }
        }
        return newObjectIdList;
    }

    public static final String NEW_OBJECT_IDS = "newObjectIds";
    protected static final String EVENT_ONCHANGEOBJECTTYPE = "onchangeobjecttype";
    private static final String HANDLER_ONCHANGEOBJECTTYPE = "onChangeObjectType";
    private Set m_setFiles;
    private String m_folderId;
    private String m_targetDocbaseName;
    private int m_compStartedAutoCommit;
    private String m_pageStartedOnOk;
    private boolean m_fileFolderMix;
    private static final Comparator DIRS_FIRST = new DirsFirstComparator();

    String m_mrcsapp, m_gftype, m_parentfolderid;
 
    // from UCFImportContainer (concrete impl of ImportContentContainer)
    protected Collection getUploadedFilesFromRequest()
    {
        Set files = ListOrderedSet.decorate(new HashSet());
        FileSelector fselector = getFileselectorControl(false);
        if(fselector != null)
        {
            IFile ifiles[] = fselector.getSelectedFiles();
            if(ifiles != null)
            {
                for(int i = 0; i < ifiles.length; i++)
                {
                    IFile ifile = ifiles[i];
                    if(ifile != null)
                        addFile(files, ifile, null);
                }

            }
        }
        return files;
    }

    private void addFile(Set files, IFile file, IFile parent)
    {
        MrcsImportContainerUCF.ImportFile importFile = new MrcsImportContainerUCF.ImportFile(null, file.getPath(), null, parent == null ? null : parent.getPath(), file.isDirectory());
        files.add(importFile);
        IFile children[] = file.listFiles();
        if(children != null)
        {
            for(int i = 0; i < children.length; i++)
                addFile(files, children[i], file);

        }
    }

    protected FileSelector getFileselectorControl(boolean create)
    {
        return (FileSelector)(create ? getControl("fileselector", com.documentum.web.form.control.fileselector.FileSelector.class) : getControl("fileselector"));
    }    
    
}
