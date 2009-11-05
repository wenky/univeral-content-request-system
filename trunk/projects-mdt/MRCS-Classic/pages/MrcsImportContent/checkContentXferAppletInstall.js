function checkContentXferAppletInstall(strVersion, bFullInstall, bIsPlugInBeingUsed)
{
var bCorrectAppletInstalled = false;
var strCookies = document.cookie;
if ( bIsPlugInBeingUsed == true )
{
bCorrectAppletInstalled = isLevelCorrect(strCookies, bFullInstall);
}
else
{
strCurrentVersion = getCurrentVersionInIE();
if ( strCurrentVersion == "undefined" )
{
bCorrectAppletInstalled = false;
}
else
{
// we have the current version number, let's make sure its up to date
if ( compareVersionStrings(strCurrentVersion, strVersion, ",") < 0 )
{
bCorrectAppletInstalled = false;
}
else
{
bCorrectAppletInstalled = isLevelCorrect(strCookies, bFullInstall);
}
}
}
return bCorrectAppletInstalled;
}
function checkContentXferAppletInstall(strVersion, bFullInstall)
{
var bIsIE = (document.all) ? true : false;
var bCorrectAppletInstalled = false;
var strCookies = document.cookie;
if ( bIsIE == false )
{
bCorrectAppletInstalled = isLevelCorrect(strCookies, bFullInstall);
}
else
{
var bIsMsVm = isMicrosoftVm();
if (bIsMsVm == false)
{
bCorrectAppletInstalled = isLevelCorrect(strCookies, bFullInstall);
}
else
{
strCurrentVersion = getCurrentVersionInIE();
if ( strCurrentVersion == "undefined" )
{
bCorrectAppletInstalled = false;
}
else
{
// we have the current version number, let's make sure its up to date
if ( compareVersionStrings(strCurrentVersion, strVersion, ",") < 0 )
{
bCorrectAppletInstalled = false;
}
else
{
bCorrectAppletInstalled = isLevelCorrect(strCookies, bFullInstall);
}
}
}
}
return bCorrectAppletInstalled;
}
function checkLightInstall(successMsg, failureMsg)
{
var bIsIE = (document.all) ? true : false;
var isLightInstalled = "true";
if (bIsIE == true)
{
isLightInstalled = "false";
var checkInstallApplet = document.getElementById("checkInstallApplet");
if (checkInstallApplet != null)
{
isLightInstalled = checkInstallApplet.getIsLightInstalled();
}
}
if (isLightInstalled == "true")
{
alert(successMsg);
}
// we're not showing failure msg - bug 68997
document.cookie = "installContentXferSuccess=" + isLightInstalled + ";path=/";
}
function checkFullInstall(successMsg, failureMsg)
{
var bIsIE = (document.all) ? true : false;
var isFullInstalled = "true";
if (bIsIE == true)
{
isFullInstalled = "false";
var checkInstallApplet = document.getElementById("checkInstallApplet");
if (checkInstallApplet != null)
{
isFullInstalled = checkInstallApplet.getIsFullInstalled();
}
}
if (isFullInstalled == "true")
{
alert(successMsg);
}
// we're not showing failure msg - bug 68997
document.cookie = "installContentXferSuccess=" + isFullInstalled + ";path=/";
}
function isLevelCorrect(strCookies, bFullInstall)
{
var bLevelCorrect = false;
// if a full install is not required it doesn't matter whether the cookie is present
if ( bFullInstall == true )
{
if ( (strCookies != null) && (strCookies.indexOf("fullInstall=true") != -1) )
{
bLevelCorrect = true;
}
}
else
{
bLevelCorrect = true;
}
return bLevelCorrect;
}
function getCurrentVersionInIE()
{
var strVersion = "undefined";
var showVersionApplet = document.getElementById("showVersion");
if ( showVersionApplet != null )
{
var version = showVersionApplet.getVersion();
if (version != null && version != "")
{
strVersion = version;
}
}
return strVersion;
}
function isMicrosoftVm()
{
var bMsVm = false;
var vmDetectApplet = document.getElementById("vmDetectApplet");
if (vmDetectApplet != null)
{
try
{
var strIsMsVm = vmDetectApplet.getIsMicrosoftVm();
bMsVm = (strIsMsVm == "true");
document.cookie = "isMicrosoftVm=" + bMsVm + ";path=/";
}
catch (ex)
{
bMsVm = false;
postServerEvent(null, null, null, "onNoVMInstalled");
}
}
return bMsVm;
}
function compareVersionStrings(ver1, ver2, delim)
{
if (ver1 != null && ver2 != null && ver1.length > 0 && ver2.length > 0)
{
var arr1 = ver1.split(delim);
var arr2 = ver2.split(delim);
var i=0;
for (; ((i < arr1.length) && (i < arr2.length)); i++)
{
var n1 = parseInt(arr1[i]);
var n2 = parseInt(arr2[i]);
if (n1 != n2)
{
return (n1 > n2) ? 1 : -1;
}
}
// if we're still here, the two arrays are equal
// but if one is longer, then it's greater if has non-zero remaining components
if (arr1.length != arr2.length)
{
var arr = (arr1.length > arr2.length) ? arr1 : arr2;
for (; i < arr.length; i++)
{
if (parseInt(arr[i]) > 0)
{
return 1;
}
}
return 0;
}
return 0;
}
else
{
return isNaN(parseInt(ver1 > ver2)) ? 0 : parseInt(ver1 > ver2);
}
}
