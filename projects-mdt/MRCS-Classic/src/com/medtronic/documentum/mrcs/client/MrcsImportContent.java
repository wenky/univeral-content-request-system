/* jadclipse */// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.kpdus.com/jad.html
//Decompiler options: packimports(3) radix(10) lradix(10)
//Source File Name: ImportContent.java
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

 Filename       $RCSfile: MrcsImportContent.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/30 22:53:21 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.operations.DfXMLUtils;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.LocaleService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.control.LinkDetectorApplet;
import com.documentum.web.form.Control;
import com.documentum.web.form.IVisitor;
import com.documentum.web.form.control.BooleanInputControl;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.web.formext.component.ComboContainer;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.config.IConfigElement;
import com.documentum.web.formext.control.docbase.DocbaseAttributeValue;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.util.XmlUtil;
import com.documentum.webcomponent.library.contentxfer.UploadUtil;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsCustomPageProcessorPlugin;
import com.thoughtworks.xstream.XStream;

//Referenced classes of package
// com.documentum.webcomponent.library.importcontent:
//       ImportContainer

public class MrcsImportContent extends Component
{
    private final class Visitor implements IVisitor
    {

        public boolean visit(Control control) {
            try {
                if (control.isEnabled())
                    if (control instanceof DocbaseAttributeValue) {
                        DocbaseAttributeValue docbaseattributevalue = (DocbaseAttributeValue) control;
                        if (docbaseattributevalue.isDirty()) {
                            String s1 = "dmf_" + docbaseattributevalue.getAttribute();
                            Object obj1 = (List) m_map.get(s1);
                            if (obj1 == null) {
                                obj1 = new ArrayList();
                                m_map.put(s1, obj1);
                            }
                            if (docbaseattributevalue.isRepeating()) {
                                Vector vector = docbaseattributevalue.getValues();
                                StringBuffer stringbuffer = new StringBuffer(128);
                                if (vector != null) {
                                    for (int i = 0; i < vector.size(); i++) {
                                        if (i > 0)
                                            stringbuffer.append("__prs__");
                                        String s2 = (String) vector.get(i);
                                        String s3 = docbaseattributevalue.getActualValue(s2);
                                        if (docbaseattributevalue.getDataType() == 0)
                                            s3 = s3.equals("T") ? "true" : "false";
                                        stringbuffer.append(s3);
                                    }

                                }
                                ((List) obj1).add(docbaseattributevalue.getActualValue(stringbuffer.toString()));
                            } else {
                                ((List) obj1).add(docbaseattributevalue.getActualValue(docbaseattributevalue.getValue()));
                            }
                        }
                    } else {
                        String s = control.getName();
                        if (s != null && m_dfType.findTypeAttrIndex(s) != -1) {
                            Object obj = (List) m_map.get(s);
                            if (obj == null) {
                                obj = new ArrayList();
                                m_map.put(s, obj);
                            }
                            if (control instanceof StringInputControl)
                                ((List) obj).add(((StringInputControl) control).getValue());
                            else if (control instanceof BooleanInputControl)
                                ((List) obj).add(new Boolean(((BooleanInputControl) control).getValue()));
                        }
                    }
            } catch (DfException dfexception) {
                throw new WrapperRuntimeException("Failed to get attributes", dfexception);
            }
            return true;
        }

        public String getPropertySettings() {
            StringBuffer stringbuffer = new StringBuffer(256);
            Iterator iterator = m_map.keySet().iterator();
            boolean flag = false;
            while (iterator.hasNext()) {
                String s = (String) iterator.next();
                List list = (List) m_map.get(s);
                for (int i = 0; i < list.size(); i++) {
                    if (flag)
                        stringbuffer.append("~~pfs~~");
                    else
                        flag = true;
                    stringbuffer.append(s).append("~~pfs~~").append((String) list.get(i));
                }

            }
            return stringbuffer.toString();
        }

        IDfType m_dfType;

        Map m_map;

