/*
 * Created on Jan 24, 2005
 *
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
 Version        4.2.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: ESignHelper.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2007/03/05 22:39:05 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.Iterator;
import java.util.List;

import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPackage;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfId;
import com.documentum.services.workflow.inbox.ITask;
import com.documentum.web.form.control.DropDownList;
import com.documentum.web.form.control.Option;
import com.medtronic.documentum.mrcs.config.ESignatureConfigFactory;

/**
 * @author prabhu1
 *
 * The ESignature Helper contains common initializations.
 */
public class ESignHelper {

	public final static String WFT_REPEAT ="Repeat";
	public final static String WFT_FORWARD ="Forward";
	public final static String WFT_REJECT ="Reject";
	public final static String WFT_ROUTE_REJECT ="RejectRouter";
	public final static String WFT_ROUTE_FWD ="FwdRouter";
	public final static String WFT_FINISH ="Reject";

	public final static String PASSWORD_CONTROL_NAME ="__PASSWORD_CONTROL_NAME"; //attribute_object_pswd
	public final static String USRTXT_CONTROL_NAME ="attribute_object_usr";
	public final static String REASONTXT_CONTROL_NAME ="attribute_object_reason";
	public final static String REASONSELECT_CONTROL_NAME ="reason_select";
	public final static String DEFAULT_REASON ="Select or Enter the Reason for Signing";

	public final static String DOCREJECT_RSNTXT_CONTROL_NAME ="attribute_object_rejectrsn";

	public final static String DOCREJECT_RSNLBL_CONTROL_NAME ="attribute_object_lbl_rejectrsn";
	public final static String DOCREJECT_DOCINFORSNLBL_CONTROL_NAME ="attribute_object_lbl_docinfo";
	public final static String DOCREJECT_HEADER_LBL_CONTROL_NAME="attribute_object_docrrlblhdr";
	public final static String DOCREJECT_DOCINFOHDR_LBL_CONTROL_NAME="attribute_object_dochdr";
	public final static String DOCREJECT_RSNHDR_LBL_CONTROL_NAME="attribute_object_rsnhdr";

	public ESignHelper() {
		super();
	}


/**
 * Initialize the Reason list using the Configuration.
 *
 * @param appName
 * @param rsnListCtrl
 * @throws Exception
 */
public static void initReasonList(String appName, DropDownList rsnListCtrl) throws Exception{

    ESignatureConfigFactory eSignConf = ESignatureConfigFactory.getESignConfig();
	// TODO get the Application Name from the IDF Document.
	//IDfDocument doc = getSignableDocument(itask, session, taskInfo);
	//appName = doc.getAppName();
	List rsnList = eSignConf.getSignatureReasons(appName);
	Option opt = null;
	String val = null;
	Iterator itList = rsnList.iterator();
		while(itList.hasNext()){
			val = (String)itList.next();
			opt = new Option();
			opt.setLabel(val);
			opt.setValue(val);
			rsnListCtrl.addOption(opt);
		}
}


	/**
	 * Get the Current signable document.
	 *
	 * @param itask
	 * @param session
	 * @return
	 * @throws DfException
	 */
	public static IDfDocument getSignableDocument(IDfSession idfsession, ITask itask)throws DfException{
        /*-CONFIG-*/String m="getSignableDocument - ";
		com.documentum.fc.client.IDfDocument documentObj = null;
        //IDfSessionManager idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
        //IDfSession idfsession = idfsessionmanager.getSession(SessionManagerHttpBinding.getCurrentDocbase());

        //CEM: this attachment retrival strategy has a strange bug: it only works athe first time you open the task, so if something fails
        //     and you try to reperform the task, it will return the wrong document version (the document at the beginning of the wf start,'
        //     not the current one), so if renditions are in the current version, it won't find them since it's looking at the pre-promoted
        //     document version.
		//MJH:	This doesn't work either.  Workflow task is a client object.  There's also a bug with anything that a query returns.
		//		The query is alway stale.  This will return the object based on the chronicle id.  That means that only the current version can be
		//		signed until Documentum comes up with a fix for the query problem that affects the DFC.
		/*
		IWorkflowTask  iworkflowtask = (IWorkflowTask )itask;
		IDfList lDocs= iworkflowtask.getAttachments();
		IWorkflowTaskAttachment wrkAttachment = (IWorkflowTaskAttachment)lDocs.get(0);
		IDfId docID = wrkAttachment.getDocumentId(0);
        */


        //CEM: this is an alternative way to get the package/attachment that seems to be much more reliable.
        //MJH: see above to avoid queries DfQuery packagequery = new DfQuery();
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"getting workflow id",null,null);
        IDfId workflowid = itask.getWorkflowId();
		IDfWorkitem wi = (IDfWorkitem)idfsession.getObject(itask.getId("item_id"));
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"workflow id: "+(workflowid==null?null:workflowid.getId()),null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"looking up workflow's package aka the attachment",null,null);
        IDfCollection attachments = wi.getPackages(null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"getting first package",null,null);
        attachments.next();
        IDfId packageid = attachments.getId("r_object_id");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"packageid: "+packageid.getId(),null,null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"retrieving IDfPackage object",null,null);
        IDfPackage pack = (IDfPackage)idfsession.getObject(packageid);
        IDfId docChronId = pack.getId("r_component_chron_id");
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class,m+"package doc chronicle id: "+(docChronId==null?null:docChronId.getId()),null,null);
        documentObj = (IDfDocument)idfsession.getObjectByQualification("dm_document where i_chronicle_id = '" + docChronId.getId() + "'");

        attachments.close();
		/*-DEBUG-*/if (DfLogger.isDebugEnabled(ESignHelper.class))DfLogger.debug(ESignHelper.class, m+"documentObj1 : "+documentObj, null, null);

		return documentObj;
	}

}
