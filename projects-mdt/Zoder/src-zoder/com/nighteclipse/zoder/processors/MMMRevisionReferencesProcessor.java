package com.nighteclipse.zoder.processors;

import java.util.List;
import java.util.Map;

import org.w3c.dom.Element;
import org.w3c.dom.Node;

import com.nighteclipse.zoder.util.DOMUtils;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractDocumentProcessor;
import com.zoder.util.Context;

public class MMMRevisionReferencesProcessor extends AbstractDocumentProcessor
{

    public void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception 
    {
        List versionlist = (List)document.get("Versions");
        for (int i=0; i < versionlist.size(); i++) {
            Map version = (Map)versionlist.get(i);
            glueRevisions(i,version,document,context);
        }
    }
    
    void glueRevisions(int idx, Map version, Map document, Map context)
    {
        Element curobj = (Element)version.get("DOM");
        DOMUtils.put(curobj,"objectRevision",""+(idx+1));
        Node revisionselement = DOMUtils.getmake(curobj,"revisions");
        List versionlist = (List)document.get("Versions");
        DOMUtils.put(revisionselement,"total", ""+'"'+versionlist.size()+'"');
        
        // check if this is the first version
        if (idx != 0)
        {
            // there is a previous...
            Map prevBO = (Map)versionlist.get(idx-1);
            Element prevBODOM = (Element)prevBO.get("DOM");
            Node previousRevisionElement = DOMUtils.getmake(revisionselement,"previousRevision");
            Node businessObjectRefElement = DOMUtils.getmake(previousRevisionElement,"businessObjectRef");
            
            String objname = DOMUtils.get(prevBODOM, "objectName").getTextContent();
            String objtype = DOMUtils.get(prevBODOM, "objectType").getTextContent();
            String vault   = DOMUtils.get(prevBODOM, "vaultRef").getTextContent();
            DOMUtils.put(businessObjectRefElement,"objectName", objname);
            DOMUtils.put(businessObjectRefElement,"objectRevision", ""+idx);
            DOMUtils.put(businessObjectRefElement,"objectType",objtype);
            DOMUtils.put(businessObjectRefElement,"vaultRef", vault);
        }
        // check if this is the last version
        if (idx+1 < versionlist.size())
        {
            // there is a next...
            Map nextBO = (Map)versionlist.get(idx+1);
            Element nextBODOM = (Element)nextBO.get("DOM");
            Node nextRevisionElement = DOMUtils.getmake(revisionselement,"nextRevision");
            Node businessObjectRefElement = DOMUtils.getmake(nextRevisionElement,"businessObjectRef");
            
            String objname = DOMUtils.get(nextBODOM, "objectName").getTextContent();
            String objtype = DOMUtils.get(nextBODOM, "objectType").getTextContent();
            String vault   = DOMUtils.get(nextBODOM, "vaultRef").getTextContent();
            DOMUtils.put(businessObjectRefElement,"objectName", objname);
            DOMUtils.put(businessObjectRefElement,"objectRevision", ""+(idx+2)); 
            DOMUtils.put(businessObjectRefElement,"objectType",objtype);
            DOMUtils.put(businessObjectRefElement,"vaultRef", vault);
        }
    }
    

}
