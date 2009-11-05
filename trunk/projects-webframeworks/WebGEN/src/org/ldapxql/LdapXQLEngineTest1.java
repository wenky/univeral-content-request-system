package org.ldapxql;import javax.naming.NamingException;public class LdapXQLEngineTest1 {  public static void main(String[] args)  {/*    // select    // - works!    // - can't get classname with .getClass()...    // - DN is not absolute, how make absolute? add searchbase?    String      lxm =  "<LdapXQL>";      lxm +=   "<config_info>";      lxm +=     "<provider_url>ldap://imobile2:389</provider_url>";      lxm +=     "<security_authentication>simple</security_authentication>";      lxm +=     "<security_principal>uid=cmueller, ou=People, o=deloitte.com</security_principal>";      lxm +=     "<security_credentials>..cmueller</security_credentials>";      lxm +=   "</config_info>";      lxm +=   "<select>";      lxm +=     "<searchbase>ou=People, o=deloitte.com</searchbase>";      lxm +=     "<filter>(uid=*)</filter>";      lxm +=     "<attrlist>";      lxm +=       "<attr name=\"cn\" action=\"literal\"/>";      lxm +=       "<attr name=\"givenname\" action=\"literal\" classname=\"tobecont'd\"/>";      lxm +=       "<attr name=\"userpassword\" action=\"literal\"/>";      lxm +=       "<attr name=\"objectclass\" action=\"literal\"/>";      lxm +=       "<attr name=\"sn\" action=\"literal\"/>";      lxm +=     "</attrlist>";      lxm +=   "</select>";      lxm += "</LdapXQL>";*//*    // select    // - base64 encode/decode test    String      lxm =  "<LdapXQL>";      lxm +=   "<config_info>";      lxm +=     "<provider_url>ldap://imobile2:389</provider_url>";      lxm +=     "<security_authentication>simple</security_authentication>";      lxm +=     "<security_principal>uid=cmueller, ou=People, o=deloitte.com</security_principal>";      lxm +=     "<security_credentials>..cmueller</security_credentials>";      lxm +=   "</config_info>";      lxm +=   "<select>";      lxm +=     "<searchbase>ou=People, o=deloitte.com</searchbase>";      lxm +=     "<filter>(uid=blahblah)</filter>";      lxm +=     "<attrlist>";      lxm +=       "<attr name=\"cn\" action=\"decode\"/>";      lxm +=       "<attr name=\"givenname\" action=\"literal\" classname=\"tobecont'd\"/>";      lxm +=       "<attr name=\"userpassword\" action=\"literal\"/>";      lxm +=       "<attr name=\"objectclass\" action=\"literal\"/>";      lxm +=       "<attr name=\"sn\" action=\"decode\"/>";      lxm +=     "</attrlist>";      lxm +=   "</select>";      lxm += "</LdapXQL>";*//*    // insert    // - need "add" permission    // - all systems go!    // - failed insertion will throw an exception...    String      lxm =  "<LdapXQL>";      lxm +=   "<config_info>";      lxm +=     "<provider_url>ldap://imobile2:389</provider_url>";      lxm +=     "<security_authentication>simple</security_authentication>";      lxm +=     "<security_principal>uid=admin, ou=Administrators, ou=TopologyManagement, o=NetscapeRoot</security_principal>";      lxm +=     "<security_credentials>admin</security_credentials>";      lxm +=   "</config_info>";      lxm +=   "<insert>";      lxm +=     "<name>uid=blahblah, ou=People, o=deloitte.com</name>";      lxm +=     "<attrlist>";      lxm +=       "<attr name=\"uid\" encodeaction=\"literal\">";      lxm +=         "<value>blahblah</value>";      lxm +=       "</attr>";      lxm +=       "<attr name=\"userpassword\" encodeaction=\"literal\">";      lxm +=         "<value>..blahblah</value>";      lxm +=       "</attr>";      lxm +=       "<attr name=\"cn\" encodeaction=\"encode\">";      lxm +=         "<value>eric carr</value>";      lxm +=       "</attr>";      lxm +=       "<attr name=\"givenname\" encodeaction=\"literal\">";      lxm +=         "<value>eric</value>";      lxm +=       "</attr>";      lxm +=       "<attr name=\"sn\" encodeaction=\"encode\">";      lxm +=         "<value>blahblah2</value>";      lxm +=       "</attr>";      lxm +=       "<attr name=\"objectclass\" encodeaction=\"literal\">";      lxm +=         "<value>top</value>";      lxm +=         "<value>person</value>";      lxm +=         "<value>organizationalPerson</value>";      lxm +=         "<value>inetOrgPerson</value>";      lxm +=       "</attr>";      lxm +=     "</attrlist>";      lxm +=   "</insert>";      lxm += "</LdapXQL>";*//*    // update    String      lxm =  "<LdapXQL>";      lxm +=   "<config_info>";      lxm +=     "<provider_url>ldap://imobile2:389</provider_url>";      lxm +=     "<security_authentication>simple</security_authentication>";      lxm +=     "<security_principal>uid=admin, ou=Administrators, ou=TopologyManagement, o=NetscapeRoot</security_principal>";      lxm +=     "<security_credentials>admin</security_credentials>";      lxm +=   "</config_info>";      lxm +=   "<encode_decode_data>";      lxm +=     "<EncryptionAlgorithm decodeaction=\"literal\">DESede</EncryptionAlgorithm>";      lxm +=     "<EncryptionKey decodeaction=\"decodedeserialize\">rO0ABXNyACFjb20uc3VuLmNyeXB0by5wcm92aWRlci5ERVNlZGVLZXkPqx5HbQlugwIAAVsAA2tleXQAAltCeHB1cgACW0Ks8xf4BghU4AIAAHhwAAAAGBk7pAhA98sjUme5kTTvzZEZO6QIQPfLIw==</EncryptionKey>";      lxm +=   "</encode_decode_data>";      lxm +=   "<update>";      lxm +=     "<name>uid=blahblah, ou=People, o=deloitte.com</name>";      lxm +=     "<attrlist>";      lxm +=       "<attr name=\"mail\" operation=\"replace\" encodeaction=\"encryptencode\">";      lxm +=         "<value>doofus@dc.com</value>";      lxm +=       "</attr>";      lxm +=     "</attrlist>";      lxm +=   "</update>";      lxm += "</LdapXQL>";*//*    // delete    String      lxm =  "<LdapXQL>";      lxm +=   "<config_info>";      lxm +=     "<provider_url>ldap://imobile2:389</provider_url>";      lxm +=     "<security_authentication>simple</security_authentication>";      lxm +=     "<security_principal>uid=admin, ou=Administrators, ou=TopologyManagement, o=NetscapeRoot</security_principal>";      lxm +=     "<security_credentials>admin</security_credentials>";      lxm +=   "</config_info>";      lxm +=   "<delete>";      lxm +=     "<name>uid=bmccormick, ou=People, o=deloitte.com</name>";      lxm +=   "</delete>";      lxm += "</LdapXQL>";*/    // select    // - base64 encode/decode test    String      lxm =  "<LdapXQL>";      lxm +=   "<config_info>";      lxm +=     "<context_factory>com.sun.jndi.ldap.LdapCtxFactory</context_factory>";      lxm +=     "<provider_url>ldap://usscantadv08:389</provider_url>";//      lxm +=     "<provider_url>ldap://192.168.100.13:7000</provider_url>";a      lxm +=     "<security_authentication>simple</security_authentication>";//      lxm +=     "<security_principal>uid=admin, ou=Administrators, ou=TopologyManagement, o=NetscapeRoot</security_principal>";//      lxm +=     "<security_principal>cn=directory manager</security_principal>";//      lxm +=     "<security_credentials>dirmanager</security_credentials>";//      lxm +=     "<security_principal>uid=cmueller, ou=People, o=deloitte.com</security_principal>";//      lxm +=     "<security_credentials>..cmueller</security_credentials>";      lxm +=     "<security_principal>uid=a, ou=People, o=deloitte.com</security_principal>";      lxm +=     "<security_credentials>a</security_credentials>";      lxm +=   "</config_info>";/*    lxm +=   "<encode_decode_data>";      lxm +=     "<EncryptionAlgorithm decodeaction=\"literal\">DESede</EncryptionAlgorithm>";      lxm +=     "<EncryptionKey decodeaction=\"decodedeserialize\">rO0ABXNyACFjb20uc3VuLmNyeXB0by5wcm92aWRlci5ERVNlZGVLZXkPqx5HbQlugwIAAVsAA2tleXQAAltCeHB1cgACW0Ks8xf4BghU4AIAAHhwAAAAGBk7pAhA98sjUme5kTTvzZEZO6QIQPfLIw==</EncryptionKey>";      lxm +=   "</encode_decode_data>";*/      lxm += "</LdapXQL>";    String      mxm =  "<LdapXQL>";      mxm +=   "<select>";      mxm +=     "<searchbase>uid=cmueller, ou=People, o=deloitte.com</searchbase>";      mxm +=     "<filter>(uid=*)</filter>";      mxm +=     "<attrlist>";      mxm +=       "<attr name=\"cn\" action=\"literal\"/>";//      mxm +=       "<attr name=\"mail\" action=\"decodedecrypt\"/>";      mxm +=       "<attr name=\"givenname\" action=\"literal\"/>";      mxm +=       "<attr name=\"objectclass\" action=\"literal\"/>";      mxm +=       "<attr name=\"sn\" action=\"literal\"/>";      mxm +=     "</attrlist>";      mxm +=   "</select>";      mxm += "</LdapXQL>";    // setup...    SunLdapXQLEngine slxe = new SunLdapXQLEngine();    try {      slxe.setConfigurationWithXml(lxm);    } catch (NamingException ne) {      System.err.println("NamingException explanation: " + ne.getExplanation());      System.err.println(" ne toString: " + ne);      ne.printStackTrace();    } catch (java.io.InvalidClassException ice) {      System.err.println("ICE message: " + ice.getMessage());      System.err.println("  toString: " + ice);      ice.printStackTrace();    } catch (ClassNotFoundException cnfe) {      System.err.println("ClassNotFoundException: " + cnfe);      cnfe.printStackTrace();    } catch (Exception e) {      System.err.println("Exception class: " + e.getClass());      System.err.println("  toString: " + e);      e.printStackTrace();    }    // connect...    try {      slxe.Connect();    } catch (NamingException ne) {      System.err.println("NamingException explanation: " + ne.getExplanation());      System.err.println(" ne toString: " + ne);      ne.printStackTrace();    } catch (java.io.InvalidClassException ice) {      System.err.println("ICE message: " + ice.getMessage());      System.err.println("  toString: " + ice);      ice.printStackTrace();    } catch (ClassNotFoundException cnfe) {      System.err.println("ClassNotFoundException: " + cnfe);      cnfe.printStackTrace();    } catch (Exception e) {      System.err.println("Exception class: " + e.getClass());      System.err.println("  toString: " + e);      e.printStackTrace();    }    // execute statment    try {      String s = slxe.ProcessLdapXQLtoXML(mxm);      System.out.println("Ldap out: "+s);    } catch (NamingException ne) {      System.err.println("NamingException explanation: " + ne.getExplanation());      System.err.println(" ne toString: " + ne);      ne.printStackTrace();    } catch (java.io.InvalidClassException ice) {      System.err.println("ICE message: " + ice.getMessage());      System.err.println("  toString: " + ice);      ice.printStackTrace();    } catch (ClassNotFoundException cnfe) {      System.err.println("ClassNotFoundException: " + cnfe);      cnfe.printStackTrace();    } catch (Exception e) {      System.err.println("Exception class: " + e.getClass());      System.err.println("  toString: " + e);      e.printStackTrace();    }  }}