package com.nighteclipse.zoder.processors;

import java.util.Iterator;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;
import com.nighteclipse.zoder.util.DOMUtils;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractVersionProcessor;
import com.zoder.util.Context;

public class MMMFinalCountsProcessor extends AbstractVersionProcessor
{
    public void processVersion(DctmAccess access, Map script, Context context, Map document, Map version, int veridx) throws Exception 
    {
        IDfSession session = null;
        try { 
            session = access.getSession();
            IDfSysObject so = (IDfSysObject)session.getObject(new DfId((String)version.get("r_object_id")));
            Element d = (Element)version.get("DOM");
            
            Map countelements = (Map)context.get("CountElements");
            
            Iterator i = countelements.keySet().iterator();
            while (i.hasNext())
            {
                String element = (String)i.next();
                String subelement = (String)countelements.get(element);
                Node curnode = DOMUtils.get(d,element);
                addCount(curnode,subelement);
            }

        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }        
        
    }
    
    public void addCount(Node parentelem, String subelementname)
    {
        int nodecount = 0;
        NodeList nl = parentelem.getChildNodes();
        if (nl == null) {
            parentelem.getAttributes().setNamedItem(DOMUtils.newElement(parentelem, "count","0"));
            return;
        }
        for (int i=0; i < nl.getLength(); i++) {
            Node cn = nl.item(i); 
            String nodename = cn.getNodeName();
            if (subelementname.equals(nodename))
                nodecount++;
        }
        
        Node newattr = DOMUtils.newAttribute(parentelem, "count",""+nodecount);
        parentelem.getAttributes().setNamedItem(newattr);
    }
    

}
