/*
 * Created on Feb 25, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: MrcsImportContainer.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.TreeSet;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.operations.DfXMLUtils;
import com.documentum.operations.IDfImportNode;
import com.documentum.operations.IDfImportOperation;
import com.documentum.web.common.AccessibilityService;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.LocaleService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.control.ImportApplet;
import com.documentum.web.contentxfer.control.LinkDetectorApplet;
import com.documentum.web.contentxfer.control.ServiceProgressFeedback;
import com.documentum.web.contentxfer.server.ContentTransferService;
import com.documentum.web.contentxfer.server.ContentXferServiceEvent;
import com.documentum.web.contentxfer.server.IContentXferServiceListener;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.ListBox;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.fileselector.FileSelector;
import com.documentum.web.formext.component.Component;
import com.documentum.web.util.Browser;
import com.documentum.webcomponent.library.contentxfer.UploadContainer;
import com.documentum.webcomponent.library.messages.MessageService;

/**
 * @author muellc4
 *
 * Code duplication with ImportContainer is necessary since Java cannot do super.super.onNextPage, 
 * and we need custom handling in onNextPage() - try reflection?
 */
public class MrcsImportContainer 
       extends UploadContainer
       implements IContentXferServiceListener
{
    
    // ahem HACK HACK HACK!!!! something is magically overwriting mrcsimport with import as the component. 
    //     we will stop this at all costs
    //     - that something is the onNextPage() on MrcsImportContainer/ImportContainer, where the setup of the argumentlists has
    //       the doctype dm_document and component "import" hardcoded. This has now been changed to "mrcsimport" and "m_mrcs_document"...
    //       ...although these are hardcoded as well. (changed only in MrcsImportContainer, ImportContainer is still hardcoded to "import"
    //       and dm_document)
    protected void setContainedComponentId(String s)
    {
        if ("import".equals(s))
        {
            s ="mrcsimport";
            return;
        }        
        super.setContainedComponentId(s);        
    }
    
    protected void setContainedComponentName(String s)
    {
        if (s != null && s.length() > "import".length())
        {
            if (s.substring(0,"import".length()).equals("import"))
            {
                s = "mrcs"+s;
            }         
            
        }        
        super.setContainedComponentName(s);
    }    

public MrcsImportContainer()
{
    m_appletImport = null;
    m_appletLinkDetector = null;
    m_progress = null;
    m_strContentTicket = null;
    m_setFiles = new TreeSet();
    m_setFiles_MacNav = null;
    m_listboxFiles = null;
    m_strNewObjectIds = null;
    m_xmlParsable = false;
    m_intXmlEndIndex = 0;
}

private boolean macOS9Nav()
{
    HttpServletRequest httpservletrequest = (HttpServletRequest)getPageContext().getRequest();
    boolean flag = Browser.isMac(httpservletrequest) && Browser.isNetscape(httpservletrequest) && !Browser.isMacOSXNav(httpservletrequest);
    return flag;
}

public void updateControls()
{
    super.updateControls();
    Component containedcomponent = getContainedComponent();
    if(containedcomponent != null)
    {
        String s = containedcomponent.getComponentPage();
        if(s != null && s.equals("xmlappdetect"))
            getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
    }
}

private String[] putXmlFirst(String as[])
{
    if(!macOS9Nav())
        return as;
    Vector vector = new Vector();
    Vector vector1 = new Vector();
    for(int i = 0; i < as.length; i++)
    {
        String s = as[i];
        if(s.toLowerCase().endsWith(".xml"))
            vector.addElement(s);
        else
            vector1.addElement(s);
    }

    if(vector.size() == 0)
        return as;
    String as1[] = new String[as.length];
    m_intXmlEndIndex = vector.size();
    for(int j = 0; j < m_intXmlEndIndex; j++)
        as1[j] = (String)vector.get(j);

    for(int k = 0; k < vector1.size(); k++)
        as1[m_intXmlEndIndex + k] = (String)vector1.get(k);

    return as1;
}

public boolean canCommitChanges()
{
    boolean cancommit = super.canCommitChanges();
    if(!cancommit || !m_xmlParsable || !macOS9Nav())
        return cancommit;
    int i = getCurrentComponent();
    if(hasNextPage() && m_intXmlEndIndex > i)
        cancommit = false;
    return cancommit;
}

public void onInit(ArgumentList argumentlist)
{
    super.onInit(argumentlist);
    
    // MRCS: detect the gftype and mrcsapp of this folder (it must be a m_mrcs_folder...)
    try { 
        m_parentfolderid = argumentlist.get("objectId");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - objectid: "+m_parentfolderid,null,null);
        IDfSession session = getDfSession();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - composing idfid",null,null);
        IDfId newid = new DfId(m_parentfolderid);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - looking up folder object",null,null);
        IDfFolder parentfolder = (IDfFolder)session.getObject(newid);

        // check permission
        int permission = parentfolder.getPermit();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - user's permission level: "+permission,null,null);
        if (IDfACL.DF_PERMIT_WRITE > permission)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContainer.onInit - Insufficient permit level to import MRCS documents in this folder");
            Exception e = new Exception("Insufficient permit level to import MRCS documents in this folder");
            setReturnError("MSG_DFC_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);   
            setComponentReturn();
            return;               
        }
        
        if (parentfolder.hasAttr("mrcs_application"))
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - current folder is a mrcs folder!",null,null);
            m_mrcsapp = parentfolder.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - mrcsapp: "+m_mrcsapp,null,null);
            m_gftype = parentfolder.getString("mrcs_config");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - gftype: "+m_gftype,null,null);
        }
        // else should we throw an exception???
    } catch (Exception dfe) {
        /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContainer.OnInit - error in getting MRCS state data from folder sysobject");
        setReturnError("MSG_DFC_ERROR", null, dfe);
        ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", dfe);
        setComponentReturn();
        return;
    }

    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - performing black magic (rest of copied onInit method)",null,null);
    m_appletImport = (ImportApplet)getControl("importapplet", com.documentum.web.contentxfer.control.ImportApplet.class);
    m_appletLinkDetector = (LinkDetectorApplet)getControl("linkdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
    m_progress = (ServiceProgressFeedback)getControl("serviceprogressfeedback", com.documentum.web.contentxfer.control.ServiceProgressFeedback.class);
    m_listboxFiles = (ListBox)getControl("filelist", com.documentum.web.form.control.ListBox.class);
    m_listboxFiles.setMutable(true);
    Panel iepanel = (Panel)getControl("IEButtons", com.documentum.web.form.control.Panel.class);
    Panel nspanel = (Panel)getControl("NSButtons", com.documentum.web.form.control.Panel.class);
    iepanel.setVisible(isBrowserIE());
    nspanel.setVisible(isBrowserNetscape());
    getControl("prev", com.documentum.web.form.control.Button.class).setEnabled(false);
    getControl("next", com.documentum.web.form.control.Button.class).setEnabled(!AccessibilityService.isAllAccessibilitiesEnabled());
    getControl("ok", com.documentum.web.form.control.Button.class).setEnabled(false);
    setReturnValue("success", "false");
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.OnInit - black magic performed, MrcsImportContainer initialized...",null,null);
}

public void onOk(Control control, ArgumentList argumentlist)
{
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onOk - finish has been invoked, proceeding with importing of content",null,null);
    validate();
    boolean isValid = getIsValid();
    boolean canCommit = canCommitChanges();
    if(isValid && canCommit && onCommitChanges())
    {
        LinkedList xmlcatfilelist = new LinkedList();
        String xmlcategoryCSVlist = null;
        ArrayList containedcomponents = getContainedComponents();
        for(int i = 0; i < containedcomponents.size(); i++)
        {
            MrcsImportContent importcontent = (MrcsImportContent)containedcomponents.get(i);
            String filepath = importcontent.getFilePath();
            String xmlcategory = importcontent.getXmlCategory();
            String format = importcontent.getFormat();
            if(macOS9Nav() && !format.equals("xml"))
                xmlcategory = "";
            if(xmlcategory != null && xmlcategory.length() > 0)
            {
                if(xmlcategoryCSVlist == null)
                    xmlcategoryCSVlist = xmlcategory;
                else
                    xmlcategoryCSVlist = xmlcategoryCSVlist + "," + xmlcategory;
                xmlcatfilelist.add(filepath);
            }
        }

        if(xmlcatfilelist.size() > 0)
        {
            m_appletLinkDetector.setRootFiles(xmlcatfilelist);
            m_appletLinkDetector.setCategories(xmlcategoryCSVlist);
            m_appletLinkDetector.setOperation("3");
            setComponentPage("linkdetect");
        } else
        {
            importUpload("");
        }
    } else
    if((!isValid || !canCommit) && inAutoCommit())
        stopAutoCommit();
}

public void onImportUploadComplete(Control control, ArgumentList argumentlist)
{
    m_strContentTicket = argumentlist.get("contentTicket");
    m_progress.setServiceMgr(this);
    setComponentPage("serviceprogress");
}

public void onXMLAppListUpdated(Control control, ArgumentList argumentlist)
{
    MrcsImportContent importcontent = (MrcsImportContent)getContainedComponent();
    importcontent.onXMLAppListUpdated(control, argumentlist);
}

public void onAfterLinkDetect(Control control, ArgumentList argumentlist)
{
    String linkinstruction = argumentlist.get("linkInstructions");
    importUpload(linkinstruction);
}

public void startService(IContentXferServiceListener icontentxferservicelistener)
{
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.startService - content Xfer service start event detected...",null,null);
    ContentTransferService contenttransferservice = new ContentTransferService();
    contenttransferservice.addContentXferServiceListener(icontentxferservicelistener);
    contenttransferservice.addContentXferServiceListener(this);
    startImport(contenttransferservice);
}

protected void startImport(ContentTransferService contenttransferservice)
{
    try
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.startImport - performing import on content ticket: "+m_strContentTicket,null,null);
        contenttransferservice.importContent(m_strContentTicket, getPageContext());
    }
    catch(Exception exception)
    {
        setReturnError("MSG_ERROR_IMPORT", null, exception);
        ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_IMPORT", exception);
    }
}

