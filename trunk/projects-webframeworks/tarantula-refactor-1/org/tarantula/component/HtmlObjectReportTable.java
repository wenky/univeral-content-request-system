package org.tarantula.component;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

public class HtmlObjectReportTable implements PageComponentInterface {

    public String getNickname() { return "HtmlObjectReportTable"; }

    public void init() throws Exception {
        // TODO Auto-generated method stub

    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        // TODO sort order
        // get Object list to display
        List objectlist = (List)arguments.get("Objects");
        // get column list ( a list of serialized Properties objects )
        List columnlist = (List)arguments.get("Columns");
        
        return generatetable(objectlist,columnlist);
    }
    
    public String generatetable(List objectlist, List columnlist) throws Exception
    {
        
        String table = "";
        table += "<table>";
        // header row
        table += "<th>";
        for (int i=0; i < columnlist.size(); i++)
        {
            Properties columnprops = (Properties)columnlist.get(i);
            table += "<td>";
            table += columnprops.getProperty("Name");
            table += "</td>";
        }
        table += "</th>";

        Class beantype = objectlist.get(0).getClass();        
        for (int oli=0; oli < objectlist.size(); oli++)
        {
            table +="<tr>";
            Object curobj = objectlist.get(oli);
            for (int ci = 0; ci < columnlist.size(); ci++)
            {
                Properties columnprops = (Properties)columnlist.get(ci);
                String propname = columnprops.getProperty("Name");
                Method m = beantype.getMethod("get"+propname,null);
                Object propvalue = m.invoke(curobj,null);
                table +="<td>"+propvalue.toString()+"</td>";
            }
            table += "</tr>";
        }
        table += "</table>";
        
        
        return table;
    }

    public class testbean
    {
        testbean(String i1, int j1, double k1) { i=i1; j=j1; k=k1; }
        String i;
        int j;
        double k;
        public String getI() {
            return i;
        }
        public void setI(String i) {
            this.i = i;
        }
        public int getJ() {
            return j;
        }
        public void setJ(int j) {
            this.j = j;
        }
        public double getK() {
            return k;
        }
        public void setK(double k) {
            this.k = k;
        }
    }
    
    public static void main(String[] args) throws Exception
    {
        HtmlObjectReportTable t = new HtmlObjectReportTable();
        //create a beanlist
        ArrayList a = new ArrayList();
        ArrayList c = new ArrayList();
        Properties p;
        
        p = new Properties(); p.setProperty("Name","I"); c.add(p);
        p = new Properties(); p.setProperty("Name","J"); c.add(p);
        p = new Properties(); p.setProperty("Name","K"); c.add(p);
        
        
        testbean b;
        b = t.new testbean("ack",45,-5.31415);a.add(b);
        b = t.new testbean("oomph",145,-15.44);a.add(b);
        b = t.new testbean("OI",455,-5.411);a.add(b);
        b = t.new testbean("yada",450,-5.34);a.add(b);
        b = t.new testbean("poo",1045,-6.44);a.add(b);

        String table = t.generatetable(a,c);
    }
}
