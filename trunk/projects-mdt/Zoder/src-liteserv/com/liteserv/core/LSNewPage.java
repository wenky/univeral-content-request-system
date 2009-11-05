package com.liteserv.core;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import lbase.Lg;

import com.liteserv.config.LSActionHandler;
import com.liteserv.core.utils.Is;

public class LSNewPage extends LSBaseController
{

    // overrides base controller with additional frame wrapping
    
    public String process(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException 
    {
        /*-INFO-*/Lg.inf("New Page request - calling super to generate page contents");
        String responsediv = super.process(request, response);
        /*-trc-*/Lg.trc("look up action to check action-specific Frame");
        LSActionHandler action = (LSActionHandler)Configuration.Actions.get(getActionFromRequest(request));
        
        /*-trc-*/Lg.trc("resolve frameaction");
        String frameactionname = null;
        if (!Is.empty(action.FrameAction)) {
            frameactionname = action.FrameAction;
            /*-trc-*/Lg.trc("-- action-specific frame: %s",frameactionname);            
        } else {
            frameactionname = Configuration.FrameAction;
            /*-trc-*/Lg.trc("-- global frame: %s",frameactionname);            
        }
        
        /*-trc-*/Lg.trc("looking up frame action");            
        LSActionHandler frameaction = (LSActionHandler)Configuration.Actions.get(frameactionname);
        
        if (frameaction != null) {
            /*-trc-*/Lg.trc("frame action found, executing");            
            responsediv = executeAction(frameaction,responsediv);
        }  
        
        // send fully generated page
        /*-trc-*/Lg.trc("returning generated page");            
        return responsediv;
    }


}
