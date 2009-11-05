package org.tarantula.util;

import java.io.InputStream;
import java.io.Reader;
import java.math.BigDecimal;
import java.net.URL;
import java.sql.Array;
import java.sql.Blob;
import java.sql.Clob;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Time;
import java.sql.Timestamp;
import java.sql.Types;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

/**
 * @author muellc4
 * 
 * prepared statements with $'d keys, not ?'s
 */
public class KeyedPreparedStatement
{
   String keyedStatement;
   String jdbcStatement;
   PreparedStatement prepstmt;
   Map keymap;


   public KeyedPreparedStatement(Connection conn, String statement) throws SQLException
   {
       keyedStatement = statement;
       ParseStatement(); // generates keymap and conventional JDBCprepstmt from provided keyed statement
       prepstmt = conn.prepareStatement(jdbcStatement);
   }

   private KeyedPreparedStatement()
   {
       // for use only by main() method for testing...
   }

   // TODO: do a pure array treatment, which should be faster...
   // fix case if at end of string...
   void ParseStatement()
   {
       jdbcStatement = "";
       keymap = new HashMap();

       // parse statement, tracking keys and replacing with std JDBC prepstmt ?'s
       String scratch = keyedStatement;

       int tokencount = 0;
       while (scratch.indexOf('$') != -1)
       {
           int tokenstart = scratch.indexOf('$');
           // store statement fragment before the current token...
           jdbcStatement += scratch.substring(0,tokenstart);

           // get current token:
           boolean first = true;
           boolean isToken = false;
           int index = tokenstart+1;
           while (true)
           {
               if (first)
               {
                   first = false;
                   // first char of token must be letter or underscore
                   if (index >= scratch.length() || !(Character.isLetter(scratch.charAt(index)) || scratch.charAt(index) == '_'))
                   {
                       // not a token...
                       isToken = false;
                       break;
                   }
                   else
                   {
                       isToken = true;
                       index++;
                   }
               }
               else
                   // subsequent chars can be any alphanumeric or '_',
                   if (index >= scratch.length() || !(Character.isLetterOrDigit(scratch.charAt(index)) || scratch.charAt(index) == '_'))
                       break; // (end of token)
                   else
                       index++;
           }
           // check if this is a token...
           if (!isToken)
           {
               // increment scan by one...
               jdbcStatement += scratch.charAt(tokenstart);
               if (tokenstart+1 >= scratch.length())
                   scratch = "";
               else
                   scratch = scratch.substring(tokenstart+1);
           }
           else
           {
               // get token:
               String token = scratch.substring(tokenstart+1,index);
               keymap.put(token,new Integer(tokencount+1)); // prep statements
                                                            // begin indexing at
                                                            // 1, not 0...
               tokencount++;
               // add prepstatment ?
               jdbcStatement += '?';
               // substr scratch to next token position...
               scratch = scratch.substring(index);
           }
       }
       jdbcStatement += scratch;
   }

   // setMap
   // caller provides a Map keyed with strings that match the keysparsed in the
    // KeyedPreparedStatement constructor,
   // and the entry object for that key will be set depending on
    // it'sauto-detected type (null, String, Integer, Date, Double, etc).
   // note that primitives must be wrapped in their Wrapper classes(int in
    // Integer, boolean in Boolean, etc)
   public void setWithMap(Map data) throws SQLException
   {
       Iterator i = data.keySet().iterator();
       while (i.hasNext())
       {
           String key = (String)i.next();
           Object o = data.get(key);
           // massive instanceof branching
           if (o == null) setNull(key, Types.NULL); // <-- I sense portability
                                                    // problems b/w DB
                                                    // vendors...
           else if (o instanceof String)  setString(key,(String)o);
           else if (o instanceof Integer) setInt (key,((Integer)o).intValue());
           else if (o instanceof Date)    setDate (key, (Date)o);
           else if (o instanceof Double)  setDouble (key,((Double)o).doubleValue());
           else if (o instanceof Integer) setInt (key,((Integer)o).intValue());
           else if (o instanceof Float)   setFloat (key,((Float)o).floatValue());
           else if (o instanceof Boolean) setBoolean (key,((Boolean)o).booleanValue());
           else if (o instanceof Short)   setShort (key,((Short)o).shortValue());
           else if (o instanceof Byte)    setByte (key,((Byte)o).byteValue());
           else if (o instanceof Long)    setLong (key,((Long)o).longValue());
           else if (o instanceof Array)   setArray(key, (Array)o);
           else if (o instanceof Time)    setTime(key, (Time)o);
           else if (o instanceof URL)     setURL(key, (URL)o);
           else if (o instanceof Blob)    setBlob(key, (Blob)o);
           else if (o instanceof Clob)    setClob(key, (Clob)o);
           else if (o instanceof byte[])  setBytes(key, (byte[])o);
           else if (o instanceof Timestamp) setTimestamp(key,(Timestamp)o);
           else if (o instanceof BigDecimal) setBigDecimal(key,(BigDecimal)o);
           else throw new NullPointerException("auto-set in setWithMapcould not determine type of "+key+": "+o.getClass());
       }
   }

