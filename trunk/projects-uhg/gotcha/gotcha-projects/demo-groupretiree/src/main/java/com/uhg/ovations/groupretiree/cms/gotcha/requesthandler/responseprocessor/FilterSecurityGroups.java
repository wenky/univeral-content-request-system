package com.uhg.ovations.groupretiree.cms.gotcha.requesthandler.responseprocessor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.requesthandler.responseprocessor.idef.ContentResponseProcessor;

public class FilterSecurityGroups implements ContentResponseProcessor
{
    String securityGroupParameter = null;
    String securityGroupAttribute = null;
    

    public void processResponse(ContentRequest contentrequest, ContentResponse response)
    {
        if (response.isMultipart()) {

            if (contentrequest != null && contentrequest.getRequestMetaData() != null) {
                ContentMetaData field = contentrequest.getRequestMetaData().get(securityGroupParameter);
                if (field != null) {
                    Set<String> securityGroupMembership = new HashSet<String>((List<String>)field.getValue());
        
                    List<ContentResponse> filtered = new ArrayList<ContentResponse>();
                    for (ContentResponse part : response.getMultipartResponse())
                    {
                        boolean isPrivledged = false;
                        List securitygroups = (List)part.getMetaData().get(securityGroupAttribute).getValue();
                        for (Object secgrpobj : securitygroups) {
                            String securitygroup = (String)secgrpobj;
                            if (securityGroupMembership.contains(securitygroup))
                            {
                                isPrivledged = true;
                            }
                        }
                        if (isPrivledged) {
                            filtered.add(part);
                        }
                    }            
                    ((BaseContentResponse)response).setCompoundResponse(filtered);
                }
            }
        }        
    }

        
        
    public String getSecurityGroupAttribute()
    {
        return securityGroupAttribute;
    }

    public void setSecurityGroupAttribute(String securityGroupAttribute)
    {
        this.securityGroupAttribute = securityGroupAttribute;
    }

    public String getSecurityGroupParameter()
    {
        return securityGroupParameter;
    }

    public void setSecurityGroupParameter(String securityGroupParameter)
    {
        this.securityGroupParameter = securityGroupParameter;
    }



}
