def results = request.getAttribute("docbaselist");

response.setHeader("max-age", "0");

out.write """
{
 basecount:"${results.size()}",
 docbases: 
 [
"""

for (def i=0; i < results.size(); i++) {
  if (i!=0) out.write ",";
  out.write """
  {base:"${results[i]}",name:"${results[i]}"}
  """
}  

out.write """
 ]
}
"""


