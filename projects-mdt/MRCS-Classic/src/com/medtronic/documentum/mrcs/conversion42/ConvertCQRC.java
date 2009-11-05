package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;

public class ConvertCQRC 
{
	public static void main(String[] args) throws Exception
	{
		// -- AUDIT PHASE --
		// put mradmin in all cqrc groups...
		// Check document types for overlap...
		// - m_mrcs_cqrc_document
		// - m_mrcs_cqrc_ref_document (CQRC Taining and Reference)
		// - m_mrcs_cqrc_ref_document (CQRC Other)
		// -==no overlapping inheritance like CRMSDO==-
		// - required ops: Ret+Obs, LC reattach, that's it...
		//getPolicyReattachmentDoclist();
		//processPolicyReattachment();
		
		// RET/OBS:
		//   mrcs_cqrc_other_doc LC: Obsolete = Obsolete-Other, Retired = Retired-Other
		//   mrcs_cqrc_document LC: Obsolete, Retired
		//getListOfRetObsCQRCDocs();
		//processRetObsCQRC();
		
		// ---------------- training run from CQRC -----------------
		//getPolicyReattachmentDoclist();
		//processPolicyReattachment();
		//getListOfRetObsCQRCDocs();
		//processRetObsCQRC();

		// ---------------- PROD run for CQRC -----------------
		//getPolicyReattachmentDoclist();
		//getListOfRetObsCQRCDocs();
		//processPolicyReattachment();
		//processRetObsCQRC();

	}
	
	public static void getPolicyReattachmentDoclist()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			List pre412doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document where FOLDER('/Corporate',descend) and r_policy_id != '0000000000000000'");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-REATTACHPOLICIES-["+pre412doclist.size()+"].xml",pre412doclist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	
	public static void processPolicyReattachment() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		try {
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-cqrc-REATTACHPOLICIES-[182].xml");
			
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
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-REATTACHPOLICIES--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-REATTACHPOLICIES--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-REATTACHPOLICIES-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}
	

	public static void getListOfRetObsCQRCDocs()
	{
		IDfSession session = BaseClass.getSession();	
		try {
			
			List nppdocs = DqlQueryIdentifier.executeQuery (session,"SELECT r_object_id FROM m_mrcs_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_cqrc_document') and FOLDER('/Corporate/CQRC',descend)");			
			List nppforms = DqlQueryIdentifier.executeQuery(session,"SELECT r_object_id FROM m_mrcs_document WHERE r_policy_id in (SELECT r_object_id FROM dm_policy(all) where object_name = 'mrcs_cqrc_other_doc') and FOLDER('/Corporate/CQRC',descend)");			
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-cqrcdoc["+nppdocs.size()+"].xml",nppdocs);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-cqrcother["+nppforms.size()+"].xml",nppforms);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	public static void processRetObsCQRC() throws Exception
	{
		IDfSession session = BaseClass.getSession();	
		try {
			
			List nppdocs = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-cqrcdoc[132].xml");
			List nppforms = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-cqrcother[50].xml");

			Map results; List success, error;
			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Obsolete","Retired",nppdocs);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-docs-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-docs-ERROR["+error.size()+"].xml",error);

			results = ObsoleteRetiredProcessor.processObsoleteAndRetired(session,"Obsolete-Other","Retired-Other",nppforms);
			success = (List)results.get("success");
			error = (List)results.get("error");
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-others-SUCCESS["+success.size()+"].xml",success);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-others-ERROR["+error.size()+"].xml",error);

			
		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-cqrc-RETOBS-__FATALERROR__.xml",errlist);
		} finally {BaseClass.sMgr.release(session);}		
		
	}

	
}
