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
// Functions for interacting with Tables
// =====================================
var Table = {};

Table.VERSION = .955;
	
// Get a parent TABLE object reference from any element within it
// --------------------------------------------------------------
Table.getTable = function(o) {
	if (o==null) { 
		return o; 
	}
	return DOM.getParentByTagName(o,"TABLE");
};

// Resolve a table given an element reference, and make sure it has an ID
// ----------------------------------------------------------------------
Table.resolve = function(o) {
	if (o==null) { return null; }
	if (o.nodeName && o.nodeName!="TABLE") {
		o = this.getTable(o);
	}
	CSS.createId(o);
	return o;
};

// Find the table cell containing any passed-in element
// ----------------------------------------------------
Table.getCell = function(o) {
	if (o==null) { return null; }
	if (o.nodeName && o.nodeName!="TH" && o.nodeName!="TD") {
		o = DOM.getParentByTagName(o,["TD","TH"]);
	}
	return o;
};

// Expand all hidden TBODY tags	
// ----------------------------
Table.expandBodies = function(t) {
	var bodies = this.getBodies(t);
	if (bodies==null) { 
		return bodies; 
	}
	for (var i=0; i<bodies.length; i++) {
		if (CSS.get(bodies[i],"display")=="none") {
			CSS.set(bodies[i],"display","block");
		}
	}
};

// Get all the tbody elements in a table
// -------------------------------------
Table.getBodies = function(t) {
	if (t==null) { 
		return t; 
	}
	if (t.getElementsByTagName) {
		return t.getElementsByTagName("TBODY");
	}
	return null;
};

// Get all the thead elements in a table
// -------------------------------------
Table.getHeads = function(t) {
	if (t==null) { 
		return t; 
	}
	if (t.getElementsByTagName) {
		return t.getElementsByTagName("THEAD");
	}
	return null;
};

// Expand all table rows and hide the row containing the onclick action
// --------------------------------------------------------------------
Table.expandRowClicked = function(o) {
	var t, tr;
	if ((tr = DOM.getParentByTagName(o,"TR"))==null) { 
		return; 
	}
	if ((t = this.getTable(tr))==null) { 
		return; 
	}
	this.expandBodies(t);
	CSS.setStyle(tr,"display","none");
};

// Run a function against each cell in a table header, usually to add
// or remove css classes based on sorting, filtering, etc.
// ------------------------------------------------------------------
Table.processHeaderCells = function(t, func) {
	t = this.resolve(t);
	if (t==null) { return; }
	var theads = this.getHeads(t);
	for (var i=0; i<theads.length; i++) {
		var th = theads[i];
		if (th.rows && th.rows.length && th.rows.length>0) { 
			var rows = th.rows;
			var len = rows.length;
			for (var j=0; j<len; j++) { 
				var row = rows[j];
				if (row.cells && row.cells.length && row.cells.length>0) {
					var cells = row.cells;
					var len2 = cells.length;
					for (var k=0; k<len2; k++) {
						var cellsK = cells[k];
						func(cellsK);
					}
				}
			}
		}
	}
};

// Get the cellIndex value for a cell. This is only needed because of a Safari
// bug that causes cellIndex to exist but always be 0.
// Rather than feature-detecting each time it is called, the function will
// re-write itself the first time it is called.
// ---------------------------------------------------------------------------
Table.getCellIndex = function(td) {
	var tr = td.parentNode;
	var cells = tr.cells;
	if (cells && cells.length) {
		if (cells.length>1 && cells[cells.length-1].cellIndex && cells[cells.length-1].cellIndex>0) {
			this.getCellIndex = function(td) {
				return td.cellIndex;
			};
			return td.cellIndex;
		}
		for (var i=0; i<cells.length; i++) {
			if (tr.cells[i]==td) {
				return i;
			}
		}
	}
	return 0;
};

