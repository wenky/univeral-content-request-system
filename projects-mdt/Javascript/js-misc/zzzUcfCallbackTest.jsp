
<%@ page language="java" import="java.util.*" pageEncoding="UTF-8"%>
<%@ page import="com.medtronic.documentum.ajax.*"%>
<%
String path = request.getContextPath();
String basePath = request.getScheme()+"://"+request.getServerName()+":"+request.getServerPort()+path+"/";

UcfCallbackTest uct = new UcfCallbackTest(request,response);

uct.getClass();

%>

<html><body>hello zzzucfcallbacktest</body></html>