   // stmt accessor
   public PreparedStatement getStatement() { return prepstmt; }

   // trivial sets
   public void setArray(String key, Array x) throws SQLException {prepstmt.setArray(((Integer)keymap.get(key)).intValue(),x); }
   public void setDate(String key, Date x) throws SQLException {prepstmt.setDate(((Integer)keymap.get(key)).intValue(),x); }
   public void setDate(String key, Date x, Calendar c) throws SQLException {prepstmt.setDate(((Integer)keymap.get(key)).intValue(),x,c); }
   public void setTime(String key, Time x) throws SQLException {prepstmt.setTime(((Integer)keymap.get(key)).intValue(),x); }
   public void setTime(String key, Time x, Calendar c) throws SQLException {prepstmt.setTime(((Integer)keymap.get(key)).intValue(),x,c); }
   public void setTimestamp(String key, Timestamp x) throws SQLException {prepstmt.setTimestamp(((Integer)keymap.get(key)).intValue(),x); }
   public void setTimestamp(String key, Timestamp x, Calendar c) throws SQLException {prepstmt.setTimestamp(((Integer)keymap.get(key)).intValue(),x,c); }
   public void setURL(String key, URL x) throws SQLException {prepstmt.setURL(((Integer)keymap.get(key)).intValue(),x); }
   public void setBigDecimal(String key, BigDecimal x) throws SQLException {prepstmt.setBigDecimal(((Integer)keymap.get(key)).intValue(),x); }
   public void setBlob(String key, Blob x) throws SQLException {prepstmt.setBlob(((Integer)keymap.get(key)).intValue(),x); }
   public void setClob(String key, Clob x) throws SQLException {prepstmt.setClob(((Integer)keymap.get(key)).intValue(),x); }
   public void setBoolean(String key, boolean x) throws SQLException {prepstmt.setBoolean(((Integer)keymap.get(key)).intValue(),x); }
   public void setByte(String key, byte x) throws SQLException {prepstmt.setByte(((Integer)keymap.get(key)).intValue(),x); }
   public void setBytes(String key, byte[] x) throws SQLException {prepstmt.setBytes(((Integer)keymap.get(key)).intValue(),x); }
   public void setDouble(String key, double x) throws SQLException {prepstmt.setDouble(((Integer)keymap.get(key)).intValue(),x); }
   public void setFloat(String key, float x) throws SQLException {prepstmt.setFloat(((Integer)keymap.get(key)).intValue(),x); }
   public void setInt(String key, int x) throws SQLException {prepstmt.setInt(((Integer)keymap.get(key)).intValue(),x); }
   public void setLong(String key, long x) throws SQLException {prepstmt.setLong(((Integer)keymap.get(key)).intValue(),x); }
   public void setShort(String key, short x) throws SQLException {prepstmt.setShort(((Integer)keymap.get(key)).intValue(),x); }
   public void setString(String key, String x) throws SQLException {prepstmt.setString(((Integer)keymap.get(key)).intValue(),x); }
   // nontrivial sets
   public void setObject(String key, Object x) throws SQLException {prepstmt.setObject(((Integer)keymap.get(key)).intValue(),x); }
   public void setObject(String key, Object x, int sqltype) throws SQLException {prepstmt.setObject(((Integer)keymap.get(key)).intValue(),x,sqltype); }
   public void setObject(String key, Object x, int sqltype, int scale) throws SQLException {prepstmt.setObject(((Integer)keymap.get(key)).intValue(),x,sqltype,scale); }
   public void setNull(String key, int sqltype) throws SQLException {prepstmt.setNull(((Integer)keymap.get(key)).intValue(),sqltype); }
   public void setNull(String key, int sqltype, String typename) throws SQLException {prepstmt.setNull(((Integer)keymap.get(key)).intValue(),sqltype,typename); }
   public void setAsciiStream(String key, InputStream x, int length) throws SQLException {prepstmt.setAsciiStream(((Integer)keymap.get(key)).intValue(),x,length);}
   public void setBinaryStream(String key, InputStream x, int length) throws SQLException {prepstmt.setBinaryStream(((Integer)keymap.get(key)).intValue(),x,length); }
   public void setCharacterStream(String key, Reader x, int length) throws SQLException {prepstmt.setCharacterStream(((Integer)keymap.get(key)).intValue(),x,length); }
   public void setUnicodeStream(String key, InputStream x, int length) throws SQLException {prepstmt.setUnicodeStream(((Integer)keymap.get(key)).intValue(),x,length); }

   // TODO: add rest of pass-throughs?

   public static void main(String[] args) throws Exception
   {
       KeyedPreparedStatement stmt = new KeyedPreparedStatement(); 
       stmt.keyedStatement = "fdsjklsdf $keyone askdfkj $keytwosdjfksd";
       stmt.ParseStatement();

       stmt.keyedStatement = "sdlfjlk $keyA dsfjksl $keyB";
       stmt.ParseStatement();

       stmt.keyedStatement = "sdlfjlk $5.00 $keyA dsfjksl $keyB $";
       stmt.ParseStatement();

       int a=1;
   }

}