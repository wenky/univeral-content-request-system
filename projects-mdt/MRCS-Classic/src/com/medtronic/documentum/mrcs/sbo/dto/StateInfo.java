/*
 * Created on Feb 3, 2005
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
/*
**********************************************************************

 Confidential Property of Medtronic, Inc.
 c Copyright Medtronic, Inc. 2006.
 All Rights reserved.
 May not be used without prior written agreement
 signed by a corporate officer.

***********************************************************************

 Project        MRCS
 Version        4.1.1
 Description
 Created on

***********************************************************************

 CVS Maintained Data

 Filename       $RCSfile: StateInfo.java,v $
 Author         $Author: muellc4 $
 Revision       $Revision: 1.2 $
 Modified on    $Date: 2006/06/05 20:30:16 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo.dto;

import java.util.List;
import java.util.Map;

import com.medtronic.documentum.mrcs.config.MrcsPlugin;

/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class StateInfo {

	// legacy fields that still apply 
	public String Name;
	public String PromotionType;
	public String VersionToNext;
	public List AllowableWorkflows;
	
	public boolean ProcessOnServer;
	public List ServerEntryPlugins;
	public List ServerActionPlugins;
	public List ServerPostPlugins;
	
	// legacy tag information...
	private String NextState;
	private String DemoteState;
	private String ExceptionState;
	private String RejectedDocACL;
	private String DocACL;
	private String Label;
	private boolean CurrentLabel;
	private boolean ReferToOriginal;
	private String CopyToPlugin;
	private String CopyToLocation;
	private String CopyDocACL;
	private String CopyDocLabel;
    public String SuppressRendition;
    public String RenditionPluginClass;
    public Map RenditionPluginConfig;
	private boolean PromoteAllPreviousVersionsToNextState;
	public MrcsPlugin MrcsNPPManualPromoteInfo;
    // begin promotion plugin layer (CEM) -- these execute INSIDE of the transaction
    public List PostPromotePlugins;
    public List PostDemotePlugins;
    // end promotion plugin laye (CEM)
    // begin another plugin layer -- these execute AFTER the promote transaction has committed
    public List PostTransactionPromotePlugins;
    public List PostTransactionDemotePlugins;
    // end another plugin layer -- these execute AFTER the promote transaction has committed
	/**
	 * @return Returns the copyToLocation.
	 */
	public String getCopyToLocation() {
		return CopyToLocation;
	}
	/**
	 * @param copyToLocation The copyToLocation to set.
	 */
	public void setCopyToLocation(String copyToLocation) {
		CopyToLocation = copyToLocation;
	}
	
	/**
	 * @return Returns the copyToLocation.
	 */
	public String getCopyToPlugin() {
		return CopyToPlugin;
	}
	/**
	 * @param copyToLocation The copyToLocation to set.
	 */
	public void setCopyToPlugin(String copyToplugin) {
	    CopyToPlugin = copyToplugin;
	}
	
	/**
	 * @return Returns the demoteState.
	 */
	public String getDemoteState() {
		return DemoteState;
	}
	/**
	 * @param demoteState The demoteState to set.
	 */
	public void setDemoteState(String demoteState) {
		DemoteState = demoteState;
	}
	/**
	 * @return Returns the docPromotetype.
	 */
	public String getPromotionType() {
		return PromotionType;
	}
	/**
	 * @param docPromotetype The docPromotetype to set.
	 */
	public void setPromotionType(String docPromotetype) {
		PromotionType = docPromotetype;
	}
	
	/**
	 * @return Returns the label.
	 */
	public String getLabel() {
		return Label;
	}
	/**
	 * @param label The label to set.
	 */
	public void setLabel(String label) {
		Label = label;
	}
	/**
	 * @return Returns the name.
	 */
	public String getName() {
		return Name;
	}
	/**
	 * @param name The name to set.
	 */
	public void setName(String name) {
		Name = name;
	}
	/**
	 * @return Returns the nextState.
	 */
	public String getNextState() {
		return NextState;
	}
	/**
	 * @param nextState The nextState to set.
	 */
	public void setNextState(String nextState) {
		NextState = nextState;
	}
	/**
	 * @return Returns the referToOriginal.
	 */
	public boolean getReferToOriginal() {
		return ReferToOriginal;
	}
	/**
	 * @param referToOriginal The referToOriginal to set.
	 */
	public void setReferToOriginal(boolean referToOriginal) {
		ReferToOriginal = referToOriginal;
	}
	/**
	 * @return Returns the versionToNext.
	 */
	public String getVersionToNext() {
		return VersionToNext;
	}
	/**
	 * @param versionToNext The versionToNext to set.
	 */
	public void setVersionToNext(String versionToNext) {
		VersionToNext = versionToNext;
	}
	/**
	 * 
	 */
	public StateInfo() {
		super();
		// TODO Auto-generated constructor stub
	}

	/**
	 * @return Returns the demoteExceptionRule.
	 */
	public boolean getCurrentLabel() {
		return CurrentLabel;
	}
	/**
	 * @param demoteExceptionRule The demoteExceptionRule to set.
	 */
	public void setCurrentLabel(boolean currLabel) {
		CurrentLabel = currLabel;
	}
    /**
     * @return Returns the exceptionState.
     */
    public String getExceptionState() {
        return ExceptionState;
    }
    /**
     * @param exceptionState The exceptionState to set.
     */
    public void setExceptionState(String exceptionState) {
        ExceptionState = exceptionState;
    }
    /**
     * @return Returns the docACL.
     */
    public String getDocACL() {
        return DocACL;
    }
    /**
     * @param docACL The docACL to set.
     */
    public void setDocACL(String docACL) {
        DocACL = docACL;
    }
    /**
     * @return Returns the rejectedDocACL.
     */
    public String getRejectedDocACL() {
        return RejectedDocACL;
    }
    /**
     * @param rejectedDocACL The rejectedDocACL to set.
     */
    public void setRejectedDocACL(String rejectedDocACL) {
        RejectedDocACL = rejectedDocACL;
    }
    
    
    /**
     * @return Returns the copyDocACL.
     */
    public String getCopyDocACL() {
        return CopyDocACL;
    }
    /**
     * @param copyDocACL The copyDocACL to set.
     */
    public void setCopyDocACL(String copyDocACL) {
        CopyDocACL = copyDocACL;
    }
    /**
     * @return Returns the copyDocLabel.
     */
    public String getCopyDocLabel() {
        return CopyDocLabel;
    }
    /**
     * @param copyDocLabel The copyDocLabel to set.
     */
    public void setCopyDocLabel(String copyDocLabel) {
        CopyDocLabel = copyDocLabel;
    }
   
    /**
     * @return Returns the promoteAllPreviousVersionsToNextState.
     */
    public boolean getPromoteAllPreviousVersionsToNextState() {
        return PromoteAllPreviousVersionsToNextState;
    }
    /**
     * @param promoteAllPreviousVersionsToNextState The promoteAllPreviousVersionsToNextState to set.
     */
    public void setPromoteAllPreviousVersionsToNextState(boolean promoteAllPreviousVersionsToNextState) {
        PromoteAllPreviousVersionsToNextState = promoteAllPreviousVersionsToNextState;
    }
    /**
     * @return Returns the allowableWorkflows.
     */
    public List getAllowableWorkflows() {
        return AllowableWorkflows;
    }
    /**
     * @param allowableWorkflows The allowableWorkflows to set.
     */
    public void setAllowableWorkflows(List allowableWorkflows) {
        AllowableWorkflows = allowableWorkflows;
    }
    /**
     * @return Returns the mrcsNPPManualPromoteInfo.
     */
    public MrcsPlugin getMrcsNPPManualPromoteInfo() {
        return MrcsNPPManualPromoteInfo;
    }
    /**
     * @param mrcsNPPManualPromoteInfo The mrcsNPPManualPromoteInfo to set.
     */
    public void setMrcsNPPManualPromoteInfo(MrcsPlugin mrcsNPPManualPromoteInfo) {
        MrcsNPPManualPromoteInfo = mrcsNPPManualPromoteInfo;
    }
    
    // added by CEM
    
    
    /**
     * @return Returns the postDemotePlugins.
     */
    public List getPostDemotePlugins() {
        return PostDemotePlugins;
    }
    /**
     * @return Returns the postPromotePlugins.
     */
    public List getPostPromotePlugins() {
        return PostPromotePlugins;
    }

    public List getPostTransactionPromotePlugins() {
        return PostTransactionPromotePlugins;
    }
}
