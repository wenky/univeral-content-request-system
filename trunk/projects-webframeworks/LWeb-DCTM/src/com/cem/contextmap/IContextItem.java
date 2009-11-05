package com.cem.contextmap;

import java.util.Enumeration;
import java.util.Iterator;

public interface IContextItem 
{
    public Object get(Object key);
    public Object set(Object key, Object value);
    public boolean isEnumerable();
    public boolean isIterable();
    public Enumeration enumerate();
    public Iterator iterate();
}
