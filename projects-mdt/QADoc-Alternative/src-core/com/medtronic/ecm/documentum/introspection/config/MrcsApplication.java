package com.medtronic.ecm.documentum.introspection.config;

import java.util.List;
import java.util.Map;


public class MrcsApplication
{
    public boolean DisplayPermitUI;
    public String ApplicationName;
    public String SystemUsername;
    public String SystemPassword;
    public String DocBase;
    public List RootFolders;
    public Map  DocumentTypes;        // Map of MrcsDocumentType keyed by MrcsDocumentType.Name
    public Map  GroupingFolderTypes;  // Map of MrcsGroupingFolderType, keyed by MrcsGroupingFolderType.Name

    public MrcsESignature ESignature;
    public MrcsCheckInInfo CheckinInfo;    
    public Map MrcsPreconditions;
    public String ConfigVersion;       // not implemented....
    
    public Map MrcsDocumentLifecycles; // deprecated
    public Map MrcsLifecycleState;     // deprecated
    public Map MrcsWFTemplates;        // deprecated
    public Map MrcsWFTasks;            // deprecated
    
    // refactor of lifecycle and workflow configuration and execution
    // - allow same-named states, activities
    // - move as much processing to the OOB DCTM and JavaServerMethod hooks
    // - enable complex scheduled promote
    // - clean up the definitions
    // - enable document-associated lifecycle/workflow, with GF-based override/addition
    public Map MrcsLifecycles;
    public Map MrcsWorkflows;
    
    public MrcsPlugin CustomWorkflowLocatorQuery;
}
