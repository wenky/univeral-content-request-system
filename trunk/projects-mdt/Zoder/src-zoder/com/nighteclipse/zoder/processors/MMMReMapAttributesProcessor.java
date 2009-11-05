package com.nighteclipse.zoder.processors;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.documentum.fc.common.IDfValue;
import com.nighteclipse.zoder.util.DOMUtils;
import com.nighteclipse.zoder.util.ZuluDateFormatter;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;
import com.zoder.util.Lg;

public class MMMReMapAttributesProcessor extends AbstractVersionProcessor
{

    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            
            populateAttrs(version,so,context);
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
    }
    
    void populateAttrs(Map version, IDfSysObject so, Context context) throws Exception
    {
        Element curobj = (Element)version.get("DOM");
        // check config for attrs to map
        Node attrListElement = DOMUtils.getmake(curobj,"attributeList");
        Map attributes = (Map)context.get("Attributes");
        //List attrlist = attributeList.getList("attribute");
        Iterator i = attributes.keySet().iterator();
        while (i.hasNext())
        {
            /*-log-*/Lg.log("map next attr");
            String dctmattr = (String)i.next();
            String emattr = (String)attributes.get(dctmattr);
            /*-log-*/Lg.log("map "+dctmattr+" to "+emattr);
            Node attrElement = DOMUtils.newElement(attrListElement, "emattr");
            attrListElement.appendChild(attrElement);
            
            DOMUtils.put(attrElement, "name",emattr);
            IDfValue v = so.getValue(dctmattr);
            switch (v.getDataType()) {
            case IDfValue.DF_BOOLEAN :
                DOMUtils.put(attrElement, "string", ""+v.asBoolean()); break;
            case IDfValue.DF_DOUBLE :
                DOMUtils.put(attrElement, "string", ""+v.asDouble()); break;
            case IDfValue.DF_ID :
                DOMUtils.put(attrElement, "string", v.asId().getId()); break;
            case IDfValue.DF_INTEGER :
                DOMUtils.put(attrElement, "string", ""+v.asInteger()); break;
            case IDfValue.DF_STRING :
                DOMUtils.put(attrElement, "string", v.asString()); break;
            case IDfValue.DF_TIME :
                DOMUtils.put(attrElement, "string", ZuluDateFormatter.formatDate(v.asTime().getDate())); break;
            }
        }
    }
    

}
