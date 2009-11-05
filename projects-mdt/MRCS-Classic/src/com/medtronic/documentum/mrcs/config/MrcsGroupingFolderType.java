package com.medtronic.documentum.mrcs.config;

import java.util.List;

public class MrcsGroupingFolderType
{
    public String Name;
    public String DocumentumSystemType;        // documentum sysobject type (must be a descendant of mrcs_folder)
    public String Component;             // webtop component for creating a new grouping folder
    public MrcsPlugin ComponentProcessorPlugin;
    public String GroupingFolderACL;             // ACL applied to grouping folders (but possibly not the system subfolders)
    public String SubfolderACL;
    public String DocumentACL;           // ACL applied to documents and user-created subfolders in the GF
    public String SubfolderDocumentumType;
    public String SubfolderComponent;    // webtop component for creating a new subfolder (null means auto-generate from config plugins)
    public boolean DisallowUserCreatedSubfolders; // defaults to false (allow users to create subfolders)
    public MrcsPlugin SubfolderComponentProcessorPlugin;
    public MrcsPlugin SubfolderNamingFormat;
    public MrcsPlugin GroupingFolderNamingFormat;
    public MrcsPlugin DocumentNamingFormat;
    public List   GroupingFolderCreationPlugins;   // list of MrcsPlugins executed after the grouping folder is created
    public List   SubfolderCreationPlugins;   // list of MrcsPlugins executed after the subfolder folder is created
    public List   AllowableDocumentTypes; // List of MrcsGroupingFolderAllowableDocument - Lists are easier XML-wise, but maybe these should be Maps...
    public List   GroupingFolderTypes; // for nested grouping folders - List of MrcsGroupingFolderType
}
