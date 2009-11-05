package org.webgen.core;

public class WebgenUser 
{
	protected String user;
	protected String pass;
	protected boolean authenticated;
	protected String server; // authentication server
	protected String fulluser; // i.e. for ldap fully-qualified name
	public boolean isAuthenticated() {
		return authenticated;
	}
	public void setAuthenticated(boolean authenticated) {
		this.authenticated = authenticated;
	}
	public String getFulluser() {
		return fulluser;
	}
	public void setFulluser(String fulluser) {
		this.fulluser = fulluser;
	}
	public String getPass() {
		return pass;
	}
	public void setPass(String pass) {
		this.pass = pass;
	}
	public String getServer() {
		return server;
	}
	public void setServer(String server) {
		this.server = server;
	}
	public String getUser() {
		return user;
	}
	public void setUser(String user) {
		this.user = user;
	}

}
