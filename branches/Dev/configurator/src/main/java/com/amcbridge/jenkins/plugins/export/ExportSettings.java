package com.amcbridge.jenkins.plugins.export;

import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import net.sf.json.JSONObject;

import org.kohsuke.stapler.StaplerRequest;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;

public class ExportSettings extends Builder
{
	@Override
	public Settings getDescriptor()
	{
		return (Settings)super.getDescriptor();
	}

	@Extension
	public static final class Settings extends BuildStepDescriptor<Builder>
	{
		private String url, login, password;

		public Settings()
		{
			load();
		}

		public String getUrl()
		{
			return url;
		}

		public void setUrl(String value)
		{
			url = value;
		}

		public String getLogin()
		{
			return login;
		}

		public void setLogin(String value)
		{
			login = value;
		}

		public String getPassword()
		{
			return password;
		}

		public void setPassword(String value)
		{
			password = value;
		}

		@Override
		public boolean isApplicable(Class<? extends AbstractProject> aClass)
		{
			return true;
		}

		@Override
		public String getDisplayName()
		{
			return BuildConfigurationManager.STRING_EMPTY;
		}

		@Override
		public boolean configure(StaplerRequest req, JSONObject formData) throws FormException
		{
			req.bindJSON(this, formData);
			save();
			return super.configure(req,formData);
		}

		public boolean isSettingsSet()
		{	
			if(url == null || url.isEmpty() || login == null ||
					login.isEmpty() || password == null || password.isEmpty() )
			{
				return false;
			}
			return true;
		}
	}
}