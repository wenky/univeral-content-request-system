package com.uhg.umvs.bene.cms.contentretrieval.common;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

// these route requests to a content source(s), if logic in the handleRequest() method determines that the
// request is handled by any of those sources.
//
// The handle call should either return true if it was handled, or false if this did not handle, so that 
// other handlers can attempt to serve the request.

public interface ContentRequestHandler
{
    // returns true if the request was identified as a source match, or false if the source doesn't match this request
    public boolean handleRequest(HttpServletRequest req, HttpServletResponse resp);
}
