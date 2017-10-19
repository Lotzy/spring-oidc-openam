package com.lotzy.sample;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Description;
import org.springframework.context.annotation.Profile;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.oauth2.client.OAuth2ClientContext;
import org.springframework.security.oauth2.client.OAuth2RestTemplate;
import org.springframework.security.oauth2.client.filter.OAuth2ClientContextFilter;
import org.springframework.security.oauth2.client.resource.OAuth2ProtectedResourceDetails;
import org.springframework.security.oauth2.client.token.grant.code.AuthorizationCodeResourceDetails;
import org.springframework.security.oauth2.common.AuthenticationScheme;
import org.springframework.security.oauth2.config.annotation.web.configuration.EnableOAuth2Client;
import org.springframework.security.web.authentication.LoginUrlAuthenticationEntryPoint;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;
import org.springframework.security.web.context.SecurityContextPersistenceFilter;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

/**
 * <pre>
 * Title: AppConfigurator class
 * Description: Spring application configuration with annotations
 * </pre>
 *
 * @author Lotzy
 * @version 1.0
 */
@Configuration
@EnableOAuth2Client
@EnableWebMvc
@EnableWebSecurity
@ComponentScan({ "com.lotzy.sample" })
public class AppConfigurator extends WebSecurityConfigurerAdapter {

	@Autowired
	private OAuth2ClientContextFilter oauth2ClientContextFilter;

	@Autowired
	private OAuth2ClientContext oauth2ClientContext;

	@Configuration
	@Profile("default")
	@PropertySource(value = { "classpath:application.properties" }, ignoreResourceNotFound = true)
	static class Defaults {
		// nothing needed here if you are only overriding property values
	}

	@Configuration
	@Profile("override")
	@PropertySource(value = { "classpath:application.properties", "classpath:application-override.properties" }, ignoreResourceNotFound = true)
	static class Overrides {
		// nothing needed here if you are only overriding property values
	}

	@Value("${oauth2.clientId}")
	private String clientId;

	@Value("${oauth2.clientSecret}")
	private String clientSecret;

	@Value("${oauth2.userAuthorizationUri}")
	private String userAuthorizationUri;

	@Value("${oauth2.accessTokenUri}")
	private String accessTokenUri;

	@Value("${oauth2.tokenName:authorization_code}")
	private String tokenName;

	@Value("${oauth2.scope}")
	private String scope;

	@Value("${oauth2.filterCallbackPath}")
	private String oauth2FilterCallbackPath;

	@Bean
	public OAuth2ProtectedResourceDetails openAM() {
		AuthorizationCodeResourceDetails details = new AuthorizationCodeResourceDetails();
		details.setId("openam-oauth-client");
		details.setClientId(clientId);
		details.setClientSecret(clientSecret);
		details.setUserAuthorizationUri(userAuthorizationUri);
		details.setAccessTokenUri(accessTokenUri);
		details.setTokenName(tokenName);
		String commaSeparatedScopes = scope;
		details.setScope(parseScopes(commaSeparatedScopes));
		// Defaults to use current URI
		/*
		 * If a pre-established redirect URI is used, it will need to be an absolute
		 * URI. To do so, it'll need to compute the URI from a request. The HTTP
		 * request object is available when you override
		 * OAuth2ClientAuthenticationProcessingFilter#attemptAuthentication().
		 *
		 * details.setPreEstablishedRedirectUri(
		 * env.getProperty("oauth2.redirectUrl")); details.setUseCurrentUri(false);
		 */
		details.setAuthenticationScheme(AuthenticationScheme.query);
		details.setClientAuthenticationScheme(AuthenticationScheme.form);
		return details;
	}

	private List<String> parseScopes(String commaSeparatedScopes) {
		List<String> scopes = new LinkedList<>();
		Collections.addAll(scopes, commaSeparatedScopes.split(","));
		return scopes;
	}

	@Bean
	public OAuth2RestTemplate openamOpenIdTemplate(OAuth2ClientContext clientContext) {
		return new OAuth2RestTemplate(openAM(), clientContext);
	}

	@Override
	public void configure(WebSecurity web) throws Exception {
		web.ignoring().antMatchers("/resources/**");
	}

	@Bean
	public OpenIdConnectFilter myFilter() {
		System.out.println("initialize filter");
		final OpenIdConnectFilter filter = new OpenIdConnectFilter("/rest/login");
		filter.setRestTemplate(openamOpenIdTemplate(oauth2ClientContext));
		return filter;
	}

	@Override
	protected void configure(HttpSecurity http) throws Exception {
	// @formatter:off
		http.antMatcher("/**").authorizeRequests().anyRequest()
				.authenticated().and().exceptionHandling()
				.authenticationEntryPoint(new LoginUrlAuthenticationEntryPoint("/rest/login")).and().logout()
				.logoutSuccessUrl("/").permitAll().and()
				.addFilterAfter(oauth2ClientContextFilter, SecurityContextPersistenceFilter.class)
				.addFilterBefore(myFilter(), BasicAuthenticationFilter.class);
	// @formatter:on
	}

	@Override
	protected AuthenticationManager authenticationManager() throws Exception {
		return new NoopAuthenticationManager();
	}

	private static class NoopAuthenticationManager implements AuthenticationManager {
		@SuppressWarnings("unused")
		@Override
		public Authentication authenticate(Authentication authentication) throws AuthenticationException {
			throw new UnsupportedOperationException("No authentication should be done with this AuthenticationManager");
		}
	}

	@Bean
	@Description("Enables ${...} expressions in the @Value annotations on fields of this configuration. Not needed if one is already available.")
	public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
		return new PropertySourcesPlaceholderConfigurer();
	}

}
