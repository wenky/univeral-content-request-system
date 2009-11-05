package com.medtronic.ecm.documentum.common;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;

import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.DfQuery;
import com.documentum.fc.client.DfService;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfCollection;
import com.documentum.fc.client.IDfDocument;
import com.documentum.fc.client.IDfGroup;
import com.documentum.fc.client.IDfQuery;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.client.IDfSysObject;
import com.documentum.fc.common.DfException;
import com.documentum.fc.common.DfLoginInfo;
import com.documentum.fc.common.IDfId;
import com.documentum.fc.common.IDfLoginInfo;
import com.documentum.fc.common.IDfTime;
import com.medtronic.ecm.common.EEx;
import com.medtronic.ecm.common.Lg;
import com.medtronic.ecm.documentum.core.sbo.IMdtConfigLoader;
import com.medtronic.ecm.documentum.core.sbo.IMdtConfigService;

/**
 * Medtronic Layer facility for centralized configuration/context settings/information access for
 * all known execution environments (webtop/wdk, SBOs, TBOs, Methods, Jobs, or DFS Services). Has
 * two primary goals:
 * 
 * - provide common means for determining Medtronic application context and associated object type
 *   and role mappings
 * - provide application-specific configuration access that is storage/schema agnostic, including a
 *   dynamic config refresh facility.  
 *   
 * The first goal makes this facility required for any and all applications utilizing the Medtronic
 * Layer since it is core to the operation of many of the components (obtaining/assigning context 
 * and other tasks). The second goal is architecturally optional, and projects may implement their
 * own schemes/code/means for obtaining application data.
 * 
 * Configuration Service expects a set of documents in a given docbase to provide directions as to
 * what applications are served by a docbase, how roles and object types map to the application, 
 * and how to load its configuration information into memory. Here is a description of the core 
 * documents/objects:
 * 
 * mdt_configservice - object type that specifies how configservice loads and processes application
 * configuration information. By convention, this must be named MdtApplication_ + application name.
 * It uses these four attributes to customize its behavior:
 *  -  loaderclass - SBO of a custom loader class/deserializer (defaults to MdtCfgLdrXStream)
 *  -  configfile - name of file that configures the loader class (defaults to the content of the current object)
 *  -  refreshinterval - time in milliseconds between checks on the refresh config file (defaults to 100000 ms/100 seconds)
 *  -  refreshflag - name of file that is monitored for refresh (defaults to the current object)  
 * By default it will use an XStream-based deserializer. Regardless of the method used to obtain/load
 * the information, loader classes (which must implement IMdtConfigLoader) are expected to return
 * an object tree, map, list, or graph of java collections and/or simple java objects. These should
 * be accessed in a read-only manner. 
 * 
 * mdt_app_role_type_map - this provides core mapping information to the Medtronic Layer as to 
 * which object types and roles are associated with which application. It has been decided that
 * object types will be associated with a single Medtronic Application Code, and not used across
 * applications or sub-applications (a subapplication would be a specific Dept Docs instance). It
 * uses the following attributes to specify this mapping information:
 *  -  m_application - Medtronic application or subapplication key  
 *  -  m_types - list of object types that map to this application and role
 *  -  m_role - NOT USED CURRENTLY
 * 
 * MdtConfigService may be accessed as a POJO or called via SBO (TODO: ?web service?). It should be
 * available via POJO referencing in Medtronic-prepared webtop instances, from most TBOs, Methods,
 * and Web Services to make things simple. 
 * 
 * @author $Author: dentrf1 $
 *
 * @version $Revision: 1.6 $
 *
 */
 
public class MdtConfigService extends DfService implements IMdtConfigService 
{
	public static final String CONFIG_SERVICE_SEARCH_QUALIFICATION = "mdt_application_configsvc WHERE m_application = '";
	public static final String CONFIG_SERVICE_DEFAULT_LOADER = MdtCfgLdrXStream.class.getName();
	
	public static final String CONFIG_SERVICE_APPLICATION_FROM_TYPE_LOOKUP_QUERY = "SELECT r_object_id, m_application FROM mdt_application_configsvc WHERE any m_types = '";
	public static final String CONFIG_SERVICE_GET_CONFIG_SERVICE_QUERY = "SELECT r_object_id, m_application FROM mdt_application_configsvc";
	
	static Map applications = new HashMap();
	static Timer configmonitor = new Timer();
	
