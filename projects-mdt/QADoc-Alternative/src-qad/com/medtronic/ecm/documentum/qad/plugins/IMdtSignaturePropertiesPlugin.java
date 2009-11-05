package com.medtronic.ecm.documentum.qad.plugins;

import java.util.Map;

import com.documentum.fc.client.IDfSysObject;

public interface IMdtSignaturePropertiesPlugin 
{
    public void getProperties(IDfSysObject tboinstance, Map propertymap, String username, String justification, String signaturemethod,String appprops, String passthru1,String passthru2,Map context) throws Exception;

}
