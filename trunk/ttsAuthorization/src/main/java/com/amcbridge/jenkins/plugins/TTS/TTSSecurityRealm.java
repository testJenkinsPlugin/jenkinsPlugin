package com.amcbridge.jenkins.plugins.TTS;

import org.acegisecurity.Authentication;
import org.acegisecurity.AuthenticationException;
import org.acegisecurity.AuthenticationManager;
import org.acegisecurity.BadCredentialsException;
import org.acegisecurity.GrantedAuthority;
import org.acegisecurity.providers.UsernamePasswordAuthenticationToken;
import org.acegisecurity.userdetails.UserDetails;
import org.acegisecurity.userdetails.UserDetailsService;
import org.acegisecurity.userdetails.UsernameNotFoundException;
import org.kohsuke.stapler.DataBoundConstructor;
import org.springframework.dao.DataAccessException;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.model.Hudson;
import hudson.security.SecurityRealm;
import javax.servlet.Filter;
import javax.servlet.FilterConfig;

public class TTSSecurityRealm extends SecurityRealm {

	private static String LOGIN_EXCEPTION = "Unexpected authentication type: ";

	@DataBoundConstructor
	public TTSSecurityRealm()
	{
		super();
	}

	@Override
	public boolean allowsSignup() {
		return false;
	}

	@Override
	public Filter createFilter(FilterConfig filterConfig) {
		Filter defaultFilter = super.createFilter(filterConfig);
		return new TTSServletFilter(defaultFilter);
	}

	@Override
	public SecurityComponents createSecurityComponents() {
		Hudson.getInstance().setDisableRememberMe(true);
		return new SecurityComponents(
				new AuthenticationManager() {
					public Authentication authenticate(Authentication authentication) throws AuthenticationException {
						try {
							if (TTSServiceConnection.checkUser(authentication.getName(), authentication.getCredentials().toString())) {
								return new UsernamePasswordAuthenticationToken(authentication.getPrincipal(), authentication.getCredentials(), 
										new GrantedAuthority[]{SecurityRealm.AUTHENTICATED_AUTHORITY});
							}
						} catch (Exception e) {
							e.printStackTrace();
						}
						throw new BadCredentialsException(LOGIN_EXCEPTION + authentication);
					}
				}, 
				new UserDetailsService() {
					public UserDetails loadUserByUsername(String username)
							throws UsernameNotFoundException, DataAccessException {
						throw new UsernameNotFoundException(username);
					}
				}
				);
	}

	@Extension
	public static final class DescriptorImpl extends Descriptor<SecurityRealm> {

		private static final String DISPLAY_NAME = "TTS";

		@Override
		public String getDisplayName() {
			return DISPLAY_NAME;
		}
	}
}