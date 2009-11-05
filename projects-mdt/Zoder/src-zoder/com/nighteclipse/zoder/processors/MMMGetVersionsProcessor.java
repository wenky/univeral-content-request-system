package com.nighteclipse.zoder.processors;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.IDfId;
import com.nighteclipse.zoder.util.ZuluDateFormatter;
import com.nighteclipse.zoder.util.DOMUtils;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractDocumentProcessor;
import com.zoder.util.Context;

public class MMMGetVersionsProcessor extends AbstractDocumentProcessor
{

    public void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            
            // create DOM Document object
            DocumentBuilderFactory documentBuilderFactory = DocumentBuilderFactory.newInstance();
            DocumentBuilder documentBuilder = documentBuilderFactory.newDocumentBuilder();
            Document domdoc = documentBuilder.newDocument();
            String rootname = (String)context.get("RootTag");
            Element rootElement = domdoc.createElement(rootname);
            domdoc.appendChild(rootElement);
            
            Node creationpropsentity = DOMUtils.newElement(domdoc,"creationProperties");
            DOMUtils.put(creationpropsentity, "release", (String)context.get("CreationPropertiesRelease"));
            DOMUtils.put(creationpropsentity, "datetime", context.containsKey("CreationPropertiesDatetime")?(String)context.get("CreationPropertiesDatetime"):ZuluDateFormatter.formatDate(new Date()));
            DOMUtils.put(creationpropsentity, "event", (String)context.get("CreationPropertiesEvent"));
            DOMUtils.put(creationpropsentity, "dtdInfo", "&ematrixProductDtd;");
            rootElement.appendChild(creationpropsentity);

            
            Map statefilter = (Map)context.get("ValidStates");            
            String chronid = (String)document.get("ChronicleId");
            IDfSysObject curdoc = (IDfSysObject)session.getObjectByQualification("dm_sysobject where i_chronicle_id = '"+chronid+"'");
            
            List versionlist = new ArrayList();
            
            while (true) {
                Element verrootElement = domdoc.createElement("businessObject");
                rootElement.appendChild(verrootElement);
                Map version = new HashMap();
                version.put("r_object_id", curdoc.getObjectId().getId());
                version.put("DOM", verrootElement);
                
                if (statefilter != null) {
                    if (statefilter.containsKey(curdoc.getCurrentStateName())) {
                        versionlist.add(0,version);                        
                    }
                } else {                
                    versionlist.add(0,version);                        
                }
                                
                if (curdoc.getObjectId().getId().equals(chronid)) {
                    // end loop, no more previous versions
                    break;
                } else {
                    IDfId antecedent = curdoc.getAntecedentId();
                    curdoc = (IDfSysObject)session.getObject(antecedent);
                }
                
            }            
            document.put("Versions", versionlist);
            document.put("DOM", domdoc);
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
        
    }

}
