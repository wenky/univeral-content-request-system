package com.medtronic.ecm.documentum.mdtworkflow.method.common;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;

import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.IDfTime;
import com.lowagie.text.Rectangle;
import com.lowagie.text.pdf.AcroFields;
import com.lowagie.text.pdf.PdfContentByte;
import com.lowagie.text.pdf.PdfImportedPage;
import com.lowagie.text.pdf.PdfReader;
import com.lowagie.text.pdf.PdfStamper;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;

public class PdfOverlayProcessing 
{
    
    public static void doSimpleOverlay(IDfSession sess, IDfSysObject doc, String templatepath, String processedformat)
    {
        try {
            /*-WARN-*/Lg.wrn("Top of overlay plugin - get current thread");
            Thread curthread = Thread.currentThread();
            long threadid = curthread.getId();
            /*-WARN-*/Lg.wrn("threadid: %d",threadid);
            long timeval = (new Date()).getTime();
            /*-WARN-*/Lg.wrn("timval: %d",timeval);
            String outputmergedfilename = "processed"+threadid+'-'+timeval+".pdf";
            int jj=0;
            while (true) {
                /*-WARN-*/Lg.wrn("check existence of merged final file %s",outputmergedfilename);
                File f = new File(outputmergedfilename);
                if (f.exists()) {                    
                    /*-WARN-*/Lg.wrn("dupe of %s detected, incrementing to unique file",outputmergedfilename);
                    timeval = (new Date()).getTime();
                    outputmergedfilename = "processed"+threadid+'-'+timeval+'_'+jj+".pdf";
                    jj++;
                } else {
                    break;
                }
            }            
            /*-WARN-*/Lg.wrn("threadsafe merged final file: %s",outputmergedfilename);
            
            timeval = (new Date()).getTime();
            String templatesubsitutionfilename = "template"+threadid+'-'+timeval+".pdf";
            int kk=0;
            while (true) {
                /*-WARN-*/Lg.wrn("check existence of template substitution file %s",templatesubsitutionfilename);
                File f = new File(templatesubsitutionfilename);
                if (f.exists()) {                    
                    /*-WARN-*/Lg.wrn("dupe of %s detected, incrementing to unique file",templatesubsitutionfilename);
                    timeval = (new Date()).getTime();
                    templatesubsitutionfilename = "template"+threadid+'-'+timeval+'_'+kk+".pdf";
                    kk++;
                } else {
                    break;
                }
            }
            /*-WARN-*/Lg.wrn("threadsafe template substitution file: %s",templatesubsitutionfilename);
            
            /*-WARN-*/Lg.wrn("lookup template doc");
            IDfDocument templatedoc = (IDfDocument)sess.getObjectByPath(templatepath);
                        
            /*-WARN-*/Lg.wrn("create pdfreader on docobject %s",templatedoc);
            PdfReader template = new PdfReader(templatedoc.getContent()); 
            PdfStamper stamper = new PdfStamper(template, new FileOutputStream(templatesubsitutionfilename));
            AcroFields form = stamper.getAcroFields();
            HashMap fields = form.getFields();
            String key;
            /*-WARN-*/Lg.wrn("iterate on form fields, attempt to match with attribute names on document");
            for (Iterator i = fields.keySet().iterator(); i.hasNext(); ) {
                key = (String) i.next();
                /*-WARN-*/Lg.wrn("processing next form field: check if key %s is an attr",key);
                if (doc.hasAttr(key)) {
                    /*-WARN-*/Lg.wrn("attr match for form field %s found",key);
                    if (doc.isAttrRepeating(key)){
                        form.setField(key, doc.getAllRepeatingStrings(key, ","));
                    } else if (doc.getAttrDataType(key) == 4) {
                        IDfTime date = doc.getTime(key);
                        form.setField(key, date.asString(IDfTime.DF_TIME_PATTERN4));
                    } else {
                        form.setField(key, doc.getString(key));
                    }
                }
            }
            stamper.setFormFlattening(true);
            stamper.close();
            
            // the document we're watermarking
            /*-WARN-*/Lg.wrn("open reader source on content to overlay");
            PdfReader document= new PdfReader(doc.getContentEx("pdf",0));
            int num_pages= document.getNumberOfPages();

            // the watermark (or letterhead, etc.)
            /*-WARN-*/Lg.wrn("open reader on prepped overly pdf");
            PdfReader mark= new PdfReader(templatesubsitutionfilename);
            Rectangle mark_page_size= mark.getPageSize( 1 );

            // the output document
            /*-WARN-*/Lg.wrn("open writer on final overlay output pdf");
            PdfStamper writer = new PdfStamper( document, new FileOutputStream(outputmergedfilename) );

            // create a PdfTemplate from the first page of mark
            // (PdfImportedPage is derived from PdfTemplate)
            PdfImportedPage mark_page= writer.getImportedPage( mark, 1 );

            /*-WARN-*/Lg.wrn("iterate on pages");
            for( int ii= 0; ii< num_pages; ) {
              // iterate over document's pages, adding mark_page
              ++ii;
              /*-WARN-*/Lg.wrn("begin overlay onto page %d",ii);
              PdfContentByte contentByte= writer.getOverContent( ii );
              contentByte.addTemplate( mark_page, 1, 0, 0, 1, 0, 0 );
            }
            writer.close();
            
            // Import as alternate format
            /*-WARN-*/Lg.wrn("prepare to import the overlay output as a rendition as format %s",processedformat);            
            doc.addRendition(outputmergedfilename, processedformat);
            doc.save();
            
            // temp file cleanup
            /*-WARN-*/Lg.wrn("temp file cleanup");
            File outputfile = new File(outputmergedfilename);
            outputfile.delete();
            File templatefile = new File(templatesubsitutionfilename);
            templatefile.delete();
            /*-WARN-*/Lg.wrn("done");
            
        }
        catch (Exception e){
            /*-ERROR-*/Lg.err("Error occurred in pdf overlay processing",e);
            throw EEx.create("OverlayError","Error occurred in pdf overlay processing",e);
        }
        
    }    

}
