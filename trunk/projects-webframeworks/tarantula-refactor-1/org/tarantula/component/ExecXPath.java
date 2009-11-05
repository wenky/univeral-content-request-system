package org.tarantula.component;

import java.io.StringReader;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;

import org.tarantula.component.interfacedefinition.PageComponentInterface;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XNull;
import com.sun.org.apache.xpath.internal.objects.XObject;

// executes a series of XPath expressions
// on the specified XML document...
// inputs:
// - ExecXPath.Source		if null, then pipelined input, if not null, a key in the scratchpad

public class ExecXPath implements PageComponentInterface {

    public String getNickname() { return "ExecXPath"; }

    public void init()
    {
        // do nothing for now...
    }
    
    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        // get XML document to be analyzed - for now, assume its the input
        // document
        String sourcexml = (String) scratchpad.get(arguments.get("Source"));

        // parse the document
        StringReader strrdr = new StringReader(sourcexml);
        InputSource l_inXml = new InputSource(strrdr);
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true); // need this call?
        Document doc = dfactory.newDocumentBuilder().parse(l_inXml);

        // prep a transformer to process the XPath statements
        Transformer serializer = TransformerFactory.newInstance().newTransformer();
        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");

        // process XPath statements
        int i = 1;
        while (scratchpad.containsKey("ExecXPath.Statement" + i)) {
            String l_sXPathStmt = (String) scratchpad.get("ExecXPath.Statement" + i);
            String l_sStorageKey = (String) scratchpad.get("ExecXPath.Key" + i);

            // execute the XPath statement
            XObject l_xobj = XPathAPI.eval(doc, l_sXPathStmt);

            // place result in plu environment
            if (l_xobj instanceof XNull) {
                scratchpad.put(l_sStorageKey, null);
            }
            else {
                String l_sResult = l_xobj.str();
                scratchpad.put(l_sStorageKey, l_sResult);
            }
            i++;
        }

        return "";
    }

}
