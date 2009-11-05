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
var Position =(function(){function resolveObject(s){if(document.getElementById && document.getElementById(s)!=null){return document.getElementById(s);}else if(document.all && document.all[s]!=null){return document.all[s];}else if(document.anchors && document.anchors.length && document.anchors.length>0 && document.anchors[0].x){for(var i=0;i<document.anchors.length;i++){if(document.anchors[i].name==s){return document.anchors[i]}}}}var pos ={};pos.$VERSION = 1.0;
pos.set = function(o,left,top){if(typeof(o)=="string"){o = resolveObject(o);}if(o==null || !o.style){return false;}if(typeof(left)=="object"){var pos = left;left = pos.left;top = pos.top;}o.style.left = left + "px";o.style.top = top + "px";return true;};
pos.get = function(o){var fixBrowserQuirks = true;if(typeof(o)=="string"){o = resolveObject(o);}if(o==null){return null;}var left = 0;var top = 0;var width = 0;var height = 0;var parentNode = null;var offsetParent = null;offsetParent = o.offsetParent;var originalObject = o;var el = o;while(el.parentNode!=null){el = el.parentNode;if(el.offsetParent==null){}else{var considerScroll = true;if(fixBrowserQuirks && window.opera){if(el==originalObject.parentNode || el.nodeName=="TR"){considerScroll = false;}}if(considerScroll){if(el.scrollTop && el.scrollTop>0){top -= el.scrollTop;}if(el.scrollLeft && el.scrollLeft>0){left -= el.scrollLeft;}}}if(el == offsetParent){left += o.offsetLeft;if(el.clientLeft && el.nodeName!="TABLE"){left += el.clientLeft;}top += o.offsetTop;if(el.clientTop && el.nodeName!="TABLE"){top += el.clientTop;}o = el;if(o.offsetParent==null){if(o.offsetLeft){left += o.offsetLeft;}if(o.offsetTop){top += o.offsetTop;}}offsetParent = o.offsetParent;}}if(originalObject.offsetWidth){width = originalObject.offsetWidth;}if(originalObject.offsetHeight){height = originalObject.offsetHeight;}return{'left':left, 'top':top, 'width':width, 'height':height};};
pos.getCenter = function(o){var c = this.get(o);if(c==null){return null;}c.left = c.left +(c.width/2);c.top = c.top +(c.height/2);return c;};return pos;})();

