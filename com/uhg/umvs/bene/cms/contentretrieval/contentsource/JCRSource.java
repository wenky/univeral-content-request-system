package com.uhg.umvs.bene.cms.contentretrieval.contentsource;

import java.io.BufferedInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.jcr.Credentials;
import javax.jcr.Item;
import javax.jcr.LoginException;
import javax.jcr.NoSuchWorkspaceException;
import javax.jcr.Node;
import javax.jcr.PathNotFoundException;
import javax.jcr.Property;
import javax.jcr.Repository;
import javax.jcr.RepositoryException;
import javax.jcr.Session;
import javax.jcr.ValueFormatException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.uhg.umvs.bene.cms.contentretrieval.common.ContentSource;

public class JCRSource implements ContentSource
{
    Repository contentrepository;
    public void setContentRepository(Repository repository) {this.contentrepository = repository;}
    
    Credentials credentials;
    public void setCredentials(Credentials creds) {this.credentials = creds;}
    
    String workspace;
    public void setWorkspace(String wkspc) {this.workspace = wkspc; }

    String basepath = "";
    public void setBasePath(String path){this.basepath = path;}

    String contentnode = "";
    public void setContentNode(String path){this.contentnode = path;}

    String contentproperty = null;
    public void setContentProperty(String propname){this.contentproperty = propname;}

    String mimeproperty = null;
    public void setMimeTypeProperty(String propname){this.mimeproperty = propname;}

    public void getContent(String contentItem, HttpServletRequest req, HttpServletResponse resp)
    {
        String itempath = basepath + contentItem + contentnode;        
        Session session = null;
        try {
            session = contentrepository.login(credentials, workspace);
        } catch (NoSuchWorkspaceException nowse) {
            throw new RuntimeException("JCRSource: login attempt failed due to unknown workspace "+workspace,nowse);            
        } catch (LoginException le) {
            throw new RuntimeException("JCRSource: login attempt threw LoginException, check configured credentials",le);                        
        } catch (RepositoryException re) {
            throw new RuntimeException("JCRSource: login attempt threw RepositoryException",re);                        
        }        
        
        Item item = null;
        
        try { 
            item = session.getItem(itempath);
        } catch (PathNotFoundException badpath) {
            throw new RuntimeException("JCRSource: item "+itempath+" not found",badpath);                        
        } catch (RepositoryException re) {
            throw new RuntimeException("JCRSource: item lookup of "+itempath+" threw RepositoryException",re);                        
        } 
        
        Property propitem = null;
        
        if (item.isNode()) {
            // we need to know what property stores
            Node node = (Node)item;
            
            if (mimeproperty != null) {
                try {
                    if (node.hasProperty(mimeproperty)) {
                        Property mimepropitem = node.getProperty(mimeproperty);
                        String mimetype = mimepropitem.getString();
                        if (mimetype != null) {
                            resp.setContentType(mimetype);
                        }
                    }
                } catch (PathNotFoundException badpath) {
                    throw new RuntimeException("JCRSource: property "+mimeproperty+" not found on item "+itempath,badpath);                        
                } catch (RepositoryException re) {
                    throw new RuntimeException("JCRSource: property lookup of "+mimeproperty+" on item "+itempath+" threw RepositoryException",re);                        
                } 
                
            }
            
            String property = contentproperty;
            if (property == null) {
                throw new RuntimeException("JCRSource: need to know what property to get content from for node reference "+itempath);
            }
            try {
                propitem = node.getProperty(property);
            } catch (PathNotFoundException badpath) {
                throw new RuntimeException("JCRSource: property "+property+" not found on item "+itempath,badpath);                        
            } catch (RepositoryException re) {
                throw new RuntimeException("JCRSource: property lookup of "+property+" on item "+itempath+" threw RepositoryException",re);                        
            } 
            itempath = itempath + "/" + property;
        } else {
            propitem = (Property)item;
        }
        
        
        InputStream contentstream;
        try {
        contentstream = new BufferedInputStream(propitem.getStream());
        } catch (ValueFormatException vfe) {
            throw new RuntimeException("JCRSource: error in getting input stream from "+itempath,vfe);                                    
        } catch (RepositoryException re) {
            throw new RuntimeException("JCRSource: getting input stream for "+itempath+" threw RepositoryException",re);                        
        } 
        try { 
            OutputStream os = resp.getOutputStream();
            int read = 0;
            byte[] bytes = new byte[1024];
       
            //While there are still bytes in the file, read them and write them to our OutputStream
            while((read = contentstream.read(bytes)) != -1) {
               os.write(bytes,0,read);
            }
    
            //Clean resources
            os.flush();
            os.close();
        } catch (IOException ioe) {
            throw new RuntimeException("JCRSource: IOException writing "+itempath+" to response",ioe);                        
            
        }
    }

}
