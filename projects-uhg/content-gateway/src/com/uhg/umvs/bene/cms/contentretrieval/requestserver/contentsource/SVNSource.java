package com.uhg.umvs.bene.cms.contentretrieval.requestserver.contentsource;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

import javax.servlet.http.HttpServletRequest;

import org.tmatesoft.svn.core.SVNException;
import org.tmatesoft.svn.core.SVNNodeKind;
import org.tmatesoft.svn.core.SVNProperties;
import org.tmatesoft.svn.core.SVNProperty;
import org.tmatesoft.svn.core.SVNURL;
import org.tmatesoft.svn.core.auth.ISVNAuthenticationManager;
import org.tmatesoft.svn.core.internal.io.dav.DAVRepositoryFactory;
import org.tmatesoft.svn.core.io.SVNRepository;
import org.tmatesoft.svn.core.io.SVNRepositoryFactory;
import org.tmatesoft.svn.core.wc.SVNWCUtil;

import com.uhg.umvs.bene.cms.contentretrieval.requestserver.ContentResponse;
import com.uhg.umvs.bene.cms.contentretrieval.requestserver.interfaces.ContentSource;

// SVN accessor, need to provide repository url, possibly the password and username and base path in the source tree to act as root.

public class SVNSource implements ContentSource
{
    // props...
    String repositoryurl;
    public void setRepositoryUrl(String repositoryurl){this.repositoryurl = repositoryurl;}

    String username = "";
    public void setUsername(String username){this.username = username;}

    String password = "";
    public void setPassword(String password){this.password = password;}

    String basepath = "";
    public void setBasepath(String basepath){this.basepath = basepath;}

    public boolean hasContent(String contentItem, HttpServletRequest request)
    {
        DAVRepositoryFactory.setup();
        
        String itempath = basepath + contentItem;
        
        SVNRepository repository = null;

        try {
            repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( repositoryurl ) );
        } catch (SVNException svne) {
            throw new RuntimeException("SVNSource: SVN error in getting repository reference for repo url "+repositoryurl);            
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( username , password );
        repository.setAuthenticationManager( authManager );
        
        SVNNodeKind nodeKind = null;
        
        try {
            nodeKind = repository.checkPath( itempath , -1 );
        } catch (SVNException svne) {
            return false;            
        }
        
        if ( nodeKind == SVNNodeKind.NONE ) {
            return false;
        } else if ( nodeKind == SVNNodeKind.DIR ) {
            throw new RuntimeException("SVNSource: The entry at '" + repositoryurl + "' is a directory while a file was expected." );
        }
        
        return true;
        
    }
    
    
    public ContentResponse getContent(String contentItem, HttpServletRequest req)
    {
        ContentResponse response = new ContentResponse();
        
        DAVRepositoryFactory.setup();
        
        String itempath = basepath + contentItem;
        
        SVNRepository repository = null;

        try {
            repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( repositoryurl ) );
        } catch (SVNException svne) {
            throw new RuntimeException("SVNSource: SVN error in getting repository reference for repo url "+repositoryurl);            
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( username , password );
        repository.setAuthenticationManager( authManager );

        SVNNodeKind nodeKind = null;
        
        try {
            nodeKind = repository.checkPath( itempath , -1 );
        } catch (SVNException svne) {
            throw new RuntimeException("SVNSource: SVN error in getting getting node type for repo url "+repositoryurl);            
        }
        
        if ( nodeKind == SVNNodeKind.NONE ) {
            throw new RuntimeException("SVNSource: there is no entry at "+repositoryurl);
        } else if ( nodeKind == SVNNodeKind.DIR ) {
            throw new RuntimeException("SVNSource: The entry at '" + repositoryurl + "' is a directory while a file was expected." );
        }
        
        SVNProperties props = new SVNProperties(); 
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { 
            repository.getFile(itempath,-1 ,props,baos);
        } catch (SVNException svne) {
            throw new RuntimeException("SVNSource: SVN error in getting file"+itempath+" from repo "+repositoryurl);            
        }

        String mimeType = props.getStringValue(SVNProperty.MIME_TYPE);
        response.setMimetype(mimeType);

        ByteArrayInputStream inputstream = new ByteArrayInputStream(baos.toByteArray());
        response.setContent(inputstream);
        
        return response;
            
        
    }

}
