/*
 * Created on Mar 4, 2005
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

 Filename       $RCSfile: MrcsCheckinContainer.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.3 $
 Modified on    $Date: 2006/08/22 21:01:37 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.util.Map;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.common.WrapperRuntimeException;
import com.documentum.web.contentxfer.ContentTransferException;
import com.documentum.web.contentxfer.JobAdapter;
import com.documentum.web.contentxfer.impl.CheckinService;
import com.documentum.web.form.Control;
import com.documentum.web.form.Form;
import com.documentum.web.form.control.Radio;
import com.documentum.webcomponent.library.contenttransfer.checkin.CheckinContainer;
import com.documentum.webcomponent.library.messages.MessageService;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsCheckinContainer extends CheckinContainer {
    private IDfDocument docObject = null;

    private Radio _radioBranchRevision;

    private Radio _radioNewVersion;

    private Radio _radioSameVersion;

    private Radio _radioMinorVersion;

    private Radio _radioMajorVersion;

    private String arrRadioValues = null;

    public void onInit(ArgumentList argumentlist) {
         super.onInit(argumentlist);
         try {
             String val[] = argumentlist.getValues("objectId");
             docObject = (IDfDocument) getDfSession().getObject(new DfId(val[0]));
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsCheckinContainer:----onInit---------" + docObject, null, null);
            // initializeControls();
         } catch (DfException e) {
             /*-ERROR-*/DfLogger.error(this, "MrcsCheckinContainer:----onInit Exception---------", null, e);
             throw new RuntimeException("MrcsCheckinContainer's onInit event threw an error",e);
         }
    }


    public void onRender() {
        try {
            super.onRender();
        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, "Mrcs:MrcsCheckinContainer.onRender : Exception Occurred ", null, e);
            throw new RuntimeException("Error in MrcsCheckin's onRender event",e);
        }
    }


    public void onOk(Control control, ArgumentList argumentlist)
    {
        /*-CONFIG-*/String m="onOk-";
        boolean flag = false;
       /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"..."+control, null, null);
       // CEM: I bombed on this next log statement...IllegalArgumentException/cannot parse argument number
       //*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, "MrcsCheckinContainer:onOk : argumentlist: "+argumentlist, null, null);

        try {
           MrcsCheckin checkin = (MrcsCheckin)getContainedComponent();
           //*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get SAME/MINOR/MAJOR:  " + checkin.getSameMinorMajorVersion(), null, null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"get SAME/MINOR/MAJOR:  " + checkin.getSameMinorMajorVersion(), null, null);
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+"Allowableversions:  " + checkin.getAllowableVersions(), null, null);

            if(checkin.getAllowableVersions().indexOf(checkin.getSameMinorMajorVersion()) > 0){
                flag = true;
            }
           /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" : flag 1 " + flag, null, null);
            if(flag){
                //setMessage("MSG_CHECKIN_DEFAULT");
                MessageService.clear(this);
                super.onOk(control, argumentlist);
                //setMessage("MSG_CHECKIN_SUCCESS");
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this, m+" : flag" + flag, null, null);
            }else{
            	setMessage("MSG_CHECKIN_VERSION_NEEDED");
            }

        } catch (Exception e) {
            /*-ERROR-*/DfLogger.error(this, m+" : Exception Occurred : ", null, e);
            throw new WrapperRuntimeException("MrcsCheckinContainer onOk event threw error",e);
        }

    }


	private void setMessage(String s)
	{
	    MessageService.addMessage(this, s, null);
	}


    public boolean onCommitChanges() {
        boolean superreturn = super.onCommitChanges();
        return superreturn;

    }

    protected void handleOnReturnFromProgressSuccess(Form form, Map map, JobAdapter job)
    {
    	int i=1;
    	super.handleOnReturnFromProgressSuccess(form,map,job);
    	finished(form,map,job);
    	i++;
    }

    
    //public void finished(ContentXferServiceEvent contentxferserviceevent)
    public void finished(Form form, Map map, JobAdapter job)
    {
        /*-CONFIG-*/String m="finished-";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"calling super()", null, null);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting newly versioned doc's objid from checkin operation return values", null, null);
        // set state label
        try { 
            CheckinService service = (CheckinService)job.getService();
            Map newobjmap = service.getNewObjectIds();
            String newobj = (String)newobjmap.keySet().iterator().next();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"-- newid: "+newobj, null, null);
            // look up the newly checked in document...
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"retrieving new doc from docbase", null, null);
            IDfDocument newdoc = (IDfDocument)getDfSession().getObject(new DfId(newobj));
            // get the current state name
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting currentstate", null, null);
            String state = newdoc.getCurrentStateName();
            StateTransitionConfigFactory sts = StateTransitionConfigFactory.getSTConfig();
            
            // MRCS 4.1.2 legacy compatibility
            String mrcsapp = newdoc.getString("mrcs_application");
            if (sts.isLegacyLCWF(mrcsapp))
            {
	            String statename = sts.getStateInfo(mrcsapp,state).getLabel(); 
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"getting current state: "+statename, null, null);
	            // see if the document already has the label (this should only happen on initial checkin of NEW docs)
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"scanning doc labels to see if state name already attached", null, null);
	            int numlabels = newdoc.getVersionLabelCount();
	            boolean found = false;
	            for (int i=0; i < numlabels; i++)
	            {
	                String label = newdoc.getVersionLabel(i);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"checking doc label #"+i+": "+label, null, null);
	                if (statename.equals(label))
	                {
	                    /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"state label is already there", null, null);
	                    found = true;
	                }
	            }
	            if (!found)
	            {
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"doc label is not there, checkout doc so we can mark it", null, null);
	                newdoc.checkout();                  
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"marking document", null, null);
	                newdoc.mark(statename);
	                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this))DfLogger.debug(this,m+"saving", null, null);
	                newdoc.save();
	            }
            }
        } catch (DfException dfe) {  
            /*-ERROR-*/DfLogger.error(this,m+"finished event threw error in checkin while attempting to remark state name on document", null, dfe);
            throw new RuntimeException("MrcsCheckinContainer finished event threw error",dfe);
        } catch (ContentTransferException cte) {  
            /*-ERROR-*/DfLogger.error(this,m+"finished event threw content transfer error in checkin while attempting to get id of new doc version", null, cte);
            throw new RuntimeException("MrcsCheckinContainer finished event threw error",cte);
        }
    }


    /**
     *
     */
    public MrcsCheckinContainer() {
        super();
        // TODO Auto-generated constructor stub
    }

}
