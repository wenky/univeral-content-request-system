package com.uhg.ewp.common.gotcha.contentsource.exception;

import com.uhg.ewp.common.gotcha.util.exception.GotchaRuntimeException;

/**
 * 
 * For communication, I/O, or other miscellaneous errors disrupting retrieval
 * 
 * @author cmuell7
 *
 */

public class SourceRetrievalException extends GotchaRuntimeException
{

    public SourceRetrievalException(String errcode, String message, Throwable t)
    {
        super(errcode, message, t);
    }

    public SourceRetrievalException(String errcode, String message)
    {
        super(errcode, message);
    }

    public SourceRetrievalException(String errcode, Throwable t)
    {
        super(errcode, t);
    }

    public SourceRetrievalException(String errcode)
    {
        super(errcode);
    }

}