public void onContentTransferServiceComplete(Control control, ArgumentList argumentlist)
{
    setComponentReturn();
}

public void onAddToList(Control control, ArgumentList argumentlist)
{
    String fullfilename = argumentlist.get("filenameWithPath");
    if(fullfilename != null && fullfilename.length() > 0 && !m_setFiles.contains(fullfilename))
    {
        m_setFiles.add(fullfilename);
        setupFileListBox();
        getControl("next", com.documentum.web.form.control.Button.class).setEnabled(true);
    }
}

public void onRemoveFromList(Control control, ArgumentList argumentlist)
{
    String fullfilename = argumentlist.get("filenameWithPath");
    if(fullfilename != null && fullfilename.length() > 0 && m_setFiles.contains(fullfilename))
    {
        m_setFiles.remove(fullfilename);
        setupFileListBox();
        if(m_setFiles.size() == 0)
            getControl("next", com.documentum.web.form.control.Button.class).setEnabled(false);
    }
}

public boolean onMrcsNextPage(Control control, ArgumentList argumentlist)
{
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage(control,arglist) - mrcsnext button clicked, invoking standard onNextPage",null,null);
    return onNextPage();    
}


public boolean onNextPage()
{    
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - top of onNextPage",null,null);
    boolean flag = false;
    // get current page, it indicates the current state of the container:
    // - fileselection: initial file selection applet
    // - else... contained component iteration?
    String currentpage = getComponentPage();
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - current container page: "+currentpage,null,null);
    if(currentpage.equals("fileselection") || currentpage.equals("accessibleFileselection"))
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - perform file import list black magic",null,null);
        if(currentpage.equals("fileselection"))
        {
            FileSelector fileselector = (FileSelector)getControl("__FILE_SELECTOR_APPLET_CONTROL");
            String selectedfilenames[] = fileselector.getFiles();
            if(selectedfilenames == null || selectedfilenames.length == 0)
            {
                setReturnError("MSG_NO_FILES_SELECTED", null, null);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_NO_FILES_SELECTED", null);
                return flag;
            }
            if(macOS9Nav())
            {
                selectedfilenames = putXmlFirst(selectedfilenames);
                m_setFiles_MacNav = selectedfilenames;
            }
            m_setFiles.addAll(Arrays.asList(selectedfilenames));
        }
        int i = m_setFiles.size();
        boolean xmlparsabletrigger = false;
        String folderId = getObjectIds();
        ArgumentList componentarglists[] = new ArgumentList[i];
        if(m_setFiles_MacNav == null)
        {
            Iterator iterator = m_setFiles.iterator();
            for(int k = 0; k < i; k++)
            {
                ArgumentList pcArgList = new ArgumentList();
                pcArgList.add("component", "mrcsimport");
                pcArgList.add("objectId", folderId);
                pcArgList.add("type", "m_mrcs_document");
                String curfilename = (String)iterator.next();
                pcArgList.add("filenameWithPath", curfilename);
                componentarglists[k] = pcArgList;
                if(!xmlparsabletrigger)
                    xmlparsabletrigger = MrcsImportContent.isXmlParseable(curfilename, getDfSession());
            }

        } else
        {
            for(int j = 0; j < m_setFiles_MacNav.length; j++)
            {
                ArgumentList macNavArgList = new ArgumentList();
                macNavArgList.add("component", "mrcsimport");
                macNavArgList.add("objectId", folderId);
                macNavArgList.add("type", "m_mrcs_document");
                String s3 = m_setFiles_MacNav[j];
                macNavArgList.add("filenameWithPath", s3);
                componentarglists[j] = macNavArgList;
                if(!xmlparsabletrigger)
                    xmlparsabletrigger = MrcsImportContent.isXmlParseable(s3, getDfSession());
            }

        }
        setContainedComponentsArgs(componentarglists);
        setCurrentComponent(0);
        if(xmlparsabletrigger)
        {
            m_xmlParsable = true;
            setComponentPage("checkfullapplet");
        } else
        {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - setting mrcscombocontainer so we have complete control over next button",null,null);
            setComponentPage("mrcscombocontainer"); // darn rebellious Next button... 
        }
        flag = true;
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - end of file import list black magic",null,null);
    } 
    else
    {
        // not the initial screens for file selection, so process MrcsImportContent pages
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - handle MrcsImportContent combo/wizard/container flow",null,null);        
        // check the page state of the contained component
        String containedpage = getContainedComponentPage();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - current contained component page: "+containedpage,null,null);        
        MrcsImportContent importcomp = (MrcsImportContent)getContainedComponent();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - current contained component: "+importcomp.getFilePath(),null,null);        
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
                flag = super.onNextPage();
                Control nextbutton = getControl("next", com.documentum.web.form.control.Button.class);
                nextbutton.setEnabled(true);
            } else {
                // call onOk(), we can use nulls since onOk() doesn't use either of its parameters
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.onNextPage - completing component flow, being importing...",null,null);
                onOk(null,null);
            }
        }
    }
    return flag;
}

