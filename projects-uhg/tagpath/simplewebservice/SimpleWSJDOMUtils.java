package com.uhg.ovations.portal.partd.simplewebservice;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.XMLOutputter;
import org.jdom.xpath.XPath;

import com.uhg.ovations.portal.partd.simplewebservice.exceptions.SimpleResponseParsingException;

public class SimpleWSJDOMUtils {

    private static final Logger log = Logger.getLogger(SimpleWSJDOMUtils.class);

    public static Document parseXmlToDOM(InputStream xmlStream)
    {
        try {
        	SAXBuilder builder = new SAXBuilder();
        	Document doc = builder.build(xmlStream);
            return doc;
        } catch (JDOMException saxe) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - JDOM parse encountered an xml parse exception ",saxe);           
        } catch (Exception pce) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - JDOM parse encountered an exception ",pce);           
        }
        
    }

//    public static Document parseXmlToDOMNoNamespaces(InputStream xmlStream)
//    {
//        try {
//        	SAXBuilder builder = new SAXBuilder();
//        	//builder.setFeature( "http://xml.org/sax/features/namespaces", false );
//        	//builder.setFeature( "http://xml.org/sax/features/namespace-prefixes", false );
//        	Document doc = builder.build(xmlStream);
//            return doc;
//        } catch (JDOMException saxe) {
//            throw new SimpleResponseParsingException("SimpleJDOMUtils - JDOM parse encountered an xml parse exception ",saxe);           
//        } catch (Exception pce) {
//            throw new SimpleResponseParsingException("SimpleJDOMUtils - JDOM parse encountered an exception ",pce);           
//        }
//        
//    }

    
    
    // For namespace ignoring, maybe SAXBuilder with setProperty calls:
    // namespaces=false
    // namespace-prefixes=false
    
    // compiles and executes xpath that expects to return a NodeList
    public static List execListXPath(Object doc, String expression)
    {
    	XPath xpath = null;
    	try {
    		xpath = XPath.newInstance(expression);
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not compile xpath "+expression,jdome);    		
    	}
    	try {
	    	List resultlist = xpath.selectNodes(doc);
	    	return resultlist;
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not execute xpath "+expression,jdome);                		
    	}    	
    }

    public static List execListXPath(Object doc, String expression, Map<String,String> namespaces)
    {
    	XPath xpath = null;
    	try {
    		xpath = XPath.newInstance(expression);
    		if (namespaces != null) {
    			for (String prefix : namespaces.keySet()) {
    				String uri = namespaces.get(prefix);
    				xpath.addNamespace(prefix, uri);
    			}
    		}    		
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not compile xpath "+expression,jdome);    		
    	}
    	try {
	    	List resultlist = xpath.selectNodes(doc);
	    	return resultlist;
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not execute xpath "+expression,jdome);                		
    	}    	
    }

    public static Object execSingleXPath(Object doc, String expression)
    {
    	XPath xpath = null;
    	try {
    		xpath = XPath.newInstance(expression);
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not compile xpath "+expression,jdome);    		
    	}
    	try {
	    	Object result = xpath.selectSingleNode(doc);
	    	return result;
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not execute xpath "+expression,jdome);                		
    	}    	
    	
    }
    
    public static Object execSingleXPath(Object doc, String expression, Map<String,String> namespaces)
    {
    	XPath xpath = null;
    	try {
    		xpath = XPath.newInstance(expression);
    		if (namespaces != null) {
    			for (String prefix : namespaces.keySet()) {
    				String uri = namespaces.get(prefix);
    				xpath.addNamespace(prefix, uri);
    			}
    		}
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not compile xpath "+expression,jdome);    		
    	}
    	try {
	    	Object result = xpath.selectSingleNode(doc);
	    	return result;
    	} catch (JDOMException jdome) {
            throw new SimpleResponseParsingException("SimpleJDOMUtils - could not execute xpath "+expression,jdome);                		
    	}    	
    	
    }


	public static String serializeDOMToString(Document dom)
	{
		try {
			StringWriter out = new StringWriter(); 
	  		XMLOutputter output = new XMLOutputter();  // And output the document ... 		
	  		output.output(dom, out);    		
	        String result = out.toString();
	        return result;
		} catch (IOException ioe) {
			throw new SimpleResponseParsingException("Error serializing DOM document to string",ioe);
	  	}
	}

    public static Element findChild(Element rootNode, String nodeName)
    {
    	if (rootNode == null) return null;    	
    	if (StringUtils.isEmpty(nodeName)) return null;
    	
		List children = rootNode.getChildren();
		if (children == null) return null;
		
		for (int j=0; j < children.size(); j++) {
			Element childnode = (Element)children.get(j);
			if (StringUtils.equals(nodeName, childnode.getName())) {
				return childnode;
			}
		}		
		return null;			
    }
    
    public static Element getTagPath(Element rootnode, String... tagpath)
    {
    	Element currentNode = rootnode;
    	if (tagpath == null) return null;
    	if (currentNode == null) return null;
    	for (int i=0; i < tagpath.length; i++) {
    		currentNode = findChild(currentNode,tagpath[i]);
    		if (currentNode == null) return null;
    	}
    	return currentNode;
    }

    public static Element getTagPath(Element rootnode, List<String> tagpath)
    {
    	Element currentNode = rootnode;
    	if (tagpath == null) return null;
    	if (currentNode == null) return null;
    	for (String tagname :  tagpath) {
    		currentNode = findChild(currentNode,tagname);
    		if (currentNode == null) return null;
    	}
    	return currentNode;
    }
    
    public static String getStringValue(Element element)
    {
    	if (element == null) return null;
    	else return element.getValue();
    }
    
    public static List<String> buildTagPath(String... tagnames)
    {
    	List<String> tagpath = new ArrayList<String>();
    	if (tagnames == null) return tagpath;
    	for (String tagname : tagnames) { tagpath.add(tagname);}
    	return tagpath;
    }

}
