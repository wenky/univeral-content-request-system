/*
 * Superfish - jQuery menu widget
 *
 * Copyright (c) 2007 Joel Birch
 *
 * Dual licensed under the MIT and GPL licenses:
 * 	http://www.opensource.org/licenses/mit-license.php
 * 	http://www.gnu.org/licenses/gpl.html
 *
 */

(function($){
	$.fn.superfish = function(o){
		var defaults = {
			hoverClass	: "sfHover",
			delay		: 500,
			animation	: {opacity:"show"},
			speed		: "normal"
		},
			over = function(){
				var $$ = $(this);
				clearTimeout(this.sfTimer);
				if (!$$.is("."+o.hoverClass)) {
					$$.addClass(o.hoverClass)
						.find("ul")
							.animate(o.animation,o.speed)
							.end()
						.siblings()
						.removeClass(o.hoverClass);
				}
			},
			out = function(){
				var $$ = $(this);
				this.sfTimer=setTimeout(function(){
					$$.removeClass(o.hoverClass)
					.find("iframe", this)
						.remove();
				},o.delay);
			};
		o = $.extend(defaults, o || {});
		var sfHovAr=$("li",this)
			.hover(over,out)
			.find("a").each(function() {
				var $a = $(this), $li = $a.parents("li");
				$a.focus(function(){ $li.each(over); })
				  .blur(function(){ $li.each(out); });
			}).end();
		$(window).unload(function() {
			sfHovAr.unbind("mouseover").unbind("mouseout");
		});
		return this;
	};
})(jQuery);