	// TODO - for single thread timer rewrite, when the time comes...
	static boolean trackerstarted = false;
	static boolean trackerfirst = true;
	static String trackerlist = "";
	
	IDfSessionManager sMgr;
	String docbase;

	
	
	protected MdtConfigService() {}


	
	/**
	 * Factory method for MdtConfigService. An authenicated session manager and docbase must be
	 * provided. Note this implies that config objects should be WORLD READ in general... 
	 */
	public static MdtConfigService getConfigService(IDfSessionManager mgr, String docbase)
	{
		MdtConfigService cs = new MdtConfigService();
		cs.docbase = docbase; 
		cs.sMgr = mgr;
		return cs;
	}


	
	/**
	 * Returns a list of object types that are mapped to the given application key and user. 
	 * Note that we ignore what groups the user is in now, since the decision has been made
	 * that object types are assigned exclusively to a single application and are not shared.
	 * 
	 * @param appname <font color="#0000FF"><b>(String)</b></font> application key/context
	 * 
	 * @return <font color="#0000FF"><b>List</b></font> - TODO:
	 * 
	 */
	public List getTypesForApplication(String appname)
	{
		IDfSession session = null;
		IDfCollection queryresults = null;
		try { 
			/*-dbg-*/Lg.dbg("get session");
			session = sMgr.getSession(docbase);
			String dql = CONFIG_SERVICE_GET_CONFIG_SERVICE_QUERY+" WHERE m_application = '"+appname+"'";
			/*-dbg-*/Lg.dbg("initializing query %s",dql);
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
			/*-dbg-*/Lg.dbg("exec");
			queryresults = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        Set results = new TreeSet();
			/*-dbg-*/Lg.dbg("iterate");
	        while (queryresults.next()) 
	        {
				/*-dbg-*/Lg.dbg("get next result");
	        	IDfId objid = queryresults.getId("r_object_id");
	        	String app = queryresults.getString("m_application");
				/*-dbg-*/if (Lg.dbg())Lg.dbg("found: %s %s",objid.getId(),app);
				/*-dbg-*/if (Lg.dbg())Lg.dbg("user is member of role, adding types - retrieve roletypemap sysobject");
				IDfSysObject roletypemap = (IDfSysObject)session.getObject(objid);
				/*-dbg-*/if (Lg.dbg())Lg.dbg("retrieved: %s",roletypemap);
				for (int i=0; i < roletypemap.getValueCount("m_types"); i++)
				{
					String newtype = roletypemap.getRepeatingString("m_types",i); 
					/*-dbg-*/if (Lg.dbg())Lg.dbg("adding type %s",newtype);
					results.add(roletypemap.getRepeatingString("m_types",i));
				}
	        }
			/*-dbg-*/if (Lg.dbg())Lg.dbg("close collection");	        
	        queryresults.close();
	        sMgr.release(session);

			/*-dbg-*/if (Lg.dbg())Lg.dbg("return set as list");	        
	        return new ArrayList(results);
		} catch (DfException dfe) {
        	// attempt cleanup on aisle 5
        	try {if (queryresults != null)queryresults.close(); } catch (Exception e) {Lg.wrn("collection could not be released in catch");}
        	try {if (session != null)sMgr.release(session); } catch (Exception e) {Lg.wrn("session could not be released in catch");}
			throw EEx.create("CS-GetAppTypes-DFE","DCTM error during types lookup",dfe);
        } catch (Throwable e) {
        	// let's be paranoid of NPEs at this low level...
        	try {if (queryresults != null)queryresults.close(); } catch (Exception e2) {Lg.wrn("collection could not be released in catch");}
        	try {if (session != null)sMgr.release(session); } catch (Exception e2) {Lg.wrn("session could not be released in catch");}       	
			throw EEx.create("CS-GetAppTypes-Err","Unexpected error during application lookup",e);
        } 
	}


