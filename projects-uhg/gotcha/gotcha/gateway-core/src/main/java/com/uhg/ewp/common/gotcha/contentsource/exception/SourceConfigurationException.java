package com.uhg.ewp.common.gotcha.contentsource.exception;

import com.uhg.ewp.common.gotcha.util.exception.GotchaRuntimeException;

/**
 * 
 * Indicates improperly configured/uninitialized ContentSource
 * 
 * @author cmuell7
 *
 */

public class SourceConfigurationException extends GotchaRuntimeException
{

    public SourceConfigurationException(String errcode, String message, Throwable t)
    {
        super(errcode, message, t);
        // TODO Auto-generated constructor stub
    }

    public SourceConfigurationException(String errcode, String message)
    {
        super(errcode, message);
        // TODO Auto-generated constructor stub
    }

    public SourceConfigurationException(String errcode, Throwable t)
    {
        super(errcode, t);
        // TODO Auto-generated constructor stub
    }

    public SourceConfigurationException(String errcode)
    {
        super(errcode);
        // TODO Auto-generated constructor stub
    }

}
