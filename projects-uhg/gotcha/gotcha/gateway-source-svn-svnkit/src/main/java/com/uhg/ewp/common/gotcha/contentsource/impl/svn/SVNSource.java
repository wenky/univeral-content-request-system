package com.uhg.ewp.common.gotcha.contentsource.impl.svn;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

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

import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentRequest;
import com.uhg.ewp.common.gotcha.contentsource.data.idef.ContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.data.impl.BaseContentResponse;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceAccessException;
import com.uhg.ewp.common.gotcha.contentsource.exception.SourceRetrievalException;
import com.uhg.ewp.common.gotcha.contentsource.idef.ContentSource;
import com.uhg.ewp.common.gotcha.util.log.Lg;


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
    
    String sourceName = "SVN";
    public String getSourceName() { return sourceName; }
    public void setSourceName(String sourceName) { this.sourceName = sourceName; }



    public ContentResponse getContent(ContentRequest req)
    {
        BaseContentResponse response = new BaseContentResponse();
        
        DAVRepositoryFactory.setup();
        
        String itempath = basepath + req.getContentItem();
        
        response.setItemRef(req.getContentItem());
        response.setSourceId(itempath);
        response.setSourceName(getSourceName());
        
        SVNRepository repository = null;

        try {
            repository = SVNRepositoryFactory.create( SVNURL.parseURIEncoded( repositoryurl ) );
        } catch (SVNException svne) {
            throw new SourceAccessException("SVNSRC-REPOCREATE",Lg.err("SVN error in getting repository reference for repo url "+repositoryurl,svne),svne);            
        }
        ISVNAuthenticationManager authManager = SVNWCUtil.createDefaultAuthenticationManager( username , password );
        repository.setAuthenticationManager( authManager );

        SVNNodeKind nodeKind = null;
        
        try {
            nodeKind = repository.checkPath( itempath , -1 );
        } catch (SVNException svne) {
            throw new SourceAccessException("SVNSRC-CHECKPATH",Lg.err("SVN error in getting getting node type for repo url "+repositoryurl,svne),svne);            
        }
        
        if ( nodeKind == SVNNodeKind.NONE ) {
            response.setFound(false);
            return response;
        } else if ( nodeKind == SVNNodeKind.DIR ) {
            response.setFound(false);
            return response;
        }
        
        
        //TODO: version/lastmodified
        //SVNPropertyValue attrval = repository.getRevisionPropertyValue(-1, "svn:date");
        //response.setVersion(version);
        
        SVNProperties props = new SVNProperties();
        
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        try { 
            repository.getFile(itempath,-1 ,props,baos);
        } catch (SVNException svne) {
            throw new SourceRetrievalException("SVNSRC-GETFILE-FAIL",Lg.err("SVN error in getting file"+itempath+" from repo "+repositoryurl,svne),svne);            
        }

        String mimeType = props.getStringValue(SVNProperty.MIME_TYPE);
        response.setMimetype(mimeType);

        String date = props.getStringValue(SVNProperty.COMMITTED_DATE);

        byte[] content = baos.toByteArray();
        long size = content.length;
        response.setSize(size);
        ByteArrayInputStream inputstream = new ByteArrayInputStream(content);
        
        response.setContent(inputstream);
        
        response.setFound(true);
        
        return response;
            
        
    }

}