	/**
	 * Given an object type name, return the Medtronic application key associated with it. 
	 * 
	 * @param type <font color="#0000FF"><b>(String)</b></font>
	 * 			name of a docbase type used in a medtronic application
	 * 
	 * @return <font color="#0000FF"><b>String</b></font> - TODO:
	 * 
	 */	
	public String getMdtApplicationName(String type)
	{
		/*-INFO-*/Lg.inf("type: [%s]",type);		
		IDfSession session = null;
		IDfCollection myObj1 = null;
		try { 
			/*-dbg-*/Lg.dbg("get session");
			session = sMgr.getSession(docbase);
			String dql = CONFIG_SERVICE_APPLICATION_FROM_TYPE_LOOKUP_QUERY +type+"'";
			/*-dbg-*/Lg.dbg("initializing query %s",dql);
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
			/*-dbg-*/Lg.dbg("exec");
	        myObj1 = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
	        List results = new ArrayList();
			/*-dbg-*/Lg.dbg("storing results for two-pass analysis");
	        while (myObj1.next()) 
	        {
				/*-dbg-*/Lg.dbg("get next result");
	        	IDfId objid = myObj1.getId("r_object_id");
	        	String app = myObj1.getString("m_application");
				/*-dbg-*/if (Lg.dbg())Lg.dbg("found: %s %s",objid.getId(),app);
	        	String[] result = {objid.getId(),app};
	        	results.add(result);
	        }
	        myObj1.close();
	        
			/*-dbg-*/Lg.dbg("check number of results");
			if (results.size() == 0) {
	        	sMgr.release(session);
	        	/*-ERROR-*/Lg.err("Object type %s not registered to any Medtronic application",type);
	        	throw EEx.create("CS-GetApp-TypeNotRegistered","Object type %s not registered to any Medtronic application",type);
			} else if (results.size() == 1) {
				/*-dbg-*/Lg.dbg("only one found, so roles don't even play into this - releasing session");
	        	sMgr.release(session);
				/*-dbg-*/Lg.dbg("extracting appname");
	        	String appname = ((String[])results.get(0))[1];
				/*-dbg-*/Lg.dbg("return app %s",appname);
	        	return appname;
	        } else {
	        	// return first role match...
//				/*-dbg-*/MdtLog.dbg("multiple results, so we need to resolve the role membership");
//	        	String appname = null;
//	        	boolean found = false;
//	        	for (int i=0; i < results.size(); i++) {
//					/*-dbg-*/MdtLog.dbg("get next role");
//	        		String role = ((String[])results.get(i))[2];
//					/*-dbg-*/MdtLog.dbg("check if user has role: %s",role);
//	        		if (session.getGroup(role).isUserInGroup(currentuser)) {
//						/*-dbg-*/MdtLog.dbg("user has the role, check for dupes/config errs");
//	        			if (!found) { 
//							/*-dbg-*/MdtLog.dbg("no dupe found yet, store this appname");
//	        				appname = ((String[])results.get(i))[1];
//							/*-dbg-*/MdtLog.dbg("appname: %s",appname);
//	        				found = true;
//	        			} else {
//	        				EnrichableException ee = EnrichableException.create("CS-GetApp-Dup","MDT Configuration Error - multiple applications found for a given type/role mapping on type %s",type); 
//							/*-ERROR-*/MdtLog.err("MDT Configuration Error - multiple applications found for a given type/role mapping on type %s and role %s",type,role,ee);
//							throw ee;
//	        			}
//	        		} 
//	        	}
//				/*-dbg-*/MdtLog.dbg("done with role matching, releasing session");
//	        	sMgr.release(session);
//				/*-dbg-*/MdtLog.dbg("returning appname: %s",appname);
//	        	return appname;
	        	sMgr.release(session);
	        	/*-ERROR-*/Lg.err("MDTConfigurationDetected an object type in multiple applications");
	        	throw EEx.create("CS-GetApp-TypeNotExclusive", "DCTM Object Type assigned to more than one MdtApplication ");
	        } 
		} catch (DfException dfe) {
        	// attempt cleanup on aisle 5
        	try {if (myObj1 != null)myObj1.close(); } catch (Exception e) {Lg.wrn("collection could not be released in catch");}
        	try {if (session != null)sMgr.release(session); } catch (Exception e) {Lg.wrn("session could not be released in catch");}
			throw EEx.create("CS-GetApp-DFE","DCTM error during application lookup for type %s",type,dfe);
        } catch (Throwable e) {
        	// let's be paranoid of NPEs at this low level...
        	try {if (myObj1 != null)myObj1.close(); } catch (Exception e2) {Lg.wrn("collection could not be released in catch");}
        	try {if (session != null)sMgr.release(session); } catch (Exception e2) {Lg.wrn("session could not be released in catch");}       	
			throw EEx.create("CS-GetApp-Err","Unexpected error during application lookup for type %s",type,e);
        }
	}
		
	
	
