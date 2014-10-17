package com.amcbridge.jenkins.plugins.vsc;

public interface VersionControlSystem
{
	public VersionControlSystemResult doCommit(String filePath, String url, String login, String password);
}
