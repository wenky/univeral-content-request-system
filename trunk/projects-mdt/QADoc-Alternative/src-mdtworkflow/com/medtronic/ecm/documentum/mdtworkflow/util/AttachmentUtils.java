package com.medtronic.ecm.documentum.mdtworkflow.util;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfWorkflow;
import com.documentum.fc.client.IDfWorkitem;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfId;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.common.MdtConfigService;
import com.medtronic.ecm.documentum.common.MdtPlugin;
import com.medtronic.ecm.documentum.common.MdtPluginLoader;
import com.medtronic.ecm.documentum.mdtworkflow.common.plugins.IMdtAddAttachment;
import com.medtronic.ecm.documentum.mdtworkflow.common.plugins.IMdtRemoveAttachment;
import com.medtronic.ecm.documentum.util.DctmUtils;

public class AttachmentUtils 
{
    public static IDfWorkflow getAttachmentWorkflow(IDfSysObject sysobject) throws DfException
    {
        // by validation enforcement, a sysobject should only be in a single active workflow
        // --we could make this better by doing an IN on the sysobject current version id and previous version id
        // --however, since types can only belong to a single mdtapp, we should be okay
        String dql = 
            "SELECT DISTINCT r_workflow_id "+
            "FROM dmi_wf_attachment " +
            "WHERE r_component_name = '"+sysobject.getObjectName()+"' AND "+
            "r_component_type = '"+sysobject.getTypeName()+"'";
        
        IDfId wfid = DctmUtils.execSingleValueQuery(sysobject.getSession(), dql).asId();
        IDfWorkflow wf = (IDfWorkflow)sysobject.getSession().getObject(wfid);
        return wf;
    }
    
    
    public static IDfSysObject getMostRecent(IDfSysObject object) throws DfException 
    {
        return (IDfSysObject)object.getSession().getObjectByQualification("dm_document where i_chronicle_id = '"+object.getChronicleId().getId()+"'");        
    }
    
