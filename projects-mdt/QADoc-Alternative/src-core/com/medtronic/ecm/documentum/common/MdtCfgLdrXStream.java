package com.medtronic.ecm.documentum.common;

/**
 * Default config loader for ConfigService that uses XStream to load application configs. This
 * loader assumes the existence of two files in the docbase named in the following manner:
 * 
 *   - application key + .aliases  - list of classname aliases for the xstream decoder (text document/xml)
 *   - application key + .xml  - config xml to deserialize (text document/xml)
 *   
 * This can run as both an SBO and as a normal POJO. Since it is the default one for ConfigService
 * 
 * @author $Author: dentrf1 $ 
 * @version $Revision: 1.5 $
 *
 */

import java.io.ByteArrayInputStream;
import java.util.HashMap;
import java.util.Map;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.sbo.IMdtConfigLoader;
import com.thoughtworks.xstream.XStream;

public class MdtCfgLdrXStream extends DfService implements IMdtConfigLoader
{
	public Map loadConfig(String docbase, String application, String configfile)
	{
		/*-INFO-*/Lg.inf("SBO invocation of Default XStream loader for docbase %s application key %s and configfile %s",docbase,application,configfile); 
		/*-dbg-*/Lg.dbg("call the static non-SBO load call");
		Map cfg = loadConfigNonSBO(this.getSessionManager(),docbase,application,configfile);
		/*-dbg-*/Lg.dbg("done, returning config");
		return cfg;
	}
	
    /**
     * Assuming the content is text or xml, this returns a string with the contents of the 
     * supplied sysobject. 
     * 
     * @param sysObj Sysobject to grab the content from, must be text or xml or this will not turn out well. 
     * 
     */
	public static String readDocbaseTextFile(IDfSysObject sysObj)
	{
		/*-INFO-*/Lg.inf("sysObj: %s",sysObj); 
        IDfClientX clientx = new DfClientX();
        ByteArrayInputStream bais = null;
		try { 
			bais = sysObj.getContent();
		} catch (DfException dfe) {
			/*-ERROR-*/Lg.err("Error getting text/char content from docbase object",dfe);
			throw EEx.create("CS-ContentReadError","Error getting text/char content from docbase object",dfe);			
		}
        String aliasfilecontent = null;
        if (bais.available() > 0)
        {
            /*-TRC-*/Lg.trc("unmarshalling ByteArrayInputStream");
            aliasfilecontent = clientx.ByteArrayInputStreamToString(bais);
            /*-TRC-*/Lg.trc("unmarshalled aliasfilecontent: %s",aliasfilecontent);
        }
        return aliasfilecontent;

	}
	

	
	
	///Hack temp non-SBO
	public static Map loadConfigNonSBO(IDfSessionManager smgr, String docbase, String application, String configfile)
	{
		/*-INFO-*/Lg.inf("Static non-SBO invocation of Default XStream loader for docbase %s application key %s and configfile %s",docbase,application,configfile); 
		IDfSession session = null;
		IDfDocument aliasdoc = null;
		IDfDocument configdoc = null;
		String configfilecontents = null;
		// magically get the session. ?What is the privledge of this session?
		try { 
			/*-dbg-*/Lg.dbg("get session"); 
			session = smgr.getSession(docbase);
			/*-dbg-*/Lg.dbg("look up config doc"); 
			configdoc = (IDfDocument)session.getObjectByQualification("mdt_appcfg_xstream where m_application = '"+application+"'");
			/*-dbg-*/Lg.dbg("read file contents of object %s",configdoc); 
			configfilecontents = readDocbaseTextFile(configdoc);
			/*-trc-*/Lg.trc("configfile: %s",configfilecontents);
		} catch (DfException dfe) {
			// attempt session cleanup
			try {smgr.release(session);} catch (Exception e) {Lg.wrn("Session release failure in catch",e);} 
			/*-ERROR-*/Lg.err("DCTM Error in reading alias file or config file for application %s in docbase %s",application,docbase,dfe);
			throw EEx.create("CfgLdrXS-DCTM","DCTM Error in reading alias file or config file for application %s in docbase %s",application,docbase,dfe);
		} finally {
			/*-dbg-*/Lg.dbg("attempt session release");
			try {smgr.release(session);}catch(Exception e){/*-WARN-*/Lg.wrn("session release attempt failed");}		  
		}

        // prepare primary XStream with alias list
        /*-dbg-*/Lg.dbg("instantiate XStream"); 
        XStream xstream = new XStream();        
        try {
        	/*-dbg-*/Lg.dbg("iterate through aliases in alias attrs"); 
	        for (int i=0; i < configdoc.getValueCount("m_alias_displaynames"); i++)
	        {
        		/*-dbg-*/Lg.dbg("begin next alias read"); 
	        	String currentdisplayname = configdoc.getRepeatingString("m_alias_displaynames", i); 
        		/*-dbg-*/Lg.dbg("  --displayname: %s",currentdisplayname); 
	        	String currentclassname = configdoc.getRepeatingString("m_alias_classnames", i);
        		/*-dbg-*/Lg.dbg("  --classname: %s",currentclassname); 	        	
	            try { 
        			/*-dbg-*/Lg.dbg("setting alias"); 	        	
	            	xstream.alias(currentdisplayname,Class.forName(currentclassname));
	            } catch (ClassNotFoundException cnfe) {
	    			/*-ERROR-*/Lg.err("XStream alias class %s not visible to JVM",currentclassname,cnfe);
	    			throw EEx.create("CfgLdrXS-Alias-CNFE","XStream alias class %s not visible to JVM",currentclassname,cnfe);
	            } 
	        }
        } catch (DfException dfe) {
			/*-ERROR-*/Lg.err("Documentum error while reading xstream aliases from config object %s",configdoc);
			throw EEx.create("CfgLdrXS-Alias-DFE","Documentum error while reading xstream aliases from config object %s",configdoc);            	
        }
        
        //get configfile.xml
        /*-dbg-*/Lg.dbg("deserializing configfile's contents using aliases xstream deserializer"); 	        	
		Object configuration = null;
		try { 
			configuration = xstream.fromXML(configfilecontents);
        } catch (Exception e) {
			/*-ERROR-*/Lg.err("XStream parsing error in deserialization of primary config file",e);
			throw EEx.create("CfgLdrXS-CfgRd","XStream parsing error in deserialization of primary config file",e);
        }
        
        // Create map with base objects, xml, and parsed xml
        /*-dbg-*/Lg.dbg("done!"); 	        	
		Map returnmap = new HashMap();
		returnmap.put(application, configuration);        
        return returnmap;
	}
	
	
	
	
	
	
	
	
	
	
	
}
