package com.zoder.processors.exporting;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.client.IDfType;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;

public class GetMetaData extends AbstractVersionProcessor
{
    public static Set sysattrs = null;
    public void process(Map script, Context context) throws Exception
    {
        super.process(script, context);
    }

    public void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception
    {        
        document.put("MetaData", new HashMap());
        super.processItem(access, script, context, document, idx);
    }
    
    public static synchronized void initSysAttrs(IDfType type) throws Exception
    {
        sysattrs = new HashSet();
        for (int i=0; i < type.getTypeAttrCount(); i++) {
            String name = type.getTypeAttrNameAt(i);
            sysattrs.add(name);
        }
        
    }

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception
    {
        boolean appattrsdefault = context.containsKey("ExportAllApplicationAttributes");
        Map attrconfig = (Map)context.get("Attributes");
        Map metadataAllVersions = (Map)document.get("MetaData");
        Map metadata = new HashMap();
        
        version.put("MetaData",metadata);
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            metadataAllVersions.put(so.getObjectId().getId(), metadata);
            IDfType type = session.getType("dm_sysobject");
            
            if (sysattrs == null) 
                initSysAttrs(type);
            
            
            // here we go, main value export
            for (int i=0; i < so.getAttrCount(); i++) {
                IDfAttr attr = so.getAttr(i);
                String name = attr.getName();
                
                boolean export = false;
                
                // check if it is in the dm_sysobject type (aka is not an application/custom attr)
                if (!sysattrs.contains(name)) {
                    if (appattrsdefault) {
                        // it's an application attribute -- include by default
                        export = true;
                    } else {
                        export = attrconfig.containsKey(name);
                    }
                } else {
                    export = attrconfig.containsKey(name);
                }
                
                if (export) {
                    // implement later... TODO
                    if (attrconfig.get(name) != null) {
                        // detect remappers/attr processors
                    } else {
                        metadata.put(name, serializeAttribute(version,name,attr,so));
                    }
                }
            }
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
        
        
    }    
    
    public static Object serializeAttribute(Map version, String name, IDfAttr attr, IDfSysObject docobj) throws Exception
    {
        if (attr.isRepeating()) {
            List valuelist = new ArrayList();
            for (int i=0; i < docobj.getValueCount(name); i++) {
                IDfValue val = docobj.getValueAt(i);
                switch (attr.getDataType()) {
                    case IDfAttr.DM_BOOLEAN:
                        Boolean b = val.asBoolean();
                        valuelist.add(b);
                        break;
                    case IDfAttr.DM_DOUBLE:
                        Double d = val.asDouble();
                        valuelist.add(d);
                        break;
                    case IDfAttr.DM_ID:
                        String s = val.asId().getId();
                        valuelist.add(s);
                        break;
                    case IDfAttr.DM_INTEGER:
                        Integer integer = val.asInteger();
                        valuelist.add(integer);
                        break;
                    case IDfAttr.DM_STRING:
                        String str = val.asString();
                        valuelist.add(str);                        
                        break;
                    case IDfAttr.DM_TIME:
                        Date date = val.asTime().getDate();
                        valuelist.add(date);
                        break;
                    case IDfAttr.DM_UNDEFINED:
                        String undef = val.asString();
                        valuelist.add(undef);
                        break;
                    default:
                        String def = val.asString();
                        valuelist.add(def);
                        break;
                }
            }
            return valuelist;
        } else {
            IDfValue val = docobj.getValue(name);
            switch (attr.getDataType()) {
                case IDfAttr.DM_BOOLEAN:
                    Boolean b = val.asBoolean();
                    return b;
                case IDfAttr.DM_DOUBLE:
                    Double d = val.asDouble();
                    return d;
                case IDfAttr.DM_ID:
                    String s = val.asId().getId();
                    return s;
                case IDfAttr.DM_INTEGER:
                    Integer integer = val.asInteger();
                    return integer;
                case IDfAttr.DM_STRING:
                    String str = val.asString();
                    return str;                        
                case IDfAttr.DM_TIME:
                    Date date = val.asTime().getDate();
                    return date;
                case IDfAttr.DM_UNDEFINED:
                    String undef = val.asString();
                    return undef;
                default:
                    String def = val.asString();
                    return def;
            }
        }
    }
}
