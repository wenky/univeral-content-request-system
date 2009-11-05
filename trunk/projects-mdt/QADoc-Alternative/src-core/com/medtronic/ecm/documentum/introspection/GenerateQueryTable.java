package com.medtronic.ecm.documentum.introspection;

import java.io.BufferedWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Map;

import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.common.IDfAttr;
import com.documentum.fc.common.IDfTime;

public class GenerateQueryTable 
{
    public static String call(DctmAccess access, String baseurl, String credurl, String query) throws Exception
    {
        StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));
        String baselink = "<a href=\""+baseurl+"?do=doDumpObjectById.gvw"+credurl;             
        htmlbuffer.write("<table border=1>");
        IDfSession session = access.accessSession();
        try { 
        
            IDfQuery qry = new DfQuery();
            qry.setDQL(query);
            IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
            {
                String rowhtml = "<tr><td>#</td>";
                for (int i=0; i < myObj1.getAttrCount(); i++)
                {
                    String title = myObj1.getAttr(i).getName();
                    if (title == null) title = "&nbsp;";
                    else if ("".equals(title)) title = "&nbsp;";
                    rowhtml += "<td>"+title+"</td>";
                }
                rowhtml += "</tr>";
                htmlbuffer.write(rowhtml);
            }
            int count = 0;
            try { 
            while (myObj1.next()) {
                count++;
                String rowhtml = "<tr><td>"+count+"</td>";
                for (int i=0; i < myObj1.getAttrCount(); i++)
                {
                    if (myObj1.getAttr(i).isRepeating()) {
                        String output = "";
                        boolean first = true;
                        for (int j=0; j < myObj1.getValueCount(myObj1.getAttr(i).getName()); j++)
                        {
                            if (first) first = false; else output += ", ";
                            myObj1.getRepeatingValue(myObj1.getAttr(i).getName(),j);
                            switch (myObj1.getAttr(i).getDataType()) {
                                case IDfAttr.DM_BOOLEAN : output += myObj1.getRepeatingBoolean(myObj1.getAttr(i).getName(),j); break;  
                                case IDfAttr.DM_DOUBLE : output += myObj1.getRepeatingDouble(myObj1.getAttr(i).getName(),j); break;  
                                case IDfAttr.DM_ID : output += baselink+"&id="+myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId()+"\">"+myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId() + "</a>"; break;  
                                case IDfAttr.DM_INTEGER: output += myObj1.getRepeatingInt(myObj1.getAttr(i).getName(),j); break;  
                                case IDfAttr.DM_TIME: output += myObj1.getRepeatingTime(myObj1.getAttr(i).getName(),j).asString(IDfTime.DF_TIME_PATTERN18); break;
                                case IDfAttr.DM_STRING:
                                    // see if it's an ID anyway (ID type detection doesn't seem to work...)
                                    String value = myObj1.getRepeatingString(myObj1.getAttr(i).getName(),j);
                                    boolean isId = false;
                                    if (value.length() == 16) {
                                        try {
                                            Long.parseLong(value,16);
                                            isId = true;
                                        } catch (NumberFormatException nfe) { isId = false; }
                                    }
                                    if ("0000000000000000".equals(value)) isId = false;
                                    if (isId)
                                        output += baselink+"&id="+value+"\">"+value+"</a>";  
                                    else
                                        output += value; 
                                    break;
                                default: rowhtml+= "--";
                            }
                        }
                        if (output == null) output = "&nbsp;";
                        else if ("".equals(output)) output = "&nbsp;";
                        rowhtml += "<td>"+output+"</td>";                       
                    } else {
                        switch (myObj1.getAttr(i).getDataType()) {
                            case IDfAttr.DM_BOOLEAN : rowhtml += "<td>"+myObj1.getBoolean(myObj1.getAttr(i).getName())+"</td>"; break;  
                            case IDfAttr.DM_DOUBLE : rowhtml += "<td>"+myObj1.getDouble(myObj1.getAttr(i).getName())+"</td>"; break;  
                            case IDfAttr.DM_ID : rowhtml += "<td>"+baselink+"&id="+myObj1.getId(myObj1.getAttr(i).getName()).getId()+"\">"+myObj1.getId(myObj1.getAttr(i).getName()).getId()+"</td>"; break;  
                            case IDfAttr.DM_INTEGER: rowhtml += "<td>"+myObj1.getInt(myObj1.getAttr(i).getName())+"</td>"; break;  
                            case IDfAttr.DM_TIME: rowhtml += "<td>"+myObj1.getTime(myObj1.getAttr(i).getName()).asString(IDfTime.DF_TIME_PATTERN18)+"</td>"; break;
                            case IDfAttr.DM_STRING:
                                // see if it's an ID anyway (ID type detection doesn't seem to work...)
                                String value = myObj1.getString(myObj1.getAttr(i).getName());
                                boolean isId = false;
                                if (value.length() == 16) {
                                    try {
                                        Long.parseLong(value,16);
                                        isId = true;
                                    } catch (NumberFormatException nfe) { isId = false; }
                                }
                                if ("0000000000000000".equals(value)) isId = false;
                                if (isId)
                                    rowhtml += "<td>"+baselink+"&id="+value+"\">"+value+"</td>";  
                                else {
                                    if (value == null) value = "&nbsp;";
                                    else if ("".equals(value)) value = "&nbsp;";
                                    rowhtml += "<td>"+value+"</td>";
                                }
                                break;
                            default: rowhtml+= "--";
                        }
                    }
                }
                rowhtml += "</tr>";
                htmlbuffer.write(rowhtml);
            }
            } catch (Exception e) {
                htmlbuffer.write("</table><BR><BR><BR>ERROR during query execution: "+e+"<BR>record #: "+count+"<BR>Stacktrace:<BR><table>");
                e.printStackTrace(htmlbuffer);
            } catch (Throwable t) {
                int a=1;
                a++;
            } finally { 
                myObj1.close();
            }
        } catch (Exception e) {
            htmlbuffer.write("</table>Error or Bad Query<BR>"+e.getMessage()+"<br>Query: "+query);
        }
        htmlbuffer.write("</table>");
        htmlbuffer.close();
        
        return sw.toString();
        
    }

    public static String callWithTranslation(DctmAccess access, String baseurl, String credurl, String query, Map translations) throws Exception
    {
        StringWriter sw = new StringWriter();
        PrintWriter htmlbuffer = new PrintWriter(new BufferedWriter(sw));
        String baselink = "<a href=\""+baseurl+"?do=doDumpObjectById.gvw"+credurl;             
        htmlbuffer.write("<table border=1>");
        IDfSession session = access.accessSession();
        try { 
        
            IDfQuery qry = new DfQuery();
            qry.setDQL(query);
            IDfCollection myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
            {
                String rowhtml = "<tr><td>#</td>";
                for (int i=0; i < myObj1.getAttrCount(); i++)
                {
                    String title = myObj1.getAttr(i).getName();
                    if (title == null) title = "&nbsp;";
                    else if ("".equals(title)) title = "&nbsp;";
                    rowhtml += "<td>"+title+"</td>";
                }
                rowhtml += "</tr>";
                htmlbuffer.write(rowhtml);
            }
            int count = 0;
            try { 
            while (myObj1.next()) {
                count++;
                String rowhtml = "<tr><td>"+count+"</td>";
                for (int i=0; i < myObj1.getAttrCount(); i++)
                {
                    if (myObj1.getAttr(i).isRepeating()) {
                        String output = "";
                        boolean first = true;
                        for (int j=0; j < myObj1.getValueCount(myObj1.getAttr(i).getName()); j++)
                        {
                            if (first) first = false; else output += ", ";
                            myObj1.getRepeatingValue(myObj1.getAttr(i).getName(),j);
                            switch (myObj1.getAttr(i).getDataType()) {
                                case IDfAttr.DM_BOOLEAN : output += myObj1.getRepeatingBoolean(myObj1.getAttr(i).getName(),j); break;  
                                case IDfAttr.DM_DOUBLE : output += myObj1.getRepeatingDouble(myObj1.getAttr(i).getName(),j); break;  
                                case IDfAttr.DM_ID : output += baselink+"&id="+myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId()+"\">"+myObj1.getRepeatingId(myObj1.getAttr(i).getName(),j).getId() + "</a>"; break;  
                                case IDfAttr.DM_INTEGER: 
                                    if (translations != null && translations.containsKey(myObj1.getAttr(i).getName())) {
                                        Map attrtranslate = (Map)translations.get(myObj1.getAttr(i).getName());
                                        output += attrtranslate.get(myObj1.getRepeatingInt(myObj1.getAttr(i).getName(),j));  
                                    } else { output += myObj1.getRepeatingInt(myObj1.getAttr(i).getName(),j); } 
                                    break;  
                                case IDfAttr.DM_TIME: output += myObj1.getRepeatingTime(myObj1.getAttr(i).getName(),j).asString(IDfTime.DF_TIME_PATTERN18); break;
                                case IDfAttr.DM_STRING:
                                    // see if it's an ID anyway (ID type detection doesn't seem to work...)
                                    String value = myObj1.getRepeatingString(myObj1.getAttr(i).getName(),j);
                                    if (translations != null && translations.containsKey(myObj1.getAttr(i).getName())) {
                                        Map attrtranslate = (Map)translations.get(myObj1.getAttr(i).getName());
                                        value = (String)attrtranslate.get(value);  
                                    }  
                                    
                                    boolean isId = false;
                                    if (value.length() == 16) {
                                        try {
                                            Long.parseLong(value,16);
                                            isId = true;
                                        } catch (NumberFormatException nfe) { isId = false; }
                                    }
                                    if ("0000000000000000".equals(value)) isId = false;
                                    if (isId)
                                        output += baselink+"&id="+value+"\">"+value+"</a>";  
                                    else
                                        output += value; 
                                    break;
                                default: rowhtml+= "--";
                            }
                        }
                        if (output == null) output = "&nbsp;";
                        else if ("".equals(output)) output = "&nbsp;";
                        rowhtml += "<td>"+output+"</td>";                       
                    } else {
                        switch (myObj1.getAttr(i).getDataType()) {
                            case IDfAttr.DM_BOOLEAN : rowhtml += "<td>"+myObj1.getBoolean(myObj1.getAttr(i).getName())+"</td>"; break;  
                            case IDfAttr.DM_DOUBLE : rowhtml += "<td>"+myObj1.getDouble(myObj1.getAttr(i).getName())+"</td>"; break;  
                            case IDfAttr.DM_ID : rowhtml += "<td>"+baselink+"&id="+myObj1.getId(myObj1.getAttr(i).getName()).getId()+"\">"+myObj1.getId(myObj1.getAttr(i).getName()).getId()+"</td>"; break;  
                            case IDfAttr.DM_INTEGER:
                                if (translations != null && translations.containsKey(myObj1.getAttr(i).getName())) {
                                    Map attrtranslate = (Map)translations.get(myObj1.getAttr(i).getName());
                                    rowhtml += "<td>"+attrtranslate.get(myObj1.getInt(myObj1.getAttr(i).getName()))+"</td>";
                                } else { rowhtml += "<td>"+myObj1.getInt(myObj1.getAttr(i).getName())+"</td>"; } 
                                break;  
                            case IDfAttr.DM_TIME: rowhtml += "<td>"+myObj1.getTime(myObj1.getAttr(i).getName()).asString(IDfTime.DF_TIME_PATTERN18)+"</td>"; break;
                            case IDfAttr.DM_STRING:
                                // see if it's an ID anyway (ID type detection doesn't seem to work...)
                                String value = myObj1.getString(myObj1.getAttr(i).getName());
                                if (translations != null && translations.containsKey(myObj1.getAttr(i).getName())) {
                                    Map attrtranslate = (Map)translations.get(myObj1.getAttr(i).getName());
                                    value = (String)attrtranslate.get(value);
                                }
                                boolean isId = false;
                                if (value.length() == 16) {
                                    try {
                                        Long.parseLong(value,16);
                                        isId = true;
                                    } catch (NumberFormatException nfe) { isId = false; }
                                }
                                if ("0000000000000000".equals(value)) isId = false;
                                if (isId)
                                    rowhtml += "<td>"+baselink+"&id="+value+"\">"+value+"</td>";  
                                else {
                                    if (value == null) value = "&nbsp;";
                                    else if ("".equals(value)) value = "&nbsp;";
                                    rowhtml += "<td>"+value+"</td>";
                                }
                                break;
                            default: rowhtml+= "--";
                        }
                    }
                }
                rowhtml += "</tr>";
                htmlbuffer.write(rowhtml);
            }
            } catch (Exception e) {
                htmlbuffer.write("</table><BR><BR><BR>ERROR during query execution: "+e+"<BR>record #: "+count+"<BR>Stacktrace:<BR><table>");
                e.printStackTrace(htmlbuffer);
            } catch (Throwable t) {
                int a=1;
                a++;
            } finally { 
                myObj1.close();
            }
        } catch (Exception e) {
            htmlbuffer.write("</table>Error or Bad Query<BR>"+e.getMessage()+"<br>Query: "+query);
        }
        htmlbuffer.write("</table>");
        htmlbuffer.close();
        
        return sw.toString();
        
    }

}
