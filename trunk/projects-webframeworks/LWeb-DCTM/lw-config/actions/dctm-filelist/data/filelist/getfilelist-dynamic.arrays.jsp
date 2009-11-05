<%@ page import="com.documentum.com.*"%>
<%@ page import="com.documentum.fc.client.*"%>
<%@ page import="com.documentum.fc.common.*"%>
<%@ page import="com.cem.dctm.DctmUtils"%>
<%@ page import="java.util.*"%>
<%@ page import="com.cem.lweb.core.util.*"%>

<%
    // implicits: 
    // HttpServletRequest request;
    // HttpServletResponse response;
    // HttpSession Session;
    // JspWriter Out;
    // PageContext PageContext;
    // ServletContext Application;
    // ServletConfig Config;  
    String querystring = com.cem.lweb.jsp.Util.getQueryString(request);
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";
    
    //anyway...demo-riffic! get DCTM session:
    IDfClientX clientx = new DfClientX();
    IDfClient client = clientx.getLocalClient();
    IDfSessionManager sMgr = client.newSessionManager();
    IDfLoginInfo loginInfoObj = clientx.getLoginInfo();
    loginInfoObj.setUser("ecsadmin");
    loginInfoObj.setPassword("spring2005");
    loginInfoObj.setDomain(null);
    sMgr.setIdentity("mqadoc_test", loginInfoObj);
                         
    String folderspec = request.getParameter("folderspec");

    IDfSession dctmsession = null;
    try { 
        dctmsession = sMgr.getSession("mqadoc_test");  
        IDfFolder folder = dctmsession.getFolderBySpecification(folderspec);
        String parentfolderid = folder.getObjectId().getId();
    
%>

    <%
    {
      String dql =   "SELECT 1 as topsort,upper(object_name) as namesort,r_object_id,object_name,r_object_type,r_lock_owner,owner_name,"+
                         "r_content_size,a_content_type,r_policy_id,title,r_modify_date,thumbnail_url,a_is_hidden,'1' as isfolder "+
                   //"FROM dm_folder WHERE a_is_hidden=false and any i_folder_id='"+folderid+"'"+
                     "FROM dm_folder WHERE any i_folder_id='"+parentfolderid+"' "+
                     "UNION "+
                     "SELECT 2 as topsort,upper(object_name) as namesort,r_object_id,object_name,r_object_type,r_lock_owner,owner_name,"+
                         "r_content_size,a_content_type,r_policy_id,title,r_modify_date,thumbnail_url,a_is_hidden,'0' as isfolder " +
                   //"FROM dm_document where a_is_hidden=false and any i_folder_id='"+folderid+"' order by 1,2";
                     "FROM dm_document where any i_folder_id='"+parentfolderid+"' order by 1,2";
        ResultSetList results = (ResultSetList)DctmUtils.execMultiColumnQuery(dctmsession,dql);
        %>
        [
        <%for (int i =0; i < results.size(); i++) {%>
          <% if(i!=0){%>,<%}%>
          [<% Object[] rowdata = (Object[])results.get(i); for (int j=0; j < rowdata.length; j++){%><%if(j!=0){%>,<%}%>"<%=rowdata[j].toString()%>"<%}%>]   
        <%}%>
        ]
  <%}%>

<%
    } finally { try {sMgr.release(dctmsession);} catch (Exception e) {} }
%>