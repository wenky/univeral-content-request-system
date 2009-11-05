package com.medtronic.documentum.mrcs.conversion42;


public class ConvertRootFolders {

/*	
	
// Change object types: EFS  
  
CHANGE m_mrcs_subfolder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Central File' AND FOLDER('/CRDM/TDS EFS',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Central File' AND FOLDER('/CRDM/TDS EFS',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Project Files' AND FOLDER('/CRDM/TDS EFS/Central File',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Project Files' AND FOLDER('/CRDM/TDS EFS/Central File',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Literature' AND FOLDER('/CRDM/TDS EFS/Central File',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Literature' AND FOLDER('/CRDM/TDS EFS/Central File',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Product Assurance Test' AND FOLDER('/CRDM/TDS EFS/Central File',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Product Assurance Test' AND FOLDER('/CRDM/TDS EFS/Central File',descend);
	
CHANGE m_mrcs_subfolder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'MQA' AND FOLDER('/CRDM/TDS EFS',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'MQA' AND FOLDER('/CRDM/TDS EFS',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Part File' AND FOLDER('/CRDM/TDS EFS/MQA',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Part File' AND FOLDER('/CRDM/TDS EFS/MQA',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Supplier Audit' AND FOLDER('/CRDM/TDS EFS/MQA',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Supplier Audit' AND FOLDER('/CRDM/TDS EFS/MQA',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Supplier File' AND FOLDER('/CRDM/TDS EFS/MQA',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseGroupingFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Supplier File' AND FOLDER('/CRDM/TDS EFS/MQA',descend);

CHANGE m_mrcs_subfolder OBJECT TO m_mrcs_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Corrective Action' AND FOLDER('/CRDM/TDS EFS',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'Corrective Action' AND FOLDER('/CRDM/TDS EFS',descend);

// these two are for subfolder->groupingfolder fixes on MRCS_Training
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_mqa' WHERE object_name = 'MQA' AND FOLDER('/CRDM/TDS EFS',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'BaseRootFolder', SET mrcs_application = 'mrcs_crm_efs_cf' WHERE object_name = 'Central File' AND FOLDER('/CRDM/TDS EFS',descend);


// Change Object types: NPP

CHANGE dm_folder OBJECT TO m_mrcs_folder WHERE object_name = 'BSP - Business Systems Procedures' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'BSP - Business Systems Procedures' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder WHERE object_name = 'PDS Device' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'PDS Device' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'PDS Pharma' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder WHERE object_name = 'PDS Pharma' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Customer Interface' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder WHERE object_name = 'Customer Interface' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Infrastructure' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Infrastructure' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Device Development' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Device Development' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Device Order Fulfillment' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Device Order Fulfillment' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Pharma Development' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Pharma Development' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Pharma Order Fulfillment' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'NeuroBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_neuro' WHERE object_name = 'Pharma Order Fulfillment' AND FOLDER('/Neurological',descend);

CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Continuous Improvement' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Continuous Improvement' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Product and Market Support' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Product and Market Support' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Product Creation' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Product Creation' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Product Distribution' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Product Distribution' AND FOLDER('/Neurological',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Quality System Manual' AND FOLDER('/Neurological',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'MGUBaseDocumentFolder', SET mrcs_application = 'mrcs_npp_mgu' WHERE object_name = 'Quality System Manual' AND FOLDER('/Neurological',descend);
	
// Change Objet types: CRMSDO // need DELETE?

CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'crmsdo_commodity_root', SET mrcs_application = 'mrcs_crm_sdo' WHERE object_name = 'Commodity Information' AND FOLDER('/CRDM/CRDM Supply Chain',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder WHERE r_object_id = '0b025b4180002fc7';
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'crmsdo_commodity_root', SET mrcs_application = 'mrcs_crm_sdo' WHERE object_name = 'Commodity Information' AND FOLDER('/CRDM/CRDM Supply Chain',descend);
CHANGE dm_folder OBJECT TO m_mrcs_folder SET mrcs_config = 'crmsdo_other_root', SET mrcs_application = 'mrcs_crm_sdo' WHERE object_name = 'Other Documents' AND FOLDER('/CRDM/CRDM Supply Chain',descend);
CHANGE m_mrcs_folder OBJECT TO m_mrcs_grouping_folder SET mrcs_config = 'crmsdo_other_root', SET mrcs_application = 'mrcs_crm_sdo' WHERE object_name = 'Other Documents' AND FOLDER('/CRDM/CRDM Supply Chain',descend);


*/

}
