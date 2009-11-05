/*
 * Created on Jan 26, 2006
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package com.medtronic.documentum.mrcs.client;


import java.util.Date;
import java.util.HashMap;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.DfLogger;
import com.documentum.fc.common.IDfValue;
import com.documentum.web.formext.config.IQualifier;
import com.documentum.web.formext.config.QualifierContext;
import com.documentum.web.formext.session.SessionManagerHttpBinding;
import com.medtronic.documentum.mrcs.config.StateTransitionConfigFactory;

/**
 * @author muellc4
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsWorkflowCustomScopeQualifier implements IQualifier
{

 // DQL query cache
 // since this class gets called something like twenty to thirty times (don't know why),
 // we don't want to do the overhead of twenty DQL queries to look up the same value,
 // but we don't want to save this information for as long as the JVM is running. So we
 // cache the objectid - wf object names in a Hashmap when we look them up (querycache),
 // and count the number of calls to this method. Every ten thousand calls, we chuck the
 // old cache and create a new one. So the cache shouldn't reach more than 100 or 200 
 // entries. Synchronicity shouldn't be any concern, based on how I coded it. 
 static HashMap querycache = new HashMap();
 static int count = 0;
 
 public MrcsWorkflowCustomScopeQualifier()
 {
     
 }

 // context for this qualifier should be null...
 public String[] getContextNames()
 {
     return (new String[] {"objectId"});
     //return null;
 }

 public String getScopeName()
 {
     return "mrcsworkflow";
 }

 public /*synchronized*/ String getScopeValue(QualifierContext qualifiercontext)
 {
     /*-CFG-*/String m="getScopeValue()-";
     // I think this method determines the runtime scope value of the current component, which it returns,
     // and that is compared with the value in the component's scoping element of its configuration xml.
     // if the qualifier succeeds in detecting a workflowtask with an MRCS doc attached, we should return "mrcs"
          
     // default to not-mrcs:
     String matchvalue = "not mrcs";
     
     // get the objectid associated with this task (hopefully it is the task's id) if no objectid, no match
     String objectid = qualifiercontext.get("objectId");
     if (objectid == null) return matchvalue;
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"objectid in context: "+objectid,null,null);         
     // see if it is a wf task we can introspect, if not, return no-match
     String objecttype = qualifiercontext.get("type");
     if (objecttype == null) return matchvalue;
     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"type in context: "+objecttype,null,null);         
     
     if ("dm_task".equals(objecttype))
     {         
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"objectid and type found, type is dm_task",null,null);         
         IDfSessionManager idfsessionmanager = null;
         IDfSession idfsession = null;
                  
         // caching of calls to eliminate unnecessary DQL requests, clear cache every 10000 calls or so
         count++;
         if (count % 10000 == 0)
         {
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"count is modulus 10000, resetting query cache",null,null);         
             querycache = new HashMap();
         }

         
         // check if this query has been cached
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"start- "+new Date().getTime()+" -looking up objectid's workflow name",null,null);         
         String wfid = (String)querycache.get(objectid);
         /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"finish- "+new Date().getTime()+" -checking workflow to see if it is an MRCS workflow "+wfid,null,null);         
         if (wfid != null)
         {
             StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
             String docbase = SessionManagerHttpBinding.getCurrentDocbase();
             boolean found = config.isMrcsWorkflow(docbase,wfid);             
             if (found) matchvalue = "mrcs";             
             /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"finish- "+new Date().getTime()+" -looking up objectid's workflow name (cached lookup) - "+matchvalue,null,null);         
         }
         else // we have to look it up
         {
             try {
                 // see what the name of the dm_process this belongs to and see if it is in any of the registered mrcs configs...
                 
                 // we're in the web tier, so we can do the ol' SessionManagerHttpBinding
                 /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"get an IDFSession for our workflow lookup query",null,null);         
                 idfsessionmanager = SessionManagerHttpBinding.getSessionManager();
                 String docbase = SessionManagerHttpBinding.getCurrentDocbase();
                 idfsession = idfsessionmanager.getSession(docbase);         
                 // DQL to get workflow id from our current dm_task's object id (BTW, dm_task is not a valid dql object...): 
                 // - select r_workflow_id from dmi_workitem,dmi_queue_item where dmi_workitem.r_object_id = dmi_queue_item.item_id and dmi_queue_item.r_object_id = $objectIdd;
                 DfQuery wfidquery = new DfQuery();             
                 /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"getting workflow name",null,null);
                 // this four-object query shouldn't be too bad since we should be searching with indexes the whole way
                 String dqlWFID = "select dm_process.object_name from dm_process, dm_workflow, dmi_queue_item, dmi_workitem "+
                                  "where dm_process.r_object_id = dm_workflow.process_id and "+
                                        "dm_workflow.r_object_id = dmi_workitem.r_workflow_id and "+
                                        "dmi_workitem.r_object_id = dmi_queue_item.item_id and "+
                                        "dmi_queue_item.r_object_id = '"+objectid+"'"; 
                 /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"--> dql: "+dqlWFID,null,null);
                 wfidquery.setDQL(dqlWFID);
                 /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"--> executing",null,null);         
                 IDfCollection wfids = wfidquery.execute(idfsession, 0);
                 boolean hasnext = wfids.next();
                 if (hasnext)
                 {
                     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"--> trying to get result",null,null);
                     int vals = wfids.getValueCount(wfids.getAttr(0).getName());
                     IDfValue wfidval = wfids.getValue(wfids.getAttr(0).getName());
                     wfid = wfidval.asString();
                     // add to querycache
                     querycache.put(objectid,wfid);
                     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"--> result wfid: "+wfid,null,null);             
                     // now we scan for the wf in the configurations
                     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"getting StateTransition Config",null,null);         
                     StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
                     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"locating WF",null,null);         
                     boolean found = config.isMrcsWorkflow(docbase,wfid);             
                     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"Found? "+found,null,null);         
                     if (found) matchvalue = "mrcs";
                     /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"finish- "+new Date().getTime()+" -looking up objectid's workflow name (DQL lookup)",null,null);         
                 }
                 wfids.close();
             } catch (Exception e) {
                 /*-ERROR-*/DfLogger.error(MrcsWorkflowCustomScopeQualifier.class,m+"Mrcs WF Qualifier - error in workflow lookup",null,e);         
             }
             finally {
                 /*-DEBUG-*/if (DfLogger.isDebugEnabled(MrcsWorkflowCustomScopeQualifier.class))DfLogger.debug(MrcsWorkflowCustomScopeQualifier.class,m+"Releasing session",null,null);         
                 idfsessionmanager.release(idfsession);
             }
         }
     }     
     return matchvalue;
 }
/* 
 public String getScopeValue(QualifierContext qualifiercontext)
 {
     String s = qualifiercontext.get("application");
     if(s == null || s.length() == 0)
         s = ConfigService.getAppName();
     return s;
 }
*/

 public String getParentScopeValue(String s)
 {
     return null;
 }

 public String[] getAliasScopeValues(String s)
 {
     return null;
 }

 private static final String OBJECTID = "objectId";
 private static final String DOCBASE = "docbase";
}