    public static List getMostRecent(List attachments) throws DfException
    {
        List recentattachments = new ArrayList();
        for (int i=0; i < attachments.size(); i++)
        {
            IDfSysObject so = (IDfSysObject)attachments.get(i);
            IDfSysObject newso = (IDfSysObject)so.getSession().getObjectByQualification("dm_sysobject where i_chronicle_id = '"+so.getChronicleId().getId()+"'");
            recentattachments.add(newso);
        }
        return recentattachments;
    }
    
    
    public static List getAttachmentsByRelationship(IDfSession session, IDfSysObject formdoc, String relationtypename) throws DfException
    {
        IDfCollection packages = null;
        String pkgs = null;
        String parentdocid = null;
        List attachmentlist = new ArrayList();            
        try {
            String query = "select r_object_id, r_object_type, r_aspect_name, i_vstamp from dm_document(all) " +
                           "where r_object_id in (select child_id from dm_relation where relation_name = '"+relationtypename+"' and parent_id = '"+formdoc.getObjectId().getId()+"')";
            /*-dbg-*/Lg.wrn("QUERY: %s",query);
            IDfQuery qry = new DfQuery();
            qry.setDQL(query);
            /*-dbg-*/Lg.wrn("exec query");
            packages = qry.execute(session,IDfQuery.DF_READ_QUERY);
            while (packages.next()) 
            {
                /*-dbg-*/Lg.wrn("get next related doc");
                String compid = packages.getId("r_object_id").getId();
                /*-dbg-*/Lg.wrn("related docid is %s",compid);
                IDfDocument pkgdoc = (IDfDocument)session.getObject(new DfId(compid));
                /*-dbg-*/Lg.wrn("check if %s is a mdt_document",pkgdoc);
                if (pkgdoc.isInstanceOf("mdt_document"))
                {
                    /*-dbg-*/Lg.wrn("--getting most recent version of attachment");
                    IDfSysObject recentdoc = (IDfSysObject)session.getObjectByQualification("mdt_document where i_chronicle_id = '"+pkgdoc.getChronicleId().getId()+"'");
                    /*-dbg-*/Lg.wrn("--most recent retrieved: %s",recentdoc);                    
                    attachmentlist.add(recentdoc);                    
                }
            }
            packages.close(); packages = null;
        } finally {
            try { if (packages != null)packages.close(); } catch (Exception e) {}
        }
        
        
        /*-dbg-*/Lg.wrn("done");
        return attachmentlist;
    }
    
    
    public static List getWorkflowAttachments(IDfWorkitem workitem) throws DfException
    {
        // get attachments from wf attachments
        List attachmentlist = new ArrayList();
        // okey-dokey: now check for additional attachments to the workflow...
        IDfCollection attachments = null;
        try {
            /*-dbg-*/Lg.wrn("getting ATTACHMENT collection for workitem");
            attachments =  workitem.getAttachments();
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
                    IDfDocument attacheddoc = (IDfDocument)workitem.getSession().getObject(new DfId(compid));
                    /*-dbg-*/Lg.wrn("--pkgdoc retrieved: "+attacheddoc.getObjectName() + " - "+attacheddoc.getObjectId().getId());

                    /*-dbg-*/Lg.wrn("--getting most recent version of attachment");
                    IDfDocument pkgdoc = (IDfDocument)workitem.getSession().getObjectByQualification("dm_document where i_chronicle_id = '"+attacheddoc.getChronicleId().getId()+"'");
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

    
    public static void addAttachmentToForm(IDfSysObject formdoc, String attachmentid)
    {
        /*-dbg-*/Lg.dbg("add %s to %s",attachmentid,formdoc);
        try { 
            String mdtapp = formdoc.getString("m_application");
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            String formtype = formdoc.getTypeName();
            /*-dbg-*/Lg.dbg("getting CR Config for formtype: %s",formtype);
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("get attachment, was cr null? %b",crconfig==null);
            IDfSysObject attachment = (IDfSysObject)formdoc.getSession().getObject(new DfId(attachmentid));
            // check for "AttachmentAddPlugins"
            List attachmentplugins = (List)crconfig.get("AttachmentAdditionPlugins");
            /*-dbg-*/Lg.dbg("has add attachment plugins (AttachmentAddPlugins key) in cr? %b",attachmentplugins==null);
            if (attachmentplugins != null) {
                for (int i=0; i < attachmentplugins.size(); i++) {
                    /*-dbg-*/Lg.dbg("do plugin #%d",i);
                    MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                    /*-dbg-*/Lg.dbg("load plugin");
                    IMdtAddAttachment addplug = (IMdtAddAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                    /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                    addplug.add(formdoc, attachment, crconfig, (Map)plugin.context);
                    /*-dbg-*/Lg.dbg("done");
                }
            }
            /*-dbg-*/Lg.dbg("appending attachment name to m_attachments, attachment: %s",attachment);
            formdoc.appendString("m_attachments", attachment.getObjectName());
            // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
            //formdoc.appendString("m_attachments_chids", attachment.getChronicleId().getId());
            /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
            formdoc.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("addAttachmentToForm : Exception Occurred ",e);
            throw EEx.create("addAttachmentToForm-Error","addAttachmentToForm : Exception Occurred ",e);
        }
    }
    
    public static void addDocumentToDocumentAttribute(IDfSysObject formdoc, String attachmentid, String attrname, String addpluginkey)
    {
        /*-dbg-*/Lg.dbg("add %s to %s",attachmentid,formdoc);
        try { 
            String mdtapp = formdoc.getString("m_application");
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            String formtype = formdoc.getTypeName();
            /*-dbg-*/Lg.dbg("getting CR Config for formtype: %s",formtype);
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("get attachment, was cr null? %b",crconfig==null);
            IDfSysObject attachment = (IDfSysObject)formdoc.getSession().getObject(new DfId(attachmentid));
            // check for "AttachmentAddPlugins"
            List attachmentplugins = (List)crconfig.get(addpluginkey);
            /*-dbg-*/Lg.dbg("has add attachment plugins (AttachmentAddPlugins key) in cr? %b",attachmentplugins==null);
            if (attachmentplugins != null) {
                for (int i=0; i < attachmentplugins.size(); i++) {
                    /*-dbg-*/Lg.dbg("do plugin #%d",i);
                    MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                    /*-dbg-*/Lg.dbg("load plugin");
                    IMdtAddAttachment addplug = (IMdtAddAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                    /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                    addplug.add(formdoc, attachment, crconfig, (Map)plugin.context);
                    /*-dbg-*/Lg.dbg("done");
                }
            }
            /*-dbg-*/Lg.dbg("appending attachment name to docattr, attachment: %s",attachment);
            formdoc.appendString(attrname, attachment.getObjectName());
            // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
            //formdoc.appendString("m_attachments_chids", attachment.getChronicleId().getId());
            /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
            formdoc.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("addDocumentToDocumentAttribute : Exception Occurred ",e);
            throw EEx.create("addDoctoAttr-Error","addAttachmentToForm : Exception Occurred ",e);
        }
    }
    
    
    // add a iterator of ids to the form (this cuts down on the number of calls to formdoc.save();
    public static void addAttachmentsToForm(IDfSysObject formdoc, Iterator attachmentids)
    {
        try {
            
            /*-dbg-*/Lg.dbg("get mdt app value and change request config");
            String mdtapp = formdoc.getString("m_application");
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            List attachmentplugins = (List)crconfig.get("AttachmentAdditionPlugins");
            while (attachmentids.hasNext()) {
                String attachmentid = (String)attachmentids.next();                
                /*-dbg-*/Lg.dbg("add %s to %s",attachmentid,formdoc);
                /*-dbg-*/Lg.dbg("get attachment, was cr null? %b",crconfig==null);
                IDfSysObject attachment = (IDfSysObject)formdoc.getSession().getObject(new DfId(attachmentid));
                // check for "AttachmentAddPlugins"
                /*-dbg-*/Lg.dbg("has add attachment plugins (AttachmentAddPlugins key) in cr? %b",attachmentplugins==null);
                if (attachmentplugins != null) {
                    for (int i=0; i < attachmentplugins.size(); i++) {
                        /*-dbg-*/Lg.dbg("do plugin #%d",i);
                        MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                        /*-dbg-*/Lg.dbg("load plugin");
                        IMdtAddAttachment addplug = (IMdtAddAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                        /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                        addplug.add(formdoc, attachment, crconfig, (Map)plugin.context);
                        /*-dbg-*/Lg.dbg("done");
                    }
                }
                /*-dbg-*/Lg.dbg("appending attachment name to m_attachments, attachment: %s",attachment);
                formdoc.appendString("m_attachments", attachment.getObjectName());
                // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
                //formdoc.appendString("m_attachments_chids", attachment.getChronicleId().getId());
                /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
                /*-dbg-*/Lg.dbg("done");
            }
            formdoc.save();
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("addAttachmentToForm : Exception Occurred ",e);
            throw EEx.create("addAttachmentToForm-Error","addAttachmentToForm : Exception Occurred ",e);
        }
    }

    
    public static void addDocumentsToDocumentAttribute(IDfSysObject formdoc, Iterator attachmentids, String docattr, String addpluginkey)
    {
        try {
            
            /*-dbg-*/Lg.dbg("get mdt app value and change request config");
            String mdtapp = formdoc.getString("m_application");
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            List attachmentplugins = (List)crconfig.get(addpluginkey);
            while (attachmentids.hasNext()) {
                String attachmentid = (String)attachmentids.next();                
                /*-dbg-*/Lg.dbg("add %s to %s",attachmentid,formdoc);
                /*-dbg-*/Lg.dbg("get attachment, was cr null? %b",crconfig==null);
                IDfSysObject attachment = (IDfSysObject)formdoc.getSession().getObject(new DfId(attachmentid));
                // check for "AttachmentAddPlugins"
                /*-dbg-*/Lg.dbg("has add attachment plugins (AttachmentAddPlugins key) in cr? %b",attachmentplugins==null);
                if (attachmentplugins != null) {
                    for (int i=0; i < attachmentplugins.size(); i++) {
                        /*-dbg-*/Lg.dbg("do plugin #%d",i);
                        MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                        /*-dbg-*/Lg.dbg("load plugin");
                        IMdtAddAttachment addplug = (IMdtAddAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                        /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                        addplug.add(formdoc, attachment, crconfig, (Map)plugin.context);
                        /*-dbg-*/Lg.dbg("done");
                    }
                }
                /*-dbg-*/Lg.dbg("appending attachment name to m_attachments, attachment: %s",attachment);
                formdoc.appendString(docattr, attachment.getObjectName());
                // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
                //formdoc.appendString("m_attachments_chids", attachment.getChronicleId().getId());
                /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
                /*-dbg-*/Lg.dbg("done");
            }
            formdoc.save();
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("addAttachmentToForm : Exception Occurred ",e);
            throw EEx.create("addAttachmentToForm-Error","addAttachmentToForm : Exception Occurred ",e);
        }
    }

    
    
    // retrieve/lookup a document by name, using the config settings of the config to subset the document search so we reduce risk of encountering a name collision
    // basically, docs are required to have a unique name within a defined mdtapp's assigned object types.
    // TODO: filter copies (copies are a bad idea...) Idea: copies always have a different name (say originalname + "-approvedcopy")
    public static IDfSysObject lookupAttachmentByName(IDfSysObject formobject, String name) 
    {
        // get mdtapp
        try { 
            String mdtapp = formobject.getString("m_application");
            IDfSession session = formobject.getSession();
            return lookupMdtDocumentByNameMdtApp(session,name,mdtapp);
        } catch(DfException dfe) {
            /*-ERROR-*/Lg.err("lookupAttachmentByName - dfexception on formobject %s looking up attachment named %s",formobject,name,dfe);
            throw EEx.create("attachmentLookup-DFE","lookupAttachmentByName - dfexception on formobject %s looking up attachment named %s",formobject,name,dfe);            
        } catch(NullPointerException npe) {
            /*-ERROR-*/Lg.err("lookupAttachmentByName - NULLPOINTER on formobject %s looking up attachment named %s",formobject,name,npe);
            throw EEx.create("attachmentLookup-DFE","lookupAttachmentByName - NULLPOINTER on formobject %s looking up attachment named %s",formobject,name,npe);            
        }
    }
    public static IDfSysObject lookupMdtDocumentByNameMdtApp(IDfSession session,  String name, String mdtapp) 
    {
        IDfSysObject attachment = null;
        try {
            boolean strict = false;
            // old/strict implementation (breaks supporting docs which is much more lax: fix by separating into strict and loose)
            if (strict) {
                // get config
                MdtConfigService cfgsvc = MdtConfigService.getConfigService(session.getSessionManager(), session.getDocbaseName());
                // get list of assigned object types for the mdtapp
                List objecttypes = cfgsvc.getTypesForApplication(mdtapp);
                // search each object type for a doc named that
                for (int i=0; i < objecttypes.size(); i++) {
                    String qual = (String)objecttypes.get(i) + " WHERE object_name = '"+name+"'";
                    attachment = (IDfSysObject)session.getObjectByQualification(qual);
                    if (attachment != null) {
                        return attachment;
                    }
                }
            }
            
            if (!strict) {
                String qual = "mdt_qad_doc WHERE object_name = '"+name+"' and m_application = '"+mdtapp+"'";
                attachment = (IDfSysObject)session.getObjectByQualification(qual);
                if (attachment != null) {
                    return attachment;
                }
            }
            
            
            
        } catch(DfException dfe) {
            /*-ERROR-*/Lg.err("lookupAttachmentByName - dfexception looking up attachment named %s",name,dfe);
            throw EEx.create("attachmentLookup-DFE","lookupAttachmentByName - dfexception looking up attachment named %s",name,dfe);            
        } catch(NullPointerException npe) {
            /*-ERROR-*/Lg.err("lookupAttachmentByName - NULLPOINTER looking up attachment named %s",name,npe);
            throw EEx.create("attachmentLookup-DFE","lookupAttachmentByName - NULLPOINTER looking up attachment named %s",name,npe);            
        }
        /*-WARN-*/Lg.wrn("attachment %s not found",name);
        return null;
    }
    
    public static void removeAllAttachmentsFromForm(IDfSysObject formdoc)
    {
        /*-dbg-*/Lg.dbg("removingall attachments from %s",formdoc);
        try { 
            String mdtapp = formdoc.getString("m_application");
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            String formtype = formdoc.getTypeName();
            /*-dbg-*/Lg.dbg("getting CR Config for formtype: %s",formtype);
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("cr config is null? %b",crconfig==null);
            
            // get list of attachment names from formdoc
            /*-dbg-*/Lg.dbg("get list of attachment names");
            List l = new ArrayList();
            for (int a=0; a < formdoc.getValueCount("m_attachments"); a++) {
                l.add(formdoc.getRepeatingString("m_attachments",a));
            }
            
            /*-dbg-*/Lg.dbg("iterate on m_attachments");
            for (int att=0; att < l.size(); att++)
            {
                /*-dbg-*/Lg.dbg("get next name to remove");
                String name = (String)l.get(att);
                /*-dbg-*/Lg.dbg("lookup object named %s",name);
                IDfSysObject attachment = lookupAttachmentByName(formdoc, name);
                // check for "AttachmentRemovePlugins"
                List attachmentplugins = (List)crconfig.get("AttachmentRemovalPlugins");
                /*-dbg-*/Lg.dbg("has remove attachment plugins (AttachmentRemovalPlugins key) in cr? %b",attachmentplugins==null);
                if (attachmentplugins != null) {
                    for (int i=0; i < attachmentplugins.size(); i++) {
                        /*-dbg-*/Lg.dbg("do plugin #%d",i);
                        MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                        /*-dbg-*/Lg.dbg("load plugin");
                        IMdtRemoveAttachment remplug = (IMdtRemoveAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                        /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                        remplug.remove(formdoc, attachment, crconfig, (Map)plugin.context);
                        /*-dbg-*/Lg.dbg("done");
                    }
                }
                /*-dbg-*/Lg.dbg("try to locate name %s",name);
                for (int i=0; i < formdoc.getValueCount("m_attachments"); i++) {
                    if (name.equals(formdoc.getRepeatingString("m_attachments", i))) {
                        /*-dbg-*/Lg.dbg("found at index %d, removing",i);
                        formdoc.remove("m_attachments", i);
                    }
                }
                // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
                //String chronid = attachment.getChronicleId().getId();
                //for (int i=0; i < formdoc.getValueCount("m_attachments_chids"); i++) {
                //    if (chronid.equals(formdoc.getRepeatingString("m_attachment_chids", i))) {
                //        formdoc.remove("m_attachments_chids", i);
                //    }
                //}                
            }
            /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
            formdoc.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("removeAttachmentFromForm : Exception Occurred ",e);
            throw EEx.create("removeAttachmentFromForm-Error","removeAttachmentFromForm : Exception Occurred ",e);            
        }
        
    }

    
    public static void removeAllDocumentsFromDocumentAttribute(IDfSysObject formdoc, String docattr, String removepluginkey)
    {
        /*-dbg-*/Lg.dbg("removingall attachments from %s",formdoc);
        try { 
            String mdtapp = formdoc.getString("m_application");
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            String formtype = formdoc.getTypeName();
            /*-dbg-*/Lg.dbg("getting CR Config for formtype: %s",formtype);
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("cr config is null? %b",crconfig==null);
            
            // get list of attachment names from formdoc
            /*-dbg-*/Lg.dbg("get list of attachment names");
            List l = new ArrayList();
            for (int a=0; a < formdoc.getValueCount(docattr); a++) {
                l.add(formdoc.getRepeatingString(docattr,a));
            }
            
            /*-dbg-*/Lg.dbg("iterate on m_attachments");
            for (int att=0; att < l.size(); att++)
            {
                /*-dbg-*/Lg.dbg("get next name to remove");
                String name = (String)l.get(att);
                /*-dbg-*/Lg.dbg("lookup object named %s",name);
                IDfSysObject attachment = lookupAttachmentByName(formdoc, name);
                // check for "AttachmentRemovePlugins"
                List attachmentplugins = (List)crconfig.get(removepluginkey);
                /*-dbg-*/Lg.dbg("has remove attachment plugins (%s key) in cr? %b",removepluginkey,attachmentplugins==null);
                if (attachmentplugins != null) {
                    for (int i=0; i < attachmentplugins.size(); i++) {
                        /*-dbg-*/Lg.dbg("do plugin #%d",i);
                        MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                        /*-dbg-*/Lg.dbg("load plugin");
                        IMdtRemoveAttachment remplug = (IMdtRemoveAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                        /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                        remplug.remove(formdoc, attachment, crconfig, (Map)plugin.context);
                        /*-dbg-*/Lg.dbg("done");
                    }
                }
                /*-dbg-*/Lg.dbg("try to locate name %s",name);
                for (int i=0; i < formdoc.getValueCount(docattr); i++) {
                    if (name.equals(formdoc.getRepeatingString(docattr, i))) {
                        /*-dbg-*/Lg.dbg("found at index %d, removing",i);
                        formdoc.remove(docattr, i);
                    }
                }
                // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
                //String chronid = attachment.getChronicleId().getId();
                //for (int i=0; i < formdoc.getValueCount("m_attachments_chids"); i++) {
                //    if (chronid.equals(formdoc.getRepeatingString("m_attachment_chids", i))) {
                //        formdoc.remove("m_attachments_chids", i);
                //    }
                //}                
            }
            /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
            formdoc.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("removeAttachmentFromForm : Exception Occurred ",e);
            throw EEx.create("removeAttachmentFromForm-Error","removeAttachmentFromForm : Exception Occurred ",e);            
        }
        
    }    
    
    public static void removeAttachmentFromForm(IDfSysObject formdoc, String attachmentid)
    {
        /*-dbg-*/Lg.dbg("removing %s from %s",attachmentid,formdoc);
        try { 
            String mdtapp = formdoc.getString("m_application");
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            String formtype = formdoc.getTypeName();
            /*-dbg-*/Lg.dbg("getting CR Config for formtype: %s",formtype);
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("get attachment, was cr null? %b",crconfig==null);
            IDfSysObject attachment = (IDfSysObject)formdoc.getSession().getObject(new DfId(attachmentid));
            // check for "AttachmentRemovePlugins"
            List attachmentplugins = (List)crconfig.get("AttachmentRemovalPlugins");
            /*-dbg-*/Lg.dbg("has add attachment plugins (AttachmentAddPlugins key) in cr? %b",attachmentplugins==null);
            if (attachmentplugins != null) {
                for (int i=0; i < attachmentplugins.size(); i++) {
                    /*-dbg-*/Lg.dbg("do plugin #%d",i);
                    MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                    /*-dbg-*/Lg.dbg("load plugin");
                    IMdtRemoveAttachment remplug = (IMdtRemoveAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                    /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                    remplug.remove(formdoc, attachment, crconfig, (Map)plugin.context);
                    /*-dbg-*/Lg.dbg("done");
                }
            }
            /*-dbg-*/Lg.dbg("get attachment name so we can remove it from the form");
            String objectname = attachment.getObjectName();
            /*-dbg-*/Lg.dbg("try to locate name %s",objectname);
            for (int i=0; i < formdoc.getValueCount("m_attachments"); i++) {
                if (objectname.equals(formdoc.getRepeatingString("m_attachments", i))) {
                    /*-dbg-*/Lg.dbg("found at index %d, removing",i);
                    formdoc.remove("m_attachments", i);
                }
            }
            // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
            //String chronid = attachment.getChronicleId().getId();
            //for (int i=0; i < formdoc.getValueCount("m_attachments_chids"); i++) {
            //    if (chronid.equals(formdoc.getRepeatingString("m_attachment_chids", i))) {
            //        formdoc.remove("m_attachments_chids", i);
            //    }
            //}
            /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
            formdoc.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("removeAttachmentFromForm : Exception Occurred ",e);
            throw EEx.create("removeAttachmentFromForm-Error","removeAttachmentFromForm : Exception Occurred ",e);            
        }
        
    }

    public static void removeDocumentFromDocumentAttribute(IDfSysObject formdoc, String attachmentid, String docattr, String removepluginkey)
    {
        /*-dbg-*/Lg.dbg("removing %s from %s",attachmentid,formdoc);
        try { 
            String mdtapp = formdoc.getString("m_application");
            /*-dbg-*/Lg.dbg("mdtapp: %s",mdtapp);
            String formtype = formdoc.getTypeName();
            /*-dbg-*/Lg.dbg("getting CR Config for formtype: %s",formtype);
            Map crconfig = WorkflowUtils.getChangeRequestConfig(formdoc.getSessionManager(), formdoc.getSession().getDocbaseName(), mdtapp, formdoc);
            /*-dbg-*/Lg.dbg("get attachment, was cr null? %b",crconfig==null);
            IDfSysObject attachment = (IDfSysObject)formdoc.getSession().getObject(new DfId(attachmentid));
            // check for "AttachmentRemovePlugins"
            List attachmentplugins = (List)crconfig.get(removepluginkey);
            /*-dbg-*/Lg.dbg("has add attachment plugins (%s key) in cr? %b",removepluginkey,attachmentplugins==null);
            if (attachmentplugins != null) {
                for (int i=0; i < attachmentplugins.size(); i++) {
                    /*-dbg-*/Lg.dbg("do plugin #%d",i);
                    MdtPlugin plugin = (MdtPlugin)attachmentplugins.get(i);
                    /*-dbg-*/Lg.dbg("load plugin");
                    IMdtRemoveAttachment remplug = (IMdtRemoveAttachment)MdtPluginLoader.loadPlugin(plugin, formdoc.getSessionManager());
                    /*-dbg-*/Lg.dbg("executing %s",plugin.classname);
                    remplug.remove(formdoc, attachment, crconfig, (Map)plugin.context);
                    /*-dbg-*/Lg.dbg("done");
                }
            }
            /*-dbg-*/Lg.dbg("get attachment name so we can remove it from the form");
            String objectname = attachment.getObjectName();
            /*-dbg-*/Lg.dbg("try to locate name %s",objectname);
            for (int i=0; i < formdoc.getValueCount(docattr); i++) {
                if (objectname.equals(formdoc.getRepeatingString(docattr, i))) {
                    /*-dbg-*/Lg.dbg("found at index %d, removing",i);
                    formdoc.remove(docattr, i);
                }
            }
            // TODO: just to be safe, we should add this property (do it when we have time to risk cache-purging and bouncing with the docbase and webapps)
            //String chronid = attachment.getChronicleId().getId();
            //for (int i=0; i < formdoc.getValueCount("m_attachments_chids"); i++) {
            //    if (chronid.equals(formdoc.getRepeatingString("m_attachment_chids", i))) {
            //        formdoc.remove("m_attachments_chids", i);
            //    }
            //}
            /*-dbg-*/Lg.dbg("saving/persisting formdoc changes");
            formdoc.save();
            /*-dbg-*/Lg.dbg("done");
        } catch (Exception e) {
            /*-ERROR-*/Lg.err("removeAttachmentFromForm : Exception Occurred ",e);
            throw EEx.create("removeAttachmentFromForm-Error","removeAttachmentFromForm : Exception Occurred ",e);            
        }
        
    }    
    
    // attachment screen custom column/query stuff...

}
