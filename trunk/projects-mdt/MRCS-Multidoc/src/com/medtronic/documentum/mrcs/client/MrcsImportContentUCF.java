package com.medtronic.documentum.mrcs.client;

/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
//Jad home page: http://www.kpdus.com/jad.html
//Decompiler options: packimports(3) radix(10) lradix(10) 
//Source File Name:   ImportContent.java


import java.io.File;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.ServletRequest;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.client.IDfValidator;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfProperties;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.documentum.fc.common.IDfProperties;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.ErrorMessageService;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.contentxfer.impl.ImportProcessor;
import com.documentum.web.failover.TransientObjectWrapper;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.Util;
import com.documentum.web.form.control.BooleanInputControl;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Option;
import com.documentum.web.form.control.StringInputControl;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.ScrollableResultSet;
import com.documentum.web.formext.control.docbase.DocbaseIcon;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.formext.control.docbase.ITypedObjectDefaultValues;
import com.documentum.web.formext.docbase.FormatService;
import com.documentum.web.formext.docbase.TypeUtil;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.util.DocbaseObjectAttributeReader;
import com.documentum.web.util.XmlUtil;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.contenttransfer.ContentTransferComponent;
import com.documentum.webcomponent.library.contentxfer.UploadUtil;
import com.documentum.webcomponent.util.Types;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsPlugin;
import com.medtronic.documentum.mrcs.plugin.MrcsCustomPageProcessorPlugin;
import com.thoughtworks.xstream.XStream;

public class MrcsImportContentUCF extends ContentTransferComponent
{
 private class CachedResultSet extends ScrollableResultSet
     implements Cloneable
 {

     public Object clone()
     {
         CachedResultSet result;
         try
         {
             result = (CachedResultSet)super.clone();
             result.setCursor(-1);
         }
         catch(CloneNotSupportedException e)
         {
             throw new WrapperRuntimeException(e);
         }
         return result;
     }

     public void setCursor(int pos)
     {
         int size = m_lisData.size();
         m_iRow = pos >= size ? size : pos;
     }

     public int getResultsCount()
     {
         return m_lisData.size();
     }

     public Object getObject(int columnIndex)
     {
         Object objItem = null;
         if(m_lisData.size() - 1 >= m_iRow)
         {
             Object objData = m_lisData.get(m_iRow);
             objItem = ((Object[])objData)[columnIndex - 1];
         }
         return objItem;
     }

     public boolean next()
     {
         m_iRow++;
         return m_iRow < m_lisData.size();
     }

     public int findColumn(String columnName)
     {
     	try { 
	            int retValue;
	            retValue = -1;
	            int len = m_metaData.getColumnCount();
	            int i = 0;
	            do
	            {
	                if(i >= len)
	                    break;
	                if(columnName.equals(m_metaData.getColumnName(i + 1)))
	                {
	                    retValue = i + 1;
	                    break;
	                }
	                i++;
	            } while(true);
	            if(retValue == -1)
	                throw new WrapperRuntimeException("Specified column does not exist: " + columnName);
	            return retValue;
     	} catch (SQLException e) {
     		throw new WrapperRuntimeException(e);
     	}
     }

     public void sort(String s, int i, int j)
     {
     }

     public void close()
     {
     }

     private void initFromResultSet(ScrollableResultSet resultSet)
     {
         int rowPosition = 0;
         try
         {
             int columnCount = super.getMetaData().getColumnCount();
             Object objArray[];
             for(; resultSet.next(); m_lisData.add(((Object) (objArray))))
             {
                 rowPosition++;
                 objArray = new Object[columnCount];
                 for(int i = 0; i < columnCount; i++)
                 {
                     Object obj = resultSet.getObject(i + 1);
                     objArray[i] = obj;
                 }

             }

         }
         catch(SQLException e)
         {
             throw new WrapperRuntimeException(e);
         }
     }

     private List m_lisData;
     private int m_iRow;
     private ResultSetMetaData m_metaData;

     public CachedResultSet(ScrollableResultSet resultSet)
     {
         super(MrcsImportContentUCF.buildColumnArray(resultSet));
         m_iRow = -1;
         m_metaData = super.getMetaData();
         m_lisData = new ArrayList(resultSet.getResultsCount());
         initFromResultSet(resultSet);
         resultSet.setCursor(-1);
     }
 }

 private class TypedObjectDefaultValues
     implements ITypedObjectDefaultValues
 {

     public int getValueCount(String attrName)
     {
         if(m_attributes != null)
         {
             List attrValue = (List)m_attributes.get(attrName);
             if(attrValue == null)
                 return 0;
             else
                 return attrValue.size();
         } else
         {
             return 0;
         }
     }

     public String getRepeatingValue(String attrName, int valueIndex)
     {
         if(m_attributes != null)
         {
             List attrValue = (List)m_attributes.get(attrName);
             if(attrValue == null)
                 return null;
             else
                 return (String)attrValue.get(valueIndex);
         } else
         {
             return null;
         }
     }

     private TypedObjectDefaultValues()
     {
     }

 }


 public MrcsImportContentUCF()
 {
     m_mrcsdocumentname = null;
     m_directory = false;
     m_format = null;
 }

