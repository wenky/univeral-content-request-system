package com.liteserv.core;

import java.io.IOException;
import java.util.List;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.liteserv.config.LSConfiguration;
import com.liteserv.config.LSActionHandler;

// provides access point for more traditional AJAX request (i.e. not managed by DWR) 
// the main difference between this and LSController is that LSController will wrap its 
// responses in the configured frame html.

public class LSNewDiv extends LSBaseController 
{
    // nothing special so far...
}
