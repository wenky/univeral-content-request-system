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
 %>
<html>

    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <!--  jquery/ext include -->
    <script type="text/javascript" src="<%=path%>/js/jquery-1.2.1-regex.js"></script>
    <script type="text/javascript" src="<%=path%>/js/ext-2.2/adapter/jquery/jquery-plugins.js"></script>
    <script type="text/javascript" src="<%=path%>/js/ext-2.2/adapter/jquery/ext-jquery-adapter.js"></script>
    <script type="text/javascript" src="<%=path%>/js/ext-2.2/ext-all.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=path%>/js/ext-2.2/resources/css/ext-all.css">
    <script type="text/javascript" src="<%=path%>/js/json4jquery.js"></script> <!--  !pass dates as strings from server! -->

    <script type="text/javascript">    
      // ready processors for jquery + ext
      var jqueryready = false; var extready = false; var readytogo = false;
      var divinitcalls = [];
      
      $(document).ready(function() {jqueryready = true;if (jqueryready && extready) readyToGo();});        
      Ext.onReady(function() {extready = true;if (jqueryready && extready) readyToGo();});
      
      function readyToGo() {
        readytogo = true;        
        for (var initcall=0; initcall < divinitcalls.length; initcall++) {          
          divinitcalls[initcall].initfunction(divinitcalls[initcall].initdata);
        }         
      }
      
      function divInit(divname,divinitfunction,initfunctiondata)
      {
        if (readytogo) {divinitfunction(initfunctiondata);} 
        else {divinitcalls[divinitcalls.length] = { initdiv:divname,initfunction:divinitfunction,initdata:initfunctiondata };}
      }
      
      // function callSubAction
      // function callWithFrame(indicates call with a frame, either default or specific)
      // function callAjax (indicates to server that no frame is needed)
      // function callDiv (separate <script> from rest of div body, evals the parsed/extracted js
      
    </script>
    
    <body>
        <%=request.getAttribute("actionresponse")%>
    </body>
    
</html>
    