package com.amcbridge.jenkins.plugins.TTS;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.commons.lang.StringUtils;
import org.kohsuke.stapler.DataBoundConstructor;

import hudson.Extension;
import hudson.model.Descriptor;
import hudson.security.ACL;
import hudson.security.AuthorizationStrategy;

public class TTSAuthorizationStrategy extends AuthorizationStrategy {

	private final TTSRequireOrganizationMembershipACL rootACL;

	@DataBoundConstructor
	public TTSAuthorizationStrategy(String adminUserNames)
	{
		super();
		rootACL = new TTSRequireOrganizationMembershipACL(adminUserNames);
	}

	@Override
	public ACL getRootACL() {
		return rootACL;
	}

	@Override
	public Collection<String> getGroups() {
		return new ArrayList<String>(0);
	}

	public String getAdminUserNames()
	{
		return StringUtils.join(rootACL.getAdminUserNames(), TTSRequireOrganizationMembershipACL.SEPARATOR);
	}

	@Extension
	public static final class DescriptorImpl extends
	Descriptor<AuthorizationStrategy> {

		private static final String DISPLAY_NAME = "TTS Authorization Strategy";

		public String getDisplayName() {
			return DISPLAY_NAME;
		}
	}
}