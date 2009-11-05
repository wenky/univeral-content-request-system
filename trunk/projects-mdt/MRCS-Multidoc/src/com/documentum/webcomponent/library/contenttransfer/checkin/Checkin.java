/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   Checkin.java

package com.documentum.webcomponent.library.contenttransfer.checkin;

import java.util.Map;

import javax.servlet.ServletRequest;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfUser;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.services.subscriptions.ISubscriptions;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.failover.TransientObjectWrapper;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.FileBrowse;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Link;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.ScrollableResultSet;
import com.documentum.web.form.control.validator.RequiredFieldValidator;
import com.documentum.web.formext.control.docbase.DocbaseIcon;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.formext.docbase.FormatService;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.documentum.web.util.DocbaseObjectAttributeReader;
import com.documentum.webcomponent.common.WebComponentErrorService;
import com.documentum.webcomponent.library.contenttransfer.SysobjectContentTransferComponent;
import com.documentum.webcomponent.library.subscription.SubscriptionsHttpBinding;
import com.documentum.webcomponent.library.workflow.ScrollableResultSetAdapter;

// Referenced classes of package com.documentum.webcomponent.library.contenttransfer.checkin:
//            CheckinProcessor

public abstract class Checkin extends SysobjectContentTransferComponent
{

    public Checkin()
    {
        m_strFilenameWithPath = null;
    }

    public void onInit(ArgumentList args)
    {
        super.onInit(args);
        m_strFilenameWithPath = args.get("filenamewithpath");
        initControls();
    }

    public IServiceProcessor getServiceProcessor()
    {
        IServiceProcessor sp = createServiceProcessor();
        if(sp instanceof CheckinProcessor)
        {
            CheckinProcessor proc = (CheckinProcessor)sp;
            proc.setObjectId(getObjectId());
            proc.setCheckinVersion(getCheckinVersionSelection());
            proc.setVersionLabel(getVersionLabelSelection());
            proc.setMakeCurrent(getMakeCurrentSelection());
            proc.setKeepLocalFile(getKeepLocalFileSelection());
            proc.setRetainLock(getRetainLockSelection());
            proc.setWithDescendants(getCheckinDescendantsSelection());
            proc.setNewObjectAttributes(getObjectAttributes());
            proc.setVirtualDocRootObjectId(getInitArgs().get("vdmRootObjectId"));
            proc.setVirtualDocNodeId(getInitArgs().get("nodeId"));
            proc.setFormat(getFormatSelection());
            proc.setSubscribe(getSubscribeSelection());
        }
        return sp;
    }

