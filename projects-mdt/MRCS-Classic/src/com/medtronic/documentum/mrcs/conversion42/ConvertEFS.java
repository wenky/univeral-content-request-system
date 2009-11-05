package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfACL;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.DfId;

public class ConvertEFS 
{
	public static void main(String[] args) throws Exception
	{
		// the big bertha...
		
		// -- AUDIT PHASE --
		// MAKE SURE mradmin IS IN mrcs_efs_admin 
		// Check document types for overlap...
		// - no overlap, all four descend from m_mrcs_document directly
		// Required Operations:
		// - migration history, acro->pdf, obs/retired, LC reattach, approved reconstruction
		// - rootfolder -> basefolder conversions
		// Migration History: Basically 2/16/06 or thereabouts for wallach conversion of EFS docs
		// TDS EFS migration:
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) <= DATE('02/23/2006') and FOLDER('/CRM/TDS EFS',descend) == 0 documents
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('02/24/2006') and FOLDER('/CRM/TDS EFS',descend) == 21000 documents!
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('02/25/2006') and FOLDER('/CRM/TDS EFS',descend) == 850 documents
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('02/26/2006') and FOLDER('/CRM/TDS EFS',descend) == 8016 documents
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('02/27/2006') and FOLDER('/CRM/TDS EFS',descend) == 13 documents
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('02/28/2006') and FOLDER('/CRM/TDS EFS',descend) == 136 documents
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('03/01/2006') and FOLDER('/CRM/TDS EFS',descend) == 14 documents
		// EP Systems migration:
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) <= DATE('02/23/2006') and FOLDER('/CRM/EP Systems',descend) == 0
		//   select count(*) from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) = DATE('02/24/2006') and FOLDER('/CRM/EP Systems',descend) == 0
		
		// -- EXECUTION of MIGRAITON HISTORY --
		// Wallach did the EFS migration on the weekend of 2/26/2006, so we will set all doc versions created that day or earlier to pre-MRCS
		// running getListOfPre412EFSDocumentsAllVersions(); == 29866 docs
		// setMigrationHistoryOnPre412Docs(); == ALL SUCCESSFUL! Excellent!
		// get post-conversion docs
		// getListOfPostMigrationEFSDocumentsAllVersions();
		// encountered an error -- setMigrationHistoryOnPostMigrationDocs();
		// use splitterprocessor to split it
		//splitPostMigrationDocList();
		//setMigrationHistoryOnPostMigrationDocsD1();
		//setMigrationHistoryOnPostMigrationDocsD2();
		//setMigrationHistoryOnPostMigrationDocsD0();
		//setMigrationHistoryOnPostMigrationDocsD3();
		// - so runs were more or less successful in split batches of 10,000 docs. May need to split further for the intensive reconstruction of approved copies...
		
		// -- AUDIT FOR LIFECYCLE REATTACHMENT --
		// this query is very interesting: 
		//   select * from m_mrcs_document where any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRM/TDS EFS',descend)) and r_policy_id != '0000000000000000'
		//   ...it shouldn't return any but returned two documents...a good audit/confirmation query...
		//   ...I think these two docs (one is "archived" and one is "in-progress" are hack jobs by the support team...
		// this query should give all non-approved copies (source docs):
		//   select r_object_id from m_mrcs_document where any i_folder_id not in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRM/TDS EFS',descend)) and r_policy_id != '0000000000000000'
		// should be identical to (removed the policyid condition):
		//   select r_object_id from m_mrcs_document where any i_folder_id not in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRM/TDS EFS',descend))
		// I think this is the perfect query:
		//   select r_object_id from m_mrcs_document where r_policy_id != '0000000000000000' and FOLDER('/CRM/TDS EFS',descend) and NOT (any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRM/TDS EFS',descend)))
		// getListOfSourceEFSDocuments();
		// exec...with splitter...  died on id 09025b41800b72ab
		// processPolicyReattachment(); - 33 minutes per 5000 document batch
		
		// -- APPROVED COPY RECONSTRUCTION --
		// this query identifies approved copies in TDS EFS:
		//   select r_object_id from m_mrcs_document where any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRM/TDS EFS',descend))
		//   - getListOfApprovedCopyDocuments(); == 29015docs
		// move/backup...done successfully (100%)
		// get list of source documents
		//   - getListOfSourceEFSCentralDocuments(); = 20956
		//   - getListOfSourceEFSMQADocuments(); = 8164
		// rebuild approved copies
		//   rebuildCentralFileApprovedCopies();
		//   rebuildMQAApprovedCopies();
		
		// -- RET/OBS FLAG FLIP --
		// this should be easy, since all the docs with the relevant lifecycles use "Retired" and "Archived", and we only need to do "Retired" processing...
		// - get list of all efs documents with "active" lifecycles - CF_LIFECYCLE, EP_LIFECYCLE, MQA_LIFECYCLE
		//   select r_object_id from m_mrcs_document where FOLDER('/CRM/TDS EFS',descend) and r_policy_id in (select r_object_id from dm_policy(all) where object_name in ('mrcs_efs_central_lc','mrcs_efs_epsys_lc','mrcs_efs_mqa_lc'))
		//     getListOfObsRetDocs(); == 29120 docs
		//   processRetiredEFSDocs();
		
		// -- PDF to ACRO conversion --
		//
		//getPdfDocumentsList();
		//convertPdfFormatDocumentsToAcro();
		// - weird error with about 6500 docs left, (file size mismatches), I'll retry
		//RETRYconvertPdfFormateDocumentsToAcro();
		
		// --------------------------------------------------------------------------------------------
		// ---------  BEGIN MRCS_TRAINING test upgrade
		// --------------------------------------------------------------------------------------------
		
		// LC reattach
		//getListOfSourceEFSDocuments();
		//processPolicyReattachment();
		
		// Approved Copy Reconstruction
		//getListOfApprovedCopyDocuments();
		//processApprovedCopyBackup();
		//getListOfSourceEFSCentralDocuments();
		//getListOfSourceEFSMQADocuments();
		//rebuildCentralFileApprovedCopies();
		//rebuildMQAApprovedCopies();
		
		// RET/OBS flipping
		//getListOfObsRetDocs();
		//processRetiredEFSDocs();
		
		//pdf to acro
		//getPdfDocumentsList();
		//convertPdfFormatDocumentsToAcro();
		
		//-------------------------
		// logging test
		//-------------------------
		
		// -__________________-------------PROOODUCTION RUN! -------------------------------------

		// LC reattach
		//getListOfSourceEFSDocuments();
		//processPolicyReattachment();
		
		// RET/OBS flipping
		//getListOfObsRetDocs();
		//processRetiredEFSDocs();
		
		//pdf to acro
		//getPdfDocumentsList();
		//convertPdfFormatDocumentsToAcro();

		// Approved Copy Reconstruction
		//getListOfApprovedCopyDocuments();
		//getListOfSourceEFSCentralDocuments();
		//getListOfSourceEFSMQADocuments();
		
		//processApprovedCopyBackup();
		//rebuildCentralFileApprovedCopies();
		//rebuildMQAApprovedCopies();
		
		// - post-production run to create approved copy for some objects: BL0004201
		//rebuildCentralFileApprovedCopies();

		//convertPdfFormatDocumentsToAcro();
		//rebuildCentralFileApprovedCopies();
		
		// Yaling fan run
		//convertPdfFormatDocumentsToAcro();
		//processPolicyReattachment();
		
		//BL15880
		// - cancelled checkout...
		//processPolicyReattachment();
		
		// bunch of 2.0 In-Progress docs that need fixing...
		//convertPdfFormatDocumentsToAcro();
		//rebuildCentralFileApprovedCopies();
		
		
	
	}
	
	
	//
	// PDF to ACRO conversion
	//
	
	
	public static void getPdfDocumentsList() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// - 
		List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document where a_content_type = 'pdf' and r_policy_id in (select r_object_id from dm_policy(all) where object_name in ('mrcs_efs_central_lc','mrcs_efs_epsys_lc','mrcs_efs_mqa_lc')) AND FOLDER('/CRDM/TDS EFS',descend) and NOT (any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRDM/TDS EFS',descend)))");			
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO-["+doclist.size()+"].xml",doclist);
		
		BaseClass.sMgr.release(session);		
	}

	public static void convertPdfFormatDocumentsToAcro() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		Map renderedstates = new HashMap(); renderedstates.put("Approved",""); renderedstates.put("In-Approval",""); renderedstates.put("Archived","");
		Map unrenderedstates = new HashMap(); unrenderedstates.put("In-Progress","");
		
		try {
			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO.xml");
			//List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO-[test].xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(3000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = PdfToAcroProcessor.convertPdfToAcro(session,renderedstates,unrenderedstates,doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}

	public static void RETRYconvertPdfFormatDocumentsToAcro() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		Map renderedstates = new HashMap(); renderedstates.put("Approved",""); renderedstates.put("In-Approval",""); renderedstates.put("Archived","");
		Map unrenderedstates = new HashMap(); unrenderedstates.put("In-Progress","");
		
		try {
			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--RETRY[6773].xml");
			fulldoclist = ClearErrorProcessor.clearErrorMessages(fulldoclist);
						
			// split the doclist
			Map splits = SplitterProcessor.splitList(3000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--RETRY--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = PdfToAcroProcessor.convertPdfToAcro(session,renderedstates,unrenderedstates,doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--RETRY--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--RETRY--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-PDF2ACRO--RETRY-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}

	
	
	//
	// RET/OBS settings
	//
	
	public static void getListOfObsRetDocs()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document where FOLDER('/CRDM/TDS EFS',descend) and r_policy_id in (select r_object_id from dm_policy(all) where object_name in ('mrcs_efs_central_lc','mrcs_efs_epsys_lc','mrcs_efs_mqa_lc'))");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-RETIREDDOCS-["+doclist.size()+"].xml",doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	public static void processRetiredEFSDocs() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-RETIREDDOCS-[29355].xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(5000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-RETIREDDOCS--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"ObsoleteNotUsedInEFS","Retired",doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-RETIREDDOCS--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-RETIREDDOCS--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-RETIREDDOCS-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
	//
	// APproved copy reconstruction
	//
	
	public static void getListOfSourceEFSCentralDocuments()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_central_document where r_policy_id != '0000000000000000' and FOLDER('/CRDM/TDS EFS',descend) and NOT (any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRDM/TDS EFS',descend)))");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-Central-["+doclist.size()+"].xml",doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	public static void getListOfSourceEFSMQADocuments()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_mqa_document where r_policy_id != '0000000000000000' and FOLDER('/CRDM/TDS EFS',descend) and NOT (any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRDM/TDS EFS',descend)))");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-MQA-["+doclist.size()+"].xml",doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	
	public static void rebuildCentralFileApprovedCopies() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			
			HashMap approvalstates = new HashMap(); approvalstates.put("Approved",""); approvalstates.put("Retired","");
			List copyattrs = new ArrayList();
			copyattrs.add("title");
			copyattrs.add("authors");
			copyattrs.add("keywords");
			copyattrs.add("m_mrcs_central_author_date");
			copyattrs.add("m_mrcs_central_dhf");
			copyattrs.add("m_mrcs_central_doc_type");
			copyattrs.add("m_mrcs_central_func_group");
			copyattrs.add("m_mrcs_central_phase");
			copyattrs.add("m_mrcs_central_ref_number");

			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-Central.xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(1000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-Central--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = RebuildApprovedCopiesProcessor.rebuildApprovedCopies(session,doclist,approvalstates,"m_mrcs_central_document",copyattrs,"pdf","Approved","Approved Document","CopyToRelationship","mrcs_central_approved");
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-Central--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-Central--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-Central-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}

	public static void rebuildMQAApprovedCopies() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			
			HashMap approvalstates = new HashMap(); approvalstates.put("Approved",""); approvalstates.put("Retired","");
			List copyattrs = new ArrayList();
			copyattrs.add("title");
			copyattrs.add("authors");
			copyattrs.add("keywords");
			copyattrs.add("m_mrcs_mqa_part_number");
			copyattrs.add("m_mrcs_mqa_author_date");
			copyattrs.add("m_mrcs_mqa_document_type");
			copyattrs.add("m_mrcs_mqa_supplier");

			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"Fix-MQA.xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(1000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-MQA--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = RebuildApprovedCopiesProcessor.rebuildApprovedCopies(session,doclist,approvalstates,"m_mrcs_mqa_document",copyattrs,"pdf","Approved","Approved Document","CopyToRelationship","mrcs_mqa_approved");
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-MQA--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-MQA--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-REBUILDAPPROVEDCOPIES-MQA-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}

	
	
	public static void getListOfApprovedCopyDocuments()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document where any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRDM/TDS EFS',descend))");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-APPROVEDCOPYBACKUPLIST-["+doclist.size()+"].xml",doclist);
		} finally {BaseClass.sMgr.release(session);}				
	}
	
	public static void processApprovedCopyBackup() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-APPROVEDCOPYBACKUPLIST-[29321].xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(5000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-APPROVEDCOPYBACKUPLIST--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = RebuildApprovedCopiesProcessor.backupExistingApprovedCopies(session,"/EFS_Staging/TDSEFS_MRCS421/MainRunBackupFolder"+i,"mrcs_hide_old_efs_copies",doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-APPROVEDCOPYBACKUPLIST--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-APPROVEDCOPYBACKUPLIST--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-APPROVEDCOPYBACKUPLIST-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}

	

	
	//
	// Reattach Policies	
	//
	
	public static void getListOfSourceEFSDocuments()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document where r_policy_id != '0000000000000000' and FOLDER('/CRDM/TDS EFS',descend) and NOT (any i_folder_id in (select distinct r_object_id from dm_folder where object_name = 'Approved' and FOLDER('/CRDM/TDS EFS',descend)))");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-POLICYREATTACH-["+doclist.size()+"].xml",doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	public static void processPolicyReattachment() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"Fix-BL0015880.xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(5000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;
			
				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = ReattachPolicyProcessor.reattachPolicies(session,doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-POLICYREATTACH--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-POLICYREATTACH--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-POLICYREATTACH-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
	
	//
	// Migration History
	//
	
	public static void getListOfPre412EFSDocumentsAllVersions()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List pre412doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) <= DATE('02/26/2006') and FOLDER('/CRM/TDS EFS',descend)");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-pre412-["+pre412doclist.size()+"].xml",pre412doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPre412Docs() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List pre412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-pre412-[29866].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,pre412docs,"pre-MRCS");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-pre412-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-pre412-ERROR["+error.size()+"].xml",error);
	
		} finally {BaseClass.sMgr.release(session);}		
	}

	public static void getListOfPostMigrationEFSDocumentsAllVersions()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List pre412doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document(all) where DATEFLOOR(day,r_creation_date) > DATE('02/26/2006') and FOLDER('/CRM/TDS EFS',descend)");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-["+pre412doclist.size()+"].xml",pre412doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	public static void splitPostMigrationDocList() throws Exception
	{
		List docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-[39978].xml");
		Map split = SplitterProcessor.splitList(10000,docs);
		List d1 = (List)split.get("output1");
		List d2 = (List)split.get("output2");
		List d3 = (List)split.get("output3");
		List d4 = (List)split.get("output4");
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D0["+d1.size()+"].xml",d1);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D1["+d2.size()+"].xml",d2);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D2["+d3.size()+"].xml",d3);
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D3["+d4.size()+"].xml",d4);
		
	}
	
	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPostMigrationDocsD1() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List pre412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D1[10001].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,pre412docs,"MRCS412");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D1-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D1-ERROR["+error.size()+"].xml",error);
	
		} finally {BaseClass.sMgr.release(session);}		
	}

	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPostMigrationDocsD2() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List pre412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D2[10001].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,pre412docs,"MRCS412");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D2-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D2-ERROR["+error.size()+"].xml",error);
	
		} finally {BaseClass.sMgr.release(session);}		
	}

	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPostMigrationDocsD0() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List pre412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D0[10001].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,pre412docs,"MRCS412");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D0-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D0-ERROR["+error.size()+"].xml",error);
	
		} catch (Exception e) {
			int i=1;
			int j=2;
			i=i+j;
		}
		finally {BaseClass.sMgr.release(session);}		
	}
	
	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPostMigrationDocsD3() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List pre412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D3[9975].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,pre412docs,"MRCS412");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D3-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-SETMIGRATION-postmigration-D3-ERROR["+error.size()+"].xml",error);
	
		} catch (Exception e) {
			int i=1;
			int j=2;
			i=i+j;
		}
		finally {BaseClass.sMgr.release(session);}		
	}
	
	public static void moveFolders()
	{
		// assumes the TDS EFS root folders for Central File and MQA have been created
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// get list of folders 
			List projectfilesfolders = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_folder where FOLDER('/CRM/TDS EFS/Central File/Project Files')");
			List literaturefolders = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_folder where FOLDER('/CRM/TDS EFS/Central File/Literature')");
			List productassurancetestfolders = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_folder where FOLDER('/CRM/TDS EFS/Central File/Product Assurance Test')");
			List partfilesfolders = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_folder where FOLDER('/CRM/TDS EFS/MQA/Part File')");
			List supplierauditfolders = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_folder where FOLDER('/CRM/TDS EFS/MQA/Supplier Audits')");
			List supplierfilefolders = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_folder where FOLDER('/CRM/TDS EFS/MQA/Supplier File')");
						
			List foldermovelist = new ArrayList();
			foldermovelist.add(processFolderMove(session,projectfilesfolders,"/CRM/TDS EFS/Central File/Project Files","/CRDM/TDS EFS/Central File/Project Files"));
			foldermovelist.add(processFolderMove(session,literaturefolders,"/CRM/TDS EFS/Central File/Literature","/CRDM/TDS EFS/Central File/Literature"));
			foldermovelist.add(processFolderMove(session,productassurancetestfolders,"/CRM/TDS EFS/Central File/Product Assurance Test","/CRDM/TDS EFS/Central File/Product Assurance Test"));
			foldermovelist.add(processFolderMove(session,partfilesfolders,"/CRM/TDS EFS/MQA/Part File","/CRDM/TDS EFS/MQA/Part File"));
			foldermovelist.add(processFolderMove(session,supplierauditfolders,"/CRM/TDS EFS/MQA/Supplier Audits","/CRDM/TDS EFS/MQA/Supplier Audits"));
			foldermovelist.add(processFolderMove(session,supplierfilefolders,"/CRM/TDS EFS/MQA/Supplier File","/CRDM/TDS EFS/MQA/Supplier File"));
						
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-MOVEFOLDERS-results.xml",foldermovelist);
	
		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-efs-movefolders-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
	}
	
	public static Map processFolderMove(IDfSession session, List folders, String unlink, String link) throws Exception
	{
		List success = new ArrayList();
		List error = new ArrayList();

		for (int i=0; i < folders.size(); i++)
		{
			DocbaseObjectRecord dor = (DocbaseObjectRecord)folders.get(i);
			BaseClass.sMgr.beginTransaction();
			try {
				IDfFolder folder = (IDfFolder)session.getObject(new DfId(dor.objectid));
				folder.unlink(unlink);
				folder.link(link);
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
		
		HashMap results = new HashMap(); results.put("error",error); results.put("success",success);
		return results;

	}

}