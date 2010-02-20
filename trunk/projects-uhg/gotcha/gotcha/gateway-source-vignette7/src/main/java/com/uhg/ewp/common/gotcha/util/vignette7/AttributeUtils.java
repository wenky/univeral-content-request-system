package com.uhg.ewp.common.gotcha.util.vignette7;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.util.log.Lg;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.DataType;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.dbms.VgnConnectionPool;
import com.vignette.util.VgnException;

public class AttributeUtils
{
    private static final String PREFIX_CONTENT = "content";
    private static final String PREFIX_LARGEOBJECT = "largeobject";
    private static final String SUFFIX_GUID = "guid";


    public static Object getSimpleAttributeValue(ContentInstance ci, String attrname) throws ApplicationException
    {
        AttributeData attrdata = ci.getAttribute(attrname);
        if (attrdata != null) {
            Object valueobj = attrdata.getValue();
            return valueobj;
        }        
        return null;         
    }

    public static ContentMetaData getSimpleAttributeMetaData(ContentInstance ci, String attrname) throws ApplicationException
    {
        AttributeData attrdata = ci.getAttribute(attrname);
        if (attrdata != null) {
            BaseContentMetaData metadatavalue = new BaseContentMetaData();
            Object valueobj = attrdata.getValue();
            metadatavalue.setSingleValue(valueobj);
            metadatavalue.setName(attrname);
            DataType type = attrdata.getDataType();
            if (DataType.STRING.equals(type)) {
                metadatavalue.setType(String.class);
            } else if (DataType.INT.equals(type)) {
                metadatavalue.setType(Integer.class);
            } else {
                metadatavalue.setType(String.class);
                metadatavalue.setSingleValue(valueobj.toString());
            }
            return metadatavalue;
        }        
        return null;         
    }

    
    public static String getSimpleContent(ContentInstance ci, String contentattrname) throws ApplicationException
    {
        Object[] relationalresponse = getRelationalContentAndMetaDataAttributeValues(ci,contentattrname,null);
        List contentresponse = (List)relationalresponse[0];
        
        // do simple attributes
        
        Object contentsimple = getSimpleAttributeValue(ci,contentattrname);
        if (contentsimple != null) {
            contentresponse.add(contentsimple);
        }

        if (contentresponse.size() == 0) {
            return null;
        } else {
            if(Lg.trc())Lg.trc("number of content responses: %d",contentresponse.size());
            return contentresponse.get(0).toString(); 
        }
    }
    
    
    public static Object[] getContentAndMetaDataAttributeValues(ContentInstance ci, String contentattrname, Set<String> attrnames) throws ApplicationException
    {
        Object[] relationalresponse = getRelationalContentAndMetaDataAttributeValues(ci,contentattrname,attrnames);
        List contentresponse = (List)relationalresponse[0];
        Map<String,ContentMetaData> attrresponse = (Map<String,ContentMetaData>)relationalresponse[1];
        
        // do simple attributes
        
        Object contentsimple = getSimpleAttributeValue(ci,contentattrname);
        if (contentsimple != null) {
            contentresponse.add(contentsimple);
        }
        
        // simple attrvalues supercede relationals?
        if (attrnames != null) {
            for (String attrname : attrnames) {
                ContentMetaData simplefield = getSimpleAttributeMetaData(ci,attrname);
                if (simplefield != null) {
                    attrresponse.put(attrname,simplefield);
                }
            }
        }
        
        return relationalresponse;
        
    }


