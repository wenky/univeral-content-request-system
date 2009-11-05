package com.medtronic.ecm.documentum.mdtworkflow.util;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfPackage;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.util.CollUtils;


public class WorkflowUtils 
{
    public static IDfSessionManager getSystemSessionManager(IDfSession session, String mdtapp) throws DfException
    {
        /*-dbg-*/Lg.dbg("get session");
        IDfSessionManager sMgrSU = session.getClient().newSessionManager();
        
        /*-dbg-*/Lg.dbg("switch to SU");
        IDfSysObject appcfg = (IDfSysObject)session.getObjectByQualification(MdtConfigService.CONFIG_SERVICE_SEARCH_QUALIFICATION+mdtapp+"'");
        String suuser = appcfg.getString("m_zzz_uid");
        String supass = appcfg.getString("m_zzz_pcred");
        IDfLoginInfo logininfo = new DfLoginInfo();
        logininfo.setUser(suuser);
        logininfo.setPassword(supass);
        sMgrSU.setIdentity(session.getDocbaseName(),logininfo);
        /*-dbg-*/Lg.dbg("returning SU system manager");
        
        return sMgrSU;
        
    }
    
	
	public static IDfDocument getPrimaryPackage(IDfSession idfsession, String wfid, String itemid) throws DfException
	{
		IDfDocument documentObj = null;

        //CEM: this attachment retrival strategy has a strange bug: it only works athe first time you open the task, so if something fails
        //     and you try to reperform the task, it will return the wrong document version (the document at the beginning of the wf start,'
        //     not the current one), so if renditions are in the current version, it won't find them since it's looking at the pre-promoted
        //     document version.
		//MJH:	This doesn't work either.  Workflow task is a client object.  There's also a bug with anything that a query returns.
		//		The query is alway stale.  This will return the object based on the chronicle id.  That means that only the current version can be
		//		signed until Documentum comes up with a fix for the query problem that affects the DFC.

        //CEM: this is an alternative way to get the package/attachment that seems to be much more reliable.
        //MJH: see above to avoid queries DfQuery packagequery = new DfQuery();
        /*-dbg-*/Lg.dbg("getting workflow id %s",wfid);
        IDfId workflowid = new DfId(wfid);
        /*-dbg-*/Lg.dbg("getting workitem id %s",itemid);
		IDfWorkitem wi = (IDfWorkitem)idfsession.getObject(new DfId(itemid));
        /*-dbg-*/Lg.dbg("workflow id: %s",workflowid);
        /*-dbg-*/Lg.dbg("looking up workflow's package aka the attachment");
        IDfCollection attachments = null;
        try {
        	attachments = wi.getPackages(null);
	        /*-dbg-*/Lg.dbg("getting first package");
	        attachments.next();
	        IDfId packageid = attachments.getId("r_object_id");
	        /*-dbg-*/Lg.dbg("packageid: %s",packageid);
	        /*-dbg-*/Lg.dbg("retrieving IDfPackage object");
	        IDfPackage pack = (IDfPackage)idfsession.getObject(packageid);
	        IDfId docChronId = pack.getId("r_component_chron_id");
	        /*-dbg-*/if(Lg.dbg())Lg.dbg("package doc chronicle id: ",(docChronId==null?null:docChronId.getId()));
	        documentObj = (IDfDocument)idfsession.getObjectByQualification("dm_document where i_chronicle_id = '" + docChronId.getId() + "'");
        } finally {
        	try { attachments.close();} catch (Exception e){}
        }

		return documentObj;
	}

