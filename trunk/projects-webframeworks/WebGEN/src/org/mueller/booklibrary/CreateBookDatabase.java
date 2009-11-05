package org.mueller.booklibrary;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.Statement;

public class CreateBookDatabase {


    static String createdatabase =
        " CREATE TABLE books ("+
        "   keyname varchar(512), "+
        "   title   varchar(512), "+
        "   categories varchar(1024), "+
        "   extention varchar(5), "+
        "   link varchar(1024) "+
        ")";
    /**
     * @param args
     */
    public static void main(String[] args) throws Exception
    {
        try {
            Class.forName("org.apache.derby.jdbc.EmbeddedDriver");
            // URL is "jdbc:derby:<databasename>"
            // note: you cannot create the directory the database will be in, derby has to do it.
            //       so if you want to create the db booklibrary in f:\projects\databases, don't create
            //       the booklibrary directory manually, let derby do it when you connect using:
            //       'jdbc:derby:f:/projects/databases/booklibrary;create=true' as the JDBC connection url.
            Connection c = DriverManager.getConnection("jdbc:derby:c:\\projects\\databases\\booklibrary;create=true");
            Statement s = c.createStatement();
            s.execute(createdatabase);
            s.close();
            c.close();
        } catch (Exception e) {
            // check it out...
            Throwable t = e;
            while (t != null)
            {
               System.err.println("Message: "+t.getMessage());
               t.printStackTrace();
               t = t.getCause();
            }
        }
    }

}