	/**
	 * Get application-specific configuration information for the given Medtronic application key.
	 * This is lazily loaded, so first request for an uncached application config may incur a 
	 * delay while the configuration loads/deserializes. Make sure the mdt_configservice object 
	 * has been created and configured for this application key in the docbase.
	 * 
	 * @param - application <font color="#0000FF"><b>(String)</b></font> key
	 * 
	 * @return <font color="#0000FF"><b>Object</b></font> - TODO:
	 * 
	 */		
	public Map getAppConfig(String application)
	{
		/*-INFO-*/Lg.inf("application: [%s]",application);		
		Object appconfig = applications.get(application);
		// do lazyload check on if application is loaded/cached
		if (appconfig == null) {
			/*-dbg-*/Lg.dbg("app not found, begin app config load");
			// trigger config load of requested application...
			loadApplicationConfig(application,sMgr);
			
			// do it again
			/*-dbg-*/Lg.dbg("get application objects");
			appconfig = applications.get(application);
		}
		/*-dbg-*/Lg.dbg("returning objects");
		return (Map)appconfig;
	}
	
//	// Object Graph Navigation Language expression evaluator
//	public static Object lookupOGNL(String exprOGNL, Object root)
//	{
//		/*-INFO-*/MdtLog.inf("expr: [%s]",exprOGNL);
//		try { 
//			/*-trc-*/MdtLog.trc("calling getValue on expr: %s",exprOGNL);
//			Object result = Ognl.getValue(exprOGNL,root);
//			return result;
//		} catch (ExpressionSyntaxException ese) {
//			/*-ERROR-*/MdtLog.err("Syntax Error on expr: %s",exprOGNL,ese);
//			throw EnrichableException.create("CS-BadExpr","Invalid OGNL expr: "+exprOGNL,ese);
//		} catch (MethodFailedException mfee) {
//			/*-ERROR-*/MdtLog.err("Failed method invocation inside expr: %s",exprOGNL,mfee);
//			throw EnrichableException.create("CS-ExprErr","Error occurred inside OGNL expr: "+exprOGNL,mfee);
//		} catch (NoSuchPropertyException nsp) {
//			/*-ERROR-*/MdtLog.err("Nonexistant property refrenced in expr: %s",exprOGNL,nsp);
//			throw EnrichableException.create("CS-BadProp","Invalid property referenced: "+exprOGNL,nsp);
//		} catch (InappropriateExpressionException iee) {
//			/*-ERROR-*/MdtLog.err("Inappropriate expression for given context: %s",exprOGNL,iee);
//			throw EnrichableException.create("CS-DirtyExpr","OGNL expr not valid for context: "+exprOGNL,iee);
//		} catch (OgnlException oe) {
//			/*-ERROR-*/MdtLog.err("General OGNL error: %s",exprOGNL,oe);
//			throw EnrichableException.create("CS-OGNLErr","OGNL error: "+exprOGNL,oe);
//		}
//	}

