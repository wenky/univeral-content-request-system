package org.webgen.util;


import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.logging.LogFactory;
import org.keyedstatement.KeyedPreparedStatement;
import org.webgen.core.WebgenLogger;

// performs key-style query
// converts result set to xml doc...

public class SQLQuery {
	
	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(SQLQuery.class));		
	
    public static String execXml(Connection conn, String statement, Map context)
    {
    	return (String)exec(conn,statement,context,"XML");
    }

    public static List execList(Connection conn, String statement, Map context)
    {
    	return (List)exec(conn,statement,context,"List");
    }
	
	// exec keyed statement (?how do I get the context...?)	
    public static Object exec(Connection conn, String statement, Map context, String mode) 
    {
		/*-CFG-*/String m="exec(conn,str,map)-";
		/*-TRC-*/if(log.tOn)log.trc(m+"compiling keyed statement"+statement);
    	KeyedPreparedStatement keyprepstmt;
        try { 
	        keyprepstmt = new KeyedPreparedStatement(conn,statement);
        } catch (SQLException sqle) {
			/*-ERR-*/log.err(m+"Could not compile keyed statement "+statement,sqle);
        	RuntimeException re = new RuntimeException("SQL Error while compiling keyed statement "+statement,sqle);
        	throw re;
        }
        
        // set keys from arguments map (very dynamic arguments map here...)
		/*-TRC-*/if(log.tOn)log.trc(m+"setting keyed parameters in keyed statement");
        try { 
        	keyprepstmt.setWithMap(context);
        } catch (SQLException sqle) {
			/*-ERR-*/log.err(m+"Could not set keyed parameters"+statement,sqle);
        	RuntimeException re = new RuntimeException("SQL Error while setting keyed values in keyed statement "+statement,sqle);
        	throw re;
        }

        ResultSet l_rs;
        ResultSetMetaData l_metadata;
        try {
    		/*-TRC-*/if(log.tOn)log.trc(m+"getting result set");
	        l_rs = keyprepstmt.getStatement().executeQuery();
    		/*-TRC-*/if(log.tOn)log.trc(m+"getting result set metadata");
	        l_metadata = keyprepstmt.getStatement().getMetaData();
	    } catch (SQLException sqle) {
	    	RuntimeException re = new RuntimeException("SQL Error while executing keyed statement "+statement,sqle);
	    	throw re;
	    }

	    try {
		    if ("List".equals(mode))
		    {
				/*-TRC-*/if(log.tOn)log.trc(m+"converting result set to List o' Maps collection");
		        List datalist = makelist(l_rs, l_metadata);
		        return datalist;
		    }
		    else if ("XML".equals(mode))
		    {
				/*-TRC-*/if(log.tOn)log.trc(m+"converting result set to XML doc");
		        String xml = makexml(l_rs, l_metadata);
				/*-TRC-*/if(log.tOn)log.trc(m+"xmldoc: "+xml);
		        return xml;
		    }
		    else
		    {
				/*-ERR-*/log.err(m+"Invalid mode specified: "+mode);
	        	RuntimeException re = new RuntimeException("Invalid mode specified: "+mode);
	        	throw re;
		    }
	    } finally {	
	        try {
	    		/*-TRC-*/if(log.tOn)log.trc(m+"cleaning up");
		        l_rs.close();
		        keyprepstmt.getStatement().close();
		    } catch (SQLException sqle) {
				/*-ERR-*/log.err(m+"error in cleanup of SQL structs");
		    	RuntimeException re = new RuntimeException("SQL Error while cleaning up sql objects",sqle);
		    	throw re;
		    }
	    }
    }
	
	public static String makexml(ResultSet rs, ResultSetMetaData md)
	{		
        // convert resultset to XML using metadata
        StringBuffer l_strbuf = new StringBuffer("<ResultSet>");
        try {
		    while (rs.next()) {
		        l_strbuf.append("<Row>");
		        for (int i = 1; i <= md.getColumnCount(); i++) {
		            l_strbuf.append("<" + md.getColumnName(i) + ">");
		            Object l_objColumnValue = rs.getObject(i);
		            if (l_objColumnValue != null)
		                l_strbuf.append(l_objColumnValue.toString());
		            l_strbuf.append("</" + md.getColumnName(i) + ">");
		        }
		        l_strbuf.append("</Row>");
		    }
		    l_strbuf.append("</ResultSet>");
        } catch (SQLException sqle) {
        	RuntimeException re = new RuntimeException("SQL Error while converting resultset to xml",sqle);
        	throw re;
        }
        return l_strbuf.toString();
	}
    
	public static List makelist(ResultSet rs, ResultSetMetaData md)
	{
        // do Lists+Maps
        List rows = new ArrayList();
        try {
	        while (rs.next()) {
	            Map row = new HashMap();
	            for (int i = 1; i <= md.getColumnCount(); i++) {
	                
	                Object l_objColumnValue = rs.getObject(i);
	                if (l_objColumnValue != null)
	                    row.put(md.getColumnName(i),l_objColumnValue);
	                else
	                    row.put(md.getColumnName(i),null);
	            }
	            rows.add(row);
	        }
	    } catch (SQLException sqle) {
	    	RuntimeException re = new RuntimeException("SQL Error while converting resultset to xml",sqle);
	    	throw re;
	    }
        return rows;            
    }
        

}
