package com.nighteclipse.zoder.processors;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Result;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;

import com.documentum.fc.client.IDfSession;
import com.zoder.access.DctmAccess;
import com.zoder.processors.AbstractDocumentProcessor;
import com.zoder.util.Context;
import com.zoder.util.Lg;

public class MMMSerializeXML extends AbstractDocumentProcessor
{

    public void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception 
    {
        String outputdir = (String)context.get("OutputDirectory");
        String filenameprefix = (String)context.get("OutputPrefix");
        String xmlheader = (String)context.get("XmlHeader");
        if (outputdir.charAt(outputdir.length()-1) != '/') outputdir += '/';
        
        IDfSession session = null;
        try { 
            session = access.getSession();
            
            List doclist = (List)document.get("Versions");
            /*-trc-*/Lg.log("doclist found? "+(doclist!=null));
            if (doclist != null) {
                // get top-level DOM
                Document docdom = (Document)document.get("DOM");
                
                /*-dbg-*/Lg.log("%s - iterating on versions");
                for (int c=0; c < doclist.size(); c++) {
                    /*-trc-*/Lg.log("doc version "+c);
                    Map version = (Map)doclist.get(c);
                    Element verdom = (Element)version.get("DOM");
                    docdom.getDocumentElement().appendChild(verdom);
                }
                StringWriter buf = new StringWriter();
                
                // jaxp for outputting, may need to use Xerces-J...
                TransformerFactory xformFactory  = TransformerFactory.newInstance();
                Transformer idTransform = xformFactory.newTransformer();
                Source input = new DOMSource(docdom);
                Result output = new StreamResult(buf);
                idTransform.transform(input, output);
                
                String serializedxml = buf.toString();
                
                // now what?
                String newdir = outputdir+document.get("ObjectName")+"-"+document.get("ChronicleId")+"/";
                File newdirfile = new File(newdir);
                if (!newdirfile.exists()) {
                    newdirfile.mkdirs();
                }
                String filename = newdir + filenameprefix + '_' + ".xml";
                File file = new File(filename);
                BufferedWriter wrt = new BufferedWriter(new FileWriter(file));
                wrt.write(serializedxml);               
                wrt.close();
            }
            
        } finally {
            try{access.releaseSession(session);}catch(Exception e) {}
        }
    }
            
}
