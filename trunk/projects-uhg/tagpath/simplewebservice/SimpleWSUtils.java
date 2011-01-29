package com.uhg.ovations.portal.partd.simplewebservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpression;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.HttpException;
import org.apache.commons.httpclient.HttpMethod;
import org.apache.commons.httpclient.methods.InputStreamRequestEntity;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang.StringEscapeUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;
import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleServiceInvocationException;

public class SimpleWSUtils
{
    private static final Logger log = Logger.getLogger(SimpleWSUtils.class);
    
    // simple quick-and-dirty creation of a context map, all values are XML-escaped
    public static Map mvelAddToContextEscapeXML(Map context,String... keyvalues)
    {
        // setup mvel template context
        for (int i=0; i < keyvalues.length/2; i++) {
        	if (keyvalues.length > i*2) {
	            String key = keyvalues[i*2];
	            String value = keyvalues[i*2+1];
	            context.put(key, StringEscapeUtils.escapeXml(value));
        	}
        }        
        return context;
    }

    public static HttpMethod makePostCall(String request, HttpClient httpClient, String requestURI) {return makePostCall(request, httpClient,requestURI,null); }

    // initiates basic POST method call, assuming XML content type
    // ...no soap headers or actions, haven't needed them yet though.
    public static HttpMethod makePostCall(String request, HttpClient httpClient, String requestURI, String contentTypeHeader)
    {
    	PostMethod postMethod = null;
        try {        
            postMethod = new PostMethod(requestURI);
            // - if you get "Transport level information does not match SOAP message namespace URI", 
            // then you may need to either not send a content type, send a soap action in the content type,
            // or change the soap-action embedded in the request XML.
            // - javax.mail.parseException is probably a bad mime type in the Content-Type header            
            if (StringUtils.isNotEmpty(contentTypeHeader)) {
            	postMethod.setRequestHeader("Content-Type",contentTypeHeader);
            }
            postMethod.setRequestEntity(new InputStreamRequestEntity(IOUtils.toInputStream(request)));
            
            int code = httpClient.executeMethod(postMethod);
            if (log.isDebugEnabled())log.debug("SimpleWSUtils - POST invocation returned http code: "+code);
            
            // do NOT convert this stream into a string as an intermediary, since some protocol 
            // is not properly serialized and the DOM parse fails.
            return postMethod;
        
        } catch (HttpException httpe) {
            log.error("HTTPEXCEPTION failed webservice request: "+request,httpe);
            throw new SimpleServiceInvocationException("Unable to make webservice call",httpe);
        } catch (IOException ioe) {
            throw new SimpleServiceInvocationException("ioexception making call",ioe);
        } 

    }

    public static Document parseXmlToDOM(InputStream xmlStream)
    {
        try {
            // incredibly, this factory is not guaranteed to be threadsafe, so we must create a new one each time
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(false);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            return doc;
        } catch (SAXException saxe) {
            throw new SimpleResponseParsingException("SimpleWSUtils - parse to DOM encountered an xml parse exception ",saxe);           
        } catch (Exception pce) {
            throw new SimpleResponseParsingException("SimpleWSUtils - parse to DOM encountered an exception ",pce);           
        }
        
    }

    public static Document parseXmlToDomWithNamespaces(InputStream xmlStream)
    {
        try {
            // incredibly, this factory is not guaranteed to be threadsafe, so we must create a new one each time
            DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
            factory.setNamespaceAware(true);
            DocumentBuilder builder = factory.newDocumentBuilder();
            Document doc = builder.parse(xmlStream);
            return doc;
        } catch (SAXException saxe) {
            throw new SimpleResponseParsingException("SimpleWSUtils - parse to DOM encountered an xml parse exception ",saxe);           
        } catch (Exception pce) {
            throw new SimpleResponseParsingException("SimpleWSUtils - parse to DOM encountered an exception ",pce);           
        }
        
    }

    
    // compiles and executes xpath that expects to return a NodeList
    public static NodeList execNodeListXPath(Node doc, String expression)
    {
        // none of this is threadsafe in java's xml apis, nice job guys
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression xpathexpr = null;        
        try { 
            xpathexpr = xpath.compile(expression);
        } catch (XPathExpressionException xpe) {
            throw new SimpleResponseParsingException("SimpleWSUtils - could not compile xpath "+expression,xpe);
        }
        try {
            Object returnval = xpathexpr.evaluate(doc, XPathConstants.NODESET);
            try {
                NodeList nodeset = (NodeList)returnval;
                return nodeset;
            } catch (ClassCastException cce) {
                throw new SimpleResponseParsingException("SimpleWSUtils - xpath execution expected NodeList, returned "+returnval.getClass().getName()+" for expression "+expression,cce);                
            }
        } catch (XPathExpressionException xpee) {
            throw new SimpleResponseParsingException("SimpleWSUtils - could not execute xpath "+expression,xpee);            
        }
    }
    
