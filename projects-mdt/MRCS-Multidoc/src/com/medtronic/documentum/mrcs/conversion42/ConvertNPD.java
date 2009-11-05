package com.medtronic.documentum.mrcs.conversion42;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.documentum.fc.client.IDfSession;

public class ConvertNPD {

	public static void main(String[] args) throws Exception
	{
		convertPdfFormatDocumentsToAcro();
	}

	public static void getPdfDocumentsList() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
		
		// get list of documents to convert
		// - use FOLDER to eliminate docs from other cabinets, Templates cabinet, staging, etc.
		//   - beware of 25,000 limit on this for large documents sets like EFS and neuro
		// - 
		List doclist = DqlQueryIdentifier.executeQuery(session,"select r_object_id from m_mrcs_document where a_content_type = 'pdf' and r_policy_id in (select r_object_id from dm_policy(all) where object_name in ('mrcs_npd_nsp_lc','mrcs_npd_nsp_lc','mrcs_npd_dhf_lc')) and FOLDER('/Neurological/Neuro Design History Files',descend)");			
		BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npd-PDF2ACRO-["+doclist.size()+"].xml",doclist);
		
		BaseClass.sMgr.release(session);		
	}
	
	public static void convertPdfFormatDocumentsToAcro() throws Exception
	{
		// begin migration control script - instantiate
		IDfSession session = BaseClass.getSession();
				
		Map renderedstates = new HashMap(); renderedstates.put("Approved","");
		Map unrenderedstates = new HashMap(); unrenderedstates.put("In-Progress","");
		
		try {
			
			// load list of documents to convert
			List fulldoclist = BaseClass.readDocumentList(BaseClass.conversiondirectory+"test-npd-PDF2ACRO-[33].xml");
			
			// split the doclist
			Map splits = SplitterProcessor.splitList(3000,fulldoclist);

			int i=1;
			while (true) {
				List doclist = (List)splits.get("output"+i);
				if (doclist == null) break;

				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npd-PDF2ACRO--"+i+"--AAA-INPUT["+doclist.size()+"].xml",doclist);

				// set migration history attribute on all versions of all documents
				// - this is a config-specific operation, so we will code it in this class
				Map results = PdfToAcroProcessor.convertPdfToAcro(session,renderedstates,unrenderedstates,doclist);
			
				List success = (List)results.get("success");
				List error = (List)results.get("error");
			
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npd-PDF2ACRO--"+i+"--SUCCESS["+success.size()+"].xml",success);
				BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npd-PDF2ACRO--"+i+"--ERROR["+error.size()+"].xml",error);
				
				i++;
			}

		} catch (Exception e) {
			List errlist = new ArrayList();
			errlist.add(e);
			BaseClass.writeDocumentList(BaseClass.conversiondirectory+"test-npd-PDF2ACRO-__FATALERROR__.xml",errlist);
		}
		finally {BaseClass.sMgr.release(session);}		
		
	}
	
	
}
