package com.medtronic.ecm.util;

import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

public class ResultSetList implements List
{
    List list = null;
    
    Map columns = null;
    public ResultSetList(List data, Map columnmap) {
        list = data;
        columns = columnmap;        
    }

    public void add(int index, Object element) {
        list.add(index,element);
    }

    public boolean add(Object o) {
        return list.add(o);
    }

    public boolean addAll(Collection c) {
        return list.addAll(c);
    }

    public boolean addAll(int index, Collection c) {
        return list.addAll(index,c);
    }

    public void clear() {
        list.clear();        
    }

    public boolean contains(Object o) {
        return list.contains(o);
    }
    
    // specialized
    public boolean contains(Object o, String column) {
        int colidx = (Integer)columns.get(column);
        return contains(o,colidx);
    }
    public boolean contains(Object o, int colidx) {
        for (int i=0; i < list.size(); i++) {
            Object[] datarow = (Object[])list.get(i);
            if (o.equals(datarow[colidx])) {
                return true;
            }
        }
        return false;
    }
    
    public int indexOf(Object o, String columnname) {
        int colidx = (Integer)columns.get(columnname);
        return indexOf(o,colidx);
    }
    public int indexOf(Object o, int colidx) {
        for (int i=0; i < list.size(); i++) {
            Object[] datarow = (Object[])list.get(i);
            if (o.equals(datarow[colidx])) {
                return i;
            }
        }
        return -1;
    }

    
    public Object get(int index, String column) {
        int colidx = (Integer)columns.get(column);
        return get(index,colidx);
    }
    public Object get(int index, int colidx) {
        Object[] datarow = (Object[])list.get(index);
        return datarow[colidx];
    }
    
    public Set columnNames()
    {
        return columns.keySet();
    }



    public boolean containsAll(Collection c) {
        return list.containsAll(c);
    }

    public Object get(int index) {
        return list.get(index);
    }


    public int indexOf(Object o) {
        return list.indexOf(o);
    }
    


    public boolean isEmpty() {
        return list.isEmpty();
    }

    public Iterator iterator() {
        return list.iterator();
    }

    public int lastIndexOf(Object o) {
        return list.lastIndexOf(o);
    }

    public ListIterator listIterator() {
        return list.listIterator();
    }

    public ListIterator listIterator(int index) {
        return list.listIterator(index);
    }

    public Object remove(int index) {
        return list.remove(index);
    }

    public boolean remove(Object o) {
        return list.remove(o);
    }

    public boolean removeAll(Collection c) {
        return list.removeAll(c);
    }

    public boolean retainAll(Collection c) {
        return list.retainAll(c);
    }

    public Object set(int index, Object element) {
        return list.set(index, element);
    }

    public int size() {
        return list.size();
    }

    public List subList(int fromIndex, int toIndex) {
        return new ResultSetList(list.subList(fromIndex, toIndex),columns);
    }

    public Object[] toArray() 
    {
        return list.toArray();
    }

    public Object[] toArray(Object[] a) {
        return list.toArray(a);
    }

}
