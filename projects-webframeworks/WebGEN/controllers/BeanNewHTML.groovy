import org.webgen.util.*;


// This generates HTML for creating a new Bean
// - requires: bean definition (a groovy config file under beans/ directory)

// 1. load WebGEN's bean definition
//String beandef = Bean.Definition(this.getBinding(),params["bean"]);
Map beandef = Bean.Definition(config["bean"]);
// 2. instantiate bean
String beanclass = beandef["bean.class"];
newbean = Bean.Create(beanclass);
// 3. initialize bean
newbean["title"] = "New Book";

// 4. create bean editing HTML using initialized bean
print """
<html>
<body>
  <b>New ${config["bean"]}</b><br>
  <!-- validations/err msgs at some point... -->
  <form method="POST" action="BeanNewProcess">
  <input type="hidden" name="beanclass" value="${beanclass}">
  <table>
""";

int i = 1;
while (beandef["prop"+i] != null)
{
  propname = beandef["prop"+i];
  propvalue = newbean[propname];  
  // check for label override
  if (beandef["prop"+i+".label"] != null) 
    beanlabel = beandef["prop"+i+".label"];  
  else 
    beanlabel = propname;
  // determine widget type/rendering strategy
  if (beandef["prop"+i+".widget"] == "text")
    print """
      <tr><td>${beanlabel}</td><td><input type="text" name="${propname}" value="${propvalue == null ? "" : propvalue}"></td></tr>
    """;
  if (beandef["prop"+i+".widget"] == "bigtext")
    print """
      <tr><td>${beanlabel}</td><td><textarea name="${propname}">${propvalue == null ? "" : propvalue}</textarea></td></tr>
    """;
  if (beandef["prop"+i+".widget"] == "select")
  {
    // need widget's list data - invoke appropriate plugin
    plugin = beandef["prop"+i+".source"]["plugin"];
    pluginconfig = beandef["prop"+i+".source"];
    List datalist = Class.forName(plugin).newInstance().getDataList(this.getBinding(),newbean,beandef,pluginconfig);
    print "<tr><td>${beanlabel}</td><td>";
    print "<select name=\"${propname}\">";
    for (int j=0; j < datalist.size(); j++)
    {
      print "<option value=\"${datalist[j][0]}\" ";
      if (propvalue == datalist[j][0]) print "selected";
      print ">${datalist[j][1]}</option>";
    }
    print "</select>";
    print "</tr>";
  }  
  i++;
} 

print """
  </table>
  <input type="submit" name="Create" value="Create">
  </form>
</body>
</html>
""";