 public void onInit(ArgumentList arg)
 {
     super.onInit(arg);
     setFolderId(arg.get("objectId"));
     setFilePath(arg.get("filenameWithPath"));
     setParentPath(arg.get("parentPath"));
     setObjectName(arg.get("objectName"));
     //setDocbaseType(arg.get("docbaseType")); // CEM - on re-init after change of type, docbaseType has the new DCTM object type selected
     // - MRCS hacks/subverts this by passing doctype in docbaseType, and mrcs doctype in format!!!!
     String refresh_dctm_doctype = arg.get("docbaseType");
     String refresh_mrcs_docytpe = arg.get("format");
     if (refresh_dctm_doctype != null)
     {
    	 this.m_systemtype = refresh_dctm_doctype;
    	 this.m_mrcsdoctype = refresh_mrcs_docytpe;
     }

     setBaseDocbaseType(arg.get("baseDocbaseType"));
     
     // CEM: we don't allow directories...hmmm, do we? - this would be an opportunity to filter them out, or should we do that in container.initContainedComponents???
     setDirectory(Boolean.valueOf(arg.get("isDirectory")).booleanValue());
     
     // CEM: see docbaseType comment above for important information...
     /*
     setDefaultFormat(arg.get("format"));
     String strFormat = arg.get("format");
     if(strFormat != null && strFormat.length() > 0)
     {
         DocbaseIcon icon = (DocbaseIcon)getControl("obj_icon", com.documentum.web.formext.control.docbase.DocbaseIcon.class);
         if(icon != null)
         {
             icon.setFormat(strFormat);
             icon.setType("dm_document");
         }
     }
     */
     
     String defaultAttrValues = arg.get("defaultAttributesValues");
     if(defaultAttrValues != null)
         setDefaultAttributesValues(ArgumentList.decode(defaultAttrValues));
     String localFilePath = arg.get("localFilePath");
     if(localFilePath != null && localFilePath.length() > 0)
     {
         File f = new File(localFilePath);
         if(f.exists())
             setLocalFilePath(f.getAbsolutePath());
     }
          
     // performing MRCS initialization steps
     boolean isMrcsFolder = false;
     try {
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - getting context from parent folder: " + folderId,null,null);
         IDfSession session = getDfSession();
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.OnInit - composing idfid",null,null);
         IDfId newid = new DfId(folderId);
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
     
     initContext();
     initControls();
     
 }
 
 protected void setDefaultAttributesValues(ArgumentList attributes)
 {
	 /*-CONFIG-*/String m="setDefaultAttributesValues - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     m_attributesArgs = attributes;
     m_attributes = new HashMap(5);
     String name;
     List values;
     for(Iterator names = attributes.nameIterator(); names.hasNext(); m_attributes.put(name, values))
     {
         name = (String)names.next();
         String valuesArray[] = attributes.getValues(name);
         values = Arrays.asList(valuesArray);
     }

 }

 protected ArgumentList getDefaultAttributesValues()
 {
	 /*-CONFIG-*/String m="getDefaultAttributesValues - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     return m_attributesArgs;
 }

 protected void setDefaultFormat(String format)
 {
	 /*-CONFIG-*/String m="setDefaultFormat - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     m_format = format;
 }

 protected String getDefaultFormat()
 {
	 /*-CONFIG-*/String m="getDefaultFormat - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     return m_format;
 }

 protected boolean isPreserveFileExtension()
 {
     Boolean preserve = lookupBoolean("preserve-file-extension");
     if(preserve != null)
         return preserve.booleanValue();
     else
         return true;
 }

 protected void initContext()
 {
	 /*-CONFIG-*/String m="initContext - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP - getting docbase base type",null,null);
     String type = m_systemtype;
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - type: "+type,null,null);
     if(!typeExists(type))
         type = getDefaultDocumentType();
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" - type after default lookups: "+type,null,null);
     getContext().set("type", type);
 }

 protected void initControls()
 {
	 /*-CONFIG-*/String m="initControls - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     m_unknownFormatLabel = (Label) getControl("unknown_format_info_label", com.documentum.web.form.control.Label.class);
     m_unknownFormatLabel.setVisible(false);
     initDocbaseObjectControl();
     initFormatWarningLabelControl();
     initTypeListControl();
     
     // init filename label
     Label label = (Label) getControl("filename", com.documentum.web.form.control.Label.class);
     if (label != null)
     {
         String s = UploadUtil.getFilenameFromPath(m_filePath);
         label.setLabel(s);
     }
     
     // initFormatControl(); // This is done by initTypeListControl...
     // initFilenameLabelControl();
     //initSelectedFormat(); // CEM - I think we do this in initFormatControl
 }

 public IServiceProcessor getServiceProcessor()
 {
	 /*-CONFIG-*/String m="getServiceProcessor - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     IServiceProcessor sp = createServiceProcessor();
     if(sp instanceof ImportProcessor)
     {
         ImportProcessor proc = (ImportProcessor)sp;
         if(getLocalFilePath() != null)
             proc.setServerFilePath(getLocalFilePath());
         else
             proc.setClientFilePath(getFilePathSelection());
         proc.setDirectory(false);
         proc.setClientParentPath(getParentPathSelection());
         proc.setNewObjectFormat(getFormatSelection());
         proc.setNewObjectName(getObjectNameSelection());
         proc.setNewObjectType(getTypeSelection());
         proc.setNewObjectAttributes(getObjectAttributes());
     }
     return sp;
 }

 protected void initFormatWarningLabelControl()
 {
	 /*-CONFIG-*/String m="initFormatWarningLabelControl - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     getFormatWarningLabelControl(true).setVisible(false);
 }

 protected void initDocbaseObjectControl()
 {
	 /*-CONFIG-*/String m="initDocbaseObjectControl - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP - get control from form",null,null);
     DocbaseObject docbaseObj = getDocbaseObjectControl(true);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"found? "+(docbaseObj != null),null,null);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting type value to "+m_systemtype,null,null);
     docbaseObj.setType(m_systemtype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SettingPrepopulateValue",null,null);
     docbaseObj.setPrepopulateValue(new TypedObjectDefaultValues());
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DONE",null,null);
 }


 protected void initTypeListControl()
 {
	 /*-CONFIG-*/String m="initTypeListControl - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP - initialize hidden object types control",null,null);
     ScrollableResultSet resultSet = createTypesResultSet();
     DataDropDownList typeList = getTypeListControl(true);
     typeList.getDataProvider().setScrollableResultSet(resultSet);
     typeList.setValue(m_systemtype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"done with initialize hidden object dctm types control",null,null);
 	
     // get the applicable doctype list from docconfig
 	 MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting allowable mrcs document type list",null,null);
     List doctypes = docconfig.getAllowableDocumentTypes(m_mrcsapp, m_gftype);
     // get reference to documentum doctype dropdownlist so we can hide it
     m_dctmtypelist = (DataDropDownList)getControl("objectTypeList",DataDropDownList.class);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"HIDING original dctm document type list",null,null);
     m_dctmtypelist.setVisible(false);
     // get reference to type dropdownlist
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up the dropdown",null,null);
     m_typeList = (DropDownList) getControl("mrcsdoctypes", com.documentum.web.form.control.DropDownList.class);
     m_typeList.setMutable(true);
     // populate the list
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"populating dropdown",null,null);
     for (int i = 0; i < doctypes.size(); i++) {
         Option newopt = new Option();
         newopt.setLabel((String) doctypes.get(i)); // should I do a decode to a descriptionattribute from config?
         newopt.setValue((String) doctypes.get(i));
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding option " + newopt.getLabel(),null,null);
         m_typeList.addOption(newopt);
     }
     // if the dropdown defaulted to a particular type, simulate it was clicked...
     if (m_mrcsdoctype != null)
    	 m_typeList.setValue(m_mrcsdoctype);
     else 
    	 m_mrcsdoctype = m_typeList.getValue();
     if (m_mrcsdoctype == null) {
         m_systemtype = null;
     } else {
         // we can cheat with nulls as arguments since onSelectType doesn't use them...
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"dropdown was defaulted, calling onSelectType with empty args",null,null);
         initMrcsSelectType();
     }
 	
 }

 protected void initMrcsFormatControl()
 {
     //if(isDirectory())
     //{
     //    getFormatListControl(true).setVisible(false);
     //} else
     //{
     //    ScrollableResultSet resultSet = createFormatsResultSet();
     //    DataDropDownList formatList = getFormatListControl(true);
     //    formatList.getDataProvider().setScrollableResultSet(resultSet);
     //    formatList.setValue("unknown");
     //}
	 
	 /*-CONFIG-*/String m = "initMrcsFormatControl - ";
 	
     m_unknownFormatLabel.setVisible(false);

 	MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();        
     
     // determine the extension of the file
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"extracting file extension",null,null);
     String ext = UploadUtil.extractExtension(m_filePath);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"extension: "+ext,null,null);

     // get valid format list from docconfig
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting format list from docconfig",null,null);
     List formatlist = docconfig.getDocumentFormats(m_mrcsapp,m_gftype,m_mrcsdoctype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"composing DQL query...",null,null);
     String formatclause = "";
     for (int i=0; i < formatlist.size(); i++)
     {
         if (i > 0) formatclause += ",";
         formatclause += "'"+(String)formatlist.get(i) +"'";
     }
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"formatclause: "+formatclause,null,null);
     String dqlquery = "SELECT name,description,dos_extension FROM dm_format "+
                       "WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') AND "+
                       "name IN ("+formatclause+") ORDER BY description";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"dql query: "+dqlquery,null,null);    
     DfQuery dfquery = new DfQuery();
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting dql query",null,null);    
     dfquery.setDQL(dqlquery);
     
     // get reference to formatlist
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up the formatlist control",null,null);    
     m_formatList = (DropDownList) getControl("formatList", com.documentum.web.form.control.DropDownList.class);
     m_formatList.setMutable(true);
     // empty it
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"clearing it out...",null,null);    
     m_formatList.clearOptions();
     
     boolean formatfound = false;
     String formatfoundname = null;
     
     IDfCollection idfcollection = null;
     try {
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing dql query",null,null);
         idfcollection = dfquery.execute(getDfSession(), 0); 
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterating through result set",null,null);
         while (idfcollection.next()) 
         {
             String formatname = idfcollection.getString("name");
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format name: "+formatname,null,null);
             String formatdesc = idfcollection.getString("description");
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format desc: "+formatdesc,null,null);
             String formatextension = idfcollection.getString("dos_extension");
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format ext: "+formatextension,null,null);
                             
             // add list option!
             Option newopt = new Option();
             newopt.setLabel(formatdesc);
             newopt.setValue(formatname);
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding option " + newopt.getLabel(),null,null);
             m_formatList.addOption(newopt);
             if (formatextension.equals(ext))
             {
                 formatfound = true;
                 formatfoundname = formatname;                    
             }                                
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

         m_format = m_formatList.getValue();

     } catch (DfException dfexception) {
         /*-ERROR-*/DfLogger.getRootLogger().error(m+"exception thrown during DQL execution",dfexception);
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
 
 protected ScrollableResultSet createTypesResultSet()
 {
	 /*-CONFIG-*/String m="createTypesResultSet - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);

 	try {
        ScrollableResultSet resultSet;
        String baseObjectType = getBaseDocbaseType();
        if(baseObjectType != null && baseObjectType.length() > 0)
        {
            resultSet = getTypes(baseObjectType, "dm_folder");
        } else
            resultSet = getTypes(getDocumentBaseType(), "dm_folder");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DONE",null,null);
        return resultSet;
 	} catch (DfException e) {
        /*-ERROR-*/DfLogger.error(this,m+"ERROR in createTypesResultSet",null,e);
 		throw new WrapperRuntimeException("Unable to query types from docbase!", e);
 	}
 }

 private ScrollableResultSet getTypes(String supertype, String notSupertype)
     throws DfException
 {
	 /*-CONFIG-*/String m="getTypes - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP, supertype: "+supertype,null,null);
     CachedResultSet resultSet = null;
     String typeKey = String.valueOf(supertype) + "," + String.valueOf(notSupertype);
     ServletRequest req = getPageContext().getRequest();
     TransientObjectWrapper tw = (TransientObjectWrapper)req.getAttribute(TYPES_RESULTSET_REQ_CACHE);
     Map typesCache;
     if(tw != null && (typesCache = (Map)tw.get()) != null)
     {
         resultSet = (CachedResultSet)typesCache.get(typeKey);
     } else
     {
         typesCache = new HashMap();
         tw = new TransientObjectWrapper(typesCache);
         req.setAttribute(TYPES_RESULTSET_REQ_CACHE, tw);
     }
     if(resultSet == null)
     {
         ScrollableResultSet rs = Types.getTypes(supertype, notSupertype);
         rs.sort("description", 0, 1);
         resultSet = new CachedResultSet(rs);
         typesCache.put(typeKey, resultSet);
     } else
     {
         resultSet = (CachedResultSet)resultSet.clone();
     }
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DONE",null,null);
     
     return resultSet;
 }

 public void onRender()
 {
	// CEM - we manually populate these via MRCS config values
	/*-CONFIG-*/String m="onRender - ";
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
    DataDropDownList list = getTypeListControl(false); // CEM for safety reasons...we'll keep the original types list around until it is needed
    if(list != null)
        list.getDataProvider().setDfSession(getDfSession());
    super.onRender();
 }

 public void onRenderEnd()
 {
	 /*-CONFIG-*/String m="onRenderEnd - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     super.onRenderEnd();
     
     // CEM - this code was clearing out the format and type dropdowns when returning from setting a multi-value property in the import screen...I think...
     
     // CEM - I think this is needed if we're going to keep the object dropdown hidden behind-the-scenes
     DataDropDownList list = getTypeListControl(false);
     if(list != null)
     {
         list.setMutable(true);
         list.clearOptions();
     }
//     list = getFormatListControl(false);
//     if(list != null)
//     {
//         list.setMutable(true);
//         list.clearOptions();
//     }
//     
//     Container cont = (Container)this.getContainer();
//     Control nextbutton = cont.getControl("next", com.documentum.web.form.control.Button.class);
//     nextbutton.setEnabled(true);
//     Control okbutton = cont.getControl("ok", com.documentum.web.form.control.Button.class);
//     okbutton.setEnabled(false);

 }


 public String[] getEventNames()
 {
	 /*-CONFIG-*/String m="getEventNames - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     return Util.concatarray(super.getEventNames(), new String[] {"onsynctype"});
 }

 public String getEventHandlerMethod(String strEvent)
 {
	 /*-CONFIG-*/String m="getEventHandlerMethod - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     String strMethod = null;
     if(strEvent.equals("onsynctype"))
         strMethod = "onSyncType";
     else
         strMethod = super.getEventHandlerMethod(strEvent);
     return strMethod;
 }

 public Control getEventHandler(String strEvent)
 {
	 /*-CONFIG-*/String m="getEventHandler - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     Control handler = null;
     if(strEvent.equals("onsynctype"))
         handler = this;
     else
         handler = super.getEventHandler(strEvent);
     return handler;
 }



 public void onSyncType(Control control, ArgumentList arg)
 {
	 /*-CONFIG-*/String m="onSyncType - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     boolean bFound;
     String strInitialValue;
     bFound = false;
     String strValue = arg.get("newType");
     strInitialValue = arg.get("initType");
     DocbaseObject docbaseObj = getDocbaseObjectControl(false);
     ScrollableResultSet typesResultset = createTypesResultSet();
     if(!(typesResultset == null || typesResultset.getResultsCount() <= 0))
     {
	     typesResultset.setCursor(-1);
	     int nTypeNameColumn = typesResultset.findColumn("type_name");
	     String strType;
	     while (typesResultset.next())
	     {
	    	 strType = (String)typesResultset.getObject(nTypeNameColumn);
	    	 if (strType.equals(strValue)) {
	    	     bFound = true;
	    	     if(docbaseObj != null)
	    	         docbaseObj.setType(strValue);
	    	     break;
	    	 }
	     }
     }

     if(!bFound)
     {
         DataDropDownList list = getTypeListControl(false);
         if(list == null)
             list = getTypeListControl(true);
         list.setValue(strInitialValue);
     }
 }


 public void onSelectFormat(DropDownList dropdownlist, ArgumentList arg)
 {
     /*-CONFIG-*/String m = "onSelectFormat";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting selcted format, previous format: "+m_format,null,null);
	 m_format = dropdownlist.getValue();
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format selected: "+m_format,null,null);
 }

 public void onSelectType(DropDownList dropdownlist, ArgumentList arg)
 {
     /*-CONFIG-*/String m = "onSelectType";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" onSelectType for DCTM doctype dropdown invoked",null,null);
     m_systemtype = m_dctmtypelist.getValue();
     
 }

 public void initMrcsSelectType()
 {
	 // basically identical to onMrcsSelectType without the pesky event firing
     /*-CONFIG-*/String m = "initMrcsSelectType - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" initilizing for MRCS doctype dropdown invoked",null,null);
 	
	 MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
 	
     // get the selected Mrcs doctype
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting selected mrcs doctype",null,null);
     m_mrcsdoctype = m_typeList.getValue();     
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype: " + m_mrcsdoctype,null,null);     

     // lookup the equivalent DCTM systype
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"translating to system type",null,null);
     m_systemtype = docconfig.getDocumentSystemType(m_mrcsapp, m_gftype, m_mrcsdoctype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SYNCING with dctm doctype list control (hidden)",null,null);
     if (m_dctmtypelist.getValue() == null || !m_dctmtypelist.getValue().equals(m_systemtype))
     {
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SYNCING with dctm doctype list control - updating to: "+m_systemtype,null,null);
    	 m_dctmtypelist.setValue(m_systemtype);
     }
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"systype: " + m_systemtype,null,null);

     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" SET docbaseobject's type  in context ?default type?",null,null);     
     DocbaseObject docbaseObj = getDocbaseObjectControl(false);
     if(docbaseObj != null)
     {
         docbaseObj.setType(m_dctmtypelist.getValue());
         getContext().set("type", m_dctmtypelist.getValue());
     }
     

     // populate the formats dropdown since new doctype selected
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"refreshing format control",null,null);
     initMrcsFormatControl();
 }
 
 public void forceMrcsInitFormats()
 {
	 /*-CONFIG-*/String m = "forceMrcsInitFormats - ";	 	
 	MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();        
     
     // determine the extension of the file
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"extracting file extension from "+m_filePath,null,null);
     String ext = UploadUtil.extractExtension(m_filePath);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"extension: "+ext,null,null);

     // get valid format list from docconfig
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting format list from docconfig for doctype "+m_mrcsdoctype,null,null);
     List formatlist = docconfig.getDocumentFormats(m_mrcsapp,m_gftype,m_mrcsdoctype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"composing DQL query...",null,null);
     String formatclause = "";
     for (int i=0; i < formatlist.size(); i++)
     {
         if (i > 0) formatclause += ",";
         formatclause += "'"+(String)formatlist.get(i) +"'";
     }
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"formatclause: "+formatclause,null,null);
     String dqlquery = "SELECT name,description,dos_extension FROM dm_format "+
                       "WHERE is_hidden=0 AND name NOT IN ('jpeg_lres', 'jpeg_th') AND "+
                       "name IN ("+formatclause+") ORDER BY description";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"dql query: "+dqlquery,null,null);    
     DfQuery dfquery = new DfQuery();
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"setting dql query",null,null);    
     dfquery.setDQL(dqlquery);
     
     // get reference to formatlist
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"looking up the formatlist control",null,null);    
     m_formatList = (DropDownList) getControl("formatList", com.documentum.web.form.control.DropDownList.class);
     // empty it
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"clearing it out...",null,null);    
     m_formatList.clearOptions();
     
     boolean formatfound = false;
     String formatfoundname = null;
     
     IDfCollection idfcollection = null;
     try {
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"executing dql query",null,null);
         idfcollection = dfquery.execute(getDfSession(), 0); 
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"iterating through result set",null,null);
         while (idfcollection.next()) 
         {
             String formatname = idfcollection.getString("name");
             String formatdesc = idfcollection.getString("description");
             String formatextension = idfcollection.getString("dos_extension");
                             
             // add list option!
             Option newopt = new Option();
             newopt.setLabel(formatdesc);
             newopt.setValue(formatname);
             m_formatList.addOption(newopt);
             if (formatextension.equals(ext))
             {
                 formatfound = true;
                 formatfoundname = formatname;                    
             }                                
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
         m_format = m_formatList.getValue();

     } catch (DfException dfexception) {
         /*-ERROR-*/DfLogger.getRootLogger().error(m+"exception thrown during DQL execution",dfexception);
         setReturnError("MSG_MRCS_IMPORT_FORMAT_LOOKUP_ERROR", null, dfexception);
         ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_FORMAT_LOOKUP_ERROR", dfexception);
         throw new WrapperRuntimeException("Unable to query format types from docbase!", dfexception);
     } finally {
         try {
             if (idfcollection != null)
                 idfcollection.close();
         } catch (DfException dfexception1) {
         }
     } }
 
 public void forceMrcsDocumentType()
 {
     /*-CONFIG-*/String m = "forceMrcsDocumentType - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" forcing the setting of the document type widgets and values",null,null);
	 MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
 	
     // get the selected Mrcs doctype
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting selected mrcs doctype",null,null);
     m_mrcsdoctype = m_typeList.getValue();     
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype: " + m_mrcsdoctype,null,null);     

     // lookup the equivalent DCTM systype
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"translating to system type",null,null);
     m_systemtype = docconfig.getDocumentSystemType(m_mrcsapp, m_gftype, m_mrcsdoctype);
     ///*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SYNCING with dctm doctype list control (hidden)",null,null);
     //if (m_dctmtypelist.getValue() == null || !m_dctmtypelist.getValue().equals(m_systemtype))
     //{
     //    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SYNCING with dctm doctype list control - updating to: "+m_systemtype,null,null);
     //    m_dctmtypelist.setValue(m_systemtype);
     //}
     // forgot to assert the format...
     m_format = m_formatList.getValue();
     
 }
 
 public void onMrcsSelectType(DropDownList dropdownlist, ArgumentList arg)
 {
 	 // CEM - override, we need to manually extract proper formats from MRCS config
     //ArgumentList eventArgs = new ArgumentList();
     //eventArgs.add("type", list.getValue());
     //eventArgs.add("objectName", getObjectNameTextControl(false).getValue());
     //eventArgs.add("format", getFormatListControl(false).getValue());
     //Form form = getForm();
     //form.fireEvent("onchangeobjecttype", eventArgs);
     //DocbaseObject docbaseObj = getDocbaseObjectControl(false);
     //if(docbaseObj != null)
     //{
     //    docbaseObj.setType(list.getValue());
     //    getContext().set("type", list.getValue());
     //}
     /*-CONFIG-*/String m = "onMrcsSelectType - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" onSelectType for MRCS doctype dropdown invoked",null,null);
 	
	 MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
 	
     // get the selected Mrcs doctype
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting selected mrcs doctype",null,null);
     m_mrcsdoctype = m_typeList.getValue();     
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doctype: " + m_mrcsdoctype,null,null);     

     // lookup the equivalent DCTM systype
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"translating to system type",null,null);
     m_systemtype = docconfig.getDocumentSystemType(m_mrcsapp, m_gftype, m_mrcsdoctype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SYNCING with dctm doctype list control (hidden)",null,null);
     if (m_dctmtypelist.getValue() == null || !m_dctmtypelist.getValue().equals(m_systemtype))
     {
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"SYNCING with dctm doctype list control - updating to: "+m_systemtype,null,null);
    	 m_dctmtypelist.setValue(m_systemtype);
     }
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"systype: " + m_systemtype,null,null);

     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" firing event for object type change - changed to: "+m_systemtype,null,null);
     ArgumentList eventArgs = new ArgumentList();
     String dctmtype = m_dctmtypelist.getValue();
     eventArgs.add("type", dctmtype);
     eventArgs.add("objectName", m_mrcsdocumentname);
     eventArgs.add("format", m_mrcsdoctype); // "format" will actually be the mrcs doctype
     Form form = getForm();
     form.fireEvent("onchangeobjecttype", eventArgs);
     
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+" SET docbaseobject's type  in context ?default type?",null,null);     
     DocbaseObject docbaseObj = getDocbaseObjectControl(false);
     if(docbaseObj != null)
     {
         docbaseObj.setType(m_dctmtypelist.getValue());
         getContext().set("type", m_dctmtypelist.getValue());
     }

     
     // populate the formats dropdown since new doctype selected
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"refreshing format control",null,null);
     initMrcsFormatControl();
 }
 
 public boolean hasCustomPage() 
 {
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.hasMoreCustomPages - top",null,null);
 	MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
     // query configuration to see if there is a custom page
     String custompage = docconfig.getDocumentComponent(m_mrcsapp, m_gftype, m_mrcsdoctype);
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.hasMoreCustomPages - returned custom page: "+custompage,null,null);
     return custompage != null;
 }

 public String setComponentCustomPage() 
 {
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.setComponentCustomPage - getting custom component page",null,null);
 	MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
     String custompage = docconfig.getDocumentComponent(m_mrcsapp, m_gftype, m_mrcsdoctype);
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
 	MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
     MrcsPlugin pageprocessor = docconfig.getDocumentComponentProcessor(m_mrcsapp, m_gftype, m_mrcsdoctype);
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
     m_customdata = customdata;
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
//         final IDfSessionManager manager = SessionManagerHttpBinding.getNewDfSessionManager();
//         IMrcsDocumentSBO docsbo = (IMrcsDocumentSBO) DfClient.getLocalClient().newService(IMrcsDocumentSBO.class.getName(), manager);
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - getting current user/pass",null,null);
         IDfSession session = getDfSession();
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - invoking doc naming service",null,null);
         MrcsDocumentSBO docsbo = MrcsDocumentSBO.getService();
         newdocname = docsbo.generateDocumentName(session, m_mrcsapp, m_gftype, m_mrcsdoctype, folderId, m_customdata);
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.generateMrcsName - doc name generated successfully: " + newdocname,null,null);
     } catch (Exception e) {
         /*-ERROR-*/DfLogger.getRootLogger().error("MRCS:MrcsImportContent.generateMrcsName - exception thrown MRCS name generation",e);
         setReturnError("MSG_MRCS_IMPORT_NAME_GENERATION_ERROR", null, e);
         ErrorMessageService.getService().setNonFatalError(this, "MSG_MRCS_IMPORT_NAME_GENERATION_ERROR", e);
     }
     // set the objectname on this MrcsImportContent component
     m_mrcsdocumentname = newdocname;
     return newdocname;
 }
 
 public void processImportedDocument(String newdocid) 
 {
     // we are at post-import of the content, need to perform MRCS post-processing
     // invoke document SBO to perform the name generation
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - post-document import, need to attach lifecycle and execute plugins",null,null);
     try {
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,"MRCS:MrcsImportContent.processImportedDocument - instantiating document SBO service",null,null);
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
 

 public boolean canCommitChanges()
 {
     //return isObjectNameValid();
	 return true;
 }

 
 public boolean onCommitChanges()
 {
	 /*-CONFIG-*/String m="onCommitChanges - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     return getIsValid();
 }

 protected String getFolderId()
 {
     return folderId;
 }

 protected String getFilePathSelection()
 {
     return m_filePath;
 }

 protected String getObjectNameSelection()
 {
 	// CEM - MRCS autogenerates object names...
 	return this.m_mrcsdocumentname;
 }

 protected String getFormatSelection()
 {
     String strFormat;
  	 strFormat = m_format;
     return strFormat;
 }

 protected String getTypeSelection()
 {
	 /*-CONFIG-*/String m="getTypeSelection - ";
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     //String strType = getTypeListControl(true).getValue();
     //return strType;
	 return m_systemtype;
 }

 protected Map getObjectAttributes()
 {
    /*-CONFIG-*/String m="getObjectAttributes - ";
    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
 	try { 
	        Map userFilledValues;
	        IDfType type = getDfSession().getType(getTypeSelection());
	        DocbaseObjectAttributeReader reader = new DocbaseObjectAttributeReader(type);
	        visitDepthFirst(reader);
	        userFilledValues = reader.getProperties();
	        // add mrcs attrs...
	        userFilledValues.put("mrcs_application",m_mrcsapp);
	        userFilledValues.put("mrcs_config",m_mrcsdoctype);
	        userFilledValues.put("mrcs_folder_config",m_gftype);
	        if(m_attributes != null)
	        {
	            Set entries = m_attributes.entrySet();
	            Iterator iterator = entries.iterator();
	            do
	            {
	                if(!iterator.hasNext())
	                    break;
	                java.util.Map.Entry entry = (java.util.Map.Entry)iterator.next();
	                String attributeName = (String)entry.getKey();
	                if(!userFilledValues.containsKey(attributeName))
	                {
	                    int attrIndex = type.findTypeAttrIndex(attributeName);
	                    if(attrIndex >= 0)
	                    {
	                        IDfAttr attr = type.getTypeAttr(attrIndex);
	                        if(attr.isRepeating())
	                        {
	                            userFilledValues.put(attributeName, entry.getValue());
	                        } else
	                        {
	                            List valueAsList = (List)entry.getValue();
	                            userFilledValues.put(attributeName, valueAsList.get(0));
	                        }
	                    }
	                }
	            } while(true);
	        }
	        return userFilledValues;
 	} catch (DfException e) {
 		throw new WrapperRuntimeException("Fail to retrieve docbase type " + getTypeSelection(), e);
 	}
 }

 static boolean isXmlParseable(String strFilenameWithPath, IDfSession session)
     throws RuntimeException
 {
     return XmlUtil.isXmlExtension(strFilenameWithPath == null ? "" : strFilenameWithPath, session);
 }

 /*
 protected void initSelectedFormat()
 {
     try
     {
         String format = getDefaultFormat();
         if(format == null)
             format = guessFormatFromFileExtension();
         DataDropDownList list = getFormatListControl(true);
         list.setValue(format);
         String strSelectedFormat = getFormatListControl(true).getValue();
         if(strSelectedFormat != null && strSelectedFormat.equals("unknown"))
             getFormatWarningLabelControl(true).setVisible(true);
     }
     catch(DfException e)
     {
         throw new WrapperRuntimeException(e);
     }
 }
 */

 private String guessFormatFromFileExtension()
     throws DfException
 {
	    /*-CONFIG-*/String m="guessFormatFromFileExtension - ";
	    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     String strFileExtension = UploadUtil.extractExtension(getFilePathSelection());
     if(strFileExtension != null)
     {
         String format = FormatService.getInstance().guessFormatFromFileExtension(strFileExtension);
         if(format != null)
             return format;
     }
     return "unknown";
 }

 protected void setFolderId(String folderId)
 {
     this.folderId = folderId;
 }

 protected void setFilePath(String filePath)
 {
     m_filePath = filePath;
 }

 public String getLocalFilePath()
 {
     return m_localFilePath;
 }

 public void setLocalFilePath(String path)
 {
     m_localFilePath = path;
 }

 protected String getBaseDocbaseType()
 {
	 /*-CONFIG-*/String m="getBaseDocbaseType - ";
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP - returning basetype "+m_baseDocbaseType,null,null);
     return m_baseDocbaseType;
 }

 private String getDefaultFolderType()
 {
     String defaultType = lookupString("folder-docbase-type");
     if(typeExists(defaultType))
         return defaultType;
     else
         return "dm_folder";
 }

 private String getDocumentBaseType()
 {
	 /*-CONFIG-*/String m="getDocumentBaseType - ";
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP - looking up default base doc type",null,null);
     String documentBaseType = lookupString("document-docbase-base-type");
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"BASE TYPE: "+documentBaseType,null,null);
     if(typeExists(documentBaseType))
         return documentBaseType;
     else
         return "dm_document";
 }

 private String getDefaultDocumentType()
 {
	 /*-CONFIG-*/String m="getDefaultDocumentType - ";
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
     String defaultType = lookupString("document-docbase-type");
     if(typeExists(defaultType))
         return defaultType;
     else
         return "dm_document";
 }

 public String getParentPathSelection()
 {
     return m_parentPath;
 }

 public String getObjectName()
 {
     return this.m_mrcsdocumentname;
 }

 public void setParentPath(String parentPath)
 {
     m_parentPath = parentPath;
 }

 public void setObjectName(String objectName)
 {
     m_mrcsdocumentname = objectName;
 }

 protected void setBaseDocbaseType(String baseDocbaseType)
 {
     m_baseDocbaseType = baseDocbaseType;
 }

 public void setDirectory(boolean directory)
 {
     m_directory = directory;
 }

 private boolean typeExists(String type)
 {
	 /*-CONFIG-*/String m="typeExists - ";
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP - see if type exists: "+type,null,null);
 	try { 
	        if(type == null || type.length() == 0)
	            return false;
	        IDfType dftype = getDfSession().getType(type);
	        return dftype != null;
 	} catch (DfException ignore) {}
     return false;
 }

 protected DropDownList getMrcsTypeListControl(boolean create)
 {
	 /*-CONFIG-*/String m="getMrcsTypeListControl - ";
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
  	 if (m_typeList == null)
         m_typeList = (DropDownList) getControl("mrcsdoctypes", com.documentum.web.form.control.DropDownList.class);
     return m_typeList;
 }

 protected DataDropDownList getTypeListControl(boolean create)
 {
	 /*-CONFIG-*/String m="getTypeListControl - ";
	 /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"TOP",null,null);
 	 if (m_dctmtypelist == null)
 		m_dctmtypelist = (DataDropDownList) getControl("objectTypeList", DataDropDownList.class);
     return m_dctmtypelist;
 }

 protected DropDownList getFormatListControl(boolean create)
 {
 	if (m_formatList == null)
         m_formatList = (DropDownList) getControl("formatList", com.documentum.web.form.control.DropDownList.class);    		
     return m_formatList;
 }

 protected Label getFormatWarningLabelControl(boolean create)
 {
     return (Label)getControl0("unknownFormatWarningLabel", create, com.documentum.web.form.control.Label.class);
 }

 protected Label getFilenameLabelControl(boolean create)
 {
     return (Label)getControl0("filename", create, com.documentum.web.form.control.Label.class);
 }

 protected DocbaseObject getDocbaseObjectControl(boolean create)
 {
     return (DocbaseObject)getControl0("docbaseObj", create, com.documentum.web.formext.control.docbase.DocbaseObject.class);
 }

 private Control getControl0(String name, boolean create, Class cl)
 {
     return create ? getControl(name, cl) : getControl(name);
 }

 private static String[] buildColumnArray(ResultSet resultSet)
 {
 	try { 
	        String retArr[];
	        if(resultSet == null)
	            throw new WrapperRuntimeException(new NullPointerException());
	        ResultSetMetaData metaData = resultSet.getMetaData();
	        int size = metaData.getColumnCount();
	        retArr = new String[size];
	        for(int i = 0; i < size; i++)
	            retArr[i] = metaData.getColumnName(i + 1);
	
	        return retArr;
 	} catch (SQLException e) {
 		throw new WrapperRuntimeException(e);
 	}
 }
 
 // play with onNextPage in the component to see if we can influence the Next,Previous and Finish buttons to behave
 public boolean onNextPage()
 {
     return true;
 }


 // this is a flag to try to get Import to behave correctly on a document type change.
 //    doc type change fires an event which invokes onInit(), which messes up the values just selected... 
 public boolean typechangeflag = false;

 private static final String UNKNOWN_FORMAT = "unknown";
 protected static final String EVENT_ONSYNCTYPE = "onsynctype";
 private static final String HANDLER_ONSYNCTYPE = "onSyncType";
 private String folderId;
 private String m_filePath;
 private String m_parentPath;
 private String m_localFilePath;
 private boolean m_directory;
 private String m_baseDocbaseType;
 private String m_format;
 private Map m_attributes;
 private ArgumentList m_attributesArgs;
 private static final String FORMATS_RESULTSET_REQ_CACHE;
 private static final String TYPES_RESULTSET_REQ_CACHE;
 
 public String m_gftype, m_mrcsdoctype, m_mrcsapp, m_systemtype, m_mrcsdocumentname;
 public Map m_customdata;
 public boolean m_onCustomPage = false;
 public boolean getOnCustomPage() {return m_onCustomPage;}
 public void setOnCustomPage(boolean b) {m_onCustomPage = b;}

 protected DataDropDownList m_dctmtypelist;
 private DropDownList m_typeList;
 private DropDownList m_formatList;
 private Label m_unknownFormatLabel;
 

 static 
 {
     FORMATS_RESULTSET_REQ_CACHE = (com.medtronic.documentum.mrcs.client.MrcsImportContentUCF.class).getName() + ".formatsResultSetCache";
     TYPES_RESULTSET_REQ_CACHE = (com.medtronic.documentum.mrcs.client.MrcsImportContentUCF.class).getName() + ".typesResultSetCache";
 }


}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/importcontent/ImportContent.class


	TOTAL TIME: 47 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't fully decompile method findColumn
Couldn't resolve all exception handlers in method findColumn
Couldn't fully decompile method createFormatsResultSet
Couldn't resolve all exception handlers in method createFormatsResultSet
Couldn't fully decompile method createTypesResultSet
Couldn't resolve all exception handlers in method createTypesResultSet
Couldn't fully decompile method getObjectAttributes
Couldn't resolve all exception handlers in method getObjectAttributes
Couldn't fully decompile method typeExists
Couldn't resolve all exception handlers in method typeExists
Couldn't fully decompile method buildColumnArray
Couldn't resolve all exception handlers in method buildColumnArray

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

********************************/