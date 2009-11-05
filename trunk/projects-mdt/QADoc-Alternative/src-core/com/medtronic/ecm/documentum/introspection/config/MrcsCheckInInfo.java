/*
 * Created on Mar 3, 2005
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

 Filename       $RCSfile: MrcsCheckInInfo.java,v $
 Author         $Author: dms01 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2008/11/18 20:07:37 $

***********************************************************************
*/

package com.medtronic.ecm.documentum.introspection.config;


/**
 * @author prabhu1
 *
 * TODO To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
public class MrcsCheckInInfo {

    private boolean CheckinAsSameVersion;
    private boolean CheckinAsMajorVersion;
    private boolean CheckinAsMinorVersion;
    private boolean CheckinAsBranchVersion;
    private boolean AllowDescription;
    
    /**
     * 
     */
    public MrcsCheckInInfo() {
        super();
    }

    
    /**
     * @return Returns the allowDescription.
     */
    public boolean getAllowDescription() {
        return AllowDescription;
    }
    /**
     * @param allowDescription The allowDescription to set.
     */
    public void setAllowDescription(boolean allowDescription) {
        AllowDescription = allowDescription;
    }
    /**
     * @return Returns the checkinAsBranchVersion.
     */
    public boolean getCheckinAsBranchVersion() {
        return CheckinAsBranchVersion;
    }
    /**
     * @param checkinAsBranchVersion The checkinAsBranchVersion to set.
     */
    public void setCheckinAsBranchVersion(boolean checkinAsBranchVersion) {
        CheckinAsBranchVersion = checkinAsBranchVersion;
    }
    /**
     * @return Returns the checkinAsMajorVersion.
     */
    public boolean getCheckinAsMajorVersion() {
        return CheckinAsMajorVersion;
    }
    /**
     * @param checkinAsMajorVersion The checkinAsMajorVersion to set.
     */
    public void setCheckinAsMajorVersion(boolean checkinAsMajorVersion) {
        CheckinAsMajorVersion = checkinAsMajorVersion;
    }
    /**
     * @return Returns the checkinAsMinorVersion.
     */
    public boolean getCheckinAsMinorVersion() {
        return CheckinAsMinorVersion;
    }
    /**
     * @param checkinAsMinorVersion The checkinAsMinorVersion to set.
     */
    public void setCheckinAsMinorVersion(boolean checkinAsMinorVersion) {
        CheckinAsMinorVersion = checkinAsMinorVersion;
    }
    /**
     * @return Returns the checkinAsSameVersion.
     */
    public boolean getCheckinAsSameVersion() {
        return CheckinAsSameVersion;
    }
    /**
     * @param checkinAsSameVersion The checkinAsSameVersion to set.
     */
    public void setCheckinAsSameVersion(boolean checkinAsSameVersion) {
        CheckinAsSameVersion = checkinAsSameVersion;
    }
}
