package com.uhg.ovations.groupretiree.cms.gotcha.requesthandler.impl;

import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.requesthandler.idef.ContentRequestHandler;
import com.uhg.ewp.common.gotcha.requestparser.idef.ContentRequestParser;
import com.uhg.ewp.common.gotcha.util.log.Lg;

public class SortAndFilterMultipartHandler implements ContentRequestHandler
{
    // PROPERTIES ----    
    ContentRequestParser        requestParser = null;    
    ContentSource               contentSource = null;
    String                      securityGroupParameter = null;
    String                      securityGroupAttribute = null;
    String                      sortOrderAttribute = null;                              
    // ---- END PROPS


    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp)
    {
        ContentRequest contentrequest = requestParser.parseRequest(req);
        
        if (!contentrequest.isParsedSuccessfully()) {
            return false;
        } else {
            
            // get security group membership info from parsed content request
            Set<String> securityGroupMembership = new HashSet<String>((List<String>)contentrequest.getRequestMetaData().get(securityGroupParameter).getValue());
        
            ContentResponse contentresponse = contentSource.getContent(contentrequest);
            
            if (contentresponse.isFound()) {
                if (!contentresponse.isMultipart()) {
                    try { 
                        IOUtils.copy(contentresponse.getContent(), resp.getOutputStream());
                    } catch (IOException ioe) {
                        throw new RuntimeException("IOException writing content response to HTTP Response",ioe);                
                    }
                    return true;
                } else {
                    try {                        
                        List<ContentResponse> multipart = contentresponse.getMultipartResponse();
                        List<ContentResponse> filteredsorted = new ArrayList<ContentResponse>();
                        // filter on security groups                        
                        for (ContentResponse part : multipart) 
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
                                filteredsorted.add(part);
                            }
                        }
                        // now sort on sortorder
                        class SortOrderComparator implements Comparator
                        {
                            int getValue(ContentMetaData field)
                            {
                                if (field.isRepeating()) {
                                    List values = (List)(field.getValue());
                                    if (values != null && values.size() > 0)
                                    {
                                        String val = (String)values.get(0);
                                        int parsed = Integer.parseInt(val);
                                        return parsed;
                                    } else {
                                        return Integer.MAX_VALUE;
                                    }
                                } else {
                                    if (Integer.class.equals(field.getType())) {
                                        Integer val = (Integer)field.getValue();
                                        if (val == null) return Integer.MAX_VALUE;
                                        else return val;
                                    } else {                                        
                                        String val = field.getValue().toString();
                                        if (val == null) return Integer.MAX_VALUE;
                                        int parsed = Integer.parseInt(val);
                                        return parsed;
                                    }
                                }
                                
                            }
                            
                            public int compare(Object o1, Object o2) 
                            {                                
                                ContentResponse part1 = (ContentResponse)o1;
                                ContentResponse part2 = (ContentResponse)o2;
                                    
                                int sortorder1 = getValue(part1.getMetaData().get(sortOrderAttribute));
                                int sortorder2 = getValue(part2.getMetaData().get(sortOrderAttribute));
                                Lg.trc("sort1: %d sort2:%d",sortorder1,sortorder2);
                                if (sortorder1 < sortorder2) return -1;
                                if (sortorder1 == sortorder2) return 0;
                                return 1;
                            }
                            
                            public boolean equals(Object obj)  { return false; }
                        }
                        
                        
                        Collections.sort(filteredsorted,new SortOrderComparator());
                        
                        OutputStream out = resp.getOutputStream();
                        for (ContentResponse part : filteredsorted)
                        {
                            IOUtils.copy(part.getContent(), out);                            
                        }
                        
                    } catch (IOException ioe) {
                        throw new RuntimeException("IOException writing content response to HTTP Response",ioe);                
                    }
                    return true;
                }
            } else {
                return false;
            }
        }
        
    }
    



    // getters setters
    public void setRequestParser(ContentRequestParser requestParser)
    {
        this.requestParser = requestParser;
    }

    public void setContentSource(ContentSource contentSource)
    {
        this.contentSource = contentSource;
    }
    
    public String getSecurityGroupAttribute()
    {
        return securityGroupAttribute;
    }


    public void setSecurityGroupAttribute(String securityGroupAttribute)
    {
        this.securityGroupAttribute = securityGroupAttribute;
    }


    public String getSortOrderAttribute()
    {
        return sortOrderAttribute;
    }

    public void setSortOrderAttribute(String sortOrderAttribute)
    {
        this.sortOrderAttribute = sortOrderAttribute;
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
