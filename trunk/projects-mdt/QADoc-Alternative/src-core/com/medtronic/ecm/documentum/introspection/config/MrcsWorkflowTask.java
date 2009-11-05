package com.medtronic.ecm.documentum.introspection.config;

import java.util.List;
import java.util.Map;

public class MrcsWorkflowTask 
{
    public String Name;
    public List Validations;   // validates before task can be accepted - i.e. rendition exists...
    public List Analysis;      // analysis messages - rejection reason, etc...
    public List Actions;       // pseudo-legacy client-side workflow actions
    public boolean AllowMultipleForwardPaths;
    public Map MethodConfiguration; // for parametrizing JSM method calls 
}
