<%--
  Simple basic frame, useful for single-div actions like:
  - login
  - object properties
  
  This frame assumes:
  - is provided the action name that renders its content in ?ActionContext?
--%>
<%
    // implicits: 
    // HttpServletRequest request;
    // HttpServletResponse response;
    // HttpSession Session;
    // JspWriter Out;
    // PageContext PageContext;
    // ServletContext Application;
    // ServletConfig Config;  
    String querystring = lweb.jsp.Util.getQueryString(request);
    String path = request.getContextPath();
    String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";    
 %>
<html>

    <meta http-equiv="Content-Type" content="text/html; charset=iso-8859-1">
    <!--  jquery/ext include -->
    <script type="text/javascript" src="/lweb/js/jquery-1.2.1-regex.js"></script>
    <script type="text/javascript" src="/lweb/js/ext-2.2/adapter/jquery/jquery-plugins.js"></script>
    <script type="text/javascript" src="/lweb/js/ext-2.2/adapter/jquery/ext-jquery-adapter.js"></script>
    <script type="text/javascript" src="/lweb/js/ext-2.2/ext-all.js"></script>
    <link rel="stylesheet" type="text/css" href="/lweb/js/ext-2.2/resources/css/ext-all.css">
    <script type="text/javascript" src="/lweb/js/json4jquery.js"></script> <!--  !pass dates as strings from server! -->

    <script type="text/javascript">    
      // ready processors for jquery + ext
      var globals = {}; // in case evals inside of functions don't globally register the f(x) defs
      var jqueryready = false;
      var extready = false;      
      $(document).ready(function() {jqueryready = true;if (jqueryready && extready) readyToGo();});        
      Ext.onReady(function() {extready = true;if (jqueryready && extready) readyToGo();});
      function readyToGo() {
        divexec('main-div');
      }
    </script>
    
    <body>
      <div id="header"><hr color='#cad8ea'><table border=0><tr><td><img src="/lweb/lw-img/logo.gif"></td><td><img src="/lweb/lw-img/cacfp.gif"></td></tr></table><hr color='#cad8ea'></div>
      <div id="main-div"></div>
      <div id="footer"><hr color='#cad8ea'><h3>...footer...</h3><hr color='#cad8ea'></div>
      <div id="hiddenscratch" style="display:none;">
        <!-- MUST INCLUDE A SCRIPT TAG THAT DEFINES function readyToGo() which initializes/renders the page -->
        <%=request.getAttribute("actionresponse")%>
      </div> 
    </body>
    
</html>
    