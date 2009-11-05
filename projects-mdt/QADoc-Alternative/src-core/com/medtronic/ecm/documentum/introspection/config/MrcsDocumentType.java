package com.medtronic.ecm.documentum.introspection.config;

import java.util.List;

public class MrcsDocumentType
{
    public String Name;
    public String DocumentumSystemType;
    public List AllowableFormats; // List of MrcsDocumentAllowableFormat - GF overrideable...
    public String Component; // custom page for document-type-specific creation data input, overrideable at GF level if necessary
    public MrcsPlugin ComponentProcessorPlugin; // processes the custom data entry screen for the document
    public List DocumentCreationPlugins; // post-creation plugin for custom settings (custom attribs, custom validations, etc)
    
    // new...
    public String Lifecycle; // lifecycle to apply to documents of this mrcs type (overrideable at GF level)

    // deprecated...
    //public String MrcsDocumentLifecycle; 
    public List MrcsDocumentLifecycles;
    public List MrcsWFTemplates;           // pretty sure this is deprecated, allowables defined by LC state...
}
