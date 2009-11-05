package com.nighteclipse.zoder.processors;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.nighteclipse.zoder.util.DOMUtils;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;
import com.zoder.util.ResultSetList;

public class MMMMapCsvData extends AbstractVersionProcessor
{

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            String csvkey = (String)context.get("CsvKey");
            ResultSetList csvdata = (ResultSetList)context.get(csvkey);
            mapCsvData(version,so,context,csvdata);
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }                
    }
    
    public void mapCsvData(Map version,IDfSysObject so,Context context,ResultSetList csvdata) throws Exception 
    {
        Element curobj = (Element)version.get("DOM");
        Map columnconfig = (Map)context.get("Attributes");        
        
        int datarow = csvdata.indexOf(so.getObjectId().getId(), 0);
        
        if (datarow >= 0) {
            Node attributeList = DOMUtils.getmake(curobj, "attributeList");
            Iterator i = csvdata.columnNames().iterator();
            while (i.hasNext()) {
                String columnname = (String)i.next();
                if (columnconfig.containsKey(columnname)) {
                    Node attrElement = DOMUtils.newElement(attributeList, "attribute");
                    attributeList.appendChild(attrElement);
                    
                    if (columnconfig.get(columnname) == null) {
                        DOMUtils.put(attrElement,"name",columnname);
                    } else {
                        DOMUtils.put(attrElement,"name",(String)columnconfig.get(columnname));
                    }
                    DOMUtils.put(attrElement,"string",(String)csvdata.get(datarow, columnname));
                }
            }
        }
    }

}
