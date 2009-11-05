/*jadclipse*/// Decompiled by Jad v1.5.8f. Copyright 2001 Pavel Kouznetsov.
// Jad home page: http://www.kpdus.com/jad.html
// Decompiler options: packimports(3) radix(10) lradix(10) 
// Source File Name:   Checkout.java

package com.documentum.webcomponent.library.contenttransfer.checkout;

import com.documentum.debug.Trace;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionLabels;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.IServiceProcessor;
import com.documentum.web.contentxfer.impl.CheckoutProcessor;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.formext.control.docbase.DocbaseIcon;
import com.documentum.web.formext.control.docbase.DocbaseObject;
import com.documentum.web.formext.session.DocbaseUtils;
import com.documentum.web.util.DfcUtils;
import com.documentum.webcomponent.library.contenttransfer.SysobjectContentTransferComponent;

public class Checkout extends SysobjectContentTransferComponent
{

    public Checkout()
    {
        m_currentVersion = true;
        m_lockOnly = false;
        m_ignoreDescendants = false;
    }

    public void onInit(ArgumentList args)
    {
        setLockOnly(Boolean.valueOf(args.get("isLockOnly")).booleanValue());
        setIgnoreDescendants(Boolean.valueOf(args.get("isIgnoreDescendents")).booleanValue());
        super.onInit(args);
        initControls();
    }

    protected void initControls()
    {
        initHeaderControls();
        initVirtualDocOptions();
        initVersionOptions();
        initWarningMessage();
    }

    protected void initVersionOptions()
    {
        try
        {
            if(isCurrentVersion())
            {
                Panel panel = getSelectedVersionOptionsPanelControl(true);
                panel.setVisible(false);
            } else
            {
                com.documentum.fc.client.IDfPersistentObject pobj = getDfSession().getObject(new DfId(getCurrentVersionObjectId()));
                if(pobj instanceof IDfSysObject)
                {
                    String strLockOwner = ((IDfSysObject)pobj).getLockOwner();
                    if(strLockOwner != null && strLockOwner.length() > 0)
                        getCurrentVersionRadioControl(true).setEnabled(false);
                }
            }
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(getString("MSG_ERROR_CHECKING_OUT"), e);
        }
    }

    protected void initVirtualDocOptions()
    {
        if(isObjectVirtualDoc())
        {
            if(isObjectXmlDoc())
                getVdmOptionsPanelControl(true).setVisible(false);
            else
                getXmlVdmOptionsPanelControl(true).setVisible(false);
        } else
        {
            getVdmOptionsPanelControl(true).setVisible(false);
            getXmlVdmOptionsPanelControl(true).setVisible(false);
        }
    }

    protected void initFromSysobject(IDfSysObject sysObj)
    {
        try
        {
            if(sysObj.getHasFolder())
            {
                setCurrentVersion(true);
                setCurrentVersionObjectId(getObjectId());
            } else
            {
                setCurrentVersion(false);
                setCurrentVersionObjectId(getCurrentObjectId(sysObj));
            }
            boolean insynch = true;
            boolean replicaDeletedAfterRefresh = false;
            boolean isReplica = sysObj.isReplica();
            try
            {
                if(isReplica)
                {
                    IDfVersionLabels beforeVersionLabel = sysObj.getVersionLabels();
                    getDfSession().apiExec("refresh", sysObj.getObjectId().toString());
                    sysObj.fetch(null);
                    IDfVersionLabels afterVersionLabel = sysObj.getVersionLabels();
                    insynch = isSameVersion(beforeVersionLabel, afterVersionLabel);
                }
                if(sysObj.isReference() || isReplica)
                {
                    IDfSysObject remoteObject = (IDfSysObject)getDfSession().getObject(sysObj.getRemoteId());
                    if(remoteObject.isCheckedOut())
                    {
                        String remoteLockOwner = remoteObject.getLockOwner();
                        if(!remoteLockOwner.equals(getCurrentLoginUsername()))
                            insynch = false;
                    }
                }
            }
            catch(DfException e)
            {
                String exceptionMsg = e.getMessage();
                if(exceptionMsg.indexOf("[DM_API_E_ACCESS]", 0) >= 0 || exceptionMsg.indexOf("[DM_FOREIGN_E_REFERENCE_NONE]") >= 0 || exceptionMsg.indexOf("[DM_API_E_EXIST]") >= 0)
                {
                    insynch = false;
                    if(exceptionMsg.indexOf("[DM_API_E_EXIST]") >= 0 && isReplica)
                    {
                        replicaDeletedAfterRefresh = true;
                        Panel objInfoPanel = getObjectInfoPanel(true);
                        objInfoPanel.setVisible(false);
                    }
                } else
                {
                    throw e;
                }
            }
            setObjectReplicaOrRefInSynch(insynch);
            setReplicaIsDeletedAfterRefresh(replicaDeletedAfterRefresh);
        }
        catch(DfException e)
        {
            throw new WrapperRuntimeException(getString("MSG_ERROR_CHECKING_OUT"), e);
        }
    }

