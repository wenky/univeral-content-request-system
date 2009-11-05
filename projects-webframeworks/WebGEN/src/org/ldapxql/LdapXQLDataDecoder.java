package org.ldapxql;import java.util.HashMap;import javax.naming.directory.DirContext;public interface LdapXQLDataDecoder {  // interface for encode/decode custom classes. convert provides:  // - DirContext: in case the encoding/decoding is dependent on another field in the object  //               or some hardcoded path/location in Ldap. Note that this should be SubContexted  //               if the Context needs to be changed in some way.  // - ConverterData: provides any stored data (either set up in java code or in the LdapXQL document),  //                  such as a DES key, or a keyword, or a mask.  // - String Data: the data to be converted.  // it returns any type of Object, since the decode process may involve deserialization.  public Object decode(DirContext a_ctx, HashMap a_hshConverterData, String a_sData);}