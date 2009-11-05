package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;

public class ConvertApprovedCopies 
{
	// general extract/setup for EFS configs
	//
	// -- EFS Central -- mrcs_crm_efs_cf
	// plugin: CopyRenditionToSystemFolder
	// copy at state: Approved
	// CopyToLocation: Approved
	// COpyDocACL: mrcs_central_approved
	// CopyDocLabel: Approved
	// Relationtype: CopyToRelationship
	// RelationDesc: Approved Document
	// RenditionFormat: pdf
	// attrs: title,authors,keywords,m_mrcs_central_author_date,m_mrcs_central_dhf,
	//        m_mrcs_central_doc_type,m_mrcs_central_func_group,m_mrcs_central_phase,m_mrcs_central_ref_number
	//
	// -- EFS EP Systems -- mrcs_crm_efs_epsys
	// plugin: CopyRenditionToSystemFolder
	// copy at state: Approved
	// CopyToLocation: Approved
	// COpyDocACL: mrcs_epsys_approved
	// CopyDocLabel: Approved
	// Relationtype: CopyToRelationship
	// RelationDesc: Approved Document
	// RenditionFormat: pdf
	// attrs: title,authors,keywords,m_mrcs_epsys_device_type,m_mrcs_epsys_model_number,m_mrcs_epsys_doc_type,
	//        m_mrcs_epsys_phase,m_mrcs_epsys_product_name,m_mrcs_epsys_project_name
	//
	// -- EFS MQA -- mrcs_crm_efs_mqa
	// plugin: CopyRenditionToSystemFolder
	// copy at state: Approved
	// CopyToLocation: Approved
	// COpyDocACL: mrcs_mqa_approved
	// CopyDocLabel: Approved
	// Relationtype: CopyToRelationship
	// RelationDesc: Approved Document
	// RenditionFormat: pdf
	// attrs: title,authors,keywords,m_mrcs_mqa_part_number,m_mrcs_mqa_author_date,m_mrcs_mqa_document_type,
	//        m_mrcs_mqa_supplier

	public static void main(String[] args) throws Exception
	{
		// get list of efs approved docs to backup 
		// -- /CRM/TDS EFS/Central File/Project Files/CACOPA System/Approved
		// pare list to a subset/folder we can test on.
		// backup the docs from the pared list
		// get list of source docs that correspond to that approved folder
		// reconstruct the approved copies
		// compare with backed up copies.
		
		// begin test: 
		// getApprovedCopiesList (getting list of approved docs in CACOPA System/Approved --> success
		// backupCopies() --> moving to EFS_Staging/TDSEFS_MRCS421/ApprovedBackupTest --> success
		// getSourceDocsList
		reconstructCopies();


		
	}

	public static void reconstructCopies() throws Exception
	{
		IDfSession session = BaseClass.getSession();
		try { 
			List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-sourcedocs-efs-[25].xml");
			
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
			
			Map results = RebuildApprovedCopiesProcessor.rebuildApprovedCopies(session,contracts,approvalstates,"m_mrcs_central_document",copyattrs,"pdf","Approved","Approved Document","CopyToRelationship","mrcs_central_approved");
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-reconstruct-efs-success-["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-reconstruct-efs-error-["+error.size()+"].xml",error);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	// sample...
	public static void backupCopies() throws Exception
	{
		// begin migration control script - instantiate		
		IDfSession session = BaseClass.getSession();
		try { 
			List contracts = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-backupcopies-efs-[27].xml");
			Map results = RebuildApprovedCopiesProcessor.backupExistingApprovedCopies(session,"/EFS_Staging/TDSEFS_MRCS421/ApprovedBackupTest","mrcs_central_approved",contracts);
			
			List success = (List)results.get("success");
			List error = (List)results.get("error");
			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-backupcopies-efs-success-["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-backupcopies-efs-error-["+error.size()+"].xml",error);
		} finally {BaseClass.sMgr.release(session);}		
	}
	public static void getApprovedCopiesList() throws Exception
	{
		IDfSession session = BaseClass.getSession();
		try { 
			// get list of documents to convert
			List documents = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_central_document WHERE FOLDER('/CRM/TDS EFS/Central File/Project Files/CACOPA System/Approved',DESCEND)");
			documents = DocumentStateIdentifier.populateDocumentState(session,documents); 
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-backupcopies-efs-["+documents.size()+"].xml",documents);
		} finally {BaseClass.sMgr.release(session);}		
	}
	
	// assumes the approved copies have been backedup/moved already so we don't clone approved copies...
	public static void getSourceDocsList() throws Exception
	{
		IDfSession session = BaseClass.getSession();
		try { 
			// get list of documents to convert
			List documents = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_central_document WHERE FOLDER('/CRM/TDS EFS/Central File/Project Files/CACOPA System',DESCEND)");
			documents = DocumentStateIdentifier.populateDocumentState(session,documents); 
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-sourcedocs-efs-["+documents.size()+"].xml",documents);
		} finally {BaseClass.sMgr.release(session);}		
	}


	

}
