/*
 * Created on Jan 11, 2005
 *
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

 Filename       $RCSfile: ESignDTO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:01 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo.dto;


/**
 * @author prabhu1
 *
 * The ESignature Data transfer Object
 */
public class ESignDTO {

	private String usr = null;
	private String pswd = null;
	private String reason = null;
		
	public ESignDTO() {
	}

	/**
	 * @param usr
	 * @param pswd
	 * @param reason
	 */
	public ESignDTO(String usr, String pswd, String reason) {
		super();
		this.usr = usr;
		this.pswd = pswd;
		this.reason = reason;
	}
	/**
	 * @return Returns the pswd.
	 */
	public String getPswd() {
		return pswd;
	}
	/**
	 * @param pswd The pswd to set.
	 */
	public void setPswd(String pswd) {
		this.pswd = pswd;
	}
	/**
	 * @return Returns the reason.
	 */
	public String getReason() {
		return reason;
	}
	/**
	 * @param reason The reason to set.
	 */
	public void setReason(String reason) {
		this.reason = reason;
	}
	/**
	 * @return Returns the usr.
	 */
	public String getUsr() {
		return usr;
	}
	/**
	 * @param usr The usr to set.
	 */
	public void setUsr(String usr) {
		this.usr = usr;
	}
}
