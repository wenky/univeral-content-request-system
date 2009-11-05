package org.webgen.util;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.LogFactory;
import org.keyedstatement.KeyedPreparedStatement;
import org.webgen.core.WebgenLogger;


public class SQLUpdate {

	protected static WebgenLogger log = new WebgenLogger(LogFactory.getLog(SQLUpdate.class));		

    public static int exec(Connection conn, String statement, Map context) 
    {
		/*-CFG-*/String m="exec(conn,str,map)-";
        // compile stmt
		KeyedPreparedStatement keyprepstmt;
		try { 
			/*-TRC-*/if(log.tOn)log.trc(m+"compiling keyed statement"+statement);        
	        keyprepstmt = new KeyedPreparedStatement(conn,statement);        
	    } catch (SQLException sqle) {
			/*-ERR-*/log.err(m+"Could not compile keyed statement "+statement,sqle);
	    	RuntimeException re = new RuntimeException("SQL Error while compiling keyed statement "+statement,sqle);
	    	throw re;
	    }
        
        // set keys from context map 
		/*-TRC-*/if(log.tOn)log.trc(m+"populating "+statement);        
        try { 
        	keyprepstmt.setWithMap(context);
        } catch (SQLException sqle) {
			/*-ERR-*/log.err(m+"Could not set keyed parameters"+statement,sqle);
        	RuntimeException re = new RuntimeException("SQL Error while setting keyed values in keyed statement "+statement,sqle);
        	throw re;
        }
        
		/*-TRC-*/if(log.tOn)log.trc(m+"executing update");        
		int rowschanged;
        try {
	        rowschanged = keyprepstmt.getStatement().executeUpdate();
	    } catch (SQLException sqle) {
			/*-ERR-*/log.err(m+"error in update execution"+statement,sqle);
	    	RuntimeException re = new RuntimeException("SQL Error while executing update "+statement,sqle);
	    	throw re;
	    }
        // close it all
		/*-TRC-*/if(log.tOn)log.trc(m+"cleanup...");        
	    try {
	    	keyprepstmt.getStatement().close();
	    } catch (SQLException sqle) {
			/*-ERR-*/log.err(m+"error in cleanup of SQL structs");
	    	RuntimeException re = new RuntimeException("SQL Error while cleaning up sql objects",sqle);
	    	throw re;
	    }
        
        // set the prepared statement's fields (the ?s in the sql statement)
        return rowschanged;
    }

    
}
