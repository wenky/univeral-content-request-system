/*
 * Created on Jan 31, 2005
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

 Filename       $RCSfile: StateTransitionServiceSBO.java,v $
 Author         $Author: hansom5 $
 Revision       $Revision: 1.1 $
 Modified on    $Date: 2006/05/30 21:36:01 $

***********************************************************************
*/

package com.medtronic.documentum.mrcs.sbo;


import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfTypedObject;
import com.documentum.fc.common.DfException;
import com.medtronic.documentum.mrcs.sbo.dto.StateInfo;

/**
 * @author prabhu1
 *
 * The State Transition SBO
 */
public class StateTransitionServiceSBO extends DfService implements
		IStateTransitionServiceSBO {

	private String vendorString =  "Medtronics";
	private static final String version = "4.0";
	private String appName = "";

	public StateTransitionServiceSBO() {
		super();
	}

	/* (non-Javadoc)
	 * @see com.documentum.fc.client.IDfService#getVersion()
	 */
	public String getVersion() {
		return version;
		}

	/* (non-Javadoc)
	 * @see com.documentum.fc.client.IDfService#getVendorString()
	 */
	public String getVendorString() {
		return vendorString;
		}

	/* (non-Javadoc)
	 * @see com.documentum.fc.client.IDfService#isCompatible(java.lang.String)
	 */
	public boolean isCompatible(String arg0) {
	       int i =arg0.compareTo(getVersion() );
	        if(i <= 0 )
	            return true;
	        else
	            return false;
	        }

	/* (non-Javadoc)
	 * @see com.medtronic.documentum.mrcs.sbo.IStateTransitionServiceSBO#promote(java.lang.String, boolean, boolean)
	 */
	public void promote(IDfDocument docObject, StateInfo stInfo)
			throws DfException {
/*		System.out.println("~~~Promote~~~~");
        try{
        	//Need to eliminate the usage of this try catch block by
        	//better exception handling mechanizsm at Config broker
			StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
			//StateInfo stInfo = config.getStateInfo(appName,docObject.getCurrentStateName());
			StateInfo nextstInfo = config.getStateInfo(appName,stInfo.getNextState());

			//On successful promotion, attach Lifecycle State label
			String labeltoAttach = stInfo.getLabel();
			System.out.println( "Check for the last State" );
			//if promotion is successful then change the version [VersionToNext]
			String nextLabel = null;
			if(nextstInfo.getNextState().equals("")){
				nextLabel = nextstInfo.getVersionToNext();
			}else{
				nextLabel = stInfo.getVersionToNext();
			}



			System.out.println( ">>>>>>DOC Properties if promotion is successful then change the version [VersionToNext]>>>>>> nextLabel " + nextLabel);
				String  verLabel = null;
				IDfVersionPolicy verPolicy = docObject.getVersionPolicy();
				if (nextLabel.equalsIgnoreCase("MAJOR")){
					verLabel = verPolicy.getNextMajorLabel();
					System.out.println( ">>>>>>DOC Properties MAJOR>>>>>> verLabel  " + verLabel);
					if (!docObject.isCheckedOut()) docObject.checkout();
					docObject.mark(labeltoAttach);
					docObject.checkin(false,verLabel);
				}
				else if(nextLabel.equalsIgnoreCase("MINOR")){
					verLabel = verPolicy.getNextMinorLabel();
					System.out.println( ">>>>>>DOC Properties MINOR>>>>>> verLabel  " + verLabel);
					if (!docObject.isCheckedOut()) docObject.checkout();
					docObject.mark(labeltoAttach);
					docObject.checkin(false,verLabel);
					}
				else if (nextLabel.equalsIgnoreCase("BRANCH")){
					verLabel = verPolicy.getBranchLabel();
					System.out.println( ">>>>>>DOC Properties BRANCH>>>>>> verLabel  " + verLabel);
					if (!docObject.isCheckedOut()) docObject.checkout();
					docObject.mark(labeltoAttach);
					docObject.checkin(false,verLabel);
				}
				else if (nextLabel.equalsIgnoreCase("SAME")){
					verLabel = verPolicy.getSameLabel();
					System.out.println( ">>>>>>DOC Properties SAME>>>>>> verLabel  " + verLabel);
					docObject.mark(labeltoAttach);
					docObject.save();
				}


			//Check for the last State
			if(nextstInfo.getNextState().equals("")){
				System.out.println( "if promotion is successful then create the Link [ReferToOriginal]" );
				//if promotion is successful then create the Link [ReferToOriginal]
				if(nextstInfo.getReferToOriginal()){
			    //create a copy of the document and link to original
					IDfId	child_id = ((IMrcsDocumentTBO)docObject).doCopy(nextstInfo.getCopyToLocation());
					System.out.println( ">>>>>>DOC Properties getReferToOriginal>>>>>> child_id  " + child_id);
				//Create the relation
					((IMrcsDocumentTBO)docObject).setRelation("ReferToOriginalRelationShip", child_id.getId() , docObject.getObjectId().getId(), "description");
                /*
				objId = ((IDfSysObject)oSysObj).getObjectId();
				IDfRelation oRelation = oCopySysObj.addParentRelative("my_relation", objId, "", false, "");
				oRelation.setPermanentLink(true);
				System.out.println("Relationship created with name: " + oRelation.getRelationName());
				oRelation.save();
				*/
	    /*}

			}
			System.out.println( ">>>>>>DOC Properties  PROMOTE ENDS >>>>>> CURRENTSTATE " + docObject.getCurrentStateName());

        }catch(Exception e){
        	System.out.println("Exception@promote"+e);
        }
*/
	}

	/* (non-Javadoc)
	 * @see com.medtronic.documentum.mrcs.sbo.IStateTransitionServiceSBO#demote(java.lang.String, boolean)
	 */
	public void demote(IDfDocument docObject, String state, boolean toBase) throws DfException {
		/*System.out.println("~~~Demote~~~~");
        try{
        	//Need to eliminate the usage of this try catch block by
        	//better exception handling mechanizsm at Config broker
			StateTransitionConfigFactory config = StateTransitionConfigFactory.getSTConfig();
			StateInfo stInfo = config.getStateInfo(appName,docObject.getCurrentStateName());
			//super.demote(stInfo.getDemoteState(), true); //demote to base true
			docObject.demote(stInfo.getDemoteState(), false);
			//System.out.println( ">>>>>>DOC Properties After Demote>>>>>> getTypeName " + docObject.getTypeName());
			System.out.println( ">>>>>>DOC Properties After Demote>>>>>> CurrentState " + docObject.getCurrentStateName());
			//System.out.println( ">>>>>>DOC Properties After Demote>>>>>> NextState " + docObject.getNextStateName());
			//System.out.println( ">>>>>>DOC Properties After Demote>>>>>> ResumeState " + docObject.getResumeStateName());

        }catch(Exception e){
        	System.out.println("Exception@Demote"+e);
        }
	*/

	}

    /* (non-Javadoc)
     * @see com.medtronic.documentum.mrcs.sbo.IStateTransitionServiceSBO#preCheckPromote(com.documentum.fc.client.IDfDocument, com.medtronic.documentum.mrcs.sbo.dto.StateInfo)
     */
    public boolean preCheckPromote(IDfDocument docObject) throws DfException {
        //docObject.getLinkCount()
        IDfCollection wrkFlws = docObject.getWorkflows("","");
        System.out.println( " wrkFlws "+wrkFlws);
        IDfTypedObject tObj = null;
        System.out.println( " wrkFlws.getAttrCount() "+wrkFlws.getAttrCount());
        //for (int i=0; i<wrkFlws.getAttrCount(); i++){

        //while(wrkFlws.next()){
            tObj = wrkFlws.getTypedObject();
            System.out.println( " tObj "+tObj);
            System.out.println( " tObj ID  "+tObj.getObjectId());
            System.out.println( " tObj ID  "+tObj.getValueAt(0));
        //}
        if (wrkFlws != null)
            wrkFlws.close();

        return true;
    }

}
