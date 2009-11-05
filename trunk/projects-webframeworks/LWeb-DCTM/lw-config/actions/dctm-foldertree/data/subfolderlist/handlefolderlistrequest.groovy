import javax.servlet.http.HttpServletRequest;

import com.cem.contextmap.ContextMap;
import com.cem.dctm.DctmUtils;
import com.cem.lweb.core.ThreadData;
import com.cem.lweb.core.util.ResultSetList;
import com.documentum.com.DfClientX;
import com.documentum.com.IDfClientX;
import com.documentum.fc.client.IDfClient;
import com.documentum.fc.client.IDfSession;
import com.documentum.fc.client.IDfSessionManager;
import com.documentum.fc.common.IDfLoginInfo;


  try { 
      HttpServletRequest request = ThreadData.getHttpRequest();
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
                           
      String parentfolderid = request.getParameter("parentfolderid");

      IDfSession dctmsession = null;
      try { 
          dctmsession = sMgr.getSession("mqadoc_test");
          if("getcabinetlist".equals(parentfolderid))
          {
                String dql = "SELECT 1,upper(object_name),r_object_id,object_name,r_object_type,r_lock_owner,owner_name,"+
                                   "r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,r_assembled_from_id,"+
                                   "r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id,title,r_modify_date,thumbnail_url,a_is_hidden,'1' as isfolder "+
                               "FROM dm_cabinet order by 1,2";
                ResultSetList results = (ResultSetList)DctmUtils.execMultiColumnQuery(dctmsession,dql);
                request.setAttribute("results", results);
            }else{
                String dql = "SELECT 1,upper(object_name),r_object_id,object_name,r_object_type,r_lock_owner,owner_name,"+
                                  "r_link_cnt,r_is_virtual_doc,r_content_size,a_content_type,i_is_reference,r_assembled_from_id,"+
                                  "r_has_frzn_assembly,a_compound_architecture,i_is_replica,r_policy_id,title,r_modify_date,thumbnail_url,a_is_hidden,'1' as isfolder "+
                               "FROM dm_folder WHERE any i_folder_id='"+parentfolderid+"' order by 1,2";
                  ResultSetList results = (ResultSetList)DctmUtils.execMultiColumnQuery(dctmsession,dql);
                request.setAttribute("results", results);
            }
      } finally { try {sMgr.release(dctmsession);} catch (Exception e) {} }
  } catch (Exception e) {
  
  } 