public void setObjectType(String s)
{
    ArgumentList argumentlist = getContainedComponentArgs();
    argumentlist.replace("type", s);
    Component component = (Component)getContainedComponents().get(getCurrentComponent());
    remove(component);
    getContainedComponent();
}

public void onControlInitialized(Form form, Control control)
{
    String s = control.getName();
    if((s == null || !s.equalsIgnoreCase("formatList")) && (s == null || !s.equalsIgnoreCase("attribute_object_name")))
        super.onControlInitialized(form, control);
}

public void errorOccurred(ContentXferServiceEvent contentxferserviceevent)
{
}

public void finished(ContentXferServiceEvent contentxferserviceevent)
{
    // this is the contentXferServiceEvent that is invoked after the import operations have completed
    // perform postprocessing 
    
    IDfImportOperation idfimportoperation = (IDfImportOperation)contentxferserviceevent.getOperation();
    if(idfimportoperation != null)
    {
        // MRCS: grab the component list so we can line up the new object ids with the MrcsImportContent components
        // so we can do postprocessing of the new objects (post-import/create plugins
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.finished - getting contained components for postprocessing",null,null);
        ArrayList complist = this.getContainedComponents();
        
        try
        {
            IDfList idflist = idfimportoperation.getNodes();
            m_strNewObjectIds = new String[idflist.getCount()];
            for(int i = 0; i < idflist.getCount(); i++)
            {
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.finished - postprocessing document node #"+i,null,null);
                IDfImportNode idfimportnode = (IDfImportNode)idflist.get(i);
                IDfId idfid = idfimportnode.getNewObjectId();
                if(!idfid.isNull())
                {
                    String newdocobjectid = idfid.toString();
                    m_strNewObjectIds[i] = new String(newdocobjectid);
                    // MRCS: get component that should correspond with this...(or we could match on name)
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.finished - current document: "+idfimportnode.getNewObjectName(),null,null);
                    MrcsImportContent importcomp = (MrcsImportContent)complist.get(i);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.finished - current component: "+importcomp.getObjectName(),null,null);                    
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.finished - postprocessing importing document",null,null);                    
                    importcomp.processImportedDocument(newdocobjectid);
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContainer.finished - document successfully post-processed",null,null);                    
                } else
                {
                    m_strNewObjectIds[i] = null;
                }
            }

            setReturnValue("newObjectIds", m_strNewObjectIds);
            setReturnValue("success", "true");
            MessageService.addMessage(this, "MSG_IMPORT_SUCCESS");
        }
        catch(Exception exception)
        {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContainer.finished - exception occurred while doing postprocessing of imported documents",exception);                    
            setReturnError("MSG_ERROR_IMPORT", null, exception);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_ERROR_IMPORT", exception);
            if (exception instanceof WrapperRuntimeException)
                throw (WrapperRuntimeException)exception;
            else throw new WrapperRuntimeException("Mrcs Import failed",exception);
        }
    }
}

