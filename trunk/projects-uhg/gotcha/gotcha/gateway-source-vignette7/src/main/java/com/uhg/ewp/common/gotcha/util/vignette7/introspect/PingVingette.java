package com.uhg.ewp.common.gotcha.util.vignette7.introspect;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;
import javax.sql.DataSource;

import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.Site;

/*
import com.vignette.as.client.common.AppObjectId;
import com.vignette.as.client.common.AttributeData;
import com.vignette.as.client.common.AttributeDefinitionData;
import com.vignette.as.client.common.ChannelData;
import com.vignette.as.client.common.ContentRelationInstance;
import com.vignette.as.client.common.ObjectTypeData;
import com.vignette.as.client.common.RelationData;
import com.vignette.as.client.common.RequestParameters;
import com.vignette.as.client.exception.ApplicationException;
import com.vignette.as.client.exception.ValidationException;
import com.vignette.as.client.javabean.AttributedObject;
import com.vignette.as.client.javabean.Channel;
import com.vignette.as.client.javabean.ContentInstance;
import com.vignette.as.client.javabean.ContentType;
import com.vignette.as.client.javabean.IPagingList;
import com.vignette.as.client.javabean.Site;
*/


public class PingVingette
{
    
    public static int sitecount()    
    {
        try { 
            int sitecount = Site.getSiteCount();
            return sitecount;
        } catch (ApplicationException ae) {
            throw new RuntimeException(ae);
        } catch (ValidationException ve) {
            throw new RuntimeException(ve);
        }        
    }
    
    public static String lookupDataSource(String jndiname)
    {
        try { 
            Context ctx = new InitialContext();
            DataSource ds = (DataSource) ctx.lookup(jndiname);
            
            Connection conn = ds.getConnection();
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT 1 FROM DUAL");
            rs.next();
            String value = rs.getObject(1).toString();
            rs.close();
            stmt.close();
            conn.close();
            
            if (ds != null)
                return "DS "+jndiname+" located";
            else return "DS "+jndiname+" not found";
        } catch (NamingException ne) {
            throw new RuntimeException(ne);
        } catch (SQLException sqle) {
            throw new RuntimeException (sqle);
        }
        
    }

}
