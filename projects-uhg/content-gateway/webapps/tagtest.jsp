<html>
<head>
	<title>Your first JSP tag : FirstTag</title>
	<style>
	p, b { font-family:Tahoma,Sans-Serif; font-size:10pt; }
	b { font-weight:bold; }
	</style>
</head>
<body>

<p align="center">
<em><u>Your first JSP tag : FirstTag</u></em></p>

<%@ taglib uri="/WEB-INF/tlds/ContentRetrievalTag.tld" prefix="cms" %>
<!-- p>FTP test : cms:content item="HEADER.html" source="CEM-FTP" </p -->
<p>SVN+fs-img test : <cms:content item="/html/cms-svn-img-test.html" /></p>

</body>
</html>