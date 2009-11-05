package com.liteserv.config.namedprocessors;

import java.util.Map;

import com.liteserv.config.LSPlugin;

public class ExecXPath extends LSPlugin 
{
    public Map Expressions;

    public ExecXPath() {
        Class = this.getClass().getName();
    }
}
