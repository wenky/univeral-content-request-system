package test;

import com.documentum.fc.common.DfException;
import com.documentum.fc.tools.RegistryPasswordUtils;

public class TestDfcEncryptDecrypt {
    
    public static String dfcEncrypt(String s) throws DfException
    {
        String enc = RegistryPasswordUtils.encrypt(s);
        return enc;
    }

    public static String dfcDecrypt(String s) throws DfException
    {
        String dec = RegistryPasswordUtils.decrypt(s);
        return dec;
    }

}