// Get the text value of a cell. Only use innerText if explicitly told to, because 
// otherwise we want to be able to handle sorting on inputs and other types
// -------------------------------------------------------------------------------
Table.getCellValue = function(td,useInnerText) {
	if (td==null) { 
		return null; 
	}
	if (useInnerText && defined(td.innerText)) {
		return td.innerText;
	}
	if (!td.childNodes) { 
		return ""; 
	}
	var childNodes = td.childNodes;
	var childNodesLength = childNodes.length;
	var node = null;
	var ret = "";
	for (var i=0; i<childNodesLength; i++) {
		node = childNodes[i];
		if (node.nodeType && node.nodeType==1) {
			if (node.nodeName=="INPUT" && defined(node.value)) {
				if (node.type && (node.type!="checkbox" || node.checked)) {
					ret += node.value;
				}
			}
			else if (node.nodeName=="SELECT" && node.selectedIndex>=0 && node.options) {
				// Sort select elements by the visible text
				ret += node.options[node.selectedIndex].text;
			}
			else if (node.nodeName=="IMG" && node.name) {
				// Sort image elements by their NAME attribute
				ret += node.name;
			}
			else {
				ret += this.getCellValue(node);
			}
		}
		else {
			if (node.nodeType && node.nodeType==3) {
				if (defined(node.innerText)) {
					ret += node.innerText;
				}
				else if (defined(node.nodeValue)) {
					ret += node.nodeValue;
				}
			}
		}
	}
	return ret;
};

// Consider colspan and rowspan values in table header cells to calculate the actual cellIndex
// of a given cell
// -------------------------------------------------------------------------------------------
Table.tableHeaderIndexes = {};
Table.getActualCellIndex = function(tableCellObj) {
	var tableObj = this.getTable(tableCellObj);
	var cellCoordinates = tableCellObj.parentNode.rowIndex+"-"+this.getCellIndex(tableCellObj);

	// If it has already been computed, return the answer from the lookup table
	if (typeof(this.tableHeaderIndexes[tableObj.id])!='undefined') {
		return this.tableHeaderIndexes[tableObj.id][cellCoordinates];			
	} 

	var matrix = [];
	this.tableHeaderIndexes[tableObj.id] = {};
	var thead = tableObj.getElementsByTagName('THEAD')[0];
	var trs = thead.getElementsByTagName('TR');

	// Loop thru every tr and every cell in the tr, building up a 2-d array "grid" that gets
	// populated with an "x" for each space that a cell takes up. If the first cell is colspan
	// 2, it will fill in values [0] and [1] in the first array, so that the second cell will
	// find the first empty cell in the first row (which will be [2]) and know that this is
	// where it sits, rather than its internal .cellIndex value of [1].
	for (var i=0; i<trs.length; i++) {
		var cells = trs[i].cells;
		for (var j=0; j<cells.length; j++) {
			var c = cells[j];
			var rowIndex = c.parentNode.rowIndex;
			var cellId = rowIndex+"-"+this.getCellIndex(c);
			var rowSpan = c.rowSpan || 1;
			var colSpan = c.colSpan || 1
			var firstAvailCol;
			if(typeof(matrix[rowIndex])=="undefined") { 
				matrix[rowIndex] = []; 
			}
			var m = matrix[rowIndex];
			// Find first available column in the first row
			for (var k=0; k<m.length+1; k++) {
				if (typeof(m[k])=="undefined") {
					firstAvailCol = k;
					break;
				}
			}
			this.tableHeaderIndexes[tableObj.id][cellId] = firstAvailCol;
			for (var k=rowIndex; k<rowIndex+rowSpan; k++) {
				if(typeof(matrix[k])=="undefined") { 
					matrix[k] = []; 
				}
				var matrixrow = matrix[k];
				for (var l=firstAvailCol; l<firstAvailCol+colSpan; l++) {
					matrixrow[l] = "x";
				}
			}
		}
	}
	// Store the map so future lookups are fast.
	return this.tableHeaderIndexes[tableObj.id][cellCoordinates];
};


// Sort all rows in each TBODY (tbodies are sorted independent of each other)
// --------------------------------------------------------------------------
Table.lastSortedColumn = {};
Table.SortedAscendingClassName = "TableSortedAscending";
Table.SortedDescendingClassName = "TableSortedDescending";
Table.SortableClassName = "sortable";

