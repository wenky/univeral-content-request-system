package com.zoder.struct;

import java.util.Map;

import lbase.Lg;

public class DocumentError extends RuntimeException // should this be EEx?
{
    public Map document;
    
    public DocumentError(String message, Throwable t, Map document) 
    {
        super(message,t);
        /*-ERROR-*/Lg.errUtil(5,message,t);
        this.document = document;
        if (document != null) {
            document.put("Error.Backreference", this);
        }
    }

    public DocumentError(String message, Throwable t, Map document, String backreference) 
    {
        super(message,t);
        /*-ERROR-*/Lg.errUtil(5,message,t);
        this.document = document;
        if (document != null && backreference != null) {
            document.put(backreference, this);
        }
    }

    public DocumentError(String message, Map document) 
    {
        super(message);
        /*-ERROR-*/Lg.errUtil(5,message,this); // may actually be stacklevel 4...
        this.document = document;
        if (document != null) {
            document.put("Error.Backreference", this);
        }
    }

    public DocumentError(String message, Map document, String backreference) 
    {
        super(message);
        /*-ERROR-*/Lg.errUtil(5,message,this); // may actually be stacklevel 4...
        this.document = document;
        if (document != null && backreference != null) {
            document.put(backreference, this);
        }
    }

}
