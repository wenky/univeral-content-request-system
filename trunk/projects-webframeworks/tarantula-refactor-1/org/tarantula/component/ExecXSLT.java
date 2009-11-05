package org.tarantula.component;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Result;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

public class ExecXSLT implements PageComponentInterface 
{
    public String getNickname() { return "ExecXSLT"; }

    
    TransformerFactory transformerfactory = null;

    public void init() throws Exception 
    {
        transformerfactory = TransformerFactory.newInstance();
    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String xsltdoc = (String)arguments.get("XSLT");
        String xmldoc  = (String)arguments.get("XmlInput");
        
        // get xslt transformer from factory
        Transformer xsltprocessor = transformerfactory.newTransformer(new StreamSource(new StringReader(xsltdoc)));        
        xsltprocessor.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
        
        int i = 0;
        while (arguments.containsKey("XsltInputKey"+i))
        {
            xsltprocessor.setParameter((String)arguments.get("XsltInputKey"+i),(String)arguments.get("XsltInputValue"+i));
            i++;
        }
        StringWriter result = new StringWriter();

        // apply transformation (finally)
        try {
            xsltprocessor.transform(new StreamSource(new StringReader(xmldoc)), new StreamResult(result));
        } catch (TransformerException xslexcpt) {
            throw xslexcpt;
        }

        return result.toString();
    }

}