    // compiles and executes an xpath that expects to return a string
    public static String execStringXPath(Node doc, String expression)
    {
        // TODO: make more flexible, such that if xpath exec returns a node/tag, 
        //       we attempt to see if it is a simple tag with a single text element
        
        // none of this is threadsafe in java's xml apis, nice job guys
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression xpathexpr = null;        
        try { 
            xpathexpr = xpath.compile(expression);
        } catch (XPathExpressionException xpe) {
            throw new SimpleResponseParsingException("SimpleWSUtils - could not compile xpath "+expression,xpe);
        }
        try {
            Object returnval = xpathexpr.evaluate(doc, XPathConstants.STRING);
            try {
                String stringval = (String)returnval;
                return stringval;
            } catch (ClassCastException cce) {
                throw new SimpleResponseParsingException("SimpleWSUtils - xpath execution expected String, returned "+returnval.getClass().getName()+" for expression "+expression,cce);                
            }
        } catch (XPathExpressionException xpee) {
            throw new SimpleResponseParsingException("SimpleWSUtils - could not execute xpath "+expression,xpee);            
        }
    }

    // compiles and executes an xpath that expects to return a string
    public static Node execNodeXPath(Node doc, String expression)
    {        
        // none of this is threadsafe in java's xml apis, nice job guys
        XPathFactory xpathfactory = XPathFactory.newInstance();
        XPath xpath = xpathfactory.newXPath();
        XPathExpression xpathexpr = null;        
        try { 
            xpathexpr = xpath.compile(expression);
        } catch (XPathExpressionException xpe) {
            throw new SimpleResponseParsingException("SimpleWSUtils - could not compile xpath "+expression,xpe);
        }
        try {
            Object returnval = xpathexpr.evaluate(doc, XPathConstants.NODE);
            try {
                Node stringval = (Node)returnval;
                return stringval;
            } catch (ClassCastException cce) {
                throw new SimpleResponseParsingException("SimpleWSUtils - xpath execution expected Node, returned "+returnval.getClass().getName()+" for expression "+expression,cce);                
            }
        } catch (XPathExpressionException xpee) {
            throw new SimpleResponseParsingException("SimpleWSUtils - could not execute xpath "+expression,xpee);            
        }
    }
    
    public static Node findChild(Node rootNode, String nodeName)
    {
    	if (rootNode == null) return null;    	
    	if (StringUtils.isEmpty(nodeName)) return null;
    	
		NodeList children = rootNode.getChildNodes();
		for (int j=0; j < children.getLength(); j++) {
			Node childnode = children.item(j);
			String childname = childnode.getLocalName();
			if (childname == null) {
				childname = childnode.getNodeName();
				if (StringUtils.contains(childname, ':')) {
					childname = childname.substring(childname.indexOf(':')+1);
				}
			}
			if (StringUtils.equals(nodeName, childnode.getNodeName())) {
				return childnode;
			}
		}		
		return null;			
    }
    
    public static Node getRelativeNode(Node rootnode, String... tagpath)
    {
    	Node currentNode = rootnode;
    	for (int i=0; i < tagpath.length; i++) {
    		currentNode = findChild(currentNode,tagpath[i]);
    		if (currentNode == null) return null;
    	}
    	return currentNode;
    }

    public static Node getRelativeNode(Node rootnode, List<String> tagpath)
    {
    	Node currentNode = rootnode;
    	for (String tagname : tagpath) {
    		currentNode = findChild(currentNode,tagname);
    		if (currentNode == null) return null;
    	}
    	return currentNode;
    }

