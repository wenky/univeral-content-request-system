package com.medtronic.ecm.documentum.util;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfPersistentObject;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfVersionPolicy;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfValue;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.util.ResultSetList;

// Pure DFC helper functions and procedures

public class DctmUtils 
{
    static DfClientX clientx = new DfClientX();     
    
    public static String getValueAsString(IDfValue val) {
        if (val == null) {
            throw new NullPointerException("getValueAsString - IDfValue input was null");
        }
        switch (val.getDataType())
        {
            case IDfAttr.DM_BOOLEAN:
                Boolean b = val.asBoolean();
                return b.toString();
            case IDfAttr.DM_DOUBLE:
                Double d = val.asDouble();
                return d.toString();
            case IDfAttr.DM_ID:
                String sid = val.asId().getId();
                return sid;
            case IDfAttr.DM_INTEGER:
                Integer integer = val.asInteger();
                return integer.toString();
            case IDfAttr.DM_STRING:
                String str = val.asString();
                return str;                        
            case IDfAttr.DM_TIME:
                Date date = val.asTime().getDate();
                return date.toGMTString();
            case IDfAttr.DM_UNDEFINED:
                String undef = val.asString();
                return undef;
            default:
                String def = val.asString();
                return def;
        }
    }
    
    public static String getVersionNumber(IDfSysObject so) throws Exception 
    {
        int labelcount = so.getVersionLabelCount();
        for (int i=0; i < labelcount; i++) {
            String label = so.getVersionLabel(i);
            if (Character.isDigit(label.charAt(0))) {
                // assume float
                int peridx = label.indexOf('.');
                if (peridx != -1 && peridx != label.lastIndexOf('.')) {
                    // branch or multiple detected
                    /*-dbg-*/Lg.dbg("bad label found: %s",label);                    
                }
                double version = -1.0;
                try {
                    version = Double.parseDouble(label);
                    /*-dbg-*/Lg.dbg("returning version number: %s",label);                                        
                    return label;
                } catch (NumberFormatException nfe) {
                    /*-dbg-*/Lg.dbg("label failed numberformatting: %s",label);                    
                }
            }
        }
        return "-1.-1";
    }
        
