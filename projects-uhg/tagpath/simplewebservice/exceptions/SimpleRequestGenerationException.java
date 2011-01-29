package com.uhg.ovations.portal.partd.simplewebservice.exceptions;

// something went wrong with request generation
public class SimpleRequestGenerationException extends SimpleServiceException 
{
    public SimpleRequestGenerationException() {super();}	
    public SimpleRequestGenerationException(String message) {super(message);}
    public SimpleRequestGenerationException(String message, Throwable cause) {super(message, cause);}
    public SimpleRequestGenerationException(Throwable cause) {super(cause);}	
}