    public static String getNodeValue(Node node)
    {
    	if (node == null) return null;
    	NodeList children = node.getChildNodes();
    	if (children == null) return null;
    	for (int i=0; i < children.getLength(); i++) {
    		Node child = children.item(i);
    		// TODO: CDATA too?
    		if (child.getNodeType() == Node.TEXT_NODE) {
    			String value = child.getNodeValue();
    			return value;
    		}
    	}
    	return null;    	    	
    }

    public static String getRelativeNodeValue(Node rootnode, String... tagpath)
    {
    	Node relnode = getRelativeNode(rootnode, tagpath);
    	if (relnode == null) return null;
    	String relnodeval = getNodeValue(relnode);
    	return relnodeval;
    }
    
    public static String serializeDOMToString(Document dom)
    {
   		try {
    		if (log.isDebugEnabled())log.debug("Serializing XML using LS 3.0 jaxp");
    		TransformerFactory factory = TransformerFactory.newInstance();
    		Transformer transformer = factory.newTransformer();
    	    //transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
    	    //transformer.setOutputProperty(OutputKeys.METHOD, "xml");
    	    //transformer.setOutputProperty(OutputKeys.ENCODING, "ISO-8859-1");
    	    //transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "4");
    	    //transformer.setOutputProperty(OutputKeys.INDENT, "yes");    		
    		DOMSource source = new DOMSource(dom);
    		StringWriter sw = new StringWriter();
	        StreamResult strresult = new StreamResult(sw);
    		transformer.transform(source, strresult);
    		String result = sw.toString();
    		return result;
   		} catch (Throwable t) {
			if(log.isDebugEnabled())log.debug("error occurred in jaxp xml serialization attempt",t);
   		}    		
    	
    	if (dom.getFeature("Core","3.0") != null && dom.getFeature("LS", "3.0") != null) {
    		try {
	    		if (log.isDebugEnabled())log.debug("Serializing XML using LS 3.0 dom impl feature");
	    		DOMImplementationLS domLS = (DOMImplementationLS)dom.getImplementation().getFeature("LS", "3.0");
	    		LSOutput lsout = domLS.createLSOutput();
		        StringWriter sw = new StringWriter(); 
	    		lsout.setCharacterStream(sw);
	    		LSSerializer lsser = domLS.createLSSerializer();
	    		boolean success = lsser.write(dom, lsout);
	    		String result = sw.toString();
	    		return result;
    		} catch (Throwable t) {
    			if(log.isDebugEnabled())log.debug("error occurred in LS 3.0 xml serialization attempt, will try jaxp",t);
    		}
    	} else {
    		if(log.isDebugEnabled())log.debug("No support for Core/LS 3.0 detected, xml serializaton not attempted");
    	}

//   		// xerces impl
//    	try {
//	        StringWriter out = new StringWriter(); 
//	        XMLSerializer serializer = new XMLSerializer(out, new OutputFormat(dom)); 
//	        serializer.serialize(dom); 
//	        String result = out.toString();
//	        return result;
//    	} catch (IOException ioe) {
//    		throw new SimpleResponseParsingException("Error serializing DOM document to string",ioe);
//    	}

   		// nothing worked, return null
		if(log.isDebugEnabled())log.debug("Unable to serialize DOM tree");
		return null;

    }
    
    public static String[] appendStrings(String[] originalArgs, Map<String,String> map, String... extras)
    {    	
    	String[] newArray = new String[(originalArgs != null ? originalArgs.length : 0) + 
    	                               (extras != null ? extras.length : 0) +
    	                               (map != null ? (map.keySet().size()*2) : 0)];
    	int i = 0;
    	if (originalArgs != null) {
	    	for (; i < originalArgs.length; i++) {
	    		newArray[i] = originalArgs[i];
	    	}    	
    	}
    	if (extras != null) {
	    	for (int j = 0; j < extras.length; j++,i++) {	    		
	    		newArray[i] = extras[j];
	    	}    	
    	}
    	if (map != null) {
    		int j=0; 
	    	for(String key : map.keySet()) {
	    		newArray[i+j*2] = key;
	    		newArray[i+j*2+1] = map.get(key);
	    		j++;
	    	}
    	}
    	return newArray;    		
    }
    
}
