package com.liteserv.config.namedprocessors;

import java.util.Map;

import com.liteserv.config.LSPlugin;

/**
 * Velocity-specific "named"/"feature" actionhandler config info specifier 
 * 
 * @author muellc4
 *
 */

public class VelocityTemplate extends LSPlugin
{
    public String TemplateLocalUrl;
    public String TemplateResource;
    public String TemplateUrl;
    public String TemplateFile;
    public Map InitialContext; 
    
    public VelocityTemplate() {
        Class = this.getClass().getName();
    }
}
