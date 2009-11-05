package org.webgen.util;

import java.io.StringReader;
import java.io.StringWriter;
import java.util.Iterator;
import java.util.Map;

import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerConfigurationException;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;

import org.apache.commons.logging.LogFactory;
import org.webgen.core.WebgenLogger;

public class XSLT 
{
	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(XSLT.class));		
	static TransformerFactory transformerfactory = TransformerFactory.newInstance();;
	
	public static String exec(String xsltdoc, String xmldoc, Map xsltparameters)
	{
		/*-CFG-*/String m="exec(xsl,xml,map)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"xslt to apply: "+xsltdoc);
		/*-TRC-*/if(log.tOn)log.trc(m+"xml to tranform: "+xmldoc);
		/*-TRC-*/if(log.tOn)log.trc(m+"getting XSLT transformer");
	    Transformer xsltprocessor = null;
	    try {
	    	xsltprocessor = transformerfactory.newTransformer(new StreamSource(new StringReader(xsltdoc)));
	    } catch (TransformerConfigurationException tce) {
			/*-ERR-*/log.err(m+"could not initialize XSLT transformer engine",tce);
			RuntimeException re = new RuntimeException("XSLT could not initialize XSLT transformer engine",tce);
			throw re;	    	
	    }

	    // omit all the XML crap at the top (so we can do transforms of fragments easily...
		/*-TRC-*/if(log.tOn)log.trc(m+"omit XML declaration from transformer output");
	    xsltprocessor.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
	    // set transformation properties
		/*-TRC-*/if(log.tOn)log.trc(m+"check for transformation parameters");
	    if (xsltparameters != null)
	    {
			/*-TRC-*/if(log.tOn)log.trc(m+"parameters found, assigning them to the XSLT engine");
		    Iterator i = xsltparameters.keySet().iterator();
		    while (i.hasNext())
		    {
				/*-TRC-*/if(log.tOn)log.trc(m+"getting next transform parameter");
		    	String parameter = (String)i.next();
				/*-TRC-*/if(log.tOn)log.trc(m+"  name: "+parameter);
				/*-TRC-*/if(log.tOn)log.trc(m+"  value: "+xsltparameters.get(parameter));
		        xsltprocessor.setParameter(parameter, xsltparameters.get(parameter));		    	
		    }
	    }
	    // do we need a bufferedwriter here to improve performance?
		/*-TRC-*/if(log.tOn)log.trc(m+"prepping output buffer");
		StringWriter result = new StringWriter();
	
	    // apply transformation (finally)
	    try {
			/*-TRC-*/if(log.tOn)log.trc(m+"transforming...!!!");
	        xsltprocessor.transform(new StreamSource(new StringReader(xmldoc)), new StreamResult(result));
	    } catch (TransformerException te) {
			/*-ERR-*/log.err(m+"xsl transform threw exception",te);
			RuntimeException re = new RuntimeException("XSLT threw execption during xsl transform",te);
			throw re;	    	
	    }
		/*-TRC-*/if(log.tOn)log.trc(m+"  result: "+result.toString());
	
	    return result.toString();
	}
}