public void percentComplete(ContentXferServiceEvent contentxferserviceevent)
{
}

public void started(ContentXferServiceEvent contentxferserviceevent)
{
}

public void stepFinished(ContentXferServiceEvent contentxferserviceevent)
{
}

public String[] getNewObjectIds()
{
    return m_strNewObjectIds;
}

protected void continueAfterAppletCheck()
{
    m_appletImport.setServiceUrl(getContentReceiverUrl());
    m_appletImport.setLocale(LocaleService.getLocale().toString());
    m_appletLinkDetector.setLocale(LocaleService.getLocale().toString());
    m_appletLinkDetector.setXmlAppServletURL(getXmlAppServletUrl());
    DfXMLUtils dfxmlutils = new DfXMLUtils();
    dfxmlutils.setSession(getDfSession());
    String s = DfXMLUtils.getParseableFileExt();
    m_appletLinkDetector.setParseableFileExt(s);
    if(AccessibilityService.isAllAccessibilitiesEnabled())
        setComponentPage("accessibleFileselection");
    else
        setComponentPage("fileselection");
}

protected void continueAfterFullAppletCheck()
{
    setComponentPage("containerstart");
}

protected void setCurrentComponent(int i)
{
    if(i > getCurrentComponent())
    {
        MrcsImportContent importcontent = (MrcsImportContent)getContainedComponent();
        String s = "dm_document";
        try
        {
            s = importcontent.getType();
        }
        catch(Exception exception) { }
        super.setCurrentComponent(i);
        ArgumentList argumentlist = getContainedComponentArgs();
        argumentlist.replace("type", s);
    } else
    {
        super.setCurrentComponent(i);
    }
}

