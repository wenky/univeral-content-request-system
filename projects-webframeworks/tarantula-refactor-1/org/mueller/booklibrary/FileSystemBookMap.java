package org.mueller.booklibrary;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.tarantula.component.interfacedefinition.PageComponentInterface;

public class FileSystemBookMap implements PageComponentInterface {

    public void init() throws Exception 
    {

    }

    public Object process(HttpServletRequest req, HttpServletResponse resp, Map arguments, Map scratchpad) throws Exception 
    {
        String basedir = (String)arguments.get("BasePath");
        List filemap = new ArrayList();
        File base = new File(basedir);
        File[] bookdirs = base.listFiles();
        for (int i=0; i<bookdirs.length; i++)
        {
            if (bookdirs[i].isDirectory())
            {
                File[] booklist = bookdirs[i].listFiles();
                for (int j=0; j< booklist.length; j++)
                    filemap.add(bookdirs[i].getName()+"/"+booklist[j].getName());
            }
        }
        
        return filemap;
    }

}