Table.sort = function(t,args) {
	var colIndex, sortType, descending, rowShade, ignoreHiddenRows;
	if (!defined(args)) { 
		args = {}; 
	}

	// Save a ref to the original object passed in
	var origT = t;

	if (t==null) { 
		return; 
	}
	// Resolve the table
	t = this.resolve(t);

	// Resolve actual colIndex
	if (defined(args['colIndex'])) {
		colIndex = args['colIndex'];
	} else if (defined(origT) && defined(origT.cellIndex)) {
		colIndex = this.getActualCellIndex(origT);
	} else {
		colIndex = 0;
	}

	// Resolve sortType
	sortType = ((!defined(args['sortType'])) || (typeof(args['sortType'])!="function")) ? Sort.Default : args['sortType'];

	// Resolve descending
	if (defined(this.lastSortedColumn[t.id]) && this.lastSortedColumn[t.id]['index']==colIndex) {
		descending = !(this.lastSortedColumn[t.id]['descending']);
	} else if (defined(args['descending']) && typeof(args['descending'])=="boolean") { 
		descending = args['descending'];
	} else {
		descending = false;
	}

	// Resolve whether or not to consider hidden rows when shading alternate rows
	ignoreHiddenRows = (defined(args['ignoreHiddenRows'])) ? args['ignoreHiddenRows'] : false;

	// Class name corresponding to the sort order
	var sortedAscendingClassName = this.SortedAscendingClassName;
	var sortedDescendingClassName = this.SortedDescendingClassName;
	var sortableClassName = this.SortableClassName;
	var sortedClassName = descending?sortedDescendingClassName:sortedAscendingClassName;

	// If standard sorting functions are used, convert each cell value in advance using a conversion
	// function, then sort by alphanumeric so sorting is much faster
	var sortConversion = false;
	if (sortType==Sort.Default || sortType==Sort.AlphaNumeric) {
		sortType=Sort.AlphaNumeric;
	} else if (sortType==Sort.IgnoreCase) {
		sortConversion = Sort.IgnoreCaseConversion;
		sortType=Sort.AlphaNumeric;
	} else if (sortType==Sort.Numeric) {
		sortConversion = Sort.NumericConversion;
		sortType=Sort.AlphaNumeric;
	} else if (sortType==Sort.Currency) {
		sortConversion = Sort.CurrencyConversion;
		sortType=Sort.AlphaNumeric;
	} else if (sortType==Sort.Date) {
		sortConversion = Sort.DateConversion;
		sortType=Sort.AlphaNumeric;
	}

	// Store the last sorted column so clicking again will reverse the sort order
	this.lastSortedColumn[t.id] = {'index':colIndex, 'descending':descending};

	// Loop through all THEADs and remove sorted class names, then re-add them for the colIndex
	// that is being sorted
	var self = this;
	this.processHeaderCells(t,
		function(cell) {
			if (CSS.hasClass(cell,sortableClassName)) {
				CSS.removeClass(cell,sortedAscendingClassName);
				CSS.removeClass(cell,sortedDescendingClassName);
				// If the computed colIndex of the cell equals the sorted colIndex, flag it as sorted
				if (colIndex==self.getActualCellIndex(cell)) {
					CSS.addClass(cell,sortedClassName);
				}
			}
		}
	);

	// Sort each tbody independently
	var bodies = this.getBodies(t);
	if (bodies==null || bodies.length==0) { return; }
	for (var i=0; i<bodies.length; i++) {
		var tb = bodies[i];
		var tbrows = tb.rows;
		var tbrowslength = tbrows.length;
		var rows = [];

		// If there are no inputs in the tbody, feel free to use innerText which is faster
		var useInnerText = (tb.getElementsByTagName('INPUT').length==0);

		// Create a separate array which will store the converted values and refs to the
		// actual tows. This is the array that will be sorted.
		var cRow;
		var cRowIndex=0;
		if (cRow=tbrows[cRowIndex]){
			// Funky loop style because it's faster in IE
			do {
				if (rowCells = cRow.cells) {
					var cellValue = (rowCells&&colIndex<rowCells.length)?this.getCellValue(rowCells[colIndex],useInnerText):null;
					if (sortConversion) cellValue = sortConversion(cellValue);
					rows[cRowIndex] = [cellValue,tbrows[cRowIndex]];
				}
			} while (cRow=tbrows[++cRowIndex])
		}

		// Do the actual sorting
		var newSortFunc = function(a,b) {
			return (descending)?sortType(b[0],a[0]):sortType(a[0],b[0]);
		};
		rows.sort(newSortFunc);

		// Move the rows to the correctly sorted order. Appending an existing DOM object
		// just moves it!
		var cRow;
		var cRowIndex=0;
		if (cRow=rows[cRowIndex]){
			do { tb.appendChild(cRow[1]); } while (cRow=rows[++cRowIndex])
		}
	}

	// Re-shade alternate rows if a class name was supplied
	if (defined(args['rowShade'])) {
		this.shadeOddRows(t,args['rowShade'],ignoreHiddenRows);
	}
};

