package com.cem.lweb.core.interfaces;


import java.util.Map;

import com.cem.contextmap.ContextMap;


public interface ITaskHandler {
    public String execute(final Map progitdef, ContextMap context, String response);
}
