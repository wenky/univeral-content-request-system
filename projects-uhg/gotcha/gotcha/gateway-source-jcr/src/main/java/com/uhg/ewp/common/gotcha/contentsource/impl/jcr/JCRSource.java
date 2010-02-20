package com.uhg.ewp.common.gotcha.contentsource.impl.jcr;

import java.io.InputStream;

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

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceAccessException;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceConfigurationException;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;


// gets content from a JCR content repository, likely a Jackrabbit one
// need to configure the Repository, credentials, and workspace name
// also, will need to set the content node name and the property that has the content on the content node
// specifying the name of the content node's mime property is optional
// optionally provide a base path to start from in the JCR tree

public class JCRSource implements ContentSource
{
    //---- PROPERTIES

    //set
    Repository contentrepository;

    //set
    Credentials credentials;
    
    //set
    String workspace;

    //set
    String basepath = "";

    //set
    String contentNode = "";

    //set
    String contentProperty = null;

    //set
    String mimeTypeProperty = null;
    
    //getset
    String sourceName = "JCR";

    //---- END PROPERTIES

    
    public ContentResponse getContent(ContentRequest req)
    {
        BaseContentResponse response = new BaseContentResponse();
        
        String contentItem = req.getContentItem();
        
        String itempath = basepath + contentItem + contentNode;
        
        response.setItemRef(contentItem);
        response.setSourceId(itempath);
        response.setSourceName(getSourceName());
        
        Session session = null;
        
        try {
            session = contentrepository.login(credentials, workspace);
        } catch (NoSuchWorkspaceException nowse) {
            throw new SourceAccessException("JCRSRC-LOGIN-NOWRKSPC",Lg.err("login attempt failed due to unknown workspace "+workspace,nowse),nowse);            
        } catch (LoginException le) {
            throw new SourceAccessException("JCRSRC-LOGIN-FAIL",Lg.err("login attempt threw LoginException, check configured credentials",le),le);                        
        } catch (RepositoryException re) {
            throw new SourceAccessException("JCRSRC-LOGIN-REPOFAIL",Lg.err("login attempt threw RepositoryException",re),re);                        
        }        
        
        Item item = null;
        
        try { 
            item = session.getItem(itempath);
        } catch (PathNotFoundException badpath) {
            response.setFound(false);
            return response;
        } catch (RepositoryException re) {
            throw new SourceRetrievalException("JCRSRC-FINDITEM-REPOFAIL",Lg.err("item lookup of "+itempath+" threw RepositoryException",re),re);                        
        } 
        
        if (item == null) 
        {
            response.setFound(false);
            return response;
        }
        
        Property propitem = null;
        
        if (item.isNode()) {
            // we need to know what property stores
            Node node = (Node)item;
            
            if (mimeTypeProperty != null) {
                try {
                    if (node.hasProperty(mimeTypeProperty)) {
                        Property mimepropitem = node.getProperty(mimeTypeProperty);
                        String mimetype = mimepropitem.getString();
                        if (mimetype != null) {
                            response.setMimetype(mimetype);
                        }
                    }
                } catch (PathNotFoundException badpath) {
                    throw new SourceRetrievalException("JCRSRC-GETMIME-BADATTR",Lg.err("property "+mimeTypeProperty+" not found on item "+itempath,badpath),badpath);                        
                } catch (RepositoryException re) {
                    throw new SourceRetrievalException("JCRSRC-GETMIME-REPOFAIL",Lg.err("property lookup of "+mimeTypeProperty+" on item "+itempath+" threw RepositoryException",re),re);                        
                } 
                
            }
            
            String property = contentProperty;
            if (property == null) {
                throw new SourceConfigurationException("JCRSRC-CFG-CONTENTPROPNAME",Lg.err("need to know what property to get content from for node reference "+itempath));
            }
            try {
                propitem = node.getProperty(property);
            } catch (PathNotFoundException badpath) {
                throw new SourceRetrievalException("JCRSRC-CONTENTNODE-PATHFAIL",Lg.err("property "+property+" not found on item "+itempath,badpath),badpath);                        
            } catch (RepositoryException re) {
                throw new SourceRetrievalException("JCRSRC-CONTENTNODE-REPOFAIL",Lg.err("property lookup of "+property+" on item "+itempath+" threw RepositoryException",re),re);                        
            } 
            itempath = itempath + "/" + property;
        } else {
            propitem = (Property)item;
        }
        
        // TODO: version...lastmod...size...
        
        
        InputStream contentstream;
        try {
            contentstream = propitem.getStream();
            response.setContent(contentstream);
        } catch (ValueFormatException vfe) {
            throw new SourceRetrievalException("JCRSRC-GETCONTENT-VALUEFORMATFAIL",Lg.err("error in getting input stream from "+itempath,vfe),vfe);                                    
        } catch (RepositoryException re) {
            throw new SourceRetrievalException("JCRSRC-GETCONTENT-REPOFAIL",Lg.err("getting input stream for "+itempath+" threw RepositoryException",re),re);                        
        } 
        
        return response;
    }
    
    //---- getter setter cruft
    
    public void setContentRepository(Repository repository) {this.contentrepository = repository;}
    
    public void setCredentials(Credentials creds) {this.credentials = creds;}
    
    public void setWorkspace(String wkspc) {this.workspace = wkspc; }

    public void setBasePath(String path){this.basepath = path;}

    public void setContentNode(String path){this.contentNode = path;}

    public void setContentProperty(String propname){this.contentProperty = propname;}

    public void setMimeTypeProperty(String propname){this.mimeTypeProperty = propname;}

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }



}
