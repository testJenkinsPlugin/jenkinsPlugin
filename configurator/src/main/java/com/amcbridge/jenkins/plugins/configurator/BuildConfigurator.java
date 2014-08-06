package com.amcbridge.jenkins.plugins.configurator;

import hudson.Extension;
import hudson.model.RootAction;

@Extension
public final class BuildConfigurator implements RootAction {

     public String getDisplayName() {
            return "Build Configurator";
        }

        public String getIconFileName() {
            return "/plugin/configurator/icons/system_config_services.png";
        }

        public String getUrlName() {
            return "BuildConfigurator";
        }
}