    public static IDfValue execSingleValueQuery(IDfSession session, String dql) throws DfException
    {
        IDfCollection c = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            c = query.execute(session, query.DF_READ_QUERY);
            if (c.next()) {
                IDfValue retval = c.getValueAt(0);
                return retval;
            }
        } finally {
            try {c.close();}catch(Exception e){}
        }
        return null;
    }
    
    public static List execQuery(IDfSession session, String dql) throws DfException
    {
        IDfCollection c = null;
        List results = new ArrayList();
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            c = query.execute(session, query.DF_READ_QUERY);
            while (c.next()) {
                Map result = new HashMap(c.getAttrCount());
                for (int i=0; i < c.getAttrCount(); i++)
                {
                    IDfAttr attr = c.getAttr(i);
                    IDfValue value = c.getValueAt(i);
                    switch (attr.getDataType()) { 
                        case IDfAttr.DM_TIME :
                            result.put(attr.getName(),value.asTime().getDate());
                            break;
                        case IDfAttr.DM_INTEGER :
                            result.put(attr.getName(),value.asInteger());
                            break;
                        case IDfAttr.DM_DOUBLE:
                            result.put(attr.getName(),value.asDouble());
                            break;
                        default : 
                            result.put(attr.getName(),value.asString());
                            break;
                    }
                }
                results.add(result);
            }
        } finally {
            try {c.close();}catch(Exception e){}
        }
        return results;
        
    }

    public static List execSingleColumnQuery(IDfSession session, String dql) throws DfException
    {
        IDfCollection c = null;
        List results = new ArrayList();
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            c = query.execute(session, query.DF_READ_QUERY);
            while (c.next()) {
                IDfAttr attr = c.getAttr(0);
                IDfValue value = c.getValueAt(0);
                switch (attr.getDataType()) { 
                    case IDfAttr.DM_TIME :
                        results.add(value.asTime().getDate());
                        break;
                    case IDfAttr.DM_INTEGER :
                        results.add(value.asInteger());
                        break;
                    case IDfAttr.DM_DOUBLE :
                        results.add(value.asDouble());
                        break;
                    case IDfAttr.DM_ID :
                        results.add(value.asId().getId());
                        break;
                    case IDfAttr.DM_STRING :
                        results.add(value.asString());
                        break;
                    case IDfAttr.DM_BOOLEAN :
                        results.add(value.asBoolean());
                        break;
                    default : 
                        results.add(value.asString());
                        break;
                }
            }
        } finally {
            try {c.close();}catch(Exception e){}
        }
        return results;
        
    }

    public static List execMultiColumnQuery(IDfSession session, String dql) throws Exception
    {
        IDfCollection c = null;
        List results = new ArrayList();
        Map columns = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            c = query.execute(session, query.DF_READ_QUERY);
            while (c.next()) {
                if (columns == null) {
                    columns = new HashMap();
                    for (int j=0; j < c.getAttrCount(); j++) {
                        IDfAttr attr = c.getAttr(j);
                        columns.put(attr.getName(), j);
                    }
                }
                int attrcount = c.getAttrCount();
                Object[] datarow = new Object[attrcount];
                for (int i=0; i < attrcount; i++) {
                    IDfAttr attr = c.getAttr(i);
                    IDfValue value = c.getValueAt(i);
                    switch (attr.getDataType()) { 
                        case IDfAttr.DM_TIME :
                            datarow[i] = value.asTime().getDate();
                            break;
                        case IDfAttr.DM_INTEGER :
                            datarow[i] = value.asInteger();
                            break;
                        case IDfAttr.DM_DOUBLE:
                            datarow[i] = value.asDouble();
                            break;
                        case IDfAttr.DM_ID :
                            datarow[i] = value.asId().getId();
                            break;
                        case IDfAttr.DM_STRING :
                            datarow[i] = value.asString();
                            break;
                        case IDfAttr.DM_BOOLEAN :
                            datarow[i] = value.asBoolean();
                            break;
                        default : 
                            datarow[i] = value.asString();
                            break;
                    }
                }
                results.add(datarow);
            }
        } finally {
            try {c.close();}catch(Exception e){}
        }
        return new ResultSetList(results, columns);
        
    }
    
    public static String getObjectSummary(IDfSysObject so) throws Exception {
        String objsum = '['+so.getObjectName()+']'+'['+getVersionNumber(so)+']'+"[chronid:"+so.getChronicleId().getId()+']'+"[CURID:"+so.getObjectId().getId()+']';
        return objsum;
    }
    
    public static boolean verifyPermissionsOnAllVersions(IDfSession session, IDfSysObject so, int permitlevel) throws Exception
    {
        IDfSysObject curobj = so;
        while (true) {
            if (curobj.getPermit() < permitlevel) 
                return false;
            if (curobj.getChronicleId().equals(curobj.getObjectId()))
                break;
            curobj = (IDfSysObject)session.getObject(curobj.getAntecedentId());
        }
        return true;        
    }

    public static boolean verifyNotLockedOnAllVersions(IDfSession session, IDfSysObject so, int permitlevel) throws Exception
    {
        IDfSysObject curobj = so;
        while (true) {
            if (curobj.isCheckedOut()) 
                return false;
            if (curobj.getChronicleId().equals(curobj.getObjectId()))
                break;
            curobj = (IDfSysObject)session.getObject(curobj.getAntecedentId());
        }
        return true;        
    }

    public static void unlockAllVersions(IDfSession session, IDfSysObject so, int permitlevel) throws Exception
    {
        IDfSysObject curobj = so;
        while (true) {
            if (curobj.isCheckedOut()) {
                curobj.cancelCheckout();  //do we need to save?                
            }
            if (curobj.getChronicleId().equals(curobj.getObjectId()))
                break;
            curobj = (IDfSysObject)session.getObject(curobj.getAntecedentId());
        }
    }

    // make entire version tree mutable
    public static void makeAllVersionsMutable(IDfSession session, IDfSysObject so) throws Exception
    {
        IDfSysObject curobj = so;
        while (true) {
            if (so.isImmutable()) {
                so.setBoolean("r_immutable_flag", false);
                so.save();
            }
            if (curobj.getChronicleId().equals(curobj.getObjectId()))
                break;
            curobj = (IDfSysObject)session.getObject(curobj.getAntecedentId());
        }
    }

    // make all previous versions immutable, since when you're locking, 
    // you don't want to make the current version immutable
    public static void makeAllPreviousVersionsImmutable(IDfSession session, IDfSysObject so) throws Exception
    {
        if (so.getChronicleId().equals(so.getObjectId())) return;        
        IDfSysObject curobj = so;
        while (true) {
            curobj = (IDfSysObject)session.getObject(curobj.getAntecedentId());            
            if (!so.isImmutable()) {
                so.setBoolean("r_immutable_flag", true);
                so.save();
            }
            if (curobj.getChronicleId().equals(curobj.getObjectId()))
                break;
        }
    }

    public static boolean isCurrent(IDfSysObject so) throws Exception 
    {
        for (int i=0; i < so.getVersionLabelCount(); i++) {
            String label = so.getVersionLabel(i);
            if ("CURRENT".equalsIgnoreCase(label))
                return true;
        }
        return false;
    }
    
    public static String loadFileContents(IDfSession session, String filepath) throws DfException
    {
        String thedoc = null;
        /*-dbg-*/Lg.wrn("get sysobj");
        IDfSysObject so = (IDfSysObject)session.getObjectByPath(filepath);
        /*-dbg-*/Lg.wrn("retrieved %s, get BAIS content",so);
        // view the content (assume it's text or xml)
        ByteArrayInputStream bais = so.getContent();
        
        // need this for stream conversion
        /*-dbg-*/Lg.wrn("doing DfClientX bais helper method");
        if (bais.available() > 0)
        {
            thedoc = clientx.ByteArrayInputStreamToString(bais);
            // ... apparently, logger has some key subbing it does on its own that will kill things ...
            ///*-trc-*/MdtLog.trc("returning %s",thedoc);
        }        
        /*-dbg-*/Lg.wrn("returning");
        return thedoc;
    }

    public static String loadFileContents(IDfSession session, IDfId objectid) throws DfException
    {
        String thedoc = null;
        /*-dbg-*/Lg.wrn("get sysobj");
        IDfSysObject so = (IDfSysObject)session.getObject(objectid);
        /*-dbg-*/Lg.wrn("retrieved %s, get BAIS content",so);
        // view the content (assume it's text or xml)
        ByteArrayInputStream bais = so.getContent();
        
        // need this for stream conversion
        /*-dbg-*/Lg.wrn("doing DfClientX bais helper method");
        if (bais.available() > 0)
        {
            thedoc = clientx.ByteArrayInputStreamToString(bais);
            // ... apparently, logger has some key subbing it does on its own that will kill things ...
            ///*-trc-*/MdtLog.trc("returning %s",thedoc);
        }        
        /*-dbg-*/Lg.wrn("returning");
        return thedoc;
    }

    public static void setTextContent(IDfSysObject sysobject, String text, String format) throws DfException 
    {
        /*-dbg-*/Lg.wrn("convert velocity substitution result to an output stream");
        ByteArrayOutputStream contentstream = clientx.StringToByteArrayOutputStream(text);            
        // attach result to the form object
        /*-dbg-*/Lg.wrn("set the content stream on the formobj %s",sysobject);
        sysobject.setContentEx(contentstream,format,0);
        /*-dbg-*/Lg.wrn("save the formobject");
        sysobject.save();
        
    }

    public static void makeRenditionRequest(IDfSysObject document, String format) throws DfException
    {
        /*-dbg-*/Lg.dbg("makeRenditionRequest - top");            
        // check if there is a rendition present already
        boolean hasrendition = false;
        IDfCollection myColl = null;
        /*-dbg-*/Lg.dbg("check if a rendition is already there...");            
        try {
            myColl = document.getRenditions("full_format" );
            while ( myColl.next() ) {
                String currentformat = myColl.getString("full_format");
                if (currentformat != null && currentformat.equals(format))
                {
                    // get the id of the current rendition
                    /*-dbg-*/Lg.dbg("rendition already present");            
                    hasrendition = true;                        
                }
            }
            myColl.close(); myColl = null;
        } finally {
            if(myColl != null)myColl.close();
        }
        
        if (!hasrendition) {
            // get current format (handle acro)
            /*-dbg-*/Lg.dbg("rendition needed, check for acro");            
            if ("acro".equals(document.getFormat().getName()))
            {
                /*-dbg-*/Lg.dbg("acro format: getfile, setfile");            
                // copy acro source as pdf format rendition
                String filename = document.getFile(null);
                document.addRendition(filename,"pdf");
                /*-dbg-*/Lg.dbg("done with acro pdf generation");            
            } else {
                /*-dbg-*/Lg.dbg("queue rendition request");            
                // TODO: other rendition formats?
                document.queue("dm_autorender_win31", "rendition", 0, false, null, "rendition_req_ps_pdf");
                /*-dbg-*/Lg.dbg("rendition request queued");            
            }
        }
    }
    
    public static boolean hasRendition(IDfSession session, String docid, String format) throws DfException
    {
        /*-dbg-*/Lg.dbg("hasRendition - top");            
        boolean flag = false;
        IDfCollection idfcollection = null;
        try { 
            /*-dbg-*/Lg.dbg("look up formats");            
            DfQuery dfquery = new DfQuery();
            String dql = "select r_object_id from dmr_content where any parent_id='" + docid + "' and full_format='" + format + "'";
            /*-dbg-*/Lg.dbg("  -- dql: "+dql);            
            dfquery.setDQL(dql);
            /*-dbg-*/Lg.dbg("exec dql");            
            idfcollection = dfquery.execute(session, 0);
            /*-dbg-*/Lg.dbg("next result...");            
            flag = idfcollection.next();
            /*-dbg-*/Lg.dbg("rendition found? "+flag);            
        } finally {
            try {idfcollection.close();}catch(Exception e){}
        }
        /*-dbg-*/Lg.dbg("returning "+flag);            
        return flag;

    }
    
    public static IDfSysObject versionDocument(IDfSysObject attachment, String versiontype, boolean preserverendition) throws DfException
    {
        IDfId _newObjId = null;
        String attachmentformat = null;
        /*-dbg-*/Lg.dbg("get current log_entry");
        String logentry = attachment.getString("log_entry");
        /*-dbg-*/Lg.dbg("- logentry: %s",logentry);
        
        
        String renditionfile = null;
        if (preserverendition) {
            attachmentformat = attachment.getFormat().getName();
            /*-dbg-*/Lg.dbg("check if we need to preserve rendition");
            if (!"pdf".equals(attachmentformat))
            {             
                /*-dbg-*/Lg.dbg("caching rendition");
                renditionfile = attachment.getFileEx(null,"pdf",0,false);             
                /*-dbg-*/Lg.dbg("  -rendfile: %s",renditionfile);
            }
        }

        /*-dbg-*/Lg.dbg("If nextVersion ");            
        String verLabel = null;
        IDfVersionPolicy verPolicy = attachment.getVersionPolicy();
        if (versiontype.equalsIgnoreCase("MAJOR")) {
            verLabel = verPolicy.getNextMajorLabel();
            /*-dbg-*/Lg.dbg("Next MAJOR version %s",verLabel);
            if (!attachment.isCheckedOut())
                attachment.checkout();
            attachment.mark("CURRENT");
            _newObjId = attachment.checkin(false, verLabel);
        } else if (versiontype.equalsIgnoreCase("MINOR")) {
            verLabel = verPolicy.getNextMinorLabel();
            /*-dbg-*/Lg.dbg("Next MINOR version %s",verLabel);
            if (!attachment.isCheckedOut())
                attachment.checkout();
            attachment.mark("CURRENT");
            _newObjId = attachment.checkin(false, verLabel);
        } else if (versiontype.equalsIgnoreCase("BRANCH")) {
            verLabel = verPolicy.getBranchLabel();
            /*-dbg-*/Lg.dbg("Next BRANCH version %s",verLabel);
            if (!attachment.isCheckedOut())
                attachment.checkout();
            attachment.mark("CURRENT");
            _newObjId = attachment.checkin(false, verLabel);
        } else if (versiontype.equalsIgnoreCase("SAME")) {
            verLabel = verPolicy.getSameLabel();
            /*-dbg-*/Lg.dbg("Next SAME version %s",verLabel);
            attachment.mark("CURRENT");
            attachment.save();
            _newObjId = attachment.getObjectId();
        }
        /*-dbg-*/Lg.dbg("get new doc so we can preserve the logentry");
        IDfSysObject newdoc = (IDfSysObject)attachment.getSession().getObject(_newObjId);
        /*-dbg-*/Lg.dbg("setting logentry");
        newdoc.setString("log_entry",logentry);
        
        if (preserverendition) {
        /*-dbg-*/Lg.dbg("check if we need to preserve rendition");
            if (!"pdf".equals(attachmentformat) && renditionfile != null)
            {
                /*-dbg-*/Lg.dbg("preserving rendition");
              newdoc.addRendition(renditionfile,"pdf");
            }
        }
        
        /*-dbg-*/Lg.dbg("saving");
        newdoc.save();
        /*-dbg-*/Lg.dbg("saved");
        
        return newdoc;
        
    }

    
    public static void clearAttribute(IDfPersistentObject obj, List attrlist) throws DfException
    {
        /*-dbg-*/Lg.dbg("iterate through list of attributes");
        for (int i=0; i < attrlist.size(); i++)
        {
            /*-dbg-*/Lg.dbg("get next attr");
            String attrname = (String)attrlist.get(i);
            /*-dbg-*/Lg.dbg("check if attr %s is repeating",attrname);
            if (obj.isAttrRepeating(attrname)) {
                /*-dbg-*/Lg.dbg("clear repeating values");
                obj.removeAll(attrname);
            } else {
                /*-dbg-*/Lg.dbg("set null");
                obj.setNull(attrname);
            }
        }
        /*-dbg-*/Lg.dbg("save changes");
        obj.save();
        
    }
}
