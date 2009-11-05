package org.tarantula.component;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;
import org.tarantula.util.KeyedPreparedStatement;

public class ExecSQLUpdate implements PageComponentInterface {

    public String getNickname() { return "ExecSQLUpdate"; }

    public void init(){}

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        // get datasource...
        // request connection from connection pool (TBD) 

        // get the connection
        Connection conn = null;
        String keyedstatement = (String)arguments.get("KeyedStatement");
        
        // compile stmt
        KeyedPreparedStatement keyprepstmt = new KeyedPreparedStatement(conn,keyedstatement);        
        
        // set keys from arguments map (very dynamic arguments map here...)
        keyprepstmt.setWithMap(arguments);
        int rowschanged = keyprepstmt.getStatement().executeUpdate();

        // close it all
        keyprepstmt.getStatement().close();
        
        // set the prepared statement's fields (the ?s in the sql statement)
        return new Integer(rowschanged);
    }

    
}
