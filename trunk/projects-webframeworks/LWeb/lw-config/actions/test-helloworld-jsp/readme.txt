Example of the simplest action possible. Since there is no processing specified,
it outputs the contents of the first outputtable file it finds, looking for jsp,
then velocity (.vtl), and failing that, htm or html files.

Essentially, barring any processing, the default action is to output the first
jsp, vtl, or html file contained in the action's base directory. In this case,
test.jsp is invoked.

Test.jsp's generated output isn't directly passed to the output response, it 
is stored in a standard response string, so it may be postprocessed with a frame,
should this be part of a new page/frame...

security=unsecured setting  is still necessary for public/unauthentication.