private void importUpload(String linkinstructions)
{
    String folderid = null;
    String filepath = "";
    String objtype = "";
    String format = "";
    String objname = "";
    String xmlcategory = "";
    String propertysettings = "";
    ArrayList componentlist = getContainedComponents();
    for(int i = 0; i < componentlist.size(); i++)
    {
        MrcsImportContent importcontent = (MrcsImportContent)componentlist.get(i);
        if(folderid == null)
        {
            folderid = importcontent.getFolderId();
            filepath += importcontent.getFilePath();
            objname  += importcontent.getObjectName();
            objtype  += importcontent.getType();
            format   += importcontent.getFormat();
            if(macOS9Nav())
            {
                String macOS9_format = importcontent.getFormat();
                if(!macOS9_format.equals("xml"))
                    xmlcategory += "";
                else
                    xmlcategory += importcontent.getXmlCategory();
            } else
            {
                xmlcategory += importcontent.getXmlCategory();
            }
            propertysettings += importcontent.getPropertySettings();
        } else
        {
            filepath += "|" + importcontent.getFilePath();
            objname  += "|" + importcontent.getObjectName();
            objtype  += "|" + importcontent.getType();
            format   += "|" + importcontent.getFormat();
            if(macOS9Nav())
            {
                String contentformat = importcontent.getFormat();
                if(!contentformat.equals("xml"))
                    xmlcategory += "|";
                else
                    xmlcategory += "|" + importcontent.getXmlCategory();
            } else
            {
                xmlcategory += "|" + importcontent.getXmlCategory();
            }
            propertysettings += "|" + importcontent.getPropertySettings();
        }
    }

    m_appletImport.setLinkInstructions(linkinstructions);
    m_appletImport.setFile(filepath);
    m_appletImport.setType(objtype);
    m_appletImport.setFormat(format);
    m_appletImport.setFilename(objname);
    m_appletImport.setCategory(xmlcategory);
    m_appletImport.setPropertySettings(propertysettings);
    m_appletImport.setFolderId(folderid);
    setComponentPage("importupload");
}

