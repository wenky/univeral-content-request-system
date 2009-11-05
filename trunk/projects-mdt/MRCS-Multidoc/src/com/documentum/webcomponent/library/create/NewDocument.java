/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   NewDocument.java

package com.documentum.webcomponent.library.create;

import com.documentum.com.IDfClientX;
import com.documentum.fc.client.*;
import com.documentum.fc.common.*;
import com.documentum.operations.IDfDeleteOperation;
import com.documentum.operations.IDfOperationError;
import com.documentum.services.subscriptions.ISubscriptions;
import com.documentum.web.common.*;
import com.documentum.web.form.*;
import com.documentum.web.form.control.*;
import com.documentum.web.form.control.databound.*;
import com.documentum.web.formext.component.Component;
import com.documentum.web.formext.docbase.FolderUtil;
import com.documentum.web.formext.docbase.ServerUtil;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.web.util.DfcUtils;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.locator.LocatorItemResultSet;
import com.documentum.webcomponent.library.messages.MessageService;
import com.documentum.webcomponent.library.subscription.SubscriptionsHttpBinding;
import java.util.*;

// Referenced classes of package com.documentum.webcomponent.library.create:
//            CreateService

public class NewDocument extends Component
    implements IReturnListener
{

    public NewDocument()
    {
        m_strFolderId = null;
        m_typeList = null;
        m_formatList = null;
        m_templateList = null;
        m_bAllowFormatChoice = true;
        m_strNewObjectId = null;
        m_bShowOptions = false;
        m_bCreateSuccess = false;
        m_strNewType = null;
        m_strBaseType = null;
        m_labelNormalCharsNeeded = null;
        m_bConfigDefaultTypeAvailable = false;
        m_bConfigDefaultFormatAvailable = false;
    }

    public void onInit(ArgumentList arg)
    {
        super.onInit(arg);
        String objectIdArg = arg.get("objectId");
        try
        {
            Panel locationPanel = (Panel)getControl("location_panel", com.documentum.web.form.control.Panel.class);
            IDfSession dfSession = getDfSession();
            DfId dfId = new DfId(objectIdArg);
            IDfSysObject dfSysObject = (IDfSysObject)dfSession.getObject(dfId);
            if(dfSysObject.isVirtualDocument())
            {
                locationPanel.setVisible(true);
                String folderPath = FolderUtil.getPrimaryFolderPath(dfSysObject.getObjectId().getId());
                setLocationId(FolderUtil.getFolderId(folderPath));
                if(folderPath != null)
                {
                    Text locationToSave = (Text)getControl("locationToSave", com.documentum.web.form.control.Text.class);
                    locationToSave.setValue(FolderUtil.formatFolderPath(folderPath));
                }
            } else
            {
                IDfType dfType = dfSysObject.getType();
                String typeName = dfType.getName();
                if(typeName.equals("dm_folder") || dfType.isSubTypeOf("dm_folder"))
                {
                    setLocationId(objectIdArg);
                    locationPanel.setVisible(false);
                    Checkbox checkboxMakeVirtual = (Checkbox)getControl("makevirtual", com.documentum.web.form.control.Checkbox.class);
                    checkboxMakeVirtual.setVisible(false);
                } else
                {
                    throw new IllegalArgumentException();
                }
            }
        }
        catch(DfException dfException)
        {
            throw new WrapperRuntimeException(dfException);
        }
        m_labelNormalCharsNeeded = (Label)getControl("labelnormalcharsneeded", com.documentum.web.form.control.Label.class);
        if(m_labelNormalCharsNeeded != null)
            m_labelNormalCharsNeeded.setVisible(false);
        m_typeList = (DataDropDownList)getControl("objectTypeList", com.documentum.web.form.control.databound.DataDropDownList.class);
        initTypeCombo();
        if(m_bAllowFormatChoice)
        {
            m_formatList = (DataDropDownList)getControl("formatList", com.documentum.web.form.control.databound.DataDropDownList.class);
            updateFormatCombo(m_typeList.getValue());
            m_templateList = (DataDropDownList)getControl("templateList", com.documentum.web.form.control.databound.DataDropDownList.class);
            if(m_formatList != null)
                setupTemplateList(m_typeList.getValue(), m_formatList.getValue());
        }
        String strFormat = arg.get("contentType");
        if(strFormat != null && strFormat.length() > 0)
        {
            m_formatList.setValue(strFormat);
            onSelectFormat(m_formatList, arg);
        }
        Panel panel = (Panel)getControl("optionspanel", com.documentum.web.form.control.Panel.class);
        if(panel != null)
            panel.setVisible(false);
    }

    public void onRender()
    {
        super.onRender();
        m_typeList.getDataProvider().setDfSession(getDfSession());
    }

    public boolean onCommitChanges()
    {
        validate();
        if(getIsValid())
        {
            boolean bDocumentExists = getNewObjectId() != null;
            boolean bDocuemtCreated = false;
            if(!bDocumentExists)
                bDocuemtCreated = createNewObject();
            if(bDocumentExists || bDocuemtCreated)
            {
                try
                {
                    IDfSysObject newObj = (IDfSysObject)getDfSession().getObject(new DfId(getNewObjectId()));
                    if(newObj.isCheckedOutBy(null))
                        newObj.cancelCheckout();
                }
                catch(DfException e)
                {
                    throw new WrapperRuntimeException(e);
                }
                if(m_bCreateSuccess)
                {
                    handleSubscription();
                    handleMakeVirtual();
                    displaySuccessMessage();
                    return true;
                }
            }
        }
        return false;
    }

    public boolean onCancelChanges()
    {
        if(getNewObjectId() != null)
        {
            deleteObject(getNewObjectId(), getDfSession(), false);
            setNewObjectId(null);
        }
        return true;
    }

    public boolean onCancelChanges(IDfSessionManager manager)
    {
        IDfSession dfSession = null;
    	try { 
	        if(getNewObjectId() == null)
	            return true;
	        try
	        {
	            DfId newObjectId = new DfId(getNewObjectId());
	            String docbase = DocbaseUtils.getDocbaseNameFromId(newObjectId);
	            dfSession = manager.getSession(docbase);
	            deleteObject(getNewObjectId(), dfSession, true);
	            setNewObjectId(null);
	        }
	        catch(DfException dfException)
	        {
	            if(dfSession != null)
	                manager.release(dfSession);
	            return false;
	        }
	        if(dfSession != null)
	            manager.release(dfSession);
    	} catch (Exception exception) {
	        if(dfSession != null)
	            manager.release(dfSession);
	        throw new RuntimeException(exception);
    	}
	    return true;
    }

    public void onSelectType(DataDropDownList typeList, ArgumentList arg)
    {
        String strType = typeList.getValue();
        updateFormatCombo(strType);
        if(m_formatList != null)
            setupTemplateList(strType, m_formatList.getValue());
    }

    public void onSelectFormat(DataDropDownList formatList, ArgumentList arg)
    {
        String strFormat = formatList.getValue();
        String strType = m_typeList.getValue();
        setupTemplateList(strType, strFormat);
    }

    public void onClickShowHideOptions(Link linkShowHideOptions, ArgumentList arg)
    {
        m_bShowOptions = !m_bShowOptions;
        Panel panel = (Panel)getControl("optionspanel", com.documentum.web.form.control.Panel.class);
        if(m_bShowOptions)
        {
            panel.setVisible(true);
            linkShowHideOptions.setLabel(getString("MSG_HIDE_OPTIONS"));
            linkShowHideOptions.setToolTip(getString("MSG_HIDE_OPTIONS_TIP"));
        } else
        {
            panel.setVisible(false);
            linkShowHideOptions.setLabel(getString("MSG_SHOW_OPTIONS"));
            linkShowHideOptions.setToolTip(getString("MSG_SHOW_OPTIONS_TIP"));
        }
    }

    public void onReturn(Form form, Map map)
    {
        if(map != null)
        {
            Text locationToSave = (Text)getControl("locationToSave", com.documentum.web.form.control.Text.class);
            LocatorItemResultSet locatorItemResultSet = (LocatorItemResultSet)map.get("_locator_sel");
            if(locatorItemResultSet != null && locatorItemResultSet.first())
            {
                String selectedObjectId = locatorItemResultSet.getObject("r_object_id").toString();
                setLocationId(selectedObjectId);
                String fullPath = locatorItemResultSet.getObject("fullpath").toString();
                locationToSave.setValue(fullPath);
            }
        }
    }

    public String getNewObjectId()
    {
        return m_strNewObjectId;
    }

    public String getNewType()
    {
        return m_strNewType;
    }

    public boolean createNewObject()
    {
        if(getNewObjectId() == null)
        {
            m_bCreateSuccess = false;
            Text nameTextControl = (Text)getControl("attribute_object_name", com.documentum.web.form.control.Text.class);
            String strName = nameTextControl.getValue();
            DataDropDownList typeList = (DataDropDownList)getControl("objectTypeList", com.documentum.web.form.control.databound.DataDropDownList.class);
            String strType = typeList.getValue();
            if(strType == null || strType.length() == 0)
            {
                setReturnError("MSG_ERROR_TYPE_NULL", null, null);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_TYPE_NULL", null);
                return false;
            }
            DataDropDownList formatList = (DataDropDownList)getControl("formatList", com.documentum.web.form.control.databound.DataDropDownList.class);
            String strFormat = formatList.getValue();
            DataDropDownList templateList = (DataDropDownList)getControl("templateList", com.documentum.web.form.control.databound.DataDropDownList.class);
            String strTemplate = templateList.getValue();
            try
            {
                CreateService createSvc = new CreateService();
                if(isFormatAndTemplateSupplied())
                    setNewObjectId(createSvc.createDocument(getLocationId(), strTemplate, strType, strFormat, strName, false));
                else
                    setNewObjectId(createSvc.createObject(getLocationId(), strType, strName));
                setNewType(strType);
                IDfSysObject newObj = (IDfSysObject)getDfSession().getObject(new DfId(getNewObjectId()));
                newObj.checkout();
                m_bCreateSuccess = true;
            }
            catch(CreateService.CreateException e)
            {
                setReturnError("MSG_ERROR_CREATING_DOC", null, e);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_CREATING_DOC", e);
            }
            catch(DfException e)
            {
                setReturnError("MSG_ERROR_CREATING_DOC", null, e);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_CREATING_DOC", e);
            }
        }
        return m_bCreateSuccess;
    }

    public void validate()
    {
        getTopForm().validate();
        boolean bForbiddenChars = nameContainsOnlyForbiddenChars();
        if(bForbiddenChars)
            setInvalid();
        if(m_labelNormalCharsNeeded != null)
            m_labelNormalCharsNeeded.setVisible(bForbiddenChars);
    }

    public boolean isFormatAndTemplateSupplied()
    {
        boolean bRetVal = true;
        if(m_formatList != null && m_templateList != null)
        {
            String strFormat = m_formatList.getValue();
            String strTemplate = m_templateList.getValue();
            if(strFormat == null || strFormat.equals("") || strTemplate == null || strTemplate.length() == 0)
                bRetVal = false;
        } else
        {
            bRetVal = false;
        }
        return bRetVal;
    }

    public void onBrowse(Control button, ArgumentList args)
    {
        ArgumentList locatorArgs = new ArgumentList();
        locatorArgs.add("component", "allfolderlocator");
        locatorArgs.add("multiselect", "false");
        setComponentNested("folderlocatorcontainer", locatorArgs, getContext(), this);
    }

    protected void setAllowFormatChoice(boolean allowFormatChoice)
    {
        m_bAllowFormatChoice = allowFormatChoice;
    }

    protected void setNewObjectId(String newObjectId)
    {
        m_strNewObjectId = newObjectId;
    }

    protected void setNewType(String type)
    {
        m_strNewType = type;
    }

    protected void setObjectCreatedSuccessfully(boolean objectCreateSuccessfully)
    {
        m_bCreateSuccess = objectCreateSuccessfully;
    }

    protected String getLocationId()
    {
        return m_strFolderId;
    }

    protected void setLocationId(String id)
    {
        m_strFolderId = id;
    }

    private void handleSubscription()
    {
        Checkbox checkboxSubscribe = (Checkbox)getControl("subscribe", com.documentum.web.form.control.Checkbox.class);
        boolean subscribe = checkboxSubscribe.getValue();
        if(subscribe)
        {
            ISubscriptions subscriptions = (new SubscriptionsHttpBinding()).getSubscriptionsService();
            String strDocbase = SessionManagerHttpBinding.getCurrentDocbase();
            if(subscriptions.isInstalled(strDocbase))
            {
                subscriptions.subscribe(strDocbase, getNewObjectId());
            } else
            {
                setReturnError("MSG_SUBSCRIPTIONS_NOT_INSTALLED_ERROR", null, null);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_SUBSCRIPTIONS_NOT_INSTALLED_ERROR", null);
            }
        }
    }

    private void handleMakeVirtual()
    {
        Checkbox checkboxMakeVirtual = (Checkbox)getControl("makevirtual", com.documentum.web.form.control.Checkbox.class);
        boolean makeVirtual = checkboxMakeVirtual.getValue();
        if(makeVirtual)
            try
            {
                IDfSession dfSession = getDfSession();
                DfId dfId = new DfId(getNewObjectId());
                IDfSysObject dfSysObject = (IDfSysObject)dfSession.getObject(dfId);
                dfSysObject.setIsVirtualDocument(true);
                if(dfSysObject.isCheckedOut())
                    dfSysObject.saveLock();
                else
                    dfSysObject.save();
            }
            catch(DfException dfException)
            {
                setReturnError("MSG_MAKE_VIRTUAL_ERROR", null, dfException);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_MAKE_VIRTUAL_ERROR", dfException);
            }
    }

    private void displaySuccessMessage()
    {
        Text nameTextControl = (Text)getControl("attribute_object_name", com.documentum.web.form.control.Text.class);
        String strName = nameTextControl.getValue();
        Object params[] = new Object[1];
        params[0] = strName;
        MessageService.addMessage(this, "MSG_CREATE_SUCCESS", params);
    }

    private void deleteObject(String strObjectId, IDfSession idfSession, boolean fIgnoreError)
    {
        try
        {
            IDfDeleteOperation deleteOperation = DfcUtils.getClientX().getDeleteOperation();
            deleteOperation.getProperties().putBoolean("RemoteMode", true);
            IDfSysObject sysObj = (IDfSysObject)idfSession.getObject(new DfId(strObjectId));
            if(sysObj.isCheckedOutBy(null))
                sysObj.cancelCheckout();
            if(sysObj.isFrozen())
            {
                sysObj.unfreeze(true);
                sysObj.save();
            }
            deleteOperation.add(sysObj);
            boolean executeSucceeded = deleteOperation.execute();
            if(!executeSucceeded && !fIgnoreError)
            {
                String strDeleteExecuteError = "";
                IDfList list = deleteOperation.getErrors();
                int count = list.getCount();
                if(count > 0)
                {
                    IDfOperationError opErr = (IDfOperationError)list.get(0);
                    strDeleteExecuteError = opErr.getMessage();
                }
                Exception e = new Exception(strDeleteExecuteError);
                setReturnError("MSG_ERROR_DELETING_DOC", null, e);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_DELETING_DOC", e);
            }
        }
        catch(DfException e)
        {
            if(!fIgnoreError)
            {
                setReturnError("MSG_ERROR_DELETING_DOC", null, e);
                WebComponentErrorService.getService().setNonFatalError(this, "MSG_ERROR_DELETING_DOC", e);
            }
        }
    }

    private void initTypeCombo()
    {
        boolean bIs4_2Docbase = ServerUtil.compareDocbaseVersion(4, 2);
        String strLang = LocaleService.getLocale().getLanguage();
        TableResultSet resultSet = null;
        if(bIs4_2Docbase)
        {
            resultSet = getTypeComboResultSet(strLang, true);
            if(resultSet.getResultsCount() < 1)
                resultSet = getTypeComboResultSet("en", true);
        } else
        {
            resultSet = getTypeComboResultSet(null, false);
        }
        m_typeList.getDataProvider().setResultSet(resultSet, null);
        boolean bHasResults = resultSet.getResultsCount() > 0;
        updateSelectedTypeOption(bHasResults);
    }

    private TableResultSet getTypeComboResultSet(String strLocale, boolean is4_2Docbase)
    {
        TableResultSet resultSet = new TableResultSet(new String[] {
            "type_name", "label_text"
        });
        String strTypeQuery = null;
        if(is4_2Docbase)
        {
            String strNlsKey = "en";
            if(strLocale != null && strLocale.length() > 0)
                strNlsKey = strLocale;
            String strBaseType = getBaseType();
            StringBuffer sbTypeQuery = new StringBuffer(300);
            sbTypeQuery.append("select b.type_name, b.label_text from dmi_type_info a, dmi_dd_type_info b where").append(" (any a.r_supertype='").append(strBaseType).append("' and not any r_supertype='dm_folder') and (a.r_type_name = b.type_name)").append(" and (b.nls_key = '").append(strNlsKey).append("') and (b.business_policy_id = '0000000000000000') and not (b.life_cycle = 3) order by 2,1");
            strTypeQuery = sbTypeQuery.toString();
        } else
        {
            strTypeQuery = "select r_type_name as type_name, r_type_name as label_text from dmi_type_info where any r_supertype='dm_sysobject' and not any r_supertype='dm_folder' order by 1";
        }
        IDfCollection iCollection = null;
        String strConfigDefaultType = getConfigDefaultType();
        try
        {
            IDfQuery query = DfcUtils.getClientX().getQuery();
            query.setDQL(strTypeQuery);
            iCollection = query.execute(getDfSession(), 0);
            do
            {
                if(!iCollection.next())
                    break;
                String strTypeName = iCollection.getString("type_name");
                if(!strTypeName.equals("dmc_comment") && !strTypeName.equals("dmc_notepage") && !strTypeName.equals("dmc_richtext"))
                {
                    String strLabel = iCollection.getString("label_text");
                    String strDescription = strLabel + " (" + strTypeName + ")";
                    resultSet.add(new String[] {
                        strTypeName, strDescription
                    });
                    if(!m_bConfigDefaultTypeAvailable && strConfigDefaultType != null && strConfigDefaultType.length() > 0 && strTypeName.equals(strConfigDefaultType))
                        m_bConfigDefaultTypeAvailable = true;
                }
            } while(true);
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(getString("MSG_TYPE_QUERY_FAILED"), e);
        }
        finally
        {
            try
            {
                if(iCollection != null)
                    iCollection.close();
            }
            catch(DfException e) { }
        }
        return resultSet;
    }

    private void updateFormatCombo(String strSelectedType)
    {
        if(m_formatList != null)
        {
            TableResultSet formatResultSet = new TableResultSet(new String[] {
                "name", "description"
            });
            String strIndirectlySelectableFormat = "";
            String strConfigDefaultFormat = getConfigDefaultFormat();
            if(strSelectedType != null && strSelectedType.length() > 0)
            {
                String strNoneFormat = getString("MSG_NONE_FORMAT");
                formatResultSet.add(new String[] {
                    "", strNoneFormat
                });
                StringBuffer sbFormatQuery = new StringBuffer(280);
                sbFormatQuery.append("SELECT name, description FROM dm_format WHERE name IN (");
                sbFormatQuery.append("SELECT a_content_type FROM dm_sysobject WHERE FOLDER('/Templates', DESCEND) AND r_object_type ");
                sbFormatQuery.append("= '");
                sbFormatQuery.append(strSelectedType);
                sbFormatQuery.append("'");
                sbFormatQuery.append(") AND is_hidden=0 ORDER BY description");
                IDfCollection iCollection = null;
                IDfQuery query = DfcUtils.getClientX().getQuery();
                query.setDQL(sbFormatQuery.toString());
                String strName = null;
                String strDescription = null;
                try
                {
                    iCollection = query.execute(getDfSession(), 0);
                    for(int nIdx = 0; iCollection.next(); nIdx++)
                    {
                        strName = iCollection.getString("name");
                        strDescription = iCollection.getString("description");
                        formatResultSet.add(new String[] {
                            strName, strDescription
                        });
                        if(nIdx == 0)
                            strIndirectlySelectableFormat = strName;
                        if(!m_bConfigDefaultFormatAvailable && strConfigDefaultFormat != null && strName.equals(strConfigDefaultFormat))
                            m_bConfigDefaultFormatAvailable = true;
                    }

                }
                catch(DfException e)
                {
                    throw new WrapperRuntimeException(getString("MSG_FORMAT_QUERY_FAILED"), e);
                }
                finally
                {
                    try
                    {
                        if(iCollection != null)
                            iCollection.close();
                    }
                    catch(DfException e) { }
                }
            }
            m_formatList.getDataProvider().setScrollableResultSet(formatResultSet);
            if(m_bConfigDefaultFormatAvailable)
                m_formatList.setValue(strConfigDefaultFormat);
            else
                m_formatList.setValue(strIndirectlySelectableFormat);
        }
    }

    private void setupTemplateList(String strType, String strFormat)
    {
        if(m_templateList != null)
        {
            m_templateList.setValue(null);
            boolean bIsTemplateAvailable = false;
            boolean bIsValidFormat = strFormat != null && !strFormat.equals("");
            if(strType != null && strType.length() > 0 && bIsValidFormat)
            {
                StringBuffer sbTemplateQuery = new StringBuffer(215);
                sbTemplateQuery.append("SELECT r_object_id, object_name FROM dm_sysobject WHERE FOLDER('/Templates',DESCEND) AND r_object_type ");
                sbTemplateQuery.append("= '");
                sbTemplateQuery.append(strType);
                sbTemplateQuery.append("'");
                sbTemplateQuery.append(" AND a_content_type = '");
                sbTemplateQuery.append(strFormat);
                sbTemplateQuery.append("'");
                IDfCollection iCollection = null;
                IDfQuery query = DfcUtils.getClientX().getQuery();
                query.setDQL(sbTemplateQuery.toString());
                TableResultSet templateResultSet = new TableResultSet(new String[] {
                    "r_object_id", "object_name"
                });
                String strObjectId = null;
                String strObjectName = null;
                try
                {
                    iCollection = query.execute(getDfSession(), 0);
                    for(int nIdx = 0; iCollection.next(); nIdx++)
                    {
                        strObjectId = iCollection.getString("r_object_id");
                        strObjectName = iCollection.getString("object_name");
                        templateResultSet.add(new String[] {
                            strObjectId, strObjectName
                        });
                        bIsTemplateAvailable = true;
                        if(nIdx == 0)
                            m_templateList.setValue(strObjectId);
                    }

                }
                catch(DfException e)
                {
                    throw new WrapperRuntimeException(getString("MSG_TEMPLATE_QUERY_FAILED"), e);
                }
                finally
                {
                    try
                    {
                        if(iCollection != null)
                            iCollection.close();
                    }
                    catch(DfException e) { }
                }
                m_templateList.getDataProvider().setScrollableResultSet(templateResultSet);
            }
            Panel templatePanel = (Panel)getControl("template_panel", com.documentum.web.form.control.Panel.class);
            templatePanel.setVisible(bIsTemplateAvailable);
        }
    }

    private String getBaseType()
    {
        if(m_strBaseType == null)
        {
            String strBaseType = getConfigBaseType();
            if(strBaseType == null || strBaseType.length() == 0)
                strBaseType = getFallbackBaseType();
            m_strBaseType = strBaseType;
        }
        return m_strBaseType;
    }

    private String getFallbackBaseType()
    {
        return "dm_document";
    }

    private String getConfigBaseType()
    {
        String strBaseType = null;
        strBaseType = lookupString("combo_defaults.base_type");
        return strBaseType;
    }

    private String getConfigDefaultType()
    {
        String strDefaultType = null;
        strDefaultType = lookupString("combo_defaults.type");
        return strDefaultType;
    }

    private String getConfigDefaultFormat()
    {
        String strDefaultFormat = null;
        strDefaultFormat = lookupString("combo_defaults.format");
        return strDefaultFormat;
    }

    private void updateSelectedTypeOption(boolean bHasResults)
    {
        if(bHasResults)
        {
            String strConfigDefaultType = getConfigDefaultType();
            if(strConfigDefaultType != null && strConfigDefaultType.length() > 0 && m_bConfigDefaultTypeAvailable)
                m_typeList.setValue(strConfigDefaultType);
            else
                m_typeList.setValue(getBaseType());
        } else
        {
            m_typeList.setValue(null);
        }
    }

    private boolean nameContainsOnlyForbiddenChars()
    {
        boolean containsOnlyForbidden = false;
        Text nameTextControl = (Text)getControl("attribute_object_name", com.documentum.web.form.control.Text.class);
        String strName = nameTextControl.getValue();
        if(strName != null && strName.length() > 0)
        {
            strName = removeForbiddenChars(strName);
            if(strName.length() == 0)
                containsOnlyForbidden = true;
        }
        return containsOnlyForbidden;
    }

    private String removeForbiddenChars(String strFileName)
    {
        StringBuffer nameBuffer = new StringBuffer("");
        String strForbiddenChars = "\\/:*?\"<>|#<>`|&; *^$[]\\/'\"";
        for(StringTokenizer st = new StringTokenizer(strFileName, strForbiddenChars); st.hasMoreTokens(); nameBuffer.append(st.nextToken()));
        return nameBuffer.toString();
    }

    public static final String FORBIDDEN_CHARS_WINDOWS = "\\/:*?\"<>|";
    public static final String FORBIDDEN_CHARS_UNIX = "#<>`|&; *^$[]\\/'\"";
    private static final String STR_NONE_FORMAT = "";
    private static final String FOLDER_TYPE = "dm_folder";
    private String m_strFolderId;
    private DataDropDownList m_typeList;
    private DataDropDownList m_formatList;
    private DataDropDownList m_templateList;
    private boolean m_bAllowFormatChoice;
    private String m_strNewObjectId;
    private boolean m_bShowOptions;
    private boolean m_bCreateSuccess;
    private String m_strNewType;
    private String m_strBaseType;
    private Label m_labelNormalCharsNeeded;
    private boolean m_bConfigDefaultTypeAvailable;
    private boolean m_bConfigDefaultFormatAvailable;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/create/NewDocument.class


	TOTAL TIME: 140 ms


	JAD REPORTED MESSAGES/ERRORS:

Overlapped try statements detected. Not all exception handlers will be resolved in the method onCancelChanges
Couldn't fully decompile method onCancelChanges
Couldn't resolve all exception handlers in method onCancelChanges
Overlapped try statements detected. Not all exception handlers will be resolved in the method getTypeComboResultSet
Overlapped try statements detected. Not all exception handlers will be resolved in the method updateFormatCombo
Overlapped try statements detected. Not all exception handlers will be resolved in the method setupTemplateList

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/