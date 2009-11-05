package com.medtronic.documentum.mrcs.config;

import java.util.List;
import java.util.Map;

public class MrcsGroupingFolderAllowableDocument // finer-grained control over documents and formats of those documents
{
    public String DocumentType; // references MrcsDocumentTypes.[DocumentType]
    public String DocumentACL;
    public List AllowableFormats; // List of MrcsDocumentAllowableFormat, overrides the global definition of valid types
    public String Component; // overrides Component on MrcsDocumentType if not null...
    public MrcsPlugin ComponentProcessorPlugin;
    public List DocumentCreationPlugins; // overrides doctype-level, post-creation plugin for custom settings (custom attribs, custom validations, etc)
    public String Lifecycle;

    // gf-specific operations to perform for this doctype in this folder
    // map of states, which has the list per state
    public Map LifecycleEntryPlugins; // plugins to execute after entry validations, but before any user or standard actions (transactionalized) 
    public Map LifecycleActionPlugins; // plugins to execute after standard actions(transactionalized) 
    public Map LifecyclePostPlugins; // plugins to execute after standard and user actions (NOT transactionalized) 
    
}