    public static IDfSysObject getPrimaryPackage (IDfSession session, String wfId) throws DfException 
    {
        try{
            String qualification = "select r_component_id from dmi_package where r_workflow_id = '" + wfId +"'";

            IDfQuery qry = new DfQuery();
            /*-dbg-*/Lg.dbg("SET QUERY : %s",qualification);
            qry.setDQL(qualification);

            String goodid = null;
            /*-dbg-*/Lg.dbg("EXEC QUERY");            
            IDfCollection myObj1 = null; 
            try { 
                myObj1 = (IDfCollection)qry.execute(session,IDfQuery.DF_READ_QUERY);
                while(myObj1.next()) {
                    for (int i = 0; i < myObj1.getAttrCount(); i++) { 
                        IDfAttr attr = myObj1.getAttr(i);
                        if (attr.getName().equalsIgnoreCase("r_component_id")) {
                            String id = myObj1.getString(attr.getName());
                            // in certain situations, such as rollback, the id may be for a destroyed object, but the original is should still be in there, in the InitailPromote packages... 
                            try {
                                IDfSysObject getdoc = (IDfSysObject)session.getObject(new DfId(id));
                                if (getdoc != null) {
                                    goodid = id;
                                }
                            } catch (DfException dfe) {
                                /*-WARN-*/Lg.dbg("invalid id, could be a rollback artifact: %s",id);
                            }
                            /*-dbg-*/Lg.dbg("id : --->>%s<<",id);
                        }
                    }
                }
            } finally {
                try { myObj1.close(); } catch (Exception e) {}
            }
            
            /*-dbg-*/Lg.dbg("RETRIEVING %s",goodid);
            IDfSysObject pkg = (IDfSysObject)session.getObject(new DfId(goodid));
            /*-dbg-*/Lg.dbg("pkg: %s",pkg);

            /*-dbg-*/Lg.dbg("make sure it is the latest version");
            IDfSysObject latest = (IDfSysObject)session.getObjectByQualification("mdt_workflow_form where i_chronicle_id = '"+pkg.getChronicleId().getId()+"'");
            /*-dbg-*/Lg.dbg("latest: %s",latest);

            return latest;

        }catch(Exception e){
            /*-ERROR-*/Lg.err("MdtAbortWorkflow : getDocID: Exception Occurred ",e);
            throw EEx.create("MdtAbortWorkflow-GetPkg","MdtAbortWorkflow : getDocID: Exception Occurred ",e);
        }
    }
    
    public static List getMostRecentAttachments(IDfSession session,String wfid) throws DfException
    {
        /*-dbg-*/Lg.wrn("get workflow object");
        IDfWorkflow workflow = (IDfWorkflow)session.getObject(new DfId(wfid));
        /*-dbg-*/Lg.wrn("-- retrieved %s",workflow);
        
        // get attachments from wf attachments
        List attachmentlist = new ArrayList();
        // okey-dokey: now check for additional attachments to the workflow...
        IDfCollection attachments = null;
        try {
            /*-dbg-*/Lg.wrn("getting ATTACHMENT collection for workitem");
            attachments =  workflow.getAttachments();
            while(attachments.next())
            {
                /*-dbg-*/Lg.wrn("--NEXT ATTACHMENT--");
                for (int i = 0; i < attachments.getAttrCount(); i++)
                {
                    /*-dbg-*/Lg.wrn("attr:    "+attachments.getAttr(i).getName());
                    /*-dbg-*/Lg.wrn("type:    "+attachments.getAttr(i).getDataType());
                    /*-dbg-*/Lg.wrn("repeats: "+attachments.getAttr(i).isRepeating());
                    /*-dbg-*/Lg.wrn("val:     "+attachments.getValueAt(i).asString());
                }
                String compid = attachments.getString("r_component_id");
                /*-dbg-*/Lg.wrn("--ATTACHMENT id: "+compid);
                if (compid != null) {
                    /*-dbg-*/Lg.wrn("--getting ATTACHMENT from docbase");
                    IDfDocument attacheddoc = (IDfDocument)session.getObject(new DfId(compid));
                    /*-dbg-*/Lg.wrn("--pkgdoc retrieved: "+attacheddoc.getObjectName() + " - "+attacheddoc.getObjectId().getId());

                    /*-dbg-*/Lg.wrn("--getting most recent version of attachment");
                    IDfDocument pkgdoc = (IDfDocument)session.getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
                    /*-dbg-*/Lg.wrn("--most recent retrieved: "+pkgdoc.getObjectName() + " - "+pkgdoc.getObjectId().getId());

                    /*-dbg-*/Lg.wrn("adding most-recent to attachment list");
                    attachmentlist.add(pkgdoc);
                }
            }
        } finally {
            /*-dbg-*/Lg.wrn("finally, close the attachment collection");
            if (attachments != null)
                attachments.close();
        }
        return attachmentlist;
    }