	// XPath expression evaluator
//	public Object lookupXPath(String exprXPath, Document domdocroot, QName resulttype /* <-- defined in XPathConstants */)
//	{
//		XPathConstants x;
//		/*-INFO-*/MdtLog.inf("xpath: [%s]",exprXPath);
//		XPathFactory xpf = XPathFactory.newInstance();
//		XPath xpath = xpf.newXPath();
//		try { 
//			XPathExpression expr = xpath.compile(exprXPath);
//			Object result = expr.evaluate(domdocroot, resulttype);
//			return result;
//		} catch (XPathExpressionException xpe) {
//			/*-ERROR-*/MdtLog.err("XPath Evaluation error : %s",exprXPath,xpe);
//			throw EnrichableException.create("CS-XPathErr","XPath Eval error: "+exprXPath,xpe);
//		}
//	}
	
//    /**
//     * Executes JOSQL query against the given data list. 
//     *
//     * @param josqlquery Query to be executed 
//     * @param root List of objects on which the query is executed 
//     * @return List of objects that result from the query execution
//     * 
//     */
//	public static List execJOSQL(String josqlquery, List root)
//	{
//		/*-INFO-*/MdtLog.inf("josqlquery: [%s] root isnull: %s",josqlquery,root==null);
//		try { 
//			Query q = new Query ();
//			/*-trc-*/MdtLog.trc("parsing JOSQL query");
//			q.parse(josqlquery);
//			/*-trc-*/MdtLog.trc("executing JOSQL query");
//			QueryResults qr = q.execute (root);
//			/*-trc-*/MdtLog.trc("returning query result");
//			List result = qr.getResults ();
//			return result;
//		} catch (QueryParseException qpe) {
//			/*-ERROR-*/MdtLog.err("JOSQL query parse error : %s",josqlquery,qpe);
//			throw EnrichableException.create("CS-JOSQLParseErr","JOSQL query parse error : "+josqlquery,qpe);
//		} catch (QueryExecutionException qee) {
//			/*-ERROR-*/MdtLog.err("JOSQL query exec error : %s",josqlquery,qee);
//			throw EnrichableException.create("CS-JOSQLExecErr","JOSQL query exec error : "+josqlquery,qee);
//		}
//	}

	
    /**
     * Performs config load of specified application. This method relies on a specific sysobject 
     * in the docbase configures the deserialization/loading process, named with the following
     * naming convention:
     * 
     *     MdtConfiguration_ + application key
     * 
     * This must be a sysobject of mdt_configservice [TODO change type name], which has four 
     * attributes which direct the behavior of the application's specific configuration load.
     *   loaderclass - SBO of loader class (defaults to MdtCfgLdrXStream)
     *   configfile - name of file that configures the loader class (defaults to the content of the current object)
     *   refreshinterval - time in milliseconds between checks on the refresh config file (defaults to 100000 ms/100 seconds)
     *   refreshflag - name of file that is monitored for refresh (defaults to the current object)  
     * 
     * From there, it is up to the loader class to know what it's supposed to do and where to 
     * pass in information.
     * 
     * @param application <font color="#0000FF"><b>(String)</b></font>
     * 					Application name/key that needs configuration information loaded. 
     * 
     */
	void loadApplicationConfig(String application, IDfSessionManager sMgr)
	{
		/*-INFO-*/Lg.inf("application: %s",application);
		IDfSession session = null;
		try { 
			/*-trc-*/Lg.trc("Get session");
			session = sMgr.getSession(docbase);
		} catch (DfException dfe) {
			/*-ERROR-*/Lg.err("Session acquisition error, probably due to bad login credentials or docbase name",dfe);
			throw EEx.create("CS-GetSessionErr","DfIdentityException, DfAuthenticationException, DfPrincipalException, or DfServiceException exception",dfe);			
		}
		
		String qual = null;
		try {
			// locate configuration service config object for this application  
			qual = CONFIG_SERVICE_SEARCH_QUALIFICATION+application+"'";
			/*-trc-*/Lg.trc("config obj qualification: %s",qual);
			IDfDocument serviceconfiguration = (IDfDocument)session.getObjectByQualification(qual);
			/*-trc-*/Lg.trc("config obj returned %s: ",serviceconfiguration);
			// read loaderclass
			String loaderclass = serviceconfiguration.getString("m_loader_classname");
			if (loaderclass == null || "".equals(loaderclass.trim())) loaderclass = CONFIG_SERVICE_DEFAULT_LOADER;
			/*-trc-*/Lg.trc("loaderclass: %s ",loaderclass);
			// read config file source
			String configfile = serviceconfiguration.getString("m_loader_config");
			if (configfile == null|| "".equals(configfile.trim())) configfile = serviceconfiguration.getObjectName();
			/*-trc-*/Lg.trc("configfile: %s ",configfile);
			String user = serviceconfiguration.getString("m_zzz_uid");
			String pass = serviceconfiguration.getString("m_zzz_pcred");
			/*-trc-*/Lg.trc("usr: %s ",user);
			// start monitor for this application
			if (applications.get(application) == null) {
				// read monitor info
				/*-trc-*/Lg.trc("initial load, reading monitor info");
				String monitorfile = serviceconfiguration.getString("m_monitor_docname");
				if (monitorfile == null || "".equals(monitorfile.trim())) monitorfile = serviceconfiguration.getObjectName();
				/*-trc-*/Lg.trc("configured monitor file: %s",monitorfile);
				/*-trc-*/Lg.trc("getting refresh interval");
				int monitorinterval = serviceconfiguration.getInt("m_monitor_interval");
				if (monitorinterval < 100000) monitorinterval = 100000; // at LEAST one hundred seconds...
				/*-trc-*/Lg.trc("configured monitor interval (unless less than 100000/100 seconds): %d",monitorinterval);
				/*-INFO-*/Lg.inf("scheduling new monitor timer task monitoring file %s for application %s with interval %d",monitorfile, application, monitorinterval);
				configmonitor.schedule(new ConfigMonitorTask(sMgr,user,pass,docbase,monitorfile,application), monitorinterval, monitorinterval);
				/*-dbg-*/Lg.dbg("returned from scheduling call");
			}
			
			/*-trc-*/Lg.trc("check for POJO/default loaderclass");
			if (!CONFIG_SERVICE_DEFAULT_LOADER.equals(loaderclass)) {
				// perform SBO instantiation
				/*-trc-*/Lg.trc("begin SBO instantiation of custom loader class - get DfClient");
				IDfClient dctmclient = session.getClient();
				/*-trc-*/Lg.trc("instantiate SBO service");
				IMdtConfigLoader loaderservice = (IMdtConfigLoader)dctmclient.newService(loaderclass,sMgr);
				/*-trc-*/Lg.trc("invoke service");
				Map configmap = loaderservice.loadConfig(docbase, application, configfile);
				/*-trc-*/Lg.trc("merge with applications cache");
				applications.putAll(configmap);
			} else {
				try {
					/*-trc-*/Lg.trc("default loader, use POJO method");
					MdtCfgLdrXStream loader = (MdtCfgLdrXStream)Class.forName(loaderclass).newInstance();
					/*-trc-*/Lg.trc("EXEC loader");
					Map configmap = loader.loadConfigNonSBO(sMgr, docbase, application, configfile);
					/*-trc-*/Lg.trc("merge");
					applications.putAll(configmap);
				} catch (Exception e) {
					/*-ERROR-*/Lg.err("Instantiation exception of default config service config loader - core jar is probably not properly associated with Config Service SBO",e);
					throw EEx.create("CS-DfltLdrCNFE","Instantiation exception of default config service config loader - core jar is probably not properly associated with Config Service SBO",e);
				}
			}						
		} catch (DfException dfe) {
			/*-ERROR-*/Lg.err("Unable to retrieve ConfigService configuration using qualification %s",qual);
			throw EEx.create("CS-ConfigObjectErr","Error retrieving ConfigService docbase config object",dfe);
		} finally {
			/*-trc-*/Lg.trc("release session in finally clause...");
			try {if (session != null)sMgr.release(session);} catch (Exception ee) {Lg.wrn("session could not be released in catch");}
		}
	}
	
	
    /**
     * Refresh monitor tasks initiated once a configuration is requested to monitor for configuration
     * updates. Make sure the mdt_configservice object that corresponds to the requested application
     * key has been configured for the monitor file and sleep period.
     *
     *  TODO: probably should rewrite as a single thread that checks all the tracker docs, 
     *  rather than multiple timers.
     */
    class ConfigMonitorTask extends TimerTask
    {    	
    	String docbase = null;
    	String trackerdocumentname = null;
    	String user = null;
    	String pass = null;
        IDfTime lastModified = null;
        String application = null;
        IDfClientX clientx = null;
        IDfClient client = null;

