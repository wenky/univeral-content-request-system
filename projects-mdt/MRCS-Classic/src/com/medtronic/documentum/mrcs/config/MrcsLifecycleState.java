package com.medtronic.documentum.mrcs.config;

import java.util.List;

public class MrcsLifecycleState 
{
	public String Name;
	
	public String PromotionType; //MANUAL, WORKFLOW, or SCHEDULED - promotion types allowed *from* this state
	public List   AllowableWorkflows; // list of allowable workflows from this point (only if WORKFLOW is specified in PromotionType)
	
	public String VersionType; //BRANCH, SAME, MINOR, MAJOR - version performed when document promoted *to* this state
	
	// list of MrcsPlugins to be executed when a document is being promoted to this state
	// - entry and action plugins are inside the main document promotion transaction
	// - post plugins execute outside of a transaction, so an error does not undo the promotion
	public List ServerEntryPlugins;  
	public List ServerActionPlugins;
	public List ServerPostPlugins;
	
	
	
	
	//	deprecated... 4.1.2 compatibility
	public List State;
}