private void setupFileListBox()
{
    m_listboxFiles.setMutable(true);
    m_listboxFiles.clearOptions();
    Option option;
    for(Iterator iterator = m_setFiles.iterator(); iterator.hasNext(); m_listboxFiles.addOption(option))
    {
        String s = (String)iterator
        .next();
        option = new Option();
        option.setValue(s);
        if(s.length() > 64)
        {
            StringBuffer stringbuffer = new StringBuffer(67);
            String s1 = s.substring(0, 3);
            String s2 = s.substring((6 + s.length()) - 64);
            stringbuffer.append(s1);
            stringbuffer.append("...");
            stringbuffer.append(s2);
            s = stringbuffer.toString();
        }
        option.setLabel(s);
    }

}

private static final int FILEPATH_MAXLENGTH = 64;
private static final int FILEPATH_PREFIX_LENGTH = 3;
public static final String FILE_SELECTOR_APPLET_CONTROL = "__FILE_SELECTOR_APPLET_CONTROL";
private ImportApplet m_appletImport;
private LinkDetectorApplet m_appletLinkDetector;
private ServiceProgressFeedback m_progress;
private String m_strContentTicket;
private TreeSet m_setFiles;
private String m_setFiles_MacNav[];
private ListBox m_listboxFiles;
private String m_strNewObjectIds[];
private boolean m_xmlParsable;
private int m_intXmlEndIndex;

String m_mrcsapp, m_gftype, m_parentfolderid;

}