// Filter all rows in each TBODY
// -----------------------------
// TODO: Finish up and Test table filtering!

Table.FilteredClassName = "TableFiltered";
Table.FilterableClassName = "filterable";
Table.Filters = {};

Table.filter = function(t,filters,args) {
	var colIndex, rowShade, filter, allFilters;
	var reset = false; // If null is passed in, reset the whole table
	if (!defined(args)) { args = {}; }
	// Filters can either be sent in one at a time or as a group
	if (!defined(filters)) { return; }
	// Allow for passing a select list in as the filter, since this is common design
	if (filters && filters.nodeName && filters.nodeName=="SELECT" && filters.type) {
		if (filters.type=="select-one" && defined(filters.selectedIndex) && filters.selectedIndex>-1) {
			var sel = filters;
			filters = {};
			filters.filter = sel.options[sel.selectedIndex].value;
		}
	}
	if (isObject(filters) && !isArray(filters)) {
		filters = [filters];
	}
	else if (filters==null) {
		reset = true;
		filters = [];
	}
	else { return; }

	// Find the cell
	t = this.getCell(t);
	
	// Resolve colIndex for each filter
	for (var i=0; i<filters.length; i++) {
		colIndex = filters[i].colIndex;
		if (!defined(colIndex) && defined(t) && defined(t.cellIndex)) {
			filters[i].colIndex = this.getCellIndex(t);
		}
	}

	if (t==null) { return; }
	// Resolve the table
	t = this.resolve(t);

	// Update the list of all active filters for this table
	if (!defined(this.Filters[t.id]) || reset) {
		this.Filters[t.id] = {};
	}
	var allFilters = this.Filters[t.id];
	for (var i=0; i<filters.length; i++) {
		filter = filters[i];
		if (filter.filter==null || filter.filter=="") {
			delete allFilters[filter.colIndex];
		}
		else {
			allFilters[filter.colIndex] = filter.filter;
		}
	}

	// Filter all tbodies of the table
	var bodies = this.getBodies(t);
	if (bodies==null || bodies.length==0) { return; }
	for (var i=0; i<bodies.length; i++) {
		var tb = bodies[i];
		for (var j=0; j<tb.rows.length; j++) {
			var row = tb.rows[j];
			if (reset) {
				row.style.display="";
			}
			else if (row.cells) {
				var cells = row.cells;
				var cellsLength = cells.length;
				// Test each filter
				var hide = false;
				for (colIndex in allFilters) {
					if (!hide) {
						filter = allFilters[colIndex];
						if (colIndex < cellsLength) {
							var val = this.getCellValue(cells[colIndex]);
							if (filter.charAt(0)=="/" && val.search) {
								hide = (val.search(new RegExp(filter.substring(1,filter.length-1)))<0);
							}
							else if (val!=filter) {
								hide = true;
							}
						}
					}
				}
				if (hide) {
					row.style.display = "none";
				}
				else {
					row.style.display="";
				}
			}
		}
	}

	// Loop through all THEADs and add filtered class names
	var self = this;
	this.processHeaderCells(t,
		function(cell) {
			if (defined(allFilters[self.getCellIndex(cell)]) && CSS.hasClass(cell,self.FilterableClassName)) {
				CSS.addClass(cell,self.FilteredClassName);
			}
			else {
				CSS.removeClass(cell,self.FilteredClassName);
			}
		}
	);

	// Shade rows if a class name was supplied
	if (defined(args['rowShade'])) {
		this.shadeOddRows(t,args['rowShade']);
	}
};

