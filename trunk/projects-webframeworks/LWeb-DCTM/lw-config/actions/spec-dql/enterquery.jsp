<% String path = request.getContextPath(); %>

<html>
<head>
    <script type="text/javascript" src="<%=path%>/js/jquery-1.2.1-regex.js"></script>
    <script type="text/javascript" src="<%=path%>/js/ext-2.2/adapter/jquery/jquery-plugins.js"></script>
    <script type="text/javascript" src="<%=path%>/js/ext-2.2/adapter/jquery/ext-jquery-adapter.js"></script>
    <script type="text/javascript" src="<%=path%>/js/ext-2.2/ext-all.js"></script>
    <link rel="stylesheet" type="text/css" href="<%=path%>/js/ext-2.2/resources/css/ext-all.css">
    <script type="text/javascript" src="<%=path%>/js/json4jquery.js"></script> <!--  !pass dates as strings from server! -->
    <script type="text/javascript" src="<%=path%>/js/sessvars.js"></script> <!--  !pass dates as strings from server! -->
 
    <script type="text/javascript">    
      // ready processors for jquery + ext
      var jqueryready = false;
      var extready = false;      
      $(document).ready(function() {jqueryready = true;if (jqueryready && extready) readyToGo();});        
      Ext.onReady(function() {extready = true;if (jqueryready && extready) readyToGo();});
      function readyToGo() {
        
        // get recent query list from window.var
        sessvars.querycache = sessvars.querycache||[]; // fancy "retrieve with default" init
        for (var i=0; i < sessvars.querycache.length; i++) {
          $("#querycache").appendTo("<a href='[actions][spec-execdql]?query="+Ext.urlEncode(sessvars.querycache[i])+"'>"+sessvars.querycache[i]+"</a><br>");
        }
      }
    </script>
 
</head>
<body>
  <hr>
  username: <%=request.getAttribute("zzzu")%> docbase: <%=request.getAttribute("zzzb")%> 
  <hr>
  <div id="querycache">
  </div>
  <FORM name='DumpDoc' action='[actions][spec-execdql]' method='GET'> 
    <TEXTAREA name='query' cols=100 rows=10></TEXTAREA>
    <INPUT type='submit' name='go' value='go'/>
  </FORM>
  
</html>