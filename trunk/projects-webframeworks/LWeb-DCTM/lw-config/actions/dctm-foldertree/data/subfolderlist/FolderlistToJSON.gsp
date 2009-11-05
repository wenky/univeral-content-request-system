def results = request.getAttribute("results");

out.write """
[
"""

for (def i=0; i < results.size(); i++) {
  if (i!=0) out.write ",";
  out.write """
   { id: "${results.get(i,2)}", 
     text: "${results.get(i,3)}", 
     leaf: false, 
     dctmdata: { r_object_id: "${results.get(i,2)}"} }
  """
}  

out.write """
]
"""