    protected void initHeaderControls()
    {
        DocbaseObject docbaseObject = getDocbaseObjectControl(true);
        docbaseObject.setObjectId(getObjectId());
        DocbaseIcon icon = getObjectDocbaseIconControl(true);
        icon.setFormat(getObjectContentType());
        icon.setType(getObjectType());
    }

    protected void initWarningMessage()
    {
        Label warningMsgLabel = getSourceCheckedOutMessageLabel(true);
        Label selectOkLabel = getSelectOKMessageLabel(true);
        Label selectCancelLabel = getSelectCancelMessageLabel(true);
        Panel panel = getReplicaOrRefOutOfSyncPanel(true);
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
                    else
                        warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_CHECKED_OUT_BY_SAME_USER", new String[] {
                            remoteDocbaseName
                        }));
                    selectOkLabel.setVisible(false);
                    selectCancelLabel.setVisible(false);
                } else
                {
                    warningMsgLabel.setLabel(getString("MSG_LOCAL_OBJECT_OUT_OF_SYNC"));
                    selectOkLabel.setLabel(getString("MSG_LOCAL_OBJECT_OUT_OF_SYNC_SELECT_OK"));
                    selectOkLabel.setVisible(true);
                    selectCancelLabel.setLabel(getString("MSG_LOCAL_OBJECT_OUT_OF_SYNC_SELECT_CANCEL"));
                    selectCancelLabel.setVisible(true);
                }
                warningMsgLabel.setVisible(true);
                panel.setVisible(true);
            } else
            {
                panel.setVisible(false);
                warningMsgLabel.setVisible(false);
                selectOkLabel.setVisible(false);
                selectCancelLabel.setVisible(false);
            }
        }
        catch(DfException e)
        {
            String exceptionMsg = e.getMessage();
            if(exceptionMsg.indexOf("[DM_API_E_ACCESS]", 0) >= 0)
            {
                warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_NOT_ACCESSIBLE_CANNOT_CHECKOUT"));
                warningMsgLabel.setVisible(true);
            } else
            if(exceptionMsg.indexOf("[DM_FOREIGN_E_REFERENCE_NONE]", 0) >= 0)
            {
                warningMsgLabel.setLabel(getString("MSG_REMOTE_OBJECT_NOT_FOUND_CANNOT_CHECKOUT"));
                warningMsgLabel.setVisible(true);
            } else
            if(exceptionMsg.indexOf("[DM_API_E_EXIST]", 0) >= 0 && !isObjectReplicaOrRefInSynch())
            {
                warningMsgLabel.setLabel(getString("MSG_LOCAL_OBJECT_OUT_OF_SYNC_NOT_EXISTS"));
                warningMsgLabel.setVisible(true);
            } else
            {
                throw new WrapperRuntimeException(getString("MSG_ERROR_CHECKING_OUT"), e);
            }
        }
    }

    public IServiceProcessor getServiceProcessor()
    {
        IServiceProcessor sp = createServiceProcessor();
        if((sp instanceof CheckoutProcessor) && !isReplicaDeletedAfterRefresh())
        {
            CheckoutProcessor proc = (CheckoutProcessor)sp;
            proc.setObjectId(getObjectId());
            if(!isIgnoreDescendents())
            {
                boolean withDesc = getWithDescendantsSelection();
                proc.setWithDescendants(withDesc);
                proc.setDownloadDescendants(getDownloadDescendantsSelection());
            }
            proc.setLockOnly(isLockOnly());
            proc.setVirtualDocRootObjectId(getInitArgs().get("vdmRootObjectId"));
            proc.setVirtualDocNodeId(getInitArgs().get("nodeId"));
        }
        return sp;
    }

    protected boolean getWithDescendantsSelection()
    {
        Radio radio;
        if(isObjectVirtualDoc() && isObjectXmlDoc())
            radio = getXmlVdmCheckoutAllRadioControl(false);
        else
            radio = getVdmCheckoutAllRadioControl(false);
        return radio != null && radio.isVisible() && radio.getValue();
    }

    protected boolean getDownloadDescendantsSelection()
    {
        if(isObjectVirtualDoc() && isObjectXmlDoc())
        {
            Radio downloadDesc = getXmlVdmCheckoutRootDownloadDescRadioControl(false);
            return downloadDesc != null && downloadDesc.isVisible() && downloadDesc.getValue();
        } else
        {
            return false;
        }
    }

    public boolean canCommitChanges()
    {
        return !isReplicaDeletedAfterRefresh() && (hasRendered() || (!isObjectVirtualDoc() || isObjectVirtualDoc() && isIgnoreDescendents()) && isObjectReplicaOrRefInSynch());
    }

    public boolean isLockOnly()
    {
        if(isObjectXmlDoc())
        {
            Trace.println(this, "WARNING: isLockOnly flag ignored - XML documents cannot be checked out without content");
            return false;
        } else
        {
            return m_lockOnly;
        }
    }

    protected void setLockOnly(boolean lockOnly)
    {
        m_lockOnly = lockOnly;
    }

    public boolean isIgnoreDescendents()
    {
        return m_ignoreDescendants;
    }

    protected void setIgnoreDescendants(boolean ignoreDescendants)
    {
        m_ignoreDescendants = ignoreDescendants;
    }

    protected boolean isCurrentVersion()
    {
        return m_currentVersion;
    }

    protected void setCurrentVersion(boolean currentVersion)
    {
        m_currentVersion = currentVersion;
    }

    protected String getCurrentVersionObjectId()
    {
        return m_currentVersionObjectId;
    }

    protected void setCurrentVersionObjectId(String currentVersionObjectId)
    {
        m_currentVersionObjectId = currentVersionObjectId;
    }

    protected boolean isObjectReplicaOrRefInSynch()
    {
        return m_objectReplicaOrRefInSynch;
    }

    protected void setObjectReplicaOrRefInSynch(boolean insynch)
    {
        m_objectReplicaOrRefInSynch = insynch;
    }

    protected boolean isReplicaDeletedAfterRefresh()
    {
        return m_replicaIsDeletedAfterRefresh;
    }

    protected void setReplicaIsDeletedAfterRefresh(boolean isDeleted)
    {
        m_replicaIsDeletedAfterRefresh = isDeleted;
    }

    protected Label getSourceCheckedOutMessageLabel(boolean create)
    {
        return (Label)getControl0("sourcecheckedoutmsg", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getSelectOKMessageLabel(boolean create)
    {
        return (Label)getControl0("selectok", create, com.documentum.web.form.control.Label.class);
    }

    protected Label getSelectCancelMessageLabel(boolean create)
    {
        return (Label)getControl0("selectcancel", create, com.documentum.web.form.control.Label.class);
    }

    protected Panel getReplicaOrRefOutOfSyncPanel(boolean create)
    {
        return (Panel)getControl0("replicaorrefoutofsync", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getObjectInfoPanel(boolean create)
    {
        return (Panel)getControl0("objectinfo", create, com.documentum.web.form.control.Panel.class);
    }

    protected DocbaseIcon getObjectDocbaseIconControl(boolean create)
    {
        return (DocbaseIcon)getControl0("obj_icon", create, com.documentum.web.formext.control.docbase.DocbaseIcon.class);
    }

    protected DocbaseObject getDocbaseObjectControl(boolean create)
    {
        return (DocbaseObject)getControl0("object", create, com.documentum.web.formext.control.docbase.DocbaseObject.class);
    }

    protected Radio getCurrentVersionRadioControl(boolean create)
    {
        return (Radio)getControl0("currentversion", create, com.documentum.web.form.control.Radio.class);
    }

    protected Panel getSelectedVersionOptionsPanelControl(boolean create)
    {
        return (Panel)getControl0("selectedversionoptions", create, com.documentum.web.form.control.Panel.class);
    }

    protected Control getXmlVdmOptionsPanelControl(boolean create)
    {
        return getControl0("xmlvdmoptions", create, com.documentum.web.form.control.Panel.class);
    }

    protected Panel getVdmOptionsPanelControl(boolean create)
    {
        return (Panel)getControl0("vdmoptions", create, com.documentum.web.form.control.Panel.class);
    }

    protected Radio getXmlVdmCheckoutRootDownloadDescRadioControl(boolean create)
    {
        return (Radio)getControl0("xmlrootdownload", create, com.documentum.web.form.control.Radio.class);
    }

    protected Radio getVdmCheckoutAllRadioControl(boolean create)
    {
        return (Radio)getControl0("alldescendents", create, com.documentum.web.form.control.Radio.class);
    }

    protected Radio getXmlVdmCheckoutAllRadioControl(boolean create)
    {
        return (Radio)getControl0("xmlcheckoutall", create, com.documentum.web.form.control.Radio.class);
    }

    private Control getControl0(String name, boolean create, Class cl)
    {
        return create ? getControl(name, cl) : getControl(name);
    }

    private String getCurrentObjectId(IDfSysObject sysObj)
        throws DfException
    {
        String strCurrentObjectId = null;
        IDfQuery query = null;
        IDfCollection iCollection = null;
    	try { 
	        strCurrentObjectId = null;
	        query = DfcUtils.getClientX().getQuery();
	        String strChronicleId = sysObj.getChronicleId().toString();
	        StringBuffer sbQuery = new StringBuffer(64);
	        sbQuery.append("SELECT r_object_id from dm_sysobject where i_chronicle_id_i = '");
	        sbQuery.append(strChronicleId);
	        sbQuery.append("'");
	        query.setDQL(sbQuery.toString());
	        iCollection = null;
	        iCollection = query.execute(getDfSession(), 0);
	        if(iCollection.next())
	            strCurrentObjectId = iCollection.getString("r_object_id");
	        if(iCollection != null)
	            iCollection.close();
    	} catch (DfException e) {
            if(iCollection != null)
                iCollection.close();
            throw e;
    	}
    	return strCurrentObjectId;
    }

    private boolean isSameVersion(IDfVersionLabels versionLabel1, IDfVersionLabels versionLabel2)
    {
        boolean retVal = true;
        try
        {
            int label1Count = versionLabel1.getVersionLabelCount();
            int label2Count = versionLabel2.getVersionLabelCount();
            if(label1Count != label2Count)
            {
                retVal = false;
            } else
            {
                for(int i = 0; i < label1Count; i++)
                {
                    String label1 = versionLabel1.getVersionLabel(i);
                    String label2 = versionLabel2.getVersionLabel(i);
                    if(label1 != null && !label1.equals(label2))
                        retVal = false;
                }

            }
        }
        catch(DfException e)
        {
            retVal = false;
        }
        return retVal;
    }

    private boolean m_currentVersion;
    private String m_currentVersionObjectId;
    private boolean m_objectReplicaOrRefInSynch;
    private boolean m_replicaIsDeletedAfterRefresh;
    private boolean m_lockOnly;
    private boolean m_ignoreDescendants;
}



/***** DECOMPILATION REPORT *****

	DECOMPILED FROM: C:\2006WT53\tomcat-5.0.28-mueller-56880\webapps\webtop2\WEB-INF\classes/com/documentum/webcomponent/library/contenttransfer/checkout/Checkout.class


	TOTAL TIME: 15 ms


	JAD REPORTED MESSAGES/ERRORS:

Overlapped try statements detected. Not all exception handlers will be resolved in the method getCurrentObjectId
Couldn't fully decompile method getCurrentObjectId
Couldn't resolve all exception handlers in method getCurrentObjectId

	EXIT STATUS:	0


	CAUGHT EXCEPTIONS:

 ********************************/