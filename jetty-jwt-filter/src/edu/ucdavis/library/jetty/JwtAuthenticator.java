package edu.ucdavis.library.jetty;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.server.Authentication;
import org.eclipse.jetty.util.component.AbstractLifeCycle;
//import org.jasig.cas.client.Protocol;
//import org.jasig.cas.client.util.CommonUtils;
//import org.jasig.cas.client.util.ReflectUtils;
//import org.jasig.cas.client.validation.AbstractCasProtocolUrlBasedTicketValidator;
//import org.jasig.cas.client.validation.AbstractUrlBasedTicketValidator;
//import org.jasig.cas.client.validation.Assertion;
//import org.jasig.cas.client.validation.TicketValidator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.lang.ref.WeakReference; 
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

/**
 * Jetty authenticator component for container-managed CAS authentication.
 * <p><em>NOTE:</em> This component does not support CAS gateway mode.</p>
 *
 * @author Marvin S. Addison
 * @since 3.4.2
 */
public class JwtAuthenticator extends AbstractLifeCycle implements Authenticator {

    private static final String AUTH_HEADER_KEY = "Authorization";
    private static final String AUTH_HEADER_VALUE_PREFIX = "Bearer "; // with trailing space to separate token

    private static final int STATUS_CODE_UNAUTHORIZED = 401;
	
    /** Name of authentication method provided by this authenticator. */
    public static final String AUTH_METHOD = "JWT";

    /** Session attribute used to cache CAS authentication data. */
    private static final String CACHED_AUTHN_ATTRIBUTE = "edu.ucdavis.library.jetty.Authentication";

    /** Logger instance. */
    private final Logger logger = LoggerFactory.getLogger(JwtAuthenticator.class);

    /** Map of tickets to sessions. */
    private final ConcurrentMap<String, WeakReference<HttpSession>> sessionMap =
            new ConcurrentHashMap<String, WeakReference<HttpSession>>();

    /** CAS ticket validator component. */
    private JWTParser jwtParser = new JWTParser();


    private String issuer;

    private String secret;


    public String getIssuer() {
		return issuer;
	}


	public void setIssuer(String issuer) {
		this.issuer = issuer;
	}


	public String getSecret() {
		return secret;
	}


	public void setSecret(String secret) {
		this.secret = secret;
	}


    public void setConfiguration(final AuthConfiguration configuration) {
        // Nothing to do
        // All configuration must be via CAS-specific setter methods
    	
    		logger.info("Setting jwt config secret={} issuer={}", secret, issuer);
    	
    		try {
			jwtParser.init(secret, issuer);
		} catch (IllegalArgumentException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (UnsupportedEncodingException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }


    public String getAuthMethod() {
        return AUTH_METHOD;
    }

    public void prepareRequest(final ServletRequest request) {
        // Nothing to do
    }

    public Authentication validateRequest(
            final ServletRequest servletRequest, final ServletResponse servletResponse, final boolean mandatory)
            throws ServerAuthException {

        final HttpServletRequest request = (HttpServletRequest) servletRequest;
        
        JwtAuthentication authentication = fetchCachedAuthentication(request);
        if (authentication != null) {
        		logger.debug("jwt sesssion cached");
            return authentication;
        }

        String jwt = getBearerToken( request );
        logger.debug("jwt={}", jwt);
        
        if (jwt != null ) {
            try {
                logger.debug("Attempting to validate {}", jwt);
                final DecodedJWT decodedJwt = jwtParser.verify(jwt);
                
                if( decodedJwt == null ) {
                		return Authentication.UNAUTHENTICATED;
                }
                
                Claim username = decodedJwt.getClaims().get("username");
                logger.debug("Successfully authenticated {}", username.asString());
                
                JwtPrincipal principle = new JwtPrincipal(username.asString());
                authentication = new JwtAuthentication(this, jwt, principle);
                cacheAuthentication(request, authentication);
            } catch (Exception e) {
                throw new ServerAuthException("JWT ticket validation failed", e);
            }
        }
        if (authentication != null) {
        		logger.debug("authenticated request");
            return authentication;
        }
        
        	logger.debug("unauthenticated request");
        return Authentication.UNAUTHENTICATED;
    }

    public boolean secureResponse(
            final ServletRequest request,
            final ServletResponse response,
            final boolean mandatory,
            final Authentication.User user) throws ServerAuthException {
        return true;
    }

    @Override
    protected void doStart() throws Exception {
        if (jwtParser == null) {
            throw new RuntimeException("JwtParser cannot be null");
        }
    }

    protected void clearCachedAuthentication(final String ticket) {
        final WeakReference<HttpSession> sessionRef = sessionMap.remove(ticket);
        if (sessionRef != null && sessionRef.get() != null) {
            sessionRef.get().removeAttribute(CACHED_AUTHN_ATTRIBUTE);
        }
    }

    private void cacheAuthentication(final HttpServletRequest request, final JwtAuthentication authentication) {
        final HttpSession session = request.getSession(true);
        if (session != null) {
            session.setAttribute(CACHED_AUTHN_ATTRIBUTE, authentication);
            sessionMap.put(authentication.getTicket(), new WeakReference<HttpSession>(session));
        }
    }

    private JwtAuthentication fetchCachedAuthentication(final HttpServletRequest request) {
        final HttpSession session = request.getSession(false);
        if (session != null) {
            return (JwtAuthentication) session.getAttribute(CACHED_AUTHN_ATTRIBUTE);
        }
        return null;
    }
    
    /**
     * Get the bearer token from the HTTP request.
     * The token is in the HTTP request "Authorization" header in the form of: "Bearer [token]"
     */
    private String getBearerToken( HttpServletRequest request ) {
        String authHeader = request.getHeader( AUTH_HEADER_KEY );
        if ( authHeader != null && authHeader.startsWith( AUTH_HEADER_VALUE_PREFIX ) ) {
            return authHeader.substring( AUTH_HEADER_VALUE_PREFIX.length() );
        }
        return null;
    }

}