    /**
     * Scans the relations for a given ContentInstance, examining the attributes. If any of the attribute names matches the
     * one of the attributes in the provided set of attribute names, the value is added to that attribute's value list. The
     * method assumes that most of the time only a single primary content attribute value is being sought, btut more complicated
     * retrieval use cases may require other metadata such as sort order and security groups to be also retrieved.
     * 
     * There are presumed performance advantages to doing all these at once in a single pass through a ContentInstance's relations
     * 
     * @param ContentInstance - fully-retrieved ContentInstance 
     * @param String - attribute name being searched on in provided ContentInstance's relations
     * @param Set - optional set of additional metadata fields to retrieve while doing the primary search.
     * @return Object[List , Map<String,List>] - on success, [0] holds the content attribute values and [1] has the metadata values map 
     */
    public static Object[] getRelationalContentAndMetaDataAttributeValues(ContentInstance ci, String contentattrname, Set<String> attrnames) throws ApplicationException
    {
        List<String> values = new ArrayList<String>();        
        
        Map<String,ContentMetaData> attrvalues = null;
        if (attrnames != null) {
            attrvalues = new HashMap<String,ContentMetaData>();                
        }
            
        // try to locate the relational value
        Map mapAllRelations = ci.getAllRelations();
        if ( mapAllRelations != null ) {                                
            for (Object relationKey :  mapAllRelations.keySet() ) 
            {
                String relationName = (String)relationKey;
                Lg.trc("Looking at relation %s",relationName);
                AttributedObject[] arrRelations = (AttributedObject[])mapAllRelations.get(relationName);
                if ( arrRelations != null ) 
                {                        
                    for (AttributedObject relation : arrRelations) 
                    {
                        AttributeData[] arrRelAttr = relation.getAttributes();
                        if ( arrRelAttr != null ) 
                        {                                
                            for (AttributeData relationalAttribute : arrRelAttr) 
                            {                                    
                                if ( relationalAttribute != null ) 
                                {                                        
                                    if ( isRelationalAttribute(relationalAttribute) ) 
                                    {
                                        String relationalAttributeName = relationalAttribute.getName();
                                        Lg.trc("Checking at relation attr for match %s",relationalAttributeName);

                                        if (contentattrname != null && StringUtils.equals(relationalAttributeName, contentattrname)) {
                                            Lg.trc("-- matched content attribute");
                                            String relAttrValue = getRelationalValue(relationalAttribute);
                                            Lg.trc("Adding relation attr value %s",relAttrValue);                                        
                                            values.add(relAttrValue);
                                        }                                            
                                        
                                        if (attrnames != null && attrnames.contains(relationalAttributeName)) {                                            
                                            Lg.trc("-- matched metadata attribute");
                                            BaseContentMetaData metadatafield = (BaseContentMetaData)attrvalues.get(relationalAttributeName);
                                            if (metadatafield == null) {
                                                metadatafield = new BaseContentMetaData();
                                                metadatafield.setName(relationalAttributeName);
                                                metadatafield.setType(String.class);
                                                metadatafield.setRepeatingValues(new ArrayList());
                                                attrvalues.put(relationalAttributeName, metadatafield);
                                            } 
                                            String relAttrValue = getRelationalValue(relationalAttribute);
                                            metadatafield.getRepeatingValues().add(relAttrValue);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        
        return new Object[] {values,attrvalues};
        
    }

    

    public static boolean isRelationalAttribute(AttributeData inRelationalAttribute) 
    {
        boolean returnValue = hasPrefix(inRelationalAttribute.getName(), PREFIX_CONTENT) || hasPrefix(inRelationalAttribute.getName(), PREFIX_LARGEOBJECT);

        // return negation of having prefix of content or large object...
        return !returnValue;
    }
    
    public static List decodeGuidValues(List guiddata)
    {
        List decodes = new ArrayList();
        for (Object val : guiddata)
        {
            String guid = val.toString();
            String decode = guid;
            decode = getSystemValue(guid);                
            decodes.add(decode);
        }
        return decodes;
    }

    public static void decodeGuidMetaData(BaseContentMetaData guiddata)
    {
        List guids = (List)guiddata.getRepeatingValues();
        if (guids != null) {
            List decodes = new ArrayList();
            for (Object val : guids)
            {
                String guid = val.toString();
                String decode = getSystemValue(guid);                
                decodes.add(decode);
            }
            guiddata.setRepeatingValues(decodes);
        }
    }

    
    public static String getRelationalValue(AttributeData inAttributeData) 
    {
        String returnValue = "";
        
        // need to determine whether the value of the supplied attribute is a GUID
        if ( isGuid(inAttributeData.getValue()) ) {
            returnValue = getSystemValue((String)inAttributeData.getValue());
        } else {
            returnValue = (String)inAttributeData.getValue();
        }
        
        return returnValue;
    }
    
    public static String getSystemValue(String inGuid) 
    {
        String returnValue = "";
        String recordId = "";

        try {
            recordId = getRecordId(inGuid);
            returnValue = getManagedObjectName(recordId);
        } catch ( RemoteException re )  {
            throw new SourceRetrievalException("V7ATTRUTILS-SYSVAL-REMOTE",Lg.err("Unexpected RemoteException", re),re);
        } catch ( SQLException sqle )  {
            throw new SourceRetrievalException("V7ATTRUTILS-SYSVAL-SQL",Lg.err("Unexpected SQLException", sqle),sqle);
        } catch ( ValidationException ve )  {
            throw new SourceRetrievalException("V7ATTRUTILS-SYSVAL-VE",Lg.err("Unexpected ValidationException", ve),ve);
        } catch ( ApplicationException ae )  {
            throw new SourceRetrievalException("V7ATTRUTILS-SYSVAL-AE",Lg.err("Unexpected ApplicationException", ae),ae);
        } catch ( VgnException vgne )  {
            throw new SourceRetrievalException("V7ATTRUTILS-SYSVAL-VGNERR",Lg.err("Unexpected VgnException", vgne),vgne);
        }
        
        return returnValue;
    }
    
    
    /**
     * Returns from Vignette's VGNASMOMAP table the record id associated with the supplied GUID.
     * If the parameter is null or otherwise invalid, then an empty String object is returned.
     * @param String - object holding a Vignette GUID.
     * @return String - object holding the record id. 
     */
    public static String getRecordId(String inGuid) throws VgnException, ValidationException, SQLException, RemoteException 
    {
        
        String returnValue = "";                
        String sql = "SELECT DISTINCT recordId FROM vgnAsMoMap WHERE vgnAsMoMap.keystring1 = ?";
        
        ArrayList cts = new ArrayList();
        
        Connection con = null;
        ResultSet rs = null;
        PreparedStatement stmt = null;
        try { 
        
            con = VgnConnectionPool.getInstance("AppSvcs Resource").getConnection();
            
            Lg.trc("exec prep stmt: %s for guid %s",sql,inGuid);
            stmt = con.prepareStatement(sql);
            stmt.setString(1,inGuid);
            
            rs = stmt.executeQuery();
            while (rs.next()) {
                String id = rs.getString("recordId");
                Lg.trc("-- resultset val: %s",id);
                cts.add(id);
            }
        } finally {        
            try{rs.close();}catch(Throwable t){Lg.trc("exception in finally - resultset closing",t);}
            try{stmt.close();}catch(Throwable t){Lg.trc("exception in finally - prep statement closing",t);}
            try{con.close();}catch(Throwable t){Lg.trc("exception in finally - connection closing",t);}
        }
        
        if ( cts.size() == 1 ) {
            returnValue = (String)cts.get(0);
        } else {
            throw new SourceRetrievalException("V7ATTRUTILS-GETRECID-TOOMANY",Lg.err("Yikes! Found "+cts.size()+" record ids; should be 1 record and 1 record only!"));
        }
        
        
        return returnValue;
    }
    
    
    /**
     * Returns the name of the Vignette Managed Object referenced by the supplied record id.
     * @param String - Object holding the Vignette Record Id. 
     * @return String - Object holding the name of the referenced Managed Object.
     */
    public static String getManagedObjectName(String inRecordId) 
    {        
        String returnValue = "";
                
        try {
            Lg.trc("attempt location of record id: %s",inRecordId);
            ManagedObjectVCMRef moRef = new ManagedObjectVCMRef(inRecordId);
            ManagedObject mo = ManagedObject.findByContentManagementId(moRef);
            
            if ( mo != null ) {
                returnValue = mo.getName();
                Lg.trc("located managed object named: %s",returnValue);
            }
        } catch ( ValidationException ve )  {
            throw new SourceRetrievalException("V7ATTRUTILS-GETOBJNAME-VE",Lg.err("Unexpected ValidationException", ve),ve);
        } catch ( ApplicationException ae )  {
            throw new SourceRetrievalException("V7ATTRUTILS-GETOBJNAME-AE",Lg.err("Unexpected ApplicationException", ae),ae);
        }
                
        return returnValue;
    }


    /**
     * Returns true if the supplied object is determined to be a Vignette GUID.
     * @param Object - object being tested.
     * @return boolean
     */
    public static boolean isGuid(Object inObject) 
    {
        if ( inObject instanceof String ) {
            boolean hassuffix = hasSuffix((String)inObject, SUFFIX_GUID);
            return hassuffix;
        }
        
        return false;
    }

    
    /**
     * Returns true if the supplied String value begins with the supplied prefix.
     * @param String - object being tested.
     * @param String - object holding prefix for which to test.
     * @return boolean 
     */
    public static boolean hasPrefix(String inString, String inPrefix) 
    {
        boolean returnValue =  inString.length() > inPrefix.length() && inPrefix.equalsIgnoreCase(inString.substring(0, inPrefix.length()));                
        return returnValue;
    }
    
    
    /**
     * Returns true if the supplied String value ends with the supplied suffix.
     * @param String - object being tested.
     * @param String - object holding suffix for which to test.
     * @return boolean 
     */
    public static boolean hasSuffix(String inString, String inSuffix) 
    {
        boolean returnValue = inString.length() > inSuffix.length() && inSuffix.equalsIgnoreCase(inString.substring(inString.length() - inSuffix.length()));
        return returnValue;
    }

    
}
