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

 Filename       $RCSfile: SchedulePromote.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.5 $
 Modified on    $Date: 2006/09/25 20:13:04 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.client;


import java.text.SimpleDateFormat;
import java.util.Date;

import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.DfTime;
import com.documentum.web.common.ArgumentList;
import com.documentum.web.form.Control;
import com.documentum.web.form.control.DateTime;
import com.documentum.web.formext.component.Component;
import com.documentum.webcomponent.library.messages.MessageService;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

/**
 * @author prabhu1
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java -
 * Code Style - Code Templates
 */
public class SchedulePromote extends Component {

    private IDfDocument docObject = null;

     public void onInit(ArgumentList argumentlist) {
        try {
            String val[] = argumentlist.getValues("objectId");
            docObject = (IDfDocument) getDfSession().getObject(new DfId(val[0]));
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS: SchedulePromote----onInit---------" + docObject);
           // initializeControls();
        } catch (DfException e) {
            /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS: SchedulePromote----onInit Exception---------" + e);
            MessageService.addDetailedMessage(this, "MSG_INIT_ERROR", e.getMessage(), true);
        }
        
        super.onInit(argumentlist);
    }
        

    public boolean onCommitChanges() {
        boolean flag = super.onCommitChanges();
        
        return flag;
    }

    public void startScheduledPromote(Control button, ArgumentList argumentlist)
    {
    	/*-CONFIG-*/String m="startScheduledPromote - ";
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"startScheduledPromote called.",null,null);
        DateTime dt = (DateTime) getControl("promote_date", com.documentum.web.form.control.DateTime.class);
        String promoteTime = dt.toDateTimeString(DateTime.DF_SHORT, DateTime.TF_SHORT);
        /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"MRCS: SchedulePromote promote time " + promoteTime,null,null);

        try {
            StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
            String mrcsapp = docObject.getString("mrcs_application");
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"mrcs application of doc to be promoted: "+mrcsapp,null,null);
            String nextState = docObject.getNextStateName();
            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"state to promote to: "+nextState,null,null);
            
            if (config.isLegacyLCWF(mrcsapp)) 
            {            
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"LEGACY MRCS 4.1.2 - trying scheduled promote at time " + promoteTime + " for state " + nextState,null,null);
	            docObject.schedulePromote(nextState, new DfTime(dt.toDate()), false);
            } else { 
            
	            // new version - create job...
                /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"scheduled promote - MRCS 4.2+ ",null,null);
	            SimpleDateFormat sdf = new SimpleDateFormat("yyyyMMddHHmmss");	            
	            String execdate = sdf.format(dt.toDate()); 
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"date of promote - "+execdate,null,null);
	            String jobname = "MrcsScheduledPromote-"+docObject.getObjectId().getId()+"-"+execdate;
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"name of promotion job - "+jobname,null,null);
	            String methodargs = "-mrcsscheduledpromote yes -dboname "+getDfSession().getDocbaseOwnerName()+" -docbase_name "+getDfSession().getDocbaseName()+" -objectid "+docObject.getObjectId().getId();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    methodargs: "+methodargs,null,null);
	            
	            //String createjob = "CREATE dm_job OBJECT " +
	            //    "object_name = '"+jobname+"', " + // job name? document name + scheduled promotion time ? 
		        //    "method_name = 'MrcsPromoteMethod', " +
		        //    "method_arguments = '" +methodargs+"', " +
		        //    "pass_standard_arguments = false, " + //default is false anyway...
		        //    "start_date = "+execdate+", " +       // scheduled date of promotion
		        //    "expiration_date = "+execdate+", " +  // same as start date
		        //    "a_next_invocation = "+execdate+", " +  // same as start date
		        //    "max_iterations = 1, run_interval = 1, run_mode = 1, inactivate_after_failure = true, run_now = false";
	            
	            // create the object - need to be system user...
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"switching to system user for job creation",null,null);
	            String user = config.getSystemUsername(mrcsapp);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"system user for current mrcs app: "+user,null,null);
	            String pass = config.getSystemPassword(mrcsapp);
	            String docbase = config.getApplicationDocbase(mrcsapp);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"DOCBASE for mrcs app: "+docbase,null,null);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"reuse local client from session..."+docbase,null,null);
	            IDfClient curclient = getDfSession().getClient();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"prepare login ticket/info",null,null);
	            DfLoginInfo login = new DfLoginInfo();
	            login.setUser(user); 
	            login.setPassword(pass);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"creating new session manager",null,null);
	            IDfSessionManager sysmgr = curclient.newSessionManager();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"setting login identity",null,null);
	            sysmgr.setIdentity(docbase,login);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"finally, get system session from system session manager...",null,null);
	            IDfSession syssession = sysmgr.getSession(docbase);
	            
	            // create job object
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"CREATING new DM_JOB for scheduled promote method call",null,null);
	            IDfPersistentObject newjob = (IDfPersistentObject)syssession.newObject("dm_job");
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    object_name: "+jobname,null,null);
	            newjob.setString("object_name",jobname);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    method_name: MrcsPromoteMethod",null,null);
	            newjob.setString("method_name","MrcsPromoteMethod");
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    method_arguments: "+methodargs,null,null);
	            newjob.setRepeatingString("method_arguments",0,methodargs);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    start_date: "+execdate,null,null);
	            newjob.setTime("start_date",new DfTime(dt.toDate()));
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    a_next_invocation: "+execdate,null,null);
	            newjob.setTime("a_next_invocation",new DfTime(dt.toDate()));
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    preparing execdate + one year for expiration date...",null,null);
	            Date oneyearlater = dt.toDate();
	            oneyearlater.setYear(oneyearlater.getYear()+1);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    expiration_date: "+sdf.format(oneyearlater),null,null);
	            newjob.setTime("expiration_date",new DfTime(oneyearlater));
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    set max_iterations, run_interval, and run_mode to 1",null,null);
	            newjob.setInt("max_iterations",1);
	            newjob.setInt("run_interval",1);
	            newjob.setInt("run_mode",1);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    inactivate_after_failure = true",null,null);
	            newjob.setBoolean("inactivate_after_failure",true);
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"    run_now = false",null,null);
	            newjob.setBoolean("run_now",false);
	            newjob.setInt("method_trace_level",10);
	            
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"SAVE of new job...",null,null);
	            newjob.save();
	            /*-DEBUG-*/if (DfLogger.isDebugEnabled(this)) DfLogger.debug(this,m+"...job saved, releasing system session",null,null);
	            
	            sysmgr.release(syssession);
            }
	            

            MessageService.addMessage(this, "MSG_SUCCESS");
            
            
        }
        catch (DfException dfe){
            /*-ERROR-*/DfLogger.error(this,m+"-------startScheduledPromote Exception---------",null,dfe);
            //ErrorMessageService.getService().setNonFatalError(this, "MSG_NOT_ELIGIBLE_ERROR", null);
            MessageService.addDetailedMessage(this, "MSG_NOT_ELIGIBLE_ERROR", dfe.getMessage(), true);
        }

        setComponentReturn();
    }
    
    public void cancelScheduledPromote(Control button, ArgumentList argumentlist){
        /*-DEBUG-*/if (DfLogger.getRootLogger().isDebugEnabled()) DfLogger.getRootLogger().debug("MRCS: SchedulePromote cancelScheduledPromote called.");
        setComponentReturn();
    }

    /**
     *  
     */
    public SchedulePromote() {
        super();
    }
    

}