// Shade alternate rows
// --------------------
Table.shadeOddRows = function(t,className,ignoreHiddenRows) { 
	if (t==null) { 
		return;
	}
	ignoreHiddenRows = (defined(ignoreHiddenRows) && typeof(ignoreHiddenRows)=="boolean") ? ignoreHiddenRows : false;
	t = this.resolve(t);
	var bodies = this.getBodies(t);
	if (bodies==null || bodies.length==0) { 
		return; 
	}
	for (var i=0; i<bodies.length; i++) {
		var tb = bodies[i];
		var tbrows = tb.rows;
		var cRowIndex=0;
		var cRow;
		var displayedCount=0;
		if (cRow=tbrows[cRowIndex]){
			do {
				if (ignoreHiddenRows || CSS.getStyle(cRow,"display")!="none") {
					if (displayedCount++%2==0) { 
						CSS.removeClass(cRow,className); 
					}
					else { 
						CSS.addClass(cRow,className); 
					}
				}
			} while (cRow=tbrows[++cRowIndex])
		}
	}
};

// "Page" a table by showing only a subset of the rows
// ---------------------------------------------------
Table.pages = {};
Table.page = function(t,pageIndex,pageSize,args) {
	if (!defined(args)) { args = {}; }
	if (!defined(pageSize) || typeof(pageSize)!="number" || pageSize==0) {
		pageSize = 25; // arbitrary default
	}
	if (!defined(pageIndex) || typeof(pageIndex)!="number") {
		pageIndex = 0;
	}

	var startRow = pageIndex*pageSize;
	var endRow = startRow + pageSize - 1;
	
	if (t==null) { return; }
	// Resolve the table
	t = this.resolve(t);

	// Assumption: only one tbody!
	var bodies = this.getBodies(t);
	if (bodies==null || bodies.length==0) { return; }
	var tb = bodies[0];

	// Don't let the page go past the beginning
	if (startRow<0) {
		pageIndex = 0;
		startRow = 0;
		endRow = startRow + pageSize - 1;
	}
	// Don't let the page go past the end
	if (startRow > tb.rows.length) {
		pageIndex = Math.floor(tb.rows.length/pageSize);
		if (pageIndex==tb.rows.length/pageSize) {
			pageIndex--;
		}
		startRow = pageIndex * pageSize;
		endRow = startRow + pageSize;
	}

	// Store the table's current state	
	this.pages[t.id] = { 'pageIndex':pageIndex, 'pageSize':pageSize };

	for (var i=0; i<tb.rows.length; i++) {
		var row = tb.rows[i];
		if (i<startRow || i>endRow) {
			row.style.display="none";
		}
		else {
			row.style.display="";
		}
	}

	// Shade rows if a class name was supplied
	if (defined(args['rowShade'])) {
		this.shadeOddRows(t,args['rowShade']);
	}
}

Table.pageNext = function(t,pageSize,args) {
	t = this.resolve(t);
	if (defined(Table.pages[t.id])) {
		var pages = Table.pages[t.id];
		var newPage = pages.pageIndex+1;
		this.page(t,newPage,pageSize || pages.pageSize,args);
		return newPage;
	}
	else {
		this.page(t,1,pageSize,args);
		return 1;
	}
	return -1;
};

Table.pagePrevious = function(t,pageSize,args) {
	t = this.resolve(t);
	if (defined(Table.pages[t.id])) {
		var pages = Table.pages[t.id];
		var newPage = pages.pageIndex-1;
		this.page(t,newPage,pageSize || pages.pageSize,args);
		return newPage;
	}
	else {
		this.page(t,0,pageSize,args);
		return 0;
	}
	return -1;
};