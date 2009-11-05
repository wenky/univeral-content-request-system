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
var DIF_dragging=false;var DIF_iframeBeingDragged="";var DIF_iframeObjects=new Object();var DIF_iframeWindows=new Object();var DIF_iframeMouseDownLeft = new Object();var DIF_iframeMouseDownTop = new Object();var DIF_pageMouseDownLeft = new Object();var DIF_pageMouseDownTop = new Object();var DIF_handles = new Object();var DIF_highestZIndex=99;var DIF_raiseSelectedIframe=false;var DIF_allowDragOffScreen=false;
function bringSelectedIframeToTop(val){DIF_raiseSelectedIframe = val;}
function allowDragOffScreen(val){DIF_allowDragOffScreen=val;}
function addHandle(o, win){if(arguments.length==2 && win==window){var p = win;while(p=p.parent){if(p.addHandle){p.addHandle(o,win,true);return;}if(p==win.top){return;}}return;}var topRef=win;var topRefStr = "window";while(topRef.parent && topRef.parent!=window){topRef = topRef.parent;topRefStr = topRefStr + ".parent";}if(typeof(win.DIF_mainHandlersAdded)=="undefined" || !win.DIF_mainHandlersAdded){with(win){eval("function OnMouseDownHandler(evt){if(typeof(evt)=='undefined'){evt=event;}"+topRefStr+".parent.DIF_begindrag(evt, "+topRefStr+")}");eval("document.onmousedown = OnMouseDownHandler;");eval("function OnMouseUpHandler(evt){if(typeof(evt)=='undefined'){evt=event;}"+topRefStr+".parent.DIF_enddrag(evt, "+topRefStr+")}");eval("document.onmouseup = OnMouseUpHandler;");eval("function OnMouseMoveHandler(evt){if(typeof(evt)=='undefined'){evt=event;}"+topRefStr+".parent.DIF_iframemove(evt, "+topRefStr+")}");eval("document.onmousemove = OnMouseMoveHandler;");win.DIF_handlersAdded = true;win.DIF_mainHandlersAdded = true;}}if(typeof(window.DIF_handlersAdded)!="undefined" || !window.DIF_handlersAdded){eval("function OnMouseMoveHandler(evt){if(typeof(evt)=='undefined'){evt=event;}DIF_mouseMove(evt, window)}");eval("document.onmousemove = OnMouseMoveHandler;");eval("function OnMouseUpHandler(evt){if(typeof(evt)=='undefined'){evt=event;}DIF_enddrag(evt, window)}");eval("document.onmouseup = OnMouseUpHandler;");window.DIF_handlersAdded=true;}o.style.cursor="move";var name = DIF_getIframeId(topRef);if(DIF_handles[name]==null){DIF_handles[name] = new Array();DIF_iframeMouseDownLeft[name] = 0;DIF_iframeMouseDownTop[name] = 0;DIF_pageMouseDownLeft[name] = 0;DIF_pageMouseDownTop[name] = 0;}DIF_handles[name][DIF_handles[name].length] = o;}
function DIF_getEventPosition(evt){var pos=new Object();pos.x=0;pos.y=0;if(!evt){evt = window.event;}if(typeof(evt.pageX) == 'number'){pos.x = evt.pageX;pos.y = evt.pageY;}else{pos.x = evt.clientX;pos.y = evt.clientY;if(!top.opera){if((!window.document.compatMode) ||(window.document.compatMode == 'BackCompat')){pos.x += window.document.body.scrollLeft;pos.y += window.document.body.scrollTop;}else{pos.x += window.document.documentElement.scrollLeft;pos.y += window.document.documentElement.scrollTop;}}}return pos;}
function DIF_getIframeId(win){var iframes = document.getElementsByTagName("IFRAME");for(var i=0;i<iframes.length;i++){var o = iframes.item(i);var w = null;if(o.contentWindow){w = o.contentWindow;}else if(window.frames && window.frames[o.id].window){w = window.frames[o.id];}if(w == win){DIF_iframeWindows[o.id] = win;DIF_iframeObjects[o.id] = o;return o.id;}}return null;}
function DIF_getObjectXY(o){var res = new Object();res.x=0;res.y=0;if(o != null){res.x = o.style.left.substring(0,o.style.left.indexOf("px"));res.y = o.style.top.substring(0,o.style.top.indexOf("px"));}return res;}
function getSrcElement(e){var tgt = e.target;while(tgt.nodeType != 1){tgt = tgt.parentNode;}return tgt;}
function isHandleClicked(handle, objectClicked){if(handle==objectClicked){return true;}while(objectClicked.parentNode != null){if(objectClicked==handle){return true;}objectClicked = objectClicked.parentNode;}return false;}
function DIF_begindrag(e, win){var iframename = DIF_getIframeId(win);if(iframename==null){return;}if(DIF_handles[iframename]==null || DIF_handles[iframename].length<1){return;}var isHandle = false;var t = e.srcElement || getSrcElement(e);for(var i=0;i<DIF_handles[iframename].length;i++){if(isHandleClicked(DIF_handles[iframename][i],t)){isHandle=true;break;}}if(!isHandle){return false;}DIF_iframeBeingDragged = iframename;if(DIF_raiseSelectedIframe){DIF_iframeObjects[DIF_iframeBeingDragged].style.zIndex=DIF_highestZIndex++;}DIF_dragging=true;var pos=DIF_getEventPosition(e);DIF_iframeMouseDownLeft[DIF_iframeBeingDragged] = pos.x;DIF_iframeMouseDownTop[DIF_iframeBeingDragged] = pos.y;var o = DIF_getObjectXY(DIF_iframeObjects[DIF_iframeBeingDragged]);DIF_pageMouseDownLeft[DIF_iframeBeingDragged] = o.x - 0 + pos.x;DIF_pageMouseDownTop[DIF_iframeBeingDragged] = o.y -0 + pos.y;}
function DIF_enddrag(e){DIF_dragging=false;DIF_iframeBeingDragged="";}
function DIF_mouseMove(e){if(DIF_dragging){var pos = DIF_getEventPosition(e);DIF_drag(pos.x - DIF_pageMouseDownLeft[DIF_iframeBeingDragged] , pos.y - DIF_pageMouseDownTop[DIF_iframeBeingDragged]);}}
function DIF_iframemove(e){if(DIF_dragging){var pos = DIF_getEventPosition(e);DIF_drag(pos.x - DIF_iframeMouseDownLeft[DIF_iframeBeingDragged] , pos.y - DIF_iframeMouseDownTop[DIF_iframeBeingDragged]);}}
function DIF_drag(x,y){var o = DIF_getObjectXY(DIF_iframeObjects[DIF_iframeBeingDragged]);var newPositionX = o.x-0+x;var newPositionY = o.y-0+y;if(!DIF_allowDragOffScreen){if(newPositionX < 0){newPositionX=0;}if(newPositionY < 0){newPositionY=0;}}DIF_iframeObjects[DIF_iframeBeingDragged].style.left = newPositionX + "px";DIF_iframeObjects[DIF_iframeBeingDragged].style.top  = newPositionY + "px";DIF_pageMouseDownLeft[DIF_iframeBeingDragged] += x;DIF_pageMouseDownTop[DIF_iframeBeingDragged] += y;}

