package edu.ucdavis.library.jetty;

import java.security.Principal;

public class JwtPrincipal implements Principal {
	
	private String name;
	
	public JwtPrincipal(String name) {
		this.name = name;
	}
	
	public String getName() {
		return name;
	}
}
