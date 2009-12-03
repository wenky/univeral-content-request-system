package com.uhg.umvs.bene.cms.contentretrieval.messages;

import java.text.MessageFormat;
import java.util.Locale;

import org.springframework.context.support.AbstractMessageSource;

import com.uhg.umvs.bene.cms.contentretrieval.messages.interfaces.MessageLookupService;

public class ProxiedSpringMessageSource extends AbstractMessageSource
{
    // to (theoretically) help with AOP caching, we proxy this with a simpler bean interface...
    
    MessageLookupService messageService;
    public void setMessageService(MessageLookupService messageService) {this.messageService = messageService;}

    @Override
    protected MessageFormat resolveCode(String code, Locale locale)
    {
        MessageFormat format = null;
        // attempt base code (code may have had additional info like bean and field "helpfully" appended by Spring Webflow
        format = messageService.resolveCode(code, locale);
        if (format != null) return format;
        
        // pick off one of the .tokens off the end of the code
        if (code.indexOf(".") != -1) {
            code = code.substring(0,code.lastIndexOf('.'));
            format = messageService.resolveCode(code, locale);
            if (format != null) return format;
            
            // we'll try picking off one more...
            if (code.indexOf(".") != -1) {
                code = code.substring(0,code.lastIndexOf('.'));
                format = messageService.resolveCode(code, locale);
                if (format != null) return format;
            }
        }

        // give up
        return new MessageFormat(code + " not found for locale "+(locale == null ? null : locale.getCountry()));
    }

}
