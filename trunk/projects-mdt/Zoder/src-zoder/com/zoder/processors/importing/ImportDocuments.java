package com.zoder.processors.importing;

import java.io.File;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.DfTime;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfTime;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractDocumentProcessor;
import com.zoder.util.Context;

public class ImportDocuments extends AbstractDocumentProcessor 
{

    public void processItem(DctmAccess access, Map script, Context context,Map document, int idx) throws Exception 
    {
        // get basedir
        String docdir = (String)document.get("DocumentPath");
        // get version list
        List versions = (List)document.get("Versions");
        // get oldid <--> newid folder mappings
        Map oldtonew = (Map)context.get("Folders.OldToNew");
        
        String newchronid = null;
        
        IDfSession session = null;
        try {
            session = access.getSession();
            //access.getCurrentSessionManager().beginTransaction();
            IDfTypedObject serverConfig = session.getServerConfig();
            String aclDomain = serverConfig.getString("operator_name");
            
            IDfSysObject chronobj = null;
            IDfSysObject lastver = null;
            IDfSysObject newver = null;
            
            // TODO: implement a sub-layer of plugins?
            for (int i=0; i < versions.size(); i++) {
                
                Map version = (Map)versions.get(i);
                Map refdata = (Map)version.get("ReferenceData");
                String objecttype = (String)refdata.get("Type");
                if (chronobj == null) {
                    chronobj = (IDfSysObject)session.newObject(objecttype);
                    newver = chronobj;
                    Map metadata = (Map)version.get("MetaData");
                    String name = (String)metadata.get("object_name");
                    chronobj.setObjectName(name);
                    newchronid = chronobj.getObjectId().getId();
                    document.put("New.ChronicleId", newchronid);
                    version.put("New.ObjectId", newchronid);
                } else {
                    lastver.checkout();
                    IDfId newverid = lastver.checkin(false, ""); //TODO: version labels like "publish"
                    newver = (IDfSysObject)session.getObject(newverid);
                    document.put("New.ObjectId", newverid.getId());                    
                }
                
                // overwrite implicit version label, link folders, set metadata/properties
                if (!context.containsKey("Skip.Version"))setVersionLabel(newver,version);
                if (!context.containsKey("Skip.FolderLink"))linkFolders(version, oldtonew, newver, context);
                if (!context.containsKey("Skip.Metadata"))setMetadata(version,document,newver,context);
                newver.save();                
                
                // TODO: acl, alias, lifecycles
                if (!context.containsKey("Skip.ACL"))setACL(session,version,document,newver,context,aclDomain);
                newver.save();
                //setLifecycle(session,version,document,newver,context);
                //newver.save();
                
                // set content...
                if (!context.containsKey("Skip.Content"))setContent(version,refdata,docdir,newver);
                if (!context.containsKey("Skip.Rendition"))setRendition(version,refdata,docdir,newver);
                
                lastver = newver;
            }
            
            //access.getCurrentSessionManager().commitTransaction();
        } catch (Exception e) {
            try {
                // attempt rollback
                IDfSysObject baseobj = (IDfSysObject)session.getObject(new DfId(newchronid));
                baseobj.destroyAllVersions();
            } catch (Exception ee) {
                // unable to rollback document
            }
            // rethrow to ADP for error processing
            throw e;            
        } finally {
            //try{if(access.getCurrentSessionManager().isTransactionActive()) access.getCurrentSessionManager().abortTransaction();}catch(Exception e) {}
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
    }

    public void setACL(IDfSession session, Map version, Map document, IDfSysObject newver, Context context, String acldomain) throws Exception
    {
        String aclname = (String)version.get("Target.ACL");
        if (aclname == null) {
            Map refdata = (Map)version.get("ReferenceData");
            aclname = (String)refdata.get("ACL");
        }
        
        if (aclname != null) {
            IDfACL acl = session.getACL(acldomain, aclname);
            newver.setACL(acl);
        }
        
    }

    public void setLifecycle(IDfSession session, Map version, Map document, IDfSysObject newver, Context context) throws Exception
    {
        String lcname = (String)version.get("Target.Lifecycle");
        if (lcname == null) {
            Map refdata = (Map)version.get("ReferenceData");
            lcname = (String)refdata.get("Lifecycle");
        }
        
        IDfSysObject so = (IDfSysObject)session.getObjectByQualification("dm_policy where object_name = '"+lcname+"'");

        String lcstate = (String)version.get("Target.LifecycleState");
        if (lcstate == null) {
            Map refdata = (Map)version.get("ReferenceData");
            lcstate = (String)refdata.get("LifecycleState");
        }
        
        // TODO: alias set to attach...is this == the doc's alias set?
        newver.attachPolicy(so.getObjectId(),lcstate,"");
    }
    
    public void setMetadata(Map version, Map document, IDfSysObject newver, Context context) throws Exception
    {
        Map metadata = (Map)version.get("MetaData");
        Iterator i = metadata.keySet().iterator();
        while (i.hasNext()) {
            String attrname = (String)i.next();
            if (newver.hasAttr(attrname)) {
                int attrindex = newver.findAttrIndex(attrname); 
                IDfAttr attr = newver.getAttr(attrindex);
                if (attr.isRepeating()) {
                    List valuelist = (List)metadata.get(attrname);
                    if (valuelist != null) {
                        for (int v=0; v < valuelist.size(); v++) {
                            switch (attr.getDataType()) {
                                case IDfAttr.DM_BOOLEAN:
                                    Boolean b = (Boolean)valuelist.get(v);
                                    newver.appendBoolean(attrname,b);
                                    break;
                                case IDfAttr.DM_DOUBLE:
                                    Double d = (Double)valuelist.get(v);;
                                    newver.appendDouble(attrname,d);
                                    break;
                                case IDfAttr.DM_ID:
                                    String s = (String)valuelist.get(v);
                                    IDfId id = new DfId(s);
                                    newver.appendId(attrname,id);
                                    break;
                                case IDfAttr.DM_INTEGER:
                                    Integer integer = (Integer)valuelist.get(v);
                                    newver.appendInt(attrname,integer);
                                    break;
                                case IDfAttr.DM_STRING:
                                    String str = (String)valuelist.get(v);
                                    newver.appendString(attrname,str);
                                    break;
                                case IDfAttr.DM_TIME:
                                    Date date = (Date)valuelist.get(v);
                                    IDfTime idfdate = new DfTime(date);
                                    newver.appendTime(attrname,idfdate);
                                    break;
                                case IDfAttr.DM_UNDEFINED:
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                } else {
                    switch (attr.getDataType()) {
                        case IDfAttr.DM_BOOLEAN:
                            Boolean b = (Boolean)metadata.get(attrname);
                            newver.setBoolean(attrname,b);
                            break;
                        case IDfAttr.DM_DOUBLE:
                            Double d = (Double)metadata.get(attrname);;
                            newver.setDouble(attrname,d);
                            break;
                        case IDfAttr.DM_ID:
                            String s = (String)metadata.get(attrname);
                            IDfId id = new DfId(s);
                            newver.setId(attrname,id);
                            break;
                        case IDfAttr.DM_INTEGER:
                            Integer integer = (Integer)metadata.get(attrname);
                            newver.setInt(attrname,integer);
                            break;
                        case IDfAttr.DM_STRING:
                            String str = (String)metadata.get(attrname);
                            newver.setString(attrname,str);
                            break;
                        case IDfAttr.DM_TIME:
                            Date date = (Date)metadata.get(attrname);
                            IDfTime idfdate = new DfTime(date);
                            newver.setTime(attrname,idfdate);
                            break;
                        case IDfAttr.DM_UNDEFINED:
                            break;
                        default:
                            break;
                    }
                }
            }
        }
    }

    public void setRendition(Map version, Map refdata, String docdir, IDfSysObject newver) throws Exception
    {
        String implicitversion = (String)version.get("ParsedVersion");                
        String oldobjectid = (String)version.get("ParsedObjectId");
        String formatname = (String)refdata.get("Format");
        String dosext = (String)refdata.get("Format-DOSEXT");
        if (dosext == null) dosext = formatname;
        
        // set rendition... TODO: make format-agnostic
        String renditionfile = docdir+"Rendition-"+implicitversion+'-'+oldobjectid+".pdf";
        if (!"pdf".equals(dosext)) {
            File rfile = new File(renditionfile);
            if (rfile.exists()) {
                newver.addRendition(renditionfile, "pdf");
            }
            newver.save();
        }
        
    }
    
    public void setContent(Map version, Map refdata, String docdir, IDfSysObject newver) throws Exception
    {
        //Content-1.0-09017f448007d64e.pdf
        String implicitversion = (String)version.get("ParsedVersion");                
        String oldobjectid = (String)version.get("ParsedObjectId");
        String formatname = (String)refdata.get("Format");
        String dosext = (String)refdata.get("Format-DOSEXT");
        if (dosext == null) dosext = formatname;
        String contentfile = docdir+"Content-"+implicitversion+'-'+oldobjectid+'.'+dosext;
        File cfile = new File(contentfile);
        if (cfile.exists()) {
            newver.setFileEx(contentfile,formatname,0,null);
        }
        newver.save();        
    }

    public static boolean isAlreadyLinked(IDfSysObject doc, String folderid) throws Exception {
        for (int i=0; i < doc.getFolderIdCount(); i++) {
            String curid = doc.getFolderId(i).getId();
            if (folderid.equals(curid))
                return true;
        }
        return false;
    }

    public static boolean isAlreadyLinkedToPath(IDfSysObject doc, String folderpath) throws Exception {
        IDfFolder folderobj = (IDfFolder)doc.getSession().getObjectByPath(folderpath);
        String folderid = folderobj.getObjectId().getId();
        for (int i=0; i < doc.getFolderIdCount(); i++) {
            String curid = doc.getFolderId(i).getId();
            if (folderid.equals(curid))
                return true;
        }
        return false;
    }

    public void setVersionLabel(IDfSysObject document, Map version) throws Exception {
        String newimplicitversion = (String)version.get("ParsedVersion");                
        int labelcount = document.getValueCount("r_version_label");
        for (int i=0; i < labelcount; i++) {
            String curlabel = document.getRepeatingString("r_version_label", i);
            // check if it matches an implicit label:
            if (curlabel.matches("[0-9]*\\.[0-9]*")) {
                document.setRepeatingString("r_version_label", i, newimplicitversion);
            }
        }
    }
    
    public void linkFolders(Map version, Map oldtonew, IDfSysObject newver, Context context) throws Exception 
    {
        // link folders (need old folder <--> new folder id map)
        boolean linked = false;
        // iterate through folder links, make sure they are linked
        if (version.containsKey("FolderLinks")) {
            List linklist = (List)version.get("FolderLinks");
            for (int ll=0; ll < linklist.size(); ll++) {
                String folderid = (String)linklist.get(ll);
                String newfolderid = (String)oldtonew.get(folderid);
newfolderid = "0c017f4480099cb9";                        
                if (isAlreadyLinked(newver,newfolderid)) {
                    linked = true;
                } else {                            
                    newver.link(newfolderid);
                    linked = true;
                }
            }
        } else if (version.containsKey("FolderPaths")) {
            List linklist = (List)version.get("FolderPaths");
            for (int ll=0; ll < linklist.size(); ll++) {
                String folderpath = (String)linklist.get(ll);
                if (isAlreadyLinkedToPath(newver,folderpath)) {
                    linked = true;
                } else {                            
                    newver.link(folderpath);
                    linked = true;
                }
            }
            
        }
        
    }


}
