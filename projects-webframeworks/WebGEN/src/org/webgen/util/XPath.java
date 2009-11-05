package org.webgen.util;

import java.io.Reader;
import java.io.StringReader;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;

import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.webgen.core.WebgenLogger;
import org.xml.sax.InputSource;

import com.sun.org.apache.xpath.internal.XPathAPI;
import com.sun.org.apache.xpath.internal.objects.XNull;
import com.sun.org.apache.xpath.internal.objects.XObject;

// executes a series of XPath expressions on the specified XML document...
// - usual exec method is exec(Map,String), or exec(Map,Reader)
// - use exec(Map,Document) if you have a parsed xml doc that you don't
//   want reparsed

public class XPath {

	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(XPath.class));		

	// execute XPath expression map against parsed xml document 
	public static Map exec(Map expressions, Document xmldoc)
	{
		/*-CFG-*/String m="exec(map,doc)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"instantiating XPath transformer");
		try {
			// prep a transformer (?share this?)
	        Transformer serializer = TransformerFactory.newInstance().newTransformer();
	        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} catch (TransformerConfigurationException tce) {
			/*-ERR-*/log.err("exec(map,doc)-ExecXPath could not initialize XPath transformer engine",tce);
			RuntimeException re = new RuntimeException("ExecXPath could not initialize XPath transformer engine",tce);
			throw re;
		}

        Map results = new HashMap();        
        // process XPath statements
		/*-TRC-*/if(log.tOn)log.trc(m+"iterating through XPath expressions");
        Iterator i = expressions.keySet().iterator();
        while (i.hasNext()) {
        	String storagekey = (String)i.next();
    		/*-TRC-*/if(log.tOn)log.trc(m+"  current key: "+storagekey);
        	String expression = (String)expressions.get(storagekey);
    		/*-TRC-*/if(log.tOn)log.trc(m+"  current XPath: "+expression);

            // execute the XPath statement
    		/*-TRC-*/if(log.tOn)log.trc(m+"  evaluating expression");    		
            XObject l_xobj = null;
            try {
            	l_xobj = XPathAPI.eval(xmldoc, expression);
            } catch (TransformerException te) {
    			/*-ERR-*/log.err(m+"ExecXPath encoutered exception when processing XPath expression "+expression,te);
    			RuntimeException re = new RuntimeException("ExecXPath encoutered exception when processing XPath expression "+expression,te);
    			throw re;            	
            }
            // store result
            if (l_xobj instanceof XNull) {
        		/*-TRC-*/if(log.tOn)log.trc(m+"  storing null XPath result");    		
                results.put(storagekey, null);
            }
            else {
        		/*-TRC-*/if(log.tOn)log.trc(m+"  storing XPath result"+l_xobj.str());    		
                String result = l_xobj.str();
                results.put(storagekey, result);
            }
        }
		/*-TRC-*/if(log.tOn)log.trc(m+"returning result map");    		
        return results;
	}

	// prep the xml doc's input source before parsing and eval'ing
	public static Map exec(Map expressions, String xmldoc)
	{
        // parse the document
		/*-TRC-*/if(log.tOn)log.trc("exec(map,string)-converting XML string to reader for processing");
        StringReader strrdr = new StringReader(xmldoc);
		/*-TRC-*/if(log.tOn)log.trc("exec(map,string)-XML Doc: ["+xmldoc+"]");
        return exec(expressions,strrdr);
	}
	
	// parse xml document before evaluating 
	public static Map exec(Map expressions, Reader rdr)
	{
		/*-TRC-*/if(log.tOn)log.trc("exec(map,rdr)-converting reader to xml inputsource");
        InputSource l_inXml = new InputSource(rdr);
		/*-TRC-*/if(log.tOn)log.trc("exec(map,rdr)-instantiating parser");
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true); // need this call?
		/*-TRC-*/if(log.tOn)log.trc("exec(map,rdr)-parsing document");
		Document doc = null;
		try {
			doc = dfactory.newDocumentBuilder().parse(l_inXml);
		} catch (Exception e) {
			/*-ERR-*/log.err("exec(map,rdr)-ExecXPath encountered error during input xml document parsing",e);
			RuntimeException re = new RuntimeException("ExecXPath encountered error during input xml document parsing",e);
			throw re;
		}
        return exec(expressions,doc);
	}

	// prep the xml doc's input source before parsing and eval'ing
	public static String exec(String expression, String xmldoc)
	{
        // parse the document
		/*-TRC-*/if(log.tOn)log.trc("exec(str,string)-converting XML string to reader for processing");
        StringReader strrdr = new StringReader(xmldoc);
		/*-TRC-*/if(log.tOn)log.trc("exec(str,string)-XML Doc: ["+xmldoc+"]");
        return exec(expression,strrdr);
	}
	
	// parse xml document before evaluating 
	public static String exec(String expression, Reader rdr)
	{
		/*-TRC-*/if(log.tOn)log.trc("exec(str,rdr)-converting reader to xml inputsource");
        InputSource l_inXml = new InputSource(rdr);
		/*-TRC-*/if(log.tOn)log.trc("exec(str,rdr)-instantiating parser");
        DocumentBuilderFactory dfactory = DocumentBuilderFactory.newInstance();
        dfactory.setNamespaceAware(true); // need this call?
		/*-TRC-*/if(log.tOn)log.trc("exec(str,rdr)-parsing document");
		Document doc = null;
		try {
			doc = dfactory.newDocumentBuilder().parse(l_inXml);
		} catch (Exception e) {
			/*-ERR-*/log.err("exec(str,rdr)-ExecXPath encountered error during input xml document parsing",e);
			RuntimeException re = new RuntimeException("ExecXPath encountered error during input xml document parsing",e);
			throw re;
		}
        return exec(expression,doc);
	}

	public static String exec(String expression, Document xmldoc)
	{
		/*-CFG-*/String m="exec(map,doc)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"instantiating XPath transformer");
		try {
			// prep a transformer (?share this?)
	        Transformer serializer = TransformerFactory.newInstance().newTransformer();
	        serializer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
		} catch (TransformerConfigurationException tce) {
			/*-ERR-*/log.err("exec(map,doc)-ExecXPath could not initialize XPath transformer engine",tce);
			RuntimeException re = new RuntimeException("ExecXPath could not initialize XPath transformer engine",tce);
			throw re;
		}

        // process XPath statements
		/*-TRC-*/if(log.tOn)log.trc(m+"process XPath expression");

        // execute the XPath statement
		/*-TRC-*/if(log.tOn)log.trc(m+"  evaluating expression");    		
        XObject l_xobj = null;
        try {
        	l_xobj = XPathAPI.eval(xmldoc, expression);
        } catch (TransformerException te) {
			/*-ERR-*/log.err(m+"ExecXPath encoutered exception when processing XPath expression "+expression,te);
			RuntimeException re = new RuntimeException("ExecXPath encoutered exception when processing XPath expression "+expression,te);
			throw re;            	
        }

        // store result
        if (l_xobj instanceof XNull) {
    		/*-TRC-*/if(log.tOn)log.trc(m+"  storing null XPath result");    		
            return null;
        }
        else {
    		/*-TRC-*/if(log.tOn)log.trc(m+"  storing XPath result"+l_xobj.str());    		
            String result = l_xobj.str();
            return result;
        }
		
	}

}
