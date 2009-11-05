/*
 * Created on Mar 3, 2005
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

 Filename       $RCSfile: MrcsCheckin.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.6 $
 Modified on    $Date: 2007/12/12 17:39:32 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.HashMap;
import java.util.List;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfVersionLabels;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.form.control.Checkbox;
import com.documentum.web.form.control.Label;
import com.documentum.web.form.control.Panel;
import com.documentum.web.form.control.Radio;
import com.documentum.web.form.control.Text;
import com.documentum.web.form.control.databound.DataDropDownList;
import com.documentum.web.form.control.databound.TableResultSet;
import com.documentum.web.formext.control.docbase.DocbaseAttributeValue;
import com.documentum.webcomponent.library.contenttransfer.checkin.UcfCheckin;
import com.medtronic.documentum.mrcs.common.MrcsCheckInInfo;
import com.medtronic.documentum.mrcs.config.CheckinConfigFactory;
import com.medtronic.documentum.mrcs.config.MrcsDocumentConfigFactory;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class MrcsCheckin extends UcfCheckin {

    private IDfDocument docObject = null;

    private Radio _radioBranchRevision;

    private Radio _radioNewVersion;

    private Radio _radioSameVersion;

    private Radio _radioMinorVersion;

    private Radio _radioMajorVersion;

    private String arrRadioValues = "";

    private String appName = "";

    protected HashMap _extensionMap;
    protected DataDropDownList _formatList;
    protected String _strFormat;

    public String getSameMinorMajorVersion()
    {
    	int versioning = super.getCheckinVersionSelection();
    	switch (versioning)
    	{
	    	case 0: return ("MAJOR");
	    	case 1: return ("MINOR");
	    	case 2: return ("SAME"); // "NEW"?
	    	case 3: return ("BRANCH");
	    	default: throw new WrapperRuntimeException("MRCS_INVALID_VERSIONING_TYPE_ON_CHECKIN");
    	}
    }

    public void onInit(ArgumentList argumentlist) {
        try {
            String val[] = argumentlist.getValues("objectId");
            docObject = (IDfDocument) getDfSession().getObject(new DfId(val[0]));
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsCheckin:----onInit---------" + docObject, null, null);
            appName = docObject.getString("mrcs_application");
            _strFormat = docObject.getContentType();
            _strFormat = docObject.getFormat().getName();
           // initializeControls();
        } catch (DfException e) {
            /*-ERROR-*/DfLogger.error(this, "MrcsCheckin:----onInit Exception---------", null, e);
            throw new RuntimeException("MrcsCheckin's onInit event threw an error",e);
        }
        super.onInit(argumentlist);
    }


    //MRCS
    protected void initMRCSFormatCombo()
    {
        // MRCS: need to look up document format config
        /*-CONFIG-*/String m="MRCS:initFormatCombo - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting MRCS doc config" , null, null);
        MrcsDocumentConfigFactory docconfig = MrcsDocumentConfigFactory.getDocumentConfig();
        // get mrcs gftype
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting MRCS attr vals from doc to be checked in" , null, null);
        String gftype, mrcsdoctype;
        try {
            gftype = docObject.getString("mrcs_folder_config");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"gftype - "+gftype , null, null);
            mrcsdoctype = docObject.getString("mrcs_config");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"mrcsdoctype - "+mrcsdoctype , null, null);
        } catch (DfException dfe) {
            /*-ERROR-*/DfLogger.error(this,m+"DFC error getting MRCS attrs from doc "+docObject, null, dfe);
            throw new RuntimeException("Mrcs Checkin: Initialize format dropdown: MRCS attribute retrieval error from document",dfe);
        }
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"retriving allowable formats from doc config" , null, null);
        List mrcsformats = docconfig.getDocumentFormats(appName,gftype,mrcsdoctype);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"format list retreived: " + (mrcsformats == null ? null : mrcsformats.size() + " formats found"), null, null);


        TableResultSet tableresultset = new TableResultSet(new String[] {
            "name", "description"
        });
        DfQuery dfquery = new DfQuery();
        dfquery.setDQL("select name,description,dos_extension from dm_format where is_hidden=0 order by description");
        _extensionMap = new HashMap(401, 1.0F);
        IDfCollection idfcollection = null;
        try
        {
            String formatname;
            String dosext;
            idfcollection = dfquery.execute(getDfSession(), 0);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"Iterating through DQL-retrieved formats", null, null);
            while (idfcollection.next())
            {
                formatname = idfcollection.getString("name");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DQL format: "+formatname, null, null);
                String formatdesc = idfcollection.getString("description");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DQL format desc: "+formatdesc, null, null);
                dosext = idfcollection.getString("dos_extension");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"DQL format ext: "+dosext, null, null);
                String as[] = {
                    formatname, formatdesc
                };
                // see if the format is an allowable format
                boolean match = false;
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"attempting match with MRCS allowable formats", null, null);
                for (int k=0; k < mrcsformats.size(); k++)
                {
                    if (mrcsformats.get(k).equals(formatname))
                    {
                        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"match found!", null, null);
                        match = true;
                        break;
                    }
                }
                if (match) {
                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"adding match to tableresultset and extension map", null, null);
                    tableresultset.add(as);
                    _extensionMap.put(dosext, formatname);
                }
            }

        }
        catch(DfException dfexception)
        {
            /*-ERROR-*/DfLogger.error(this, m+"formats query threw error", null, dfexception);
            throw new WrapperRuntimeException("Unable to query format types from docbase!", dfexception);
        }
        finally
        {
            try
            {
                if(idfcollection != null)
                    idfcollection.close();
            }
            catch(DfException dfexception1) { }
        }
        _formatList = (DataDropDownList)getControl("formatlist", com.documentum.web.form.control.databound.DataDropDownList.class);
        if(_formatList != null)
        {
            _formatList.getDataProvider().setResultSet(tableresultset, null);
            if(_strFormat != null && _strFormat.length() > 0)
                _formatList.setValue(_strFormat);
            else
                _formatList.setValue("unknown");
        }
    }



    /*
     * (non-Javadoc)
     *
     * @see com.documentum.webcomponent.library.checkin.Checkin#initControls() protected void
     *      initControls() { super.initControls(); initializeControls(); }
     */

    protected void initializeControls() {
        /*-CONFIG-*/String m="initializeControls - ";

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Initializing checkin controls... ", null, null);
        MrcsCheckInInfo chkInfo = null;
        int countRadio = 0;
        try {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Document : " + docObject, null, null);
            //Need to eliminate the usage of this try catch block by
            //better exception handling mechanizsm at Config broker
            CheckinConfigFactory config = CheckinConfigFactory.getCheckinConfig();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"config > " + config, null, null);
            chkInfo = config.getCheckinInfo(appName);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"CheckinInfo : " + chkInfo, null, null);
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+"Exception occurred while obtaining Checkin configurations ", null, e);
            throw new WrapperRuntimeException("Exception while retrieving MRCS checkin configuration",e);
        }

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locating checkin mode radio button controls in view", null, null);
        _radioBranchRevision = (Radio) getControl("branchrevision", com.documentum.web.form.control.Radio.class);
        _radioNewVersion = (Radio) getControl("newversion", com.documentum.web.form.control.Radio.class);
        _radioSameVersion = (Radio) getControl("sameversion", com.documentum.web.form.control.Radio.class);
        _radioMinorVersion = (Radio) getControl("minorversion", com.documentum.web.form.control.Radio.class);
        _radioMajorVersion = (Radio) getControl("majorversion", com.documentum.web.form.control.Radio.class);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locating locating same/minor/major panel controls in view", null, null);
        Panel sameversionPanel = (Panel) getControl("sameversionpanel", com.documentum.web.form.control.Panel.class);
        Panel minorVersionPanel = (Panel) getControl("minorversionpanel", com.documentum.web.form.control.Panel.class);
        Panel majorVersionPanel = (Panel) getControl("majorversionpanel", com.documentum.web.form.control.Panel.class);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"locating version label entry control", null, null);
        Text text = (Text) getControl("versionlabel", com.documentum.web.form.control.Text.class);
        if (text != null)text.setVisible(false);
        Label verlabel = (Label) getControl("MSG_DESCRIPTION", com.documentum.web.form.control.Label.class);
        if (verlabel != null)verlabel.setVisible(false);

        Panel descPanel = (Panel) getControl("descriptionspanel", com.documentum.web.form.control.Panel.class);
        if(descPanel != null)descPanel.setVisible(chkInfo.getAllowDescription());
        Panel descReqPanel = (Panel) getControl("descreqpanel", com.documentum.web.form.control.Panel.class);
        if(descReqPanel != null)descReqPanel.setVisible(chkInfo.getAllowDescription());

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"document attributes panel...", null, null);
        DocbaseAttributeValue docbaseattributevalue = (DocbaseAttributeValue) getControl("attr_value_description",
                com.documentum.web.formext.control.docbase.DocbaseAttributeValue.class);
        if(docbaseattributevalue != null)
            {
	            if (!docbaseattributevalue.isVisible()) {
	            docbaseattributevalue.setValue("");
	            docbaseattributevalue.setIsHidden(true);
	            docbaseattributevalue.setRequired(false);
	            docbaseattributevalue.setEnabled(false);
	            }
	            docbaseattributevalue.setVisible(chkInfo.getAllowDescription());
	            docbaseattributevalue.setRequired(true);
            }

        // disable editable name
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Disable editable object name", null, null);
        DocbaseAttributeValue namevalue = (DocbaseAttributeValue) getControl("attribute_object_name",com.documentum.web.formext.control.docbase.DocbaseAttributeValue.class);
        namevalue.setEnabled(false);

        Panel vlPanel = (Panel) getControl("versionlabelpanel", com.documentum.web.form.control.Panel.class);
        Checkbox mcPanel = (Checkbox) getControl("makecurrent", Checkbox.class);

        Panel saveasPanel = (Panel) getControl("existingobjversion", com.documentum.web.form.control.Panel.class);
        Panel saveasBranchPanel = (Panel) getControl("branchversion", com.documentum.web.form.control.Panel.class);


        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"defaulting all versioning radio buttons (Same/minor/major/branch/new) to disabled", null, null);
        _radioSameVersion.setEnabled(false);
        _radioMinorVersion.setEnabled(false);
        _radioMajorVersion.setEnabled(false);
        _radioBranchRevision.setEnabled(false);
        _radioNewVersion.setEnabled(false);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"defaulting all versioning radio buttons (Same/minor/major/branch/new) to unselected", null, null);
        _radioSameVersion.setValue(false);
        _radioMinorVersion.setValue(false);
        _radioMajorVersion.setValue(false);
        _radioBranchRevision.setValue(false);
        _radioNewVersion.setValue(false);

        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"determine if document is new...", null, null);
        boolean isNew = false;
        try {
        	for (int lbls = 0; lbls < docObject.getVersionLabelCount(); lbls++)
        	{
        		String curlabel = docObject.getVersionLabel(lbls);
        		if ("_NEW_".equals(curlabel)) isNew = true;
        	}
        	} catch (Exception e) {
                /*-ERROR-*/DfLogger.error(this, m+"Exception occurred while determining if object is _NEW_", null, e);
                throw new WrapperRuntimeException("Exception occurred while determining if object is _NEW_",e);
        	}
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" -- isnew? "+isNew, null, null);

        if (isNew)  {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"document is new, make _radioNewVersion enabled, selected, visible" , null, null);
            _radioNewVersion.setEnabled(true); //? needed for new? is there a "new" radio?
            _radioNewVersion.setValue(true); //?is there a "new" radio?
            if (vlPanel != null) vlPanel.setVisible(false);
            if (mcPanel != null) mcPanel.setVisible(false);

            saveasPanel.setVisible(false);
            saveasBranchPanel.setVisible(false);

            //saveasBranchPanel.setVisible(false);
            //majorVersionPanel.setVisible(false);
            //minorVersionPanel.setVisible(false);

            arrRadioValues = arrRadioValues+" "+"SAME";

            countRadio = 0;

        } else { // begin not new...

            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"document is not new, enable checkin modes based on MRCS CheckinConfiguration" , null, null);
	        if (_radioBranchRevision.isVisible()) {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"_radioBranchRevision is visible", null, null );
	            _radioBranchRevision.setVisible(chkInfo.getCheckinAsBranchVersion());
	            //saveasBranchPanel.setVisible(chkInfo.getCheckinAsBranchVersion());
	            if (chkInfo.getCheckinAsBranchVersion()) {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"BranchVersion true ", null, null);
	                arrRadioValues = arrRadioValues+" "+"BRANCH";
	                countRadio = ++countRadio;
	                _radioBranchRevision.setEnabled(true);
	                _radioBranchRevision.setValue(true);
	            }else{
	                _radioBranchRevision.setValue(false);
	                saveasBranchPanel.setVisible(false);
	            }
	            if (vlPanel != null) vlPanel.setVisible(true);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"vlPanel (Branch ) is visible: " +vlPanel.isVisible(), null, null);
	            if (mcPanel != null) mcPanel.setVisible(true);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"mcPanel (Branch ) is visible: " +mcPanel.isVisible(), null, null);

	        }

	        if (_radioMajorVersion.isVisible()) {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"_radioMajorVersion  is visible" , null, null);
	            if (chkInfo.getCheckinAsMajorVersion()) {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"MajorVersion true ", null, null);
	                _radioMajorVersion.setEnabled(true);
	                _radioMajorVersion.setValue(true);
	                _radioSameVersion.setValue(false);
	                _radioMinorVersion.setValue(false);
	            } else {
	                _radioMajorVersion.setValue(false);
	                _radioSameVersion.setValue(true);
	                _radioMinorVersion.setValue(false);

	            }
	            majorVersionPanel.setVisible(chkInfo.getCheckinAsMajorVersion());
	            if (vlPanel != null) vlPanel.setVisible(false);
	            if (mcPanel != null) mcPanel.setVisible(false);
	            if (chkInfo.getCheckinAsMajorVersion()) {
	                //arrRadioValues.add("Major");
	                arrRadioValues = arrRadioValues+" "+"MAJOR";
	                countRadio = ++countRadio;
	            }
	        }

	        if (_radioMinorVersion.isVisible()) {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"_radioMinorVersion  is visible" , null, null);
	            if (chkInfo.getCheckinAsMinorVersion()) {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"MinorVersion true ", null, null);
	                _radioMinorVersion.setEnabled(true);
	                _radioMinorVersion.setValue(true);
	                _radioSameVersion.setValue(false);
	                _radioMajorVersion.setValue(false);

	            } else {
	                _radioMinorVersion.setValue(false);
	                _radioSameVersion.setValue(true);
	                _radioMajorVersion.setValue(false);

	            }
	            minorVersionPanel.setVisible(chkInfo.getCheckinAsMinorVersion());
	            if (vlPanel != null) vlPanel.setVisible(false);
	            if (mcPanel != null) mcPanel.setVisible(false);
	            if (chkInfo.getCheckinAsMinorVersion()) {
	                //arrRadioValues.add("Minor");
	                arrRadioValues = arrRadioValues+" "+"MINOR";
	                countRadio = ++countRadio;
	            }
	        }

	        if (_radioSameVersion.isVisible()) {
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"_radioSameVersion  is visible ", null, null );
	            if (chkInfo.getCheckinAsSameVersion()) {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"SameVersion true ", null, null);
	                _radioSameVersion.setEnabled(true);
	                _radioSameVersion.setValue(true);
	                _radioMinorVersion.setValue(false);
	                _radioMajorVersion.setValue(false);
	            } else {
	                _radioSameVersion.setValue(false);
	                _radioMinorVersion.setValue(true);
	                _radioMajorVersion.setValue(false);
	            }
	            sameversionPanel.setVisible(chkInfo.getCheckinAsSameVersion());
	            if (vlPanel != null) vlPanel.setVisible(false);
	            if (mcPanel != null) mcPanel.setVisible(false);
	            if (chkInfo.getCheckinAsSameVersion()) {
	                arrRadioValues = arrRadioValues+" "+"SAME";
	                countRadio = ++countRadio;
	            }
	        }
        } // end !isNew


        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"countRadio " + countRadio, null, null);

        if (countRadio > 1) {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"countRadio > 1, show checkin options...", null, null);
            saveasPanel.setVisible(true);
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"arrRadioValues " + arrRadioValues, null, null);
        } else {
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"only one checkin option, so no need to provide checkin saveas options", null, null);
            saveasPanel.setVisible(false);
            saveasBranchPanel.setVisible(false);
        }


        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"calling mrcs format combo inti", null, null);
        initMRCSFormatCombo();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Checkin Controls initialized ", null, null);

    }


    public void onRender() {
        try {
            super.onRender();
            initializeControls() ;
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "onRender event threw error", null, e);
            throw new RuntimeException("MrcsCheckin onRender event threw error",e);
        }
    }

    public String getAllowableVersions(){
        return arrRadioValues;
    }


    public boolean onCommitChanges()
    {
        boolean superreturn = super.onCommitChanges();
        return superreturn;

    }



    /**
     *
     */
    public MrcsCheckin() {
        super();
    }


}