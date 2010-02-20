package com.uhg.ewp.common.gotcha.contentsource.idef;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;


public interface ContentSource
{    
    public ContentResponse getContent(ContentRequest contentItem);
}
