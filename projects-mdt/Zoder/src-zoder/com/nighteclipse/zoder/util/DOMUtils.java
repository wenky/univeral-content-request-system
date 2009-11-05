package com.nighteclipse.zoder.util;

import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import com.zoder.util.Lg;

public class DOMUtils 
{
    public static Node get(Node n, String name) 
    {
        NodeList nl = n.getChildNodes();
        if (nl == null) {
            /*-dbg-*/Lg.log("Node %s has no children", n.getNodeName());
            return null;
        }
        for (int i=0; i < nl.getLength(); i++) {
            Node c = nl.item(i);
            String cn = c.getNodeName();
            if (name.equals(cn)) {
                return c;
            }
        }
        return null;
    }

    public static Node getmake(Node n, String name) 
    {
        NodeList nl = n.getChildNodes();
        if (nl == null) {
            /*-dbg-*/Lg.log("Node %s has no children", n.getNodeName());
            Node newnode = n.getOwnerDocument().createElement(name);
            n.appendChild(newnode);
            return newnode;
        }
        for (int i=0; i < nl.getLength(); i++) {
            Node c = nl.item(i);
            String cn = c.getNodeName();
            if (name.equals(cn)) {
                return c;
            }
        }
        Node newnode = n.getOwnerDocument().createElement(name);
        n.appendChild(newnode);
        return newnode;
    }
    
    public static Node newElement(Node n, String name) {
        if (n instanceof Document) {
            Document doc = (Document)n;
            Node newnode = doc.createElement(name);
            return newnode;
        }
        Node newnode = n.getOwnerDocument().createElement(name);
        return newnode;
    }

    public static Node newElement(Node n, String name, String value) {
        if (n instanceof Document) {
            Document doc = (Document)n;
            Node newnode = doc.createElement(name);
            newnode.appendChild(doc.createTextNode(value));
            return newnode;
        }
        Node newnode = n.getOwnerDocument().createElement(name);
        newnode.appendChild(n.getOwnerDocument().createTextNode(value));
        return newnode;
    }

    public static Node newAttribute(Node n, String name, String value) {
        if (n instanceof Document) {
            Document doc = (Document)n;
            Node newnode = doc.createAttribute(name);
            newnode.setTextContent(value);
            return newnode;
        }
        Node newnode = n.getOwnerDocument().createAttribute(name);
        newnode.setTextContent(value);
        return newnode;
    }

    // map/dictionary style: replace if already there (first match) or append
    public static void put(Node n, String name, String value)
    {
        NodeList nl = n.getChildNodes();
        if (nl == null) {
            Node newnode = newElement(n,name,value);
            n.appendChild(newnode);
            return;
        }
        for (int i=0; i < nl.getLength(); i++) {
            Node cn = nl.item(i);
            String nodename = cn.getNodeName();
            if (name.equals(nodename)) {
                Node newnode = newElement(n,name,value);
                n.replaceChild(newnode, cn);
                return;
            }
        }
        Node newnode = newElement(n,name,value);
        n.appendChild(newnode);
        
    }

}