        /**
         * TODO: ADD DESCRIPTION
         *  
         * @param smgr <font color="#0000FF"><b>(IDfSessionManager)</b></font> TODO: 
         * @param sysuser <font color="#0000FF"><b>(String)</b></font> TODO:
         * @param syspass <font color="#0000FF"><b>(String)</b></font> TODO:
         * @param dbase <font color="#0000FF"><b>(String)</b></font> TODO:
         * @param trackerdoc <font color="#0000FF"><b>(String)</b></font> TODO:
         * @param app <font color="#0000FF"><b>(String)</b></font> TODO:
         * 
         */
        public ConfigMonitorTask(IDfSessionManager smgr, String sysuser, String syspass, String dbase, String trackerdoc, String app)
        {
            /*-INFO-*/Lg.inf("Instantiating new ConfigMonitorTask for docbase %s trackerdoc %s application %s",dbase,trackerdoc,app);
        	docbase = dbase;
        	user = sysuser;
        	pass = syspass;
        	trackerdocumentname = trackerdoc;
        	application = app;
            /*-dbg-*/if(Lg.dbg())Lg.dbg("instantiate clientx");
    		clientx = new DfClientX();
        	try { 
                /*-dbg-*/if(Lg.dbg())Lg.dbg("get local client");
        		client = clientx.getLocalClient();
        	} catch (DfException dfe) {
        		throw EEx.create("ConfigMonitorInstantiate", "ConfigMonitorTask was unable to create localclient",dfe);
        	}
        }
        
