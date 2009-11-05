package com.cem.dctm;


import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


import com.cem.base.Lg;
import com.cem.lweb.core.util.ResultSetList;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;

public class DctmUtils 
{
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
        
    public static IDfValue execSingleValueQuery(IDfSession session, String dql) throws Exception
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
    
    public static List execQuery(IDfSession session, String dql) throws Exception
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

    public static List execSingleColumnQuery(IDfSession session, String dql) throws Exception
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
    
}
