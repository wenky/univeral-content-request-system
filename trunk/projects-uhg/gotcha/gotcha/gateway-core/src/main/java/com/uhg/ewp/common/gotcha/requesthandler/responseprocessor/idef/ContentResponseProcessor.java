package com.uhg.ewp.common.gotcha.requesthandler.responseprocessor.idef;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;

public interface ContentResponseProcessor
{
    public void processResponse(ContentRequest request, ContentResponse response);    

}
