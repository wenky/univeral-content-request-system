package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfId;

public class ConvertNPP 
{
	public static void main(String[] args) throws Exception
	{
		// -- AUDIT PHASE --
		// make sure mradmin has delete/write on obsolete docs...
		// Check document types for inheritence
		// - one one type: m_mrcsnpp_document
		// Determine required operations:
		// - migration history, acro->pdf, obs/retired, LC reattach, WF rollback fix
		// Check lifecycles: all are reattachable at all normal states (not rejected though)
		// - okay
		// Deliverable: identify doclists for conversion operations processing
		// - need to exclude all approved copies
		// Need to convert rootfolders to BaseFolder grouping folders...
		// - TODO TODO TODO TODO TODO TODO TODO TODO
		// - value assistance docs
		// - acls, groups, etc. 
		// - folder renames
		// migration history:
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) = DATE('03/27/2006') == 137 docs
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) = DATE('03/28/2006') == 364 docs
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) = DATE('03/29/2006') == 2251 docs
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) = DATE('03/30/2006') == 2148 docs
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) = DATE('03/31/2006') == 399 docs
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) = DATE('04/01/2006') == 0 docs
		// - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) > DATE('04/01/2006') AND DATEFLOOR(day,r_creation_date) < DATE('04/07/2006') == 1 doc
		// - so, can we say that all docs before april 01 2006 are pre-mrcs, all after are MRCS412?
		// - check exclusion by FOLDER:
		//   - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) < DATE('04/01/2006') and FOLDER('/Neurological',descend) == 3154
		//   - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) < DATE('04/01/2006') and FOLDER('/NPP_Staging',descend) == 2144
		//   - select count(*) from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) < DATE('04/01/2006') == 5306 (remaining 8 are 7 templates + a messed up document (09025b4180064909))
		// pdf->acro:
		// - statemap - 
		//   - In-Progress : direct conversion...overwrite format...
		//   - In-Approval : acro + pdf rendition  --> do not modify these until we determine --> active workflows, would disrupt hash values perhaps?
		//   - Approved : acro + pdf rendition --> grab acro source from last In-Progress before In-Approval
		//   - Effective : acro + pdf rendition --> grab acro source from last In-Progress before In-Approval
		//   - Obsolete : acro + pdf rendition --> grab acro source from last In-Progress before In-Approval
		// obs/ret:
		//   - set on Effective, Retired, Obsolete
		// lifecycle reattachment
		// workflow cleanup
		// - determine docs in workflow
		
		// EXECUTION PHASE

		// -->> Set Migration History <<--
		// LESSON LEARNED >> make sure your conversion user (mradmin or dmaster) is in mrcs_npp_system (takes care of no access rights errors we found, but not locked docs)
		// getListOfPre412NPPDocumentsAllVersions();
		// - select r_object_id from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) < DATE('04/01/2006') and FOLDER('/Neurological',descend)
		// - now process them by setting all those docs to pre-MRCS 
		// setMigrationHistoryOnPre412Docs();
		// - results: 3149 successes, 5 errors (two locked, three no write access...checking...)
		// anyway, time to set MRCS412 migration history
		// getListOfPostMigrationNPPDocumentsAllVersions();
		// - select r_object_id from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) >= DATE('04/01/2006') and FOLDER('/Neurological',descend)
		// setMigrationHistoryOnPostWallach412Docs();
		// - results: 29 errors (either no access or locked...)
		
		// -->> pdf to acro Format Conversion <<--
		// remember to exclude approved copies....crap...
		// check doctypes for ones that actually can have pdf source
		// -- in this case four out of eight doctypes/lifecycles: Neuro forms, external and MGU forms, external
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE FOLDER('/Neurological',descend) and a_content_type = 'pdf' and mrcs_config = 'FormDocument'  ==   582 docs
		// - um, according to my searches, there are absolutely NO pdf-source docs in the system...???
		// - no conversion to perform I guess...
		
		// -->> OBS/RET setting <<--
		// untested/new operation: SET obsolete/retired flags on docs... 
		// six lifecycles to process: Neuro forms maps docs, MGU forms maps docs
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_lifecycle')     Obsolete Retired
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_forms_lc')      Form-Obsolete Form-Retired
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_maps_lc')       Map-Obsolete Map-Retired
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_mgu_lifecycle') Obsolete Retired
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_mgu_forms_lc')  Form-Obsolete Form-Retired
		// - SELECT count(*) FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_mgu_maps_lc')   Map-Obsolete Map-Retired
		// getListOfRetObsNPPDocs();
		// processRetObsNPPDocs(); --> eyeball check of docs with obsolete and retired states in their version histories worked. eyeball of full execution looks good (only occaisional obs/ret found, which seems right, most of it wouldn't have been versioned in our system yet)
		
		// --> Policy reattachment <--
		// this should be a basic query:
		// select r_object_id from m_mrcs_document where FOLDER('/Neurological',descend) and r_policy_id != '0000000000000000'
		// getPolicyReattachmentDoclist(); - 1268 docs...
		
		// --------------------------------- MRCS_Training processing --------------------------
		// do policy reattachemnt
		//getPolicyReattachmentDoclist();
		//processPolicyReattachment();
		
		// do pdf->acro conversion
		//getPdfAcroList();rm
		//convertPdfFormatDocumentsToAcro();
		
		// do obs/ret processing
		//getListOfRetObsNPPDocs();
		//processRetObsNPPDocs();
		
		// ------------
		// log testing
		
		// --------------- PROD RUN --------------------
		//getPolicyReattachmentDoclist();
		//getPdfAcroList();
		//getListOfRetObsNPPDocs();

		//processPolicyReattachment();
		//processRetObsNPPDocs();
		
		// ADM1207
		//processPolicyReattachment();
		

	}
	
	public static void getPdfAcroList()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List pre412doclist = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE FOLDER('/Neurological',descend) and a_content_type = 'pdf' and mrcs_config = 'FormDocument'");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-PDF2ACRO-["+pre412doclist.size()+"].xml",pre412doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	public static void convertPdfFormatDocumentsToAcro() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		Map renderedstates = new HashMap(); renderedstates.put("Form-Approval",""); renderedstates.put("Form-Approved",""); renderedstates.put("Form-Effective","");
		Map unrenderedstates = new HashMap(); unrenderedstates.put("Form-Draft","");
		
		try {
			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-PDF2ACRO-[3].xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(3000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-PDF2ACRO--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = PdfToAcroProcessor.convertPdfToAcro(session,renderedstates,unrenderedstates,doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-PDF2ACRO--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-PDF2ACRO--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-PDF2ACRO-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
	public static void getPolicyReattachmentDoclist()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List pre412doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcsnpp_document where FOLDER('/Neurological',descend) and r_policy_id != '0000000000000000'");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-REATTACHPOLICIES-["+pre412doclist.size()+"].xml",pre412doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
	public static void processPolicyReattachment() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List mrcs412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"Fix-ADM1207.xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = ReattachPolicyProcessor.reattachPolicies(session,mrcs412docs);
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-REATTACHPOLICIES-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-REATTACHPOLICIES-ERROR["+error.size()+"].xml",error);

		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
	public static void getListOfRetObsNPPDocs()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			
			List nppdocs = DqlQueryIdentifier.executeQuery (session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_lifecycle') and FOLDER('/Neurological',descend)");			
			List nppforms = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_forms_lc') and FOLDER('/Neurological',descend)");			
			List nppmaps= DqlQueryIdentifier.executeQuery  (session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_maps_lc') and FOLDER('/Neurological',descend)");			
			List mgudocs = DqlQueryIdentifier.executeQuery (session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_mgu_lifecycle') and FOLDER('/Neurological',descend)");			
			List mguforms = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_mgu_forms_lc') and FOLDER('/Neurological',descend)");			
			List mgumaps= DqlQueryIdentifier.executeQuery  (session,"SELECT r_object_id FROM m_mrcsnpp_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_npp_mgu_maps_lc') and FOLDER('/Neurological',descend)");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppdocs["+nppdocs.size()+"].xml",nppdocs);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppforms["+nppforms.size()+"].xml",nppforms);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppmaps["+nppmaps.size()+"].xml",nppmaps);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgudocs["+mgudocs.size()+"].xml",mgudocs);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mguforms["+mguforms.size()+"].xml",mguforms);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgumaps["+mgumaps.size()+"].xml",mgumaps);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	public static void processRetObsNPPDocs() throws Exception
	{
		IDfSession session = BaseClass.getSession();	
		try {
			
			List nppdocs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppdocs[640].xml");
			List nppforms = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppforms[621].xml");
			List nppmaps = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppmaps[1].xml");
			List mgudocs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgudocs[111].xml");
			List mguforms = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mguforms[0].xml");
			List mgumaps = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgumaps[0].xml");

			Map results; List success, error;
			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Obsolete","Retired",nppdocs);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppdocs-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppdocs-ERROR["+error.size()+"].xml",error);

			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Form-Obsolete","Form-Retired",nppforms);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppforms-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppforms-ERROR["+error.size()+"].xml",error);

			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Map-Obsolete","Map-Retired",nppmaps);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppmaps-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-nppmaps-ERROR["+error.size()+"].xml",error);

			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Obsolete","Retired",mgudocs);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgudocs-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgudocs-ERROR["+error.size()+"].xml",error);

			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Form-Obsolete","Form-Retired",mguforms);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mguforms-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mguforms-ERROR["+error.size()+"].xml",error);

			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Map-Obsolete","Map-Retired",mgumaps);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgumaps-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-RETOBS-mgumaps-ERROR["+error.size()+"].xml",error);
			
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	

	public static void getListOfPostMigrationNPPDocumentsAllVersions()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List postmigrationdoclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) >= DATE('04/01/2006') and FOLDER('/Neurological',descend)");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-postwallachmigration-["+postmigrationdoclist.size()+"].xml",postmigrationdoclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPostWallach412Docs() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List mrcs412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-postwallachmigration-[3819].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,mrcs412docs,"MRCS412");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-postwallachmigration-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-postwallachmigration-ERROR["+error.size()+"].xml",error);

		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	public static void getListOfPre412NPPDocumentsAllVersions()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List pre412doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcsnpp_document(all) where DATEFLOOR(day,r_creation_date) < DATE('04/01/2006') and FOLDER('/Neurological',descend)");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-pre412-["+pre412doclist.size()+"].xml",pre412doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}
	
	// this script will try to populate the migration history attr on the docs.
	public static void setMigrationHistoryOnPre412Docs() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		try {
			// load list of documents to convert
			List pre412docs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-pre412-[3154].xml");
			
			// set migration history attribute on all versions of all documents
			// - this is a config-specific operation, so we will code it in this class
			Map results = MigrationHistoryProcessor.setMigrationHistory(session,pre412docs,"pre-MRCS");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-pre412-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npp-SETMIGRATION-pre412-ERROR["+error.size()+"].xml",error);

		} finally {BaseClass.sMgr.release(session);}		
		
	}

	

}
