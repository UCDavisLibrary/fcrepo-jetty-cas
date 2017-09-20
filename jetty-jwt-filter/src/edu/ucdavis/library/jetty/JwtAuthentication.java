package edu.ucdavis.library.jetty;

import org.eclipse.jetty.security.UserAuthentication;

/**
 * CAS-specific user authentication.
 *
 * @author Marvin S. Addison
 */
public class JwtAuthentication extends UserAuthentication {

    /** CAS authenticator that produced this authentication. */
    private final JwtAuthenticator authenticator;

    /** JWT ticket that was successfully validated to permit authentication. */
    private final String jwtToken;


    /**
     * Creates a new instance.
     *
     * @param authenticator The authenticator that produced this authentication.
     * @param ticket The CAS ticket that was successfully validated to permit authentication.
     * @param assertion The CAS assertion produced from successful ticket validation.
     */
    public JwtAuthentication(final JwtAuthenticator authenticator, final String jwtToken, final JwtPrincipal principle) {
        super(authenticator.getAuthMethod(), new JwtUserIdentity(principle));
        this.authenticator = authenticator;
        this.jwtToken = jwtToken;
    }

    /** @return The Jwt Token that was successfully validated to permit authentication. */
    public String getTicket() {
        return jwtToken;
    }

    @Override
    public void logout() {
        super.logout();
        this.authenticator.clearCachedAuthentication(jwtToken);
    }
}