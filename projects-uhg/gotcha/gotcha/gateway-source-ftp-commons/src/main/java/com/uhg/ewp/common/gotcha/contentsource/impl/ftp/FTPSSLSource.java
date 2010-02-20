package com.uhg.ewp.common.gotcha.contentsource.impl.ftp;

import java.security.NoSuchAlgorithmException;

import org.apache.commons.net.ftp.FTPClient;
import org.apache.commons.net.ftp.FTPSClient;

import com.uhg.ewp.common.gotcha.contentsource.exception.SourceConfigurationException;
import com.uhg.ewp.common.gotcha.util.log.Lg;

/**
 * For FTP over SSL (not SFTP, which is FTP over SSH tunnel)
 * 
 * @author cmuell7
 *
 */

public class FTPSSLSource extends FTPSource
{
    String sourceName = "FTP-SSL";
    

    @Override
    FTPClient getFTPClient()
    {
        try { 
            return new FTPSClient();
        } catch (NoSuchAlgorithmException noa) {
            throw new SourceConfigurationException("FTPSSLSRC-NOALG",Lg.err("Unable to instantiate FTPSClient due to algorithm exception",noa),noa);
        }
    }
    

    public String getSourceName()
    {
        return sourceName;
    }

    public void setSourceName(String sourceName)
    {
        this.sourceName = sourceName;
    }

}
