package edu.ucdavis.library.jetty;

import org.eclipse.jetty.server.UserIdentity;

import javax.security.auth.Subject;
import java.security.Principal;

/**
 * CAS user identity backed by assertion data.
 *
 * @author Marvin S. Addison
 */
public class JwtUserIdentity implements UserIdentity {

    private JwtPrincipal principal;


    /**
     * Creates a new instance from a CAS assertion containing principal information.
     *
     * @param assertion CAS assertion resulting from successful ticket validation.
     * @param roleAttribute Principal attribute containing role data.
     */
    public JwtUserIdentity(final JwtPrincipal principal) {
        this.principal = principal;
    }

    public Subject getSubject() {
        final Subject subject = new Subject();
        subject.getPrincipals().add(principal);
        return subject;
    }

    public Principal getUserPrincipal() {
        return principal;
    }

    public boolean isUserInRole(final String role, final Scope scope) {
        return false;
    }

    @Override
    public String toString() {
        return principal.getName();
    }
}