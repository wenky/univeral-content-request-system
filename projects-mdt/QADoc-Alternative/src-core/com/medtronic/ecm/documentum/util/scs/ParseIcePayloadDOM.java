package com.medtronic.ecm.documentum.util.scs;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;

import org.w3c.dom.CDATASection;
import org.w3c.dom.Document;
import org.w3c.dom.NamedNodeMap;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.medtronic.ecm.documentum.util.scs.iceobjects.DctmAttr;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IceItem;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IcePackage;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IcePayload;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IceRequest;

public class ParseIcePayloadDOM {
    
    static XPath xPath = XPathFactory.newInstance().newXPath();
    
    public static IcePayload parseIcePayloadXML(Reader xmldoc) throws Exception
    {
        // DOM Parse
        DOMResult domresult = new DOMResult();
        TransformerFactory transformerfactory = TransformerFactory.newInstance();
        Transformer transformer = transformerfactory.newTransformer();
        transformer.transform(new StreamSource(xmldoc), domresult);
        Document document = (Document)domresult.getNode();
        document.normalize();
        //normalizeCDATASections(document);
        Node root = (Node) document.getDocumentElement();
        
        IcePayload payload = null;
        
        String tagname = root.getNodeName();
        if ("ice-payload".equals(tagname)) {
            payload = processIcePayload(root);
        }
        
        return payload;
    }
    
    public static IcePayload processIcePayload(Node node) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IcePayload payload = new IcePayload();
        NamedNodeMap attrs = node.getAttributes();
        for (int i=0; i < attrs.getLength(); i++)
        {
            String attrname = attrs.item(i).getNodeName();
            String attrvalue = attrs.item(i).getNodeValue();
            if ("payload-id".equals(attrname)) payload.PayloadId = attrvalue;
            if ("timestamp".equals(attrname)) payload.Timestamp = attrvalue;
            if ("ice.version".equals(attrname)) payload.Version = attrvalue;
        }
        
        NodeList subnodes = node.getChildNodes();
        
