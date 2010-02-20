package com.uhg.ewp.common.gotcha.requestparser.impl.extrapath;

import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentRequest;
import com.uhg.ewp.common.gotcha.requestparser.idef.ContentRequestParser;
import com.uhg.ewp.common.gotcha.util.log.Lg;

public class ExtraPathPlusMetaDataParser implements ContentRequestParser
{
    Set<String>         requiredParameters = null;
    Set<String>         multiValueParameters = null; // else single-value
    Set<String>         responseMetaData = null;             

    public ContentRequest parseRequest(HttpServletRequest req)
    {
        String extrapath = req.getPathInfo();
        BaseContentRequest contentrequest = new BaseContentRequest();
        contentrequest.setParsedSuccessfully(true);
        contentrequest.setContentItem(extrapath);
        
        Map<String,ContentMetaData> metadata = new HashMap<String,ContentMetaData>();
        Enumeration params = req.getParameterNames();
        while(params.hasMoreElements()) {
            String name = (String)params.nextElement();
            if (multiValueParameters != null && multiValueParameters.contains(name)) {
                List<String> values = Arrays.asList(req.getParameterValues(name));
                BaseContentMetaData requestmeta = new BaseContentMetaData();
                requestmeta.setName(name);
                requestmeta.setType(String.class); // TODO: autoconversion utilities?
                requestmeta.setRepeatingValues(values);
                metadata.put(name, requestmeta);
            } else {
                String value = req.getParameter(name);
                BaseContentMetaData requestmeta = new BaseContentMetaData();
                requestmeta.setName(name);
                requestmeta.setType(String.class); // TODO: autoconversion utilities?
                requestmeta.setSingleValue(value);
                metadata.put(name, requestmeta);
            }
        }
        
        // check required parameters
        boolean isRequiredValid = true;
        if (requiredParameters != null) {
            for (String key : requiredParameters)
            {
                if (!metadata.containsKey(key)) {
                    isRequiredValid = false;
                    if(Lg.trc())Lg.trc("Request url %s querystring %s does not contain required parameter %s",req.getRequestURL(), req.getQueryString(), key); 
                }
            }
            if (!isRequiredValid) {
                contentrequest.setParsedSuccessfully(false);
            } 
        }
        
        contentrequest.setRequestMetaData(metadata);
        contentrequest.setResponseMetaDataFields(responseMetaData);
        
        return contentrequest;
    }
    

    
    public Set<String> getRequiredParameters()
    {
        return requiredParameters;
    }


    public void setRequiredParameters(Set<String> requiredParameters)
    {
        this.requiredParameters = requiredParameters;
    }


    public Set<String> getMultiValueParameters()
    {
        return multiValueParameters;
    }

    public void setMultiValueParameters(Set<String> multiValueParameters)
    {
        this.multiValueParameters = multiValueParameters;
    }

    public Set<String> getResponseMetaData()
    {
        return responseMetaData;
    }

    public void setResponseMetaData(Set<String> responseMetaData)
    {
        this.responseMetaData = responseMetaData;
    }

}
