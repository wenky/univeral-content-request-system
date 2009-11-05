package com.medtronic.ecm.documentum.util;

import java.io.BufferedWriter;
import java.io.StringWriter;

import com.csvreader.CsvWriter;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfValue;

public class CsvUtils {

    public static String execQueryCSV(IDfSession session, String dql) throws Exception
    {
        StringWriter csv = new StringWriter();
        CsvWriter csvwrt = new CsvWriter(new BufferedWriter(csv), ',');
        boolean headerwritten = false;
        IDfCollection c = null;
        try {
            IDfQuery query = new DfQuery();
            query.setDQL(dql);
            c = query.execute(session, query.DF_READ_QUERY);
            while (c.next()) {
                if (!headerwritten) {
                    String[] headers = new String[c.getAttrCount()];
                    for (int h=0; h < c.getAttrCount(); h++)
                    {
                        headers[h] = c.getAttr(h).getName();
                    }
                    csvwrt.writeRecord(headers);
                    headerwritten = true;
                }
                String[] row = new String[c.getAttrCount()];
                for (int i=0; i < c.getAttrCount(); i++)
                {
                    IDfAttr attr = c.getAttr(i);
                    IDfValue value = c.getValueAt(i);
                    switch (attr.getDataType()) { 
                        case IDfAttr.DM_TIME :
                            row[i] = value.asTime().getDate().toGMTString();
                            break;
                        case IDfAttr.DM_INTEGER :
                            row[i] = ""+value.asInteger();
                            break;
                        case IDfAttr.DM_DOUBLE:
                            row[i] = ""+value.asDouble();
                            break;
                        default : 
                            row[i] = value.asString();
                            break;
                    }
                }
                csvwrt.writeRecord(row);
            }
            csvwrt.flush();
            return csv.toString();
        } finally {
            try {c.close();}catch(Exception e){}
        }
        
    }

    
}
