package org.tarantula.webgen;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

import com.thoughtworks.xstream.XStream;


public class InstallDatabeans implements PageComponentInterface 
{
    String Config; // databeans configuration file to be loaded by init()
    transient Map databeans;

    public void init() throws Exception {
        XStream xs = new XStream();
        File config = new File(Config);        
        databeans = (Map)xs.fromXML(new BufferedReader(new FileReader(config)));
    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception {
        // get database connection
        String dbdriver = (String)databeans.get("Database.Driver");
        String dburl =    (String)databeans.get("Database.URL");
        Class.forName(dbdriver);
        Connection conn = DriverManager.getConnection(dburl);
        // check if objectlist table has been created
        try {
            DatabaseMetaData dbdata = conn.getMetaData();
            ResultSet rs = dbdata.getTables(null,null,"WebGEN_ObjectList",null);
            if (!rs.next())
            {
                // table not found, need to create:
                Statement st = conn.createStatement();
                st.execute("Create table WebGEN_ObjectList (name varchar(255))");
                st.close();
            }
            rs.close();            
        } catch (Exception e) {
            throw e;            
        }
        // audit objectlist table entries, object tables, and object table columns
        Map beans = (Map)databeans.get("Databeans");
        Iterator i = beans.keySet().iterator();
        while (i.hasNext())
        {
            String beanname = (String)i.next();
            // add to objectlist table (if necessary)
            AddToObjectList(beanname,conn);
            // create table (if necessary)
            
        }
        
        
        return null;
    }

    public void AddToObjectList(String beanname, Connection conn) throws Exception
    {
        Statement st = conn.createStatement();
        ResultSet rs = st.executeQuery("SELECT name FROM WebGEN_ObjectList WHERE name = '"+beanname+"'");
        if (!rs.next())
        {
            // need to add it
            Statement stadd = conn.createStatement();
            stadd.executeUpdate("INSERT INTO WebGEN_ObjectList VALUES ('"+beanname+"')");
            stadd.close();
        }
        rs.close(); st.close();
    }
    
    public static void main(String[] args)
    {
        // serialize a config
        HashMap beanconfig = new HashMap();
        beanconfig.put("Database.Driver","org.apache.derby.jdbc.EmbeddedDriver");
        beanconfig.put("Database.URL","jdbc:derby:f:/projects/databases/booklibrary2;create=true");
        
        
    }

}
