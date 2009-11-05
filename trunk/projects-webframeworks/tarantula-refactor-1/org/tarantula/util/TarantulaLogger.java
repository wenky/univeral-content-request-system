package org.tarantula.util;

import java.util.logging.Logger;
import java.util.logging.Level;

public class TarantulaLogger 
{
    public Logger l;
    public boolean err,warn,inf,cfg,dbg;
    public TarantulaLogger(String classname)
    {
        l = Logger.getLogger(classname);
        err  = l.isLoggable(Level.SEVERE);
        warn = l.isLoggable(Level.WARNING);
        inf  = l.isLoggable(Level.INFO);
        cfg  = l.isLoggable(Level.CONFIG);
        dbg  = l.isLoggable(Level.FINE);                
    }
    
    public void err(String m) {l.severe(m);}
    public void warn(String m) {l.severe(m);}
    public void inf(String m) {l.info(m);}
    public void cfg(String m) {l.config(m);}
    public void dbg(String m) {l.fine(m);}
    
}
