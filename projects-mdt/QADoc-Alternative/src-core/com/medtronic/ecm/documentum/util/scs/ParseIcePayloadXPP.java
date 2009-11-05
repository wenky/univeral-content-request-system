package com.medtronic.ecm.documentum.util.scs;

import java.io.FileReader;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import org.xmlpull.mxp1.MXParser;
import org.xmlpull.v1.XmlPullParser;

import com.medtronic.ecm.documentum.util.scs.iceobjects.DctmAttr;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IceItem;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IcePackage;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IcePayload;
import com.medtronic.ecm.documentum.util.scs.iceobjects.IceRequest;

public class ParseIcePayloadXPP {
    
    public static IcePayload parseIcePayloadXML(Reader xmldoc) throws Exception
    {
        // let's try pull-parsing
        IcePayload payload = new IcePayload();
        XmlPullParser xpp = new MXParser();
        xpp.setInput(xmldoc);
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                return payload;
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                // name --> handler mapping, but here we should know it already
                payload = processIcePayload(xpp);
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                // do nothing;
            } else if(eventType == xpp.TEXT) {
                // shouldn't happeyn
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT);
        
        return payload;
    }
    
    public static IcePayload processIcePayload(XmlPullParser xpp) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IcePayload payload = new IcePayload();
        for (int i=0; i < xpp.getAttributeCount(); i++)
        {
            String attrname = xpp.getAttributeName(i);
            String attrvalue = xpp.getAttributeValue(i);
            if ("payload-id".equals(attrname)) payload.PayloadId = attrvalue;
            if ("timestamp".equals(attrname)) payload.Timestamp = attrvalue;
            if ("ice.version".equals(attrname)) payload.Version = attrvalue;
        }
            
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                return payload;
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                if (name.equals("ice-header")) {
                    skipTag(xpp,"ice-header");
                }
                if (name.equals("ice-request")) {
                    payload.Request = processIceRequest(xpp);
                }
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                return payload;
            } else if(eventType == xpp.TEXT) {
                // do nothing and shouldn't happen
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        return null;
    }
    
    public static void skipTag(XmlPullParser xpp, String tagname) throws Exception
    {
        int stackcount = 0;
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                return;
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                stackcount++;
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                if (stackcount==0)
                    return;
                else
                    stackcount--;
            } else if(eventType == xpp.TEXT) {
                // do nothing and shouldn't happen
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        
    }


    
    public static IceRequest processIceRequest(XmlPullParser xpp) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IceRequest request = new IceRequest();
        for (int i=0; i < xpp.getAttributeCount(); i++)
        {
            // attr processing
            String attrname = xpp.getAttributeName(i);
            String attrvalue = xpp.getAttributeValue(i);
            if ("request-id".equals(attrname)) request.RequestId = attrvalue;
        }
            
        // subtag processing
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                throw new Exception("invalid ENDDOC");
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                if (name.equals("ice-package")) {
                    IcePackage pkg = processIcePackage(xpp);
                    if (request.Packages == null) request.Packages = new ArrayList<IcePackage>();
                    request.Packages.add(pkg);
                }
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                return request;
            } else if(eventType == xpp.TEXT) {
                // do nothing and shouldn't happen
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        return null;
    }

    

    public static IcePackage processIcePackage(XmlPullParser xpp) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IcePackage pkg = new IcePackage();
        for (int i=0; i < xpp.getAttributeCount(); i++)
        {
            // attr processing
            String attrname = xpp.getAttributeName(i);
            String attrvalue = xpp.getAttributeValue(i);
            if ("subscription-id".equals(attrname)) pkg.SubscriptionId = attrvalue;
            if ("package-id".equals(attrname)) pkg.PackageId = attrvalue;
        }
            
        // subtag processing
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                throw new Exception("invalid ENDDOC");
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                if (name.equals("ice-item")) {
                    IceItem item = processIceItem(xpp);
                    if (pkg.Items == null) pkg.Items = new ArrayList<IceItem>();
                    pkg.Items.add(item);
                }
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                return pkg;
            } else if(eventType == xpp.TEXT) {
                // do nothing and shouldn't happen
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        return null;
    }

    
    
    
    public static IceItem processIceItem(XmlPullParser xpp) throws Exception
    {
        // we are already at start-tag, parse the attributes
        IceItem item = new IceItem();
        for (int i=0; i < xpp.getAttributeCount(); i++)
        {
            // attr processing
            String attrname = xpp.getAttributeName(i);
            String attrvalue = xpp.getAttributeValue(i);
            if ("item-id".equals(attrname)) item.Id = attrvalue;
            if ("name".equals(attrname)) item.Name = attrvalue;
        }
            
        // subtag processing
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                throw new Exception("invalid ENDDOC");
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                if (name.equals("dctm-object")) {
                    item.DctmObject = processDctmObject(xpp);
                }
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                return item;
            } else if(eventType == xpp.TEXT) {
                // do nothing and shouldn't happen
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        return null;
    }
    

    
    public static Map<String,DctmAttr> processDctmObject(XmlPullParser xpp) throws Exception
    {
        // we are already at start-tag, parse the attributes
        Map<String,DctmAttr> attrs = new HashMap<String,DctmAttr>();
        for (int i=0; i < xpp.getAttributeCount(); i++)
        {
            // attr processing
            String attrname = xpp.getAttributeName(i);
            String attrvalue = xpp.getAttributeValue(i);
            // no attrs to process AFAIK
        }
            
        // subtag processing
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                throw new Exception("invalid ENDDOC");
            } else if(eventType == xpp.START_TAG) {
                String name = xpp.getName();
                if (name.equals("dctm-attr")) {                    
                    // ignore repeating for now
                    DctmAttr attr = processDctmAttr(xpp);
                    attrs.put(attr.Name, attr);
                }
            } else if(eventType == xpp.END_TAG) {
                String name = xpp.getName();
                return attrs;
            } else if(eventType == xpp.TEXT) {
                // do nothing and shouldn't happen
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        return null;
    }
    

    
    
    public static DctmAttr processDctmAttr(XmlPullParser xpp) throws Exception
    {
        // we are already at start-tag, parse the attributes
        DctmAttr attr = new DctmAttr(); 
        for (int i=0; i < xpp.getAttributeCount(); i++)
        {
            // attr processing
            String attrname = xpp.getAttributeName(i);
            String attrvalue = xpp.getAttributeValue(i);
            if ("name".equals(attrname)) attr.Name = attrvalue;
            if ("repeating".equals(attrname)) attr.Repeating = true;
            if ("type".equals(attrname)) attr.Type= attrvalue;
        }
            
        // subtag processing
        xpp.next();
        int eventType = xpp.getEventType();
        do {
            if(eventType == xpp.START_DOCUMENT) {
                // do nothing
            } else if(eventType == xpp.END_DOCUMENT) {
                throw new Exception("invalid ENDDOC");
            } else if(eventType == xpp.START_TAG) {
                // shouldn't be any subtags...
            } else if(eventType == xpp.END_TAG) {
               return attr;
            } else if(eventType == xpp.TEXT) {
                attr.Value = xpp.getText();  // ?CDATA compatibility?
            }
            eventType = xpp.next();
        } while (eventType != xpp.END_DOCUMENT); // should not happen...
        return null;
    }

    
    
    public static void main (String[] args) throws Exception 
    {
        FileReader frdr = new FileReader("C:/icepayload.xml");
        IcePayload payload = parseIcePayloadXML(frdr);
        int a=1;
        a++;
        a++;
        
    }
            

}
