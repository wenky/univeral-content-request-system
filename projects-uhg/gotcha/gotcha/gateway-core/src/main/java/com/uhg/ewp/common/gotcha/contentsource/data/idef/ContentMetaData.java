package com.uhg.ewp.common.gotcha.contentsource.data.idef;

// Content Sources returning metadata field values in ContentResponses should it encapsulated in this interface

public interface ContentMetaData
{
    public boolean isRepeating();
    public String getName();
    public Class getType();
    public Object getValue();
    // TODO, if needed: getCode...
}
