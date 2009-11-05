package com.zoder.processors;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import lbase.Lg;

import com.documentum.fc.common.DfException;
import com.zoder.access.DctmAccess;
import com.zoder.main.IProcessor;
import com.zoder.struct.DocumentError;
import com.zoder.struct.ErrorDetail;
import com.zoder.util.CollUtils;
import com.zoder.util.Context;

public abstract class AbstractDocumentProcessor implements IProcessor {

    public void process(Map script, Context context) throws Exception 
    {
        /*-log-*/String zzzcn = getClass().getSimpleName();
        /*-dbg-*/Lg.dbg("%s top of ADP",zzzcn);
        String accesskey = (String)context.get("AccessKey");
        if (accesskey == null)
            accesskey = "SourceAccess";
        DctmAccess access = (DctmAccess)context.get(accesskey);        
        List success = CollUtils.getList(context,"Success");
        List newsuccess = new ArrayList();
        
        /*-dbg-*/Lg.dbg("%s - iterating on items",zzzcn);int err_successcount=0;int err_failcount=0;
        for (int i=0; i < success.size(); i++) 
        {
            /*-trc-*/Lg.trc("item %d",i);long t=Lg.curtime();
            Map document = (Map)success.get(i);
            if (document == null) {
                /*-WARN-*/Lg.wrn("%s - item %d is null in success list",zzzcn,i);
            } else {
                try { 
                    /*-trc-*/Lg.trc("exec concrete item processing method");
                    processItem(access,script,context,document,i);                        
                    /*-dbg-*/Lg.dbg("%s - item success %s",zzzcn,CollUtils.getDocChronid(document));err_successcount++;
                    newsuccess.add(document);
                } catch (DocumentError docerr) {
                    /*-INFO-*/Lg.inf("%s - item processing explicitly detected DocumentError %s",zzzcn,CollUtils.getDocChronid(document));
                    err_failcount++;
                    moveToErrorList(context,document);
                    /*-trc-*/Lg.trc("moved to error list");                    
                } catch (DfException dfe) { 
                    /*-ERROR-*/Lg.err("%s - item processing failed due to DCTM error %s",zzzcn,CollUtils.getDocChronid(document),dfe);
                    DocumentError docerr = new DocumentError(zzzcn +" - " + " unhandled DCTM document processing error: [chronid:"+CollUtils.getDocChronid(document)+ "] err: " + dfe.getMessageId() + "-" + dfe.getMessage(),dfe,document);
                    err_failcount++;
                    moveToErrorList(context,document);
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("%s - item processing failed due to thrown exception %s",zzzcn,CollUtils.getDocChronid(document),e);
                    err_failcount++;
                    DocumentError docerr = new DocumentError(zzzcn +" - " + " unhandled document processing exception: [chronid:"+CollUtils.getDocChronid(document)+ "] err: " + e.getMessage(),e,document);
                    moveToErrorList(context,document);
                }
            }
            /*-trc-*/Lg.trc("document iteration done %d time: %d",i,Lg.curtime()-t);            
        }
        
        context.put("Success", newsuccess);
        
        /*-dbg-*/Lg.dbg("%s - document processing done - success: %d fail: %d",zzzcn,err_successcount, err_failcount);        
    }
    
    public abstract void processItem(DctmAccess access, Map script, Context context, Map document, int idx) throws Exception;
    
    public static void moveToErrorList(Context c, Map item)
    {
        /*-trc-*/Lg.trc("Move to error list");
        List failure = CollUtils.getList(c, "Failure");        
        List errors = CollUtils.getList(c, "ErrorList");        
        /*-trc-*/Lg.trc("adding to failure");
        failure.add(item);        
        /*-trc-*/Lg.trc("compose error detail");
        ErrorDetail err = new ErrorDetail();
        DocumentError docerr = (DocumentError)item.get("Error.Backreference");
        err.message = docerr.getMessage();
        err.t = docerr.getCause();
        err.reference = item;
        /*-trc-*/Lg.trc("adding error detail");
        errors.add(err);
        /*-trc-*/Lg.trc("done with error list move");
    }

}
