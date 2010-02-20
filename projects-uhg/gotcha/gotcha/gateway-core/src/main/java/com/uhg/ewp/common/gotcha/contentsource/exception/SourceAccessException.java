package com.uhg.ewp.common.gotcha.contentsource.exception;

import com.uhg.ewp.common.gotcha.util.exception.GotchaRuntimeException;

/**
 * Indicates server connection attempt failures (server down, bad credentials)
 * 
 * @author cmuell7
 *
 */

public class SourceAccessException extends GotchaRuntimeException
{

    public SourceAccessException(String errcode, String message, Throwable t)
    {
        super(errcode, message, t);
    }

    public SourceAccessException(String errcode, String message)
    {
        super(errcode, message);
    }

    public SourceAccessException(String errcode, Throwable t)
    {
        super(errcode, t);
    }

    public SourceAccessException(String errcode)
    {
        super(errcode);
    }

}