    public static boolean checkRenditions(IDfSysObject doc, String format) throws DfException
    {
        // stolen from ESignPrecondition
        boolean flag = false;
        String objectId = doc.getObjectId().getId();
        /*-dbg-*/Lg.dbg("top");
        if(objectId == null || format == null)
            throw new IllegalArgumentException("Object ID and format must be valid!");
        IDfCollection idfcollection = null;
        /*-dbg-*/Lg.dbg("executing rendition search");        
        IDfSession session = doc.getSession();
        DfQuery dfquery = new DfQuery();
        String dql = "select r_object_id from dmr_content where any parent_id='" + objectId + "' and full_format='" + format + "' and rendition > 0";
        /*-dbg-*/Lg.dbg("-- DQL [%s]", dql);
        try { 
            dfquery.setDQL(dql);
            idfcollection = dfquery.execute(session, 0);
            /*-dbg-*/Lg.dbg("check if search returned items, thus a rendition found");
            flag = idfcollection.next();
            /*-dbg-*/Lg.dbg("found? "+flag);
        } finally { 
            try {idfcollection.close();} catch (Exception e){}
        }
        // return inverse of flag: we want to return true if no renditions found, false if they were found
        return flag;
    }
 
    public static List getChangeRequestTypesForApplication(IDfSession session, MdtConfigService cfg,String application)
    {
        /*-INFO-*/Lg.inf("top, app: %s",application);
        List crtypelist = new ArrayList();
        // get config
        /*-dbg-*/Lg.dbg("get app config");
        Map appcfg = (Map)cfg.getAppConfig(application);
        // get workflow list
        /*-dbg-*/Lg.dbg("get workflows from config");
        Map workflows = (Map)appcfg.get("ChangeRequests");
        if (workflows == null) {
            return crtypelist;
        }
        
        // iterate through the workflows, getting the associated change request doc type (what about access rights - check base workflow access rights...)
        /*-dbg-*/Lg.dbg("iterate through workflow config defs");
        Iterator workflowkeys = workflows.keySet().iterator();
        while (workflowkeys.hasNext()) 
        {
            /*-dbg-*/Lg.dbg("next key");
            String crtype = (String)workflowkeys.next();
            /*-dbg-*/Lg.dbg("get %s def",crtype);
            Map workflowdef = (Map)workflows.get(crtype);
            if (workflowdef.containsKey("CheckWorkflowTemplatePermissions")) {
                /*-dbg-*/Lg.dbg("get associated cr type");
                String workflow = (String)workflowdef.get("WorkflowTemplate");
                // check workflow access rights...
                try { 
                    /*-dbg-*/Lg.dbg("check access rights of current user to workflow");
                    IDfSysObject workflowobj = (IDfSysObject)session.getObjectByQualification("dm_process where object_name = '"+workflow+"'");
                    /*-dbg-*/Lg.dbg("get permissions on %s",workflowobj);
                    int permit = workflowobj.getPermit();
                    /*-dbg-*/Lg.dbg("is permit %d greater than relate?",permit);
                    if (permit >= IDfACL.DF_PERMIT_RELATE) {
                        /*-dbg-*/Lg.dbg("yes, adding change request type %s to type list",crtype);
                        crtypelist.add(crtype);
                    }
                } catch (DfException dfe) {
                    /*-ERROR-*/Lg.err("Error retrieving workflow object %s from docbase",workflow,dfe);
                    throw EEx.create("Error retrieving workflow object %s from docbase",workflow,dfe);                
                }
            } else {            
                crtypelist.add(crtype);
            }
        }
        return crtypelist;
    }
    
    public static Map getChangeRequestConfig(IDfSessionManager mgr, String docbase, String mdtapp, IDfSysObject formobj) throws DfException
    {
        //cfg["ChangeRequests"][cr_doctype]
        MdtConfigService cfgsvc = MdtConfigService.getConfigService(mgr,docbase);
        Map config = cfgsvc.getAppConfig(mdtapp);
        return (Map)CollUtils.lookup(config,"ChangeRequests",formobj.getTypeName());
    }
    
    public static Map getTaskConfig(IDfSessionManager mgr, String docbase, String mdtapp, String taskname, IDfSysObject controldoc) throws DfException
    {
        //cfg["ChangeRequests"][formdoctype]["Tasks"][taskname()]
        MdtConfigService cfgsvc = MdtConfigService.getConfigService(mgr,docbase);
        Map config = cfgsvc.getAppConfig(mdtapp);
        Map taskconfig = (Map)CollUtils.lookup(config, "ChangeRequests",controldoc.getTypeName(),"Tasks",taskname);
        return taskconfig;
    }

    public static Map getJobConfig(IDfSessionManager mgr, String docbase, String mdtapp, String taskname) throws DfException
    {
        //cfg["Jobs"][taskname]
        MdtConfigService cfgsvc = MdtConfigService.getConfigService(mgr,docbase);
        Map config = cfgsvc.getAppConfig(mdtapp);
        Map taskconfig = (Map)CollUtils.lookup(config, "Jobs",taskname);
        return taskconfig;
    }


}