    protected void initFromSysobject(IDfSysObject sysObj)
    {
        try
        {
            if(sysObj.isCheckedOut())
            {
                boolean isObjectContentLess = sysObj.getPageCount() == 0;
                boolean insynch = true;
                try
                {
                    if(sysObj.isReference() || sysObj.isReplica())
                    {
                        IDfSysObject remoteObject = (IDfSysObject)getDfSession().getObject(sysObj.getRemoteId());
                        if(remoteObject.isCheckedOut())
                        {
                            String remoteLockOwner = remoteObject.getLockOwner();
                            if(!remoteLockOwner.equals(getCurrentLoginUsername()))
                                insynch = false;
                        } else
                        {
                            insynch = false;
                        }
                        isObjectContentLess = remoteObject.getPageCount() == 0;
                    }
                    if(sysObj.isReference())
                    {
                        DocbaseObject docbaseObj = getDocbaseObjectControl(true);
                        docbaseObj.setReferenceDataDictionaryFromSource(true);
                    }
                }
                catch(DfException e)
                {
                    String exceptionMsg = e.getMessage();
                    if(exceptionMsg.indexOf("[DM_API_E_ACCESS]", 0) >= 0 || exceptionMsg.indexOf("[DM_FOREIGN_E_REFERENCE_NONE]") >= 0)
                        insynch = false;
                    else
                        throw e;
                }
                setObjectReplicaOrRefInSynch(insynch);
                setObjectContentless(isObjectContentLess);
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
    }

    protected void initControls()
    {
        initHeaderControls();
        initStandardOptionsControls();
        initAdditionalOptionsControls();
        initWarningMessage();
    }

    protected void initHeaderControls()
    {
        DocbaseObject docbaseObject = getDocbaseObjectControl(true);
        docbaseObject.setObjectId(getObjectId());
        DocbaseIcon icon = getObjectDocbaseIconControl(true);
        icon.setFormat(getObjectContentType());
        icon.setType(getObjectType());
    }

    protected void initStandardOptionsControls()
    {
        Panel standardPanel = getStandardOptionsPanel(true);
        if(!isObjectReplicaOrRefInSynch())
        {
            standardPanel.setVisible(false);
        } else
        {
            standardPanel.setVisible(true);
            initVersionDisplays();
            initFormatDisplays();
            try
            {
                IDfUser dfUser = getDfSession().getUser(null);
                getFulltextPanelControl(true).setVisible(dfUser.isSuperUser());
            }
            catch(DfException e)
            {
                throw new WrapperRuntimeException(e);
            }
        }
    }

    protected void initVersionDisplays()
    {
        try
        {
            IDfSession iDfSession = getDfSession();
            IDfSysObject docObj = (IDfSysObject)iDfSession.getObject(new DfId(getObjectId()));
            if(docObj.isReference())
                docObj = (IDfSysObject)iDfSession.getObject(docObj.getRemoteId());
            String verLabel = docObj.getImplicitVersionLabel();
            if(!docObj.getLatestFlag())
            {
                IDfVersionPolicy verPolicy = docObj.getVersionPolicy();
                String branch = verPolicy.getBranchLabel();
                getBranchRevNumLabelControl(true).setLabel(getString("MSG_BRANCH_REVISION", new Object[] {
                    branch
                }));
                String major = verPolicy.getNextMajorLabel();
                getMajorVersionLabelControl(true).setLabel(getString("MSG_MAJOR_VERSION", new Object[] {
                    major
                }));
                getObjectVersionPanelControl(true).setVisible(false);
                getNewObjectVersionPanelControl(true).setVisible(false);
            } else
            {
                getBranchVersionPanelControl(true).setVisible(false);
                if(docObj.findString("r_version_label", "_NEW_") > 0)
                {
                    getNewVersionLabelControl(true).setLabel(getString("MSG_SAME_VERSION", new Object[] {
                        verLabel
                    }));
                    getObjectVersionPanelControl(true).setVisible(false);
                } else
                {
                    getNewObjectVersionPanelControl(true).setVisible(false);
                    IDfVersionPolicy verPolicy = docObj.getVersionPolicy();
                    if(verPolicy.canVersion(2))
                    {
                        getSameVersionLabelControl(true).setLabel(getString("MSG_SAME_VERSION", new Object[] {
                            verLabel
                        }));
                    } else
                    {
                        getSameVersionRadioControl(true).setVisible(false);
                        getSameVersionLabelControl(true).setVisible(false);
                    }
                    String minor = verPolicy.getNextMinorLabel();
                    getMinorVersionLabelControl(true).setLabel(getString("MSG_MINOR_VERSION", new Object[] {
                        minor
                    }));
                    String major = verPolicy.getNextMajorLabel();
                    getMajorVersionLabelControl(true).setLabel(getString("MSG_MAJOR_VERSION", new Object[] {
                        major
                    }));
                }
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(e);
        }
        boolean verLabelRequired = isVersionLabelRequired();
        getVersionLabelRequiredValidator(true).setEnabled(verLabelRequired);
        getVersionLabelRequiredValidator(true).setVisible(verLabelRequired);
    }

    protected void initFormatDisplays()
    {
        getFormatInfoLabelControl(true).setVisible(false);
        ScrollableResultSet resultSet = createFormatsResultSet();
        DataDropDownList formatList = getFormatListControl(true);
        formatList.getDataProvider().setResultSet(resultSet, null);
        if(getObjectContentType() != null && getObjectContentType().length() > 0)
            getFormatListControl(true).setValue(getObjectContentType());
        else
            getFormatListControl(true).setValue("unknown");
    }

    protected void initWarningMessage()
    {
        Label warningMsgLabel = getSourceNotCheckedOutMessageLabel(true);
        try
        {
            if(!isObjectReplicaOrRefInSynch())
            {
                String objectId = getObjectId();
                IDfSysObject sysObject = (IDfSysObject)getDfSession().getObject(new DfId(objectId));
                IDfSysObject remoteObject = (IDfSysObject)getDfSession().getObject(sysObject.getRemoteId());
                String remoteDocbaseName = DocbaseUtils.getDocbaseNameFromId(remoteObject.getObjectId());
                if(remoteObject.isCheckedOut())
                {
                    String remoteLockOwner = remoteObject.getLockOwner();
                    if(!remoteLockOwner.equals(getCurrentLoginUsername()))
                        warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_CHECKED_OUT_BY_OTHER_USER", new String[] {
                            remoteDocbaseName
                        }));
                } else
                {
                    warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_NOT_CHECKED_OUT", new String[] {
                        remoteDocbaseName
                    }));
                }
            } else
            {
                warningMsgLabel.setVisible(false);
            }
        }
        catch(DfException e)
        {
            String exceptionMsg = e.getMessage();
            if(exceptionMsg.indexOf("[DM_API_E_ACCESS]", 0) >= 0)
            {
                warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_NOT_ACCESSIBLE_CANNOT_CHECKIN"));
                warningMsgLabel.setVisible(true);
            } else
            if(exceptionMsg.indexOf("[DM_FOREIGN_E_REFERENCE_NONE]", 0) >= 0)
            {
                warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_NOT_FOUND_CANNOT_CHECKIN"));
                warningMsgLabel.setVisible(true);
            } else
            {
                throw new WrapperRuntimeException(e);
            }
        }
    }

    protected ScrollableResultSet createFormatsResultSet()
    {
    	try { 
	        ScrollableResultSet resultSet;
	        ServletRequest req = getPageContext().getRequest();
	        TransientObjectWrapper tw = (TransientObjectWrapper)req.getAttribute(FORMATS_RESULTSET_REQ_CACHE);
	        if(tw == null || tw.get() == null)
	        {
	            resultSet = FormatService.getInstance().getResultSet(new String[] {
	                "name", "description"
	            }, false);
	            resultSet.sort("description", 0, 1);
	            tw = new TransientObjectWrapper(resultSet);
	            req.setAttribute(FORMATS_RESULTSET_REQ_CACHE, tw);
	        } else
	        {
	            resultSet = (ScrollableResultSet)tw.get();
	            ScrollableResultSet rs = new ScrollableResultSetAdapter(resultSet);
	            resultSet.setCursor(-1);
	            resultSet = rs;
	        }
	        return resultSet;
    	} catch  (DfException e) {
    		throw new WrapperRuntimeException("Unable to query format types from docbase!", e);
    	}
    }

    protected void initAdditionalOptionsControls()
    {
        Panel additionalOptionsPanel = getAdditionalOptionsPanel(true);
        if(!isObjectReplicaOrRefInSynch())
        {
            additionalOptionsPanel.setVisible(false);
        } else
        {
            additionalOptionsPanel.setVisible(true);
            initSubscriptionControls();
            if(!isObjectVirtualDoc())
                getVdmOptionsPanel(true).setVisible(false);
            if(isObjectXmlDoc())
                getRetainLockCheckboxControl(true).setVisible(false);
            getMakeCurrentCheckboxControl(true).setValue(true);
        }
        if(m_strFilenameWithPath != null && m_strFilenameWithPath.length() > 0)
        {
            getCheckinFromFileCheckboxControl(true).setValue(true);
            getFilebrowseControl(true).setValue(m_strFilenameWithPath);
        }
    }

    protected void initSubscriptionControls()
    {
        Checkbox checkboxSubscribe = getSubscribeCheckboxControl(true);
        ISubscriptions subscriptions = (new SubscriptionsHttpBinding()).getSubscriptionsService();
        String strDocbase = SessionManagerHttpBinding.getCurrentDocbase();
        if(subscriptions.isInstalled(strDocbase))
        {
            checkboxSubscribe.setValue(subscriptions.isSubscribed(strDocbase, getObjectId()));
        } else
        {
            checkboxSubscribe.setEnabled(false);
            WebComponentErrorService.getService().setNonFatalError(this, "MSG_SUBSCRIPTIONS_NOT_INSTALLED_ERROR", null);
        }
    }

    public void onRender()
    {
        super.onRender();
        showCheckinFromFileInfo(getCheckinFromFileNeedsValue());
        showFilebrowseRequired(getCheckinFromFileSelection());
    }

    public void onClickShowHideOptions(Link linkShowHideOptions, ArgumentList arg)
    {
        showHideOptions(!getOptionsPanelControl(true).isVisible());
    }

    public void onClickMakeCurrent(Checkbox checkbox, ArgumentList arg)
    {
        boolean verLabelRequired = isVersionLabelRequired();
        getVersionLabelRequiredValidator(true).setEnabled(verLabelRequired);
        getVersionLabelRequiredValidator(true).setVisible(verLabelRequired);
    }

    protected boolean isVersionLabelRequired()
    {
        boolean bShowErrorLabel = false;
        if(!getMakeCurrentSelection())
        {
            String strVersionLabel = getVersionLabelSelection();
            if(strVersionLabel == null || strVersionLabel.trim().length() == 0)
                bShowErrorLabel = true;
        }
        return bShowErrorLabel;
    }

    protected void showHideOptions(boolean bShowOptions)
    {
        if(bShowOptions)
        {
            getOptionsPanelControl(true).setVisible(true);
            getHideOptionsLinkControl(true).setLabel(getString("MSG_HIDE_OPTIONS"));
            getHideOptionsLinkControl(true).setToolTip(getString("MSG_HIDE_OPTIONS_TIP"));
        } else
        {
            getOptionsPanelControl(true).setVisible(false);
            getHideOptionsLinkControl(true).setLabel(getString("MSG_SHOW_OPTIONS"));
            getHideOptionsLinkControl(true).setToolTip(getString("MSG_SHOW_OPTIONS_TIP"));
        }
    }

    protected void showCheckinFromFileInfo(boolean bShow)
    {
        if(bShow)
        {
            getCheckinFromFileErrorMsgLabelControl(true).setVisible(true);
            getCheckinFromFileCheckboxControl(true).setValue(true);
        } else
        {
            getCheckinFromFileErrorMsgLabelControl(true).setVisible(false);
        }
    }

    protected void showFilebrowseRequired(boolean required)
    {
        if(required)
        {
            if(getObjectContentType() == null || getObjectContentType().length() == 0)
            {
                getValidateFilebrowseControl(true).setVisible(false);
                getValidateFilebrowseControl(true).setEnabled(false);
            } else
            {
                getValidateFilebrowseControl(true).setVisible(true);
                getValidateFilebrowseControl(true).setEnabled(true);
            }
        } else
        {
            getValidateFilebrowseControl(true).setEnabled(false);
            getValidateFilebrowseControl(true).setVisible(false);
        }
    }

    protected boolean getCheckinFromFileNeedsValue()
    {
        boolean bNeedsValue = false;
        if(getCheckinFromFileSelection())
        {
            String filePath = getFilebrowseControl(false) == null ? null : getFilebrowseControl(false).getValue();
            if(filePath == null || filePath.length() == 0)
                bNeedsValue = true;
        }
        return bNeedsValue;
    }

    protected boolean getCheckinFromFileSelection()
    {
        boolean bCheckinFromFileSelected = getCheckinFromFileCheckboxControl(false) == null ? false : getCheckinFromFileCheckboxControl(false).getValue();
        return bCheckinFromFileSelected;
    }

    protected boolean getCheckinDescendantsSelection()
    {
        boolean bCheckinDescendantsSelected = false;
        if(isObjectVirtualDoc())
        {
            Checkbox withDesc = getWithDescendantsCheckboxControl(false);
            bCheckinDescendantsSelected = withDesc == null ? false : withDesc.getValue();
        }
        return bCheckinDescendantsSelected;
    }

    protected int getCheckinVersionSelection()
    {
        int version = -1;
        if(getBranchVersionPanelControl(false) != null && getBranchVersionPanelControl(false).isVisible())
        {
            if(getBranchRadioControl(false) != null && getBranchRadioControl(false).getValue())
                version = 3;
            else
            if(getMajorVersionRadioControl(false) != null && getMajorVersionRadioControl(false).getValue())
                version = 0;
        } else
        if(getNewObjectVersionPanelControl(false) != null && getNewObjectVersionPanelControl(false).isVisible() && getNewVersionRadioControl(false) != null && getNewVersionRadioControl(false).getValue() || getSameVersionRadioControl(false) != null && getSameVersionRadioControl(false).getValue())
            version = 2;
        else
        if(getMinorVersionRadioControl(false) != null && getMinorVersionRadioControl(false).getValue())
            version = 1;
        else
        if(getMajorVersionRadioControl(false) != null && getMajorVersionRadioControl(false).getValue())
            version = 0;
        return version;
    }

    protected boolean getMakeCurrentSelection()
    {
        Checkbox checkboxMakeCurrent = getMakeCurrentCheckboxControl(false);
        return checkboxMakeCurrent == null ? true : checkboxMakeCurrent.getValue();
    }

    protected boolean getRetainLockSelection()
    {
        Checkbox checkboxRetainLock = getRetainLockCheckboxControl(false);
        return checkboxRetainLock == null ? false : checkboxRetainLock.getValue();
    }

    protected boolean getSubscribeSelection()
    {
        Checkbox checkboxSubscribe = getSubscribeCheckboxControl(false);
        return checkboxSubscribe == null ? false : checkboxSubscribe.getValue();
    }

    protected Map getObjectAttributes()
    {
    	try { 
	        DocbaseObjectAttributeReader reader;
	        com.documentum.fc.client.IDfType type = getDfSession().getType(getObjectType());
	        reader = new DocbaseObjectAttributeReader(type);
	        visitDepthFirst(reader);
	        return reader.getProperties();
    	} catch (DfException e) {
    		throw new WrapperRuntimeException("Fail to retrieve docbase type " + getObjectType());
    	}
    }

    protected String getFormatSelection()
    {
        String strFormat = "";
        DataDropDownList formatList = getFormatListControl(false);
        if(formatList != null)
            strFormat = formatList.getValue();
        return strFormat;
    }

    protected String getVersionLabelSelection()
    {
        String strVersionLabel = "";
        Text textVersionLabel = getSymbolicVersionLabelTextControl(false);
        if(textVersionLabel != null)
            strVersionLabel = textVersionLabel.getValue();
        return strVersionLabel;
    }

    protected boolean getKeepLocalFileSelection()
    {
        if(getCheckinFromFileSelection())
            return true;
        Checkbox checkbox = getKeepLocalFileControl(false);
        if(checkbox != null)
            return checkbox.getValue();
        else
            return false;
    }

    protected String getCheckinFromFilePathSelection()
    {
        FileBrowse fb = getFilebrowseControl(false);
        if(fb != null && fb.getValue() != null)
            return fb.getValue();
        else
            return null;
    }

    protected boolean isObjectReplicaOrRefInSynch()
    {
        return m_objectReplicaOrRefInSynch;
    }

    protected void setObjectReplicaOrRefInSynch(boolean insynch)
    {
        m_objectReplicaOrRefInSynch = insynch;
    }

    protected boolean isObjectContentless()
    {
        return m_objectContentless;
    }

    protected void setObjectContentless(boolean contentless)
    {
        m_objectContentless = contentless;
    }

    protected Label getSourceNotCheckedOutMessageLabel(boolean create)
    {
        return (Label)getControl0("sourcenotcheckedoutmsg", create, com.documentum.web.form.control.Label.class);
    }

    protected Panel getStandardOptionsPanel(boolean create)
    {
        return (Panel)getControl0("standardoptions", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getAdditionalOptionsPanel(boolean create)
    {
        return (Panel)getControl0("additionaloptions", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getVdmOptionsPanel(boolean create)
    {
        return (Panel)getControl0("vdmoptions", create, com.documentum.web.form.control.Panel.class);
    }

    protected DocbaseIcon getObjectDocbaseIconControl(boolean create)
    {
        return (DocbaseIcon)getControl0("obj_icon", create, com.documentum.web.formext.control.docbase.DocbaseIcon.class);
    }

    protected DocbaseObject getDocbaseObjectControl(boolean create)
    {
        return (DocbaseObject)getControl0("object", create, com.documentum.web.formext.control.docbase.DocbaseObject.class);
    }

    protected Label getFormatInfoLabelControl(boolean create)
    {
        return (Label)getControl0("unknown_format_info_label", create, com.documentum.web.form.control.Label.class);
    }

    protected Checkbox getWithDescendantsCheckboxControl(boolean create)
    {
        return (Checkbox)getControl0("checkindescendents", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected Checkbox getRetainLockCheckboxControl(boolean create)
    {
        return (Checkbox)getControl0("retainlock", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected Radio getBranchRadioControl(boolean create)
    {
        return (Radio)getControl0("branchrevision", create, com.documentum.web.form.control.Radio.class);
    }

    protected Radio getNewVersionRadioControl(boolean create)
    {
        return (Radio)getControl0("newversion", create, com.documentum.web.form.control.Radio.class);
    }

    protected Radio getSameVersionRadioControl(boolean create)
    {
        return (Radio)getControl0("sameversion", create, com.documentum.web.form.control.Radio.class);
    }

    protected Radio getMinorVersionRadioControl(boolean create)
    {
        return (Radio)getControl0("minorversion", create, com.documentum.web.form.control.Radio.class);
    }

    protected Radio getMajorVersionRadioControl(boolean create)
    {
        return (Radio)getControl0("majorversion", create, com.documentum.web.form.control.Radio.class);
    }

    protected Label getCheckinFromFileErrorMsgLabelControl(boolean create)
    {
        return (Label)getControl0("checkinFromFileErrorMsg", create, com.documentum.web.form.control.Label.class);
    }

    protected RequiredFieldValidator getVersionLabelRequiredValidator(boolean create)
    {
        return (RequiredFieldValidator)getControl0("versionLabelRequireValidator", create, com.documentum.web.form.control.validator.RequiredFieldValidator.class);
    }

    protected DataDropDownList getFormatListControl(boolean create)
    {
        return (DataDropDownList)getControl0("formatlist", create, com.documentum.web.form.control.databound.DataDropDownList.class);
    }

    protected Label getBranchRevNumLabelControl(boolean create)
    {
        return (Label)getControl0("branchrevisionnum", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getNewVersionLabelControl(boolean create)
    {
        return (Label)getControl0("newversionnum", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getSameVersionLabelControl(boolean create)
    {
        return (Label)getControl0("sameversionnum", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getMinorVersionLabelControl(boolean create)
    {
        return (Label)getControl0("minorversionnum", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getMajorVersionLabelControl(boolean create)
    {
        return (Label)getControl0("majorversionnum", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getFilebrowseLabelControl(boolean create)
    {
        return (Label)getControl0("filebrowselabel", create, com.documentum.web.form.control.Label.class);
    }

    protected FileBrowse getFilebrowseControl(boolean create)
    {
        return (FileBrowse)getControl0("filebrowse", create, com.documentum.web.form.control.FileBrowse.class);
    }

    protected RequiredFieldValidator getValidateFilebrowseControl(boolean create)
    {
        return (RequiredFieldValidator)getControl0("validatefilebrowse", create, com.documentum.web.form.control.validator.RequiredFieldValidator.class);
    }

    protected Panel getObjectVersionPanelControl(boolean create)
    {
        return (Panel)getControl0("existingobjversion", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getNewObjectVersionPanelControl(boolean create)
    {
        return (Panel)getControl0("newobjversion", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getBranchVersionPanelControl(boolean create)
    {
        return (Panel)getControl0("branchversion", create, com.documentum.web.form.control.Panel.class);
    }

    protected Checkbox getSubscribeCheckboxControl(boolean create)
    {
        return (Checkbox)getControl0("subscribe", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected Checkbox getCheckinFromFileCheckboxControl(boolean create)
    {
        return (Checkbox)getControl0("checkinfromfile", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected Checkbox getMakeCurrentCheckboxControl(boolean create)
    {
        return (Checkbox)getControl0("makecurrent", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected Link getHideOptionsLinkControl(boolean create)
    {
        return (Link)getControl0("showhideoptions", create, com.documentum.web.form.control.Link.class);
    }

    protected Panel getOptionsPanelControl(boolean create)
    {
        return (Panel)getControl0("optionspanel", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getKeepLocalFilePanelControl(boolean create)
    {
        return (Panel)getControl0("keeplocalfilepanel", create, com.documentum.web.form.control.Panel.class);
    }

    protected Checkbox getKeepLocalFileControl(boolean create)
    {
        return (Checkbox)getControl0("keeplocalfile", create, com.documentum.web.form.control.Checkbox.class);
    }

    protected Panel getFulltextPanelControl(boolean create)
    {
        return (Panel)getControl0("fulltext", create, com.documentum.web.form.control.Panel.class);
    }

    protected Text getSymbolicVersionLabelTextControl(boolean create)
    {
        return (Text)getControl0("symbolicVersionLabel", create, com.documentum.web.form.control.Text.class);
    }

    private Control getControl0(String name, boolean create, Class cl)
    {
        return create ? getControl(name, cl) : getControl(name);
    }

    private static final String FORMATS_RESULTSET_REQ_CACHE;
    private boolean m_objectReplicaOrRefInSynch;
    private static final String UNKNOWN_FORMAT = "unknown";
    private String m_strFilenameWithPath;
    private boolean m_objectContentless;

    static 
    {
        FORMATS_RESULTSET_REQ_CACHE = (com.documentum.webcomponent.library.contenttransfer.checkin.Checkin.class).getName() + ".formatsResultSetCache";
    }
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/checkin/Checkin.class


	TOTAL TIME: 32 ms


	JAD REPORTED MESSAGES/ERRORS:

Couldn't fully decompile method createFormatsResultSet
Couldn't resolve all exception handlers in method createFormatsResultSet
Couldn't fully decompile method getObjectAttributes
Couldn't resolve all exception handlers in method getObjectAttributes

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/