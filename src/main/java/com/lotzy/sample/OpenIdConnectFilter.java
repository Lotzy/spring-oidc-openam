package com.lotzy.sample;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.codehaus.jackson.map.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.jwt.Jwt;
import org.springframework.security.jwt.JwtHelper;
import org.springframework.security.oauth2.client.OAuth2RestOperations;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.common.exceptions.InvalidTokenException;
import org.springframework.security.oauth2.common.exceptions.OAuth2Exception;
import org.springframework.security.web.authentication.AbstractAuthenticationProcessingFilter;

import com.lotzy.sample.security.OpenIdConnectUserDetails;

/**
 * <pre>
 * Title: OpenIdConnectFilter class
 * Description: Custom Authentication processing filter extension to extract user authentication info from id_token received when
 * access token URI is called
 * </pre>
 *
 * @author Lotzy
 * @version 1.0
 */
public class OpenIdConnectFilter extends AbstractAuthenticationProcessingFilter {

	private static final Logger log = LoggerFactory.getLogger(OpenIdConnectFilter.class);

	private OAuth2RestOperations restTemplate;

	/**
	 * @param defaultFilterProcessesUrl
	 */
	protected OpenIdConnectFilter(String defaultFilterProcessesUrl) {
		super(defaultFilterProcessesUrl);
		setAuthenticationManager(new NoopAuthenticationManager());
	}

	/*
	 * (non-Javadoc)
	 *
	 * @see org.springframework.security.web.authentication.
	 * AbstractAuthenticationProcessingFilter#attemptAuthentication(javax.servlet.
	 * http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
	 */
	@Override
	public Authentication attemptAuthentication(HttpServletRequest httpReq, HttpServletResponse httpResp) throws AuthenticationException, IOException, ServletException {
		OAuth2AccessToken accessToken;
		try {
			accessToken = restTemplate.getAccessToken();
		}
		catch (final OAuth2Exception e) {
			throw new BadCredentialsException("Could not obtain access token", e);
		}
		try {
			final String idToken = accessToken.getAdditionalInformation().get("id_token").toString();
			final Jwt tokenDecoded = JwtHelper.decode(idToken);
			String tokenDecodedClaims = tokenDecoded.getClaims();
			log.info("Decoded token claims" + tokenDecodedClaims);

			final Map<String, Object> authInfo = new ObjectMapper().readValue(tokenDecodedClaims, Map.class);

			final OpenIdConnectUserDetails user = new OpenIdConnectUserDetails(authInfo);
			return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
		}
		catch (final InvalidTokenException e) {
			throw new BadCredentialsException("Could not obtain user details from token", e);
		}
	}

	public void setRestTemplate(OAuth2RestTemplate restTemplate) {
		this.restTemplate = restTemplate;
	}

	private static class NoopAuthenticationManager implements AuthenticationManager {
		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
			throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
		}
	}

}
