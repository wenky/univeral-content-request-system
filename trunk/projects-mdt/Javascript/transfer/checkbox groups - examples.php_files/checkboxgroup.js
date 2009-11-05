/*===================================================================
 Author: Matt Kruse
 
 View documentation, examples, and source code at:
     http://www.JavascriptToolbox.com/

 NOTICE: You may use this code for any purpose, commercial or
 private, without any further permission from the author. You may
 remove this notice from your final code if you wish, however it is
 appreciated by the author if at least the web site address is kept.

 This code may NOT be distributed for download from script sites, 
 open source CDs or sites, or any other distribution method. If you
 wish you share this code with others, please direct them to the 
 web site above.
 
 Pleae do not link directly to the .js files on the server above. Copy
 the files to your own server for use with your site or webapp.
 ===================================================================*/
function CheckBoxGroup(){this.controlBox=null;this.controlBoxChecked=null;this.maxAllowed=null;this.maxAllowedMessage=null;this.masterBehavior="all";this.formRef=null;this.checkboxWildcardNames=new Array();this.checkboxNames=new Array();this.totalBoxes=0;this.totalSelected=0;this.setControlBox=CBG_setControlBox;this.setMaxAllowed=CBG_setMaxAllowed;this.setMasterBehavior=CBG_setMasterBehavior;this.addToGroup=CBG_addToGroup;this.expandWildcards=CBG_expandWildcards;this.addWildcardCheckboxes=CBG_addWildcardCheckboxes;this.addArrayCheckboxes=CBG_addArrayCheckboxes;this.addSingleCheckbox=CBG_addSingleCheckbox;this.check=CBG_check;}CheckBoxGroup.$VERSION = 1.01;
function CBG_setControlBox(name){this.controlBox=name;}
function CBG_setMaxAllowed(num,msg){this.maxAllowed=num;if(msg!=null&&msg!=""){this.maxAllowedMessage=msg;}}
function CBG_setMasterBehavior(b){this.masterBehavior = b.toLowerCase();}
function CBG_addToGroup(){if(arguments.length>0){for(var i=0;i<arguments.length;i++){this.checkboxWildcardNames[this.checkboxWildcardNames.length]=arguments[i];}}}
function CBG_expandWildcards(){if(this.formRef==null){alert("ERROR: No form element has been passed.  Cannot extract form name!");return false;}for(var i=0;i<this.checkboxWildcardNames.length;i++){var n = this.checkboxWildcardNames[i];var el = this.formRef[n];if(n.indexOf("*")!=-1){this.addWildcardCheckboxes(n);}else if(CBG_nameIsArray(el)){this.addArrayCheckboxes(n);}else{this.addSingleCheckbox(el);}}}
function CBG_addWildcardCheckboxes(name){var i=name.indexOf("*");if((i==0) ||(i==name.length-1)){var searchString=(i)?name.substring(0,name.length-1):name.substring(1,name.length);var els = this.formRef.elements;var l = els.length;for(var j=0;j<l;j++){var currentElement = els[j];if(currentElement.type && currentElement.type=="checkbox" && currentElement.name){var currentElementName=currentElement.name;var partialName =(i)?currentElementName.substring(0,searchString.length) : currentElementName.substring(currentElementName.length-searchString.length,currentElementName.length);if(partialName==searchString){if(CBG_nameIsArray(currentElement)) this.addArrayCheckboxes(currentElement);else this.addSingleCheckbox(currentElement);}}}}}
function CBG_addArrayCheckboxes(name){if((CBG_nameIsArray(this.formRef[name])) &&(this.formRef[name].length>0)){for(var i=0;i<this.formRef[name].length;i++){this.addSingleCheckbox(this.formRef[name][i]);}}}
function CBG_addSingleCheckbox(obj){if(obj != this.formRef[this.controlBox]){this.checkboxNames[this.checkboxNames.length]=obj;this.totalBoxes++;if(obj.checked){this.totalSelected++;}}}
function CBG_check(obj){var checked=obj.checked;if(this.formRef==null){this.formRef=obj.form;this.expandWildcards();if(this.controlBox==null || obj.name!=this.controlBox){this.totalSelected +=(checked)?-1:1;}}if(this.controlBox!=null&&obj.name==this.controlBox){if(this.masterBehavior=="all"){for(i=0;i<this.checkboxNames.length;i++){this.checkboxNames[i].checked=checked;}this.totalSelected=(checked)?this.checkboxNames.length:0;}else{if(!checked){obj.checked =(this.totalSelected>0)?true:false;obj.blur();}}}else{if(this.masterBehavior=="all" && this.controlBox!=null){if(!checked){this.formRef[this.controlBox].checked=false;this.totalSelected--;}else{this.totalSelected++;}if(this.controlBox!=null){this.formRef[this.controlBox].checked=(this.totalSelected==this.totalBoxes)?true:false;}}else{if(!obj.checked){this.totalSelected--;}else{this.totalSelected++;}if(this.controlBox!=null){this.formRef[this.controlBox].checked=(this.totalSelected>0)?true:false;}if(this.maxAllowed!=null){if(this.totalSelected>this.maxAllowed){obj.checked=false;this.totalSelected--;if(this.maxAllowedMessage!=null){alert(this.maxAllowedMessage);}return false;}}}}}
function CBG_nameIsArray(obj){return((typeof obj.type!="string")&&(obj.length>0)&&(obj[0]!=null)&&(obj[0].type=="checkbox"));}

