package com.uhg.umvs.bene.cms.contentretrieval.messages.interfaces;

import java.text.MessageFormat;
import java.util.Locale;

public interface MessageLookupService
{
    public MessageFormat resolveCode(String path, Locale locale);
}
