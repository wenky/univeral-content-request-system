if ("true".equals(request.getAttribute("loginsuccess"))) {
  out.write """
  { success:true,blah:"shit" }
  """
} else {
  out.write """
  { success:false, blah:"crap" }
  """
}