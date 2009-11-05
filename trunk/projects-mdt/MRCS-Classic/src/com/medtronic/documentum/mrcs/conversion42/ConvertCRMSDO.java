package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;

public class ConvertCRMSDO 
{
	public static void main(String[] args) throws Exception
	{
		// Plan:
		//
		// audit step - determine number of documents
		// -- UHOH, contract descends from document. make sure to check that document sets are distinguished....
		// -- m_mrcs_crmsdo_document - 281
		// -- m_mrcs_crmsdo_contract - 65
		// audit step - splitting of documents into subsets to avoid database/transaction overload?
		// -- no, shouldn't be necessary
		// audit step - check if all lifecycle states are directly attachable
		// -- yes
		// audit step - check for documents that are locked/checked out
		// -- m_mrcs_crmsdo_contract - 1
		// -- m_mrcs_crmsdo_document - ??
		// audit step - check if docs are in workflow right now
		// -- no workflows in CRMSDO legacy docs (added for 4.2 config though)
		// audit step - is pdf->acro format conversion necessary?
		// -- yes, so reattachment of policies will be necessary...
		// audit step - is acro format installed?
		// audit step - is Retired/Obsolete necessary?
		// -- no
		// audit step - need to set migration history?
		// -- yes, but don't need to identify previous migrations
		// audit step - integrity of approved copies? 
		// -- config has no approved copies
		// audit step - identify documents that WON'T be migrated for migration testing purposes
				
		//LOG:
		
		// CONVERTING CONTRACTS
		
		// AUDIT - get list of contracts and spot-check them for problems....
		// ran getContractList(), but the result included results from Templates cabinet and the CRM_SDO_TESTING cabinet, so changed query to use the FOLDER qualifier
		// ran getContractList() with FOLDER qualifier, proper results produced
		// -- query: SELECT r_object_id FROM m_mrcs_crmsdo_contract WHERE FOLDER('/CRM/CRM Supplier Development Organization',DESCEND) ORDER BY r_object_id
		// manually edited result list file to preserve two contracts for legacy testing: 09025b41800818f8 (MDTCT-023) and 09025b41800818fe (MDTCT-024)
		//  -- filename: C:/2006WT53/conversion42files/test-crmsdo-contracts-list.xml
		
		// SET MIGRATION HISTORY -- good, safe, easy step, identifies lots of problem/locked documents ahead of time...
		// get ALL versions of documents identified in AUDIT step so we can set migration history on all those values
		// -- filename: C:/2006WT53/conversion42files/test-crmsdo-contracts-list-ALLVERSIONS.xml
		// setting MigrationHistory attribute to MRCS412 on ALLVERSIONS file produced in the previous step
		// -- filename for successful sets: C:/2006WT53/conversion42files/test-crmsdo-contracts-list-setmigration-SUCCESS.xml
		// -- one error in error file: C:/2006WT53/conversion42files/test-crmsdo-contracts-list-setmigration-ERROR.xml
		// manually verifying one of the success documents...yes
		// attempting rerun on the single failure, will step through to try to see what went wrong...document was locked. Cancel Checkout, rerun
		// -- successful run2: C:/2006WT53/conversion42files/test-crmsdo-contracts-list-setmigration-SUCCESS[run2].xml
		
		// CONVERT pdf to acro 
		// using audit step's document list as input...
		//  -- filename: C:/2006WT53/conversion42files/test-crmsdo-contracts-list.xml
		
		// there are no previous versions from what I can see in the list, so we won't worry about them
		// pare down match list to pdf-format files (getPdfFormatContracts) - in the future, make sure you exclude the docs designated for "unconverted testing"
		// -- query: SELECT r_object_id FROM m_mrcs_crmsdo_contract WHERE a_content_type = 'pdf' and FOLDER('/CRM/CRM Supplier Development Organization',DESCEND) ORDER BY r_object_id
		// -- output file: C:/2006WT53/conversion42files/test-crmsdo-contracts-pdf-format.xml
		//getPdfFormatContracts();
		// All are in-progress...so no renditions. anyway, let's run pdf->acro processor on them.
		// -- success: C:/2006WT53/conversion42files/test-crmsdo-contracts-list-PDFTOACRO-SUCCESS.xml
		// -- error file (0 errors): C:/2006WT53/conversion42files/test-crmsdo-contracts-list-PDFTOACRO-ERROR.xml
		//convertPdfFormatContractsToAcro();
		
		// manually validating format of documents -- oops, I converted the two I wasn't supposed to convert b/c I reran the query...
		// -- unconverted the two docs with a hack version of convertpdf back to pdf...
		// -- formats were converted, checking if the content is accessible...looks okay, exported fine (my plugins are fubar'd)
		
		// Lessons learned from contracts processing:
		// need to be more careful with "reserved" nonconverted documents
		// when processing documents, put the # of documents processed in each file's filename, so we can track the numbers of docs being processed from input dataset and output datasets
		// need to be careful of mrcs doctypes that descend from other mrcs doctypes, in this case, contracts descends from the general m_mrcs_crmsdo_document
		// need to be more thorough with performing initial accounting figures calculations - number of docs total (dm_document, m_mrcs_document, config-specific doctypes)
		
		// CONVERTING CRMSDO DOCUMENTS
		
		// AUDIT
		// get list of CRMSDO documents - how many absolute? 
		// running getDocumentsList() with FOLDER qualifier, proper results produced
		// -- number of docs: 120
		// -- query: SELECT r_object_id FROM m_mrcs_crmsdo_document WHERE FOLDER('/CRM/CRM Supplier Development Organization',DESCEND) AND r_object_id not in (select r_object_id from m_mrcs_crmsdo_contract) ORDER BY r_object_id
		// -- output: test-crmsdo-documents-list[120].xml
		// manually selecting docs for legacy/unconverted document testing...
		// -- docs: MDTC030-067 MDTC030-041 MDTC030-180 (in the Enpath folder)
		// -- that should leave 117 docs to convert
		
		// SET MIGRATION HISTORY
		// get all versions of docs with getAllVersionsOfDocuments()
		// -- outputfile: test-crmsdo-documents-list-ALLVERSIONS[117].xml
		// -- 117 matches indicates no versioning has occurred on these documents either.
		// -- state appears to be "Document" for all of them
		// ooops, didn't check for locked documents... running checkDocumentsForLocking()
		// -- outputfile: test-crmsdo-documents-list-LOCKED[1].xml
		// -- one locked document: MDTC050-174
		// -- it disappeared....ahhh, the new document cancel checkout deletes the doc. This is a problem...what to do?
		// Rerun of locked scan, had to manually remove ref to 174 now that it is gone...
		// -- locked output: test-crmsdo-documents-list-LOCKED[0].xml - no locks
		// -- unlocked output: test-crmsdo-documents-list-UNLOCKED[116].xml
		// setting Migration History value MRCS412 on all input documents with setMigrationHistoryOnDocuments()
		// -- output (0 errors): test-crmsdo-documents-list-MIGRATION-SUCCESS[116].xml 
		// manual confirm on one of the docs...
		// -- verified
		
		// CONVERT PDF TO ACRO
		// generate list of pdf-format crmsdo docs that exclude our excluded docs with getPdfDocumentsList()
		// -- query: SELECT r_object_id FROM m_mrcs_crmsdo_document 
		//           WHERE FOLDER('/CRM/CRM Supplier Development Organization',DESCEND) AND 
		//                 r_object_id not in (select r_object_id from m_mrcs_crmsdo_contract) AND 
		//                 object_name not in ('MDTC030-067','MDTC030-041','MDTC030-180') AND a_content_type = 'pdf' ORDER BY r_object_id
		// -- output: test-crmsdo-documents-list-pdfformat-[82].xml
		//getPdfDocumentsList();
		// run pdf->acro, all are in "Document" state, so no renditions run convertPdfFormatDocumentsToAcro()
		// -- output (0 errors): test-crmsdo-documents-list-PDFTOACRO-SUCCESS [82].xml
		// -- dql verifies acro conversion
		// -- manually verifying acrobat opens on an acro doc...confirmed
		//convertPdfFormatDocumentsToAcro();
		
		// CRMSDO conversion should be done now. 
		
		// NOPE, need to check if attached policies are up-to-date with latest version of lifecycle
		
		// take contracts doclist for migration history and run it through the currentpolicyidentifier: getContractsLifecycleReport();
		// -- input: test-crmsdo-contracts-list.xml
		// -- result (all are obsolete): test-crmsdo-contracts-list-LIFECYCLE-obsolete-[56].xml
		// take documents doclist for migration history and run it through the currentpolicyidentifier: getContractsLifecycleReport();
		// -- input: test-crmsdo-documents-list-UNLOCKED[116].xml
		// -- result (all are obsolete): test-crmsdo-documents-list-LIFECYCLE-obsolete-[116].xml
		//getContractList();
		//getDocumentsList();
		// need to process all of them to the latest version of the lifecycle...reattach policies: reattachContractPolicies()
		// -- encountered problems (unable to login errors) due to docbroker issues related to dmsd001 docbroker, needed anil and mike wallach's aid
		// -- successful run (0 errors): test-crmsdo-contracts-list-REATTACHLC-success-[56].xml
		//reattachContractPolicies();
		//reattachDocumentPolicies();
		// reattach on CRMSDO documents: running reattachDocumentPolicies();
		// -- successful run (0 errors): test-crmsdo-documents-list-REATTACHLC-success-[116].xml
		
		// STILL TO DO:
		// - folder and cabinet name changes (CRDM for cabinet, CRDM Supply Chain for CRMSDO root
		// - convert static root folders to crmsdo_commodity_root and crmsdo_other_root
		//   -- /CRM/CRM Supplier Development Organization/Commodity Information --> m_mrcs_folder, mrcs_config = crmsdo_commodity_root and mrcs_application = mrcs_crm_sdo
		//   -- /CRM/CRM Supplier Development Organization/Other Documents --> m_mrcs_folder, mrcs_config = crmsdo_other_root and mrcs_application = mrcs_crm_sdo
		// - postaudit: check Build2 install script for proper object creation
		
		// MOVING folders to new cabinet + application folder name (CRDM Supply Chain)
		// - manually created Commodity Information and Global Documents base grouping folders
		//   moveCommodityInformationFolders(); 41 successes - MAKE SURE YOU UNLINK THE OLD ONE...
		//   moveGlobalDocumentsFolders();
		//   - okay, this can have docs, so needed to change the move code to do IDfSysobjects/be less picky
		//   - one document is locked/checkedout - ohwell...
		
		// checking build2 script objects
		// - cabinet + root folder name change - yes
		// - new value assistance document - NO!
		//   - yeah, these seem all messed up...
		// - acls - discrepancies...
		// - can't find docs in pulse
		
		// Detailed manual + script execution directions
		// 1. installation/docbase object audit
		//    - acls (discrepancies...) new build script...
		//    - groups (discrepancy?) new build script?
		//    - value assistance objects --> fix docapp dynamic query! new build script...
		//    - new cabinet, application root folder, and base grouping folders (Commodity Information, Global Documents) same build script...
		// 2. move folders and docs from old Commodity Information/Other Documents to NEW Commodity Information/Global Documents folders
		// 3. determine number of docs to update 
		//    - SELECT count(*) FROM dm_document WHERE FOLDER('/CRDM/CRDM Supply Organization',DESCEND)
		//    - SELECT count(*) FROM dm_document WHERE FOLDER('/CRDM/CRDM Supply Organization',DESCEND) and r_object_type = 'm_mrcs_crmsdo_contract'
		//    - SELECT count(*) FROM dm_document WHERE FOLDER('/CRDM/CRDM Supply Organization',DESCEND) and r_object_type = 'm_mrcs_crmsdo_document'
		// 4. migration history
		//    - get allversions list
		//    - set migration history to MRCS412
		// 5. pdftoacro
		// 6. lifecycle reattachment
		// 7. locked documents processing...
		
		
		// ----------------------------------   PRODUCTION RUN ------------------------------------------
		//getPdfFormatContracts();
		//getPdfDocumentsList();
		//getContractList();
		//getDocumentsList();
		
		//convertPdfFormatContractsToAcro();
		//convertPdfFormatDocumentsToAcro();
		//reattachContractPolicies();
		//reattachDocumentPolicies();
		
		
	}
	
