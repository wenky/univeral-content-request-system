package com.uhg.ewp.common.gotcha.requesthandler.responseprocessor.impl;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.requesthandler.responseprocessor.idef.ContentResponseProcessor;

public class MultipartSort implements ContentResponseProcessor
{
    protected Comparator comparator = null;

    public void processResponse(ContentRequest request, ContentResponse response)
    {
        if (response.isMultipart()) {
            List responses = response.getMultipartResponse();
            Collections.sort(responses,comparator);
        }
    }
    
    public Comparator getComparator()
    {
        return comparator;
    }
    public void setComparator(Comparator comparator)
    {
        this.comparator = comparator;
    }

}
