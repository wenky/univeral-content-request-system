package com.uhg.ovations.groupretiree.cms.gotcha.requesthandler.responseprocessor.comparator;

import java.util.Comparator;
import java.util.List;

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentMetaData;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.util.log.Lg;

public class SortOrderAttributeComparator implements Comparator
{
    String sortOrderAttribute = null;
    
    public String getSortOrderAttribute()
    {
        return sortOrderAttribute;
    }

    public void setSortOrderAttribute(String sortOrderAttribute)
    {
        this.sortOrderAttribute = sortOrderAttribute;
    }

    int getValue(ContentMetaData field)
    {
        if (field.isRepeating()) {
            List values = (List)(field.getValue());
            if (values != null && values.size() > 0)
            {
                String val = (String)values.get(0);
                int parsed = Integer.parseInt(val);
                return parsed;
            } else {
                return Integer.MAX_VALUE;
            }
        } else {
            if (Integer.class.equals(field.getType())) {
                Integer val = (Integer)field.getValue();
                if (val == null) return Integer.MAX_VALUE;
                else return val;
            } else {
                if (field.getValue() == null) {
                    return Integer.MAX_VALUE;
                }
                String val = field.getValue().toString();
                if (val == null) return Integer.MAX_VALUE;
                int parsed = Integer.parseInt(val);
                return parsed;
            }
        }
        
    }

    public int compare(Object o1, Object o2) 
    {                                
        ContentResponse part1 = (ContentResponse)o1;
        ContentResponse part2 = (ContentResponse)o2;
            
        int sortorder1 = getValue(part1.getMetaData().get(sortOrderAttribute));
        int sortorder2 = getValue(part2.getMetaData().get(sortOrderAttribute));
        Lg.trc("sort1: %d sort2:%d",sortorder1,sortorder2);
        if (sortorder1 < sortorder2) return -1;
        if (sortorder1 == sortorder2) return 0;
        return 1;
    }
    
    public boolean equals(Object obj)  { return false; }
}
    