        /**
         * TODO: ADD DESCRIPTION
         * 
         */
        public void run()
        {
            /*-INFO-*/Lg.inf("run() invoked for app %s, configfile %s",application,trackerdocumentname);
            try {
                /*-dbg-*/if(Lg.dbg())Lg.dbg("start next scan");
                /*-dbg-*/if(Lg.dbg())Lg.dbg("create new session manager");
                IDfSessionManager configmonitormanager = client.newSessionManager();
                /*-dbg-*/if(Lg.dbg())Lg.dbg("create login info object");
                IDfLoginInfo newlogininfo = clientx.getLoginInfo();
                /*-dbg-*/if(Lg.dbg())Lg.dbg("set user/pass/domain");
                newlogininfo.setUser(user);
                newlogininfo.setPassword(pass);
                newlogininfo.setDomain(null);
                /*-dbg-*/if(Lg.dbg())Lg.dbg("set session manager to newly created logininfo identity");
                configmonitormanager.setIdentity(docbase, newlogininfo);                
                // poll DCTM intermittently to see if config has changed...
                IDfSession session = null;
                try {
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("get session");
                	session = configmonitormanager.getSession(docbase);                        
                    String modifiedtrackerfilename = trackerdocumentname;
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("checking "+modifiedtrackerfilename);
                    IDfSysObject sysObj = (IDfSysObject)session.getObjectByQualification("dm_sysobject WHERE object_name = '"+trackerdocumentname+"'");
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("found? "+(sysObj!=null));
                    if (lastModified == null) lastModified = sysObj.getModifyDate(); // lazy init...
                    boolean b = (sysObj.getModifyDate().compareTo(lastModified) > 0);
                    /*-dbg-*/if(Lg.dbg())Lg.dbg("newer? "+b);
                    if (b)
                    {
                        synchronized (applications)
                        {
                            try {
                                /*-dbg-*/if(Lg.dbg())Lg.dbg("get new lastmodified date...");
                                lastModified = sysObj.getModifyDate();
                                /*-dbg-*/if(Lg.dbg())Lg.dbg("triggering reload!");
                    			loadApplicationConfig(application,configmonitormanager);                    			
                                /*-dbg-*/if(Lg.dbg())Lg.dbg("reload completed");
                            }catch (Exception e) {
                                /*-WARN-*/Lg.wrn("config reload failed",e);
                            }
                        }
                    }
                } catch (Exception e) {
                    /*-ERROR-*/Lg.err("error in modified tracker scan for application %s monitor file %s",application,trackerdocumentname,e);
                } finally {
        			/*-dbg-*/Lg.dbg("release session in finally clause...");
        			try {if (session != null)configmonitormanager.release(session);} catch (Exception ee) {Lg.wrn("session could not be released in catch");}
                }
            } catch (Throwable t) {
                /*-WARN-*/Lg.wrn("exception in conig monitor thread for application %s monitor file %s",application,trackerdocumentname,t);
            }
        }
    }	
    
    /**
     * TODO: ADD DESCRIPTION
     *  
     * @param user <font color="#0000FF"><b>(String)</b></font> TODO: 
     * 
     * @throws DfException
     * 
     * @return <font color="#0000FF"><b>List</b></font> - TODO:
     */    	
    public List getMdtApplicationsFromGroupMembership(String user) throws DfException
    {
        /*-INFO-*/Lg.inf("get mdt apps from group - top");
        IDfSession session = null;
    	String application = null;
		IDfCollection queryresults = null;
    	List apps = new ArrayList();
        try {
			/*-dbg-*/Lg.dbg("get session");
        	session = sMgr.getSession(docbase);
			String dql = "SELECT r_object_id,group_name FROM dm_group WHERE group_name LIKE 'mdtapp_%'";
			/*-dbg-*/Lg.dbg("initializing query %s",dql);
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
			/*-dbg-*/Lg.dbg("exec");
			queryresults = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
			/*-dbg-*/Lg.dbg("iterate");
	        while (queryresults.next()) 
	        {
				/*-dbg-*/Lg.dbg("get next result");
	        	String grpname = queryresults.getString("group_name");
				/*-dbg-*/Lg.dbg("grpname: %s",grpname);
	        	if (session.getGroup(grpname).isUserInGroup(user))
	        	{
					/*-dbg-*/Lg.dbg("user is a member");
	        		String mdtapp = grpname.substring(grpname.indexOf("mdtapp_")+7);
					/*-dbg-*/Lg.dbg("adding application %s to applist",mdtapp);
	        		apps.add(mdtapp);
	        	}
	        }
        } finally {
        	try {queryresults.close();}catch(Exception e){}
        	try {sMgr.release(session);}catch(Exception e){}
        }
        /*-dbg-*/Lg.dbg("returning app list");	            	
        return apps;
    }
    
