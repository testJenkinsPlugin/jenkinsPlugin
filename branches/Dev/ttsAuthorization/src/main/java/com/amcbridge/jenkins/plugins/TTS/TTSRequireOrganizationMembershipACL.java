package com.amcbridge.jenkins.plugins.TTS;

import java.util.LinkedList;
import java.util.List;

import jenkins.model.Jenkins;

import org.acegisecurity.Authentication;

import hudson.model.Item;
import hudson.model.Hudson;
import hudson.security.ACL;
import hudson.security.Permission;

public class TTSRequireOrganizationMembershipACL extends ACL {

	private final List<String> adminUserNameList;
	public static String SEPARATOR = ",";

	public TTSRequireOrganizationMembershipACL(String adminUserNames)
	{
		this.adminUserNameList = new LinkedList<String>();
		String[] parts = adminUserNames.split(SEPARATOR);

		for (String part : parts) {
			adminUserNameList.add(part.trim());
		}
	}

	public List<String> getAdminUserNames()
	{
		return adminUserNameList;
	}

	@Override
	public boolean hasPermission(Authentication a, Permission permission) {

		if (a == null)
			return false;

		String authenticatedUserName = a.getName();

		if (authenticatedUserName.equals(Jenkins.ANONYMOUS.getName()))
		{
			return false;
		}

		if (adminUserNameList.indexOf(authenticatedUserName)!=-1)
		{
			return true;
		}

		if (permission.equals(Hudson.ADMINISTER) ||
				permission.equals(Item.CREATE) || permission.equals(Item.DELETE))
		{
			return false;
		}

		return true;
	}
}