        for (int c=0; c<subnodes.getLength(); c++)
        {
            Node child = subnodes.item(c);
            String name = child.getNodeName();
            if (name.equals("ice-header")) {
                // ignore for now
            }
            if (name.equals("ice-request")) {
                payload.Request = processIceRequest(child);
            }
        }
        return payload;
    }

    public static IceRequest processIceRequest(Node node) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IceRequest request = new IceRequest();
        NamedNodeMap attrs = node.getAttributes();
        for (int i=0; i < attrs.getLength(); i++)
        {
            String attrname = attrs.item(i).getNodeName();
            String attrvalue = attrs.item(i).getNodeValue();
            if ("request-id".equals(attrname)) request.RequestId = attrvalue;
        }
        
        NodeList subnodes = node.getChildNodes();
        
        for (int c=0; c<subnodes.getLength(); c++)
        {
            Node child = subnodes.item(c);
            String name = child.getNodeName();
            if (name.equals("ice-package")) {
                IcePackage pkg = processIcePackage(child);
                if (request.Packages == null) request.Packages = new ArrayList<IcePackage>();
                request.Packages.add(pkg);
            }
        }
        return request;
    }

    public static IcePackage processIcePackage(Node node) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IcePackage pkg = new IcePackage();
        NamedNodeMap attrs = node.getAttributes();
        for (int i=0; i < attrs.getLength(); i++)
        {
            String attrname = attrs.item(i).getNodeName();
            String attrvalue = attrs.item(i).getNodeValue();
            if ("subscription-id".equals(attrname)) pkg.SubscriptionId = attrvalue;
            if ("package-id".equals(attrname)) pkg.PackageId = attrvalue;
        }
        
        NodeList subnodes = node.getChildNodes();
        
        for (int c=0; c<subnodes.getLength(); c++)
        {
            Node child = subnodes.item(c);
            String name = child.getNodeName();
            if (name.equals("ice-item")) {
                IceItem item = processIceItem(child);
                if (pkg.Items == null) pkg.Items = new ArrayList<IceItem>();
                pkg.Items.add(item);
            }
        }
        return pkg;
    }
    
    
    
    public static IceItem processIceItem(Node node) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IceItem item = new IceItem();
        NamedNodeMap attrs = node.getAttributes();
        for (int i=0; i < attrs.getLength(); i++)
        {
            String attrname = attrs.item(i).getNodeName();
            String attrvalue = attrs.item(i).getNodeValue();
            if ("item-id".equals(attrname)) item.Id = attrvalue;
            if ("subscription-element".equals(attrname)) item.SubscriptionElement = attrvalue;
            if ("name".equals(attrname)) item.Name = attrvalue;
            if ("content-filename".equals(attrname)) item.ContentFilename = attrvalue;
            if ("content-type".equals(attrname)) item.ContentType = attrvalue;
            if ("lang".equals(attrname)) item.Language = attrvalue;
        }
        
        NodeList subnodes = node.getChildNodes();
        
        for (int c=0; c<subnodes.getLength(); c++)
        {
            Node child = subnodes.item(c);
            String name = child.getNodeName();
            if (name.equals("dctm-object")) {
                item.DctmObject = processDctmObject(child);
                if (item.DctmObject.containsKey("Version_current"))
                    item.isCurrent = true;
                item.Label = (String)item.DctmObject.get("Version_label");
                item.VersionNbr = (String)item.DctmObject.get("Version_number");
                item.ContentRef = (String)item.DctmObject.get("dctm-content-ref");
            }
        }
            
        return item;
    }
    

    
    public static Map processDctmObject(Node node) throws Exception
    {
        // we are already at start-tag, parse the attributes
        Map attrs = new HashMap();

        NodeList subnodes = node.getChildNodes();
        
        for (int c=0; c<subnodes.getLength(); c++)
        {
            Node child = subnodes.item(c);
            String name = child.getNodeName();
            if (name.equals("dctm-attr")) {                    
                DctmAttr attr = processDctmAttr(child);
                if (attrs.containsKey(attr.Name)) {
                    Object attrvalobj = attrs.get(attr.Name);
                    if (attrvalobj instanceof List) {
                        List attrvals = (List)attrs.get(attr.Name);
                        attrvals.add(attr);
                    } else if (attrvalobj instanceof DctmAttr) {
                        // hm, is repeating, even though xml didn't indicate it was...convert this to a multivalue
                        List attrvals = new ArrayList();
                        attrvals.add(attrvalobj);
                        attrvals.add(attr);
                    } else
                        throw new Exception("bad attribute type in typemap: "+attrvalobj.getClass().getName());
                    
                } else {
                    if (attr.Repeating) {
                        List attrvals = new ArrayList();
                        attrvals.add(attr);
                        attrs.put(attr.Name, attrvals);
                    } else {
                        attrs.put(attr.Name, attr);
                    }
                }
                if ("r_version_label".equals(attr.Name)) {
                    if (attr.Value.matches("[0-9]+\\.[0-9]+"))
                    {
                        attrs.put("Version_number", attr.Value);
                    } else if ("CURRENT".equals(attr.Value)) {
                        attrs.put("Version_current", "true");
                    } else {
                        attrs.put("Version_label", attr.Value);
                    }
                }
            } else if (name.equals("dctm-content-ref")) {
                // <dctm-content-ref url="file://$STAGE/Copy (5) of QAD-00010089--Documentum_Reporting_Services_Release_Notes_6.0_SP1.pdf" />
                String contentreference = child.getAttributes().getNamedItem("url").getNodeValue();
                attrs.put("dctm-content-ref", contentreference);
            }
        }

        return attrs;
    }
    

    
    
    public static DctmAttr processDctmAttr(Node node) throws Exception
    {
        // we are already at start-tag, parse the attributes
        DctmAttr attr = new DctmAttr(); 
        
        NamedNodeMap attrs = node.getAttributes();
        for (int i=0; i < attrs.getLength(); i++)
        {
            String attrname = attrs.item(i).getNodeName();
            String attrvalue = attrs.item(i).getNodeValue();
            if ("name".equals(attrname)) attr.Name = attrvalue;
            if ("repeating".equals(attrname)) attr.Repeating = true;
            if ("type".equals(attrname)) attr.Type= attrvalue;
        }
        
        attr.Value = node.getNodeValue();
        
        if (attr.Value == null)
        {
            NodeList subnodes = node.getChildNodes();
            
            for (int c=0; c<subnodes.getLength(); c++)
            {
                Node child = subnodes.item(c);
                String value = child.getNodeValue();
                if (value != null)
                    attr.Value = value;
            }
        }
        
        return attr;
        
    }

    
    
    public static void main (String[] args) throws Exception 
    {
        FileReader frdr = new FileReader("C:/apache/Tomcat 6.0/webapps/webtop/properties.xml");
        IcePayload payload = parseIcePayloadXML(frdr);
        int a=1;
        a++;
        a++;
        
    }
            
    public static void normalizeCDATASections(Node node)
    {
        for(Node node1 = node.getFirstChild(); node1 != null; node1 = node1.getNextSibling())
        {
            short word0 = node1.getNodeType();
            if(word0 == 4)
            {
                Node node2 = node1.getPreviousSibling();
                if(node2 == null)
                    continue;
                short word1 = node2.getNodeType();
                if(word1 == 4)
                {
                    CDATASection cdatasection = (CDATASection)node1;
                    CDATASection cdatasection1 = (CDATASection)node2;
                    String s = cdatasection.getData();
                    cdatasection1.appendData(s);
                    node.removeChild(node1);
                    node1 = node2;
                }
            } else
            {
                normalizeCDATASections(node1);
            }
        }

    }

    
    public static Node selectSingleNode(Node node, String s) throws TransformerException
    {
        Node node1 = null;
        try
        {
            node1 = (Node)xPath.evaluate(s, node, XPathConstants.NODE);
        }
        catch(XPathExpressionException xpathexpressionexception)
        {
            throw new TransformerException(xpathexpressionexception);
        }
        return node1;
    }


    public static NodeList selectNodeList(Node node, String s) throws TransformerException
    {
        NodeList nodelist = null;
        try
        {
            nodelist = (NodeList)xPath.evaluate(s, node, XPathConstants.NODESET);
        }
        catch(XPathExpressionException xpathexpressionexception)
        {
            throw new TransformerException(xpathexpressionexception);
        }
        return nodelist;
    }

    
}
