package org.tarantula.component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;
import org.tarantula.util.KeyedPreparedStatement;

// performs key-style query
// converts result set to xml doc...

public class ExecSQLQuery implements PageComponentInterface {

    public String getNickname() { return "ExecSQLQuery"; }
    
    public void init(){}

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        // get datasource...
        // request connection from connection pool (TBD) 

        // get the connection
        Connection conn = null;
        String keyedstatement = (String)arguments.get("KeyedStatement");
        String outputformat   = (String)arguments.get("OutputFormat");
        
        // compile stmt
        KeyedPreparedStatement keyprepstmt = new KeyedPreparedStatement(conn,keyedstatement);        
        
        // set keys from arguments map (very dynamic arguments map here...)
        keyprepstmt.setWithMap(arguments);

        ResultSet l_rs = keyprepstmt.getStatement().executeQuery();
        ResultSetMetaData l_metadata = keyprepstmt.getStatement().getMetaData();

        if ("XML".equals(outputformat))
        {
            // convert resultset to XML using metadata
            StringBuffer l_strbuf = new StringBuffer("<ResultSet>");
            while (l_rs.next()) {
                l_strbuf.append("<Row>");
                for (int i = 1; i <= l_metadata.getColumnCount(); i++) {
                    l_strbuf.append("<" + l_metadata.getColumnName(i) + ">");
                    Object l_objColumnValue = l_rs.getObject(i);
                    if (l_objColumnValue != null)
                        l_strbuf.append(l_objColumnValue.toString());
                    l_strbuf.append("</" + l_metadata.getColumnName(i) + ">");
                }
                l_strbuf.append("</Row>");
            }
            l_strbuf.append("</ResultSet>");
    
            // close it all
            l_rs.close();
            keyprepstmt.getStatement().close();

            // set the prepared statement's fields (the ?s in the sql statement)
            return l_strbuf.toString();
        }
        else
        {
            // do Lists+Maps
            List rows = new ArrayList();
            while (l_rs.next()) {
                Map row = new HashMap();
                for (int i = 1; i <= l_metadata.getColumnCount(); i++) {
                    
                    Object l_objColumnValue = l_rs.getObject(i);
                    if (l_objColumnValue != null)
                        row.put(l_metadata.getColumnName(i),l_objColumnValue);
                    else
                        row.put(l_metadata.getColumnName(i),null);
                }
                rows.add(row);
            }
    
            // close it all
            l_rs.close();
            keyprepstmt.getStatement().close();

            // set the prepared statement's fields (the ?s in the sql statement)
            return rows;
            
        }
        
    }

}