	//
	//--------------------------------------------------------------------------------------------------
	//                             MOVE/LINK GROUPING FOLDERS 
	//--------------------------------------------------------------------------------------------------
	//
	
	public static void moveGlobalDocumentsFolders() throws Exception
	{
		// begin migration control script - instantiate		
		IDfSession session = BaseClass.getSession();

		// create Commodity Inormation folder manually first...
		// - name == Commodity Information
		// - type == m_mrcs_folder
		// - mrcs_application == mrcs_crm_sdo
		// - mrcs_config == crmsdo_commodity_root

		try {
			
			String commoditybaseid = "0b025b4180002fc8";
			
			List commodityfolders = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM dm_sysobject WHERE ANY i_folder_id = '"+commoditybaseid+"'");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-globaldocsfolders-list-["+commodityfolders.size()+"].xml",commodityfolders);
			
			List success = new ArrayList();
			List error = new ArrayList();

			for (int i=0; i < commodityfolders.size(); i++)
			{
				DocbaseObjectRecord dor = (DocbaseObjectRecord)commodityfolders.get(i);
				BaseClass.sMgr.beginTransaction();
				try {
					IDfSysObject folder = (IDfSysObject)session.getObject(new DfId(dor.objectid));
					folder.unlink("/CRM/CRM Supplier Development Organization/Other Documents");
					folder.link("/CRDM/CRDM Supply Chain/Global Documents");
					folder.save();
					success.add(dor);
					BaseClass.sMgr.commitTransaction();
				} catch (Exception e) {
					BaseClass.sMgr.abortTransaction();
					dor.note = e.getMessage();
					dor.error = e;
					error.add(dor);					
				}
				
			}
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-MOVEglobaldocs-error-["+error.size()+"].xml",error);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-MOVEglobaldocs-success-["+success.size()+"].xml",success);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
	public static void moveCommodityInformationFolders() throws Exception
	{
		// begin migration control script - instantiate		
		IDfSession session = BaseClass.getSession();

		// create Commodity Inormation folder manually first...
		// - name == Commodity Information
		// - type == m_mrcs_folder
		// - mrcs_application == mrcs_crm_sdo
		// - mrcs_config == crmsdo_commodity_root

		try {
			
			String commoditybaseid = "0b025b4180002fc7";
			
			List commodityfolders = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_folder WHERE ANY i_folder_id = '"+commoditybaseid+"'");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-commodityinfofolders-list-["+commodityfolders.size()+"].xml",commodityfolders);
			
			List success = new ArrayList();
			List error = new ArrayList();

			for (int i=0; i < commodityfolders.size(); i++)
			{
				DocbaseObjectRecord dor = (DocbaseObjectRecord)commodityfolders.get(i);
				BaseClass.sMgr.beginTransaction();
				try {
					IDfFolder folder = (IDfFolder)session.getObject(new DfId(dor.objectid));
					folder.unlink("/CRM/CRM Supplier Development Organization/Commodity Information");
					//folder.link("/CRDM/CRDM Supply Chain/Commodity Information");
					folder.save();
					success.add(dor);
					BaseClass.sMgr.commitTransaction();
				} catch (Exception e) {
					BaseClass.sMgr.abortTransaction();
					dor.note = e.getMessage();
					dor.error = e;
					error.add(dor);					
				}
				
			}
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-MOVECommodityInfo-error-["+error.size()+"].xml",error);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-MOVECommodityInfo-success-["+success.size()+"].xml",success);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	
	
	
	//
	//--------------------------------------------------------------------------------------------------
	//                             CONTRACTS+DOCUMENTS LIFECYCLE PROCESSING
	//--------------------------------------------------------------------------------------------------
	//
	
