  // TO USE THIS OBJECT:
  // - create a div in your HTML body where it will go
  // - declare a global javascript var for the SortableTable object
  // - instantiate, i.e: var globalvar = new SortableTable(divid,globalvarname);
  //   -- divid parameter is the id for the div the table will be created in your HTML body
  //   -- globalvarname is the name of the global variable you declared in step 2 above, so we can generate onclick() handlers to reference this object
  // - call InitData(dataset) on the response from a doQuery executed through DWR --> assigns the dataset
  // - call Initialize() on the object to generate the table
  
  // - Optionally, after instantiation: 
  //   - CSS styles: 
  //    -- you can set the EvenRowCSS and/or OddRowCSS values by setting the SortableTableObject.EvenRowCSS = "style=''" or "class=''". 
  //       Basically, set the string to an HTML attribute (style or class) that will be inserted in the appropriate <tr> tags
  //    -- divid+_colselect, divid+_filter, and divid+_datatable are the ids of the three divs attached to the main divid if you want to style them.
  //       So you should be able to use a CSS selector like td.SortableTable_datatable to style all the tds in the data table...
  //   - DATASET manipulation:
  //     -- set the SortableTable.SortSuffix member to declare a column to search on besides the basic table (basic sorting isn't case insensitive...)
  //        Note that the sorting columns must be added to the trimquery schema, but if they aren't in the dataset's column section, they won't be displayed as data columns....
  //     -- set the SortableTable.DisplaySuffix member to declare a column for 
  
  
  // define SortableTable javascript object template    
  function SortableTable(divid,globalvarname)
  {
    this.div = divid;             // id attribute of the div where the table's HTML will be generated
    this.varname = globalvarname; // in order to generate html onclick handlers, need to know the name of the global var that holds this object...
    this.colselectdiv = this.div + "_colselect"; // div for adding/removing columns from the table display
    this.filterdiv = this.div + "_filter";       // div for selecting a filter column and value
    this.datatablediv = this.div + "_datatable"; // div for the data table view
    this.sortedcolumn = null;  // column that is currently being sorted
    this.sortedtype = null;    // direction (ASC or DESC) of the currently sorted column
    this.indrag = false;       // column resize drag flag (== true when in drag event)
    
    this.SortSuffix = "";      // suffix to append to raw column names in the dataset which is used to sort values in a column with
    
    this.EvenRowCSS = ""; // a class= or style= attribute text to assign to even-index rows
    this.OddRowCSS = "";  // a class= or style= attribute text to assign to odd-index rows

    // dataset initialization        
    this.InitData = function (dataset) {
        this.origdata = dataset; // the original dataset 
        this.data = dataset;     // "scratch" dataset for filtering/sorting/etc.
      }
    
    this.Initialize = function() {
        var thediv = $("#"+this.div);
        thediv.empty();
        // create the divs...
        thediv.append("<hr><div id='"+this.colselectdiv+"'></div><hr>");
        thediv.append("<div id='"+this.filterdiv+"'></div><hr>");
        thediv.append("<div id='"+this.datatablediv+"'></div>");
        // generate the divs
        this.GenerateColumnSelectDiv();
        this.GenerateFilterDiv();
        this.InitializeDataTableDiv();
      }
    
      this.GenerateFilterDiv = function() {
        var columns = "Column: <select id='"+this.div+"_filterselect"+"'>";
        for (var c=0; c < this.data.columns.length; c++)
        {
          columns += "<option value='"+this.data.columns[c].name+"'>"+this.data.columns[c].name;
        }
        columns += "</select> value: <input id='"+this.div+"_filtervalue"+"' type='text'>";
        columns += " <input type='submit' name='filter' value='filter' onclick='"+this.varname+".Filter()'> <a href='#' onclick='"+this.varname+".Unfilter(); return false;'>unfilter</a>";
        //alert("filterdiv: "+columns);
        $("#"+this.filterdiv).append(columns);
      }
      
    this.GenerateColumnSelectDiv = function() {
        var columns = "";
        var first = true;
        for (var c=0; c < this.data.columns.length; c++)
        {
          if (first) first = false; else columns += " | ";
          columns += "<input type='checkbox' id='"+this.div+"_hidecolumn_"+this.data.columns[c].name+"'>" + this.data.columns[c].name;
        }
        columns += "<input type='submit' name='Hide' value='Hide' onclick='"+this.varname+".HideColumns()'>";
        //alert ("columndiv -- " + $.toJSON(columns));
        $("#"+this.colselectdiv).append(columns);
      }
      
    this.HideColumns = function() {
        for (var c=0; c < this.data.columns.length; c++)
        {              
          //alert("#"+this.div+"_hidecolumn_"+this.data.columns[c].name);
          var hideval = $("#"+this.div+"_hidecolumn_"+this.data.columns[c].name).attr("checked");
          //alert("hideval: "+hideval);
          if (hideval) this.data.columns[c].hidden = true; else this.data.columns[c].hidden = false; 
        }
        
        this.GenerateDataTableDiv();
      }

    this.InitializeDataTableDiv = function() {
        var sttime = (new Date()).getTime();
        var timecheck = "top "+((new Date()).getTime()-sttime)+"\n";
        
        // do a quick table generation to get an approx. measure of font width
        $("#"+this.datatablediv).empty();
        timecheck = "div emptied "+((new Date()).getTime()-sttime)+"\n";
        
        // hidden table's header row...
        var hiddenheaders = "";
        for (var h=0; h < this.data.columns.length; h++)
        {
          if (this.data.columns[h].hidden != true) {
            hiddenheaders += "<th nowrap>"+this.data.columns[h].name+"<img src='./wdk/theme/documentum/icons/sort/sortDown.gif' onclick='"+this.varname+".SortByColumn(\""+this.data.columns[h].name+"\");'></th><th nowrap style='cursor: move;'></th>";
          }
        }
        //timecheck += "headertable generated "+((new Date()).getTime()-sttime)+"\n";

        // data div's html...
        var table = "";
        var hiddentable = "<table id='"+this.datatablediv+"_data'>";
        hiddentable += "<tr>"+hiddenheaders+"</tr>";
        for (var d=0; d < this.data.results.length; d++)
        {
          var datarow = (d%2 == 0 ? "<tr "+this.EvenRowCSS+">" : "<tr "+this.OddRowCSS+">");
          var hiddenrow = (d%2 == 0 ? "<tr "+this.EvenRowCSS+">" : "<tr "+this.OddRowCSS+">");
          for (var dc =0; dc < this.data.columns.length; dc++)
          {
            if (this.data.columns[dc].hidden != true) {
              datarow += "<td nowrap><div style='overflow:hidden;'>" + this.data.results[d][this.data.columns[dc].name] + "</div></td><td nowrap></td>";
              hiddenrow += "<td nowrap>" + this.data.results[d][this.data.columns[dc].name] + "</td><td nowrap></td>";
            }
          }
          datarow += "</tr>";
          hiddenrow += "</tr>";
          table += datarow;
          hiddentable += hiddenrow;
        }
        table += "</table>";
        hiddentable += "</table>";
        //alert ("table -- " + $.toJSON(table));        
        timecheck += "datatables generated "+((new Date()).getTime()-sttime)+"\n";
        $("#"+this.datatablediv).append(hiddentable);
        timecheck += "hiddentable added to div "+((new Date()).getTime()-sttime)+"\n";

        var headerrow = document.getElementById(this.datatablediv+"_data").rows[0]; // we'll use this reference in the table column resizing too
        this.columnwidths = [];
        for (var hc=0; hc < headerrow.cells.length; hc++)
        {
            if (hc%2==0)
              this.columnwidths[hc/2] = headerrow.cells[hc].offsetWidth; // store the rendered widths
        }
        $("#"+this.datatablediv).empty();            
        timecheck +="initialwidths calculated"+((new Date()).getTime()-sttime)+"\n";
        
        var columnheaders = ""; 
        for (var c=0; c < this.data.columns.length; c++)
        {
          if (this.data.columns[c].hidden != true) {
            columnheaders += "<th width='"+this.columnwidths[c]+"' nowrap><div style='overflow:hidden;'>"+this.data.columns[c].name+"<img src='./wdk/theme/documentum/icons/sort/sortDown.gif' onclick='"+this.varname+".SortByColumn(\""+this.data.columns[c].name+"\");'></div></th><th nowrap style='cursor: move;'></th>";
          }
        }
        table = "<table id='"+this.datatablediv+"_data'>" + "<tr>"+columnheaders+"</tr>" + table;
        $("#"+this.datatablediv).append(table);            
        timecheck += "datatable added to div "+((new Date()).getTime()-sttime)+"\n";
        
        // add column resizing divs and handlers to header row. 
        this.AddResizers();
        // ?DisableSelections?
        timecheck += "added handlers to headerrow "+((new Date()).getTime()-sttime)+"\n";
        //alert("INITGEN timecheck: " + timecheck);
      }
      
    this.AddResizers = function () 
    {
        var dtdiv = document.getElementById(this.datatablediv);
        dtdiv.SortableTable = this;
        dtdiv.onmouseup = SortableTable_ColumnGrabberMouseUp;
        var headerrow = document.getElementById(this.datatablediv+"_data").rows[0]; // we'll use this reference in the table column resizing too
        var colcounter = 0;
        for (var dc =0; dc < this.data.columns.length; dc++)
        {              
          if (this.data.columns[dc].hidden != true) {
            var sizer = document.createElement("div");
            sizer.innerHTML = "<b>|</b>";
            sizer.SortableTable = this;
            sizer.ColumnToResize = colcounter*2;
            sizer.DataColumnToResize = dc;                
            sizer.onmousedown = SortableTable_ColumnGrabberMouseDown;
            headerrow.cells[colcounter*2+1].appendChild(sizer);
            colcounter++;
          }
        }
    }
    
    this.GenerateDataTableDiv = function() 
    {
        // column widths should be known, so we don't need to do the double-render...
        var sttime = (new Date()).getTime();
        var timecheck = "top "+((new Date()).getTime()-sttime)+"\n";
        
        // do a quick table generation to get an approx. measure of font width
        $("#"+this.datatablediv).empty();
        timecheck = "div emptied "+((new Date()).getTime()-sttime)+"\n";
            
        // data div's html...
        var table = "";
        //var maxwidths = []; for (varff=0; ff < this.data.columns.length; ff++) maxwidths[ff] = 0;
        for (var d=0; d < this.data.results.length; d++)
        {
          var datarow = (d%2 == 0 ? "<tr "+this.EvenRowCSS+">" : "<tr "+this.OddRowCSS+">");
          for (var dc =0; dc < this.data.columns.length; dc++)
          {
            if (this.data.columns[dc].hidden != true) {
              //if (this.data.results[d][this.data.columns[dc].name] > maxwidths[dc]) maxwidths[dc] = (""+this.data.results[d][this.data.columns[dc].name]).length;
              datarow += "<td nowrap><div style='overflow:hidden;'>" + this.data.results[d][this.data.columns[dc].name] + "</div></td><td nowrap></td>";
            }
          }
          datarow += "</tr>";
          table += datarow;
        }
        table += "</table>";
        //alert ("table -- " + $.toJSON(table));        
        timecheck += "datatables generated "+((new Date()).getTime()-sttime)+"\n";
        
        var columnheaders = ""; 
        for (var c=0; c < this.data.columns.length; c++)
        {
          if (this.data.columns[c].hidden != true) {
            columnheaders += "<th width='"+this.columnwidths[c]+"' nowrap><div style='overflow:hidden;'>"+this.data.columns[c].name+"<img src='./wdk/theme/documentum/icons/sort/sortDown.gif' onclick='"+this.varname+".SortByColumn(\""+this.data.columns[c].name+"\");'></div></th><th nowrap style='cursor: move;'></th>";
          }
        }
        table = "<table id='"+this.datatablediv+"_data'>" + "<tr>"+columnheaders+"</tr>" + table;
        $("#"+this.datatablediv).append(table);            
        timecheck += "datatable added to div "+((new Date()).getTime()-sttime)+"\n";
        
        // add column resizing divs and handlers to header row. 
        this.AddResizers();

        // ?DisableSelections?
        timecheck += "added handlers to headerrow "+((new Date()).getTime()-sttime)+"\n";
        //alert("REGENtimecheck: " + timecheck);
    }
      
    this.SortByColumn = function(columnname)
    {
        var schemadata = { dataobjects : this.origdata.metadata };
        //alert ("schema -- " + $.toJSON(schemadata));        
        var datatosort = { dataobjects : this.data.results };
        //alert ("datatosort -- " + $.toJSON(datatosort));        
        var querylang = TrimPath.makeQueryLang(schemadata);
        if (this.sortedcolumn == columnname && this.sortedtype == 'ASC')
          this.sortedtype = 'DESC';
        else
          this.sortedtype = 'ASC';
        this.sortedcolumn = columnname;
        var selstatement = "";
        if (this.sortedtype == 'DESC') 
          selstatement = "SELECT dataobjects.* FROM dataobjects ORDER BY dataobjects."+columnname+this.SortSuffix+" DESC";
        else
          selstatement = "SELECT dataobjects.* FROM dataobjects ORDER BY dataobjects."+columnname+this.SortSuffix+" ASC";
        //alert ("selstatement -- " + selstatement);        
        var statement = querylang.parseSQL(selstatement);
        var filtered = statement.filter(datatosort);
        this.data = { columns : this.origdata.columns, metadata : this.origdata.metadata, results : filtered };
        this.GenerateDataTableDiv();
    }
    
    this.Filter = function()
    {
        var filtercolumn = $("#"+this.div+"_filterselect").val();
        var filtervalue = $("#"+this.div+"_filtervalue").val();
        //alert("filt: " +filtercolumn+ " "+filtervalue);
        var schemadata = { dataobjects : this.origdata.metadata };
        var datatofilter = { dataobjects : this.data.results };
        var querylang = TrimPath.makeQueryLang(schemadata);
        var selstatement = "SELECT dataobjects.* FROM dataobjects WHERE RLIKE ('"+filtervalue+"',dataobjects."+filtercolumn+")";
        var statement = querylang.parseSQL(selstatement);
        var filtered = statement.filter(datatofilter);
        this.data = { columns : this.origdata.columns, metadata : this.origdata.metadata, results : filtered };
        this.GenerateDataTableDiv();
    }
    
    this.Unfilter = function()
    {
        this.data.results = this.origdata.results;
        this.GenerateDataTableDiv();
    }

  }
  
    function SortableTable_ColumnGrabberMouseDown(event)
    {
        if (!event) event = window.event;
        
        // check if we aren't in a drag action sequence already. If not, start one. 
        if (!this.SortableTable.indrag) {
          // start the dragging action sequence, flag that we are in dragging mode on this table
          this.SortableTable.indrag = true;
          // set the column we are dragging
          this.SortableTable.dragcolumn = this.ColumnToResize;
          this.SortableTable.dragdatacolumn = this.DataColumnToResize;
          // store the column's ClientX
          this.SortableTable.origcolumnX = event.clientX;
          this.SortableTable.origwidth = document.getElementById(this.SortableTable.datatablediv+"_data").rows[0].cells[this.SortableTable.dragcolumn].offsetWidth;
        }
        return true;
    }
    
    function SortableTable_ColumnGrabberMouseUp(event)
    {
        if (!event) event = window.event;
        //$("#mouseupdiv").empty();$("#mouseupdiv").append("MMU - "+event.clientX);
        // end the column dragging sequence
        if (this.SortableTable.indrag) {
          this.SortableTable.indrag = false;
          // compute the final column size based on delta
          var finalX = event.clientX;
          var delta = finalX - this.SortableTable.origcolumnX;
          // set column width - we'll try JQuery first
          var ColToResize = document.getElementById(this.SortableTable.datatablediv+"_data").rows[0].cells[this.SortableTable.dragcolumn];
          var oWidth = this.SortableTable.origwidth;
          if (oWidth + delta < 0) oWidth = 0; else oWidth = oWidth + delta;
          //alert ("new colsize: "+oWidth);
          ColToResize.width = oWidth;
          this.SortableTable.columnwidths[this.SortableTable.dragdatacolumn] = oWidth;
        }
        return true;
    }      
  
