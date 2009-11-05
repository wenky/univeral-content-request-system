package com.medtronic.ecm.documentum.mdtworkflow.method.plugins.scheduledtask;

import java.util.List;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfFolder;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfList;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfList;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;


public class SMOTriggerFWPublish extends MdtProcessAttachments
{

    public void processAttachment(IDfSessionManager smgr, String docbase, String mdtapp, IDfSysObject jobobject, IDfSysObject formobj, IDfSysObject attachdoc, Map jobparameters, Map context)
    {
        IDfSession session = null;
        try {
            session = smgr.getSession(docbase);

            Map pluginconfig = (Map)context;

            // is FW? - get list of FW dirs from config
            /*-dbg-*/Lg.wrn("TOP");            
            List fwdirs = (List)pluginconfig.get("FactoryWorksDirectories");
            boolean isFW = false;
            /*-dbg-*/Lg.wrn("check if doc is in one of the registered factoryworks dirs");            
            for (int f=0; f < attachdoc.getFolderIdCount(); f++)
            {
                IDfId folderid = attachdoc.getFolderId(f);
                IDfFolder folder = (IDfFolder)session.getObject(folderid);
                String curpath = folder.getFolderPath(0);
                for (int fwf = 0; fwf < fwdirs.size(); fwf++) {
                    String folderpath = (String)fwdirs.get(fwf);
                    if (curpath.contains(folderpath)) {
                        isFW = true;
                        break;
                    }
                }
                if (isFW) break;
            }
            /*-dbg-*/Lg.wrn("isFW? %b",isFW);            

            if (isFW) {
                // get plugin params
                String registeredtablename = (String)pluginconfig.get("TableName");
                String drltemplate = (String)pluginconfig.get("DrlTemplate");
                String scscfgloc = (String)pluginconfig.get("SCSConfigLocation"); // i.e. /export/home1/Documentum/ecsadmin/mqadoc_dev/dba/config/mqadoc_dev
                String scsconfigdoc = (String)pluginconfig.get("SCSConfigDocName");
                /*-dbg-*/Lg.wrn(" - regtable: %s",registeredtablename);                
                /*-dbg-*/Lg.wrn(" - drl link: %s",drltemplate);                
                /*-dbg-*/Lg.wrn(" - cfg loc: %s",scscfgloc);                
                /*-dbg-*/Lg.wrn(" - cfg doc: %s",scsconfigdoc);                                
                
                //// UPDATE REG TABLE
                
                try { 
                    // if unsuccessful, manually update registered table DRL links
                    // see if row exists (check to see if we are doing an INSERT or an UPDATE to the registered table
                    String selectquery = "select dctm_object_id,dctm_document_nr,dctm_version_nr from dm_dbo." + registeredtablename + " where dctm_document_nr = '"+attachdoc.getObjectName()+"'"; 
                    /*-dbg-*/Lg.wrn("  -- query: %s",selectquery);
                    IDfQuery q = new DfQuery();
                    q.setDQL(selectquery);
                    boolean isupdate = false;
                    IDfCollection checkregistered = null;
                    try { 
                        /*-dbg-*/Lg.wrn("exec check query");
                        checkregistered = q.execute(session, IDfQuery.DF_READ_QUERY);
                        /*-dbg-*/Lg.wrn("get result...");
                        isupdate = checkregistered.next();
                        /*-dbg-*/Lg.wrn("  found in fastpath? %b",isupdate);
                        
                        /*-dbg-*/if (isupdate) try { 
                            /*-dbg-*/Lg.wrn("table obijd: "+checkregistered.getString("dctm_object_id"));
                            /*-dbg-*/Lg.wrn("table version: "+checkregistered.getString("dctm_version_nr"));                        
                        /*-dbg-*/} catch (Exception e) {}
                    } finally {try {checkregistered.close();}catch(Exception e){}}
    
                    // compose drl link
                    String drl = drltemplate.replace("$docid", formobj.getObjectId().getId());
    
                    if (isupdate) {
                        IDfCollection colUpdate = null;
                        /*-dbg-*/Lg.wrn("compose update query");
                        String updatequery = "UPDATE dm_dbo." + registeredtablename +
                            " SET dctm_version_nr='" +  attachdoc.getVersionLabels().getImplicitVersionLabel() + "'," +
                            " dctm_effectivity_date=DATE(NOW)," +
                            " dctm_url='"+drl+attachdoc.getObjectId().toString()+"'," +
                            " dctm_object_id='" + attachdoc.getObjectId().toString() + "'" + 
                            " where dctm_document_nr = '" + attachdoc.getObjectName() + "'";
                        /*-dbg-*/Lg.wrn("  --update query: %s",updatequery);
                        q.setDQL(updatequery);
                        try { 
                            /*-dbg-*/Lg.wrn("exec");
                            colUpdate = q.execute(session, IDfQuery.DF_QUERY);
                            colUpdate.next();
                            /*-dbg-*/if (Lg.wrn())Lg.wrn("rows updated: %d",colUpdate.getInt("rows_updated"));
                        } finally { colUpdate.close();}
                    } else {
                        IDfCollection colInsert = null;
                        /*-dbg-*/Lg.wrn("compose insert query");
                        String insertquery = "INSERT INTO dm_dbo." + registeredtablename +
                            " (dctm_document_nr,dctm_version_nr,dctm_url,dctm_effectivity_date,dctm_object_id) VALUES (" +
                            " '" +  attachdoc.getObjectName() + "'," +
                            " '" +  attachdoc.getVersionLabels().getImplicitVersionLabel() + "'," +
                            " '" + drl + attachdoc.getObjectId().toString() + "'," +
                            " DATE(NOW)," +
                            " '" + attachdoc.getObjectId().toString() + "'" + 
                            " )";
                        /*-dbg-*/Lg.wrn("  --insert query: %s",insertquery);
                        q.setDQL(insertquery);
                        try { 
                            /*-dbg-*/Lg.wrn("exec");
                            colInsert = q.execute(session, IDfQuery.DF_QUERY);
                            colInsert.next();
                            /*-dbg-*/try{if (Lg.wrn())Lg.wrn("rows inserted: %d",colInsert.getInt("rows_inserted"));}catch(Exception e){}
                        } finally { colInsert.close();}                                 
                    }
                } catch (Exception e) {
                    /*-WARN-*/Lg.wrn("error in registered table update for %s",attachdoc);                    
                }
                
                /// CALL SCS

                String cfglookupqual = "dm_webc_config where object_name = '"+scsconfigdoc+"'";
                /*-dbg-*/Lg.wrn(" - lookup scs cfg doc with qual: %s",cfglookupqual);                                
                IDfSysObject scsconfig = (IDfSysObject)session.getObjectByQualification("dm_webc_config where object_name = '"+scsconfigdoc+"'");
                
                /*-dbg-*/Lg.wrn("compose api call for scs cfg doc %s",scsconfig);
                
                IDfList args = new DfList(); 
                IDfList argsTypes = new DfList(); 
                IDfList argsValues = new DfList();
                
                String callmethodapiargs =  "-docbase_name "+session.getDocbaseName()+" " +
                                            "-config_location "+scscfgloc+" " +
                                            "-config_object_id "+scsconfig.getObjectId().getId()+" -method_trace_level 0 -full_refresh F -update_property_schema F " +
                                            "-source_object_id '"+attachdoc.getObjectId().getId()+"'";

                args.appendString("APP_SERVER_NAME"); 
                argsTypes.appendString("S");
                argsValues.append("WebCache");

                args.appendString("TIME_OUT"); 
                argsTypes.appendString("I");
                argsValues.append("120");

                args.appendString("SAVE_RESPONSE"); 
                argsTypes.appendString("I");
                argsValues.append("1");

                args.appendString("LAUNCH_SYNC"); 
                argsTypes.appendString("B");
                argsValues.append("T");

                /*-dbg-*/Lg.wrn("meth args: %s",callmethodapiargs);            
                args.appendString("ARGUMENTS"); 
                argsTypes.appendString("S");
                argsValues.append(callmethodapiargs);

                boolean scssuccess = true;
                /*-dbg-*/Lg.wrn("  -- exec SCS METHOD CALL");            
                
                try { 
                    // well, since apiExec always returns true, apparently exec failure is communicated via DfException
                    IDfCollection returnval = session.apply(null, "HTTP_POST", args, argsTypes, argsValues);
                    try { 
                        while(returnval.next()) {
                            /*-dbg-*/Lg.wrn("Result::" + returnval.getString("result"));
                            /*-dbg-*/Lg.wrn("ResultDocId::" + returnval.getString("result_doc_id"));
                            /*-dbg-*/Lg.wrn("ProcessId::" + returnval.getString("process_id"));
                            /*-dbg-*/Lg.wrn("LaunchedFailed::" + returnval.getString("launch_failed"));
                            /*-dbg-*/Lg.wrn("MethodReturnVal::" + returnval.getString("method_return_val"));
                            /*-dbg-*/Lg.wrn("OSSystemError::" + returnval.getString("os_system_error"));
                            /*-dbg-*/Lg.wrn("TimedOut::" + returnval.getString("timed_out"));
                            /*-dbg-*/Lg.wrn("TimeOutLength::" + returnval.getString("time_out_length"));
                        }
                    } catch (DfException returncode) {
                        /*-dbg-*/Lg.wrn("WARN: error in examining method call return values");            
                    } finally {
                        try { returnval.close();}catch (Exception e) {}
                    }
                } catch (DfException returncode) {
                    /*-dbg-*/Lg.wrn("API EXEC FAILED");            
                    scssuccess = false;
                }
            }
    
        } catch (DfException dfe) {
            /*-ERROR-*/Lg.err("exception in SCS publish api call",dfe);
            throw EEx.create("SCSPublish-err", "exception in SCS publish api call",dfe);
        } finally {
            try {smgr.release(session);}catch(Exception e){}
        }
        
    }

}