    /**
     * Removes user from all MdtApp groups, and then adds them to the specified app group,
     * Ensuring that the user has a unique application (we will use this on Login for QADoc)
     *  
     * @param user <font color="#0000FF"><b>(String)</b></font> TODO: 
     * @param application <font color="#0000FF"><b>(String)</b></font> TODO:
     * 
     * @throws DfException
     * 
     * @return <font color="#0000FF"><b>List</b></font> - TODO:
     */    	
    public void setUniqueMdtApplicationForUser(String user, String application) throws DfException
    {
        /*-INFO-*/Lg.inf("set unique mdtapp %s for user %s - top",application,user);
        IDfSession session = null;
		IDfCollection queryresults = null;
    	IDfSessionManager sMgrSU = null; 
        try {
        	
			/*-dbg-*/Lg.dbg("get session");
        	session = sMgr.getSession(docbase);
        	sMgrSU = session.getClient().newSessionManager();
        	
			/*-dbg-*/Lg.dbg("switch to SU");
        	IDfSysObject appcfg = (IDfSysObject)session.getObjectByQualification(CONFIG_SERVICE_SEARCH_QUALIFICATION+application+"'");
        	String suuser = appcfg.getString("m_zzz_uid");
        	String supass = appcfg.getString("m_zzz_pcred");
    		IDfLoginInfo logininfo = new DfLoginInfo();
        	logininfo.setUser(suuser);
        	logininfo.setPassword(supass);
        	sMgrSU.setIdentity(docbase,logininfo);
			/*-dbg-*/Lg.dbg("switch to SU completed");
        } finally {
        	try {sMgr.release(session);}catch(Exception e){}        	
        }
        
        try {        	
        	session = sMgrSU.getSession(docbase);
			String dql = "SELECT r_object_id,group_name FROM dm_group WHERE group_name LIKE 'mdtapp_%'";
			/*-dbg-*/Lg.dbg("initializing query %s",dql);
	        IDfQuery qry = new DfQuery();
	        qry.setDQL(dql);
			/*-dbg-*/Lg.dbg("exec");
			queryresults = (IDfCollection) qry.execute(session, IDfQuery.DF_READ_QUERY);
			/*-dbg-*/Lg.dbg("iterate");
	        while (queryresults.next()) 
	        {
				/*-dbg-*/Lg.dbg("get next result");
	        	String grpname = queryresults.getString("group_name");
				/*-dbg-*/Lg.dbg("grpname: %s",grpname);
				IDfGroup grp = session.getGroup(grpname);
        		String mdtapp = grpname.substring(grpname.indexOf("mdtapp_")+7);
				/*-dbg-*/Lg.dbg("application: %s ",mdtapp);
        		if (!mdtapp.equals(application))
        		{
    	        	if (grp.isUserInGroup(user))
    	        	{
    	        		/*-dbg-*/Lg.dbg("user is a member,removing");
    	        		boolean b = grp.removeUser(user);
    	        		/*-dbg-*/Lg.dbg("user %s removed from group %s, success: %b",user,grpname,b);
    	        		grp.save();
    	        	}
        		} else {
    	        	if (grp.isUserInGroup(user))
    	        	{
    	        		/*-dbg-*/Lg.dbg("user already a member of group %s",grpname);
    	        	} else {
    	        		/*-dbg-*/Lg.dbg("adding user/activating mdt application");
    	        		grp.addUser(user);
    	        		/*-dbg-*/Lg.dbg("user %s added to group %s",user,grpname);
    	        		grp.save();
    	        	}
        		}
	        }
        } finally {
        	try {queryresults.close();}catch(Exception e){}
        	try {sMgrSU.release(session);}catch(Exception e){}
        }
        /*-dbg-*/Lg.dbg("done");	            	
    }
    
    
}
