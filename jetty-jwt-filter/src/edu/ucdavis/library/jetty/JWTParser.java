package edu.ucdavis.library.jetty;

import java.io.UnsupportedEncodingException;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.Claim;
import com.auth0.jwt.interfaces.DecodedJWT;

public class JWTParser {

	public static void main(String[] args) {
		String token = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJmb28iOiJiYXIiLCJpYXQiOjE1MDU4NTY2MjAsImlzcyI6ImxpYnJhcnkudWNkYXZpcy5lZHUifQ.wMo-uE1FWI4iWEshOlbyI1KduDODcBOX5c_RgOd7f94";
		try {
		    Algorithm algorithm = Algorithm.HMAC256("testtestest");
		    JWTVerifier verifier = JWT.require(algorithm)
		        .withIssuer("library.ucdavis.edu")
		        .build(); //Reusable verifier instance
		    DecodedJWT jwt = verifier.verify(token);
		    DecodedJWT jwt2 = JWT.decode(token);
		    Claim c = jwt2.getClaim("foo");
		    System.out.println("jwt decoded!");
		} catch (UnsupportedEncodingException exception){
		    //UTF-8 encoding not supported
			System.out.println("jwt failed :( 1");
		} catch (JWTVerificationException exception){
		    //Invalid signature/claims
			System.out.println("jwt failed :( 2");
		}
	}
}
