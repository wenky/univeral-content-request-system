package com.uhg.ewp.common.gotcha.util.vignette7.introspect;

import java.rmi.RemoteException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.log4j.Logger;

import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.common.ref.ManagedObjectVCMRef;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ContentType;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.ManagedObject;
import com.vignette.as.client.javabean.Site;
import com.vignette.dbms.VgnConnectionPool;
import com.vignette.util.VgnException;

/**
 * This class provides methods for retrieving content served by a Vignette Content server.
 * These methods hide the low-level server interaction to provide just the desired content.
 * 
 * @version 0.05
 * @author Thomas Griffin
 */
public class TomContentRetriever {
    
    //reference to the Logging Framework
    private static final Logger m_logger = Logger.getLogger(TomContentRetriever.class.getName());
    
    private static final String PREFIX_CONTENT = "content";
    private static final String PREFIX_LARGEOBJECT = "largeobject";
    private static final String SUFFIX_GUID = "guid";

    /**
     * Constructor
     */
    public TomContentRetriever() { 
    }
    
    
    /**
     * Returns a Channel object that is the system-provided Channel object for the supplied
     * Site name parameter.  If no such object is found, then an empty Channel object is returned.
     * @param String - name of Site
     * @return Channel - object representing system-supplied Home channel.
     */
    public static Channel getSiteHomeChannel(String inSiteName) {
        Channel returnValue = new Channel();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            // Get the home channel for this site
            m_logger.debug("Retrieving home channel for Site " + inSiteName + "...");
            Site site = Site.findByName(inSiteName);
            Channel homeChannel = site.getHomeChannel();
            
            if ( homeChannel != null ) {
                returnValue = homeChannel;
            }
            
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing as its values active Channel objects that are direct sub-channels to the
     * system-provided Home Channel object for the supplied Site name parameter.  Each Channel object is
     * keyed by the name of the object.  If no such objects are found, then an empty map is returned.
     * @param String - name of Site 
     * @return Map - collection of Channel objects, keyed by the object's name.
     */
    public static Map getActiveHomeChannels(String inSiteName) {
        Map returnValue = new HashMap();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        // Get the home channel for this site
        m_logger.debug("Retrieving home channel for Site " + inSiteName + "...");
        Channel homeChannel = getSiteHomeChannel(inSiteName);
        
        // Get the active top-level sub-channels of the home channel
        if ( homeChannel != null ) {
            returnValue = getActiveSubChannels(homeChannel);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing as its values active Channel objects that are direct sub-channels to the
     * supplied Channel parameter.  Each Channel object is keyed by the name of the object.  If no
     * such objects are found, then an empty map is returned.
     * @param Channel - location to be searched. 
     * @return Map - collection of Channel objects, keyed by the object's name.
     */
    public static Map getActiveSubChannels(Channel inChannel) {
        Map returnValue = new HashMap();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            m_logger.debug("Retrieving active subchannels for Channel " + inChannel + "...");
            
            // Get the active top-level sub-channels of the given channel
            RequestParameters reqParams = RequestParameters.getImmutableInstanceTopRelationOnly();
            Channel[] arrSubChannel = inChannel.getActiveSubchannels(reqParams);
            List lstSubChannel = Arrays.asList(arrSubChannel);
            
            Iterator channelIter = lstSubChannel.iterator();
            while ( channelIter.hasNext() ) {
                // place the channel in the map and key it by the channel's name
                Channel channel = (Channel)channelIter.next();
                String channelName = channel.getData().getName();
                returnValue.put(channelName, channel);
            }
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing as its values ContentType objects found in the specified Channel.
     * Each ContentType object is keyed by the name of the object.  If no such objects are found,
     * then an empty map is returned.
     * @param Channel - location to be searched. 
     * @return Map - collection of ContentType objects, keyed by the object's name.
     */
    public static Map getContentTypes(Channel inChannel) {
        Map returnValue = new HashMap();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            Map mapContentInstances = getContentInstancesForAllTypes(inChannel);
            if ( mapContentInstances != null && !mapContentInstances.isEmpty() ) {
                Set ciTypeNames = mapContentInstances.keySet();
                Iterator typeIter = ciTypeNames.iterator();
                while ( typeIter.hasNext() ) {
                    String typeName = (String)typeIter.next();
                    ContentType contentType = (ContentType)ContentType.findByName(typeName);
                    // due to API hiccup with object typecasting, ensure acquisition of entire object via VCMId.
                    contentType = (ContentType)ContentType.findByContentManagementId(contentType.getContentManagementId());
                    
                    returnValue.put(typeName, contentType);
                }
            }
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a List of ContentInstance objects of the specified ContentTypeName found in the specified Channel.
     * If no such objects are found in the specified channel, then an empty map is returned.
     * 
     * NOTE: Each of the ContentInstance objects contained in the map being returned may need to be
     *       validated before use.  Please see validateContentInstance() for details.
     *       
     * @param Channel - location in which the ContentInstance objects being sought reside. 
     * @param String - name of type of ContentInstance objects being sought.
     * @return List - collection of ContentInstance objects.
     */
    public static List getContentInstances(Channel inChannel, String inContentTypeName) {
        
        List returnValue = new ArrayList();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        Map mapAllChannelContent = getContentInstancesForAllTypes(inChannel);
        if ( mapAllChannelContent != null && !mapAllChannelContent.isEmpty()) {
            if ( mapAllChannelContent.containsKey(inContentTypeName) ) {
                returnValue = (List)mapAllChannelContent.get(inContentTypeName); 
            }
        }
        
        //log exit from this method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing Lists of ContentInstance objects with each list keyed by the Internal Name.
     * Each object found in the specified Channel has as its ContentType that named by ContentTypeName.
     * If no objects are found, then an empty map is returned.
     * 
     * NOTE: Each of the ContentInstance objects contained in the map being returned may need to be
     *       validated before use.  Please see validateContentInstance() for details.
     *       
     * @param Channel - location in which the ContentInstances being sought reside.
     * @param String - name of type of ContentInstance objects being sought.
     * @return Map - collection of Lists, each keyed by the Internal Name of that List's contained objects.
     */
    public static Map getNamedContentInstancesByType(Channel inChannel, String inContentTypeName) {
        
        Map returnValue = new HashMap();
        List lstContentInstances = new ArrayList();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            
            Map mapAllChannelContent = getContentInstancesForAllTypes(inChannel);
            
            if ( mapAllChannelContent != null && !mapAllChannelContent.isEmpty() ) {
                if ( mapAllChannelContent.containsKey(inContentTypeName) ) {
                    lstContentInstances = (List)mapAllChannelContent.get(inContentTypeName);
                    
                    Iterator ciIter = lstContentInstances.iterator();
                    while ( ciIter.hasNext() ) {
                        ContentInstance ci = (ContentInstance)ciIter.next();
                        
                        if ( returnValue.containsKey(ci.getName()) ) {
                            List lstInstance = (List)returnValue.get(ci.getName());
                            lstInstance.add(ci);
                        } else {
                            List lstInstance = new ArrayList();
                            lstInstance.add(ci);
                            returnValue.put(ci.getName(), lstInstance);
                        }
                    }
                }
            }
        } catch (ApplicationException ae) {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns from the specified Channel a ContentInstance that has the specified Internal Name
     * and is of the ContentType per the supplied ContentTypeName.  If more ContentInstance objects
     * have values matching those of the supplied parameters, then the first object found is returned. 
     * If no such object is located, then an empty ContentInstance is returned.
     * 
     * NOTE: The ContentInstance object being returned may need to be validated before use.
     *       Please see validateContentInstance() for details.
     *       
     * @param Channel - location in which the ContentInstance being sought resides.
     * @param String - name of type of ContentInstance object being sought.
     * @param String - internal name applied to ContentInstance being sought.
     * @return ContentInstance - object to be found given the provided parameters.
     */
    public static ContentInstance getContentInstance(Channel inChannel, String inContentTypeName, String inInternalName) {
        ContentInstance returnValue = new ContentInstance();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            // Get all content instances (CIs) in the specified channel
            m_logger.debug("Getting content instances for Channel " + inChannel.getData().getName() + "...");
            RequestParameters reqParams = RequestParameters.getImmutableInstanceTopRelationOnly();
            IPagingList iPagingList = inChannel.getContentInstances(reqParams);
            List lstContentInstance = new ArrayList();
            if ( iPagingList != null && iPagingList.size() > 0 ) {
                lstContentInstance = iPagingList.asList();
            }
            
            Iterator ciIter = lstContentInstance.iterator();
            while ( ciIter.hasNext() ) {
                ContentInstance ci = (ContentInstance)ciIter.next();
                
                if ( inContentTypeName.equals(ci.getObjectType().getName()) ) {
                    if ( inInternalName.equals(ci.getName()) ) {
                        m_logger.debug("Content Type and Name match supplied parameters - Type: "+ci.getObjectType().getName()+" / Name: "+ci.getName());
                        returnValue = ci;
                        break;
                    }
                }
                
            }
            
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
            
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing as its values Lists of ContentInstance objects found in the
     * specified Channel.  Each list is keyed by the name of type of the contained ContentInstances.
     * If there are no objects in the specified channel, then an empty map is returned.
     * 
     * NOTE: Each of the ContentInstance objects contained in the map being returned may need to be
     *       validated before use.  Please see validateContentInstance() for details.
     *       
     * @param Channel - location in which ContentInstance objects being sought reside 
     * @return Map - collection of Lists, each keyed by the name of the type of that List's contained ContentInstance objects.
     */
    public static Map getContentInstancesForAllTypes(Channel inChannel) {
        Map returnValue = new HashMap();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            // Get all content instances (CIs) in the specified channel
            m_logger.debug("Getting content instances for Channel " + inChannel.getData().getName() + "...");
            RequestParameters reqParams = RequestParameters.getImmutableInstanceTopRelationOnly();
            IPagingList iPagingList = inChannel.getContentInstances(reqParams);
            List lstContentInstance = new ArrayList();
            if ( iPagingList != null && iPagingList.size() > 0 ) {
                lstContentInstance = iPagingList.asList();
            }
            
            // Map CIs according to content type using the names of the content types as "keys".
            Iterator ciIter = lstContentInstance.iterator();
            while ( ciIter.hasNext() ) {
                ContentInstance ci = (ContentInstance)ciIter.next();
                
                String objTypeName = ci.getObjectType().getName();
                
                if ( returnValue.containsKey(objTypeName) ) {
                    List lstTypeInstance = (List)returnValue.get(objTypeName);
                    lstTypeInstance.add(ci);
                } else {
                    List lstTypeInstance = new ArrayList();
                    lstTypeInstance.add(ci);
                    returnValue.put(objTypeName, lstTypeInstance);
                }
            }
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
            
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Vignette Channel object representing the last channel in the supplied Vignette channel path.
     * If one of the channels specfied in the supplied path does not exist, the return value will be an empty object.
     * @param Channel - location from which the supplied channel path extends
     * @param String - path consisting of names of Vignette Channels separated by "/" (ex. path/to/channel).  The path
     *                 must not begin or end with "/".
     * @return Channel - object representing the last channel entity in the supplied channel path
     */
    public static Channel findChannelByPath(Channel inChannel, String inChannelPath) throws VgnException, ValidationException {
        
        Channel returnValue = new Channel();

        //log entrance into this method
        m_logger.debug("Enter Method");
        
        String[] arrSubChannelPath = inChannelPath.split("/");
        List lstSubChannelPath = new ArrayList();
        if ( arrSubChannelPath != null && arrSubChannelPath.length > 0 ) {
            lstSubChannelPath = Arrays.asList(arrSubChannelPath);
        }
        
        m_logger.debug(lstSubChannelPath.size() + " subchannel(s) found in supplied path.");
        
        returnValue = inChannel;
        Channel subChannel = new Channel();
        RequestParameters pathChannelParams = RequestParameters.getImmutableInstanceTopRelationOnly();
        
        // get a list of subchannels for each channel name contained in the path
        Iterator scPathIter = lstSubChannelPath.iterator();
        while ( scPathIter.hasNext() ) {
            m_logger.debug("Current Channel: "+returnValue.getData().getName());
            
            String scpChannelName = (String)scPathIter.next();
            
            m_logger.debug("Channel being sought in Sub-Channel Path: "+scpChannelName);
            
            // Get the top-level sub-channels of the currently supplied channel
            List lstSubChannel = Arrays.asList(returnValue.getActiveSubchannels(pathChannelParams));
            
            Iterator subChanIter = lstSubChannel.iterator();
            while ( subChanIter.hasNext() ) {
                subChannel = (Channel)subChanIter.next();
                
                String subChannelName = subChannel.getData().getName();
                m_logger.debug("Sub-Channel Name: "+subChannelName);
                
                if ( scpChannelName.equals(subChannelName) ) {
                    m_logger.debug("Successful Match! Processing of sub-channels for "+returnValue.getData().getName()+" should now stop");
                    returnValue = subChannel;
                    break;
                }
            }
        }
        
        //log exit from this method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a "complete" ContentInstance object based upon that instance's Content Management Id.
     * If the parameter is null or otherwise invalid, then an empty ContentInstance object is returned.
     * 
     * NOTE: Due to a reference issue/bug in the Vignette API, any ContentInstance (CI) retrieved that
     *       has attributes or relations destined for additional processing must be validated using this
     *       method.  As this method does affect performance, it should should be used only when necessary. 
     *       
     * @param ContentInstance - object holding the Content Management Id being sought.
     * @return ContentInstance - object complete with all of its attributes and relations.
     */
    public static ContentInstance validateContentInstance(ContentInstance inInstance) {
        ContentInstance returnValue = new ContentInstance();
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            ContentInstance ci = (ContentInstance)ContentInstance.findByContentManagementId(inInstance.getContentManagementId());
            
            if ( ci != null ) {
                returnValue = ci;
            }
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from this method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing as its values Lists of String objects representing attribute values.
     * Each list is keyed by the name of attribute.  If there are no values for that attribute,
     * then that attribute is not placed in the Map.  If there are no attributes for the supplied Content
     * Instance, then an empty map is returned.
     * @param ContentInstance - object holding the Content Management Id being sought.
     * @return Map - collection of Lists, each keyed by the name of the applicable attribute.
     */
    public static Map getAllAttributes(ContentInstance inInstance) {
        Map returnValue = new HashMap();
        
        //log entrance into method
        m_logger.debug("Enter Method");
        
        try {
            m_logger.debug("Attempting to get attribute data for " + inInstance.getName() + "...");
            
            AttributeData[] arrAttributes = inInstance.getAttributes();
            if ( arrAttributes != null && arrAttributes.length > 0 ) {
                List lstAttributes = Arrays.asList(arrAttributes);
                
                m_logger.debug("Content Instance < " + inInstance.getName() + " > contains the following " + lstAttributes.size() + " direct attributes:" );
                
                Iterator attribIter = lstAttributes.iterator();
                while ( attribIter.hasNext() ) {
                    AttributeData attribute = (AttributeData)attribIter.next();
                    
                    // Store attribute data. If attribute is null, it will not be recorded (should never happen!!!).
                    if ( attribute != null ) {
                        m_logger.debug("Attribute Name: " + attribute.getName() + "   Value: " + attribute.getValue());
                        
                        // Place values in the map being returned
                        if ( returnValue.containsKey(attribute.getName()) ) {
                            List lstValues = (List)returnValue.get(attribute.getName());
                            lstValues.add(attribute.getValue());
                        } else {
                            List lstValues = new ArrayList();
                            lstValues.add(attribute.getValue());
                            returnValue.put(attribute.getName(), lstValues);
                        }
                    } else {
                        m_logger.debug("!!!  Attribute found to be null - Should NEVER happen!  !!!");
                    }
                }
            } else {
                m_logger.debug("!!!  Cannot retrieve attributes for " + inInstance.getName() + "  !!!");
            }
            
            // get relational attributes and put them in the map being returned
            returnValue = getAllRelations(inInstance, returnValue);
            
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns a Map containing as its values Lists of String objects representing relational attribute values.
     * Each list is keyed by the name of attribute.  If there are no values for that relational attribute,
     * then that attribute is not placed in the Map.  If there are no attributes for the supplied Content
     * Instance, then the returned Map will contain only those objects in the supplied Map.
     * @param ContentInstance - object holding the Content Management Id being sought.
     * @return Map - collection of Lists, each keyed by the name of the applicable attribute.
     */
    private static Map getAllRelations(ContentInstance inInstance, Map inAttribute) {
        
        // Log entrance into method
        m_logger.debug("Enter Method");
        
        // Set the map being returned to the supplied attribute map 
        Map returnValue = inAttribute;
        
        try {
            m_logger.debug("Attempting to get all relation data for " + inInstance.getName() + "...");
            
            Map mapAllRelations = inInstance.getAllRelations();
            if ( mapAllRelations != null && mapAllRelations.size() > 0 ) {
                m_logger.debug("Content Instance < " + inInstance.getName() + " > contains " + mapAllRelations.size() + " relations." );
                
                // Display each relation name for debug purposes
                Set setRelationNames = mapAllRelations.keySet();
                m_logger.debug("Relations are: ");
                
                Iterator relationsIter = setRelationNames.iterator();
                while ( relationsIter.hasNext() ) {
                    String relationName = (String)relationsIter.next();
                    m_logger.debug(">>>  " + relationName);
                    
                    AttributedObject[] arrRelations = (AttributedObject[])mapAllRelations.get(relationName);
                    if ( arrRelations != null && arrRelations.length > 0 ) {
                        m_logger.debug("   -- contains " + arrRelations.length + " data objects.");
                        
                        List lstRelations = Arrays.asList(arrRelations);
                        Iterator iterRelations = lstRelations.iterator();
                        int i = 0;
                        while ( iterRelations.hasNext() ) {
                            AttributedObject relation = (AttributedObject)iterRelations.next();
                            
                            i++; // counter for data object debugging
                            m_logger.debug("   Data Object "+i);
                            
                            AttributeData[] arrRelAttr = relation.getAttributes();
                            if ( arrRelAttr != null && arrRelAttr.length > 0 ) {
                                List lstRelAttr = Arrays.asList(arrRelAttr);
                                Iterator relAttrIter = lstRelAttr.iterator();
                                
                                while ( relAttrIter.hasNext() ) {
                                    AttributeData relationalAttribute = (AttributeData)relAttrIter.next();
                                    
                                    // Store attribute data. If attribute is null, it will not be recorded (should never happen!!!).
                                    if ( relationalAttribute != null ) {
                                        
                                        // if valid relational attribute, place it in map being returned
                                        if ( isRelationalAttribute(relationalAttribute) ) {
                                            
                                            String relAttrValue = getValue(relationalAttribute);
                                            
                                            m_logger.debug("    -- Relational Attribute Name: " + relationalAttribute.getName() + "   Value: " + relAttrValue);
                                            
                                            // Place value in the map being returned
                                            if ( returnValue.containsKey(relationalAttribute.getName()) ) {
                                                List lstValues = (List)returnValue.get(relationalAttribute.getName());
                                                lstValues.add(relAttrValue);
                                            } else {
                                                List lstValues = new ArrayList();
                                                lstValues.add(relAttrValue);
                                                returnValue.put(relationalAttribute.getName(), lstValues);
                                            }
                                        }
                                    } else {
                                        m_logger.debug("!!!  Relational Attribute found to be null - Should NEVER happen!  Attribute not recorded! !!!");
                                    }
                                }
                            } else {
                                m_logger.debug("!!!  Relation " + relationName + " does not have attributes.  !!!");
                            }
                        }
                    } else {
                        m_logger.debug("   -- contains no relations.");
                    }
                }
            } else {
                m_logger.debug("!!!  Cannot retrieve relations for " + inInstance.getName() + "  !!!");
            }
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns true if the supplied object is determined to be a Vignette GUID.
     * @param Object - object being tested.
     * @return boolean
     */
    private static boolean isGuid(Object inObject) {
        boolean returnValue = false;
        
        if ( inObject instanceof String ) {
            if ( hasSuffix((String)inObject, SUFFIX_GUID, false) ) {
                returnValue = true;
            }
        }
        
        return returnValue;
    }

    /**
     * Returns the actual value referenced by the supplied AttributeData object.
     * @param AttributeData - relation attribute object.
     * @return String -  Vignette system-stored value
     */
    private static String getValue(AttributeData inAttributeData) {
        String returnValue = "";
        
        // need to determine whether the value of the supplied attribute is a GUID
        if ( isGuid(inAttributeData.getValue()) ) {
            returnValue = getSystemValue((String)inAttributeData.getValue());
        } else {
            returnValue = (String)inAttributeData.getValue();
        }
        
        return returnValue;
    }
    
    /**
     * Returns true if the supplied relational attribute is valid.
     * @param String - Vignette Guid mapped to unique Vignette record id.
     * @return String - name of a Vignette Managed Object.
     */
    private static String getSystemValue(String inGuid) {
        String returnValue = "";
        String recordId = "";
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            recordId = getRecordId(inGuid);
            returnValue = getManagedObjectName(recordId);
        } catch ( RemoteException re )  {
            m_logger.error("Unexpected RemoteException", re);
        } catch ( SQLException sqle )  {
            m_logger.error("Unexpected SQLException", sqle);
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        } catch ( VgnException vgne )  {
            m_logger.error("Unexpected VgnException", vgne);
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns true if the supplied relational attribute is valid.
     * @param AttributeData - relation attribute object.
     * @return boolean 
     */
    private static boolean isRelationalAttribute(AttributeData inRelationalAttribute) {
        boolean returnValue = false;
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        returnValue = hasPrefix(inRelationalAttribute.getName(), PREFIX_CONTENT, returnValue);
        returnValue = hasPrefix(inRelationalAttribute.getName(), PREFIX_LARGEOBJECT, returnValue);
        
        // if the current value to return is true, then the relational attribute is NOT valid.
        // The use of the the NOT operator provides the proper boolean value to actually return.
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return !returnValue;
    }

    /**
     * Returns true if the supplied String value begins with the supplied prefix.
     * @param String - object being tested.
     * @param String - object holding prefix for which to test.
     * @param boolean
     * @return boolean 
     */
    private static boolean hasPrefix(String inString, String inPrefix, boolean inBool) {
        boolean returnValue = false;
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        if ( inBool ) {
            returnValue = true;
        } else if ( inString.length() > inPrefix.length() && 
                    inPrefix.equalsIgnoreCase(inString.substring(0, inPrefix.length())) ) {
            returnValue = true;
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns true if the supplied String value ends with the supplied suffix.
     * @param String - object being tested.
     * @param String - object holding suffix for which to test.
     * @param boolean
     * @return boolean 
     */
    private static boolean hasSuffix(String inString, String inSuffix, boolean inBool) {
        boolean returnValue = false;
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        if ( inBool ) {
            returnValue = true;
        } else if ( inString.length() > inSuffix.length() && 
                    inSuffix.equalsIgnoreCase(inString.substring(inString.length() - inSuffix.length())) ) {
            returnValue = true;
        }
        
        //log exit from method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns from Vignette's VGNASMOMAP table the record id associated with the supplied GUID.
     * If the parameter is null or otherwise invalid, then an empty String object is returned.
     * @param String - object holding a Vignette GUID.
     * @return String - object holding the record id. 
     */
    private static String getRecordId(String inGuid) throws VgnException, ValidationException, SQLException, RemoteException {
        
        String returnValue = "";
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        String sql =
         "SELECT DISTINCT recordId " +
         "FROM vgnAsMoMap " +
         "WHERE vgnAsMoMap.keystring1 = ?";
        
        Connection con = VgnConnectionPool.getInstance("AppSvcs Resource").getConnection();
        
        ArrayList cts = new ArrayList();
        PreparedStatement stmt = con.prepareStatement(sql);
        stmt.setString(1,inGuid);
        
        ResultSet rs = stmt.executeQuery();
        while (rs.next()) {
            String id = rs.getString("recordId");
            cts.add(id);
        }
        
        rs.close();
        stmt.close();
        con.close();
        
        if ( cts.size() == 1 ) {
            returnValue = (String)cts.get(0);
            m_logger.debug("Record Id found: "+returnValue);
        } else {
            m_logger.error("Yikes! Found "+cts.size()+" record ids; should be 1 record and 1 record only!");
        }
        
        //log exit from this method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * Returns the name of the Vignette Managed Object referenced by the supplied record id.
     * @param String - Object holding the Vignette Record Id. 
     * @return String - Object holding the name of the referenced Managed Object.
     */
    private static String getManagedObjectName(String inRecordId) {
        
        String returnValue = "";
        
        //log entrance into this method
        m_logger.debug("Enter Method");
        
        try {
            ManagedObjectVCMRef moRef = new ManagedObjectVCMRef(inRecordId);
            ManagedObject mo = ManagedObject.findByContentManagementId(moRef);
            
            if ( mo != null ) {
                returnValue = mo.getName();
            }
        } catch ( ValidationException ve )  {
            m_logger.error("Unexpected ValidationException", ve);
        } catch ( ApplicationException ae )  {
            m_logger.error("Unexpected ApplicationException", ae);
        }
        
        //log exit from this method
        m_logger.debug("Exit Method");
        
        return returnValue;
    }
    
    /**
     * @param args
     */
    public static void main(String[] args) {
        // TODO Auto-generated method stub

    }
}
