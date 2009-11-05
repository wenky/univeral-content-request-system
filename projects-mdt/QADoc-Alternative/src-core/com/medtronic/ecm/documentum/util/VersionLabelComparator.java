package com.medtronic.ecm.documentum.util;

import java.util.Comparator;

public class VersionLabelComparator implements Comparator { 
    public int compare(Object o1, Object o2) {
        String a=(String)o1;
        String b=(String)o2;
        int maja = Integer.parseInt(a.substring(0,a.indexOf('.')));
        int majb = Integer.parseInt(b.substring(0,b.indexOf('.')));
        if (maja > majb) return 1; else if (majb > maja) return -1; 
        int mina = Integer.parseInt(a.substring(a.indexOf('.')+1));
        int minb = Integer.parseInt(b.substring(b.indexOf('.')+1));
        if (mina > minb) return 1; else if (minb > mina) return -1; else return 0; 
    }
    
    public boolean equals(Object obj) {return false;}
    
}
