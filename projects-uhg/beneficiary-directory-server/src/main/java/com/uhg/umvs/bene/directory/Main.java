package com.uhg.umvs.bene.directory;

import org.apache.directory.server.core.CoreSession;
import org.apache.directory.server.core.DefaultDirectoryService;
import org.apache.directory.server.core.DirectoryService;
import org.apache.directory.server.core.entry.ServerEntry;
import org.apache.directory.server.core.partition.Partition;
import org.apache.directory.server.core.partition.impl.btree.jdbm.JdbmPartition;
import org.apache.directory.server.ldap.LdapService;
import org.apache.directory.server.protocol.shared.SocketAcceptor;
import org.apache.directory.shared.ldap.name.LdapDN;

public class Main {

	private static final String ROOT_DN = "dc=uhc,dc=com";

	public static void main(String[] args) {
		DirectoryService directoryService;

		SocketAcceptor socketAcceptor;
		LdapService ldapService;

		directoryService = new DefaultDirectoryService();
		directoryService.setShutdownHookEnabled(true);

		socketAcceptor = new SocketAcceptor(null);
		ldapService = new LdapService();
		ldapService.setSocketAcceptor(socketAcceptor);
		ldapService.setDirectoryService(directoryService);
		ldapService.setIpPort(10389);

		Partition partition = new JdbmPartition();
		partition.setId("uhc");
		partition.setSuffix(ROOT_DN);

		// startup
		try {
			directoryService.addPartition(partition);
			directoryService.startup();
			ldapService.start();

			createDomain(directoryService);
	                System.out.println("LDAP SERVICE STARTED.");

		} catch (Exception ex) {
			ex.printStackTrace();
		}

	}

	private static void createDomain(DirectoryService directoryService)
			throws Exception {
		LdapDN dn = new LdapDN(ROOT_DN);
		CoreSession adminSession = directoryService.getAdminSession();
		if (!adminSession.exists(dn)) {
			ServerEntry entry = directoryService.newEntry(dn);
			entry.add("objectClass", new String[] { "top", "domain",
					"extensibleObject" });
			entry.add("dc", new String[] { "uhc" });
			adminSession.add(entry);
		}
	}

}