        public Visitor(String s) {
            m_dfType = null;
            m_map = new HashMap(11, 1.0F);
            try {
                m_dfType = getDfSession().getType(s);
            } catch (DfException dfexception) {
                throw new WrapperRuntimeException("Failed to get object type: " + s, dfexception);
            }
        }
    }
    
    public MrcsImportContent() {
        m_strFolderId = null;
        m_typeList = null;
        m_formatList = null;
        m_categoryList = null;
        m_strFilenameWithPath = "";
        m_bIsXmlParseable = false;
        m_extensionMap = new HashMap(311, 1.0F);
        m_unknownFormatLabel = null;
        m_docbaseFormatNames = new LinkedList();
    }

    public void onInit(ArgumentList argumentlist) 
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - top, calling ancestor onInit()",null,null);
        super.onInit(argumentlist);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - getting document config service",null,null);
        try {
            m_docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
        } catch (Exception e) {
            setReturnError("MSG_MRCS_CONFIG_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_CONFIG_ERROR", e);
            return; // I think we escape out...don't we?
        }

        m_strFolderId = argumentlist.get("objectId");
        m_strFilenameWithPath = argumentlist.get("filenameWithPath");
        m_unknownFormatLabel = (Label) getControl("unknown_format_info_label", com.documentum.web.form.control.Label.class);
        m_unknownFormatLabel.setVisible(false);
        Label label = (Label) getControl("filename", com.documentum.web.form.control.Label.class);
        String s = UploadUtil.getFilenameFromPath(m_strFilenameWithPath);
        label.setLabel(s);
        //autoFill();
        String s1 = getParseableFileExtensions(getDfSession());
        m_bIsXmlParseable = isXmlParseable(m_strFilenameWithPath, getDfSession());
        Control control = getContainer();
        //if (control != null && (control instanceof HttpImportContainer)) {
        // m_bIsXmlParseable = false; }
        m_categoryList = (DataDropDownList) getControl("xmlCategoryList", com.documentum.web.form.control.databound.DataDropDownList.class);
        populateCategories("");
        if (!m_bIsXmlParseable) {
            m_categoryList.setVisible(false);
            Label label1 = (Label) getControl("xmlCategoryListLabel", com.documentum.web.form.control.Label.class);
            label1.setVisible(false);
        } else {
            LinkDetectorApplet linkdetectorapplet = (LinkDetectorApplet) getControl("xmlappdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
            linkdetectorapplet.setLocale(LocaleService.getLocale().toString());
            HttpServletRequest httpservletrequest = (HttpServletRequest) getPageContext().getRequest();
            String s2 = httpservletrequest.getContextPath();
            linkdetectorapplet.setXmlAppServletURL(s2 + "/wdk5-xmlutil");
            linkdetectorapplet.setParseableFileExt(s1);
            linkdetectorapplet.setOperation("1");
            linkdetectorapplet.setCurrentRootFile(m_strFilenameWithPath);
            if (control instanceof ComboContainer)
                ((ComboContainer) control).pauseAutoCommit();
            setComponentPage("xmlappdetect");
        }
        DocbaseObject docbaseobject = (DocbaseObject) getControl("obj", com.documentum.web.formext.control.docbase.DocbaseObject.class);
        docbaseobject.setType(argumentlist.get("type"));

        // performing MRCS initialization steps
        boolean isMrcsFolder = false;
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - getting context from parent folder: " + m_strFolderId,null,null);
            IDfSession session = getDfSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - composing idfid",null,null);
            IDfId newid = new DfId(m_strFolderId);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - looking up folder object",null,null);
            IDfFolder parentfolder = (IDfFolder) session.getObject(newid);
            if (parentfolder.hasAttr("mrcs_application")) {
                isMrcsFolder = true;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - current folder is a mrcs folder!",null,null);
                m_mrcsapp = parentfolder.getString("mrcs_application");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - mrcsapp: " + m_mrcsapp,null,null);
                m_gftype = parentfolder.getString("mrcs_config");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - gftype: " + m_gftype,null,null);
            }
        } catch (Exception dfe) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.OnInit - exception thrown during MRCS folder examination",dfe);
            setReturnError("MSG_MRCS_IMPORT_FOLDER_ATTR_ERROR", null, dfe);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_FOLDER_ATTR_ERROR", dfe);
            return; // I think we escape out...don't we?
        }

        // initialize dropdowns...
        initTypeListControl();

    }

    public void onRender() {
        //    m_typeList.getDataProvider().setDfSession(getDfSession());
        //    m_formatList.getDataProvider().setDfSession(getDfSession());
        super.onRender();
    }

    public boolean onCommitChanges() {
        return getIsValid();
    }

    public void onXMLAppListUpdated(Control control, ArgumentList argumentlist) {
        LinkDetectorApplet linkdetectorapplet = (LinkDetectorApplet) getControl("xmlappdetectorapplet", com.documentum.web.contentxfer.control.LinkDetectorApplet.class);
        String s = (String) linkdetectorapplet.getResult();
        populateCategories(s);
        Control control1 = getContainer();
        if (control1 instanceof ComboContainer)
            ((ComboContainer) control1).resumeAutoCommit();
        setComponentPage("start");
    }

    public String getFolderId() {
        return m_strFolderId;
    }

    public String getFilePath() {
        return m_strFilenameWithPath;
    }

    public String getObjectName() {
        //   Text text = (Text)getControl("attribute_object_name",
        // com.documentum.web.form.control.Text.class);
        //   String s = text.getValue();
        //   return s;
        return getDocumentName();
    }

    public String getFormat() {
        String s = m_formatList.getValue();
        return s;
    }

    public String getType() {
        //   String s = m_typeList.getValue();
        String s = m_systemtype;
        return s;
    }

    public String getXmlCategory() {
        String s = m_categoryList.getValue();
        return s;
    }

    public String getPropertySettings() {
        Visitor visitor = new Visitor(getType());
        visitDepthFirst(visitor);
        return visitor.getPropertySettings();
    }

    public static String getParseableFileExtensions(IDfSession idfsession) {
        DfXMLUtils dfxmlutils = new DfXMLUtils();
        dfxmlutils.setSession(idfsession);
        return DfXMLUtils.getParseableFileExt();
    }

    public static boolean isXmlParseable(String s, IDfSession idfsession) throws RuntimeException {
        return XmlUtil.isXmlExtension(s, idfsession);
    }

    private void autoFill() {
        Map map = readConfiguredDefaultFormats();
        String fileextension = UploadUtil.extractExtension(m_strFilenameWithPath);
        if (fileextension != null) {
            String format = null;
            boolean formatfound = false;
            format = (String) map.get(fileextension);
            if (format != null) {
                formatfound = m_docbaseFormatNames.contains(format);
                if (formatfound)
                    m_formatList.setValue(format);
            }
            if (!formatfound) {
                String extension = (String) m_extensionMap.get(fileextension);
                if (extension != null && extension.length() > 0)
                    m_formatList.setValue(extension);
                else
                    m_formatList.setValue("unknown");
            }
        }
        String currentformat = m_formatList.getValue();
        if (currentformat != null && currentformat.equals("unknown")) {
            m_unknownFormatLabel = (Label) getControl("unknown_format_info_label", com.documentum.web.form.control.Label.class);
            m_unknownFormatLabel.setVisible(true);
        }
        Text filenamelabel = (Text) getControl("attribute_object_name", com.documentum.web.form.control.Text.class);
        String filename = UploadUtil.getFilenameFromPath(m_strFilenameWithPath);
        filenamelabel.setValue(filename);
    }

    private Map readConfiguredDefaultFormats() {
        HashMap hashmap = new HashMap(23, 1.0F);
        IConfigElement iconfigelement = lookupElement("default_formats");
        if (iconfigelement != null) {
            for (Iterator iterator = iconfigelement.getChildElements("format"); iterator.hasNext();) {
                IConfigElement iconfigelement1 = (IConfigElement) iterator.next();
                if (iconfigelement1 != null) {
                    String s = iconfigelement1.getAttributeValue("dos_extension");
                    String s1 = iconfigelement1.getAttributeValue("name");
                    if (s != null && s1 != null) {
                        if (hashmap.containsKey(s))
                            throw new WrapperRuntimeException("Duplicate format entries for a dos_extension in the config file.");
                        hashmap.put(s, s1);
                    }
                }
            }

        }
        return hashmap;
    }

    private void populateCategories(String s) {
        String as[] = { "id", "description" };
        Vector vector = new Vector();
        String s1 = null;
        if (s != null && s.length() > 0) {
            String s2 = s;
            Object obj = null;
            while (s2 != null && s2.length() > 0) {
                int i = s2.indexOf(';');
                String s3;
                if (i != -1) {
                    s3 = s2.substring(0, i);
                    s2 = s2.substring(i + 1);
                } else {
                    s3 = s2;
                    s2 = "";
                }
                int j = s3.indexOf(',');
                if (j != -1) {
                    String as2[] = new String[2];
                    as2[0] = s3.substring(j + 1);
                    String s4 = s3.substring(0, j);
                    as2[1] = s4;
                    vector.add(as2);
                    if (s1 == null)
                        s1 = as2[0];
                }
            }
        } else {
            String as1[] = new String[2];
            as1[0] = "";
            as1[1] = getString("MSG_NONE");
            vector.add(as1);
        }
        TableResultSet tableresultset = new TableResultSet(vector, as);
        m_categoryList.getDataProvider().setScrollableResultSet(tableresultset);
        if (s1 == null)
            m_categoryList.setValue("");
        else
            m_categoryList.setValue(s1);
    }

    private void initFormatControl()
    {
        m_unknownFormatLabel.setVisible(false);

        // determine the extension of the file
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - extracting file extension",null,null);
        String ext = UploadUtil.extractExtension(m_strFilenameWithPath);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - extension: "+ext,null,null);

        // get valid format list from docconfig
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - getting format list from docconfig",null,null);
        List formatlist = m_docconfig.getDocumentFormats(m_mrcsapp,m_gftype,m_mrcsdoctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - composing DQL query...",null,null);
        String formatclause = "";
        for (int i=0; i < formatlist.size(); i++)
        {
            if (i > 0) formatclause += ",";
            formatclause += "'"+(String)formatlist.get(i) +"'";
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - formatclause: "+formatclause,null,null);
        String dqlquery = "SELECT name,description,dos_extension FROM dm_format "+
                          "WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') AND "+
                          "name IN ("+formatclause+") ORDER BY description";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - dql query: "+dqlquery,null,null);    
        DfQuery dfquery = new DfQuery();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - setting dql query",null,null);    
        dfquery.setDQL(dqlquery);
        
        // get reference to formatlist
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - looking up the formatlist control",null,null);    
        m_formatList = (DropDownList) getControl("formatList", com.documentum.web.form.control.DropDownList.class);
        m_formatList.setMutable(true);
        // empty it
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - clearing it out...",null,null);    
        m_formatList.clearOptions();
        
        boolean formatfound = false;
        String formatfoundname = null;
        
        IDfCollection idfcollection = null;
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - executing dql query",null,null);
            idfcollection = dfquery.execute(getDfSession(), 0); 
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - iterating through result set",null,null);
            while (idfcollection.next()) 
            {
                String formatname = idfcollection.getString("name");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - format name: "+formatname,null,null);
                String formatdesc = idfcollection.getString("description");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - format desc: "+formatdesc,null,null);
                String formatextension = idfcollection.getString("dos_extension");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - format ext: "+formatextension,null,null);
                                
                // add list option!
                Option newopt = new Option();
                newopt.setLabel(formatdesc);
                newopt.setValue(formatname);
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initFormatControl - adding option " + newopt.getLabel(),null,null);
                m_formatList.addOption(newopt);
                if (formatextension.equals(ext))
                {
                    formatfound = true;
                    formatfoundname = formatname;                    
                }                
                
                // WTFark is this for? Autofill() is the only thing that uses it...
                m_docbaseFormatNames.add(formatname); 
                if (!formatextension.equals(""))
                    m_extensionMap.put(formatextension, formatname);
            }
            if (formatfound)
            {
                m_formatList.setValue(formatfoundname);
            }
            else
            {
                // 
                m_unknownFormatLabel.setVisible(true);
            }

        } catch (DfException dfexception) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.initFormatControl - exception thrown during DQL execution",dfexception);
            setReturnError("MSG_MRCS_IMPORT_FORMAT_LOOKUP_ERROR", null, dfexception);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_FORMAT_LOOKUP_ERROR", dfexception);
            throw new WrapperRuntimeException("Unable to query format types from docbase!", dfexception);
        } finally {
            try {
                if (idfcollection != null)
                    idfcollection.close();
            } catch (DfException dfexception1) {
            }
        }
    }    

    private void initTypeListControl() 
    {
        // get the applicable doctype list from docconfig
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initTypeListControl - getting allowable mrcs document type list",null,null);
        List doctypes = m_docconfig.getAllowableDocumentTypes(m_mrcsapp, m_gftype);
        // get reference to type dropdownlist
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initTypeListControl - looking up the dropdown",null,null);
        m_typeList = (DropDownList) getControl("mrcsdoctypes", com.documentum.web.form.control.DropDownList.class);
        m_typeList.setMutable(true);
        // populate the list
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initTypeListControl - populating dropdown",null,null);
        for (int i = 0; i < doctypes.size(); i++) {
            Option newopt = new Option();
            newopt.setLabel((String) doctypes.get(i)); // should I do a decode to a descriptionattribute from config?
            newopt.setValue((String) doctypes.get(i));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initTypeListControl - adding option " + newopt.getLabel(),null,null);
            m_typeList.addOption(newopt);
        }
        // if the dropdown defaulted to a particular type, simulate it was clicked...
        m_mrcsdoctype = m_typeList.getValue();
        if (m_mrcsdoctype == null) {
            m_systemtype = null;
        } else {
            // we can cheat with nulls as arguments since onSelectType doesn't use them...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.initTypeListControl - dropdown was defaulted, calling onSelectType with empty args",null,null);
            onSelectType(null, null);
        }
    }

    public void onSelectType(DropDownList dropdownlist, ArgumentList argumentlist) {
        
        // get the selected Mrcs doctype
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - getting selected mrcs doctype",null,null);
        m_mrcsdoctype = m_typeList.getValue();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - doctype: " + m_mrcsdoctype,null,null);

        // lookup the equivalent DCTM systype
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - translating to system type",null,null);
        m_systemtype = m_docconfig.getDocumentSystemType(m_mrcsapp, m_gftype, m_mrcsdoctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - systype: " + m_systemtype,null,null);
        // populate the formats dropdown since new doctype selected
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - refreshing format control",null,null);
        initFormatControl();

        // some black box stuff? -- something to do with attributes I believe...
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - some DocBase Object manipulation, DCTM was doing this in ImportContent...",null,null);
        DocbaseObject docbaseobject = (DocbaseObject) getControl("obj", com.documentum.web.formext.control.docbase.DocbaseObject.class);
        docbaseobject.setType(m_systemtype);
        //docbaseobject.setType("dm_document");        
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.onSelectType - end of onSelectType",null,null);
    }

    public boolean hasCustomPage() 
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.hasMoreCustomPages - top",null,null);
        // query configuration to see if there is a custom page
        String custompage = m_docconfig.getDocumentComponent(m_mrcsapp, m_gftype, m_mrcsdoctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.hasMoreCustomPages - returned custom page: "+custompage,null,null);
        return custompage != null;
    }

    public String setComponentCustomPage() 
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.setComponentCustomPage - getting custom component page",null,null);
        String custompage = m_docconfig.getDocumentComponent(m_mrcsapp, m_gftype, m_mrcsdoctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.setComponentCustomPage - page: "+custompage,null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.setComponentCustomPage - setting custom page on component",null,null);
        setComponentPage(custompage);
        return custompage;
    }

    public Map processCustomPage() 
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - top of process custom page call",null,null);
        Map customdata = null;
        // check if there is a configured custom processor for the page
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - looking for page processor",null,null);
        MrcsPlugin pageprocessor = m_docconfig.getDocumentComponentProcessor(m_mrcsapp, m_gftype, m_mrcsdoctype);
        if (pageprocessor == null) {
            // use the default handler
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - no page processor defined, performing default processing",null,null);
            customdata = processCustomData();
        } else {
            try {
                // instantiate and execute custom handler - do this in an SBO? no...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - custom processor defined, instantiating "+pageprocessor.PluginClassName,null,null);
                MrcsCustomPageProcessorPlugin customprocessor = (MrcsCustomPageProcessorPlugin)Class.forName(pageprocessor.PluginClassName).newInstance();
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - executing processor",null,null);
                customdata = customprocessor.process(this,pageprocessor.PluginConfiguration);
            } catch (Exception e) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.processCustomPage - exception encountered during custom page processing");
                setReturnError("MSG_MRCS_IMPORT_CUSTOM_DATA_PROCESSING_ERROR", null, e);
                ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_CUSTOM_DATA_PROCESSING_ERROR", e);
                throw new WrapperRuntimeException("Exception thrown while executing Mrcs Custom Page Processor "+pageprocessor.PluginClassName,e);
            }
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomPage - custom page processing complete, returned customdata: "+new XStream().toXML(customdata),null,null);
        // save custom data
        setCustomData(customdata);
        return customdata;
    }

    public Map processCustomData() 
    {
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - scraping customdata",null,null);
        HashMap customdata = new HashMap();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - getting component iterator",null,null);
        Iterator formcontrols = getContainedControls();
        while (formcontrols.hasNext()) {
            Control currentcontrol = (Control) formcontrols.next();
            // what is the name?
            String controlname = currentcontrol.getName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - current control name: " + controlname,null,null);
            // see if the control name matches the custom control naming
            // convention
            String namingconventionprefix = "MrcsCustom";
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - checking if name matches prefix MrcsCustom",null,null);
            if (controlname != null && controlname.length() > namingconventionprefix.length() && controlname.substring(0, namingconventionprefix.length()).compareTo(namingconventionprefix) == 0) {
                // get the key
                String customkey = controlname.substring(namingconventionprefix.length());
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - prefix matched, key: " + customkey,null,null);
                // get the value
                // -- is it a text?
                if (currentcontrol instanceof StringInputControl) {
                    String customvalue = ((StringInputControl) currentcontrol).getValue();
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - string input control, value: " + customvalue,null,null);
                    customdata.put(customkey, customvalue);
                } else if (currentcontrol instanceof BooleanInputControl) {
                    Boolean custombool = new Boolean(((BooleanInputControl) currentcontrol).getValue());
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - boolean input control, value: " + custombool,null,null);
                    customdata.put(customkey, custombool);
                } else {
                    // log something about the unknown control type - for now,
                    // we assume that all custom fields should be StringInputControls
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processCustomData - unknown control type: " + currentcontrol.getClass(),null,null);
                }
            }
        }
        return customdata;
    }

    public String generateMrcsName() 
    {
        // invoke document SBO to perform the name generation
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - generating document name",null,null);
        String newdocname = null;
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - instantiating document SBO service",null,null);
//            final IDfSessionManager manager = SessionManagerHttpBinding.getNewDfSessionManager();
//            IMrcsDocumentSBO docsbo = (IMrcsDocumentSBO) DfClient.getLocalClient().new-Service(IMrcsDocumentSBO.class.getName(), manager);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - getting current user/pass",null,null);
            IDfSession session = getDfSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - invoking doc naming service",null,null);
            MrcsDocumentSBO docsbo = MrcsDocumentSBO.getService();
            newdocname = docsbo.generateDocumentName(session, m_mrcsapp, m_gftype, m_mrcsdoctype, m_strFolderId, m_customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - doc name generated successfully: " + newdocname,null,null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.generateMrcsName - exception thrown MRCS name generation",e);
            setReturnError("MSG_MRCS_IMPORT_NAME_GENERATION_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_NAME_GENERATION_ERROR", e);
        }
        // set the objectname on this MrcsImportContent component
        setDocumentName(newdocname);
        return newdocname;
    }

    public void processImportedDocument(String newdocid) 
    {
        // we are at post-import of the content, need to perform MRCS post-processing
        // invoke document SBO to perform the name generation
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - post-document import, need to attach lifecycle and execute plugins",null,null);
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - instantiating document SBO service",null,null);
//            final IDfSessionManager manager = SessionManagerHttpBinding.getSessionManager();
//            IMrcsDocumentSBO docsbo = (IMrcsDocumentSBO) DfClient.getLocalClient().new-Service(IMrcsDocumentSBO.class.getName(), manager);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - getting current user/pass",null,null);
            IDfSession session = getDfSession();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - invoking imported document processing service",null,null);
            MrcsDocumentSBO docsbo = MrcsDocumentSBO.getService();            
            docsbo.processImportedDocument(session, m_mrcsapp, m_gftype, m_mrcsdoctype, newdocid, m_customdata);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - doc name generated successfully",null,null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.processImportedDocument - exception thrown during postprocessing of Mrcs Document",e);
            setReturnError("MSG_MRCS_IMPORT_POSTPROCESS_ERROR", null, e);
            ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_POSTPROCESS_ERROR", e);
            // ?? delete newly imported document??
            try { 
                IDfSession session = getDfSession();
                IDfSysObject newdoc = (IDfSysObject)session.getObject(new DfId(newdocid));
                newdoc.destroy();
            } catch (Exception ee) {
                /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.processImportedDocument - unable to rollback/destroy newly imported document",ee);                
            }
            throw new WrapperRuntimeException("Error in postprocessing of MRCS document import!",e);
        }
    }

    private String m_mrcsdoctype;
    public String getMrcsDoctype() {return m_mrcsdoctype;}
    public void setMrcsDoctype(String a) {m_mrcsdoctype = a;}

    private Map m_customdata; // help track document state b/w first page and potential second page...
    public Map getCustomData() {return m_customdata;}
    public void setCustomData(Map newstate) {m_customdata = newstate;}

    private boolean m_onCustomPage = false; // detect if currently on a custom page...
    public boolean getOnCustomPage() {return m_onCustomPage;}
    public void setOnCustomPage(boolean b) {m_onCustomPage = b;}

    private String m_mrcsapp;
    public String getMrcsApp() {return m_mrcsapp;}
    public void setMrcsApp(String a) {m_mrcsapp = a;}

    private String m_gftype;
    public String getGftype() {return m_gftype;}
    public void setGftype(String a) {m_gftype = a;}

    private String m_mrcsdocumentname;
    public String getDocumentName() { return m_mrcsdocumentname; }
    public void setDocumentName(String a) { m_mrcsdocumentname = a; }

    private String m_systemtype;

    private MrcsDocumentConfigFactory m_docconfig;
    
    public static final String FORMAT_LIST = "formatList";
    private static final String UNKNOWN_FORMAT = "unknown";
    private String m_strFolderId;
    private DropDownList m_typeList;
    private DropDownList m_formatList;
    private DataDropDownList m_categoryList;
    private String m_strFilenameWithPath;
    private boolean m_bIsXmlParseable;
    private Map m_extensionMap;
    private Label m_unknownFormatLabel;
    private List m_docbaseFormatNames;

}

/*******************************************************************************
 * DECOMPILATION REPORT ***
 * 
 * DECOMPILED FROM: C:\Program Files\Apache Tomcat
 * 5.0.28\webapps\webtop\WEB-INF\classes/com/documentum/webcomponent/library/importcontent/ImportContent.class
 * 
 * 
 * TOTAL TIME: 21898 ms
 * 
 * 
 * JAD REPORTED MESSAGES/ERRORS:
 * 
 * Couldn't resolve all exception handlers in method initFormatControl Couldn't
 * resolve all exception handlers in method populateTypeList
 * 
 * EXIT STATUS: 0
 * 
 * 
 * CAUGHT EXCEPTIONS:
 *  
 ******************************************************************************/