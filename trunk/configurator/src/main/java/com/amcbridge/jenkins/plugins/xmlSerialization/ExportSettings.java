package com.amcbridge.jenkins.plugins.xmlSerialization;

import com.amcbridge.jenkins.plugins.configurator.BuildConfigurationManager;
import hudson.Extension;
import hudson.model.AbstractProject;
import hudson.tasks.BuildStepDescriptor;
import hudson.tasks.Builder;
import java.util.List;
import net.sf.json.JSONObject;
import org.kohsuke.stapler.StaplerRequest;

public class ExportSettings extends Builder {

    @Override
    public Settings getDescriptor() {
        return (Settings) super.getDescriptor();
    }

    @Extension
    public static final class Settings extends BuildStepDescriptor<Builder> {

        private String typeSCM4Config, url, login, password, commitMessage,
                localGitRepoPath, branch;

        public Settings() {
            load();
        }

        public String getCommitMessage() {
            return commitMessage;
        }

        public void setCommitMessage(String value) {
            commitMessage = value;
        }

        public String getTypeSCM4Config() {
            return typeSCM4Config;
        }

        public void setTypeSCM4Config(String value) {
            typeSCM4Config = value;
        }

        public String getUrl() {
            return url;
        }

        public void setUrl(String value) {
            url = value;
        }

        public String getBranch() {
            return branch;
        }

        public void setBranch(String value) {
            branch = value;
        }

        public String getLogin() {
            return login;
        }

        public void setLogin(String value) {
            login = value;
        }

        public String getPassword() {
            return password;
        }

        public void setPassword(String value) {
            password = value;
        }

        public String getLocalGitRepoPath() {
            return localGitRepoPath;
        }

        public void setLocalGitRepoPath(String value) {
            localGitRepoPath = value;
        }

        @Override
        public boolean isApplicable(Class<? extends AbstractProject> aClass) {
            return true;
        }

        @Override
        public String getDisplayName() {
            return BuildConfigurationManager.STRING_EMPTY;
        }

        @Override
        public boolean configure(StaplerRequest req, JSONObject formData) throws FormException {
            req.bindJSON(this, formData);
            save();
            return super.configure(req, formData);
        }

        public boolean isSettingsSet() {
            if (url == null || url.isEmpty() || login == null || commitMessage == null
                    || login.isEmpty() || password == null || password.isEmpty()) {
                return false;
            }
            return true;
        }

        public List<String> getSCM() {
            return BuildConfigurationManager.getSCM();
        }

    }
}