	public static void reattachContractPolicies() throws Exception
	{
		// begin migration control script - instantiate		
		IDfSession session = BaseClass.getSession();
		
		try { 
			List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list[60].xml");
			Map results = ReattachPolicyProcessor.reattachPolicies(session,contracts);
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-REATTACHLC-error-["+error.size()+"].xml",error);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-REATTACHLC-success-["+success.size()+"].xml",success);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	
	public static void reattachDocumentPolicies() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try { 
			List documents = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list[133].xml");
			Map results = ReattachPolicyProcessor.reattachPolicies(session,documents);
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-REATTACHLC-error-["+error.size()+"].xml",error);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-REATTACHLC-success-["+success.size()+"].xml",success);
		
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	// this script gets the initial list of CRMSDO contracts to convert 
	public static void getDocumentsLifecycleReport() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// - 
		List documents = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-UNLOCKED[116].xml");
		Map results = CurrentPolicyIdentifier.getLifecycleMappings(session,documents);
		
		List valid = (List)results.get("valid");
		List invalid = (List)results.get("invalid");
		List obsolete = (List)results.get("obsolete");
		List none = (List)results.get("none");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-LIFECYCLE-valid-["+valid.size()+"].xml",valid);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-LIFECYCLE-invalid-["+invalid.size()+"].xml",invalid);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-LIFECYCLE-none-["+none.size()+"].xml",none);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-LIFECYCLE-obsolete-["+obsolete.size()+"].xml",obsolete);
		
		BaseClass.sMgr.release(session);
	}

	// this script gets the initial list of CRMSDO contracts to convert 
	public static void getContractsLifecycleReport() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// - 
		List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list.xml");
		Map results = CurrentPolicyIdentifier.getLifecycleMappings(session,contracts);
		
		List valid = (List)results.get("valid");
		List invalid = (List)results.get("invalid");
		List obsolete = (List)results.get("obsolete");
		List none = (List)results.get("none");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-LIFECYCLE-valid-["+valid.size()+"].xml",valid);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-LIFECYCLE-invalid-["+invalid.size()+"].xml",invalid);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-LIFECYCLE-none-["+none.size()+"].xml",none);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-LIFECYCLE-obsolete-["+obsolete.size()+"].xml",obsolete);
		
		BaseClass.sMgr.release(session);		
	}
	
	
	
	
	
	//
	//--------------------------------------------------------------------------------------------------
	//                              DOCUMENTS PROCESSING METHODS
	//--------------------------------------------------------------------------------------------------
	//

	// this script gets the initial list of CRMSDO contracts to convert 
	public static void getPdfDocumentsList() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// - 
		List documents = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_crmsdo_document WHERE r_object_type = 'm_mrcs_crmsdo_document' AND FOLDER('/CRDM/CRDM Supply Chain',DESCEND) AND a_content_type = 'pdf' AND r_object_id not in (select r_object_id from m_mrcs_crmsdo_contract) ORDER BY r_object_id");
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-pdfformat-["+documents.size()+"].xml",documents);
		
		BaseClass.sMgr.release(session);		
	}

	
	// this script gets the initial list of CRMSDO contracts to convert 
	public static void getDocumentsList() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// - 
		List documents = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_crmsdo_document WHERE r_object_type = 'm_mrcs_crmsdo_document' AND FOLDER('/CRDM/CRDM Supply Chain',DESCEND) AND r_object_id not in (select r_object_id from m_mrcs_crmsdo_contract) ORDER BY r_object_id");
		documents = DocumentStateIdentifier.populateDocumentState(session,documents); 
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list["+documents.size()+"].xml",documents);
		
		BaseClass.sMgr.release(session);		
	}
	
	public static void getAllVersionsOfDocuments() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// load list of documents to convert
		List documents = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list[120].xml");
		
		documents = AllVersionsIdentifier.getAllVersions(session,documents);
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-ALLVERSIONS["+documents.size()+"].xml",documents);
		
		BaseClass.sMgr.release(session);
		
	}
	
	public static void checkDocumentsForLocking() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// load list of documents to convert
		List documents = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-ALLVERSIONS[117].xml");
		
		Map results = LockedDocumentsIdentifier.findLockedDocuments(session,documents);
		
		List locked = (List)results.get("locked");
		List unlocked = (List)results.get("unlocked");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-LOCKED["+locked.size()+"].xml",locked);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-UNLOCKED["+unlocked.size()+"].xml",unlocked);
		
		BaseClass.sMgr.release(session);
		
	}
	
	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnDocuments() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// load list of documents to convert
		List documents = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-UNLOCKED[116].xml");
		
		// set migration history attribute on all versions of all documents
		// - this is a config-specific operation, so we will code it in this class
		Map results = MigrationHistoryProcessor.setMigrationHistory(session,documents,"MRCS412");
		
		List success = (List)results.get("success");
		List error = (List)results.get("error");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-MIGRATION-SUCCESS["+success.size()+"].xml",success);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-MIGRATION-ERROR["+error.size()+"].xml",error);

		BaseClass.sMgr.release(session);		
		
	}
	
	// this script gets the initial list of CRMSDO contracts to convert 
	public static void convertPdfFormatDocumentsToAcro() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to pdf->acro convert
		List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-pdfformat-[91].xml");
		
		Map renderedstates = new HashMap(); //renderedstates.put("Approved",""); renderedstates.put("In-Approval","");
		Map unrenderedstates = new HashMap(); unrenderedstates.put("Document","");
		
		Map results = PdfToAcroProcessor.convertPdfToAcro(session,renderedstates,unrenderedstates,contracts);
		
		List success = (List)results.get("success");
		List error = (List)results.get("error");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-PDFTOACRO-SUCCESS ["+success.size()+"].xml",success);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-documents-list-PDFTOACRO-ERROR ["+error.size()+"].xml",error);

		BaseClass.sMgr.release(session);		
	}
	



	
	
	//
	//--------------------------------------------------------------------------------------------------
	//                              CONTRACTS PROCESSING METHODS
	//--------------------------------------------------------------------------------------------------
	//	
	
	// this script gets the initial list of CRMSDO contracts to convert 
	public static void convertPdfFormatContractsToAcro() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to pdf->acro convert
		List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-pdf-format [57].xml");
		
		Map renderedstates = new HashMap(); renderedstates.put("Approved",""); renderedstates.put("In-Approval","");
		Map unrenderedstates = new HashMap(); unrenderedstates.put("In-Progress","");
		
		Map results = PdfToAcroProcessor.convertPdfToAcro(session,renderedstates,unrenderedstates,contracts);
		
		List success = (List)results.get("success");
		List error = (List)results.get("error");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-PDFTOACRO-SUCCESS ["+success.size()+"].xml",success);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-PDFTOACRO-ERROR ["+error.size()+"].xml",error);

		BaseClass.sMgr.release(session);		
	}
	
	// this script gets the initial list of CRMSDO contracts to convert 
	public static void getPdfFormatContracts() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to pdf->acro convert
		List contracts = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_crmsdo_contract WHERE r_object_type = 'm_mrcs_crmsdo_contract' AND a_content_type = 'pdf' and FOLDER('/CRDM/CRDM Supply Chain',DESCEND) ORDER BY r_object_id");
		contracts = DocumentStateIdentifier.populateDocumentState(session,contracts); // note - this was very slow for only 88 documents...
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-pdf-format ["+contracts.size()+"].xml",contracts);
		
		BaseClass.sMgr.release(session);		
	}


	// this script gets the initial list of CRMSDO contracts to convert 
	public static void getContractList() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// preconversion...
		//List contracts = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_crmsdo_contract WHERE r_object_type = 'm_mrcs_crmsdo_contract' AND FOLDER('/CRM/CRM Supplier Development Organization',DESCEND) ORDER BY r_object_id");
		// postconversion...
		List contracts = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_crmsdo_contract WHERE r_object_type = 'm_mrcs_crmsdo_contract' AND FOLDER('/CRDM/CRDM Supply Chain',DESCEND) ORDER BY r_object_id");
		
		contracts = DocumentStateIdentifier.populateDocumentState(session,contracts); // note - this was very slow for only 88 documents...
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list["+contracts.size()+"].xml",contracts);
		
		BaseClass.sMgr.release(session);		
	}
	
	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnContracts() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// load list of documents to convert
		List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-setmigration-ERROR.xml");
		
		// set migration history attribute on all versions of all documents
		// - this is a config-specific operation, so we will code it in this class
		Map results = MigrationHistoryProcessor.setMigrationHistory(session,contracts,"MRCS412");
		
		List success = (List)results.get("success");
		List error = (List)results.get("error");
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-setmigration-SUCCESS["+success.size()+"].xml",success);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-setmigration-ERROR["+error.size()+"].xml",error);

		BaseClass.sMgr.release(session);		
		
	}
	
	
	// about 6 records per second...
	public static void getAllVersionsOfContracts() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// load list of documents to convert
		List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list.xml");
		
		contracts = AllVersionsIdentifier.getAllVersions(session,contracts);
		
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-crmsdo-contracts-list-ALLVERSIONS["+contracts.size()+"].xml",contracts);
		
		BaseClass.sMgr.release(session);
		
	}
	
	

}
